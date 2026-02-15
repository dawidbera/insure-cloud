package com.insurecloud.quote;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    /**
     * Endpoint to calculate an insurance quote based on the provided request details.
     * Accessible by CUSTOMER or INSURANCE_AGENT roles.
     *
     * @param request The quote request details.
     * @return A ResponseEntity containing the calculated quote response.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'INSURANCE_AGENT')")
    public ResponseEntity<QuoteResponse> getQuote(@Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.ok(quoteService.calculateQuote(request));
    }
}
