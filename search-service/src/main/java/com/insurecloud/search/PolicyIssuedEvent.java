package com.insurecloud.search;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event representing a policy that has been issued.
 * Used for deserializing messages from SQS.
 */
public record PolicyIssuedEvent(
    UUID policyId,
    String policyNumber,
    String customerId,
    BigDecimal premiumAmount
) {}
