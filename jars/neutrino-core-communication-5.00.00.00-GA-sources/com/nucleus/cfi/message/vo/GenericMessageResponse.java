package com.nucleus.cfi.message.vo;

import java.io.Serializable;


public class GenericMessageResponse implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2250241920179879045L;


	private String correlationId;

	private MessageChannels messageChannel;

	private String messageOriginatorId;

	private String uniqueRequestId;

	private String errorMessage;

	private boolean success;

	private String deliveryStatus;

	private int failedCallbackAttempts = 0;

	private long receiptTimestamp;

	private String customCallbackUrl;

	public String getMessageOriginatorId() {
		return messageOriginatorId;
	}

	public void setMessageOriginatorId(String messageOriginatorId) {
		this.messageOriginatorId = messageOriginatorId;
	}

	public String getUniqueRequestId() {
		return uniqueRequestId;
	}

	public void setUniqueRequestId(String uniqueRequestId) {
		this.uniqueRequestId = uniqueRequestId;
	}


	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public int getFailedCallbackAttempts() {
		return failedCallbackAttempts;
	}

	public void setFailedCallbackAttempts(int failedCallbackAttempts) {
		this.failedCallbackAttempts = failedCallbackAttempts;
	}

	public int incrementAndGetFailedAttempts() {
		return ++failedCallbackAttempts;
	}

	public long getReceiptTimestamp() {
		return receiptTimestamp;
	}

	public void setReceiptTimestamp(long receiptTimestamp) {
		this.receiptTimestamp = receiptTimestamp;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public String getCustomCallbackUrl() {
		return customCallbackUrl;
	}

	public void setCustomCallbackUrl(String customCallbackUrl) {
		this.customCallbackUrl = customCallbackUrl;
	}

	public MessageChannels getMessageChannel() {
		return messageChannel;
	}

	public void setMessageChannel(MessageChannels messageChannel) {
		this.messageChannel = messageChannel;
	}


}
