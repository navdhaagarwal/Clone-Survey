package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationEventLoggerDelegate {
	
	List<CommunicationRequestDetail> selectTemplateAndGenerateCommForOnDemandReq(
			Boolean isOnDemandGeneration, String eventCode, SourceProduct module, String communicationCode,
			Map<String, Object> localCacheMapForTemplate, CommunicationEventRequestLog commEventRequestLog);

	List<CommunicationRequestDetail> selectTemplateAndGenerateCommReqDtlsForBulk(Boolean isOnDemandGeneration,
			String eventCode, SourceProduct module, String subjectURI, String applicablePrimaryEntityURI,
			Long deliveryPriority, String subjectReferenceNumber, List<CommunicationName> communicationNameList);

	void selectTemplateAndGenerateCommForImmediateReq(CommunicationEventRequestLog commEventRequestLog);

}
