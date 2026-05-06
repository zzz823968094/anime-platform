FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/ad-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]
