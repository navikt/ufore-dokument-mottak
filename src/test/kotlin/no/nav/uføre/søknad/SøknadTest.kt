package no.nav.uføre.søknad

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SøknadTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val søknadJson = """
        {
          "language": "nb-NO",
          "data": {
            "data": {
              "erDuIArbeidNa": "nei",
              "erDuNaeringsdrivende": "nei",
              "driverDuGardsbruk": "nei",
              "dineOpplysninger": {
                "fornavn": "Ola", "etternavn": "Nordmann",
                "identitet": { "identitetsnummer": "12345678910" },
                "adresse": { "adresse": "Gate 1", "postnummer": "0001" },
                "adresseVarighet": { "gyldigFraOgMed": "2020-01-01", "gyldigTilOgMed": "" }
              },
              "hvaErDinSivilstatus": "samboer",
              "fornavn1": "Kari",
              "samboersFodselsdatoDdMmAaaa": "1980-05-17",
              "varDuArbeidstakerDaDuBleSyk": "ja",
              "arbeidsgiversNavn": [ { "arbeidsgiversNavn1": "Firma AS", "stillingsprosent": 80 } ],
              "varDuNaeringsdrivendeEllerGardbrukerDaDuBleSyk": "nei",
              "harDuAvtjentMilitaerEllerSivilForstegangstjeneste": "nei",
              "harDuBoddOgEllerArbeidetUtenforNorgeEtterAtDuFylte16Ar": "nei",
              "erDuNorskStatsborger": "ja",
              "mottarDuPensjonFraUtlandet": "nei",
              "harDuEnGodkjentYrkesskadeEllerYrkessykdomSomErArsakTilAtDuSokerOmUforetrygd": "nei",
              "harDuBarnUnder18Ar": "nei",
              "bruttoArligNaeringsinntekt2": 125000.5,
              "borDuUtenforNorgeEllerEos": "nei",
              "harDuAvtaltMedNavAtDuSkalSendeMerDokumentasjon": "nei",
              "jegErKjentMedAtNavKanInnhenteDeOpplysningeneSomErNodvendigForAAvgjoreSoknadenMinOpplysningeneErGittEtterBesteSkjonnOgOverbevisningOgErSaFullstendigeSomMuligErklaeringenOgUnderskriftenGjelderHeleSoknadenOmUforetrygd": true,
              "kopiAvSisteLonnslipp": "ettersender",
              "passTilDinSamboer": { "key": "levertTidligere" },
              "annenDokumentasjon": {
                "attachmentId": "a1", "navId": "eudyccn", "type": "other", "value": "leggerVedNaa",
                "files": [ { "fileId": "f1", "attachmentId": "a1", "innsendingId": "i1", "fileName": "x.pdf", "size": 1024 } ]
              }
            },
            "attachments": [ { "attachmentId": "a1", "navId": "eudyccn", "type": "other", "value": "leggerVedNaa" } ],
            "selfDeclaration": true
          }
        }
    """.trimIndent()

    @Test
    fun `deserialiserer søknad med omdøpte felter, datoer og alle vedleggsformer`() {
        val søknad = json.decodeFromString<Søknad>(søknadJson)
        val data = søknad.data.data

        data.hvaErDinSivilstatus shouldBe Sivilstatus.SAMBOER
        data.samboerFornavn shouldBe "Kari"
        data.samboersFodselsdato shouldBe LocalDate.of(1980, 5, 17)
        data.samboerBruttoArligNaeringsinntekt shouldBe 125000.5
        data.arbeidsgivereDaDuBleSyk.single().arbeidsgiversNavn shouldBe "Firma AS"
        data.dineOpplysninger!!.adresseVarighet!!.gyldigTilOgMed.shouldBeNull()
        data.erKjentMedOpplysningspliktOgSamtykke shouldBe true

        data.kopiAvSisteLonnslipp!!.valg shouldBe Vedleggsvalg.ETTERSENDER
        data.passTilDinSamboer!!.valg shouldBe Vedleggsvalg.LEVERT_TIDLIGERE
        data.annenDokumentasjon!!.type shouldBe Vedleggstype.OTHER
        data.annenDokumentasjon.filer shouldHaveSize 1
        søknad.data.attachments.single().valg shouldBe Vedleggsvalg.LEGGER_VED_NAA
    }

    @Test
    fun `serialiserer og deserialiserer tilbake til samme modell`() {
        val søknad = json.decodeFromString<Søknad>(søknadJson)
        json.decodeFromString<Søknad>(json.encodeToString(søknad)) shouldBe søknad
    }
}
