package com.insurecloud.quote;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<QuoteResponse> getQuote(@RequestBody QuoteRequest request) {
        return ResponseEntity.ok(quoteService.calculateQuote(request));
    }
}
