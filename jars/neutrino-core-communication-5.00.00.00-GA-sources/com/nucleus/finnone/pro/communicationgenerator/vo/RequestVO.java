package com.nucleus.finnone.pro.communicationgenerator.vo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.rules.model.SourceProduct;

public class RequestVO {
	
	private String communicationCode;
	
	private SourceProduct sourceProduct;
	
	private String subjectURI;

	private String applicablePrimaryEntityURI;
	
	private AdditionalData additionalData;
	
	private String subjectReferenceNumber;
	
	private String subjectReferenceType;
	
	private String regenerationReasonCode;

	private Character issueReissueFlag;
	
	private Character status;
	
	private Date referenceDate;
	
	private String primaryEmailAddress;
	
	private String bccEmailAddress;

	private String ccEmailAddress;
	
	//Performance improvement in case of initialized data from modules.
	//like subjectURI, primaryEntityURI will be already initialized. 
	private Map<String, Object> initializedData;

	private String eventCode;

	private String additionalDataString;

	private String requestReferenceId;

	private Boolean generateMergedFile;

	private String createdByUri;

	private String primaryPhoneNumber;

	private Boolean returnContentOnly = true;

	private Map<String, byte[]> onDemandAttachments;

	private List<String> filePaths;
	
	private boolean skipStorageForLetter;
	
	private Boolean previewFlag;
	
	private Long deliveryPriority;
	
	private String requestType;
	
	public String getCommunicationCode() {
		return communicationCode;
	}

	public void setCommunicationCode(String communicationCode) {
		this.communicationCode = communicationCode;
	}
	
	public SourceProduct getSourceProduct() {
		return sourceProduct;
	}

	public void setSourceProduct(SourceProduct sourceProduct) {
		this.sourceProduct = sourceProduct;
	}
	
	public String getSubjectURI() {
		return subjectURI;
	}

	public void setSubjectURI(String subjectURI) {
		this.subjectURI = subjectURI;
	}

	public String getApplicablePrimaryEntityURI() {
		return applicablePrimaryEntityURI;
	}

	public void setApplicablePrimaryEntityURI(String applicablePrimaryEntityURI) {
		this.applicablePrimaryEntityURI = applicablePrimaryEntityURI;
	}
	
	public AdditionalData getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(AdditionalData additionalData) {
		this.additionalData = additionalData;
	}

	public String getSubjectReferenceNumber() {
		return subjectReferenceNumber;
	}

	public void setSubjectReferenceNumber(String subjectReferenceNumber) {
		this.subjectReferenceNumber = subjectReferenceNumber;
	}

	public String getRegenerationReasonCode() {
		return regenerationReasonCode;
	}

	public void setRegenerationReasonCode(String regenerationReasonCode) {
		this.regenerationReasonCode = regenerationReasonCode;
	}

	public Character getIssueReissueFlag() {
		return issueReissueFlag;
	}

	public void setIssueReissueFlag(Character issueReissueFlag) {
		this.issueReissueFlag = issueReissueFlag;
	}
	
	public Character getStatus() {
		return status;
	}

	public void setStatus(Character status) {
		this.status = status;
	}

	public Date getReferenceDate() {
		return referenceDate;
	}

	public void setReferenceDate(Date referenceDate) {
		this.referenceDate = referenceDate;
	}
	
	public String getPrimaryEmailAddress() {
		return primaryEmailAddress;
	}

	public void setPrimaryEmailAddress(String primaryEmailAddress) {
		this.primaryEmailAddress = primaryEmailAddress;
	}

	public String getBccEmailAddress() {
		return bccEmailAddress;
	}

	public void setBccEmailAddress(String bccEmailAddress) {
		this.bccEmailAddress = bccEmailAddress;
	}

	public String getCcEmailAddress() {
		return ccEmailAddress;
	}

	public void setCcEmailAddress(String ccEmailAddress) {
		this.ccEmailAddress = ccEmailAddress;
	}
	
	public Map<String, Object> getInitializedData() {
		return initializedData;
	}

	public void setInitializedData(Map<String, Object> initializedData) {
		this.initializedData = initializedData;
	}
	
	public Boolean getreturnContentOnly() {
		return returnContentOnly;
	}

	public void setreturnContentOnly(Boolean generateContentOnly) {
		this.returnContentOnly = generateContentOnly;
	}

	public String getPrimaryPhoneNumber() {
		return primaryPhoneNumber;
	}

	public void setPrimaryPhoneNumber(String primaryPhoneNumber) {
		this.primaryPhoneNumber = primaryPhoneNumber;
	}

	public String getCreatedByUri() {
		return createdByUri;
	}

	public void setCreatedByUri(String createdByUri) {
		this.createdByUri = createdByUri;
	}

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public String getRequestReferenceId() {
		return requestReferenceId;
	}

	public void setRequestReferenceId(String requestReferenceId) {
		this.requestReferenceId = requestReferenceId;
	}

	public Boolean getGenerateMergedFile() {
		return generateMergedFile;
	}

	public void setGenerateMergedFile(Boolean generateMergedFile) {
		this.generateMergedFile = generateMergedFile;
	}

	public String getAdditionalDataString() {
		return additionalDataString;
	}

	public void setAdditionalDataString(String additionalDataString) {
		this.additionalDataString = additionalDataString;
	}

	public Map<String, byte[]> getOnDemandAttachments() {
		return onDemandAttachments;
	}

	public void setOnDemandAttachments(Map<String, byte[]> onDemandAttachments) {
		this.onDemandAttachments = onDemandAttachments;
	}

	public List<String> getFilePaths() {
		return filePaths;
	}

	public void setFilePaths(List<String> filePaths) {
		this.filePaths = filePaths;
	}
	
	public boolean isSkipStorageForLetter() {
		return skipStorageForLetter;
	}
	
	/**
	 * Set this true if you want to skip the storage of letter.
	 * By default it will store all letters in oracle/ECM. 
	 * 
	 * @param skipStorageForLetter {@code boolean} value
	 */
	public void setSkipStorageForLetter(boolean skipStorageForLetter) {
		this.skipStorageForLetter = skipStorageForLetter;
	}

	public Boolean getPreviewFlag() {
		return previewFlag;
	}

	public void setPreviewFlag(Boolean previewFlag) {
		this.previewFlag = previewFlag;
	}

	public Long getDeliveryPriority() {
		return deliveryPriority;
	}

	public void setDeliveryPriority(Long deliveryPriority) {
		this.deliveryPriority = deliveryPriority;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getSubjectReferenceType() {
		return subjectReferenceType;
	}

	public void setSubjectReferenceType(String subjectReferenceType) {
		this.subjectReferenceType = subjectReferenceType;
	}
	
	
}
