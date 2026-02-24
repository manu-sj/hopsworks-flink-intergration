#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Validate required environment variables
REQUIRED_VARS=(
    KAFKA_BOOTSTRAP_SERVERS
    KAFKA_TOPIC
    HOPSWORKS_HOST
    HOPSWORKS_PROJECT
    HOPSWORKS_API_KEY
)

# Load .env if present
if [ -f "$SCRIPT_DIR/.env" ]; then
    set -a
    source "$SCRIPT_DIR/.env"
    set +a
fi

MISSING=()
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var:-}" ]; then
        MISSING+=("$var")
    fi
done

if [ ${#MISSING[@]} -gt 0 ]; then
    echo "ERROR: Missing required environment variables:"
    for var in "${MISSING[@]}"; do
        echo "  - $var"
    done
    echo ""
    echo "Copy .env.template to .env and fill in the values."
    exit 1
fi

echo "==> Starting Flink Kafka to Hopsworks pipeline..."
docker compose -f "$SCRIPT_DIR/docker-compose.yml" up --build "$@"
