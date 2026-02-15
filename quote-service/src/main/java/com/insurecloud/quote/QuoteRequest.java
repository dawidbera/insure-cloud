package com.insurecloud.quote;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequest {
    @NotBlank(message = "Product code is mandatory")
    private String productCode;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Min(value = 18, message = "Customer must be at least 18 years old")
    private int customerAge;
    
    @NotNull(message = "Asset value is mandatory")
    @Positive(message = "Asset value must be positive")
    private BigDecimal assetValue;
}
