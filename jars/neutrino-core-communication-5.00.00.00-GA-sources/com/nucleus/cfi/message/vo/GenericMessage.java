package com.nucleus.cfi.message.vo;

import java.io.Serializable;
import java.util.Map;

public class GenericMessage<T> implements Serializable,Cloneable{

	/**
	 *
	 */
	private static final long serialVersionUID = -8944124058972146507L;

	private MessageChannels messageChannel;

	private String correlationId;

	private String messageOriginatorId;

	private T body;

	private String uniqueRequestId;

	private boolean callbackEnabled;

	private String customCallbackUrl;

	private Map.Entry<String,byte[]> attachments;

	public String getMessageOriginatorId() {
		return messageOriginatorId;
	}

	public void setMessageOriginatorId(String messageOriginatorId) {
		this.messageOriginatorId = messageOriginatorId;
	}

	public T getBody() {
		return body;
	}

	public void setBody(T body) {
		this.body = body;
	}

	public String getUniqueRequestId() {
		return uniqueRequestId;
	}

	public void setUniqueRequestId(String uniqueRequestId) {
		this.uniqueRequestId = uniqueRequestId;
	}

	public boolean isCallbackEnabled() {
		return callbackEnabled;
	}

	public void setCallbackEnabled(boolean callbackEnabled) {
		this.callbackEnabled = callbackEnabled;
	}


	public MessageChannels getMessageChannel() {
		return messageChannel;
	}

	public void setMessageChannel(MessageChannels messageChannel) {
		this.messageChannel = messageChannel;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public Map.Entry<String,byte[]> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map.Entry<String,byte[]> attachments) {
		this.attachments = attachments;
	}

	@SuppressWarnings("unchecked")
	@Override
	public GenericMessage<T> clone() throws CloneNotSupportedException{
		return (GenericMessage<T>) super.clone();

	}

	public String getCustomCallbackUrl() {
		return customCallbackUrl;
	}

	public void setCustomCallbackUrl(String customCallbackUrl) {
		this.customCallbackUrl = customCallbackUrl;
	}

}
