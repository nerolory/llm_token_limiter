package com.tokenlimiter.exception;

/**
 * Thrown when a requested user does not exist in the database.
 */
public class UserNotFoundException extends RuntimeException {

    private final String userId;

    /**
     * @param userId identifier of the user that was not found
     */
    public UserNotFoundException(String userId) {
        super(String.format("User not found: '%s'", userId));
        this.userId = userId;
    }

    /** @return user identifier */
    public String getUserId() {
        return userId;
    }
}
