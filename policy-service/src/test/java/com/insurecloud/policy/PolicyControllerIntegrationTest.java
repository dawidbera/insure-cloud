package com.insurecloud.policy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.awaitility.Awaitility;
import java.time.Duration;
import java.util.List;

public class PolicyControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private SnsClient snsClient;

    /**
     * Prepares the test environment by clearing repositories and creating necessary SNS topics.
     * This ensures each test starts with a clean slate.
     */
    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        policyRepository.deleteAll();
        snsClient.createTopic(CreateTopicRequest.builder().name("policy-issued-topic").build());
    }

    /**
     * Tests the full policy creation flow including the Transactional Outbox pattern.
     * Verifies that:
     * 1. The policy is created successfully via REST API.
     * 2. An unprocessed outbox event is created immediately.
     * 3. The OutboxProcessor eventually picks up and processes the event.
     */
    @Test
    void shouldCreatePolicyAndProcessOutboxEvent() {
        // Given
        Policy policy = Policy.builder()
                .policyNumber("POL-" + UUID.randomUUID())
                .customerId("CUST-123")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .premiumAmount(new BigDecimal("500.00"))
                .status(Policy.PolicyStatus.DRAFT)
                .build();

        // When
        ResponseEntity<Policy> response = restTemplate.postForEntity("/api/policies", policy, Policy.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify outbox entry exists immediately
        List<OutboxEvent> outboxEvents = outboxRepository.findAll();
        assertThat(outboxEvents).hasSize(1);
        assertThat(outboxEvents.get(0).isProcessed()).isFalse();

        // Wait for OutboxProcessor to process the event
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    List<OutboxEvent> processedEvents = outboxRepository.findAll();
                    assertThat(processedEvents.get(0).isProcessed()).isTrue();
                    assertThat(processedEvents.get(0).getProcessedAt()).isNotNull();
                });
    }
}
