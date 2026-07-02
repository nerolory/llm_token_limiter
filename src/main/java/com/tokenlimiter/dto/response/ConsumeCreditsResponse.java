package com.tokenlimiter.dto.response;

/**
 * Response after a successful token consumption.
 *
 * @param userId user identifier
 * @param remainingTokens balance after deduction
 * @param status operation result ({@code SUCCESS})
 */
public record ConsumeCreditsResponse(
        String userId,
        Long remainingTokens,
        String status) {
}
