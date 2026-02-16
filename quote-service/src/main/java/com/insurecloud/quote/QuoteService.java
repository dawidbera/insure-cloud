package com.insurecloud.quote;

import com.insurecloud.quote.strategy.QuoteStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuoteService {

    private final List<QuoteStrategy> strategies;

    /**
     * Calculates an insurance premium quote based on the product code using specialized strategies.
     * Results are cached based on the request parameters.
     *
     * @param request The quote request details.
     * @return A QuoteResponse containing the unique quote ID, calculated premium, and expiry date.
     * @throws IllegalArgumentException if no strategy is found for the given product code.
     */
    @Cacheable(value = "quotes", key = "#request.productCode + #request.customerAge + #request.assetValue")
    public QuoteResponse calculateQuote(QuoteRequest request) {
        log.info("Calculating premium for product: {} and age: {}", request.getProductCode(), request.getCustomerAge());

        BigDecimal totalPremium = strategies.stream()
                .filter(strategy -> strategy.supports(request.getProductCode()))
                .findFirst()
                .map(strategy -> strategy.calculate(request))
                .orElseThrow(() -> {
                    log.error("No calculation strategy found for product code: {}", request.getProductCode());
                    return new IllegalArgumentException("Unsupported product code: " + request.getProductCode());
                });

        return QuoteResponse.builder()
                .quoteId(UUID.randomUUID().toString())
                .totalPremium(totalPremium)
                .expiryDate(LocalDate.now().plusDays(30))
                .build();
    }
}
