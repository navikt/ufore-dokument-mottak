package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Marker
import no.nav.uføre.Variantformat.ORIGINAL
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord

private val logger = KotlinLogging.logger {}

class SøknadService(private val safClient: SafClient, private val målinger: Målinger) {
    private val teamLogsMarker = object : Marker {
        override fun getName(): String = "TEAM_LOGS"
    }

    suspend fun mottaDokument(melding: ConsumerRecord<String, GenericRecord>) {
        val journalpost = safClient.hentJournalpost(melding.key())

        målinger.kategoriserSøknad(journalpost)
        målinger.tellTitler(journalpost)

        if (journalpost.erDigitalSøknad()) {
            val søknad = safClient.hentJsonDokument(journalpost)
            logger.info(teamLogsMarker) { "Hentet journalpost ${journalpost.journalpostId} med sivilstatus ${søknad.data.data.hvaErDinSivilstatus}" }
        } else {
            // TODO: Opprett oppgave
        }
    }


}