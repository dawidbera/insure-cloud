package com.insurecloud.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Policy {
    private UUID id;
    private String policyNumber;
    private String customerId;
    private BigDecimal premiumAmount;
    private String status;
}
