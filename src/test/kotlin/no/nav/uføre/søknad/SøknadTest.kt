package no.nav.uføre.søknad

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SøknadTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserialiserer søknad med omdøpte felter, datoer og alle vedleggsformer`() {
        val søknad = json.decodeFromString<Søknad>(javaClass.getResource("/saf_søknad.json")!!.readText())
        val data = søknad.data.data

        assertEquals(76, søknad.formRevision)
        assertEquals(Sivilstatus.GIFT_ELLER_PARTNER, data.hvaErDinSivilstatus)
        assertEquals("Kari", data.ektefelleFornavn)
        assertEquals("Sverige", data.hvaErDinEktefelleEllerPartnersStatsborgerskap)
        assertEquals(7000.0, data.ektefelleBruttoArligNaeringsinntekt)
        assertEquals("Arbeidsgivers navn 1 før sykdom", data.arbeidsgivereDaDuBleSyk.first().arbeidsgiversNavn)
        assertNull(data.dineOpplysninger!!.adresseVarighet)
        assertTrue(data.erKjentMedOpplysningspliktOgSamtykke)

        assertEquals(4, søknad.data.attachments.size)
        assertEquals(Vedleggsvalg.ETTERSENDER, søknad.data.attachments[0].valg)
        assertEquals(Vedleggsvalg.LEVERT_TIDLIGERE, søknad.data.attachments[2].valg)
        val dokumentasjon = søknad.data.attachments.last()
        assertEquals(Vedleggstype.OTHER, dokumentasjon.type)
        assertEquals(1, dokumentasjon.filer.size)
        assertEquals(26251.0, dokumentasjon.filer.single().size)
        assertEquals(Vedleggsvalg.LEGGER_VED_NAA, dokumentasjon.valg)
    }

    @Test
    fun `serialiserer og deserialiserer tilbake til samme modell`() {
        val søknad = json.decodeFromString<Søknad>(javaClass.getResource("/saf_søknad.json")!!.readText())
        assertEquals(søknad, json.decodeFromString<Søknad>(json.encodeToString(søknad)))
    }
}
