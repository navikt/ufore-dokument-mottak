package no.nav.uføre

import io.ktor.client.HttpClient
import io.ktor.client.request.get

class SafClient(private val httpClient: HttpClient) {

    suspend fun hentJournalpost() {
        httpClient.get("blabla")
    }

}