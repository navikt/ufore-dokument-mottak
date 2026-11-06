package no.nav.uføre

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class SafClientTest {
    private val meterRegistry = SimpleMeterRegistry()

    companion object {
        private val tokenClient = mockk<TokenClient>()

        @BeforeAll
        @JvmStatic
        fun init() {
            coEvery { tokenClient.hentM2mToken() } returns "token"
        }
    }

    @Test
    suspend fun `henter ett dokument fra Saf og klarer å deserialisere dette`() {
        // given
        val mockEngine = MockEngine { request ->
            respond(
                content = javaClass.getResource("/saf_response_ett_dokument.json")!!.readText(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val safClient = SafClient(HttpClient(mockEngine), tokenClient, meterRegistry)

        // when
        val response: SafResponse = safClient.hentJournalpost("1")

        // then
        assertEquals("Søknad om uføretrygd", response.journalpost.dokumenter.first().tittel)
    }

    @Test
    suspend fun `henter flere dokumenter fra Saf og klarer å deserialisere disse`() {
        // given
        val mockEngine = MockEngine { request ->
            respond(
                content = javaClass.getResource("/saf_response_flere_dokumenter.json")!!.readText(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val safClient = SafClient(HttpClient(mockEngine), tokenClient, meterRegistry)

        // when
        val response: SafResponse = safClient.hentJournalpost("1")

        // then
        assertEquals(3, response.journalpost.dokumenter.size)
    }
}
