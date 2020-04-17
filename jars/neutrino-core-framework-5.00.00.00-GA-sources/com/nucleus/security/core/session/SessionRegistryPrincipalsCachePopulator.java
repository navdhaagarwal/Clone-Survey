package com.nucleus.security.core.session;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Named;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

@Named("sessionRegistryPrincipalsCachePopulator")
public class SessionRegistryPrincipalsCachePopulator extends FWCachePopulator {

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : SessionRegistryPrincipalsCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		return null;
	}

	@Override
	public void build(Long tenantId) {
		BaseLoggers.flowLogger.debug("Build Called : SessionRegistryPrincipalsCachePopulator");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.DELETE)) {
			remove(object);
		} else {
			Map<Object, Set<String>> mapForCache = (Map<Object, Set<String>>) object;
			for (Entry<Object, Set<String>> entry : mapForCache.entrySet()) {
				put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.SESSION_REGISTRY_PRINCIPALS_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.MISCELLANEOUS_CACHE_GROUP;
	}
	
	@Override
	protected boolean fallbackRequired() {
		return false;
	}

}
