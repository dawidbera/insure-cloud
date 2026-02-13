package com.insurecloud.policy;

import java.math.BigDecimal;
import java.util.UUID;

public record PolicyIssuedEvent(
    UUID policyId,
    String policyNumber,
    String customerId,
    BigDecimal premiumAmount
) {}
