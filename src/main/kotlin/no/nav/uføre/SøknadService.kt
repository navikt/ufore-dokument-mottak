package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord

private val logger = KotlinLogging.logger {}

class SøknadService(
    private val safClient: SafClient,
    private val oppgaveClient: OppgaveClient,
    private val målinger: Målinger
) {

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

        val harOppgave = oppgaveClient.harJournalpostOppgave(journalpost.journalpostId)
        logger.info { "Har oppgave ${journalpost.journalpostId}: $harOppgave" }

    }
}
