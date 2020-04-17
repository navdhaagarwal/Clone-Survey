/*
 * Author: Abhishek Pallav
 * Creation Date: 24-Aug-2012
 * Copyright: Nucleus Software Exports Ltd.
 * Description: This is base exception class which will be superclass of all custom exceptions.
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Revision:  Version         Last Revision Date                   Name                Function / Module affected  Modifications Done
 * ------------------------------------------------------------------------------------------------------------------------------------
 *                1.0             24/08/2012                    Abhishek Pallav           Initial Version created
 *                1.1             19/02/2014                    Abhishek Pallav           Added subjectId & subjectType for better diagnosis of the error stack  
 */
package com.nucleus.finnone.pro.base.exception;

import java.util.List;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.constants.BaseExceptionSubjectTypeEnum;
import com.nucleus.finnone.pro.base.constants.CoreConstant;
import com.nucleus.finnone.pro.lmsbase.utility.GenericUtility; 

public class BaseException extends RuntimeException {

	/** 
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long subjectId;
	
	private String subjectReferenceNumber;

	private String transactionReferenceNumber;

	private Long transactionId;

	private Character subjectType;

	private List<Message> messages;

	private Boolean isLogged;
	
	private Boolean reProcessingRequired;

	private Long processId;
	
	private String processDescription;
	
	private String uniqueID;

	private String logMessage;

	private Integer severity;

	private String exceptionCode;

	private Exception originalException;

	public BaseException() {
		this.isLogged = Boolean.FALSE;
		this.reProcessingRequired = Boolean.FALSE; 
		this.uniqueID = GenericUtility.getUniqueId();
		this.severity = CoreConstant.SEVERITY_LOW;
	}

	public BaseException(String message) {
		super(message);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseException(Throwable cause) {
		super(cause);
	}

	public String getExceptionCode() {
		return this.exceptionCode;
	}

	public String getLogMessage() {
		return this.logMessage;
	}

	public List<Message> getMessages() {
		return this.messages;
	}

	public Exception getOriginalException() {
		return this.originalException;
	}

	public Integer getSeverity() {
		return this.severity;
	}

	public String getUniqueID() {
		return this.uniqueID;
	}

	public Boolean isLogged() {
		return (this.isLogged==null)?Boolean.FALSE:this.isLogged;
	}

	public void setExceptionCode(String exceptionCode) {
		this.exceptionCode = exceptionCode;
	}

	public void setLogged(Boolean isLogged) {
		this.isLogged = isLogged;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	public void setMessages(List<Message> theMessages) {
		this.messages = theMessages;
	}

	public void setOriginalException(Exception originalException) {
		this.originalException = originalException;
	}

	public void setSeverity(Integer severity) {
		this.severity = severity;
	}
	
	
	/**
	 * returns 
	 * 		PrimaryKey of the subjectReferenceNumber as per the subjectType, 
	 *      for e.g. if the subjectType = LOAN then this function returns loanId 
	 *  
	 * @return
	 */
	public Long getSubjectId() {
		return this.subjectId;
	}

	
	/**
	 * set 
	 * 		PrimaryKey of the subjectReferenceNumber as per the subjectType, 
	 *      for e.g. if the subjectType = LOAN then 
	 *                  pass loanId as parameter i.e. setSubjectId(loanId) 
	 *  
	 * @return
	 */
	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public Character getSubjectType() {
		return this.subjectType;
	}

	
	/**
	 * returns 
	 * 		LAN Number if the subjectType = LOAN
	 * 		CustomerNumber if the subjectType = CUSTOMER
	 *  
	 * @return
	 */
	public String getSubjectReferenceNumber() {
		return this.subjectReferenceNumber;
	}

	
	/**
	 * set 
	 * 		LAN Number if the subjectType = LOAN
	 * 		CustomerNumber if the subjectType = CUSTOMER
	 *  
	 * @return
	 */
	public void setSubjectReferenceNumber(String subjectReferenceNumber) {
		this.subjectReferenceNumber = subjectReferenceNumber;
	}

	public void setSubjectType(Character subjectType) {
		this.subjectType = subjectType;
	}
	
	public void markSubjectTypeLoan(){
		setSubjectType(BaseExceptionSubjectTypeEnum.SUBJECT_TYPE_LOAN.getEnumValue());
	}
	
	public void markSubjectTypeCustomer(){
		setSubjectType(BaseExceptionSubjectTypeEnum.SUBJECT_TYPE_CUSTOMER.getEnumValue());
	}
	
	public void markSubjectTypeNull(){
		setSubjectType(BaseExceptionSubjectTypeEnum.NULL.getEnumValue());
	}
	
	public Boolean isReProcessingRequired() {
		return (this.reProcessingRequired==null)?Boolean.FALSE:this.reProcessingRequired;
	}
	
	public void setReProcessingRequired(Boolean reProcessingRequired) {
		this.reProcessingRequired = reProcessingRequired;
	}

	public void reProcessingRequired() {
		this.reProcessingRequired = Boolean.TRUE;
	}

	public void reProcessingNotRequired() {
		this.reProcessingRequired = Boolean.FALSE;
	}

	public Long getProcessId() {
		return this.processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	public String getProcessDescription() {
		return this.processDescription;
	}

	public void setProcessDescription(String processDescription) {
		this.processDescription = processDescription; 
	}

	public String getTransactionReferenceNumber() {
		return transactionReferenceNumber;
	}

	public void setTransactionReferenceNumber(String transactionReferenceNumber) {
		this.transactionReferenceNumber = transactionReferenceNumber;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}
}