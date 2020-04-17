package com.nucleus.security.core.session;

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Named;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.common.redis.CacheManagerRedisImpl;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

@Named("sessionRegistrySessionIdsCachePopulator")
public class SessionRegistrySessionIdsCachePopulator extends FWCachePopulator {

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : SessionRegistrySessionIdsCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		return null;
	}

	@Override
	public void build(Long tenantId) {
		BaseLoggers.flowLogger.debug("Build Called : SessionRegistrySessionIdsCachePopulator");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Action action, Object object) {

		if (action.equals(Action.DELETE)) {
			remove((String) object);
		} else {
			Map<Object, Object> mapForCache = (Map<Object, Object>) object;
			for (Entry<Object, Object> entry : mapForCache.entrySet()) {
				put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.SESSION_REGISTRY_SESSION_IDS_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.MISCELLANEOUS_CACHE_GROUP;
	}
	
	@Override
	protected boolean fallbackRequired() {
		return false;
	}
	
	@Override
	protected String getLocalCacheType() {
		return CacheManagerRedisImpl.LOCAL_REDIS;
	}

}
