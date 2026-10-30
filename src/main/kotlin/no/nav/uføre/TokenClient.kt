package no.nav.uføre

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TokenClient {
    val restClient: HttpClient = HttpClient(CIO)
    suspend fun hentM2mToken(): String {
        val response = restClient.post(System.getenv("NAIS_TOKEN_ENDPOINT")) {
            contentType(ContentType.Application.Json)
            setBody(M2mRequest(System.getenv("SAF_SCOPE"), "entra_id"))
        }
        return response.body<AccessToken>().access_token
    }

    private data class M2mRequest(
        val target: String,
        val identity_provider: String,
    )

    private data class AccessToken(
        val access_token: String,
    )
}
