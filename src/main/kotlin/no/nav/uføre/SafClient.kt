package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject

private val logger = KotlinLogging.logger {}

class SafClient(private val httpClient: HttpClient, private val tokenClient: TokenClient, private val meterRegistry: MeterRegistry) {

    private val url = System.getenv("SAF_URL") ?: "en lokal URL"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun hentJournalpost(journalpostId: String): SafResponse {
        val body = """query {
          journalpost(journalpostId: "$journalpostId") {
            journalpostId
            behandlingstema
            behandlingstemanavn
            dokumenter {
              dokumentInfoId
              tittel
              brevkode
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
//        val dokumentTittel = responseBody.journalpost.dokumenter.first().tittel ?: "Tittel mangler"
//        meterRegistry.counter("ufore_dokument_tittel", "tittel", dokumentTittel).increment()
//        logger.info { "Hentet journalpost: $responseBody, status: ${response.status}" }
        return responseBody
    }

}

@Serializable
data class SafResponse(
    @SerialName("data")
    @Serializable(with = JournalpostFromDataSerializer::class)
    val journalpost: Journalpost,
)

object JournalpostFromDataSerializer :
    JsonTransformingSerializer<Journalpost>(Journalpost.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.jsonObject.getValue("journalpost")
}

@Serializable
data class Journalpost(
    val journalpostId: String,
    val behandlingstema: String?,
    val behandlingstemanavn: String?,
    val dokumenter: List<Dokument>,
)

@Serializable
data class Dokument(
    val dokumentInfoId: String,
    val tittel: String?,
    val brevkode: String?,
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
