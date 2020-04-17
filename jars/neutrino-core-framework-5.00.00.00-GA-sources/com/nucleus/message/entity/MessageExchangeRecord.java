/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.message.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;

/**
 * @author Nucleus Software Exports Limited
 *
 * An abstract class to keep record of all message originating from this system.This class has essential
 * attributes for all type of messages.Each message is linked to some originating entity like any
 * lead,proposal or loan application.
 */
@Entity
@DynamicInsert 
@DynamicUpdate
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "MESSAGE_EXCHANGE_RECORD", indexes = {
		@Index(name = "MESSAGE_EXCHANGE_RECORD_IDX1", columnList = "UNIQUE_REQUEST_ID")
})
public abstract class MessageExchangeRecord extends BaseEntity {
	

    private static final long     serialVersionUID = -2538764717294147421L;

    @Column(length = 1000)
    private String                messageReceiptId;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime              sentTimestamp;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime              deliveryTimestamp;
    private String                destinationSystemId;
    private String                ownerEntityUri;
    private String                deliveryDescription;
    private String                deliveryComment;
    private String 				  extIdentifier;
	@Column(name="UNIQUE_REQUEST_ID")
	private String uniqueRequestId;
        
    private String eventRequestLogId;

    private int retriedAttemptsDone = 0;
    
    private int regenerationAttemptsCount = 0;
    
    private transient String  	  retryAttemptsConfigKey;
    
    @Enumerated(EnumType.STRING)
    private MessageDeliveryStatus deliveryStatus;
    
	public int getRetriedAttemptsDone() {
		return retriedAttemptsDone;
	}

	public void setRetriedAttemptsDone(int retriedAttemptsDone) {
		this.retriedAttemptsDone = retriedAttemptsDone;
	}

	public String getUniqueRequestId() {
		return uniqueRequestId;
	}

	public void setUniqueRequestId(String uniqueRequestId) {
		this.uniqueRequestId = uniqueRequestId;
	}
    
    public String getMessageReceiptId() {
        return messageReceiptId;
    }

    public void setMessageReceiptId(String messageReceiptId) {
        this.messageReceiptId = messageReceiptId;
    }

    public DateTime getSentTimestamp() {
        return sentTimestamp;
    }
    
    public void setSentTimestamp(DateTime sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public DateTime getDeliveryTimestamp() {
        return deliveryTimestamp;
    }

    public void setDeliveryTimestamp(DateTime deliveryTimestamp) {
        this.deliveryTimestamp = deliveryTimestamp;
    }

    public String getDestinationSystemId() {
        return destinationSystemId;
    }

    public void setDestinationSystemId(String destinationSystemId) {
        this.destinationSystemId = destinationSystemId;
    }

    public String getOwnerEntityUri() {
        return ownerEntityUri;
    }

    public void setOwnerEntityUri(String ownerEntityUri) {
        this.ownerEntityUri = ownerEntityUri;
    }

    public String getDeliveryDescription() {
        return deliveryDescription;
    }

    public void setDeliveryDescription(String deliveryDescription) {
        this.deliveryDescription = deliveryDescription;
    }

    public String getDeliveryComment() {
        return deliveryComment;
    }

    public void setDeliveryComment(String deliveryComment) {
        this.deliveryComment = deliveryComment;
    }
    
    public String getExtIdentifier() {
		return extIdentifier;
	}

	public void setExtIdentifier(String extIdentifier) {
		this.extIdentifier = extIdentifier;
	}

    public MessageDeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(MessageDeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

	public String getRetryAttemptsConfigKey() {
		return retryAttemptsConfigKey;
	}

	public void setRetryAttemptsConfigKey(String retryAttemptsConfigKey) {
		this.retryAttemptsConfigKey = retryAttemptsConfigKey;
	}
	
	public int getRegenerationAttemptsCount() {
		return regenerationAttemptsCount;
	}

	public void setRegenerationAttemptsCount(int regenerationAttemptsCount) {
		this.regenerationAttemptsCount = regenerationAttemptsCount;
	}

	public String getEventRequestLogId() {
		return eventRequestLogId;
	}

	public void setEventRequestLogId(String eventRequestLogId) {
		this.eventRequestLogId = eventRequestLogId;
	}	
	
    @PrePersist
    public void preSetSentTimeStamp() {
    	if (ValidatorUtils.isNull(getSentTimestamp())) {
    		this.sentTimestamp = new DateTime();
    	}
    }
}
