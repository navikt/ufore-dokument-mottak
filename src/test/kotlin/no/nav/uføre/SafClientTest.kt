package no.nav.uføre

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.uføre.søknad.Sivilstatus
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
            when {
                request.url.encodedPath.endsWith("/graphql") -> respondOk(
                    content = javaClass.getResource("/saf_response_ett_dokument.json")!!.readText(),
                )
                request.url.encodedPath.endsWith("/rest/hentdokument/1/810899999/ORIGINAL") ->
                    respondError(HttpStatusCode.BadRequest)

                else -> error("Unhandled ${request.url}")
            }
        }

        val safClient = SafClient(HttpClient(mockEngine), tokenClient, meterRegistry)

        // when
        val journalpost = safClient.hentJournalpost("1")

        // then
        assertEquals("Søknad om uføretrygd", journalpost.dokumenter.first().tittel)
    }

    @Test
    suspend fun `henter flere dokumenter fra Saf og klarer å deserialisere disse`() {
        // given
        val mockEngine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("/graphql") -> respondOk(
                    content = javaClass.getResource("/saf_response_flere_dokumenter.json")!!.readText(),
                )
                request.url.encodedPath.endsWith("/rest/hentdokument/1/810878888/ORIGINAL") ->
                    respondError(HttpStatusCode.BadRequest)

                else -> error("Unhandled ${request.url}")
            }
        }

        val safClient = SafClient(HttpClient(mockEngine), tokenClient, meterRegistry)

        // when
        val journalpost = safClient.hentJournalpost("1")

        // then
        assertEquals(3, journalpost.dokumenter.size)
    }

    @Test
    suspend fun `henter søknad fra Saf og klarer å deserialisere dette`() {
        // given
        val mockEngine = MockEngine {
            respond(
                content = javaClass.getResource("/saf_søknad.json")!!.readText(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val safClient = SafClient(HttpClient(mockEngine), tokenClient, meterRegistry)
        val dokumenter = listOf(Dokument("2", "foo", "NAV 12-06.05", listOf(Dokumentvariant("Søknad om uføretrygd", Filtype.JSON, Variantformat.ORIGINAL))))
        val journalpost = Journalpost("1", "NAV_NO", dokumenter)

        // when
        val søknad = safClient.hentJsonDokument(journalpost)

        // then
        assertEquals(Sivilstatus.ENSLIG, søknad.data.data.hvaErDinSivilstatus)
    }
}
