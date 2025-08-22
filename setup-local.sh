#!/bin/bash

# Exit on error
set -e

echo "Setting up local development environment for Multi-Module Spring Boot Application"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Docker is not installed. Please install Docker and Docker Compose first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check if Java 21 is installed
if ! command -v java &> /dev/null; then
    echo "Java is not installed. Please install Java 21 first."
    exit 1
fi

java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
if [[ ! $java_version == 21* ]]; then
    echo "Java 21 is required. Current version: $java_version"
    echo "Please install Java 21 and set it as the default Java version."
    exit 1
fi

# Create directories for Docker volumes
mkdir -p .docker/postgres-data
mkdir -p .docker/redis-data
mkdir -p .docker/minio-data

# Start Docker Compose
echo "Starting Docker Compose services..."
docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 10

# Check if services are running
if ! docker ps | grep -q postgres; then
    echo "PostgreSQL container is not running. Please check Docker logs."
    exit 1
fi

if ! docker ps | grep -q redis; then
    echo "Redis container is not running. Please check Docker logs."
    exit 1
fi

if ! docker ps | grep -q minio; then
    echo "Minio container is not running. Please check Docker logs."
    exit 1
fi

# Build the project
echo "Building the project..."
./gradlew clean build -x test

echo "Setup completed successfully!"
echo ""
echo "You can now run the application with:"
echo "./gradlew data:bootRun --args='--spring.profiles.active=local'"
echo ""
echo "Or use VS Code to run the application with the provided launch configurations."
echo ""
echo "Services:"
echo "- PostgreSQL: localhost:5432 (username: postgres, password: postgres)"
echo "- Redis: localhost:6379"
echo "- Minio: localhost:9000 (console: localhost:9001) (username: minioadmin, password: minioadmin)"
echo ""
echo "API Documentation: http://localhost:8080/api/swagger-ui.html"

