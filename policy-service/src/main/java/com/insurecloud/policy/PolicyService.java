package com.insurecloud.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurecloud.policy.audit.AuditLogEntry;
import com.insurecloud.policy.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final OutboxRepository outboxRepository;
    private final AuditLogRepository auditLogRepository; // Inject AuditLogRepository
    private final ObjectMapper objectMapper;

    /**
     * Creates a new insurance policy, saves it to the database, and records an outbox event.
     * All operations are performed within a single transaction to ensure consistency.
     * Also saves an audit log entry to DynamoDB.
     *
     * @param policy The policy object to be created.
     * @return The created policy with generated ID and ACTIVE status.
     * @throws RuntimeException if saving the outbox event fails.
     */
    @Transactional
    public Policy createPolicy(Policy policy) {
        log.info("Creating new policy for customer: {}", policy.getCustomerId());
        policy.setStatus(Policy.PolicyStatus.ACTIVE);
        Policy savedPolicy = policyRepository.save(policy);
        
        saveOutboxEvent(savedPolicy);
        saveAuditLogEntry(savedPolicy, "POLICY_CREATED"); // Save audit log for creation
        
        return savedPolicy;
    }

    /**
     * Retrieves all insurance policies from the repository.
     *
     * @return A list of all existing policies.
     */
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    /**
     * Retrieves all audit log entries from DynamoDB.
     * Performs a full scan of the AuditLog table.
     *
     * @return A list of all audit logs.
     */
    public List<AuditLogEntry> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    /**
     * Serializes the policy issued event and saves it to the outbox table.
     *
     * @param policy The policy for which the event is being recorded.
     */
    private void saveOutboxEvent(Policy policy) {
        try {
            PolicyIssuedEvent event = new PolicyIssuedEvent(
                    policy.getId(),
                    policy.getPolicyNumber(),
                    policy.getCustomerId(),
                    policy.getPremiumAmount()
            );

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(policy.getId().toString())
                    .aggregateType("POLICY")
                    .eventType("PolicyIssued")
                    .payload(objectMapper.writeValueAsString(event))
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("Recorded outbox event for policy: {}", policy.getId());
        } catch (Exception e) {
            log.error("Failed to record outbox event", e);
            throw new RuntimeException("Event persistence failed", e);
        }
    }

    /**
     * Saves an audit log entry to DynamoDB for a given policy event.
     * In a real application, userId would typically come from the security context.
     *
     * @param policy The policy object related to the audit event.
     * @param eventType The type of audit event (e.g., POLICY_CREATED, POLICY_UPDATED).
     */
    private void saveAuditLogEntry(Policy policy, String eventType) {
        try {
            AuditLogEntry auditEntry = AuditLogEntry.builder()
                    .userId("system") // Placeholder, ideally extracted from security context (e.g., JWT)
                    .policyId(policy.getId().toString())
                    .eventType(eventType)
                    .timestamp(Instant.now())
                    .details(Map.of(
                            "policyNumber", policy.getPolicyNumber(),
                            "customerId", policy.getCustomerId(),
                            "status", policy.getStatus().name()
                    ))
                    .build();
            auditLogRepository.save(auditEntry);
            log.info("Recorded audit log entry for policy: {}", policy.getId());
        } catch (Exception e) {
            log.error("Failed to record audit log entry for policy: {}", policy.getId(), e);
            // Depending on requirements, audit log failures might be critical or non-critical.
            // For now, we log the error and allow the main transaction to proceed.
        }
    }
}
