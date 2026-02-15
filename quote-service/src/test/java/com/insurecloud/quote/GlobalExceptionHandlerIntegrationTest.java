package com.insurecloud.quote;

import com.insurecloud.quote.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Global Exception Handling and Validation in Quote Service.
 */
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cloud.compatibility-verifier.enabled=false")
public class GlobalExceptionHandlerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Verifies that the Quote Service validates incoming requests.
     * Expects a 400 Bad Request when fields like productCode, customerAge, or assetValue are invalid.
     */
    @Test
    @DisplayName("Should return 400 Bad Request when quote request is invalid")
    void shouldReturn400WhenQuoteRequestIsInvalid() {
        // given: Invalid quote request (blank productCode, young customer, negative assetValue)
        QuoteRequest invalidRequest = QuoteRequest.builder()
                .productCode("")
                .customerAge(17)
                .assetValue(new BigDecimal("-500.00"))
                .build();

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/quotes", invalidRequest, ErrorResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Validation Failed");
        assertThat(response.getBody().errors())
                .containsEntry("productCode", "Product code is mandatory")
                .containsEntry("customerAge", "Customer must be at least 18 years old")
                .containsEntry("assetValue", "Asset value must be positive");
    }
}
