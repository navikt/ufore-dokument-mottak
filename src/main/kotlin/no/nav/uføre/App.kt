package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starter server" }

    val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val safClient = SafClient(HttpClient(CIO), TokenClient(), meterRegistry)
    val consumer = KafkaConfig.aivenConsumer()

    val joarkConsumer = JoarkConsumer(consumer, safClient)

    joarkConsumer.consume()

    embeddedServer(Netty, port = 8080) {
        setupPrometheusMetrics(consumer, meterRegistry)

        routing {
            get("/isalive") {
                if (joarkConsumer.isAlive()) {
                    call.respondText("ok")
                } else {
                    call.respondText("ERROR", status = HttpStatusCode.ServiceUnavailable)
                }
            }
            get("/isready") {
                call.respondText("ok")
            }
        }
    }.start(wait = true)
}
