package com.insurecloud.policy.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Client for interacting with the Quote Service.
 * Implements a Circuit Breaker pattern to ensure resilience.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuoteClient {

    private final RestTemplate restTemplate;

    @Value("${quote-service.url:http://quote-service/api/quotes}")
    private String quoteServiceUrl;

    /**
     * Calls the Quote Service to calculate a premium. 
     * Protected by a Circuit Breaker to handle service unavailability.
     *
     * @param request The quote request data.
     * @return The calculated quote response.
     */
    @CircuitBreaker(name = "quoteService", fallbackMethod = "fallbackCalculateQuote")
    public QuoteResponseDTO calculateQuote(QuoteRequestDTO request) {
        log.info("Calling quote-service for product: {} at URL: {}", request.productCode(), quoteServiceUrl);
        return restTemplate.postForObject(quoteServiceUrl, request, QuoteResponseDTO.class);
    }

    /**
     * Fallback method for calculateQuote, invoked when the Circuit Breaker is open 
     * or the call fails.
     *
     * @param request The original quote request.
     * @param t       The throwable that triggered the fallback.
     * @return A default fallback quote response.
     */
    public QuoteResponseDTO fallbackCalculateQuote(QuoteRequestDTO request, Throwable t) {
        log.error("Quote service is unavailable, using fallback. Error: {}", t.getMessage());
        return new QuoteResponseDTO("FALLBACK", new BigDecimal("1000.00"), null);
    }

    public record QuoteRequestDTO(String productCode, int customerAge, BigDecimal assetValue) {}
    public record QuoteResponseDTO(String quoteId, BigDecimal totalPremium, java.time.LocalDate expiryDate) {}
}
