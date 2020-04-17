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
package com.nucleus.contact;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.entity.masking.MaskingEntityListener;

@Entity
@DynamicInsert
@DynamicUpdate
@Synonym(grant="ALL")
@Table(indexes={@Index(name="contact_info_fk_index_email",columnList="contact_info_fk"),
        @Index(name="emailAddress_index",columnList="emailAddress")})
public class EMailInfo extends BaseEntity {

    private static final long serialVersionUID = -8294195368601264049L;

    private String            emailAddress;

    private boolean           primaryEmail;

    private Integer           verificationType;

    private boolean           blacklisted;

    @ManyToOne(fetch=FetchType.LAZY)
    private EMailType         emailType;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          verificationTimeStamp;

    private String gcdId;
    
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Integer getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(Integer verificationType) {
        this.verificationType = verificationType;
    }

    public boolean isBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    public EMailType getEmailType() {
        return emailType;
    }

    public void setEmailType(EMailType emailType) {
        this.emailType = emailType;
    }

    public boolean isPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(boolean primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        EMailInfo eMailInfo = (EMailInfo) baseEntity;
        super.populate(eMailInfo, cloneOptions);
        eMailInfo.setBlacklisted(blacklisted);
        eMailInfo.setEmailAddress(emailAddress);
        eMailInfo.setEmailType(emailType);
        eMailInfo.setPrimaryEmail(primaryEmail);
        eMailInfo.setVerificationType(verificationType);
    }

    public DateTime getVerificationTimeStamp() {
        return verificationTimeStamp;
    }

    public void setVerificationTimeStamp(DateTime verificationTimeStamp) {
        this.verificationTimeStamp = verificationTimeStamp;
    }

	public String getGcdId() {
		return gcdId;
	}

	public void setGcdId(String gcdId) {
		this.gcdId = gcdId;
	}
    
}
