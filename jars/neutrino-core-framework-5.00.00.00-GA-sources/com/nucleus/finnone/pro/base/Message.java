package com.nucleus.finnone.pro.base;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

public class Message  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Message Types
	 * @author Dhananjay.Jha
	 *
	 */
	public static enum MessageType{
		INFO, WARNING, ERROR;
	}
	/**
	 * Default constructor
	 */
	public Message(){
		
	}
	/**
	 * 
	 * @param i18Code
	 * @param messageType
	 * @param messageArguments
	 */
	public Message(String i18nCode, MessageType messageType, String... messageArguments){		
		this.i18nCode=i18nCode;
		this.type=messageType;
		this.messageArguments=messageArguments;
	}
	
	public Message(String i18nCode, MessageType messageType,Boolean isParent, String... messageArguments){		
		this.i18nCode=i18nCode;
		this.type=messageType;
		this.messageArguments=messageArguments;
		this.isParent =isParent;
	}

	@ApiModelProperty(notes = "It is a key for the message under value tag.", required = true, example = "\"\"")
	private String i18nCode;
	@ApiModelProperty (notes = "It is an array of values used to replace place holder(s) in message coming under value tag.", example = "\"\"")
	private String[] messageArguments;
	@ApiModelProperty (notes = "It returns success or error as per completion of request or failure of any business validations.", allowableValues = "INFO,ERROR,WARNING", example = "\"\"")
	private MessageType type;
	@ApiModelProperty (notes="In case of list of messages, it is possible that there exists parent Message and remaining messages are sub list of the parent message. This flag  is used to determine whether a particular message item is a parent message item or not. This flag makes the message more readable or understandable.")
	private Boolean isParent =Boolean.FALSE;
	
	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}
	public String getI18nCode() {
		return i18nCode;
	}
	public void setI18nCode(String i18nCode) {
		this.i18nCode = i18nCode;
	}
	public String[] getMessageArguments() {
		return messageArguments;
	}
	public void setMessageArguments(String... messageArguments) {
		this.messageArguments = messageArguments;
	}
	
	public Boolean getIsParent() {
		return isParent;
	}
	public void setIsParent(Boolean isParent) {
		this.isParent = isParent;
	}
	public String getMessageArgumentsString(String... messageArguments) {
		StringBuilder messageStr = new StringBuilder();
		if(messageArguments != null && messageArguments.length > 0){
			messageStr.append(messageArguments[0]);
			for(int i = 1; i < messageArguments.length; i++){
				messageStr.append(",").append(messageArguments[i]);
			}
		}
		return messageStr.toString();
	}
}
