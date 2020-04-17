package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.Map;

import com.nucleus.entity.BaseEntity;

public interface CommunicationBlockStatusVerifier {
	
	boolean isCommunicationGenerationAllowed(String communicationCode, BaseEntity subjectEntity,
			BaseEntity applicablePrimaryEntity,Map<String, Object> localCacheMap);

}
