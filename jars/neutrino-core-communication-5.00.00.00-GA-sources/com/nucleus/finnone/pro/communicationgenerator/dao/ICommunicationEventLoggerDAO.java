package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.List;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.persistence.EntityDao;

public interface ICommunicationEventLoggerDAO  extends EntityDao{

	CommunicationRequestDetail createCommunicationGenerationDetail(CommunicationRequestDetail communicationGenerationDetail);
	public CommunicationRequestDetail updateCommunicationGenerationDetail(
			CommunicationRequestDetail communicationRequestDetail);
	CommunicationEventRequestLog createCommunicationEventRequest(
			CommunicationEventRequestLog communicationEventRequestLog);
	List<CommunicationEventRequestLog> searchCommEventsBasedOnCriteria(CommunicationEventRequestLog communicationEventRequestLog);
	
	List<CommunicationEventRequestLog> searchCommEventsBasedOnCriteria(
			CommunicationEventRequestLog communicationEventRequestLog, String criteria);
}
