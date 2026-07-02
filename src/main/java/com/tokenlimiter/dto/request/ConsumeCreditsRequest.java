package com.tokenlimiter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request to deduct tokens from a user's balance.
 *
 * @param userId external user identifier
 * @param tokensRequested number of tokens to deduct (must be positive)
 */
public record ConsumeCreditsRequest(
        @NotBlank String userId,
        @NotNull @Positive Long tokensRequested) {
}
