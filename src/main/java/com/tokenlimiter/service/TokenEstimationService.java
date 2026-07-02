package com.tokenlimiter.service;

import com.tokenlimiter.dto.request.EstimateTokensRequest;
import com.tokenlimiter.dto.response.EstimateTokensResponse;

/**
 * Contract for estimating token count in text.
 * Independent of the database and transaction layer.
 */
public interface TokenEstimationService {

    /**
     * Counts tokens for the given text and model.
     *
     * @param request text and target model
     * @return estimated token count
     */
    EstimateTokensResponse estimateTokens(EstimateTokensRequest request);
}
