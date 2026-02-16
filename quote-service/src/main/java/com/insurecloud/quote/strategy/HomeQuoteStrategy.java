package com.insurecloud.quote.strategy;

import com.insurecloud.quote.QuoteRequest;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategy for calculating home insurance premiums.
 * Applies a flat 0.2% base rate of the property value.
 */
@Component
public class HomeQuoteStrategy implements QuoteStrategy {

    private static final String PRODUCT_CODE = "HOME_INSURANCE";
    private static final BigDecimal BASE_RATE = new BigDecimal("0.002");

    /**
     * Calculates home insurance premium based on property value.
     *
     * @param request The quote request details.
     * @return The calculated premium.
     */
    @Override
    public BigDecimal calculate(QuoteRequest request) {
        return request.getAssetValue()
                .multiply(BASE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Checks if the product code is HOME_INSURANCE.
     *
     * @param productCode The product code to check.
     * @return True if supported.
     */
    @Override
    public boolean supports(String productCode) {
        return PRODUCT_CODE.equalsIgnoreCase(productCode);
    }
}
