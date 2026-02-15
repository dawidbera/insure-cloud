package com.insurecloud.policy;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.cloud.compatibility-verifier.enabled=false")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.SNS, LocalStackContainer.Service.SQS);

    // Manual property configuration for LocalStack since @ServiceConnection 
    // doesn't support all AWS services automatically in the same way as DBs
    static {
        localstack.start();
        System.setProperty("spring.cloud.aws.endpoint", localstack.getEndpoint().toString());
        System.setProperty("spring.cloud.aws.region.static", localstack.getRegion());
        System.setProperty("spring.cloud.aws.credentials.access-key", localstack.getAccessKey());
        System.setProperty("spring.cloud.aws.credentials.secret-key", localstack.getSecretKey());
    }
}
