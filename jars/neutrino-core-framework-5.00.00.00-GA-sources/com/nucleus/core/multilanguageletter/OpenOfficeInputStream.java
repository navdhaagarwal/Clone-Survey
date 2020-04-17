package com.nucleus.core.multilanguageletter;

import java.io.ByteArrayInputStream;

import com.nucleus.logging.BaseLoggers;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XSeekable;

public class OpenOfficeInputStream extends ByteArrayInputStream implements XInputStream, XSeekable{

	public OpenOfficeInputStream(byte[] buf) {
        super(buf);
    }


    //
    // Implement XInputStream
    //

    public int readBytes(byte[][] buffer, int bufferSize) throws  com.sun.star.io.IOException {
        int numberOfReadBytes;
        try {
            byte[] bytes = new byte[bufferSize];
            numberOfReadBytes = super.read(bytes);
            if(numberOfReadBytes > 0) {
                if(numberOfReadBytes < bufferSize) {
                    byte[] smallerBuffer = new byte[numberOfReadBytes];
                    System.arraycopy(bytes, 0, smallerBuffer, 0, numberOfReadBytes);
                    bytes = smallerBuffer;
                }
            }
            else {
                bytes = new byte[0];
                numberOfReadBytes = 0;
            }

            buffer[0]=bytes;
            return numberOfReadBytes;
        }
        catch (java.io.IOException e) {
        	BaseLoggers.exceptionLogger.error(e.getMessage(),e);
            throw new com.sun.star.io.IOException(e.getMessage(),this);
        }
    }

    public int readSomeBytes(byte[][] buffer, int bufferSize) throws com.sun.star.io.IOException {
        return readBytes(buffer, bufferSize);
    }

    @Override
    public void skipBytes(int skipLength) throws com.sun.star.io.IOException {
        skip(skipLength);
    }

    @Override
    public void closeInput() throws com.sun.star.io.IOException {
        try {
            close();
        }
        catch (java.io.IOException e) {
        	BaseLoggers.exceptionLogger.error(e.getMessage(),e);
            throw new com.sun.star.io.IOException(e.getMessage(), this);
        }
    }


    //
    // Implement XSeekable
    //
    @Override
    public long getLength() throws com.sun.star.io.IOException {
        return count;
    }

    @Override
    public long getPosition() throws com.sun.star.io.IOException {
        return pos;
    }

    @Override
    public void seek(long position) throws com.sun.star.io.IOException {
        pos = (int) position;
    }
}

