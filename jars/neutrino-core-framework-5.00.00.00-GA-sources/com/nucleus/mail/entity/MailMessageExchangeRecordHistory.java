package com.nucleus.mail.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.message.entity.MessageExchangeRecordHistory;

@Entity
@DynamicUpdate
@DynamicInsert
@DiscriminatorValue(value="MailExchangeRecordHst")
public class MailMessageExchangeRecordHistory extends MessageExchangeRecordHistory {

	private static final long serialVersionUID = -4993061440485894520L;
	
	@Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime readTimestamp;
	
	private String fromEmailAddress;
	
	private String subject;
	
	@Column(name = "HTML_BODY", length = 4000)
	private String htmlBody;
	
	/**
	 * A semicolon separated values of TO email ids.
	 */
	@Column(name = "TO_ADDRESS_LIST", length = 2000)
	private String toAddressList;
	
	/**
	 * A semicolon separated values of CC email ids.
	 */
	@Column(name = "CC_ADDRESS_LIST", length = 2000)
	private String ccAddressList;
	
	/**
	 * A semicolon separated values of BCC email ids.
	 */
	@Column(name = "BCC_ADDRESS_LIST", length = 2000)
	private String bccAddressList;
	
	/**
	 * A semicolon separated values of storage ids.
	 */
	@Column(name = "ATTACHMENT_STORAGE_IDS", length = 1000)
	private String attachmentStorageIds;
	
	public DateTime getReadTimestamp() {
		return readTimestamp;
	}

	public void setReadTimestamp(DateTime readTimestamp) {
		this.readTimestamp = readTimestamp;
	}

	public String getFromEmailAddress() {
		return fromEmailAddress;
	}

	public void setFromEmailAddress(String fromEmailAddress) {
		this.fromEmailAddress = fromEmailAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getHtmlBody() {
		return htmlBody;
	}

	public void setHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public String getToAddressList() {
		return toAddressList;
	}

	public void setToAddressList(String toAddressList) {
		this.toAddressList = toAddressList;
	}

	public String getCcAddressList() {
		return ccAddressList;
	}

	public void setCcAddressList(String ccAddressList) {
		this.ccAddressList = ccAddressList;
	}

	public String getBccAddressList() {
		return bccAddressList;
	}

	public void setBccAddressList(String bccAddressList) {
		this.bccAddressList = bccAddressList;
	}

	public String getAttachmentStorageIds() {
		return attachmentStorageIds;
	}

	public void setAttachmentStorageIds(String attachmentStorageIds) {
		this.attachmentStorageIds = attachmentStorageIds;
	}
	
	@Override
	protected void populate(BaseEntity clonedEntity, CloneOptions cloneOptions) {
		MailMessageExchangeRecordHistory mailRecordHistory = (MailMessageExchangeRecordHistory) clonedEntity;
		super.populate(mailRecordHistory, cloneOptions);
		mailRecordHistory.setReadTimestamp(this.readTimestamp);
		mailRecordHistory.setFromEmailAddress(this.fromEmailAddress);
		mailRecordHistory.setSubject(this.subject);
		mailRecordHistory.setHtmlBody(this.htmlBody);
		mailRecordHistory.setToAddressList(this.toAddressList);
		mailRecordHistory.setCcAddressList(this.ccAddressList);
		mailRecordHistory.setBccAddressList(this.bccAddressList);
		mailRecordHistory.setAttachmentStorageIds(this.attachmentStorageIds);
	}
	
	@Override
	protected void populateFrom(BaseEntity copyEntity, CloneOptions cloneOptions) {
		MailMessageExchangeRecordHistory messageRecordHistory = (MailMessageExchangeRecordHistory) copyEntity;
		super.populateFrom(messageRecordHistory, cloneOptions);
		this.setReadTimestamp(messageRecordHistory.getReadTimestamp());
		this.setFromEmailAddress(messageRecordHistory.getFromEmailAddress());
		this.setSubject(messageRecordHistory.getSubject());
		this.setHtmlBody(messageRecordHistory.getHtmlBody());
		this.setToAddressList(messageRecordHistory.getToAddressList());
		this.setCcAddressList(messageRecordHistory.getCcAddressList());
		this.setBccAddressList(messageRecordHistory.getBccAddressList());
		this.setAttachmentStorageIds(messageRecordHistory.getAttachmentStorageIds());
	}
	
}
