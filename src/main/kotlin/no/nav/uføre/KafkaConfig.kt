package no.nav.uføre

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties
import kotlin.collections.set
import kotlin.jvm.java

object KafkaConfig {
    internal const val JOURNALFOERING_REPLICATOR_GROUPID = "ufore-journalfoering-v1"
    internal fun aivenConsumer(): KafkaConsumer<String, GenericRecord> {
        val maxPollIntervalMs = Duration.ofSeconds(60 + 50 * 2.toLong()).toMillis()
        val props = Properties()
        val env = System.getenv()
        props[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = env.getValue("KAFKA_BROKERS")
        props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = SecurityProtocol.SSL.name
        props[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
        props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "jks"
        props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
        props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = env.getValue("KAFKA_TRUSTSTORE_PATH")
        props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = env.getValue("KAFKA_CREDSTORE_PASSWORD")
        props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = env.getValue("KAFKA_KEYSTORE_PATH")
        props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = env.getValue("KAFKA_CREDSTORE_PASSWORD")
        props[ConsumerConfig.GROUP_ID_CONFIG] = JOURNALFOERING_REPLICATOR_GROUPID
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
        props[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = env.getValue("KAFKA_SCHEMA_REGISTRY")
        props[SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
        props[SchemaRegistryClientConfig.USER_INFO_CONFIG] =
            env.getValue("KAFKA_SCHEMA_REGISTRY_USER") + ":" + env.getValue("KAFKA_SCHEMA_REGISTRY_PASSWORD")
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 50
        props[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = "$maxPollIntervalMs"
        val kafkaConsumer = KafkaConsumer<String, GenericRecord>(props)
        kafkaConsumer.subscribe(listOf("teamdokumenthandtering.aapen-dok-journalfoering"))
        return kafkaConsumer
    }
}
