FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY report/build/libs/report-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

