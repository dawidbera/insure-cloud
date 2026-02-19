package com.insurecloud.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public record PolicyIssuedEvent(
    @JsonProperty("policyId") UUID policyId,
    @JsonProperty("policyNumber") String policyNumber,
    @JsonProperty("customerId") String customerId,
    @JsonProperty("premiumAmount") BigDecimal premiumAmount
) {}
