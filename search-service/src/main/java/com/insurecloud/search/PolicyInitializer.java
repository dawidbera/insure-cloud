package com.insurecloud.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initializes the Elasticsearch index with policies from the Policy Service.
 * This runs on application startup to sync existing policies that were created
 * before the Search Service was able to listen to events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "search.initialization.enabled", havingValue = "true", matchIfMissing = true)
public class PolicyInitializer {

    private final PolicySearchRepository policySearchRepository;
    private final RestTemplate restTemplate;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Fetches policies from the Policy Service and indexes them in Elasticsearch.
     * This method runs automatically when the Spring application is ready.
     * Uses a background thread with a delay to ensure discovery service is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeOnStartup() {
        Thread.startVirtualThread(() -> {
            try {
                // Wait for services to register in Eureka
                log.info("Waiting for discovery service to stabilize before first sync...");
                TimeUnit.SECONDS.sleep(30);
                initializePolicies();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Periodic synchronization to ensure consistency.
     * Also acts as a retry mechanism if startup initialization failed.
     */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void scheduledSync() {
        if (!initialized.get()) {
            initializePolicies();
        }
    }

    public synchronized void initializePolicies() {
        try {
            log.info("Starting policy synchronization for Elasticsearch");
            
            // Fetch existing policies from the Policy Service using Eureka load balancer
            ResponseEntity<List> response = restTemplate.getForEntity(
                "http://policy-service/api/policies",
                List.class
            );
            
            if (response.getBody() == null || response.getBody().isEmpty()) {
                log.info("No policies found to index");
                initialized.set(true);
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
            
            initialized.set(true);
        } catch (Exception e) {
            log.warn("Failed to synchronize policies in Elasticsearch: {}. Will retry in 5 minutes.", e.getMessage());
        }
    }
}
