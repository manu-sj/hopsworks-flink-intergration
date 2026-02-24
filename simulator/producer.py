"""Kafka producer simulator that generates fake transaction messages."""

import json
import logging
import os
import random
import time
import uuid

from kafka import KafkaProducer

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
)
logger = logging.getLogger(__name__)

BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "host.docker.internal:9092")
TOPIC = os.getenv("KAFKA_TOPIC", "transactions")
PUBLISH_INTERVAL_SEC = float(os.getenv("PUBLISH_INTERVAL_SEC", "1.0"))

CURRENCIES = ["USD", "EUR", "GBP"]
CATEGORIES = ["electronics", "food", "clothing", "travel"]
USER_IDS = [f"user_{str(i).zfill(3)}" for i in range(1, 51)]


def generate_transaction() -> dict:
    return {
        "transaction_id": f"txn_{uuid.uuid4().hex[:12]}",
        "event_time": int(time.time() * 1000),
        "user_id": random.choice(USER_IDS),
        "amount": round(random.uniform(10.0, 500.0), 2),
        "currency": random.choice(CURRENCIES),
        "category": random.choice(CATEGORIES),
    }


def main():
    logger.info("Starting Kafka producer simulator")
    logger.info("Bootstrap servers: %s", BOOTSTRAP_SERVERS)
    logger.info("Topic: %s", TOPIC)
    logger.info("Publish interval: %s sec", PUBLISH_INTERVAL_SEC)

    producer = KafkaProducer(
        bootstrap_servers=BOOTSTRAP_SERVERS,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        key_serializer=lambda k: k.encode("utf-8") if k else None,
    )

    try:
        count = 0
        while True:
            txn = generate_transaction()
            producer.send(TOPIC, key=txn["transaction_id"], value=txn)
            count += 1

            if count % 10 == 0:
                logger.info("Published %d messages. Latest: %s", count, txn["transaction_id"])

            time.sleep(PUBLISH_INTERVAL_SEC)
    except KeyboardInterrupt:
        logger.info("Shutting down after %d messages", count)
    finally:
        producer.flush()
        producer.close()


if __name__ == "__main__":
    main()
