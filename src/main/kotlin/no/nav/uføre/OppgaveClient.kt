package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

private val logger = KotlinLogging.logger { }

class OppgaveClient(private val httpClient: HttpClient, private val tokenClient: TokenClient) {
    private val url = System.getenv("OPPGAVE_URL") ?: "en lokal URL"

    suspend fun sjekkOmJournalpostHarOppgave(journalpostId: String) {
        val response: HttpResponse =
            httpClient.get("$url/api/v1/oppgaver/") {
                contentType(ContentType.Application.Json)
                parameter("statuskategori", "AAPEN")
                parameter("oppgavetype", "JFR")
                parameter("journalpostId", journalpostId)
                header(HttpHeaders.Authorization, "Bearer ${tokenClient.hentM2mToken(System.getenv("OPPGAVE_SCOPE"))}")
            }
        val body = response.bodyAsText()
        logger.info(teamLogsMarker) { body }
    }
}
