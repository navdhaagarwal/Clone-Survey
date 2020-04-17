package com.nucleus.config.persisted.populator;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.enity.ConfigurationGroup;
import com.nucleus.config.persisted.service.ConfigurationServiceImpl;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("configurationGroupIdCachePopulator")
public class ConfigurationGroupIdCachePopulator extends FWCachePopulator {

	@Inject
	@Named("configurationService")
	private ConfigurationServiceImpl configurationService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : ConfigurationGroupIdCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		return configurationService.getConfigurationGroupFromId((Long) key).getId();
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		List<ConfigurationGroup> listOfConfigurationGroup = configurationService.getAllConfigurationGroups();
		if (listOfConfigurationGroup != null) {
			for (ConfigurationGroup configurationgroup : listOfConfigurationGroup) {
				put(configurationgroup.getId(), configurationgroup);
			}
		}
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
		return FWCacheConstants.CONFIGURATION_GROUP_ID;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.CONFIGURATION_CACHE_GROUP;
	}

}
