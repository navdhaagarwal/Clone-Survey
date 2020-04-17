package com.nucleus.core.multilanguageletter;

import java.io.ByteArrayOutputStream;

import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.logging.BaseLoggers;
import com.sun.star.io.XOutputStream;

public class OpenOfficeOutputStream extends ByteArrayOutputStream implements XOutputStream {
	public OpenOfficeOutputStream() {
        super(32768);
    }


    //
    // Implement XOutputStream
    //
	@Override
    public void writeBytes(byte[] values) throws com.sun.star.io.IOException {
        try {
            this.write(values);
        }
        catch (java.io.IOException e) {
        	BaseLoggers.exceptionLogger.error(e.getMessage(),e);
            throw new com.sun.star.io.IOException(e.getMessage());
        }
    }

    @Override
    public void closeOutput() throws com.sun.star.io.IOException {
        try {
            super.flush();
            super.close();
        }
        catch (java.io.IOException e) {
        	BaseLoggers.exceptionLogger.error(e.getMessage(),e);
            throw new com.sun.star.io.IOException(e.getMessage());
        }
    }

    @Override
    public void flush() {
        try {
            super.flush();
        }
        catch (java.io.IOException e) {
        	 BaseLoggers.exceptionLogger.error(e.getMessage(),e);
 			throw ExceptionBuilder.getInstance(BusinessException.class, "IOEXCEPTION","IO Exception").build();
        }
    }
}
