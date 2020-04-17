package com.nucleus.api.documentation.vo;

import java.io.Serializable;

public class ApiMessageCodeVO implements Serializable {

	private static final long serialVersionUID = 5437549871905723708L;

	private String messageCode;

	private String messageDescription;

	private String apiCode;

	public String getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}

	public String getMessageDescription() {
		return messageDescription;
	}

	public void setMessageDescription(String messageDescription) {
		this.messageDescription = messageDescription;
	}

	public String getApiCode() {
		return apiCode;
	}

	public void setApiCode(String apiCode) {
		this.apiCode = apiCode;
	}

}
