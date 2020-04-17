package com.nucleus.web.security.cache;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

@Named("apiSecurityKeyCachePopulator")
public class APISecurityKeyCachePopulator extends FWCachePopulator{

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : APISecurityKeyCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		return null;
	}

	@Override
	public void build(Long tenantId) {
		BaseLoggers.flowLogger.debug("Build Called : APISecurityKeyCachePopulator");
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.DELETE) && !StringUtils.isEmpty((String) object) && containsKey(object)) {
			remove(object);
		} else if (action.equals(Action.INSERT)) {
			putAll(object);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.API_SECURITY_KEY_CACHE;
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
