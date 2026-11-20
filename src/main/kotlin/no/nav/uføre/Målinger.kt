package no.nav.uføre

import io.micrometer.core.instrument.MeterRegistry

class Målinger(private val meterRegistry: MeterRegistry) {
    private val tillatteTitler = listOf("Søknad om uføretrygd", "NAV 12-06.05 Søknad om uføretrygd", "Ettersendelse til søknad om uføretrygd", "Ettersendelse til klage", "Klage", "Anke", "Tittel mangler")

    fun tellTitler(journalpost: Journalpost) {

        val dokumentTittel = journalpost.dokumenter.firstOrNull()?.tittel ?: "Tittel mangler"
        val tillattTittel = if (dokumentTittel in tillatteTitler) dokumentTittel else "Annet dokument"
        meterRegistry.counter("ufore_dokument_tittel", "tittel", tillattTittel).increment()
    }

    fun kategoriserSøknad(journalpost: Journalpost) {
        val harVedlegg = journalpost.dokumenter.size > 1

        val kategori = journalpost.kategoriser()
        meterRegistry.counter("ufore_journalpost_mottatt", "kategori", kategori.name, "har_vedlegg", harVedlegg.toString()).increment()
    }
}