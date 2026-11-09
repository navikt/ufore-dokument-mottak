package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition
import java.time.Duration

private val logger = KotlinLogging.logger {}

class JoarkConsumer(private val kafkaConsumer: Consumer<String, GenericRecord>, private val safClient: SafClient) {

    private val job = Job()

    fun consume() {
        CoroutineScope(Dispatchers.IO + job).launch {
            // try catch - kafka konsument feiler

            while (job.isActive) {
                val meldinger = kafkaConsumer.poll(Duration.ofSeconds(1))
                håndter(meldinger)
            }
        }
    }

    suspend fun håndter(meldinger: ConsumerRecords<String, GenericRecord>) {
        val offsets = meldinger
            .groupBy { TopicPartition(it.topic(), it.partition()) }
            .mapValues { partition -> partition.value.minOf { it.offset() } }
            .toMutableMap()

        try {
            meldinger.forEach { melding ->
                if (skalBehandle(melding)) {
                    logger.info { "Fikk melding med nøkkel: ${melding.key()}" }

                    safClient.hentJournalpost(melding.key())
                }
                offsets[TopicPartition(melding.topic(), melding.partition())] = melding.offset() + 1
            }

        } catch (e: Exception) {
            logger.error(e) { "Feil ved håndtering av medling" }
            offsets.forEach { (partition, offset) -> kafkaConsumer.seek(partition, offset) }
            throw e
        } finally {
            kafkaConsumer.commitSync()
        }

    }

    fun isAlive(): Boolean = job.isActive

    private fun skalBehandle(melding: ConsumerRecord<String, GenericRecord>): Boolean {
        val tema = melding.value().get("temaNytt")
        val hendelsesType = melding.value().get("hendelsesType")
        val mottaksKanal = melding.value().get("mottaksKanal")
        return tema == "UFO" && hendelsesType == "JournalpostMottatt" && mottaksKanal != "EESSI"
    }
}
