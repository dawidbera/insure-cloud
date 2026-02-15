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

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class PolicySearchIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private SqsAsyncClient sqsAsyncClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolicySearchRepository policySearchRepository;

    /**
     * Prepares the test environment before each test case.
     * Clears the Elasticsearch index and ensures the SQS queue exists.
     */
    @BeforeEach
    void setUp() {
        policySearchRepository.deleteAll();
        sqsAsyncClient.createQueue(builder -> builder.queueName("search-queue")).join();
    }

    /**
     * Verifies the end-to-end flow of policy indexing and searching.
     * 1. Sends a PolicyIssuedEvent to SQS.
     * 2. Waits for the event to be indexed in Elasticsearch.
     * 3. Verifies retrieval via the REST API with a valid JWT.
     */
    @Test
    void shouldIndexAndSearchPolicy() throws Exception {
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
        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    List<PolicyDocument> documents = policySearchRepository.findByCustomerId("CUST-001");
                    assertThat(documents).hasSize(1);
                    assertThat(documents.get(0).getPolicyNumber()).isEqualTo("POL-123");
                });

        // And - Test REST API with Admin role
        mockMvc.perform(get("/api/search/by-customer")
                        .param("customerId", "CUST-001")
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].policyNumber").value("POL-123"));
    }
}
