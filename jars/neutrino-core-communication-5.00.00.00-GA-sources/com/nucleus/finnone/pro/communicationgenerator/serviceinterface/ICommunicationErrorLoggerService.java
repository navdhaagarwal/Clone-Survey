package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;

public interface ICommunicationErrorLoggerService {
	void createCommunicationProcessErrorLoggerDetail(
			List<CommunicationErrorLogDetail> communicationErrorLogDetail);

	void updateCommunicationAndCommunicationProcessErrorLoggerDetail(
			CommunicationRequestDetail communicationRequestDetail,
			List<Message> errorMessages,Map<String, Object> localCacheMap);
}
