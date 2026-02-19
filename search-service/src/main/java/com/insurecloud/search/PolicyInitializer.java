package com.insurecloud.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Initializes the Elasticsearch index with policies from the Policy Service.
 * This runs on application startup to sync existing policies that were created
 * before the Search Service was able to listen to events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyInitializer {

    private final PolicySearchRepository policySearchRepository;
    private final RestTemplate restTemplate;

    /**
     * Fetches policies from the Policy Service and indexes them in Elasticsearch.
     * This method runs automatically when the Spring application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializePolicies() {
        try {
            log.info("Starting policy initialization for Elasticsearch");
            
            // Fetch existing policies from the Policy Service
            ResponseEntity<List> response = restTemplate.getForEntity(
                "http://policy-service:8081/api/policies",
                List.class
            );
            
            if (response.getBody() == null || response.getBody().isEmpty()) {
                log.info("No policies found to index");
                return;
            }
            
            List<PolicyDocument> documents = new ArrayList<>();
            for (Object policyObj : response.getBody()) {
                if (policyObj instanceof Map) {
                    Map<String, Object> policy = (Map<String, Object>) policyObj;
                    
                    PolicyDocument document = PolicyDocument.builder()
                        .id(policy.get("id").toString())
                        .policyNumber(policy.get("policyNumber").toString())
                        .customerId(policy.get("customerId").toString())
                        .premiumAmount(
                            policy.get("premiumAmount") != null 
                                ? new java.math.BigDecimal(policy.get("premiumAmount").toString())
                                : java.math.BigDecimal.ZERO
                        )
                        .build();
                    
                    documents.add(document);
                }
            }
            
            if (!documents.isEmpty()) {
                policySearchRepository.saveAll(documents);
                log.info("Successfully indexed {} policies in Elasticsearch", documents.size());
            }
            
        } catch (Exception e) {
            log.warn("Failed to initialize policies in Elasticsearch: {}", e.getMessage());
            // Don't fail the application startup if initialization fails
        }
    }
}
