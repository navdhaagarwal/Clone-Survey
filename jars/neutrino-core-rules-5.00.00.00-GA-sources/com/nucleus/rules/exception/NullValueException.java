package com.nucleus.rules.exception;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Null Value Exception class
 */

public class NullValueException extends RuntimeException {

    private static final long serialVersionUID = 5262639314218060140L;

    /**
     * 
     * Default Constructor
     */
    public NullValueException() {
        super();
    }

    /**
     * 
     * @param message
     */
    public NullValueException(String message) {
        super(message);
    }

    /**
     * 
     * @param message
     * @param cause
     */
    public NullValueException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     * @param cause
     */
    public NullValueException(Throwable cause) {
        super(cause);
    }
}
