/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.email;

import java.io.Serializable;
import java.util.List;

import org.joda.time.DateTime;

/**
 * @author Nucleus Software Exports Limited
 * TODO -> amit.parashar Add documentation to class
 */
public class UserMailNotificationVO implements Serializable {

    private static final long serialVersionUID = -6463343609680335969L;

    private String            fromUser;
    private String 			  fromUserId;
    private List<String>      toList;
    private List<String>      toListId;
	private String            ccList;
    // for Future use
    private String            subject;
    private String            body;
    private String            mailNotificationPriority;
    private String            msgSentTimeStamp;
    private String            msgStatus;
    private Long              userId;
    private Long              notificationID;
    private DateTime          msgTimeStamp;
    private Long              nextMailUserId;
    private Long              previousMailUserId;
    private boolean           readStatus;
    private String 			  toUser;
    private String			  error;
    private Boolean			  favourite;

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    /**
     * @return the ccList
     */
    public String getCcList() {
        return ccList;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param ccList the ccList to set
     */
    public void setCcList(String ccList) {
        this.ccList = ccList;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return the fromUser
     */
    public String getFromUser() {
        return fromUser;
    }

    /**
     * @param fromUser the fromUser to set
     */
    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    /**
     * @return the mailNotificationPriority
     */
    public String getMailNotificationPriority() {
        return mailNotificationPriority;
    }

    /**
     * @return the msgSentTimeStamp
     */
    public String getMsgSentTimeStamp() {
        return msgSentTimeStamp;
    }

    /**
     * @return the msgStatus
     */
    public String getMsgStatus() {
        return msgStatus;
    }

    /**
     * @param mailNotificationPriority the mailNotificationPriority to set
     */
    public void setMailNotificationPriority(String mailNotificationPriority) {
        this.mailNotificationPriority = mailNotificationPriority;
    }

    /**
     * @param msgSentTimeStamp the msgSentTimeStamp to set
     */
    public void setMsgSentTimeStamp(String msgSentTimeStamp) {
        this.msgSentTimeStamp = msgSentTimeStamp;
    }

    /**
     * @param msgStatus the msgStatus to set
     */
    public void setMsgStatus(String msgStatus) {
        this.msgStatus = msgStatus;
    }

    /**
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<String> getToList() {
        return toList;
    }

    public void setToList(List<String> toList) {
        this.toList = toList;
    }

    public Long getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(Long notificationID) {
        this.notificationID = notificationID;
    }

    public DateTime getMsgTimeStamp() {
        return msgTimeStamp;
    }

    public void setMsgTimeStamp(DateTime msgTimeStamp) {
        this.msgTimeStamp = msgTimeStamp;
    }

    public Long getNextMailUserId() {
        return nextMailUserId;
    }

    public void setNextMailUserId(Long nextMailUserId) {
        this.nextMailUserId = nextMailUserId;
    }

    public Long getPreviousMailUserId() {
        return previousMailUserId;
    }

    public void setPreviousMailUserId(Long previousMailUserId) {
        this.previousMailUserId = previousMailUserId;
    }
    
    /**
	 * @return the fromUserId
	 */
	public String getFromUserId() {
		return fromUserId;
	}

	/**
	 * @param fromUserId the fromUserId to set
	 */
	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	/**
	 * @return the toListId
	 */
	public List<String> getToListId() {
		return toListId;
	}

	/**
	 * @param toListId the toListId to set
	 */
	public void setToListId(List<String> toListId) {
		this.toListId = toListId;
	}

	public String getToUser() {
		return toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Boolean getFavourite() {
		return favourite;
	}

	public void setFavourite(Boolean favourite) {
		this.favourite = favourite!=null?favourite:false;
	}
	
	
}