package com.nucleus.finnone.pro.cache.common;

import java.util.concurrent.Callable;

import org.joda.time.DateTime;

import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;

public interface IndividualCacheBuildTask extends Callable<String>{

	public void populateDataForTask(ImpactedCacheVO impactedCacheVO, DateTime currentTime, Long tenantId, Boolean buildAnyway);
}
