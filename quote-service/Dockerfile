FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY quote-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
