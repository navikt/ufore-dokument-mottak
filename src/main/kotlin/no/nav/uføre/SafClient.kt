package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Marker
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject
import no.nav.uføre.Variantformat.ORIGINAL
import no.nav.uføre.søknad.Søknad

private val logger = KotlinLogging.logger {}

class SafClient(private val httpClient: HttpClient, private val tokenClient: TokenClient, private val meterRegistry: MeterRegistry) {

    private val url = System.getenv("SAF_URL") ?: "en lokal URL"
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val tillatteTitler = listOf("Søknad om uføretrygd", "NAV 12-06.05 Søknad om uføretrygd", "Ettersendelse til søknad om uføretrygd", "Ettersendelse til klage", "Klage", "Anke", "Tittel mangler")
    private val teamLogsMarker = object : Marker {
        override fun getName(): String = "TEAM_LOGS"
    }

    suspend fun hentJournalpost(journalpostId: String): SafResponse {
        val body = """query {
          journalpost(journalpostId: "$journalpostId") {
            journalpostId
            kanal
            dokumenter {
              dokumentInfoId
              tittel
              brevkode
              dokumentvarianter {
                variantformat
                filnavn
                filtype
              }
            }
          }
        }""".trimIndent()

        val response: HttpResponse = httpClient.post("$url/graphql") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${tokenClient.hentM2mToken()}")
            setBody(json.encodeToString(mapOf("query" to body)))
        }

        val responseBody = json.decodeFromString<SafResponse>(response.bodyAsText())
        kategoriserSøknad(responseBody)

        if (responseBody.journalpost.dokumenter.any { it.dokumentvarianter.any { variant -> variant.variantformat == ORIGINAL } }) {
            hentSelveDokumentet(journalpostId, responseBody)
        }

        val dokumentTittel = responseBody.journalpost.dokumenter.firstOrNull()?.tittel ?: "Tittel mangler"
        val tillattTittel = if (dokumentTittel in tillatteTitler) dokumentTittel else "Annet dokument"
        meterRegistry.counter("ufore_dokument_tittel", "tittel", tillattTittel).increment()

        logger.info(teamLogsMarker) { "Hentet journalpost: $responseBody, status: ${response.status}" }
        return responseBody
    }

    private suspend fun hentSelveDokumentet(journalpostId: String, responseBody: SafResponse) {
        val response: HttpResponse =
            httpClient.post("$url/rest/hentdokument/$journalpostId/${responseBody.journalpost.dokumenter.first().dokumentInfoId}/${ORIGINAL}") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${tokenClient.hentM2mToken()}")
            }

        if (response.status == HttpStatusCode.OK) {
            val søknad = json.decodeFromString<Søknad>(response.bodyAsText())
            logger.info(teamLogsMarker) { "Hentet journalpost $journalpostId med sivilstatus ${søknad.data.data.hvaErDinSivilstatus}" }
        } else {
            logger.warn(teamLogsMarker) { "JournalpostId ${journalpostId}, status: ${response.status}" }
        }
    }

    private fun kategoriserSøknad(safResponse: SafResponse) {
        val journalpost = safResponse.journalpost
        val digitalFørstegangssøknad = journalpost.kanal == "NAV_NO" &&
                journalpost.dokumenter.any { dokument ->
                    dokument.brevkode == "NAV 12-06.05" &&
                            dokument.dokumentvarianter.any { variant ->
                                variant.variantformat == ORIGINAL
                            }
                }

        val scannetFørstegangssøknad =  journalpost.kanal == "SKAN_IM" &&
                journalpost.dokumenter.any { dokument ->
                    dokument.brevkode == "NAV 12-06.05" || dokument.brevkode == "NAVp 12-06.05"
                }

        val harVedlegg = journalpost.dokumenter.size > 1

        val kategori = when {
            digitalFørstegangssøknad -> SøknadsKategori.DIGITAL
            scannetFørstegangssøknad -> SøknadsKategori.SKANNET
            else -> SøknadsKategori.UKJENT
        }
        meterRegistry.counter("ufore_journalpost_mottatt", "kategori", kategori.name, "har_vedlegg", harVedlegg.toString()).increment()
    }
}

enum class SøknadsKategori {
    DIGITAL, SKANNET, UKJENT
}

@Serializable
data class SafResponse(
    @SerialName("data")
    @Serializable(with = JournalpostFromDataSerializer::class)
    val journalpost: Journalpost,
)

object JournalpostFromDataSerializer :
    JsonTransformingSerializer<Journalpost>(Journalpost.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.jsonObject.getValue("journalpost")
}

@Serializable
data class Journalpost(
    val journalpostId: String,
    val kanal: String?,
    val dokumenter: List<Dokument>,
)

@Serializable
data class Dokument(
    val dokumentInfoId: String,
    val tittel: String?,
    val brevkode: String?,
    val dokumentvarianter: List<Dokumentvariant>,
)

// TODO: slå opp i kodeverk istedenfor hardkoding https://kodeverk.ansatt.nav.no/kodeverk/Variantformater
enum class Variantformat {
    ARKIV,
    ORIGINAL,
    FULLVERSJON,
    SKANNING_META,
    BREVBESTILLING,
    PRODUKSJON
}

// TODO: slå opp i kodeverk istedenfor hardkoding https://kodeverk.ansatt.nav.no/kodeverk/Arkivfiltyper
enum class Filtype {
    PDFA,
    PDF,
    XML,
    AXML,
    JSON,
    RTF,
    DLF,
    XLSX,
    JPEG,
    PNG,
    TIFF,
    DXML
}

@Serializable
data class Dokumentvariant(
    val filnavn: String?,
    val filtype: Filtype,
    val variantformat: Variantformat,
)
