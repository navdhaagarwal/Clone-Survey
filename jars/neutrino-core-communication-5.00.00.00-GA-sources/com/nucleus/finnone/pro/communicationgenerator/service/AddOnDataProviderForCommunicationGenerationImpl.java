package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.AddOnDataProviderForCommunicationGeneration;

@Service("addOnDataProviderForCommunicationGenerationImpl")
public class AddOnDataProviderForCommunicationGenerationImpl implements AddOnDataProviderForCommunicationGeneration{

	@Override
	public void provideDataForCommunicationGeneration(
			Map<String, Object> contextMap, Map<String, Object> localCacheMap) {
		contextMap.put(CommunicationGeneratorConstants.SMS_PRIMARY_PHONE_NUMBERS, "9899116952");
		contextMap.put(CommunicationGeneratorConstants.TO_EMAIL_ADDRESSES, "anurag.tumloor@nucleussoftware.com");
		
	}

}
