package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.lang.reflect.InvocationTargetException;

import org.springframework.transaction.TransactionSystemException;

import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;

public class CommunicationExceptionLoggerHelper {

	public static void logAndThrowException(Exception exception,String errorMessage)
	{
			
	
	   	 if(exception instanceof TransactionSystemException){
					TransactionSystemException systemException = (TransactionSystemException) exception;
					if(systemException.getApplicationException()!=null && 
							BaseException.class.isAssignableFrom(systemException.getApplicationException().getClass()))
					{						 
						BaseException be=(BaseException) systemException.getApplicationException();
						BaseLoggers.exceptionLogger.error(errorMessage, exception);
						be.setLogged(true);
						throw be;
					}
					else
					{
						throwSystemException(exception,errorMessage);
					}
				}
				else if(exception instanceof InvocationTargetException){
					
					InvocationTargetException targetException = (InvocationTargetException)exception;
					if(targetException.getTargetException()!=null
							&&BaseException.class.isAssignableFrom(targetException.getTargetException().getClass())  )
					{
						
						BaseException be=(BaseException) targetException.getTargetException();
						BaseLoggers.exceptionLogger.error(errorMessage, exception);
						be.setLogged(true);
						throw be;
					}
					else
					{
						throwSystemException(exception,errorMessage);
					}
					
				}
				else{
					throwSystemException(exception,errorMessage);
				}
	          
    
	}
	
	public static void throwSystemException(Exception exception,String errorMessage)
	{
		
		 BaseLoggers.exceptionLogger.error(errorMessage, exception);
		 
           throw ExceptionBuilder
			.getInstance(SystemException.class)
			.setMessage(errorMessage)
			.setOriginalException(exception)
			.setSeverity(
					ExceptionSeverityEnum.SEVERITY_MEDIUM
							.getEnumValue()).build();
	}
}
