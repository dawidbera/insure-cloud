package com.insurecloud.document;

import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentListener {

    private final DocumentGenerator documentGenerator;
    private final S3Template s3Template;
    private static final String BUCKET_NAME = "policy-documents";

    /**
     * SQS Listener that handles policy issued events.
     * Generates a PDF document for the policy and uploads it to an S3 bucket.
     *
     * @param event The policy issued event received from the queue.
     */
    @SqsListener("document-queue")
    public void onPolicyIssued(PolicyIssuedEvent event) {
        log.info("Received policy issued event for document generation: {}", event.policyNumber());
        
        try {
            InputStream pdfStream = documentGenerator.generatePolicyPdf(event);
            String fileName = "policy_" + event.policyNumber() + ".pdf";
            
            s3Template.upload(BUCKET_NAME, fileName, pdfStream);
            log.info("Successfully generated and uploaded document: {} to bucket: {}", fileName, BUCKET_NAME);
        } catch (Exception e) {
            log.error("Failed to generate or upload document", e);
        }
    }
}
