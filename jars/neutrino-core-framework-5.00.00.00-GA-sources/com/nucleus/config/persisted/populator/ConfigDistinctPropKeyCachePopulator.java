package com.nucleus.config.persisted.populator;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.service.ConfigurationServiceImpl;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("configDistinctPropKeyCachePopulator")
public class ConfigDistinctPropKeyCachePopulator extends FWCachePopulator {

	@Inject
	@Named("configurationService")
	private ConfigurationServiceImpl configurationService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : ConfigDistinctPropKeyCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		String keyString = (String) key;
		if (keyString.equals(FWCacheConstants.CONFIGURATION_DISTINCT_PROPERTKEY)) {
			return new ArrayList<>(configurationService.getDistinctPropertyKeyForCache());
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		put(FWCacheConstants.CONFIGURATION_DISTINCT_PROPERTKEY,
				new ArrayList<>(configurationService.getDistinctPropertyKeyForCache()));
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.UPDATE) && ValidatorUtils.notNull(object)) {
			put(FWCacheConstants.CONFIGURATION_DISTINCT_PROPERTKEY, object);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.CONFIGURATION_DISTINCT_PROPERTKEY;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.CONFIGURATION_CACHE_GROUP;
	}

}
