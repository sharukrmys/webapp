FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY attachment/build/libs/attachment-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

