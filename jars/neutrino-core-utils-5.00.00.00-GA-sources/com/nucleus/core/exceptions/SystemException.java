package com.nucleus.core.exceptions;

/**
 * 
 * @author Nucleus Software Exports Limited
 * System Exception class
 */

public class SystemException extends BaseRuntimeException {

    private static final long serialVersionUID = 1L;

    public SystemException() {
        super();
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }

}
