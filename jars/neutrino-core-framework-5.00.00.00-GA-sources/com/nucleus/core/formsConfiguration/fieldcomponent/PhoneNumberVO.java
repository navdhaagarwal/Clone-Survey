package com.nucleus.core.formsConfiguration.fieldcomponent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.nucleus.message.entity.MessageDeliveryStatus;
import org.joda.time.DateTime;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

public class PhoneNumberVO implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes="This field is Id",required=false,dataType="Long",hidden=false)
	private Long              id;

    @ApiModelProperty(notes="This field is Isd Code",required=false,dataType="String",hidden=false)
    private String            isdCode;

    @ApiModelProperty(notes="This field is Std Code",required=false,dataType="String",hidden=false)
    private String            stdCode;

    @ApiModelProperty(notes="This field is Phone Number",required=false,dataType="String",hidden=false)
    private String            phoneNumber;

    @ApiModelProperty(notes="This field is Number Type VO",required=false,dataType="PhoneNumberTypeVO",hidden=false)
    private PhoneNumberTypeVO numberTypeVO;

    @ApiModelProperty(notes="This field is Extension",required=false,dataType="String",hidden=false)
    private String            extension;

    @ApiModelProperty(notes="This field is Country Code",required=false,dataType="String",hidden=false)
    private String            countryCode;

    @ApiModelProperty(notes="This field is Verification Code",required=false,dataType="String",hidden=false)
    private String            verificationCode;

    @ApiModelProperty(notes="This field is Code Expiration Time",required=false,dataType="DateTime",hidden=false)
    private DateTime          codeExpirationTime;

    @ApiModelProperty(notes="This field is Verified",required=false,dataType="Boolean",hidden=false)
    private Boolean           verified;
    
	@ApiModelProperty(notes="This field is Verification Token",required=false,dataType="String",hidden=false)
	private String verificationToken;
	
	private Integer trialCount = 0;
	
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(notes="This field is Ver Code Delivery Status",required=false,dataType="MessageDeliveryStatus",hidden=false)
    private MessageDeliveryStatus verCodeDeliveryStatus;
    
    @ApiModelProperty(notes="This field is Ver Code Del Status Message",required=false,dataType="String",hidden=false)
    private String verCodeDelStatusMessage;
  

    @ApiModelProperty(notes="This field is Is Valid",required=false,dataType="Boolean",hidden=false)
    private Boolean             isValid;

    public String getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(String verificationToken) {
		this.verificationToken = verificationToken;
	}

	public Integer getTrialCount() {
		return trialCount;
	}

	public void setTrialCount(Integer trialCount) {
		this.trialCount = trialCount;
	}

	public MessageDeliveryStatus getVerCodeDeliveryStatus() {
		return verCodeDeliveryStatus;
	}

	public void setVerCodeDeliveryStatus(MessageDeliveryStatus verCodeDeliveryStatus) {
		this.verCodeDeliveryStatus = verCodeDeliveryStatus;
	}

	public String getVerCodeDelStatusMessage() {
		return verCodeDelStatusMessage;
	}

	public void setVerCodeDelStatusMessage(String verCodeDelStatusMessage) {
		this.verCodeDelStatusMessage = verCodeDelStatusMessage;
	}

	public Boolean getValid() {
		return isValid;
	}

	public void setValid(Boolean isValid) {
		this.isValid = isValid;
	}

	/**
     * @return the isdCode
     */
    public String getIsdCode() {
        return isdCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     @ApiModelProperty(notes="This field is Isd Code",required=false,dataType="String",hidden=false)
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
     @ApiModelProperty(notes="This field is Std Code",required=false,dataType="String",hidden=false)
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
     @ApiModelProperty(notes="This field is Phone Number",required=false,dataType="String",hidden=false)
     * @param phoneNumber
     *            the phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public PhoneNumberTypeVO getNumberTypeVO() {
        return numberTypeVO;
    }

    public void setNumberTypeVO(PhoneNumberTypeVO numberTypeVO) {
        this.numberTypeVO = numberTypeVO;
    }

    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     @ApiModelProperty(notes="This field is Extension",required=false,dataType="String",hidden=false)
     * @param extension
     *            the extension to set
     */
    public void setExtension(String extension) {
        this.extension = extension;
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
     @ApiModelProperty(notes="This field is Country Code",required=false,dataType="String",hidden=false)
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

    public DateTime getCodeExpirationTime() {
        return codeExpirationTime;
    }

    public void setCodeExpirationTime(DateTime codeExpirationTime) {
        this.codeExpirationTime = codeExpirationTime;
    }

}
