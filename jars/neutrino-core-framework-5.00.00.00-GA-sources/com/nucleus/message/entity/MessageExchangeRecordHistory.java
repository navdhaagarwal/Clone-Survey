package com.nucleus.message.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicInsert 
@DynamicUpdate
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({ @NamedQuery(name = "getMessageExchangeRecordHistoryByUniqueId", query = "select  mer from  MessageExchangeRecordHistory mer where mer.uniqueRequestId = :uniqueRequestId")})
@Table(name="MESSAGE_EXCHANG_RECORD_HISTORY", indexes = {
		@Index(name = "MER_HST_IDX1", columnList = "UNIQUE_REQUEST_ID")
})
public class MessageExchangeRecordHistory extends BaseEntity{

	 public static final Character DELIVERED = Character.valueOf('D');
	    public static final Character NOT_DELIVERED = Character.valueOf('N');
	    public static final Character FAILED = Character.valueOf('F');
	    public static final Character WAITING_FOR_RESPONSE = Character.valueOf('W');

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
	    @Column(name="UNIQUE_REQUEST_ID")
	    private String 				  uniqueRequestId;
	    private String 				  eventRequestLogId;
	    private int 			 	  retriedAttemptsDone = 0;
	    private String 				  parentUniqueRequestId;	    
	    private int 				  regenerationAttemptsCount = 0;
	    
	    /*@PrePersist
		protected void prePersistCallback() {
			if (getEntityLifeCycleData().getUuid() == null) {
				getEntityLifeCycleData().setUuid(UUID.randomUUID().toString());
			}
		}*/

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

	    public MessageDeliveryStatus getDeliveryStatus() {
	        return deliveryStatus;
	    }

	    public void setDeliveryStatus(MessageDeliveryStatus deliveryStatus) {
	        this.deliveryStatus = deliveryStatus;
	    }

		public String getEventRequestLogId() {
			return eventRequestLogId;
		}

		public void setEventRequestLogId(String eventRequestLogId) {
			this.eventRequestLogId = eventRequestLogId;
		}

		public String getParentUniqueRequestId() {
			return parentUniqueRequestId;
		}

		public void setParentUniqueRequestId(String parentUniqueRequestId) {
			this.parentUniqueRequestId = parentUniqueRequestId;
		}

		public int getRegenerationAttemptsCount() {
			return regenerationAttemptsCount;
		}

		public void setRegenerationAttemptsCount(int regenerationAttemptsCount) {
			this.regenerationAttemptsCount = regenerationAttemptsCount;
		}
		
		@Override
		protected void populate(BaseEntity clonedEntity, CloneOptions cloneOptions) {
			MessageExchangeRecordHistory messageRecordHistory = (MessageExchangeRecordHistory) clonedEntity;
			super.populate(messageRecordHistory, cloneOptions);
			messageRecordHistory.setMessageReceiptId(this.messageReceiptId);
			//messageRecordHistory.setSentTimestamp(this.sentTimestamp);
			//Cloning is used in regenerating messageExchangeRecordHistory. So sent time stamp should not be same. 
			messageRecordHistory.setSentTimestamp(new DateTime());
			messageRecordHistory.setDeliveryTimestamp(this.deliveryTimestamp);
			messageRecordHistory.setDestinationSystemId(this.destinationSystemId);
			messageRecordHistory.setOwnerEntityUri(this.ownerEntityUri);
			messageRecordHistory.setDeliveryDescription(this.deliveryDescription);
			messageRecordHistory.setDeliveryComment(this.deliveryComment);
			messageRecordHistory.setUniqueRequestId(this.uniqueRequestId);
			messageRecordHistory.setEventRequestLogId(this.eventRequestLogId);
			messageRecordHistory.setRetriedAttemptsDone(this.retriedAttemptsDone);
			messageRecordHistory.setParentUniqueRequestId(this.parentUniqueRequestId);
			messageRecordHistory.setRegenerationAttemptsCount(this.regenerationAttemptsCount);
		}
		
		@Override
		protected void populateFrom(BaseEntity copyEntity, CloneOptions cloneOptions) {
			MessageExchangeRecordHistory messageRecordHistory = (MessageExchangeRecordHistory) copyEntity;
			super.populateFrom(messageRecordHistory, cloneOptions);
	        this.setMessageReceiptId(messageRecordHistory.getMessageReceiptId());
			//this.setSentTimestamp(messageRecordHistory.getSentTimestamp());
			//Cloning is used in regenerating messageExchangeRecordHistory. So sent time stamp should not be same. 
	        this.setSentTimestamp(new DateTime());
			this.setDeliveryTimestamp(messageRecordHistory.getDeliveryTimestamp());
			this.setDestinationSystemId(messageRecordHistory.getDestinationSystemId());
			this.setOwnerEntityUri(messageRecordHistory.getOwnerEntityUri());
			this.setDeliveryDescription(messageRecordHistory.getDeliveryDescription());
			this.setDeliveryComment(messageRecordHistory.getDeliveryComment());
			this.setUniqueRequestId(messageRecordHistory.getUniqueRequestId());
			this.setEventRequestLogId(messageRecordHistory.getEventRequestLogId());
			this.setRetriedAttemptsDone(messageRecordHistory.getRetriedAttemptsDone());
			this.setParentUniqueRequestId(messageRecordHistory.getParentUniqueRequestId());
			this.setRegenerationAttemptsCount(messageRecordHistory.getRegenerationAttemptsCount());
		}
		
}
