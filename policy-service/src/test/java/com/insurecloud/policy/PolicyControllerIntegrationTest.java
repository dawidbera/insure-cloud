package com.insurecloud.policy;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurecloud.policy.audit.AuditLogEntry;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link PolicyController}.
 * These tests cover the end-to-end flow of policy creation, including
 * transactional outbox processing and audit log creation in DynamoDB using Testcontainers.
 */
@AutoConfigureMockMvc
public class PolicyControllerIntegrationTest extends AbstractIntegrationTest {

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
    private AmazonDynamoDB amazonDynamoDB; // Injected to manage DynamoDB tables in tests
    
    @Autowired
    private DynamoDBMapper dynamoDBMapper; // Injected to interact with DynamoDB for verification

    /**
     * Prepares the test environment before each test.
     * This includes:
     * - Clearing the JPA repositories (policies and outbox events).
     * - Ensuring the DynamoDB AuditLog table exists and clearing its contents.
     * - Creating the necessary SNS topic for policy events.
     */
    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();
        outboxRepository.deleteAll();

        // Ensure DynamoDB AuditLog table exists and is clean for each test
        CreateTableRequest createTableRequest = dynamoDBMapper.generateCreateTableRequest(AuditLogEntry.class)
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        if (!amazonDynamoDB.listTables().getTableNames().contains("AuditLog")) {
            amazonDynamoDB.createTable(createTableRequest);
        }

        // Clear existing audit logs for a clean test run
        List<AuditLogEntry> allAuditLogs = dynamoDBMapper.scan(AuditLogEntry.class, new DynamoDBScanExpression());
        if (!allAuditLogs.isEmpty()) {
            dynamoDBMapper.batchDelete(allAuditLogs);
        }

        // Ensure SNS topic exists
        try {
            snsClient.createTopic(CreateTopicRequest.builder().name("policy-issued-topic").build());
        } catch (Exception e) {
            // Topic might already exist in LocalStack, log or ignore if expected
        }
    }

    /**
     * Tests the comprehensive policy creation flow, verifying:
     * 1. Successful policy creation via the REST API with appropriate security roles.
     * 2. Immediate creation of an unprocessed outbox event in the database.
     * 3. Eventual processing of the outbox event by the OutboxProcessor.
     * 4. Creation of a corresponding audit log entry in DynamoDB.
     */
    @Test
    void shouldCreatePolicyAndProcessOutboxEventAndAuditLog() throws Exception {
        // Given a new policy to be created
        Policy policy = Policy.builder()
                .policyNumber("POL-" + UUID.randomUUID())
                .customerId("CUST-123")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .premiumAmount(new BigDecimal("500.00"))
                .status(Policy.PolicyStatus.DRAFT)
                .build();

        // When a POST request is made to create the policy with an authorized agent role
        mockMvc.perform(post("/api/policies")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_INSURANCE_AGENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policy)))
                .andExpect(status().isOk()); // Then the API call should be successful
        
        // Verify that an unprocessed outbox event exists immediately after policy creation
        List<OutboxEvent> outboxEvents = outboxRepository.findAll();
        assertThat(outboxEvents).hasSize(1);
        assertThat(outboxEvents.get(0).isProcessed()).isFalse();

        // Wait for the OutboxProcessor to asynchronously process the event
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    List<OutboxEvent> processedEvents = outboxRepository.findAll();
                    assertThat(processedEvents.get(0).isProcessed()).isTrue(); // Verify event is marked as processed
                    assertThat(processedEvents.get(0).getProcessedAt()).isNotNull(); // Verify processed timestamp is set
                });

        // Verify that an audit log entry has been created in DynamoDB
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    List<AuditLogEntry> auditLogs = dynamoDBMapper.scan(AuditLogEntry.class, new DynamoDBScanExpression());
                    assertThat(auditLogs).hasSize(1); // Expect one audit log entry
                    assertThat(auditLogs.get(0).getEventType()).isEqualTo("POLICY_CREATED"); // Verify event type
                    assertThat(auditLogs.get(0).getPolicyId()).isNotNull(); // Verify policy ID is present
                    assertThat(auditLogs.get(0).getDetails().get("policyNumber")).isEqualTo(policy.getPolicyNumber()); // Verify policy number in details
                });
    }
}
