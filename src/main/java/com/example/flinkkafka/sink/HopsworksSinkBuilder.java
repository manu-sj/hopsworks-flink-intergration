package com.example.flinkkafka.sink;

import com.example.flinkkafka.model.Transaction;
import com.logicalclocks.hsfs.flink.FeatureStore;
import com.logicalclocks.hsfs.flink.HopsworksConnection;
import com.logicalclocks.hsfs.flink.StreamFeatureGroup;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HopsworksSinkBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(HopsworksSinkBuilder.class);

    public static DataStreamSink<?> build(DataStream<Transaction> stream) throws Exception {
        String host = requireEnv("HOPSWORKS_HOST");
        int port = Integer.parseInt(getEnvOrDefault("HOPSWORKS_PORT", "443"));
        String project = requireEnv("HOPSWORKS_PROJECT");
        String apiKey = requireEnv("HOPSWORKS_API_KEY");
        String featureGroupName = getEnvOrDefault("FEATURE_GROUP_NAME", "transactions");
        int featureGroupVersion = Integer.parseInt(getEnvOrDefault("FEATURE_GROUP_VERSION", "1"));

        LOG.info("Connecting to Hopsworks: host={}, port={}, project={}", host, port, project);

        HopsworksConnection connection = HopsworksConnection.builder()
                .host(host)
                .port(port)
                .project(project)
                .apiKeyValue(apiKey)
                .hostnameVerification(false)
                .build();

        FeatureStore featureStore = connection.getFeatureStore();

        LOG.info("Inserting stream into feature group: name={}, version={}", featureGroupName, featureGroupVersion);

        StreamFeatureGroup featureGroup = featureStore.getStreamFeatureGroup(featureGroupName, featureGroupVersion);
        return featureGroup.insertStream(stream);
    }

    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Required environment variable not set: " + key);
        }
        return value;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
