package com.nucleus.finnone.pro.cache.common;

import java.util.Map;
import java.util.concurrent.Callable;

public interface ICacheBuildTask extends Callable<String>{

	public void populateDataForTask(String cacheGroupName, Long tenantId, Map<String, String> individualCacheBuildStatus);
}
