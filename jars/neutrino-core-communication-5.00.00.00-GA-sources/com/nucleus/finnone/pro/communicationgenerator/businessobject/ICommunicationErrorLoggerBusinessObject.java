package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.List;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;

public interface ICommunicationErrorLoggerBusinessObject {
	void createCommunicationProcessErrorLoggerDetail(List<CommunicationErrorLogDetail> communicationErrorLogDetail);
	
	void createCommunicationProcessErrorLoggerDetailInSameTransaction(List<CommunicationErrorLogDetail> communicationErrorLogDetail);
}
