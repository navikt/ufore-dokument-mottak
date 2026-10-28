package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration

class JoarkConsumer (private val kafkaConsumer: Consumer<String, GenericRecord>) {
    val log = KotlinLogging.logger {}

    fun consume() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val meldinger = kafkaConsumer.poll(Duration.ofSeconds(1))
                meldinger.forEach { melding ->
                    if (skalBehandle(melding)) {
                        log.info { "Fikk melding med nøkkel: ${melding.key()}, verdi: ${melding.value()}" }
                    }
                }
                kafkaConsumer.commitSync()
            }
        }
    }

    private fun skalBehandle(melding: ConsumerRecord<String, GenericRecord>): Boolean {
        val tema = melding.value().get("temaNytt")
        val journalpostStatus = melding.value().get("journalpostStatus")
        val mottaksKanal = melding.value().get("mottaksKanal")
        return tema == "UFO" && journalpostStatus == "MOTTATT" && mottaksKanal != "EESSI"
    }
}
