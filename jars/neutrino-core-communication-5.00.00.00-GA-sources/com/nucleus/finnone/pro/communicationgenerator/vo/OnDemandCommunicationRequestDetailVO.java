package com.nucleus.finnone.pro.communicationgenerator.vo;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;

public class OnDemandCommunicationRequestDetailVO extends OnDemandRequestVO {
	
	private Long applicablePrimaryEntityId;
	
	private Long subjectId;
	
	private CommunicationTemplate communicationTemplate;
	
	private List<CommunicationTemplate> attachedCommunicationTemplates;
	
	private String communicationText;
	
	private String alternatePhoneNumber;
	
	private Integer retriedAttemptsDone=0;
	
	private String communicationTemplateCode;
	
	private Date processDate;
	
	private DateTime eventLogTimeStamp;
	
	public Long getApplicablePrimaryEntityId() {
		return applicablePrimaryEntityId;
	}

	public void setApplicablePrimaryEntityId(Long applicablePrimaryEntityId) {
		this.applicablePrimaryEntityId = applicablePrimaryEntityId;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

//	public String getCommunicationEventCode() {
//		return communicationEventCode;
//	}
//
//	public void setCommunicationEventCode(String communicationEventCode) {
//		this.communicationEventCode = communicationEventCode;
//	}

	public CommunicationTemplate getCommunicationTemplate() {
		return communicationTemplate;
	}

	public void setCommunicationTemplate(CommunicationTemplate communicationTemplate) {
		this.communicationTemplate = communicationTemplate;
	}
	
	public List<CommunicationTemplate> getAttachedCommunicationTemplates() {
		return attachedCommunicationTemplates;
	}

	public void setAttachedCommunicationTemplates(List<CommunicationTemplate> attachedCommunicationTemplates) {
		this.attachedCommunicationTemplates = attachedCommunicationTemplates;
	}

	public String getCommunicationText() {
		return communicationText;
	}

	public void setCommunicationText(String communicationText) {
		this.communicationText = communicationText;
	}

//	public String getPhoneNumber() {
//		return phoneNumber;
//	}
//
//	public void setPhoneNumber(String phoneNumber) {
//		this.phoneNumber = phoneNumber;
//	}

	public String getAlternatePhoneNumber() {
		return alternatePhoneNumber;
	}

	public void setAlternatePhoneNumber(String alternatePhoneNumber) {
		this.alternatePhoneNumber = alternatePhoneNumber;
	}

	public Integer getRetriedAttemptsDone() {
		return retriedAttemptsDone;
	}

	public void setRetriedAttemptsDone(Integer retriedAttemptsDone) {
		this.retriedAttemptsDone = retriedAttemptsDone;
	}

	public String getCommunicationTemplateCode() {
		return communicationTemplateCode;
	}

	public void setCommunicationTemplateCode(String communicationTemplateCode) {
		this.communicationTemplateCode = communicationTemplateCode;
	}

	public Date getProcessDate() {
		return processDate;
	}

	public void setProcessDate(Date processDate) {
		this.processDate = processDate;
	}

	public DateTime getEventLogTimeStamp() {
		return eventLogTimeStamp;
	}

	public void setEventLogTimeStamp(DateTime eventLogTimeStamp) {
		this.eventLogTimeStamp = eventLogTimeStamp;
	}

}
