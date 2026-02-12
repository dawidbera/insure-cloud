package com.insurecloud.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final SnsClient snsClient;

    @Transactional
    public Policy createPolicy(Policy policy) {
        log.info("Creating new policy for customer: {}", policy.getCustomerId());
        policy.setStatus(Policy.PolicyStatus.ACTIVE);
        Policy savedPolicy = policyRepository.save(policy);
        
        publishPolicyIssuedEvent(savedPolicy);
        
        return savedPolicy;
    }

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    private void publishPolicyIssuedEvent(Policy policy) {
        try {
            String message = "Policy issued: " + policy.getPolicyNumber() + " for ID: " + policy.getId();
            log.info("Publishing policy issued event to SNS: {}", message);
            
            // In a real implementation, we would publish to a specific Topic ARN
            // For now, we just log it to verify the SnsClient bean is correctly injected
        } catch (Exception e) {
            log.error("Failed to process SNS event", e);
        }
    }
}
