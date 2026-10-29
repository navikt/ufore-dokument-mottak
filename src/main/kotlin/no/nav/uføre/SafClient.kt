package no.nav.uføre

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SafClient(private val httpClient: HttpClient) {

    val url = System.getenv("SAF_URL") ?: "en lokal URL"

    suspend fun hentJournalpost(journalpostId: JournalpostId) {
        val body = """query {
  journalpost(journalpostId: "$journalpostId") {
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
}"""
        val response: HttpResponse = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer TOKEN")
            setBody(body)
        }
    }

}

@JvmInline
value class JournalpostId(val journalpostId: String) {
    init {
        require(journalpostId.isNotEmpty()) { "JournalpostId cannot be empty" }
        require(journalpostId.toIntOrNull() != null) { "JournalpostId må være tall" }
    }
}
