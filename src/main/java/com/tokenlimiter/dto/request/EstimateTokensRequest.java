package com.tokenlimiter.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to estimate token count in text.
 *
 * @param inputText source text to count
 * @param targetModel model name whose BPE encoding will be used
 */
public record EstimateTokensRequest(
        @NotBlank String inputText,
        @NotBlank String targetModel) {
}
