package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration

fun main() {
    val log = KotlinLogging.logger {}
    log.info { "Starter server" }

    val kafkaLytter = KafkaConfig.aivenConsumer()
    CoroutineScope(Dispatchers.IO).launch {
        while (true) {
            val meldinger = kafkaLytter.poll(Duration.ofMillis(100))
            meldinger.forEach { melding ->
                val tema = melding.value().get("temaNytt")
                if (tema == "UFO") {
                    log.info { "Fikk melding med nøkkel: ${melding.key()}, verdi: ${melding.value()}" }
                }
            }
            kafkaLytter.commitSync()
        }
    }

    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello, world!")
                log.info { "Hello Nav!" }
            }
            get("/isalive") {
                call.respondText("ok")
            }
            get("/isready") {
                call.respondText("ok")
            }
        }
    }.start(wait = true)
}
