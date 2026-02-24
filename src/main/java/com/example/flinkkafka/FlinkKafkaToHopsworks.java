package com.example.flinkkafka;

import com.example.flinkkafka.model.Transaction;
import com.example.flinkkafka.sink.HopsworksSinkBuilder;
import com.example.flinkkafka.source.KafkaSourceBuilder;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class FlinkKafkaToHopsworks {

    private static final Logger LOG = LoggerFactory.getLogger(FlinkKafkaToHopsworks.class);
    private static final int CHECKPOINTING_INTERVAL_MS = 15000;

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Flink Kafka to Hopsworks pipeline");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(CHECKPOINTING_INTERVAL_MS);
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 10000));

        KafkaSource<Transaction> kafkaSource = KafkaSourceBuilder.build();

        WatermarkStrategy<Transaction> watermarkStrategy = WatermarkStrategy
                .<Transaction>forBoundedOutOfOrderness(Duration.ofSeconds(30))
                .withTimestampAssigner((transaction, timestamp) -> transaction.getEvent_time());

        DataStream<Transaction> transactionStream = env
                .fromSource(kafkaSource, watermarkStrategy, "Kafka Transactions Source")
                .filter(txn -> txn != null && txn.getTransaction_id() != null)
                .map(txn -> {
                    if (txn.getCurrency() != null) {
                        txn.setCurrency(txn.getCurrency().toUpperCase());
                    }
                    return txn;
                })
                .returns(Transaction.class);

        HopsworksSinkBuilder.build(transactionStream);

        env.execute("Flink Kafka to Hopsworks");
    }
}
