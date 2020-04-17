package com.nucleus.finnone.pro.communicationgenerator.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.communicationgenerator.vo.OnDemandRequestVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.RequestVO;
import com.nucleus.rules.model.SourceProduct;

public  class RequestVOBuilder {

	private String eventCode;
	private String communicationCode;
	private SourceProduct sourceProduct;
	private String subjectURI;
	private String applicablePrimaryEntityURI;
	private AdditionalData additionalData;
	private String subjectReferenceNumber;
	private String subjectReferenceType;
	private String regenerationReasonCode;
	private Character issueReissueFlag;
	private Date referenceDate;
	private String requestReferenceId;
	private Boolean generateMergedFile;
	private Character status;
	private String createdByUri;
    private String additionalDataString;
    private String primaryPhoneNumber;
    private String primaryEmailAddress;
    private String bccEmailAddress;
    private String ccEmailAddress;
    private Boolean returnContentOnly;
    private Map<String, byte[]> onDemandAttachments;
	private List<String> filePaths;

	public RequestVOBuilder setEventCode(String eventCode) {
		this.eventCode = eventCode;
		return this;
	}

	public RequestVOBuilder setCommunicationCode(String communicationCode) {
		this.communicationCode = communicationCode;
		return this;
	}

	public RequestVOBuilder setSourceProduct(SourceProduct sourceProduct) {
		this.sourceProduct = sourceProduct;
		return this;
	}
	public String getPrimaryPhoneNumber() {
		return primaryPhoneNumber;
	}

	public RequestVOBuilder setPrimaryPhoneNumber(String primaryPhoneNumber) {
		this.primaryPhoneNumber = primaryPhoneNumber;
		return this;
	}

	public String getPrimaryEmailAddress() {
		return primaryEmailAddress;
	}

	public RequestVOBuilder setPrimaryEmailAddress(String primaryEmailAddress) {
		this.primaryEmailAddress = primaryEmailAddress;
		return this;
	}

	public String getBccEmailAddress() {
		return bccEmailAddress;
	}

	public RequestVOBuilder setBccEmailAddress(String bccEmailAddress) {
		this.bccEmailAddress = bccEmailAddress;
		return this;
	}

	public String getCcEmailAddress() {
		return ccEmailAddress;
	}

	public RequestVOBuilder setCcEmailAddress(String ccEmailAddress) {
		this.ccEmailAddress = ccEmailAddress;
		return this;
	}
	
	public RequestVOBuilder setReturnContentOnly(Boolean returnContentOnly) {
		this.returnContentOnly = returnContentOnly;
		return this;
	}
	
	public Boolean getReturnContentOnly(Boolean returnContentOnly) {
		return returnContentOnly;
	}
	public RequestVOBuilder setSubjectURI(String subjectURI) {
		this.subjectURI = subjectURI;
		return this;
	}

	public RequestVOBuilder setApplicablePrimaryEntityURI(
			String applicablePrimaryEntityURI) {
		this.applicablePrimaryEntityURI = applicablePrimaryEntityURI;
		return this;
	}

	public RequestVOBuilder setAdditionalData(AdditionalData additionalData) {
		this.additionalData = additionalData;
		return this;
	}

	public RequestVOBuilder setSubjectReferenceNumber(
			String subjectReferenceNumber) {
		this.subjectReferenceNumber = subjectReferenceNumber;
		return this;
	}

	public RequestVOBuilder setSubjectReferenceType(
			String subjectReferenceType) {
		this.subjectReferenceType = subjectReferenceType;
		return this;
	}
	
	
	public RequestVOBuilder setRegenerationReasonCode(
			String regenerationReasonCode) {
		this.regenerationReasonCode = regenerationReasonCode;
		return this;
	}

	public RequestVOBuilder setIssueReissueFlag(Character issueReissueFlag) {
		this.issueReissueFlag = issueReissueFlag;
		return this;
	}

	public RequestVOBuilder setReferenceDate(Date referenceDate) {
		this.referenceDate = referenceDate;
		return this;
	}

	public RequestVOBuilder setRequestReferenceId(String requestReferenceId) {
		this.requestReferenceId = requestReferenceId;
		return this;
	}

	public RequestVOBuilder setGenerateMergedFile(Boolean generateMergedFile) {
		this.generateMergedFile = generateMergedFile;
		return this;
	}

	public RequestVOBuilder setStatus(Character status) {
		this.status = status;
		return this;
	}

	public RequestVOBuilder setCreatedByUri(String createdByUri) {
		this.createdByUri = createdByUri;
		return this;
	}

	public RequestVOBuilder setAdditionalDataString(String additionalDataString) {
		this.additionalDataString = additionalDataString;
		return this;
	}

	public RequestVOBuilder setOnDemandAttachments(Map<String, byte[]> onDemandAttachments) {
		this.onDemandAttachments = onDemandAttachments;
		return this;
	}

	public RequestVOBuilder setFilePaths(List<String> filePaths) {
		this.filePaths = filePaths;
		return this;
	}

	public RequestVO build() {
		RequestVO requestVO = new RequestVO();		
        buildCommonAttributes(requestVO);
        return requestVO;
    }
	
	public OnDemandRequestVO buildOnDemandRequestVO() {
		OnDemandRequestVO onDemandRequestVO = new OnDemandRequestVO();
		buildCommonAttributes(onDemandRequestVO);
		return onDemandRequestVO;
    }
	
	private void buildCommonAttributes(RequestVO requestVO) {
		requestVO.setEventCode(this.eventCode);		
		requestVO.setCommunicationCode(this.communicationCode);
		requestVO.setSourceProduct(this.sourceProduct);
		requestVO.setSubjectURI(this.subjectURI);   
		requestVO.setApplicablePrimaryEntityURI(this.applicablePrimaryEntityURI);
		requestVO.setAdditionalData(this.additionalData);
		requestVO.setAdditionalDataString(this.additionalDataString);
		requestVO.setSubjectReferenceNumber(this.subjectReferenceNumber);
		requestVO.setSubjectReferenceType(this.subjectReferenceType);
		requestVO.setRegenerationReasonCode(this.regenerationReasonCode);
		requestVO.setIssueReissueFlag(this.issueReissueFlag);
		requestVO.setReferenceDate(this.referenceDate);
		requestVO.setRequestReferenceId(this.requestReferenceId);
		requestVO.setGenerateMergedFile(this.generateMergedFile) ;
		requestVO.setStatus(this.status);
		requestVO.setCreatedByUri(this.createdByUri);
		requestVO.setPrimaryEmailAddress(this.primaryEmailAddress);
		requestVO.setBccEmailAddress(this.bccEmailAddress);
		requestVO.setCcEmailAddress(this.ccEmailAddress);
		requestVO.setPrimaryPhoneNumber(this.primaryPhoneNumber);
		requestVO.setreturnContentOnly(this.returnContentOnly);
		requestVO.setOnDemandAttachments(this.onDemandAttachments);
		requestVO.setFilePaths(this.filePaths);
	}
 
}