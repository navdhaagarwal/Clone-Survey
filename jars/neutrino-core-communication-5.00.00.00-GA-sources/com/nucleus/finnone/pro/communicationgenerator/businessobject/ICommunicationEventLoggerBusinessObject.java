package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.vo.OnDemandCommunicationRequestDetailVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.RequestVO;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationEventLoggerBusinessObject {
	
	CommunicationRequestDetail createCommunicationGenerationDetail(CommunicationRequestDetail communicationGenerationDetail);
	public CommunicationRequestDetail updateCommunicationGenerationDetail(
			CommunicationRequestDetail communicationRequestDetail);
	CommunicationEventRequestLog createCommunicationEventRequest(
			CommunicationEventRequestLog communicationEventRequestLog);
	
	List<CommunicationEventRequestLog> searchCommEventsBasedOnCriteria(CommunicationEventRequestLog communicationEventRequestLog);
	
	Map<CommunicationTemplate, List<CommunicationTemplate>> fetchCommunicationTemplatesBasedOnRuleExecution(CommunicationEventRequestLog communicationEventRequestLog,Boolean isOnDemandGeneration,String communicationCode,Map<String,Object> localCacheMapForTemplate);
	
	CommunicationEventRequestLog markCommEventRequestComplete(CommunicationEventRequestLog communicationEventRequestLog);
	void moveCommunicationEventRequestToHistory(CommunicationEventRequestLog communicationEventRequestLog);
	void deleteCommunicationEventRequest(CommunicationEventRequestLog communicationEventRequestLog);
	List<CommunicationErrorLogDetail> prepareErrorLogData(CommunicationEventRequestLog communicationEventRequestLog,String communicationCode,List<Message> errorMessages);
	void markCommEventRequestCompleteAndMoveToHistory(CommunicationEventRequestLog communicationEventRequestLog);
	void markCommEventRequestCompleteAndMoveToHistoryInNewTransaction(CommunicationEventRequestLog communicationEventRequestLog);
	void logCommunicationEvent(String eventCode,String subjectURI,AdditionalData additionalData,SourceProduct module,String applicablePrimaryEntityURI,String subjectReferenceNumber,Date referenceDate);
	CommunicationRequestDetail setCommunicationGenerationDetailFromOnDemandVO(OnDemandCommunicationRequestDetailVO onDemandCommunicationRequestDetailVO);
	
	void logCommunicationEvent(RequestVO requestVO);
	void persistAdditionalData(AdditionalData additionalData);
	CommunicationRequestDetail setCommunicationGenerationDetailFromPreviewOnDemandVO(
			OnDemandCommunicationRequestDetailVO onDemandCommunicationRequestDetailVO);

	List<CommunicationEventRequestLog> searchCommEventsBasedOnCriteria(
			CommunicationEventRequestLog communicationEventRequestLog, String decodedCriteria);
	List<CommunicationRequestDetail> moveEventRequestLifecycleToNextStageInNewTransaction(
			Map<CommunicationTemplate, List<CommunicationTemplate>> generatedCommnTemplateMap,
			CommunicationEventRequestLog requestLog, Boolean isOnDemandGeneration, String communicationCode);
	
}
