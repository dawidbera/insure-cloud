package com.insurecloud.quote.strategy;

import com.insurecloud.quote.QuoteRequest;
import java.math.BigDecimal;

/**
 * Interface for insurance premium calculation strategies.
 * Implementing classes should provide logic for specific product types.
 */
public interface QuoteStrategy {

    /**
     * Calculates the insurance premium based on the provided request.
     *
     * @param request The quote request details.
     * @return The calculated premium amount.
     */
    BigDecimal calculate(QuoteRequest request);

    /**
     * Determines if this strategy supports the given product code.
     *
     * @param productCode The product code to check.
     * @return True if this strategy can handle the product code, false otherwise.
     */
    boolean supports(String productCode);
}
