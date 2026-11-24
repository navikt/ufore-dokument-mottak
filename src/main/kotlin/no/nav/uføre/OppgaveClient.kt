package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger { }

class OppgaveClient(private val httpClient: HttpClient, private val tokenClient: TokenClient) {
    private val url = System.getenv("OPPGAVE_URL") ?: "en lokal URL"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun harJournalpostOppgave(journalpostId: String): Boolean {
        val response: HttpResponse =
            httpClient.get("$url/api/v1/oppgaver/") {
                contentType(ContentType.Application.Json)
                parameter("statuskategori", "AAPEN")
                parameter("oppgavetype", "JFR")
                parameter("journalpostId", journalpostId)
                header(HttpHeaders.Authorization, "Bearer ${tokenClient.hentM2mToken(System.getenv("OPPGAVE_SCOPE"))}")
            }

        if (response.status != HttpStatusCode.OK) {
            throw KlientFeil("Kunne ikke hente oppgave fra oppgave-api. Status: ${response.status}")
        }

        val body = json.decodeFromString<OppgaveResponse>(response.bodyAsText())
        return body.antallTreffTotalt > 0
    }
}

@Serializable
data class OppgaveResponse(val antallTreffTotalt: Int)