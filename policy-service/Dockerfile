# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Copy the parent pom and all service poms to cache dependencies
COPY pom.xml .
COPY policy-service/pom.xml policy-service/
COPY quote-service/pom.xml quote-service/
COPY notification-service/pom.xml notification-service/
COPY document-service/pom.xml document-service/
COPY search-service/pom.xml search-service/

# Build only dependencies
RUN mvn dependency:go-offline -B

# Copy source code and build the specific service
COPY policy-service/src policy-service/src
RUN mvn clean package -pl policy-service -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/policy-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
