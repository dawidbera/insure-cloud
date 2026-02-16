package com.insurecloud.quote.strategy;

import com.insurecloud.quote.QuoteRequest;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategy for calculating life insurance premiums.
 * Premium depends on customer age and insured amount.
 * Formula: (Age * 10) + (Insured Amount * 1.0%)
 */
@Component
public class LifeQuoteStrategy implements QuoteStrategy {

    private static final String PRODUCT_CODE = "LIFE_INSURANCE";
    private static final BigDecimal AGE_FACTOR = new BigDecimal("10.00");
    private static final BigDecimal ASSET_FACTOR = new BigDecimal("0.01");

    /**
     * Calculates life insurance premium based on age and insured amount.
     *
     * @param request The quote request details.
     * @return The calculated premium.
     */
    @Override
    public BigDecimal calculate(QuoteRequest request) {
        BigDecimal agePremium = BigDecimal.valueOf(request.getCustomerAge()).multiply(AGE_FACTOR);
        BigDecimal assetPremium = request.getAssetValue().multiply(ASSET_FACTOR);
        
        return agePremium.add(assetPremium).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Checks if the product code is LIFE_INSURANCE.
     *
     * @param productCode The product code to check.
     * @return True if supported.
     */
    @Override
    public boolean supports(String productCode) {
        return PRODUCT_CODE.equalsIgnoreCase(productCode);
    }
}
