package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SafClient(private val httpClient: HttpClient, private val tokenClient: TokenClient) {

    val url = System.getenv("SAF_URL") ?: "en lokal URL"
    val log = KotlinLogging.logger {}

    suspend fun hentJournalpost(journalpostId: JournalpostId) {
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
            setBody(body)
        }

        val responseBody = response.bodyAsText()
        val responseStatus = response.status

        log.info { "Hentet journalpost body: $responseBody, status: $responseStatus" }
    }

}

@JvmInline
value class JournalpostId(val journalpostId: String) {
    init {
        require(journalpostId.isNotEmpty()) { "JournalpostId cannot be empty" }
        require(journalpostId.toIntOrNull() != null) { "JournalpostId må være tall" }
    }
}
