package com.tokenlimiter.dto.response;

/**
 * Result of token count estimation for the given text.
 *
 * @param estimatedTokens estimated number of tokens
 * @param modelUsed model used for the calculation
 */
public record EstimateTokensResponse(
        Long estimatedTokens,
        String modelUsed) {
}
