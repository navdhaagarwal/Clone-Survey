package com.nucleus.finnone.pro.cache.exception;

public class CacheFailException extends RuntimeException {
	/**
	public CacheFailException()
	{
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
	
	@Override
	{
		return getClass().getName()+": "+this.getMessage();
	}
	@Override
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}