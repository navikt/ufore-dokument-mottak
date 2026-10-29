package no.nav.uføre

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TokenClient {
    val restClient: HttpClient = HttpClient(CIO)
    suspend fun hentM2mToken(target: String): String {
        val response = restClient.post("url") {
            contentType(ContentType.Application.Json)
            setBody(M2mRequest("<cluster>:<namespace>:<other-api-app-name>", "entra_id"))
        }
        return response.toString()
    }

    private data class M2mRequest(
        val target: String,
        val identity_provider: String,
    )
}
