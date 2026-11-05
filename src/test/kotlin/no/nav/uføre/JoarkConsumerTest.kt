package no.nav.uføre

import io.mockk.awaits
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JoarkConsumerTest {
    @Test
    fun `hvis vi får feil under meldingshåndtering, så skal offsett ikke commites`() {
        // given
        val mockConsumer = MockConsumer<String, GenericRecord>("earliest")
        val topicPartition = TopicPartition("topic", 1)
        mockConsumer.updateBeginningOffsets(mapOf(topicPartition to 1))
        val safClient = mockk<SafClient>()
        coEvery { safClient.hentJournalpost(JournalpostId("123")) } just awaits
        val joarkConsumer = JoarkConsumer(mockConsumer, safClient)

        // when
        joarkConsumer.consume()

        // then
        val actual = mockConsumer.beginningOffsets(listOf(topicPartition))
        assertEquals(1, actual[topicPartition])

    }

}
