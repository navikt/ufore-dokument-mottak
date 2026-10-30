package no.nav.uføre

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class TokenClient {
    val restClient: HttpClient = HttpClient(CIO)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun hentM2mToken(): String {
        val response = restClient.post(System.getenv("NAIS_TOKEN_ENDPOINT")) {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(M2mRequest(System.getenv("SAF_SCOPE"), "entra_id")))
        }
        return json.decodeFromString<AccessToken>(response.bodyAsText()).access_token
    }

    @Serializable
    private data class M2mRequest(
        val target: String,
        val identity_provider: String,
    )

    @Serializable
    private data class AccessToken(
        val access_token: String,
    )
}
