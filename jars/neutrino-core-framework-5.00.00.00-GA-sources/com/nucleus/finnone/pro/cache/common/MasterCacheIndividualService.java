package com.nucleus.finnone.pro.cache.common;

import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;

public interface MasterCacheIndividualService {

	public static final String BUSY = "BUSY";

	public static final String FREE = "FREE";
	
	public Boolean checkIndividualCacheForBuild(ImpactedCacheVO impactedCacheVO, Long tenantId);

	public void updateTimeInMasterCacheIndividual(String cacheIdentifier);

	public void updateMasterCacheIndividualForCompletion(String cacheIdentifier);	
}
