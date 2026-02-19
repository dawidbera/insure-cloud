package com.insurecloud.document;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@org.springframework.web.bind.annotation.CrossOrigin(origins = "http://localhost:4200")
public class DocumentController {

    private final S3Template s3Template;
    private static final String BUCKET_NAME = "policy-documents";

    /**
     * Endpoint to download a policy document from S3.
     *
     * @param policyNumber The policy number to identify the document.
     * @return The PDF document as a downloadable resource.
     */
    @GetMapping("/{policyNumber}")
    public ResponseEntity<?> downloadDocument(@PathVariable String policyNumber) {
        String fileName = "policy_" + policyNumber + ".pdf";
        log.info("Request to download document: {}", fileName);

        try {
            S3Resource resource = s3Template.download(BUCKET_NAME, fileName);
            
            if (!resource.exists()) {
                log.warn("Document not found in S3: {}", fileName);
                return ResponseEntity.status(404).body(
                    new ErrorResponse("Document not found", "The document for policy " + policyNumber + " is not available yet.")
                );
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to download document: {} - Document not found or S3 access error", fileName, e);
            return ResponseEntity.status(404).body(
                new ErrorResponse("Document not found", "The document for policy " + policyNumber + " is not available. Please ensure the policy has been processed and the document has been generated.")
            );
        }
    }
    
    /**
     * Simple error response DTO.
     */
    static class ErrorResponse {
        public String error;
        public String message;
        
        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
        
        public String getError() { return error; }
        public String getMessage() { return message; }
    }
}
