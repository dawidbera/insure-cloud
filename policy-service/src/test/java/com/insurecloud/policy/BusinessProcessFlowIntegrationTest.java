package com.insurecloud.policy;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurecloud.policy.audit.AuditLogEntry;
import com.insurecloud.policy.client.QuoteClient;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-End Integration test for the full insurance business process.
 * 
 * Scenarios covered:
 * 1. Mocking Quote Service call via WireMock.
 * 2. Creating a policy via Policy Service REST API.
 * 3. Verifying Transactional Outbox consistency.
 * 4. Verifying async processing by OutboxProcessor.
 * 5. Verifying Audit Log entry in DynamoDB.
 */
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
    "quote-service.url=http://localhost:${wiremock.server.port}/api/quotes",
    "spring.main.allow-bean-definition-overriding=true"
})
public class BusinessProcessFlowIntegrationTest extends AbstractIntegrationTest {

    /**
     * Test-specific configuration to override the production RestTemplate.
     * Overriding is required to bypass the @LoadBalanced annotation during tests,
     * allowing the client to call WireMock on 'localhost' instead of searching Eureka.
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private SnsClient snsClient;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private QuoteClient quoteClient;

    /**
     * Sets up the environment for each test execution.
     * Ensures repositories are empty, DynamoDB AuditLog table exists, 
     * and required SNS topics are present in LocalStack.
     */
    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();
        outboxRepository.deleteAll();

        // Setup DynamoDB table
        CreateTableRequest createTableRequest = dynamoDBMapper.generateCreateTableRequest(AuditLogEntry.class)
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        if (!amazonDynamoDB.listTables().getTableNames().contains("AuditLog")) {
            amazonDynamoDB.createTable(createTableRequest);
        }

        List<AuditLogEntry> allAuditLogs = dynamoDBMapper.scan(AuditLogEntry.class, new DynamoDBScanExpression());
        if (!allAuditLogs.isEmpty()) {
            dynamoDBMapper.batchDelete(allAuditLogs);
        }

        // Ensure SNS topic exists
        try {
            snsClient.createTopic(CreateTopicRequest.builder().name("policy-issued-topic").build());
        } catch (Exception ignored) {}
    }

    /**
     * Verifies the full E2E flow:
     * 1. Mocking an external Quote Service dependency.
     * 2. Authenticated policy issuance via REST.
     * 3. Database persistence check.
     * 4. Asynchronous Outbox processing and event fan-out.
     * 5. Audit log generation in DynamoDB.
     */
    @Test
    void shouldCompleteFullBusinessProcessFlow() throws Exception {
        // --- STEP 1: Calculate Quote (Mocked) ---
        String productCode = "CAR_INSURANCE";
        BigDecimal assetValue = new BigDecimal("50000");
        BigDecimal expectedPremium = new BigDecimal("2500.00");

        stubFor(WireMock.post(urlEqualTo("/api/quotes"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(
                                new QuoteClient.QuoteResponseDTO(UUID.randomUUID().toString(), expectedPremium, LocalDate.now().plusDays(30))
                        ))));

        QuoteClient.QuoteRequestDTO quoteRequest = new QuoteClient.QuoteRequestDTO(productCode, 30, assetValue);
        QuoteClient.QuoteResponseDTO quoteResponse = quoteClient.calculateQuote(quoteRequest);

        assertThat(quoteResponse.totalPremium()).isEqualByComparingTo(expectedPremium);

        // --- STEP 2: Issue Policy ---
        Policy policy = Policy.builder()
                .policyNumber("POL-E2E-" + UUID.randomUUID())
                .customerId("CUST-TEST")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .premiumAmount(quoteResponse.totalPremium())
                .status(Policy.PolicyStatus.DRAFT)
                .build();

        mockMvc.perform(post("/api/policies")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_INSURANCE_AGENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policy)))
                .andExpect(status().isOk());

        // --- STEP 3: Verify Persistence & Outbox ---
        List<Policy> policies = policyRepository.findAll();
        assertThat(policies).hasSize(1);
        assertThat(policies.get(0).getPremiumAmount()).isEqualByComparingTo(expectedPremium);

        // --- STEP 4: Verify Async Processing (Outbox & Audit Log) ---
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    List<OutboxEvent> processedEvents = outboxRepository.findAll();
                    assertThat(processedEvents).isNotEmpty();
                    assertThat(processedEvents.get(0).isProcessed()).isTrue();

                    List<AuditLogEntry> auditLogs = dynamoDBMapper.scan(AuditLogEntry.class, new DynamoDBScanExpression());
                    assertThat(auditLogs).hasSize(1);
                    assertThat(auditLogs.get(0).getDetails().get("policyNumber")).isEqualTo(policy.getPolicyNumber());
                });
    }
}
