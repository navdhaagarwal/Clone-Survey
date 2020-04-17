package com.nucleus.web.security;

import javax.inject.Named;

import org.apache.commons.lang3.RandomStringUtils;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("ssoPassPhraseCachePopulator")
public class SsoPassPhraseCachePopulator extends FWCachePopulator {

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : SsoPassPhraseCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		return null;
	}

	@Override
	public void build(Long tenantId) {
		BaseLoggers.flowLogger.debug("Build Called : SsoPassPhraseCachePopulator");
	}

	@Override
	public void update(Action action, Object object) {
		String sessionId = (String) object;
		if (action.equals(Action.DELETE) && ValidatorUtils.notNull(sessionId) && containsKey(sessionId)) {
			remove(sessionId);
		} else if (action.equals(Action.INSERT)) {
			String passPhrase = RandomStringUtils.randomNumeric(8);
			put(sessionId, passPhrase);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.SSO_PHRASE_MAP;
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
