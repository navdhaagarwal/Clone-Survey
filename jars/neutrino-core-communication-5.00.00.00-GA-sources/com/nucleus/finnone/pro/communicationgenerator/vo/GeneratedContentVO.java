package com.nucleus.finnone.pro.communicationgenerator.vo;

public class GeneratedContentVO {
	private byte[] generatedContent;
	private String location;
	private String generatedText;
	private String eventRequestLogId;
	private boolean isBarcodeImageAttached = false;
	private String fileName;
	private String communicationType;
	private MailMessageContentVO mailMessageContentVO;
	private SmsMessageContentVO smsMessageContentVO;
	
	public MailMessageContentVO getMailMessageContentVO() {
		return mailMessageContentVO;
	}

	public void setMailMessageContentVO(MailMessageContentVO mailMessage) {
		this.mailMessageContentVO = mailMessage;
	}

	public SmsMessageContentVO getSmsMessageContentVO() {
		return smsMessageContentVO;
	}

	public void setSmsMessageContentVO(SmsMessageContentVO smsMessage) {
		this.smsMessageContentVO = smsMessage;
	}

	public String getCommunicationType() {
		return communicationType;
	}

	public void setCommunicationType(String communicationType) {
		this.communicationType = communicationType;
	}

	public String getGeneratedText() {
		return generatedText;
	}

	public void setGeneratedText(String generatedText) {
		this.generatedText = generatedText;
	}

	public byte[] getGeneratedContent() {
		return generatedContent;
	}

	public void setGeneratedContent(byte[] generatedContent) {
		this.generatedContent = generatedContent;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getEventRequestLogId() {
		return eventRequestLogId;
	}

	public void setEventRequestLogId(String eventRequestLogId) {
		this.eventRequestLogId = eventRequestLogId;
	}

	public boolean isBarcodeImageAttached() {
		return isBarcodeImageAttached;
	}

	public void setBarcodeImageAttached(boolean isBarcodeImageAttached) {
		this.isBarcodeImageAttached = isBarcodeImageAttached;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
