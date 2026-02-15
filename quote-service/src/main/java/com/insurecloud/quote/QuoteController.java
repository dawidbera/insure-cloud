package com.insurecloud.quote;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    /**
     * Endpoint to calculate an insurance quote based on the provided request details.
     *
     * @param request The quote request details.
     * @return A ResponseEntity containing the calculated quote response.
     */
    @PostMapping
    public ResponseEntity<QuoteResponse> getQuote(@Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.ok(quoteService.calculateQuote(request));
    }
}
