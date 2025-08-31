FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY data/build/libs/data-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

