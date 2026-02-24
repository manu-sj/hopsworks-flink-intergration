#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

HSFS_JAR="${HSFS_FLINK_JAR:-/Users/manu/Desktop/HopsWorks/hopsworks-api/java/flink/target/hsfs-flink-4.8.0-SNAPSHOT-jar-with-dependencies.jar}"

if [ ! -f "$HSFS_JAR" ]; then
    echo "ERROR: HSFS Flink JAR not found at: $HSFS_JAR"
    echo "Set HSFS_FLINK_JAR to the path of your custom JAR."
    exit 1
fi

echo "==> Building fat JAR..."
mvn -f "$SCRIPT_DIR/pom.xml" clean package -DskipTests

echo "==> Building Docker images..."
docker compose -f "$SCRIPT_DIR/docker-compose.yml" build

echo "==> Build complete."
