#!/bin/bash

# Build the application
./gradlew clean build -x test

# Build and run Docker containers
docker-compose up --build

