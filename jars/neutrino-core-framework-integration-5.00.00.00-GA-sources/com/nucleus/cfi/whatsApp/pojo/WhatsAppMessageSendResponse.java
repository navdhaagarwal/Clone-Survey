
package com.nucleus.cfi.whatsApp.pojo;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;


public class WhatsAppMessageSendResponse  implements Serializable{

	public enum WhatsAppDeliveryStatus{
		
		QUEUED,SENDING,SENT,FAILED,DELIVERED,UNDELIVERED,RECEIVING,RECEIVED
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@NonNull
    private  DateTime receiptTimestamp;
	
	@JsonProperty("messageReceiptId")
	private String messageReceiptId;
	
    @NonNull
    private WhatsAppDeliveryStatus whatsAppDeliveryStatus;
    
    @NonNull
    private String messageStatus;

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


	public String getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}

	public WhatsAppDeliveryStatus getWhatsAppDeliveryStatus() {
		return whatsAppDeliveryStatus;
	}

	public void setWhatsAppDeliveryStatus(WhatsAppDeliveryStatus whatsAppDeliveryStatus) {
		this.whatsAppDeliveryStatus = whatsAppDeliveryStatus;
	}

   
}
