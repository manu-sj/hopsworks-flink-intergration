package com.example.flinkkafka.source;

import com.example.flinkkafka.model.Transaction;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaSourceBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaSourceBuilder.class);

    public static KafkaSource<Transaction> build() {
        String bootstrapServers = getEnvOrDefault("KAFKA_BOOTSTRAP_SERVERS", "host.docker.internal:9092");
        String topic = getEnvOrDefault("KAFKA_TOPIC", "transactions");
        String groupId = getEnvOrDefault("KAFKA_GROUP_ID", "flink-kafka-hopsworks");

        LOG.info("Building Kafka source: servers={}, topic={}, groupId={}", bootstrapServers, topic, groupId);

        return KafkaSource.<Transaction>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new TransactionDeserializer())
                .build();
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
