package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SafClient(private val httpClient: HttpClient, private val tokenClient: TokenClient) {

    val url = System.getenv("SAF_URL") ?: "en lokal URL"
    val log = KotlinLogging.logger {}
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
        val responseStatus = response.status

        log.info { "Hentet journalpost body: $responseBody, status: $responseStatus" }
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

enum class Variantformat {
    ARKIV,
    ORIGINAL,
    SLADDET,
    FULLVERSJON,
    SKANNING_META,
    BREVBESTILLING,
    PRODUKSJON,
    PRODUKSJON_DLF
}

@Serializable
data class Dokumentvariant(
    val filnavn: String?,
    val filtype: String,
    val variantformat: Variantformat,
)
