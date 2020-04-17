package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationErrorLoggerDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;

@Named("communicationErrorLoggerBusinessObject")
public class CommunicationErrorLoggerBusinessObject implements ICommunicationErrorLoggerBusinessObject {
	
	
	@Inject
	@Named("communicationErrorLoggerDAO")
	private ICommunicationErrorLoggerDAO communicationErrorLoggerDAO;
	
		@Override
		@Transactional(propagation=Propagation.REQUIRES_NEW)
		public void createCommunicationProcessErrorLoggerDetail(List<CommunicationErrorLogDetail> communicationErrorLogDetail){
		communicationErrorLoggerDAO.createCommunicationProcessErrorLoggerDetail(communicationErrorLogDetail);
	}
		@Override
		@Transactional(propagation=Propagation.REQUIRED)
		public void createCommunicationProcessErrorLoggerDetailInSameTransaction(List<CommunicationErrorLogDetail> communicationErrorLogDetail){
		communicationErrorLoggerDAO.createCommunicationProcessErrorLoggerDetail(communicationErrorLogDetail);
	}
	
}
