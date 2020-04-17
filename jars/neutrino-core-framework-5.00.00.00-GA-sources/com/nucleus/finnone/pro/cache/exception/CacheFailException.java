package com.nucleus.finnone.pro.cache.exception;import com.nucleus.logging.BaseLoggers;

public class CacheFailException extends RuntimeException {
	/**	 * 	 */	private static final long serialVersionUID = -3912627789054450891L;	private String message;
	public CacheFailException()
	{	  BaseLoggers.flowLogger.debug("Default Constructor");
	}
	
	public CacheFailException(String message)
	{
		this.message= message;
	}
	
	public CacheFailException(Throwable throwable)
	{
		super(throwable);
	}
	
	public CacheFailException(Throwable throwable, String message)
	{
		super(throwable);
		this.setMessage(message);
	}
	
	@Override  	public String toString()
	{
		return getClass().getName()+": "+this.getMessage();
	}
	@Override  	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
