package com.nucleus.finnone.pro.communicationgenerator.vo;

import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.rules.model.SourceProduct;


public class CommunicationGenerationDetailVO {
	
	private Long applicablePrimaryEntityId;
	private String communicationCode;
	private String eventCode;
	private Long subjectEntityId;
	private String subjectURI;
	private Character status;
	private Boolean isOnDemandGeneration = Boolean.FALSE;
	private SourceProduct sourceProduct;
	private String requestReferenceId;
	private Boolean generateMergedFile;	
	private Long parentCommunicationRequestDetailId;
	private String  schedularInstanceId;
	private String requestType;
	private String subjectReferenceNumber;
    private String subjectReferenceType;
	private String attachmentName;
	private String eventRequestLogId;
	
	public String getSchedularInstanceId() {
		return schedularInstanceId;
	}

	public void setSchedularInstanceId(String schedularInstanceId) {
		this.schedularInstanceId = schedularInstanceId;
	}

	public Long getParentCommunicationRequestDetailId() {
		return parentCommunicationRequestDetailId;
	}

	public void setParentCommunicationRequestDetailId(Long parentCommunicationId) {
		this.parentCommunicationRequestDetailId = parentCommunicationId;
	}

	public Long getApplicablePrimaryEntityId() {
		return applicablePrimaryEntityId;
	}

	public void setApplicablePrimaryEntityId(Long applicablePrimaryEntityId) {
		this.applicablePrimaryEntityId = applicablePrimaryEntityId;
	}

	public Boolean getIsOnDemandGeneration() {
		return isOnDemandGeneration;
	}

	public void setIsOnDemandGeneration(Boolean isOnDemandGeneration) {
		this.isOnDemandGeneration = isOnDemandGeneration;
	}

	public String getCommunicationCode() {
		return communicationCode;
	}
	
	public void setCommunicationCode(String communicationCode) {
		this.communicationCode = communicationCode;
	}
	
	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public Long getSubjectEntityId() {
		return subjectEntityId;
	}

	public void setSubjectEntityId(Long subjectEntityId) {
		this.subjectEntityId = subjectEntityId;
	}

	public String getSubjectURI() {
		return subjectURI;
	}
	
	public void setSubjectURI(String subjectURI) {
		this.subjectURI = subjectURI;
	}
	
	public Character getStatus() {
		return status;
	}
	
	public void setStatus(Character status) {
		this.status = status;
	}
	
	public Boolean isOnDemandGeneration() {
		return isOnDemandGeneration != null ? isOnDemandGeneration :  Boolean.FALSE;
	}

	public void setOnDemandGeneration(Boolean isOnDemandGeneration) {
		this.isOnDemandGeneration = isOnDemandGeneration;
	}

    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

	public String getRequestReferenceId() {
		return requestReferenceId;
	}

	public void setRequestReferenceId(String requestReferenceId) {
		this.requestReferenceId = requestReferenceId;
	}

	public Boolean getGenerateMergedFile() {
	    if(ValidatorUtils.isNull(this.generateMergedFile))
	        this.generateMergedFile=false;
		return generateMergedFile;
	}

	public void setGenerateMergedFile(Boolean generateMergedFile) {
		this.generateMergedFile = generateMergedFile;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getSubjectReferenceNumber() {
		return subjectReferenceNumber;
	}

	public void setSubjectReferenceNumber(String subjectReferenceNumber) {
		this.subjectReferenceNumber = subjectReferenceNumber;
	}

	public String getSubjectReferenceType() {
		return subjectReferenceType;
	}

	public void setSubjectReferenceType(String subjectReferenceType) {
		this.subjectReferenceType = subjectReferenceType;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getEventRequestLogId() {
		return eventRequestLogId;
	}

	public void setEventRequestLogId(String eventRequestLogId) {
		this.eventRequestLogId = eventRequestLogId;
	}
	
}
