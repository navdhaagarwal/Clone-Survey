/**
 * 
 */
package com.nucleus.finnone.pro.general.util.email;

import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

/**
 * @author dhananjay.jha
 *
 */
public class EmailVO {
	/**
	 * 
	 */
	private InternetAddress fromAddress;	
	private InternetAddress replyTo;	
	private String subject;	
	private List<InternetAddress> toAddressList =null;	
	private List<InternetAddress> ccAddressList = null;	
	private List<InternetAddress> bccAddressList = null;	
	private String content;
	private String contentTemplateFilePath;	
	/**
	 * Map contains object used in template
	 */
	private Map<String, Object> templateMappingObject;
	/**
	 * List of attachments
	 */
	private List<AttachmentVO> attachments;
	public InternetAddress getFromAddress() {
		return fromAddress;
	}
	public void setFromAddress(InternetAddress fromAddress) {
		this.fromAddress = fromAddress;
	}
	public InternetAddress getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(InternetAddress replyTo) {
		this.replyTo = replyTo;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public List<InternetAddress> getToAddressList() {
		return toAddressList;
	}
	public void setToAddressList(List<InternetAddress> toAddressList) {
		this.toAddressList = toAddressList;
	}
	public List<InternetAddress> getCcAddressList() {
		return ccAddressList;
	}
	public void setCcAddressList(List<InternetAddress> ccAddressList) {
		this.ccAddressList = ccAddressList;
	}
	public List<InternetAddress> getBccAddressList() {
		return bccAddressList;
	}
	public void setBccAddressList(List<InternetAddress> bccAddressList) {
		this.bccAddressList = bccAddressList;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContentTemplateFilePath() {
		return contentTemplateFilePath;
	}
	public void setContentTemplateFilePath(String contentTemplateFilePath) {
		this.contentTemplateFilePath = contentTemplateFilePath;
	}
	public Map<String, Object> getTemplateMappingObject() {
		return templateMappingObject;
	}
	public void setTemplateMappingObject(Map<String, Object> templateMappingObject) {
		this.templateMappingObject = templateMappingObject;
	}
	public List<AttachmentVO> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<AttachmentVO> attachments) {
		this.attachments = attachments;
	}
	
}
