package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Marker
import no.nav.uføre.Variantformat.ORIGINAL
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord

private val logger = KotlinLogging.logger {}

// denne kaller saf ved behov
class SøknadService(private val safClient: SafClient, private val målinger: Målinger) {
    private val teamLogsMarker = object : Marker {
        override fun getName(): String = "TEAM_LOGS"
    }

    suspend fun mottaDokument(melding: ConsumerRecord<String, GenericRecord>){
        // hente fra sag
        val journalpost = safClient.hentJournalpost(melding.key())

        målinger.kategoriserSøknad(journalpost)

        if (journalpost.dokumenter.any { it.dokumentvarianter.any { variant -> variant.variantformat == ORIGINAL } }) {
            hentSelveDokumentet(journalpost)
        }

        målinger.tellTitler(journalpost)

        logger.info(teamLogsMarker) { "Hentet journalpost: $journalpost, status: ${response.status}" }
    }


}