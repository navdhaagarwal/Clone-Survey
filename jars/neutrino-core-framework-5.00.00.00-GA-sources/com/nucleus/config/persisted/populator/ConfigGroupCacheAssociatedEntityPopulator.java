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

@Named("configGroupCacheAssociatedEntityPopulator")
public class ConfigGroupCacheAssociatedEntityPopulator extends FWCachePopulator {

	@Inject
	@Named("configurationService")
	private ConfigurationServiceImpl configurationService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : ConfigGroupCacheAssociatedEntityPopulator");
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.CONFIGURATION_GROUP_CACHE_ASSOCIATED_ENTITY;
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		String associatedEntityUri = (String) key;
		ConfigurationGroup configGroup = configurationService.getConfiguratioGroupForFromDB(associatedEntityUri);
		if (configGroup != null) {
			return configGroup.getId();
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		List<ConfigurationGroup> listOfConfigurationGroup = configurationService.getAllConfigurationGroups();
		if (listOfConfigurationGroup != null) {
			for (ConfigurationGroup configurationgroup : listOfConfigurationGroup) {
				put(configurationgroup.getAssociatedEntityUri(), configurationgroup.getId());
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
	public String getCacheGroupName() {
		return FWCacheConstants.CONFIGURATION_CACHE_GROUP;
	}

}
