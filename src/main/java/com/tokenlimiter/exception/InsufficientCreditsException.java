package com.tokenlimiter.exception;

/**
 * Thrown when a user attempts to consume more tokens than available on the account.
 */
public class InsufficientCreditsException extends RuntimeException {

    private final String userId;
    private final Long requestedTokens;
    private final Long availableTokens;

    /**
     * @param userId user identifier
     * @param requestedTokens number of tokens requested
     * @param availableTokens balance at the time of the check
     */
    public InsufficientCreditsException(String userId, Long requestedTokens, Long availableTokens) {
        super(String.format(
                "Insufficient credits for user '%s': requested %d, available %d",
                userId,
                requestedTokens,
                availableTokens));
        this.userId = userId;
        this.requestedTokens = requestedTokens;
        this.availableTokens = availableTokens;
    }

    /** @return user identifier */
    public String getUserId() {
        return userId;
    }

    /** @return requested token count */
    public Long getRequestedTokens() {
        return requestedTokens;
    }

    /** @return available balance */
    public Long getAvailableTokens() {
        return availableTokens;
    }
}
