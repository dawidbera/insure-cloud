package com.insurecloud.quote.strategy;

import com.insurecloud.quote.QuoteRequest;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategy for calculating car insurance premiums.
 * Applies a 5% base rate and a 1.5x multiplier for young drivers (under 25).
 */
@Component
public class CarQuoteStrategy implements QuoteStrategy {

    private static final String PRODUCT_CODE = "CAR_INSURANCE";
    private static final BigDecimal BASE_RATE = new BigDecimal("0.05");
    private static final BigDecimal YOUNG_DRIVER_MULTIPLIER = new BigDecimal("1.5");

    /**
     * Calculates car insurance premium.
     * Higher risk is assumed for drivers under 25.
     *
     * @param request The quote request details.
     * @return The calculated premium.
     */
    @Override
    public BigDecimal calculate(QuoteRequest request) {
        BigDecimal basePremium = request.getAssetValue().multiply(BASE_RATE);
        
        if (request.getCustomerAge() < 25) {
            basePremium = basePremium.multiply(YOUNG_DRIVER_MULTIPLIER);
        }
        
        return basePremium.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Checks if the product code is CAR_INSURANCE.
     *
     * @param productCode The product code to check.
     * @return True if supported.
     */
    @Override
    public boolean supports(String productCode) {
        return PRODUCT_CODE.equalsIgnoreCase(productCode);
    }
}
