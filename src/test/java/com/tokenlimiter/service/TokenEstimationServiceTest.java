package com.tokenlimiter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tokenlimiter.dto.request.EstimateTokensRequest;
import com.tokenlimiter.dto.response.EstimateTokensResponse;
import com.tokenlimiter.exception.InvalidModelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenEstimationServiceTest {

    private TokenEstimationService tokenEstimationService;

    @BeforeEach
    void setUp() {
        tokenEstimationService = new JTokkitTokenEstimationService();
    }

    @Test
    void estimateTokens_shouldReturnTokenCount_forKnownModel() {
        EstimateTokensRequest request = new EstimateTokensRequest(
                "Hello, can you please analyze this system architecture and provide feedback?",
                "gpt-4o");

        EstimateTokensResponse response = tokenEstimationService.estimateTokens(request);

        assertThat(response.estimatedTokens()).isPositive();
        assertThat(response.modelUsed()).isEqualTo("gpt-4o");
    }

    @Test
    void estimateTokens_shouldThrowInvalidModelException_forUnknownModel() {
        EstimateTokensRequest request = new EstimateTokensRequest("Hello world", "unknown-model-xyz");

        assertThatThrownBy(() -> tokenEstimationService.estimateTokens(request))
                .isInstanceOf(InvalidModelException.class)
                .satisfies(ex -> {
                    InvalidModelException ime = (InvalidModelException) ex;
                    assertThat(ime.getTargetModel()).isEqualTo("unknown-model-xyz");
                });
    }
}
