package com.nucleus.finnone.pro.cache.service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.entity.CacheBuildHistory;
import com.nucleus.finnone.pro.cache.entity.CacheUpdateType;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.vo.CacheMasterVO;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;

public interface CacheCommonService {

	public ImpactedCache createImpactedCacheEntry(ImpactedCacheVO impactedCacheVO);

	public void removeImpactedCacheEntryImplicitly(ImpactedCache impactedCache);

	public <T extends BaseEntity> Object getCurrentFieldValue(String fieldName, Long id, Class<T> entityClass)
			throws IllegalAccessException, InvocationTargetException;

	public void updateImpactedCachesInPostTransaction(Map<String, Object> fieldNameToFieldValueMap,
			Map<String, Object> fieldNameToOldFieldValueMap, Map<String, Set<ImpactedCacheVO>> fieldNameToImpactedCacheVOMap,
			Map<ImpactedCacheVO, ImpactedCache> impactedCacheVOToImpactedCacheMap);

	public void updateImpactedCachesForField(Set<ImpactedCacheVO> impactedCacheVOSet,
			Map<ImpactedCacheVO, ImpactedCache> impactedCacheVOToImpactedCacheMap, Object fieldValue, Object oldFieldValue);

	public void removeImpactedCacheEntries(Set<ImpactedCache> impactedCaches);

	public Map<String, ImpactedCache> getImpactedCachesFromCacheNames(String regionName, String... cacheNames);

	public CacheBuildHistory createCacheBuildHistoryEntry(CacheMasterVO cacheMasterVO);

	public Object fallback(NeutrinoCachePopulator neutrinoCachePopulator, Object key);

	public List<ImpactedCacheVO> getFailedImpactedCacheVOs();

	public void removeImpactedCacheEntry(ImpactedCache impactedCache, CacheUpdateType cacheUpdateType);

	public void buildImpactedCache(ImpactedCacheVO impactedCacheVO, Long tenantId, Boolean buildAnyway) throws Exception;

	public void buildImpactedCache(ImpactedCacheVO impactedCacheVO, Long tenantId, DateTime currentTime, Boolean buildAnyway) throws Exception;

	public void buildImpactedCaches(String[] cacheIdentifierSet);

	public Boolean updateImpactedCacheLastUpdatedTime(String[] cacheIdentifierSet);

	public Boolean checkForFailedImpactedCacheByGroupName(String groupName);

	public void updateImpactedCacheLastUpdatedTime(String groupName);

	public Map<String, String> getBuildStatusForCacheGroups(Set<String> cacheGroupNames);
	
}
