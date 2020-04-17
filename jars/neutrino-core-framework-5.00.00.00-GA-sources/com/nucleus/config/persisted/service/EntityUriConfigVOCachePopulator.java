package com.nucleus.config.persisted.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.common.redis.CacheManagerRedisImpl;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("entityUriConfigVOCachePopulator")
public class EntityUriConfigVOCachePopulator extends FWCachePopulator {

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : EntityUriConfigVOCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		return null;
	}

	@Override
	public void build(Long tenantId) {
		configurationService.buildConfigVOPropKeyByEntityUriCache();
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.DELETE) && ValidatorUtils.notNull(object) && containsKey(object)) {
			remove(object);
		} else if (action.equals(Action.UPDATE) && ValidatorUtils.notNull(object)) {
			putAll(object);
		}

	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.ENTITYURI_PROPKEY_CONFIGVO_MAP;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.CONFIGURATION_CACHE_GROUP;
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
