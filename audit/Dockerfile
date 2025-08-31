FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY audit/build/libs/audit-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

