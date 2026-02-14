FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY policy-service/pom.xml policy-service/
COPY quote-service/pom.xml quote-service/
COPY notification-service/pom.xml notification-service/
COPY document-service/pom.xml document-service/
COPY search-service/pom.xml search-service/

RUN mvn dependency:go-offline -B

COPY document-service/src document-service/src
RUN mvn clean package -pl document-service -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/document-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
