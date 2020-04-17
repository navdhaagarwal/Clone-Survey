package com.nucleus.core.exceptions;

/**
 * 
 * @author Nucleus Software Exports Limited
 * System Exception class
 */

public class OperationNotSupportedException extends BaseRuntimeException {

    private static final long serialVersionUID = 1L;

    public OperationNotSupportedException() {
        super();
    }

    public OperationNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperationNotSupportedException(String message) {
        super(message);
    }

    public OperationNotSupportedException(Throwable cause) {
        super(cause);
    }

}
