package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.Map;

public interface AddOnDataProviderForTemplateSelection {
	/**
	 * 
	 * @param contextMap where transactional data can be put to help rule engine in selecting right template
	 * @param localCacheMap is a local cache map it can be used to store data for subsequent business calls to avoid data fetching
	 */
	void provideDataForTemplateSelection(Map<String,Object> contextMap, Map<String,Object> localCacheMap);

}
