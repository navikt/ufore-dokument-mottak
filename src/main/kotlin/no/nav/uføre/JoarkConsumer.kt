package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class JoarkConsumer (private val kafkaConsumer: KafkaConsumer<String, GenericRecord>) {
    val log = KotlinLogging.logger {}

    fun consume() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val meldinger = kafkaConsumer.poll(Duration.ofMillis(100))
                meldinger.forEach { melding ->
                    val tema = melding.value().get("temaNytt")
                    if (tema == "UFO") {
                        log.info { "Fikk melding med nøkkel: ${melding.key()}, verdi: ${melding.value()}" }
                    }
                }
                kafkaConsumer.commitSync()
            }
        }
    }
}