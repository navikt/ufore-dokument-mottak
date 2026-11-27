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
        return body.antallTreffTotalt > 0 && body.oppgaver.isNotEmpty()
    }

    suspend fun opprettOppgave(journalpost: Journalpost) {
        val request = OpprettOppgaveRequest(
            beskrivelse = "En testoppgave fra Team Uføre",
            kategorisering = Kategorisering(
                tema = Kode("UFO"),
                oppgavetype = Kode("JFR"),
                behandlingstema = avgjørBehandlingstema(journalpost),
                behandlingstype = avgjørBehandlingstype(journalpost)
            ),
            bruker = Bruker(
                ident = "", // TODO: hent ident fra journalpost
                type = "PERSON"
            ),
            arkivreferanse = Arkivreferanse(journalpost.journalpostId)
        )

        val response: HttpResponse =
            httpClient.post("$url/api/v2/oppgaver/") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${tokenClient.hentM2mToken(System.getenv("OPPGAVE_SCOPE"))}")
                setBody(request)
            }
    }

    private fun avgjørBehandlingstema(journalpost: Journalpost) = Kode(
        when {
            !journalpost.behandlingstema.isNullOrEmpty() -> journalpost.behandlingstema
            journalpost.dokumenter.first().brevkode in listOf("NAVe 22-06.01", "NAV 22-06.01") -> "ab0150"
            else -> ""
        }
    )

    private fun avgjørBehandlingstype(journalpost: Journalpost) = Kode(
        when {
            journalpost.dokumenter.first().brevkode in listOf("NAVe 90-00.08 K", "NAV 90-00.08 K") -> "ae0058"
            else -> ""
        }
    )
}

@Serializable
data class OpprettOppgaveRequest(
    val beskrivelse: String,
    val kategorisering: Kategorisering,
    val bruker: Bruker,
    val arkivreferanse: Arkivreferanse,
)

@Serializable
data class Kategorisering(
    val tema: Kode,
    val oppgavetype: Kode,
    val behandlingstema: Kode,
    val behandlingstype: Kode,
)

@Serializable
data class Kode(
    val kode: String,
)

@Serializable
data class Bruker(
    val ident: String,
    val type: String,
)

@Serializable
data class Arkivreferanse(
    val journalpostId: String,
)

@Serializable
data class OppgaveResponse(
    val antallTreffTotalt: Int,
    val oppgaver: List<Oppgave>
)

@Serializable
data class Oppgave(val id: Long)
