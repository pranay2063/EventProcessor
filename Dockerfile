FROM eclipse-temurin:21-jdk-alpine
EXPOSE 8080
ADD target/event-processor.jar event-processor.jar
ENTRYPOINT ["java", "-jar", "/event-processor.jar"]