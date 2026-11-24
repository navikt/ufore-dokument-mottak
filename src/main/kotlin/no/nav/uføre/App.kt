package no.nav.uføre

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Marker
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
val teamLogsMarker = object : Marker {
    override fun getName() = "TEAM_LOGS"
}

fun main() {
    logger.info { "Starter server" }

    val httpClient = HttpClient(CIO)
    val tokenClient = TokenClient()
    val safClient = SafClient(httpClient, tokenClient)
    val oppgaveClient = OppgaveClient(httpClient, tokenClient)
    val consumer = KafkaConfig.aivenConsumer()
    val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val målinger = Målinger(meterRegistry)
    val søknadService = SøknadService(safClient, oppgaveClient, målinger)
    val joarkConsumer = JoarkConsumer(consumer, søknadService)

    joarkConsumer.start()

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
