package com.insurecloud.policy;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    /**
     * Endpoint to create a new insurance policy.
     *
     * @param policy The policy details provided in the request body.
     * @return A ResponseEntity containing the created policy.
     */
    @PostMapping
    public ResponseEntity<Policy> createPolicy(@Valid @RequestBody Policy policy) {
        return ResponseEntity.ok(policyService.createPolicy(policy));
    }

    /**
     * Endpoint to retrieve all insurance policies.
     *
     * @return A ResponseEntity containing a list of all policies.
     */
    @GetMapping
    public ResponseEntity<List<Policy>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }
}
