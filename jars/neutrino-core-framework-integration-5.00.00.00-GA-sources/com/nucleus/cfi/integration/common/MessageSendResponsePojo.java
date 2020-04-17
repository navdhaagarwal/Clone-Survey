package com.nucleus.cfi.integration.common;

import java.io.Serializable;

import org.joda.time.DateTime;

public abstract class MessageSendResponsePojo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3186315210313752362L;

	private DateTime        receiptTimestamp;
	private String          messageReceiptId;
	private String 			deliveryStatus;
	private String 			messageStatus;
	private String 			uniqueId;

	public DateTime getReceiptTimestamp() {
		return receiptTimestamp;
	}
	
	public void setReceiptTimestamp(DateTime receiptTimestamp) {
		this.receiptTimestamp = receiptTimestamp;
	}
	
	public String getMessageReceiptId() {
		return messageReceiptId;
	}
	
	public void setMessageReceiptId(String messageReceiptId) {
		this.messageReceiptId = messageReceiptId;
	}
	
	public String getDeliveryStatus() {
		return deliveryStatus;
	}
	
	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}
	
	public String getMessageStatus() {
		return messageStatus;
	}
	
	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}
	
	public String getUniqueId() {
		return uniqueId;
	}
	
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
}
