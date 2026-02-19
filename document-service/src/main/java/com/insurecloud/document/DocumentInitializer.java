package com.insurecloud.document;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentInitializer {

    private final DocumentGenerator documentGenerator;
    private final S3Template s3Template;
    private final RestTemplate restTemplate;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final String BUCKET_NAME = "policy-documents";
    private static final String POLICY_SERVICE_URL = "http://policy-service/api/policies";

    /**
     * Initialize documents for existing policies after application has been fully registered.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDocumentsForExistingPolicies() {
        Thread.startVirtualThread(() -> {
            try {
                log.info("Waiting for discovery service to stabilize before document sync...");
                TimeUnit.SECONDS.sleep(30);
                performInitialization();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Periodic check to ensure all policies have documents.
     */
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    public void scheduledSync() {
        if (!initialized.get()) {
            performInitialization();
        }
    }

    /**
     * Performs the actual document initialization.
     */
    public synchronized void performInitialization() {
        log.info("Starting document initialization for existing policies");
        
        try {
            // Fetch all existing policies from policy-service
            Policy[] policiesArray = restTemplate.getForObject(POLICY_SERVICE_URL, Policy[].class);
            
            if (policiesArray == null || policiesArray.length == 0) {
                log.info("No existing policies found for document generation");
                return;
            }
            
            log.info("Found {} existing policies for document generation", policiesArray.length);
            
            int successCount = 0;
            
            for (Policy policy : policiesArray) {
                try {
                    String fileName = "policy_" + policy.getPolicyNumber() + ".pdf";
                    
                    // Create a PolicyIssuedEvent from the policy for PDF generation
                    PolicyIssuedEvent event = new PolicyIssuedEvent(
                        policy.getId(),
                        policy.getPolicyNumber(),
                        policy.getCustomerId(),
                        policy.getPremiumAmount()
                    );
                    
                    // Generate PDF
                    InputStream pdfStream = documentGenerator.generatePolicyPdf(event);
                    
                    // Upload to S3
                    s3Template.upload(BUCKET_NAME, fileName, pdfStream);
                    log.info("Successfully generated and uploaded document for policy: {}", policy.getPolicyNumber());
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to generate document for policy: {}", policy.getPolicyNumber(), e);
                }
            }
            
            log.info("Document initialization complete. Successfully generated: {}", successCount);
            initialized.set(true);
        } catch (Exception e) {
            log.error("Failed to initialize documents for existing policies", e);
        }
    }
}
