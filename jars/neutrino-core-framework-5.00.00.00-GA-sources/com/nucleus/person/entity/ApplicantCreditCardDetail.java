package com.nucleus.person.entity;

import java.math.BigDecimal;

import com.nucleus.regional.RegionalData;

public class ApplicantCreditCardDetail {
	
	private Long applicantID;
	
	private String cardNumber;
	
	private String expiryDate;
	
	private String cardLimit;

	private String bankCode;

    private String resultSource;
    
	private boolean isRegionEnabled;
	
	private RegionalData regionalData;

	public Long getApplicantID() {
		return applicantID;
	}

	public void setApplicantID(Long applicantID) {
		this.applicantID = applicantID;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	

	public String getCardLimit() {
		return cardLimit;
	}

	public void setCardLimit(String cardLimit) {
		this.cardLimit = cardLimit;
	}


	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getResultSource() {
		return resultSource;
	}

	public void setResultSource(String resultSource) {
		this.resultSource = resultSource;
	}

	public boolean isRegionEnabled() {
		return isRegionEnabled;
	}

	public void setRegionEnabled(boolean isRegionEnabled) {
		this.isRegionEnabled = isRegionEnabled;
	}

	public RegionalData getRegionalData() {
		return regionalData;
	}

	public void setRegionalData(RegionalData regionalData) {
		this.regionalData = regionalData;
	}

}
