#!/bin/bash

echo "Building JAR with Maven..."
./mvnw clean package

echo "Rebuilding Docker images without cache..."
docker-compose build --no-cache

echo "Restarting containers..."
docker-compose down
docker-compose up
