package com.insurecloud.search;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Listener for policy events from SQS.
 * Indexed policies in Elasticsearch for fast searching.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PolicySearchListener {

    private final PolicySearchRepository policySearchRepository;

    /**
     * Listens to the search-queue and indexes new policies.
     *
     * @param event The policy issued event received from SQS.
     */
    @SqsListener("search-queue")
    public void onPolicyIssued(PolicyIssuedEvent event) {
        log.info("Received PolicyIssuedEvent for indexing: {}", event);
        
        PolicyDocument document = PolicyDocument.builder()
                .id(event.policyId().toString())
                .policyNumber(event.policyNumber())
                .customerId(event.customerId())
                .premiumAmount(event.premiumAmount())
                .build();
        
        policySearchRepository.save(document);
        log.info("Policy indexed successfully: {}", event.policyId());
    }
}
