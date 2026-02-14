package com.insurecloud.quote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class QuoteControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Tests the quote calculation endpoint to ensure it returns a valid premium
     * based on the provided request details.
     */
    @Test
    void shouldCalculateQuote() {
        // Given
        QuoteRequest request = QuoteRequest.builder()
                .productCode("LIFE_INSURANCE")
                .customerAge(30)
                .assetValue(new BigDecimal("100000.00"))
                .build();

        // When
        ResponseEntity<QuoteResponse> response = restTemplate.postForEntity("/api/quotes", request, QuoteResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalPremium()).isNotNull();
        assertThat(response.getBody().getQuoteId()).isNotNull();
    }
}
