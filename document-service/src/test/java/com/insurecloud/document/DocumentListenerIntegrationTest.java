package com.insurecloud.document;

import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class DocumentListenerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private SqsAsyncClient sqsAsyncClient;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Template s3Template;

    private static final String BUCKET_NAME = "policy-documents";

    /**
     * Prepares the test infrastructure in LocalStack by creating the SQS queue
     * and S3 bucket required for the test.
     */
    @BeforeEach
    void setUp() {
        // Prepare infrastructure in LocalStack
        sqsAsyncClient.createQueue(CreateQueueRequest.builder().queueName("document-queue").build()).join();
        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
    }

    /**
     * Tests the document listener by sending a policy issued event to SQS
     * and verifying that a PDF document is eventually uploaded to S3.
     */
    @Test
    void shouldGeneratePdfAndUploadToS3() {
        // Given
        String policyNumber = "POL-DOC-TEST-" + UUID.randomUUID();
        PolicyIssuedEvent event = new PolicyIssuedEvent(
                UUID.randomUUID(),
                policyNumber,
                "CUST-DOC-TEST",
                new BigDecimal("250.50")
        );

        // When
        sqsTemplate.send("document-queue", event);

        // Then
        String expectedFileName = "policy_" + policyNumber + ".pdf";
        
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            boolean exists = s3Template.objectExists(BUCKET_NAME, expectedFileName);
            assertThat(exists).isTrue();
        });

        logInfo("Document successfully verified in S3: " + expectedFileName);
    }

    /**
     * Utility method to log information to the console during tests.
     *
     * @param msg The message to log.
     */
    private void logInfo(String msg) {
        System.out.println("TEST-INFO: " + msg);
    }
}
