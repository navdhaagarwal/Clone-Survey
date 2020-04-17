package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationScheduler;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationErrorLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.finnone.pro.general.util.sms.SmsVO;

@Named("communicationDataPreparationWrapper")
public class CommunicationDataPreparationWrapper {
		
	@Inject
	@Named("communicationGenerationHelper")
	private CommunicationGenerationHelper communicationGenerationHelper;
	
	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService; 
	
	@Inject
	@Named(value="communicationErrorLoggerService")
	private ICommunicationErrorLoggerService communicationErrorLoggerService;

	
	public List<CommunicationErrorLogDetail> prepareErrorLogData(List<Message> errorMessages,
			CommunicationRequestDetail communicationRequestDetail) {
		List<CommunicationErrorLogDetail> communicationErrorLogDetails= new ArrayList<CommunicationErrorLogDetail>();
		Locale locale = configurationService.getSystemLocale();
		CommunicationErrorLogDetail communicationErrorLogDetail=null;
		for(Message errorMessage:errorMessages){
			communicationErrorLogDetail=new CommunicationErrorLogDetail();
			String messageDescription = communicationGenerationHelper.getMessageDescription(errorMessage, locale);
			communicationErrorLogDetail.setErrorDescription(messageDescription);
			communicationErrorLogDetail.setErrorMessageId(errorMessage.getI18nCode());
			if(errorMessage.getMessageArguments() != null){
				communicationErrorLogDetail.setErrorMessageparameters(errorMessage.getMessageArgumentsString(errorMessage.getMessageArguments()));
			}
			communicationErrorLogDetail.setErrorType(1L);
			communicationErrorLogDetail.setApplicablePrimaryEntityID(communicationRequestDetail.getApplicablePrimaryEntityId());
			communicationErrorLogDetail.setApplicablePrimaryEntityUri(communicationRequestDetail.getApplicablePrimaryEntityURI());
			communicationErrorLogDetail.setSubjectId(communicationRequestDetail.getSubjectId());
			communicationErrorLogDetail.setSubjectUri(communicationRequestDetail.getSubjectURI());
			communicationErrorLogDetail.setSubjectReferenceNumber(communicationRequestDetail.getSubjectReferenceNumber());
			communicationErrorLogDetail.setSubjectReferenceType(communicationRequestDetail.getSubjectReferenceType());
			communicationErrorLogDetail.setCommunicationEventCode(communicationRequestDetail.getCommunicationEventCode());
			communicationErrorLogDetail.setCommunicationCode(communicationRequestDetail.getCommunicationCode());
			communicationErrorLogDetail.setCommunicationTemplateCode(communicationRequestDetail.getCommunicationTemplateCode());
			communicationErrorLogDetail.setSubjectType('L');
			communicationErrorLogDetail.setReferenceDate(communicationRequestDetail.getReferenceDate());
			communicationErrorLogDetails.add(communicationErrorLogDetail);
		}
		return communicationErrorLogDetails;
	}	
	
}
