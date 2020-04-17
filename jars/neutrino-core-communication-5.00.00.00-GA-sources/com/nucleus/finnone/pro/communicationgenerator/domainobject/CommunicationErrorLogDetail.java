package com.nucleus.finnone.pro.communicationgenerator.domainobject;


import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STRING_LENGTH_FIVE_HUNDRED;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STRING_LENGTH_FOUR_THOUSAND;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STRING_LENGTH_TWO_THOUSAND;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;


@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "COM_COMMN_ERROR_LOG_DTL") 
@Synonym(grant="ALL")
public class CommunicationErrorLogDetail  extends BaseEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "ERROR_TYPE")
	private Long errorType;
	
	@Column(name = "ERROR_MESSAGEID", length = STRING_LENGTH_FIVE_HUNDRED)
	private String errorMessageId;

	@Column(name = "ERROR_MESSAGE_PARAMETERS", length = STRING_LENGTH_TWO_THOUSAND)
	private String errorMessageparameters;
	
	@Column(name = "ERROR_DESCRIPTION_EN", length = STRING_LENGTH_FOUR_THOUSAND)
	private String errorDescription;
	
	@Column(name = "EXCEPTION_STACK_DESCRIPTION", length = STRING_LENGTH_FOUR_THOUSAND)
	private String exceptionStackDescription;	

	@Column(name = "EVENT_CODE")
	private String communicationEventCode;
	
	@Column(name = "COMMUNICATION_CODE")
	private String communicationCode;
	
	@Column(name="COMMN_TEMPLATE_CODE")
	private String communicationTemplateCode;
	
	@Column(name = "SUBJECT_ID" )
	private Long subjectId;
	
	@Column(name = "SUBJECT_URI")
	private String subjectUri;
	
	@Column(name = "APPLICABLE_PRIMARY_ENTITY_ID")
	private Long applicablePrimaryEntityID;
	
	@Column(name = "APPLICABLE_PRIMARY_ENTITY_URI")
	private String applicablePrimaryEntityUri;
	
	@Column(name = "SUBJECT_REFERENCE_NO")
	private String subjectReferenceNumber;
	
	@Column(name = "SUBJECT_REFERENCE_TYPE")
	private String subjectReferenceType;
	
	@Column(name = "SUBJECT_TYPE")
	private Character subjectType;
	
	private Date referenceDate;

	public Long getErrorType() {
		return errorType;
	}

	public void setErrorType(Long errorType) {
		this.errorType = errorType;
	}

	public String getErrorMessageId() {
		return errorMessageId;
	}

	public void setErrorMessageId(String errorMessageId) {
		this.errorMessageId = errorMessageId;
	}

	public String getErrorMessageparameters() {
		return errorMessageparameters;
	}

	public void setErrorMessageparameters(String errorMessageparameters) {
		this.errorMessageparameters = errorMessageparameters;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getExceptionStackDescription() {
		return exceptionStackDescription;
	}

	public void setExceptionStackDescription(String exceptionStackDescription) {
		this.exceptionStackDescription = exceptionStackDescription;
	}

	public String getCommunicationEventCode() {
		return communicationEventCode;
	}

	public void setCommunicationEventCode(String communicationEventCode) {
		this.communicationEventCode = communicationEventCode;
	}

	public String getCommunicationCode() {
		return communicationCode;
	}

	public void setCommunicationCode(String communicationCode) {
		this.communicationCode = communicationCode;
	}

	public String getCommunicationTemplateCode() {
		return communicationTemplateCode;
	}

	public void setCommunicationTemplateCode(String communicationTemplateCode) {
		this.communicationTemplateCode = communicationTemplateCode;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public String getSubjectUri() {
		return subjectUri;
	}

	public void setSubjectUri(String subjectUri) {
		this.subjectUri = subjectUri;
	}

	public Long getApplicablePrimaryEntityID() {
		return applicablePrimaryEntityID;
	}

	public void setApplicablePrimaryEntityID(Long applicablePrimaryEntityID) {
		this.applicablePrimaryEntityID = applicablePrimaryEntityID;
	}

	public String getApplicablePrimaryEntityUri() {
		return applicablePrimaryEntityUri;
	}

	public void setApplicablePrimaryEntityUri(String applicablePrimaryEntityUri) {
		this.applicablePrimaryEntityUri = applicablePrimaryEntityUri;
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

	public Character getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(Character subjectType) {
		this.subjectType = subjectType;
	}

	public Date getReferenceDate() {
		return referenceDate;
	}

	public void setReferenceDate(Date referenceDate) {
		this.referenceDate = referenceDate;
	}	
	
}
