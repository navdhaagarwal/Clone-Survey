package com.nucleus.core.exceptions;

public class BulkUploadDataException extends BaseRuntimeException {

    private static final long serialVersionUID = 1L;

    public BulkUploadDataException() {
        super();
    }

    public BulkUploadDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public BulkUploadDataException(String message) {
        super(message);
    }

    public BulkUploadDataException(Throwable cause) {
        super(cause);
    }

}
