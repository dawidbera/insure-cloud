package com.insurecloud.policy;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    /**
     * Endpoint to create a new insurance policy.
     * Restricted to users with the 'INSURANCE_AGENT' role.
     *
     * @param policy The policy details provided in the request body.
     * @return A ResponseEntity containing the created policy.
     */
    @PostMapping
    @PreAuthorize("hasRole('INSURANCE_AGENT')")
    public ResponseEntity<Policy> createPolicy(@Valid @RequestBody Policy policy) {
        return ResponseEntity.ok(policyService.createPolicy(policy));
    }

    /**
     * Endpoint to retrieve all insurance policies.
     * Restricted to users with the 'ADMIN' role.
     *
     * @return A ResponseEntity containing a list of all policies.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Policy>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }
}
