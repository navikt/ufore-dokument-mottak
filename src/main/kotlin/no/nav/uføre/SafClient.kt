package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject
import no.nav.uføre.Variantformat.ORIGINAL
import no.nav.uføre.søknad.Søknad

private val logger = KotlinLogging.logger {}

class SafClient(private val httpClient: HttpClient, private val tokenClient: TokenClient) {
    private val url = System.getenv("SAF_URL") ?: "en lokal URL"
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun hentJournalpost(journalpostId: String): Journalpost {
        val body = """query {
          journalpost(journalpostId: "$journalpostId") {
            journalpostId
            behandlingstema
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
            header(HttpHeaders.Authorization, "Bearer ${tokenClient.hentM2mToken(System.getenv("SAF_SCOPE"))}")
            setBody(json.encodeToString(mapOf("query" to body)))
        }

        val responseBody = json.decodeFromString<SafResponse>(response.bodyAsText())
        return responseBody.journalpost
    }

    suspend fun hentJsonDokument(journalpost: Journalpost): Søknad {
        val journalpostId = journalpost.journalpostId
        val dokumentInfoId = journalpost.dokumenter.find { it.harJsonSøknad() }?.dokumentInfoId
        if (dokumentInfoId == null) {
            throw ManglerJsonDokumentException(journalpostId)
        }
        logger.info { "Henter dokument for journalpost $journalpostId og dokumentInfoId $dokumentInfoId" }

        val response: HttpResponse =
            httpClient.get("$url/rest/hentdokument/$journalpostId/${dokumentInfoId}/${ORIGINAL}") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${tokenClient.hentM2mToken(System.getenv("SAF_SCOPE"))}")
            }

        if (response.status == HttpStatusCode.OK) {
            val søknad = json.decodeFromString<Søknad>(response.bodyAsText())
            return søknad
        } else {
           throw KlientFeil("Kunne ikke hente JSON-dokument fra Saf. Status: ${response.status}, body: ${response.bodyAsText()}")
        }
    }
}

class ManglerJsonDokumentException(journalpostId: String) : RuntimeException("Mangler JSON-dokument for journalpostId $journalpostId")
class KlientFeil(melding: String) : RuntimeException(melding)

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
    val behandlingstema: String? = null,
    val kanal: String?,
    val dokumenter: List<Dokument>,
) {
    // todo: dobbeltsjekke
    fun erDigitalSøknad() = kanal == "NAV_NO" && dokumenter.any { it.harJsonSøknad() }

    fun erSkannetSøknad() = kanal == "SKAN_IM" && dokumenter.any { it.erSøknad() }

    fun kategoriser() = when {
        erDigitalSøknad() -> SøknadsKategori.DIGITAL
        erSkannetSøknad() -> SøknadsKategori.SKANNET
        else -> SøknadsKategori.UKJENT
    }
}

@Serializable
data class Dokument(
    val dokumentInfoId: String,
    val tittel: String?,
    val brevkode: String?,
    val dokumentvarianter: List<Dokumentvariant>,
) {
    fun harJsonSøknad() = brevkode == "NAV 12-06.05" &&
            dokumentvarianter.any { variant ->
                variant.variantformat == ORIGINAL
            }

    fun erSøknad() = brevkode == "NAV 12-06.05" || brevkode == "NAVp 12-06.05"
}


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
