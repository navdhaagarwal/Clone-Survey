package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nucleus.core.event.EventCode;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.vo.GeneratedContentVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.OnDemandCommunicationRequestDetailVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.OnDemandRequestVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.RequestVO;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationEventLoggerService {

	List<CommunicationRequestDetail> createCommunicationGenerationDetail(
			List<CommunicationRequestDetail> communicationRequestDetails);

	CommunicationRequestDetail updateCommunicationGenerationDetail(
			CommunicationRequestDetail communicationRequestDetail);

	CommunicationEventRequestLog logCommunicationEvent(String eventCode, String subjectURI,
			AdditionalData additionalData, SourceProduct module,
			String applicablePrimaryEntityURI, String subjectReferenceNumber,
			Date referenceDate);

	
	CommunicationEventRequestLog logCommunicationEvent(String eventCode, String subjectURI,
			AdditionalData additionalData, SourceProduct module,
			String applicablePrimaryEntityURI, String subjectReferenceNumber,String subjectReferenceType,
			Date referenceDate);

	GeneratedContentVO logAndGenerateCommunicationForOnDemand(String eventCode,
			String communicationCode, SourceProduct module, String subjectURI,
			String applicablePrimaryEntityURI, AdditionalData additionalData,
			String subjectReferenceNumber,Date referenceDate);

	void logAndGenerateCommRequestsForLoggedEvents(List<EventCode> eventCodeList, SourceProduct module);

	void logAndGenerateCommRequestsForLoggedEvents(List<EventCode> eventCodeList, SourceProduct module,
			Map<Object, Object> parameters);
	
	GeneratedContentVO logAndGenerateCommunicationForOnDemand(
			OnDemandRequestVO onDemandRequestVO);

	GeneratedContentVO logAndGenerateCommunicationByTemplateForOnDemand(
			OnDemandCommunicationRequestDetailVO onDemandCommunicationRequestDetailVO);
	
	 GeneratedContentVO logAndGenerateCommunicationForOnDemand(String eventCode,
				String communicationCode, SourceProduct module, String subjectURI,
				String applicablePrimaryEntityURI, AdditionalData additionalData,String subjectReferenceNumber);

	 CommunicationEventRequestLog logCommunicationEvent(String eventCode,String subjectURI,AdditionalData additionalData,SourceProduct module,String applicablePrimaryEntityURI,String subjectReferenceNumber);
	 
	 CommunicationEventRequestLog logCommunicationEvent(RequestVO requestVO);

	GeneratedContentVO logAndGenerateCommunicationForOnDemandPreview(OnDemandRequestVO onDemandRequestVO);

	GeneratedContentVO logAndGenerateCommunicationByTemplateForPreviewOnDemand(
			OnDemandCommunicationRequestDetailVO onDemandCommunicationRequestDetailVO);

	CommunicationEventRequestLog createCommunicationEventRequest(CommunicationEventRequestLog communicationEventRequestLog);

	RequestVO createRequestVO(String subjectURI, String eventCode, SourceProduct module, String subjectReferenceNumber,
			String subjectReferenceType, String applicablePrimaryEntityURI, Date referenceDate, AdditionalData additionalData);

	CommunicationEventRequestLog logCommunicationEventInNewTransaction(RequestVO requestVo);

	void generateCommunicationForCallback(RequestVO requestVO, CommunicationEventRequestLog communicationEventRequestLog);

	GeneratedContentVO saveBytesAsPdfForPreview(byte[] bytes, String fileName);
}
