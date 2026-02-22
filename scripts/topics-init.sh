#!/bin/bash
# Kafka 토픽 자동 생성 스크립트 (Choreography 패턴 용)

echo "Waiting for Kafka to be ready..."
sleep 5

echo "Creating topics..."
docker exec -it msa-kafka kafka-topics --create --if-not-exists --topic transfer.events --bootstrap-server localhost:9092 --partitions 3
docker exec -it msa-kafka kafka-topics --create --if-not-exists --topic account.events --bootstrap-server localhost:9092 --partitions 3
docker exec -it msa-kafka kafka-topics --create --if-not-exists --topic ledger.events --bootstrap-server localhost:9092 --partitions 3
docker exec -it msa-kafka kafka-topics --create --if-not-exists --topic dead-letter.events --bootstrap-server localhost:9092 --partitions 3

echo "Topics created successfully!"
