/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.notification;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * @author Nucleus Software Exports Limited
 * TODO -> amit.parashar Add documentation to class
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(indexes = {@Index(name="fromUserUri_index",columnList="fromUserUri")})
public class CommonMailContent extends BaseEntity {

    private static final long serialVersionUID = -177180424343601445L;

    private String            fromUserUri;
    private String            ccList;
    private String            subject;
    @Lob
    private String            body;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          msgSentTimeStamp;
    
    private Boolean favourite;

    public String getFromUserUri() {
        return fromUserUri;
    }

    public void setFromUserUri(String fromUserUri) {
        this.fromUserUri = fromUserUri;
    }

    public String getCcList() {
        return ccList;
    }

    public void setCcList(String ccList) {
        this.ccList = ccList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public DateTime getMsgSentTimeStamp() {
        return msgSentTimeStamp;
    }

    public void setMsgSentTimeStamp(DateTime msgSentTimeStamp) {
        this.msgSentTimeStamp = msgSentTimeStamp;
    }

    @Override
	public void loadLazyFields() {
		super.loadLazyFields();		
	}

	public Boolean getFavourite() {
		return favourite;
	}

	public void setFavourite(Boolean favourite) {
		this.favourite = favourite;
	}
    
}
