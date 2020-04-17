package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationErrorLoggerBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationCommonService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationErrorLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationEventLoggerService;



@Service("communicationErrorLoggerService")
public class CommunicationErrorLoggerService implements ICommunicationErrorLoggerService {
	
	@Inject
	@Named(value="communicationErrorLoggerBusinessObject")
	private ICommunicationErrorLoggerBusinessObject communicationErrorLoggerBusinessObject;
	

	@Inject
	@Named(value="communicationEventLoggerService")
	private ICommunicationEventLoggerService communicationEventLoggerService;
	
	@Inject
	@Named(value="communicationDataPreparationWrapper")
	private CommunicationDataPreparationWrapper communicationDataPreparationWrapper;

	@Inject
	@Named("communicationCommonService")
	private ICommunicationCommonService communicationCommonService;

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void createCommunicationProcessErrorLoggerDetail(List<CommunicationErrorLogDetail> communicationErrorLogDetail){
		communicationErrorLoggerBusinessObject.createCommunicationProcessErrorLoggerDetail(communicationErrorLogDetail);
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void updateCommunicationAndCommunicationProcessErrorLoggerDetail(
			CommunicationRequestDetail communicationRequestDetail, List<Message> errorMessages,
			Map<String, Object> localCacheMap) {

		CommunicationRequestDetail communicationRequest = communicationCommonService
				.findById(communicationRequestDetail.getId(), CommunicationRequestDetail.class);
		if(localCacheMap.containsKey(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID))
		{
			communicationRequest.setSchedularInstanceId((String) localCacheMap.get(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID));
		}
		communicationRequest
				.setRetriedAttemptsDone(communicationRequest
						.getRetriedAttemptsDone() + 1);
		communicationEventLoggerService
				.updateCommunicationGenerationDetail(communicationRequest);
		 

		List<CommunicationErrorLogDetail> communicationErrorLogDetail = communicationDataPreparationWrapper
				.prepareErrorLogData(errorMessages, communicationRequest);
		
		communicationErrorLoggerBusinessObject.createCommunicationProcessErrorLoggerDetailInSameTransaction(communicationErrorLogDetail);
	}

	public void setCommunicationErrorLoggerBusinessObject(
			ICommunicationErrorLoggerBusinessObject communicationErrorLoggerBusinessObject) {
		this.communicationErrorLoggerBusinessObject = communicationErrorLoggerBusinessObject;
	}

	public void setCommunicationEventLoggerService(
			ICommunicationEventLoggerService communicationEventLoggerService) {
		this.communicationEventLoggerService = communicationEventLoggerService;
	}

	public void setCommunicationDataPreparationWrapper(
			CommunicationDataPreparationWrapper communicationDataPreparationWrapper) {
		this.communicationDataPreparationWrapper = communicationDataPreparationWrapper;
	}

	public void setCommunicationCommonService(
			ICommunicationCommonService communicationCommonService) {
		this.communicationCommonService = communicationCommonService;
	}
	
	
}
