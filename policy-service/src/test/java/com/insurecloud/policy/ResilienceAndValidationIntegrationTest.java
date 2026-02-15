package com.insurecloud.policy;

import com.insurecloud.policy.client.QuoteClient;
import com.insurecloud.policy.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Integration tests for Resilience (Circuit Breaker) and Validation logic.
 * Uses WireMock to simulate external service failures and TestRestTemplate
 * to verify global error handling.
 */
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cloud.compatibility-verifier.enabled=false")
public class ResilienceAndValidationIntegrationTest extends AbstractIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RestTemplate testRestTemplate() {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(500);
            factory.setReadTimeout(500);
            return new RestTemplate(factory);
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private QuoteClient quoteClient;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // Redirect client calls to the local WireMock server port
        registry.add("quote-service.url", () -> "http://localhost:${wiremock.server.port}/api/quotes");
    }

    /**
     * Verifies that the Policy Service strictly validates input data.
     * Expects a 400 Bad Request with a detailed error map when required fields are missing or invalid.
     */
    @Test
    @DisplayName("Should return 400 Bad Request when policy data is invalid")
    void shouldReturn400WhenPolicyIsInvalid() {
        // given: Invalid policy (missing customerId, negative premium)
        Policy invalidPolicy = Policy.builder()
                .policyNumber("INV-001")
                .customerId("")
                .startDate(LocalDate.now().minusDays(1)) // Past date
                .endDate(LocalDate.now().plusYears(1))
                .premiumAmount(new BigDecimal("-100.00"))
                .build();

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/policies", invalidPolicy, ErrorResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors()).containsKeys("customerId", "startDate", "premiumAmount");
    }

    /**
     * Verifies the Circuit Breaker's fallback mechanism when the upstream Quote Service fails.
     * Simulates a 500 Internal Server Error from Quote Service and expects a fallback quote.
     */
    @Test
    @DisplayName("Should trigger fallback when quote-service returns error")
    void shouldTriggerFallbackWhenQuoteServiceFails() {
        // given: WireMock stub for quote-service error
        stubFor(post(urlEqualTo("/api/quotes"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));

        // when: Calling the client directly to test Resilience4j logic
        QuoteClient.QuoteRequestDTO request = new QuoteClient.QuoteRequestDTO("CAR", 25, new BigDecimal("50000"));
        QuoteClient.QuoteResponseDTO response = quoteClient.calculateQuote(request);

        // then: Fallback premium should be returned
        assertThat(response.quoteId()).isEqualTo("FALLBACK");
        assertThat(response.totalPremium()).isEqualByComparingTo("1000.00");
        
        // Verify WireMock was actually called
        verify(postRequestedFor(urlEqualTo("/api/quotes")));
    }

    /**
     * Verifies the Circuit Breaker's fallback mechanism when the upstream Quote Service is too slow.
     * Simulates a response delay exceeding the configured timeout and expects a fallback quote.
     */
    @Test
    @DisplayName("Should trigger fallback when quote-service is slow (Timeout Simulation)")
    void shouldTriggerFallbackWhenQuoteServiceIsSlow() {
        // given: WireMock stub with delay
        stubFor(post(urlEqualTo("/api/quotes"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(2000) // 2 seconds delay
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"quoteId\":\"SLOW\", \"totalPremium\": 500.0}")));

        // when
        QuoteClient.QuoteRequestDTO request = new QuoteClient.QuoteRequestDTO("CAR", 25, new BigDecimal("50000"));
        QuoteClient.QuoteResponseDTO response = quoteClient.calculateQuote(request);

        // then: Even if it eventually returns, the circuit breaker should have triggered 
        // if timeout was shorter or if we are testing the fallback mechanism on error.
        // Note: For real timeout testing, we'd need to configure Resilience4j TimeLimiter.
        // For now, any exception triggers fallback in our client.
        assertThat(response.quoteId()).isEqualTo("FALLBACK");
    }
}
