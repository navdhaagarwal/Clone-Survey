package com.nucleus.rules.service;

public class ValidationError {

    private final String resourceKey;

    private final String errorMessage;

    public ValidationError(String resourceKey, String errorMessage) {
        this.resourceKey = resourceKey;
        this.errorMessage = errorMessage;
    }

    /**
     * @return the resourceKey
     */
    public String getResourceKey() {
        return resourceKey;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
