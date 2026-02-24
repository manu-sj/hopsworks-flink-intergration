FROM flink:1.17

COPY target/flink-kafka-hopsworks-1.0-SNAPSHOT.jar /opt/flink/usrlib/flink-kafka-hopsworks.jar
COPY lib/hsfs-flink.jar /opt/flink/usrlib/hsfs-flink.jar
