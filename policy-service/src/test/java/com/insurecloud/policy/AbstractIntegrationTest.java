package com.insurecloud.policy;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Abstract base class for integration tests in the Policy Service.
 * This class sets up and manages shared test infrastructure using Testcontainers,
 * including PostgreSQL and LocalStack for AWS services emulation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.compatibility-verifier.enabled=false",
        "spring.cloud.vault.enabled=false",
        "eureka.client.enabled=false",
        "KEYCLOAK_ISSUER_URI=http://localhost:8088/realms/insure-cloud"
})
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL container for database integration tests.
     * Uses ServiceConnection for automatic Spring Boot datasource configuration.
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    /**
     * LocalStack container for emulating AWS services (SNS, SQS, DynamoDB).
     */
    @Container
    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.SNS, LocalStackContainer.Service.SQS, LocalStackContainer.Service.DYNAMODB);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.endpoint", localstack::getEndpoint);
        registry.add("spring.cloud.aws.region.static", localstack::getRegion);
        registry.add("spring.cloud.aws.credentials.access-key", localstack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localstack::getSecretKey);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
