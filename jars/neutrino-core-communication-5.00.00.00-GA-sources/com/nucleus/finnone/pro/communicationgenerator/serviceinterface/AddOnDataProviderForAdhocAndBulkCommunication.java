package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.Map;

public interface AddOnDataProviderForAdhocAndBulkCommunication {

	/**
	 * 
	 * @param contextMap where transactional data can be put to help communication engine to provide data in templates
	 * @param localCacheMap is a local cache map it can be used to store data for subsequent business calls to avoid data fetching
	 */
	
	void provideDataForAdhocAndBulkCommunication(Map<String,Object> contextMap, Map<String,Object> localCacheMap);
}
