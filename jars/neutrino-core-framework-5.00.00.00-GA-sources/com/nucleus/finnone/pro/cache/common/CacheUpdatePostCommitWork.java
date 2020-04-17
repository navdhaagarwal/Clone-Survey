package com.nucleus.finnone.pro.cache.common;

import static com.nucleus.finnone.pro.cache.common.CustomCacheEntityListener.IMPACTED_CACHE_VO_TO_IMPACTED_CACHE_MAP;
import static com.nucleus.finnone.pro.cache.common.CustomCacheEntityListener.FIELD_NAME_TO_IMPACTED_CACHE_VO_MAP;
import static com.nucleus.finnone.pro.cache.common.CustomCacheEntityListener.FIELD_NAME_TO_OLD_VALUE_MAP;
import static com.nucleus.finnone.pro.cache.common.CustomCacheEntityListener.FIELD_NAME_TO_VALUE_MAP;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.service.CacheCommonService;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;

@Named("cacheUpdatePostCommitWork")
public class CacheUpdatePostCommitWork implements TransactionPostCommitWork {

	@Inject
	@Named("cacheCommonService")
	private CacheCommonService cacheCommonService;

	@SuppressWarnings("unchecked")
	@Override
	public void work(Object argument) {
		if (argument == null) {
			return;
		}
		Map<String, Object> argumentsMap = (Map<String, Object>) argument;
		Map<String, Object> fieldNameToFieldValueMap = (Map<String, Object>) argumentsMap.get(FIELD_NAME_TO_VALUE_MAP);
		Map<String, Object> fieldNameToOldFieldValueMap = (Map<String, Object>) argumentsMap
				.get(FIELD_NAME_TO_OLD_VALUE_MAP);
		Map<String, Set<ImpactedCacheVO>> fieldNameToImpactedCacheVOMap = (Map<String, Set<ImpactedCacheVO>>) argumentsMap
				.get(FIELD_NAME_TO_IMPACTED_CACHE_VO_MAP);
		Map<ImpactedCacheVO, ImpactedCache> impactedCacheVOToImpactedCacheMap = (Map<ImpactedCacheVO, ImpactedCache>) argumentsMap
				.get(IMPACTED_CACHE_VO_TO_IMPACTED_CACHE_MAP);
		if (fieldNameToFieldValueMap == null || fieldNameToImpactedCacheVOMap == null
				|| impactedCacheVOToImpactedCacheMap == null || fieldNameToFieldValueMap.isEmpty()
				|| fieldNameToImpactedCacheVOMap.isEmpty() || impactedCacheVOToImpactedCacheMap.isEmpty()) {
			return;
		}

		for (Map.Entry<String, Object> entry : fieldNameToFieldValueMap.entrySet()) {
			cacheCommonService.updateImpactedCachesForField(fieldNameToImpactedCacheVOMap.get(entry.getKey()),
					impactedCacheVOToImpactedCacheMap, entry.getValue(), fieldNameToOldFieldValueMap.get(entry.getKey()));
		}
	}

}
