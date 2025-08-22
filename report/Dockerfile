FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the JAR file
COPY report/build/libs/report-*-boot.jar app.jar

# Set environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError"
ENV SPRING_PROFILES_ACTIVE="prod"

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

