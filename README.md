# Flink Kafka to Hopsworks Feature Store

Real-time streaming pipeline that ingests transaction data from Kafka using Apache Flink and writes it into the Hopsworks Feature Store.

## What This Branch Does

This branch introduces a complete streaming data pipeline with three components:

- **Flink Streaming Job (Java)** — Consumes JSON transaction messages from a Kafka topic, normalises the currency field to uppercase, filters out malformed records, and streams the results into a Hopsworks Feature Group via `insertStream()`.
- **Kafka Producer Simulator (Python)** — Generates synthetic transaction data (50 users, 3 currencies, 4 categories, random amounts $10–$500) at a configurable rate and publishes to Kafka for end-to-end testing.
- **Feature Group Setup Script (Python)** — One-time initialisation script that creates (or retrieves) the `transactions` stream-enabled Feature Group in Hopsworks with online storage.

### Hopsworks Ingestion

The pipeline writes to Hopsworks using the [HSFS Flink client](https://docs.hopsworks.ai/). The ingestion flow works as follows:

1. **Setup** (`setup/create_feature_group.py`) — Creates a `StreamFeatureGroup` in Hopsworks with `stream=True` and `online_enabled=True`. This configures a Hopsworks-managed Kafka topic behind the feature group that accepts streaming inserts.
2. **Connection** (`HopsworksSinkBuilder.java`) — At job startup, the Flink job connects to Hopsworks using the API key, retrieves the project's Feature Store, and looks up the `StreamFeatureGroup` by name and version.
3. **Streaming insert** — `featureGroup.insertStream(dataStream)` is called, which:
   - Serializes each `Transaction` POJO to Avro
   - Writes Avro records to the feature group's internal Kafka topic
   - Hopsworks materializes the data to both the **online store** (for low-latency serving) and the **offline store** (for training data / batch queries)

### Transaction Schema

| Field | Type | Description |
|---|---|---|
| `transaction_id` | string | Unique identifier (primary key) |
| `event_time` | bigint | Epoch milliseconds |
| `user_id` | string | Customer identifier (`user_001`–`user_050`) |
| `amount` | double | Transaction amount |
| `currency` | string | ISO 4217 code (USD, EUR, GBP) |
| `category` | string | `electronics`, `food`, `clothing`, or `travel` |

## Prerequisites

- Java 8+
- Maven 3.6+
- Docker & Docker Compose
- Access to a Kafka broker
- Access to a Hopsworks instance

## Configuration

Copy the template and fill in your values:

```bash
cp .env.template .env
```

Required variables:

| Variable | Description |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address (e.g. `host.docker.internal:9092`) |
| `KAFKA_TOPIC` | Topic to consume from (default: `transactions`) |
| `HOPSWORKS_HOST` | Hopsworks cluster hostname |
| `HOPSWORKS_PROJECT` | Project name in Hopsworks |
| `HOPSWORKS_API_KEY` | Hopsworks API key |

Optional variables:

| Variable | Default | Description |
|---|---|---|
| `KAFKA_GROUP_ID` | `flink-kafka-hopsworks` | Consumer group ID |
| `HOPSWORKS_PORT` | `443` | Hopsworks API port |
| `FEATURE_GROUP_NAME` | `transactions` | Target feature group name |
| `FEATURE_GROUP_VERSION` | `1` | Feature group version |
| `PUBLISH_INTERVAL_SEC` | `1.0` | Simulator publish rate (seconds) |

## Running

### 1. Create the Feature Group (one-time)

```bash
pip install -r setup/requirements.txt
source .env
python setup/create_feature_group.py
```

### 2. Build

```bash
./build.sh
```

This runs `mvn clean package -DskipTests` to produce the fat JAR, then builds the Docker images.

### 3. Start the Pipeline

```bash
./run.sh
```

This validates required environment variables, then starts three Docker containers:

| Container | Role |
|---|---|
| `jobmanager` | Flink JobManager — submits and coordinates the streaming job |
| `taskmanager` | Flink TaskManager — executes the pipeline operators (2 task slots) |
| `simulator` | Publishes synthetic transactions to Kafka |

The Flink Web UI is available at [http://localhost:8081](http://localhost:8081).

### 4. Stop

```bash
docker compose down
```

## Project Structure

```
.
├── src/main/java/com/example/flinkkafka/
│   ├── FlinkKafkaToHopsworks.java          # Pipeline entry point
│   ├── model/
│   │   └── Transaction.java                # Transaction POJO
│   ├── source/
│   │   ├── KafkaSourceBuilder.java         # Kafka consumer config
│   │   └── TransactionDeserializer.java    # JSON deserializer
│   └── sink/
│       └── HopsworksSinkBuilder.java       # Hopsworks streaming sink
├── simulator/
│   ├── producer.py                         # Synthetic data generator
│   ├── Dockerfile
│   └── requirements.txt
├── setup/
│   ├── create_feature_group.py             # Feature group init
│   └── requirements.txt
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── build.sh
├── run.sh
├── .env.template
└── .gitignore
```
