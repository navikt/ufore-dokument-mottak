package no.nav.uføre

import io.kotest.assertions.nondeterministic.eventually
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

class JoarkConsumerTest {

    @Test
    fun `hvis meldingshåndtering kjører uten feil så skal vi committe neste offset`() {
        // given
        val journalpostId = "123"
        val topicPartition = TopicPartition("topic", 1).apply {  }
        val mockConsumer = MockConsumer<String, GenericRecord>("earliest").apply {
            updateBeginningOffsets(mapOf(topicPartition to 1))
            assign(listOf(topicPartition))
        }
        val safClient = mockk<SafClient>()
        val joarkConsumer = JoarkConsumer(mockConsumer, safClient)

        coEvery { safClient.hentJournalpost(journalpostId) } returns SafResponse(Journalpost(journalpostId, "", "",emptyList()))

        // when
        joarkConsumer.start()

        mockConsumer.addRecord(ConsumerRecord("topic", 1, 1, journalpostId, lagJoarkHendelse(journalpostId.toLong(), "UFO", "JournalpostMottatt", "kanal")))

        runBlocking { delay(500) }

        // then
        val actual = mockConsumer.committed(setOf(topicPartition))
        assertEquals(2, actual[topicPartition]?.offset())
    }

    @Test
    fun `hvis meldingshåndtering kaster feil så skal vi ikke oppdatere offset`() {
        // given
        val journalpostId = "123"
        val topicPartition = TopicPartition("topic", 1).apply {  }
        val mockConsumer = MockConsumer<String, GenericRecord>("earliest").apply {
            updateBeginningOffsets(mapOf(topicPartition to 1))
            assign(listOf(topicPartition))
        }
        val safClient = mockk<SafClient>()
        val joarkConsumer = JoarkConsumer(mockConsumer, safClient)

        coEvery { safClient.hentJournalpost(journalpostId) } throws Exception("Feil")

        // when
        joarkConsumer.start()

        mockConsumer.addRecord(ConsumerRecord("topic", 1, 1, journalpostId, lagJoarkHendelse(journalpostId.toLong(), "UFO", "JournalpostMottatt", "kanal")))
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
        val journalpostId = "123"
        val topicPartition = TopicPartition("topic", 1)
        val mockConsumer = MockConsumer<String, GenericRecord>("earliest").apply {
            updateBeginningOffsets(mapOf(topicPartition to 1))
            assign(listOf(topicPartition))
        }
        val safClient = mockk<SafClient>()
        val joarkConsumer = JoarkConsumer(mockConsumer, safClient)

        coEvery { safClient.hentJournalpost(journalpostId) } throws Exception("Feil")

        // when
        mockConsumer.addRecord(ConsumerRecord("topic", 1, 1, journalpostId, lagJoarkHendelse(journalpostId.toLong(), "UFO", "JournalpostMottatt", "kanal")))
        joarkConsumer.start()

        runBlocking {
            eventually(duration = 5.seconds) {
                assertFalse(joarkConsumer.isAlive())
                assertTrue(mockConsumer.closed())
            }
        }
    }


}
