package com.tokenlimiter.exception;

/**
 * Thrown when the requested model has no matching tokenizer encoding.
 */
public class InvalidModelException extends RuntimeException {

    private final String targetModel;

    /**
     * @param targetModel model name from the request
     * @param message error description
     */
    public InvalidModelException(String targetModel, String message) {
        super(message);
        this.targetModel = targetModel;
    }

    /** @return model name from the request */
    public String getTargetModel() {
        return targetModel;
    }
}
