package com.insurecloud.quote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
public class QuoteService {

    @Cacheable(value = "quotes", key = "#request.productCode + #request.customerAge + #request.assetValue")
    public QuoteResponse calculateQuote(QuoteRequest request) {
        log.info("Calculating premium for product: {} and age: {}", request.getProductCode(), request.getCustomerAge());
        
        // Simulating some heavy calculation
        BigDecimal baseRate = new BigDecimal("0.05"); // 5% base rate
        BigDecimal ageMultiplier = request.getCustomerAge() < 25 ? new BigDecimal("1.5") : new BigDecimal("1.0");
        
        BigDecimal totalPremium = request.getAssetValue()
                .multiply(baseRate)
                .multiply(ageMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        return QuoteResponse.builder()
                .quoteId(UUID.randomUUID().toString())
                .totalPremium(totalPremium)
                .expiryDate(LocalDate.now().plusDays(30))
                .build();
    }
}
