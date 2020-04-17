package com.nucleus.rules.exception;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Rule Validation Exception class
 */

public class RuleValidationException extends RuleException {

    private static final long serialVersionUID = 8060493532639998136L;

    /**
     * 
     * Default Constructor
     */

    public RuleValidationException() {
        super();
    }

    /**
     * 
     * @param message
     */

    public RuleValidationException(String message) {
        super(message);
    }

    /**
     * 
     * @param message
     * @param cause
     */

    public RuleValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     * @param cause
     */

    public RuleValidationException(Throwable cause) {
        super(cause);
    }

}
