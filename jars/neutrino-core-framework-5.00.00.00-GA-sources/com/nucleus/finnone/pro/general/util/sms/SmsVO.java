/**
 * 
 */
package com.nucleus.finnone.pro.general.util.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * @author dhananjay.jha
 *
 */
public class SmsVO {
	/**
	 * List of phone numbers on which message needs to be sent
	 */
	private List<PhoneNumber> telephoneNumbers=null;
	/**
	 * Message text to be sent
	 */
	private String message;
	
	/**
	 * Template mapping object
	 */
	private Map<String, Object> templateMappingObject;
	
	/**
	 * Template file path
	 */
	private String templateFilePath;
	
	/**
	 * Added specific to DCB bank
	 */
	private Long referencedBranchCode;
	private String referencedAccountNumber;
	
	public List<PhoneNumber> getTelephoneNumbers() {
		return telephoneNumbers;
	}
	public void setTelephoneNumbers(List<PhoneNumber> telephoneNumbers) {
		this.telephoneNumbers = telephoneNumbers;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void addTelephoneNumber(PhoneNumber telephoneNumber) {
		if (telephoneNumbers == null) {
			telephoneNumbers = new ArrayList<PhoneNumber>();
		}
		telephoneNumbers.add(telephoneNumber);
	}

	public void addTelephoneNumber(int countryCode, String localCode, long phoneNumber) {		
		PhoneNumber telephoneNumber = new PhoneNumber();
		telephoneNumber.setCountryCode(countryCode);
		if(localCode!=null){
			telephoneNumber.setPreferredDomesticCarrierCode(localCode);
		}		
		telephoneNumber.setNationalNumber(phoneNumber);
		addTelephoneNumber(telephoneNumber);
	}
	
	public void addTelephoneNumber(long phoneNumber) {		
		PhoneNumber telephoneNumber = new PhoneNumber();	
		telephoneNumber.setNationalNumber(phoneNumber);
		addTelephoneNumber(telephoneNumber);
	}
	
	public Map<String, Object> getTemplateMappingObject() {
		return templateMappingObject;
	}
	public void setTemplateMappingObject(Map<String, Object> templateMappingObject) {
		this.templateMappingObject = templateMappingObject;
	}
	public String getTemplateFilePath() {
		return templateFilePath;
	}
	public void setTemplateFilePath(String templateFilePath) {
		this.templateFilePath = templateFilePath;
	}


	
	/**
	 * @return the referencedBranchCode
	 */
	public Long getReferencedBranchCode() {
		return referencedBranchCode;
	}
	/**
	 * @param referencedBranchCode the referencedBranchCode to set
	 */
	public void setReferencedBranchCode(Long referencedBranchCode) {
		this.referencedBranchCode = referencedBranchCode;
	}
	/**
	 * @return the referencedAccountNumber
	 */
	public String getReferencedAccountNumber() {
		return referencedAccountNumber;
	}
	/**
	 * @param referencedAccountNumber the referencedAccountNumber to set
	 */
	public void setReferencedAccountNumber(String referencedAccountNumber) {
		this.referencedAccountNumber = referencedAccountNumber;
	}
	
}
