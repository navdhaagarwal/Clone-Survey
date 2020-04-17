package com.nucleus.finnone.pro.cache.common;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.cache.vo.CacheMasterVO;

public interface MasterCacheService {

	public static final String COMPLETED = "COMPLETED";

	public static final String INITIATED = "INITIATED";

	public static final String NOT_INITIATED = "NOT_INITIATED";
	
	public static final String ERROR = "ERROR";
	
	public Map<String, String> checkCacheForBuild(String cacheGroupName, Long tenantId);

	public int getMaxFailedAttempts();

	public void updateTimeInMasterCache(String cacheGroupName);

	public void updateMasterCacheForCompletion(String cacheGroupName, Map<String, String> individualCacheBuildStatus);

	public void updateMasterCacheForError(String cacheGroupName, Map<String, String> individualCacheBuildStatus);

	public List<CacheMasterVO> getCacheGroupStatusFromMasterCache(String regionName);

	public Boolean markCacheGroupListForRefresh(List<String> cacheGroupNames);

	public List<Object> getIndividualCacheStatusFromCacheGroup(String groupName);

	public String getBuildStatusForCacheGroup(String cacheGroupName);

}
