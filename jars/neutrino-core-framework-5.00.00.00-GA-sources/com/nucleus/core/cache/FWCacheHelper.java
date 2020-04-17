package com.nucleus.core.cache;

import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.CACHE_MANAGER;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.cache.common.CacheManager;
import com.nucleus.finnone.pro.cache.common.NeutrinoCache;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.service.CacheCommonService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;

/**
 * 
 * @author gajendra.jatav
 *
 */

@Named("fwCacheHelper")
public class FWCacheHelper {

	private Set<String> fwCacheNames;

	@Inject
	@Named(CACHE_MANAGER)
	private CacheManager cacheManager;
	
	@Inject
	@Named("cacheCommonService")
	private CacheCommonService cacheCommonService;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	public NeutrinoCache getFWCache(String cacheName) {
		return cacheManager.getNeutrinoCacheInstance(FWCacheConstants.FW_CACHE_REGION, cacheName);
	}

	public NeutrinoCachePopulator getFWCachePopulator(String cacheName) {
		return cacheManager.getNeutrinoCachePopulatorInstance(FWCacheConstants.FW_CACHE_REGION, cacheName);
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public Set<String> getFwCacheNames() {
		if (fwCacheNames != null) {
			return fwCacheNames;
		}
		return prepareAllFWCacheNames();
	}

	private Set<String> prepareAllFWCacheNames() {
		Set<String> cacheNames = cacheManager.getCacheNames();
		Set<String> fwCacheNamesLocal = new HashSet<>();
		for (String cacheName : cacheNames) {
			String[] cacheNameArray = ((String) cacheName).split(FWCacheConstants.REGEX_DELIMITER);
			if (cacheNameArray.length == 2 && cacheNameArray[0].equals(FWCacheConstants.FW_CACHE_REGION)) {
				fwCacheNamesLocal.add(cacheNameArray[1]);
			}
		}
		fwCacheNames = fwCacheNamesLocal;
		return fwCacheNames;
	}

	
	public void detachEntity(BaseEntity entity) {
		if (entity == null) {
			return;
		}
		try {
			entityDao.detach(entity);
		} catch (Exception e) {
			String message = "Exception occurred while detaching entity  " + entity;
			BaseLoggers.exceptionLogger.error(message, e);
			throw e;
		}
	}
	
	
	public Map<String, ImpactedCache> createAndGetImpactedCachesFromCacheNames(String... cacheNames) {
		return cacheCommonService.getImpactedCachesFromCacheNames(FWCacheConstants.FW_CACHE_REGION, cacheNames);
	}
	

}
