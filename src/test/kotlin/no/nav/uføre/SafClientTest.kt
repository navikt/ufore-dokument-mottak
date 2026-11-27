package no.nav.uføre

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import no.nav.uføre.søknad.Sivilstatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class SafClientTest {

    companion object {
        private val tokenClient = mockk<TokenClient>()

        @BeforeAll
        @JvmStatic
        fun init() {
            coEvery { tokenClient.hentM2mToken(any()) } returns "token"
        }
    }

    @Test
    suspend fun `henter ett dokument fra Saf og klarer å deserialisere dette`() {
        // given
        val mockEngine = MockEngine {
            respond(
                content = javaClass.getResource("/saf_response_ett_dokument.json")!!.readText(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val safClient = SafClient(HttpClient(mockEngine), tokenClient)

        // when
        val journalpost = safClient.hentJournalpost("1")

        // then
        assertEquals("Søknad om uføretrygd", journalpost.dokumenter.first().tittel)
    }

    @Test
    suspend fun `henter flere dokumenter fra Saf og klarer å deserialisere disse`() {
        // given
        val mockEngine = MockEngine {
            respond(
                content = javaClass.getResource("/saf_response_flere_dokumenter.json")!!.readText(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val safClient = SafClient(HttpClient(mockEngine), tokenClient)

        // when
        val journalpost = safClient.hentJournalpost("1")

        // then
        assertEquals(3, journalpost.dokumenter.size)
    }

    @Test
    suspend fun `henter søknad fra Saf og klarer å deserialisere denne`() {
        // given
        val mockEngine = MockEngine {
            respond(
                content = javaClass.getResource("/saf_søknad.json")!!.readText(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val safClient = SafClient(HttpClient(mockEngine), tokenClient)
        val journalpost = Json.decodeFromString<SafResponse>(javaClass.getResource("/saf_response_ett_dokument.json")!!.readText()).journalpost

        // when
        val søknad = safClient.hentJsonDokument(journalpost)

        // then
        assertEquals(Sivilstatus.GIFT_ELLER_PARTNER, søknad.data.data.hvaErDinSivilstatus)
        assertEquals(76, søknad.formRevision)
    }
}
