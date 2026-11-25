package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import no.nav.uføre.Variantformat.ORIGINAL

private val logger = KotlinLogging.logger { }

class KodeverkClient(private val httpClient: HttpClient, private val tokenClient: TokenClient) {

    private val url = System.getenv("KODEVERK_URL") ?: "en lokal URL"

    suspend fun hentTemaSkjemaGjelder() {

        val response = httpClient.get("$url/api/v1/hierarki/TemaSkjemaGjelder/noder?spraak=nb") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${tokenClient.hentM2mToken(System.getenv("KODEVERK_SCOPE"))}")
            header("Nav-Consumer-Id", "ufore-dokument-mottak")
        }

        val body = response.bodyAsText()

        logger.info { "Fikk response fra kodeverk-api: $body" }
    }
}