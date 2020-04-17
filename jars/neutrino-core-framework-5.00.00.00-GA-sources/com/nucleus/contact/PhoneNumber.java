package com.nucleus.contact;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.message.entity.MessageDeliveryStatus;

/**
 *
 * @author Nucleus Software Exports Limited
 * Phone Number class
 *      Store std code, isd code, country code
 *      number type - mobile, landline
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
//@Indexed
@Synonym(grant="ALL")
@Table(indexes={
		@Index(name = "address_fk_index", columnList = "address_fk"),
		@Index(name = "address_fk_tmp_index", columnList = "address_tmp_fk"),
		@Index(name = "contact_info_fk_index_phone", columnList = "contact_info_fk"),
		@Index(name = "PHONE_NUMBER_IDX1", columnList = "isdCode"),
		@Index(name = "PN_PHONE_NUMBER_INDX", columnList = "phoneNumber")})
public class PhoneNumber extends BaseEntity {

    public static final String STRIP_CHARS_REGEX = "[ -]";

    @Transient
    private static final long   serialVersionUID  = 1077720446849440270L;

    private String              isdCode;

    private String              stdCode;

    @EmbedInAuditAsValue
    private String              phoneNumber;

    @ManyToOne(fetch=FetchType.LAZY)
    private PhoneNumberType     numberType;

    private String              extension;

    private String              countryCode;

    private String              verificationCode;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime            codeExpirationTime;

    private Boolean             verified;

    private String recordId;
    
	@ManyToOne(fetch=FetchType.LAZY)
    private PhoneConnectionType phoneConnectionType;

	private String verificationToken;
	
	private Integer trialCount = 0;
	
	private String gcdId;
	
    @Enumerated(EnumType.STRING)
    private MessageDeliveryStatus verCodeDeliveryStatus;
    
    private String verCodeDelStatusMessage;
  

    private Boolean             isValid;

	public String getVerCodeDelStatusMessage() {
		return verCodeDelStatusMessage;
	}

	public void setVerCodeDelStatusMessage(String verCodeDelStatusMessage) {
		this.verCodeDelStatusMessage = verCodeDelStatusMessage;
	}

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

	public MessageDeliveryStatus getVerCodeDeliveryStatus() {
		return verCodeDeliveryStatus;
	}

	public void setVerCodeDeliveryStatus(MessageDeliveryStatus verCodeDeliveryStatus) {
		this.verCodeDeliveryStatus = verCodeDeliveryStatus;
	}

	public Integer getTrialCount() {
		return trialCount;
	}

	public void setTrialCount(Integer trialCount) {
		this.trialCount = trialCount;
	}

	public String getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(String verificationToken) {
		this.verificationToken = verificationToken;
	}
	
    public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
    
    /**
     * @return the isdCode
     */
    public String getIsdCode() {
        return isdCode;
    }

    /**
     * @param isdCode
     *            the isdCode to set
     */
    public void setIsdCode(String isdCode) {
        this.isdCode = isdCode;
    }

    /**
     * @return the stdCode
     */
    public String getStdCode() {
        return stdCode;
    }

    /**
     * @param stdCode
     *            the stdCode to set
     */
    public void setStdCode(String stdCode) {
        this.stdCode = stdCode;
    }

    /**
     * @return the phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber
     *            the phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber.replaceAll(STRIP_CHARS_REGEX, "");
        } else {
            this.phoneNumber = phoneNumber;
        }
    }

    /**
     * @return the numberType
     */
    public PhoneNumberType getNumberType() {
        return numberType;
    }

    /**
     * @param numberType
     *            the numberType to set
     */
    public void setNumberType(PhoneNumberType numberType) {
        this.numberType = numberType;
    }

    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @param extension
     *            the extension to set
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        if (numberType != null && (numberType.getCode().equalsIgnoreCase(PhoneNumberType.LANDLINE_NUMBER) || numberType.getCode().equalsIgnoreCase(PhoneNumberType.PRIMARY_PHONE_NUMBER))) {
            if (extension == null){
            	if(stdCode != null && phoneNumber != null){
            		return isdCode + " - " + stdCode + " - " + phoneNumber;
            	}else if(stdCode == null && phoneNumber != null){
            		return isdCode + " - " + phoneNumber;
            	}
            }else{
            	if(stdCode != null && phoneNumber != null){
            		return isdCode + " - " + stdCode + " - " + phoneNumber + " - " + extension;
            	}else if(stdCode == null && phoneNumber != null){
            		return isdCode + " - " + phoneNumber;
            	}
            }
        } else if (numberType != null && (numberType.getCode().equalsIgnoreCase(PhoneNumberType.MOBILE_NUMBER)||numberType.getCode().equalsIgnoreCase(PhoneNumberType.PRIMARY_MOBILE_NUMBER) )) {
        	if(phoneNumber != null){
        		return isdCode + " - " + phoneNumber;
        	}
        }
        return null;
    }

    /**
     *
     * @return country code
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * set the value of country code
     *
     * @param countryCode
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        PhoneNumber clonePhoneNumber = (PhoneNumber) baseEntity;
        super.populate(clonePhoneNumber, cloneOptions);
        clonePhoneNumber.setExtension(extension);
        clonePhoneNumber.setIsdCode(isdCode);
        clonePhoneNumber.setNumberType(numberType);
        clonePhoneNumber.setPhoneNumber(phoneNumber);
        clonePhoneNumber.setStdCode(stdCode);
        clonePhoneNumber.setCountryCode(countryCode);
        clonePhoneNumber.setVerificationCode(verificationCode);
        clonePhoneNumber.setCodeExpirationTime(codeExpirationTime);
        clonePhoneNumber.setVerified(verified);
        clonePhoneNumber.setPhoneConnectionType(phoneConnectionType);
        clonePhoneNumber.setVerCodeDeliveryStatus(verCodeDeliveryStatus);
        clonePhoneNumber.setValid(isValid);
        clonePhoneNumber.setVerCodeDelStatusMessage(verCodeDelStatusMessage);
    }

    public DateTime getCodeExpirationTime() {
        return codeExpirationTime;
    }

    public void setCodeExpirationTime(DateTime codeExpirationTime) {
        this.codeExpirationTime = codeExpirationTime;
    }

    public PhoneConnectionType getPhoneConnectionType() {
        return phoneConnectionType;
    }

    public void setPhoneConnectionType(PhoneConnectionType phoneConnectionType) {
        this.phoneConnectionType = phoneConnectionType;
    }

	public String getGcdId() {
		return gcdId;
	}

	public void setGcdId(String gcdId) {
		this.gcdId = gcdId;
	}
    
}