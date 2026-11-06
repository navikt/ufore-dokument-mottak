package no.nav.uføre

import io.mockk.awaits
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JoarkConsumerTest {
    @Test
    fun `hvis meldingshåndtering kjører uten feil så skal offsett committes`() {
        // given
        val topicPartition = TopicPartition("topic", 1).apply {  }
        val mockConsumer = MockConsumer<String, GenericRecord>("earliest").apply {
            updateBeginningOffsets(mapOf(topicPartition to 1))
            assign(listOf(topicPartition))
        }
        val safClient = mockk<SafClient>()
        val joarkConsumer = JoarkConsumer(mockConsumer, safClient)

        coEvery { safClient.hentJournalpost(JournalpostId("123")) } just awaits

        // when
        joarkConsumer.consume()

        mockConsumer.addRecord(ConsumerRecord("topic", 1, 1, "nøkkel", lagJoarkHendelse(1L, "UFO", "MOTTATT", "kanal")))

        runBlocking { delay(500) }

        // then
        val actual = mockConsumer.committed(setOf(topicPartition))
        assertEquals(2, actual[topicPartition]?.offset())
    }

}
