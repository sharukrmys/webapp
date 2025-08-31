FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY metadata/build/libs/metadata-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

