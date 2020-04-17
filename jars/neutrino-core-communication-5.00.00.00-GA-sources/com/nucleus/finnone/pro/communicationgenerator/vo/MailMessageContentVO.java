package com.nucleus.finnone.pro.communicationgenerator.vo;

import java.util.List;

public class MailMessageContentVO {

	private String fromEmailAddress;
	private String subject;
	private String htmlBody;
	private String toAddressList;
	private String ccAddressList;
	private String bccAddressList;
	private List<String> attachmentFileNameList;
	
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

	public List<String> getAttachmentFileNameList() {
		return attachmentFileNameList;
	}

	public void setAttachmentFileNameList(List<String> attachmentFileNameList) {
		this.attachmentFileNameList = attachmentFileNameList;
	}

}
