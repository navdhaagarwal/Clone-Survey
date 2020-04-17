package com.nucleus.lms.web.common;

import com.nucleus.finnone.pro.base.Message;
import io.swagger.annotations.ApiModelProperty;

public class MessageOutput extends Message {

	public MessageOutput(){
		
	}
	
	public MessageOutput(Message message,String value){
		this.setI18nCode(message.getI18nCode());
		this.setMessageArguments(message.getMessageArguments());
		this.setType(message.getType());
		this.setValue(value);
		this.setIsParent(message.getIsParent());
	}

	@ApiModelProperty(required = true, notes = "It is in format - key:value. Key will be i18Code and value will be the message for success or error scenario with replaced placeholders.", example = "\"\"")
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
