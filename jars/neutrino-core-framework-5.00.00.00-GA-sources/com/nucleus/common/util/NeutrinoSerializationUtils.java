package com.nucleus.common.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;

import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.logging.BaseLoggers;

public class NeutrinoSerializationUtils {

	 /* Object deserialization with class loader is not available into spring-security-oauth2(version 2.0.7.RELEASE)
        but available in its later version (version 2.0.10.RELEASE) .So you can use this method to deserialize with
	    class loader till version upgrade. */
	 
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] objectByte){		
		ObjectInputStream objectInputStream = null;
		try {				
			objectInputStream = new ClassLoaderObjectInputStream(Thread.currentThread().getContextClassLoader(),
		            new ByteArrayInputStream(objectByte));
			
			return (T) objectInputStream.readObject();				
		}catch (IOException | ClassNotFoundException exception) {
			BaseLoggers.exceptionLogger.error(exception.getMessage());
			throw new SystemException(exception);
		}finally {
			if (objectInputStream != null) {
				try {
					objectInputStream.close();
				} catch (IOException e) {
					//Do nothing
				}
			}
		}
	}
	
}
