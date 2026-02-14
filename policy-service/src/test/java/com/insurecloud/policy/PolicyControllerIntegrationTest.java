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

public class PolicyControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private SnsClient snsClient;

    /**
     * Initializes the test environment by clearing the policy repository
     * and ensuring the SNS topic exists in LocalStack.
     */
    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();
        // Create topic in LocalStack before test
        snsClient.createTopic(CreateTopicRequest.builder().name("policy-issued-topic").build());
    }

    /**
     * Tests the policy creation endpoint to ensure it correctly saves a policy
     * and returns a successful response.
     */
    @Test
    void shouldCreatePolicyAndReturnStatusOk() {
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
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPolicyNumber()).isEqualTo(policy.getPolicyNumber());
        assertThat(response.getBody().getStatus()).isEqualTo(Policy.PolicyStatus.ACTIVE);

        // Verify in DB
        assertThat(policyRepository.findAll()).hasSize(1);
    }
}
