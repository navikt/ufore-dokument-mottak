package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

class SafClient(private val httpClient: HttpClient, private val tokenClient: TokenClient, private val meterRegistry: MeterRegistry) {

    private val url = System.getenv("SAF_URL") ?: "en lokal URL"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun hentJournalpost(journalpostId: JournalpostId): SafResponse {
        val body = """query {
          journalpost(journalpostId: "${journalpostId.journalpostId}") {
            journalpostId
            dokumenter {
              dokumentInfoId
              tittel
              dokumentvarianter {
                variantformat
                filnavn
                filtype
              }
            }
          }
        }""".trimIndent()

        val response: HttpResponse = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${tokenClient.hentM2mToken()}")
            setBody(json.encodeToString(mapOf("query" to body)))
        }

        val responseBody = json.decodeFromString<SafResponse>(response.bodyAsText())
        val dokumentTittel = responseBody.data.journalpost.dokumenter.first().tittel ?: "Tittel mangler"
        meterRegistry.counter("ufore_dokument_tittel", "tittel", dokumentTittel).increment()

        logger.info { "Hentet journalpost body: $responseBody, status: ${response.status}" }
        return responseBody
    }

}

@JvmInline
value class JournalpostId(val journalpostId: String) {
    init {
        require(journalpostId.isNotEmpty()) { "JournalpostId kan ikke være tom" }
        require(journalpostId.toIntOrNull() != null) { "JournalpostId må være tall" }
    }
}

@Serializable
data class SafResponse(
    val data: SafData,
)

@Serializable
data class SafData(
    val journalpost: Journalpost,
)

@Serializable
data class Journalpost(
    val journalpostId: String,
    val dokumenter: List<Dokument>,
)

@Serializable
data class Dokument(
    val dokumentInfoId: String,
    val tittel: String?,
    val dokumentvarianter: List<Dokumentvariant>,
)

// TODO: slå opp i kodeverk istedenfor hardkoding https://kodeverk.ansatt.nav.no/kodeverk/Variantformater
enum class Variantformat {
    ARKIV,
    ORIGINAL,
    FULLVERSJON,
    SKANNING_META,
    BREVBESTILLING,
    PRODUKSJON
}

// TODO: slå opp i kodeverk istedenfor hardkoding https://kodeverk.ansatt.nav.no/kodeverk/Arkivfiltyper
enum class Filtype {
    PDFA,
    PDF,
    XML,
    AXML,
    JSON,
    RTF,
    DLF,
    XLSX,
    JPEG,
    PNG,
    TIFF,
    DXML
}

@Serializable
data class Dokumentvariant(
    val filnavn: String?,
    val filtype: Filtype,
    val variantformat: Variantformat,
)
