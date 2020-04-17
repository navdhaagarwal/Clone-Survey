package com.nucleus.rules.exception;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Rule Exception class
 */

public class RuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * Default Constructor
     */

    public RuleException() {
        super();
    }

    /**
     * 
     * @param message
     */

    public RuleException(String message) {
        super(message);
    }

    /**
     * 
     * @param message
     * @param cause
     */

    public RuleException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     * @param cause
     */

    public RuleException(Throwable cause) {
        super(cause);
    }
}
