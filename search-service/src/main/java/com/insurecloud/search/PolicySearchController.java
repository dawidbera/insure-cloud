package com.insurecloud.search;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for searching policies in Elasticsearch.
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class PolicySearchController {

    private final PolicySearchRepository policySearchRepository;

    /**
     * Searches for policies by customer ID.
     * Accessible by CUSTOMER or ADMIN.
     *
     * @param customerId The ID of the customer.
     * @return A list of matching policies.
     */
    @GetMapping("/by-customer")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public List<PolicyDocument> searchByCustomer(@RequestParam String customerId) {
        return policySearchRepository.findByCustomerId(customerId);
    }

    /**
     * Searches for a policy by its policy number.
     * Accessible by INSURANCE_AGENT or ADMIN.
     *
     * @param policyNumber The unique policy number.
     * @return A list containing the matching policy, if any.
     */
    @GetMapping("/by-number")
    @PreAuthorize("hasAnyRole('INSURANCE_AGENT', 'ADMIN')")
    public List<PolicyDocument> searchByNumber(@RequestParam String policyNumber) {
        return policySearchRepository.findByPolicyNumber(policyNumber);
    }
}
