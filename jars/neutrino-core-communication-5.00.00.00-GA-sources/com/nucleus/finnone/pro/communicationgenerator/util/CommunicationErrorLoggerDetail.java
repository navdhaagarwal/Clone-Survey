package com.nucleus.finnone.pro.communicationgenerator.util;

import java.util.Date;
public class CommunicationErrorLoggerDetail {

	private Long id;
	private Long tenantId;
	private Date processBusinessDate;
	private String transactionEvent;
	private Long transactionProcessId;
	private Long transactionProcessDetailId;
	private Long transactionReferenceId;
	private Long eodProcessId;
	private Long errorType;
	private String errorMessageId;
	private String errorMessageparameters;
	private String errorDescription;
	private Long subjectId;
	private Character subjectType;
	private String subjectReferenceNumber;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTenantId() {
		return tenantId;
	}
	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}
	public Date getProcessBusinessDate() {
		return processBusinessDate;
	}
	public void setProcessBusinessDate(Date processBusinessDate) {
		this.processBusinessDate = processBusinessDate;
	}
	public String getTransactionEvent() {
		return transactionEvent;
	}
	public void setTransactionEvent(String transactionEvent) {
		this.transactionEvent = transactionEvent;
	}
	public Long getTransactionProcessId() {
		return transactionProcessId;
	}
	public void setTransactionProcessId(Long transactionProcessId) {
		this.transactionProcessId = transactionProcessId;
	}
	public Long getTransactionProcessDetailId() {
		return transactionProcessDetailId;
	}
	public void setTransactionProcessDetailId(Long transactionProcessDetailId) {
		this.transactionProcessDetailId = transactionProcessDetailId;
	}
	public Long getTransactionReferenceId() {
		return transactionReferenceId;
	}
	public void setTransactionReferenceId(Long transactionReferenceId) {
		this.transactionReferenceId = transactionReferenceId;
	}
	public Long getEodProcessId() {
		return eodProcessId;
	}
	public void setEodProcessId(Long eodProcessId) {
		this.eodProcessId = eodProcessId;
	}
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
	public Long getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}
	public Character getSubjectType() {
		return subjectType;
	}
	public void setSubjectType(Character subjectType) {
		this.subjectType = subjectType;
	}
	public String getSubjectReferenceNumber() {
		return subjectReferenceNumber;
	}
	public void setSubjectReferenceNumber(String subjectReferenceNumber) {
		this.subjectReferenceNumber = subjectReferenceNumber;
	}

	
}
