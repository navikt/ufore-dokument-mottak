package no.nav.uføre

import io.kotest.assertions.nondeterministic.eventually
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class JoarkConsumerTest {
    private lateinit var joarkConsumer: JoarkConsumer
    private lateinit var mockConsumer: MockConsumer<String, GenericRecord>
    private val søknadService = mockk<SøknadService>()

    private val topicPartition = TopicPartition("topic", 1)
    private val journalpostId = "123"
    private val joarkHendelse = lagJoarkHendelse(journalpostId.toLong(), "UFO", "JournalpostMottatt", "NAV_NO")

    @BeforeEach
    fun setup() {
        mockConsumer = MockConsumer<String, GenericRecord>("earliest").apply {
            updateBeginningOffsets(mapOf(topicPartition to 1))
            assign(listOf(topicPartition))
        }
        joarkConsumer = JoarkConsumer(mockConsumer, søknadService)
    }

    @AfterEach
    fun ryddOpp() = runBlocking {
        joarkConsumer.stop()
    }

    @Test
    fun `hvis meldingshåndtering kjører uten feil så skal vi committe neste offset`() {
        val record: ConsumerRecord<String, GenericRecord> = ConsumerRecord("topic", 1, 1, journalpostId, joarkHendelse)
        coEvery { søknadService.mottaDokument(record) } just Runs

        // when
        joarkConsumer.start()
        mockConsumer.addRecord(record)

        runBlocking { delay(500.milliseconds) }

        // then
        val actual = mockConsumer.committed(setOf(topicPartition))
        assertEquals(2, actual[topicPartition]?.offset())
    }

    @Test
    fun `hvis meldingshåndtering kaster feil så skal vi ikke oppdatere offset`() {
        // when
        mockConsumer.addRecord(ConsumerRecord("topic", 1, 1, journalpostId, joarkHendelse))

        val meldinger = mockConsumer.poll(Duration.ZERO)

        assertThrows<Exception> {
            runBlocking {
                joarkConsumer.håndter(meldinger)
            }
        }

        val actual = mockConsumer.committed(setOf(topicPartition))
        assertEquals(1L, actual[topicPartition]?.offset())
    }

    @Test
    fun `hvis meldingshåndtering kaster feil så skal jobben bli stoppet`() {
        // given
        coEvery { søknadService.mottaDokument(any()) } throws Exception("Feil")

        // when
        mockConsumer.addRecord(ConsumerRecord("topic", 1, 1, journalpostId, joarkHendelse))
        joarkConsumer.start()

        runBlocking {
            eventually(duration = 5.seconds) {
                assertFalse(joarkConsumer.isAlive())
                assertTrue(mockConsumer.closed())
            }
        }
    }
}
