package com.insurecloud.policy;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.cloud.compatibility-verifier.enabled=false")
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

    /**
     * Static initializer to start LocalStack and configure system properties
     * for AWS client to connect to the emulated services.
     * This is necessary because @ServiceConnection does not fully support
     * all AWS services with dynamic configuration.
     */
    static {
        localstack.start();
        System.setProperty("spring.cloud.aws.endpoint", localstack.getEndpoint().toString());
        System.setProperty("spring.cloud.aws.region.static", localstack.getRegion());
        System.setProperty("spring.cloud.aws.credentials.access-key", localstack.getAccessKey());
        System.setProperty("spring.cloud.aws.credentials.secret-key", localstack.getSecretKey());
    }
}
