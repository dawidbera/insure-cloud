package com.insurecloud.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event representing a policy that has been issued.
 * Used for deserializing messages from SQS.
 */
public record PolicyIssuedEvent(
    @JsonProperty("policyId") UUID policyId,
    @JsonProperty("policyNumber") String policyNumber,
    @JsonProperty("customerId") String customerId,
    @JsonProperty("premiumAmount") BigDecimal premiumAmount
) {}
