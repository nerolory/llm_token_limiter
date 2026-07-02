package com.tokenlimiter.controller;

import com.tokenlimiter.dto.request.ConsumeCreditsRequest;
import com.tokenlimiter.dto.request.EstimateTokensRequest;
import com.tokenlimiter.dto.response.BalanceResponse;
import com.tokenlimiter.dto.response.ConsumeCreditsResponse;
import com.tokenlimiter.dto.response.EstimateTokensResponse;
import com.tokenlimiter.service.CreditService;
import com.tokenlimiter.service.TokenEstimationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for token balance operations and token estimation.
 */
@RestController
@RequestMapping("/api/v1/credits")
public class CreditController {

    private final CreditService creditService;
    private final TokenEstimationService tokenEstimationService;

    public CreditController(CreditService creditService, TokenEstimationService tokenEstimationService) {
        this.creditService = creditService;
        this.tokenEstimationService = tokenEstimationService;
    }

    /**
     * Deducts the requested number of tokens from the user's balance.
     *
     * @param request user identifier and token amount
     * @return remaining balance after deduction
     */
    @PostMapping("/consume")
    public ResponseEntity<ConsumeCreditsResponse> consumeCredits(
            @Valid @RequestBody ConsumeCreditsRequest request) {
        return ResponseEntity.ok(creditService.consumeCredits(request));
    }

    /**
     * Returns the current balance and account status for a user.
     *
     * @param userId external user identifier
     * @return balance and account status
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String userId) {
        return ResponseEntity.ok(creditService.getBalance(userId));
    }

    /**
     * Estimates the number of tokens in the given text for the specified model.
     *
     * @param request input text and target model
     * @return estimated token count
     */
    @PostMapping("/estimate")
    public ResponseEntity<EstimateTokensResponse> estimateTokens(
            @Valid @RequestBody EstimateTokensRequest request) {
        return ResponseEntity.ok(tokenEstimationService.estimateTokens(request));
    }
}
