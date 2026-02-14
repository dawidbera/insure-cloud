package com.insurecloud.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new insurance policy, saves it to the database, and records an outbox event.
     * All operations are performed within a single transaction to ensure consistency.
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
}
