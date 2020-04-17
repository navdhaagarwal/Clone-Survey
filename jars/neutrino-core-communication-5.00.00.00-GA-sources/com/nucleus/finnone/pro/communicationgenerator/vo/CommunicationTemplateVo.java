package com.nucleus.finnone.pro.communicationgenerator.vo;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * Form backing object of Communication Template Object
 *
 */
public class CommunicationTemplateVo {


	private static final long serialVersionUID = -7944174691531905242L;
	private Long id;
	private String communicationTemplateCode;
	private String communicationTemplateName;
	private String communicationTemplateFile;
	private String uploadedDocumentId;
	private CommonsMultipartFile uploadedTemplate;
	private String operationType;
	private String subject;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCommunicationTemplateCode() {
		return communicationTemplateCode;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setCommunicationTemplateCode(String communicationTemplateCode) {
		this.communicationTemplateCode = communicationTemplateCode;
	}

	public String getCommunicationTemplateName() {
		return communicationTemplateName;
	}

	public void setCommunicationTemplateName(String communicationTemplateName) {
		this.communicationTemplateName = communicationTemplateName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	

	public String getUploadedDocumentId() {
		return uploadedDocumentId;
	}

	public void setUploadedDocumentId(String uploadedDocumentId) {
		this.uploadedDocumentId = uploadedDocumentId;
	}

	public String getCommunicationTemplateFile() {
		return communicationTemplateFile;
	}

	public void setCommunicationTemplateFile(String communicationTemplateFile) {
		this.communicationTemplateFile = communicationTemplateFile;
	}

	

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public CommonsMultipartFile getUploadedTemplate() {

		return uploadedTemplate;
	}

	public void setUploadedTemplate(CommonsMultipartFile uploadedTemplate) {
		this.uploadedTemplate = uploadedTemplate;
	}

	

}
