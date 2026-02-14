package com.insurecloud.search;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class PolicySearchIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private SqsAsyncClient sqsAsyncClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PolicySearchRepository policySearchRepository;

    @BeforeEach
    void setUp() {
        policySearchRepository.deleteAll();
        sqsAsyncClient.createQueue(builder -> builder.queueName("search-queue")).join();
    }

    @Test
    void shouldIndexAndSearchPolicy() {
        // Given
        UUID policyId = UUID.randomUUID();
        PolicyIssuedEvent event = new PolicyIssuedEvent(
                policyId,
                "POL-123",
                "CUST-001",
                new BigDecimal("500.00")
        );

        // When
        sqsTemplate.send("search-queue", event);

        // Then
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    List<PolicyDocument> documents = policySearchRepository.findByCustomerId("CUST-001");
                    assertThat(documents).hasSize(1);
                    assertThat(documents.get(0).getPolicyNumber()).isEqualTo("POL-123");
                });

        // And - Test REST API
        ResponseEntity<List<PolicyDocument>> response = restTemplate.exchange(
                "/api/search/by-customer?customerId=CUST-001",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PolicyDocument>>() {}
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getPolicyNumber()).isEqualTo("POL-123");
    }
}
