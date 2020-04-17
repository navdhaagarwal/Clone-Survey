package com.nucleus.finnone.pro.communication.cache.service;

import java.util.Map;

import javax.inject.Named;

import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.AddOnDataProviderForCommunicationGeneration;
import com.nucleus.logging.BaseLoggers;

@Named("communicationGenerationDataProviderFW")
public class CommunicationGenerationDataProviderFW implements AddOnDataProviderForCommunicationGeneration{

	

	@Override
	public void provideDataForCommunicationGeneration(Map<String, Object> contextMap,
			Map<String, Object> localCacheMap) {
		BaseLoggers.exceptionLogger.error("FW DATA PROVIDER ");
		contextMap.put(CommunicationGeneratorConstants.SMS_PRIMARY_PHONE_NUMBERS,"MOB_NUMBER");
		contextMap.put(CommunicationGeneratorConstants.TO_EMAIL_ADDRESSES,"contectus@nucleussoftware.com");
		
	}

}
