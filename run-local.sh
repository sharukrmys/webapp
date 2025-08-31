#!/bin/bash

# Build the application
./gradlew clean build -x test

# Run the application with the local profile
java -jar metadata/build/libs/metadata-*.jar --spring.profiles.active=local

