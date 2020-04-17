package com.nucleus.message.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
public class ShortMessageRecordHistory extends  MessageExchangeRecordHistory {

	private static final long serialVersionUID = 1764237892634826348L;

	private String            smsFrom;
	
	private String            smsTo;
	
    @Column(name="SMS_BODY", length=1000)
	private String            smsBody;
    
	private String 			  statusMessage;

	public String getSmsFrom() {
		return smsFrom;
	}

	public void setSmsFrom(String smsFrom) {
		this.smsFrom = smsFrom;
	}

	public String getSmsTo() {
		return smsTo;
	}

	public void setSmsTo(String smsTo) {
		this.smsTo = smsTo;
	}

	public String getSmsBody() {
		return smsBody;
	}

	public void setSmsBody(String smsBody) {
		this.smsBody = smsBody;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	
	@Override
	protected void populate(BaseEntity clonedEntity, CloneOptions cloneOptions) {
		ShortMessageRecordHistory smsRecordHistory = (ShortMessageRecordHistory) clonedEntity;
		super.populate(smsRecordHistory, cloneOptions);
		smsRecordHistory.setSmsFrom(this.smsFrom);
		smsRecordHistory.setSmsTo(this.smsTo);
		smsRecordHistory.setStatusMessage(this.statusMessage);
		smsRecordHistory.setSmsBody(this.smsBody);
	}
	
	@Override
	protected void populateFrom(BaseEntity copyEntity, CloneOptions cloneOptions) {
		ShortMessageRecordHistory smsRecordHistory = (ShortMessageRecordHistory) copyEntity;
		super.populateFrom(smsRecordHistory, cloneOptions);
		this.setSmsFrom(smsRecordHistory.getSmsFrom());
		this.setSmsTo(smsRecordHistory.getSmsTo());
		this.setStatusMessage(smsRecordHistory.getStatusMessage());
		this.setSmsBody(smsRecordHistory.getSmsBody());
	}

}
