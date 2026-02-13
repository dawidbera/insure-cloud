package com.insurecloud.quote;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String quoteId;
    private BigDecimal totalPremium;
    private LocalDate expiryDate;
}
