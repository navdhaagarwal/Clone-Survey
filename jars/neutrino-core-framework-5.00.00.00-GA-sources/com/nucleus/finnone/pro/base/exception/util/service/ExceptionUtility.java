package com.nucleus.finnone.pro.base.exception.util.service;
 
import static com.nucleus.finnone.pro.base.constants.CoreConstant.ORACLE_ERROR_TAG;
import static com.nucleus.finnone.pro.base.constants.CoreConstant.CAUSED_BY_TAG;
import static com.nucleus.finnone.pro.base.constants.CoreConstant.CARRIAGE_RETURN_CHAR;
import static com.nucleus.finnone.pro.base.constants.CoreConstant.COMMA_CHAR;
import static com.nucleus.finnone.pro.base.constants.CoreConstant.SPACE_CHAR;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.inject.Named;

import com.nucleus.finnone.pro.base.exception.util.serviceinterface.IExceptionUtility;
  
@Named("exceptionUtility") 
public class ExceptionUtility implements IExceptionUtility {

	public static final int FOUR_THOUSAND=4000;
	public static final int ZERO=0;
	@Deprecated
	public String getCauseOfORAErrorIfExists(Exception exception){
		int indexOf = 0;
		String stackTrace ="" ; 
		try{
			try{
			stackTrace=exception.getCause().getMessage();
			}catch(Exception ex){
				stackTrace=exception.toString();
			}
			StringWriter errors = new StringWriter();
			exception.getCause().printStackTrace(new PrintWriter(errors));
			String stackTraceMain = errors.toString();
			if(!stackTraceMain.contains(ORACLE_ERROR_TAG))
				return stackTrace;
			
			for(;;){
				indexOf = stackTraceMain.indexOf(CAUSED_BY_TAG,indexOf+1);
				if(indexOf!=-1){
					stackTrace += COMMA_CHAR+SPACE_CHAR+stackTraceMain.substring(indexOf,stackTraceMain.indexOf(CARRIAGE_RETURN_CHAR,indexOf));
				}else{
					break;
				}
					
			}
		}catch(Exception ex){
			//DO Nothing
		}
		return stackTrace;
	}
	
	public String getCauseFromStackHierarchy(Exception exception){
		int depthOfExceptionStack = 0;
		StringBuilder stackTraceBuilder =new StringBuilder(exception.toString()) ; 
		try{
				Exception exceptionRef = exception;
				while(exceptionRef.getCause()!=null){
					exceptionRef = (Exception) exceptionRef.getCause();
					stackTraceBuilder.append(COMMA_CHAR).append(SPACE_CHAR).append(exceptionRef.getMessage());
					depthOfExceptionStack++;	
				}
				stackTraceBuilder.append(COMMA_CHAR).append(SPACE_CHAR).append("depthOfExceptionStack: [").append(depthOfExceptionStack).append("]");
		}catch(Exception ex){
			//DO Nothing
		}
		return stackTraceBuilder.toString();
	}		
	
}
