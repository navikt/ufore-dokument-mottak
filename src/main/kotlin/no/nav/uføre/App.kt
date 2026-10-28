package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val log = KotlinLogging.logger {}
    log.info { "Starter server" }

    val consumer = KafkaConfig.aivenConsumer()
    JoarkConsumer(consumer).consume()

    embeddedServer(Netty, port = 8080) {
        setupPrometheusMetrics(consumer)

        routing {
            get("/isalive") {
                call.respondText("ok")
            }
            get("/isready") {
                call.respondText("ok")
            }
        }
    }.start(wait = true)
}
