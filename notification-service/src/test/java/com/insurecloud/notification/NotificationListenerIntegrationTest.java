package com.insurecloud.notification;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class NotificationListenerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private SqsAsyncClient sqsAsyncClient;

    @SpyBean
    private NotificationListener notificationListener;

    /**
     * Initializes the test environment by ensuring the SQS queue exists in LocalStack.
     */
    @BeforeEach
    void setUp() {
        // Create queue in LocalStack before test
        sqsAsyncClient.createQueue(CreateQueueRequest.builder().queueName("notification-queue").build()).join();
    }

    /**
     * Tests the notification listener by sending a message to SQS
     * and verifying that the listener's onPolicyIssued method is invoked.
     */
    @Test
    void shouldReceiveMessageFromSqs() {
        // Given
        PolicyIssuedEvent event = new PolicyIssuedEvent(
                UUID.randomUUID(),
                "POL-TEST-123",
                "CUST-TEST",
                new BigDecimal("100.00")
        );

        // When
        sqsTemplate.send("notification-queue", event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(notificationListener).onPolicyIssued(any(PolicyIssuedEvent.class));
        });
    }
}
