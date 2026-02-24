package com.example.flinkkafka.source;

import com.example.flinkkafka.model.Transaction;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TransactionDeserializer extends AbstractDeserializationSchema<Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionDeserializer.class);
    private transient ObjectMapper objectMapper;

    @Override
    public void open(InitializationContext context) {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Transaction deserialize(byte[] bytes) throws IOException {
        try {
            return objectMapper.readValue(bytes, Transaction.class);
        } catch (IOException e) {
            LOG.error("Failed to deserialize transaction: {}", new String(bytes), e);
            return null;
        }
    }
}
