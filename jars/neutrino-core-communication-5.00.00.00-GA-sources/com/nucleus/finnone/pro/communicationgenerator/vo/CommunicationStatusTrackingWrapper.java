package com.nucleus.finnone.pro.communicationgenerator.vo;

import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.message.entity.MessageExchangeRecordHistory;

public class CommunicationStatusTrackingWrapper {
	
	private boolean isLetterTypeCommunication;
	
	private byte[] storedLetter;
	
	private CommunicationGenerationDetailHistory communicationHistory;

	private boolean isMailTypeCommunication;

	private Map<String, byte[]> emailAttachments;

	private MessageExchangeRecordHistory messageRecordHistory;

	public boolean isLetterTypeCommunication() {
		return isLetterTypeCommunication;
	}

	public void setLetterTypeCommunication(boolean isLetterTypeCommunication) {
		this.isLetterTypeCommunication = isLetterTypeCommunication;
	}

	public byte[] getStoredLetter() {
		return storedLetter;
	}

	public void setStoredLetter(byte[] storedLetter) {
		this.storedLetter = storedLetter;
	}

	public boolean isMailTypeCommunication() {
		return isMailTypeCommunication;
	}

	public void setMailTypeCommunication(boolean isMailTypeCommunication) {
		this.isMailTypeCommunication = isMailTypeCommunication;
	}

	public Map<String, byte[]> getEmailAttachments() {
		return emailAttachments;
	}

	public void setEmailAttachments(Map<String, byte[]> emailAttachments) {
		this.emailAttachments = emailAttachments;
	}

	public MessageExchangeRecordHistory getMessageRecordHistory() {
		return messageRecordHistory;
	}

	public void setMessageRecordHistory(MessageExchangeRecordHistory messageRecordHistory) {
		this.messageRecordHistory = messageRecordHistory;
	}

	public CommunicationGenerationDetailHistory getCommunicationHistory() {
		return communicationHistory;
	}

	public void setCommunicationHistory(CommunicationGenerationDetailHistory communicationHistory) {
		this.communicationHistory = communicationHistory;
	}
}
