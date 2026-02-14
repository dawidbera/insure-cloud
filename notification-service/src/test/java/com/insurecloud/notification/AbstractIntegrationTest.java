package com.insurecloud.notification;

import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.SQS);

    static {
        localstack.start();
        System.setProperty("spring.cloud.aws.endpoint", localstack.getEndpoint().toString());
        System.setProperty("spring.cloud.aws.region.static", localstack.getRegion());
        System.setProperty("spring.cloud.aws.credentials.access-key", localstack.getAccessKey());
        System.setProperty("spring.cloud.aws.credentials.secret-key", localstack.getSecretKey());
    }
}
