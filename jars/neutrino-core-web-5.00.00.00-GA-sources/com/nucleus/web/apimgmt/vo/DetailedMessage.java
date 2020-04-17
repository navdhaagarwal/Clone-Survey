package com.nucleus.web.apimgmt.vo;

import java.io.Serializable;

public class DetailedMessage implements Serializable {

	private static final long serialVersionUID = 979423704208L;
	
	private int code;
	private String errorCode;
	private String message;
	
	public DetailedMessage() {
		//default Constructor.
	}
	
	public DetailedMessage(int code, String message, String errorCode) {
		this.code = code;
		this.message = message;
		this.errorCode = errorCode;
	}

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessgae() {
		return message;
	}
	public void setMessgae(String messgae) {
		this.message = messgae;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
}
