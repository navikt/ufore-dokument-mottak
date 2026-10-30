package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val log = KotlinLogging.logger {}
    log.info { "Starter server" }

    val safClient = SafClient(HttpClient(CIO), TokenClient())
    val consumer = KafkaConfig.aivenConsumer()
    JoarkConsumer(consumer, safClient).consume()

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
