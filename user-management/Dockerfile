FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY user-management/build/libs/user-management-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

