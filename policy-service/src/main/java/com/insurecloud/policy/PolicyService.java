package com.insurecloud.policy;

import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final SnsTemplate snsTemplate;

    /**
     * Creates a new insurance policy, sets its status to ACTIVE, saves it to the repository,
     * and triggers a policy issued event.
     *
     * @param policy The policy object to be created.
     * @return The created policy with its generated ID and status.
     */
    @Transactional
    public Policy createPolicy(Policy policy) {
        log.info("Creating new policy for customer: {}", policy.getCustomerId());
        policy.setStatus(Policy.PolicyStatus.ACTIVE);
        Policy savedPolicy = policyRepository.save(policy);
        
        publishPolicyIssuedEvent(savedPolicy);
        
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
     * Publishes a PolicyIssuedEvent to the SNS topic for downstream services.
     *
     * @param policy The policy for which to publish the event.
     */
    private void publishPolicyIssuedEvent(Policy policy) {
        try {
            PolicyIssuedEvent event = new PolicyIssuedEvent(
                    policy.getId(),
                    policy.getPolicyNumber(),
                    policy.getCustomerId(),
                    policy.getPremiumAmount()
            );
            
            log.info("Publishing policy issued event to SNS: {}", event);
            snsTemplate.sendNotification("policy-issued-topic", event, "PolicyIssued");
        } catch (Exception e) {
            log.error("Failed to publish SNS event", e);
        }
    }
}
