package com.nucleus.finnone.pro.queue;

import java.io.Serializable;

public class QueueElement implements Serializable{

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	
	private String messageId;
		
	private Object message;
	
	private Boolean messageGuarantee = Boolean.FALSE;


	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		if(!Serializable.class.isInstance(message)) {
			throw new RuntimeException("Message object must be serializable!!!!");
		}
		this.message = message;
	}

	@Override
	public boolean equals(Object obj) {
		 if (obj == null)
			    return false;

		 if (this.getClass() != obj.getClass())
			    return false;
		 if(obj.getClass().equals(QueueElement.class)) {
			 	QueueElement element = (QueueElement)obj;
				return this.message.equals(element.getMessage());
		 }
		 PriorityQueueElement element = (PriorityQueueElement)obj;
			return getMessage().equals(element.getMessage());
		 
		
	}
	
	@Override
	public int hashCode() {
		return getMessage().hashCode();
	}

	public boolean isMessageGuarantee() {
		return messageGuarantee;
	}

	public void setMessageGuarantee(boolean messageGuarantee) {
		this.messageGuarantee = messageGuarantee;
	}

	

}
