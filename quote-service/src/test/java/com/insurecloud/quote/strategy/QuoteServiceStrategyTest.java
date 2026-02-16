package com.insurecloud.quote.strategy;

import com.insurecloud.quote.QuoteRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class QuoteServiceStrategyTest {

    /**
     * Verifies that CarQuoteStrategy correctly calculates premium for young drivers
     * by applying the 5% base rate and 1.5x age multiplier.
     */
    @Test
    @DisplayName("CarQuoteStrategy should apply multiplier for young drivers")
    void carStrategyYoungDriver() {
        CarQuoteStrategy strategy = new CarQuoteStrategy();
        QuoteRequest request = QuoteRequest.builder()
                .productCode("CAR_INSURANCE")
                .customerAge(20)
                .assetValue(new BigDecimal("10000.00"))
                .build();

        BigDecimal result = strategy.calculate(request);
        
        // 10000 * 0.05 * 1.5 = 750
        assertThat(result).isEqualByComparingTo("750.00");
    }

    /**
     * Verifies that HomeQuoteStrategy applies a flat 0.2% rate to the asset value.
     */
    @Test
    @DisplayName("HomeQuoteStrategy should apply 0.2% rate")
    void homeStrategy() {
        HomeQuoteStrategy strategy = new HomeQuoteStrategy();
        QuoteRequest request = QuoteRequest.builder()
                .productCode("HOME_INSURANCE")
                .assetValue(new BigDecimal("500000.00"))
                .build();

        BigDecimal result = strategy.calculate(request);
        
        // 500000 * 0.002 = 1000
        assertThat(result).isEqualByComparingTo("1000.00");
    }

    /**
     * Verifies that LifeQuoteStrategy uses the age-based formula: (Age * 10) + (Asset * 1%).
     */
    @Test
    @DisplayName("LifeQuoteStrategy should use age-based formula")
    void lifeStrategy() {
        LifeQuoteStrategy strategy = new LifeQuoteStrategy();
        QuoteRequest request = QuoteRequest.builder()
                .productCode("LIFE_INSURANCE")
                .customerAge(30)
                .assetValue(new BigDecimal("100000.00"))
                .build();

        BigDecimal result = strategy.calculate(request);
        
        // (30 * 10) + (100000 * 0.01) = 300 + 1000 = 1300
        assertThat(result).isEqualByComparingTo("1300.00");
    }
}
