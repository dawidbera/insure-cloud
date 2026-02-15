package com.insurecloud.quote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurecloud.quote.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Global Exception Handling and Validation in Quote Service.
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class GlobalExceptionHandlerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Verifies that the Quote Service validates incoming requests.
     * Expects a 400 Bad Request when fields like productCode, customerAge, or assetValue are invalid.
     */
    @Test
    @DisplayName("Should return 400 Bad Request when quote request is invalid")
    void shouldReturn400WhenQuoteRequestIsInvalid() throws Exception {
        // given: Invalid quote request (blank productCode, young customer, negative assetValue)
        QuoteRequest invalidRequest = QuoteRequest.builder()
                .productCode("")
                .customerAge(17)
                .assetValue(new BigDecimal("-500.00"))
                .build();

        // when & then
        mockMvc.perform(post("/api/quotes")
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.productCode").value("Product code is mandatory"))
                .andExpect(jsonPath("$.errors.customerAge").value("Customer must be at least 18 years old"))
                .andExpect(jsonPath("$.errors.assetValue").value("Asset value must be positive"));
    }
}
