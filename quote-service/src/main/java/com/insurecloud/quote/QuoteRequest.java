package com.insurecloud.quote;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequest {
    private String productCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private int customerAge;
    private BigDecimal assetValue;
}
