package com.nucleus.core.exceptions;

public abstract class BaseRuntimeException extends RuntimeException {
//Exception
    public BaseRuntimeException() {
        super();
    }

    public BaseRuntimeException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public BaseRuntimeException(String arg0) {
        super(arg0);
    }

    public BaseRuntimeException(Throwable arg0) {
        super(arg0);
    }

}
