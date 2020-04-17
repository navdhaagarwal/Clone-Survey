package com.nucleus.finnone.pro.communicationgenerator.populator;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.service.ConfigurationServiceImpl;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.logging.BaseLoggers;

@Named("commnRetryAttemptConfigCachePopulator")
public class CommnRetryAttemptConfigCachePopulator extends FWCachePopulator {

	@Inject
	@Named("communicationCacheService")
	ICommunicationCacheService communicationCacheService;

	@Inject
	@Named("configurationService")
	private ConfigurationServiceImpl configurationService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : CommnRetryAttemptConfigCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		String keyString = (String) key;

		if (keyString.equals(CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY)) {
			return Integer.parseInt(configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
					CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY).getPropertyValue());
		} else if (keyString.equals(CommunicationGeneratorConstants.LETTER_RETRY_ATTEMPT_CONFIG_KEY)) {
			return Integer
					.parseInt(configurationService
							.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
									CommunicationGeneratorConstants.LETTER_RETRY_ATTEMPT_CONFIG_KEY)
							.getPropertyValue());
		} else if (keyString.equals(CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY)) {
			return Integer.parseInt(configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
					CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY).getPropertyValue());
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		buildCommRetryConfigurationsCache();
	}

	private void buildCommRetryConfigurationsCache() {
		Map<String, Integer> retryConfigMap = new HashMap<>();
		retryConfigMap
				.put(CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY,
						Integer.parseInt(configurationService
								.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
										CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY)
								.getPropertyValue()));
		retryConfigMap
				.put(CommunicationGeneratorConstants.LETTER_RETRY_ATTEMPT_CONFIG_KEY,
						Integer.parseInt(configurationService
								.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
										CommunicationGeneratorConstants.LETTER_RETRY_ATTEMPT_CONFIG_KEY)
								.getPropertyValue()));
		retryConfigMap
				.put(CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY,
						Integer.parseInt(configurationService
								.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
										CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY)
								.getPropertyValue()));

		put(ICommunicationCacheService.RETRY_ATTEMPTS, retryConfigMap);
	}

	@Override
	public void update(Action action, Object object) {
		BaseLoggers.flowLogger.debug("Update Called : CommnRetryAttemptConfigCachePopulator");
		throw new SystemException(UPDATE_ERROR_MSG + getNeutrinoCacheName());
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.COMMUNICATION_RETRY_ATTEMPT_CONFIG;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.COMMUNICATION_CACHE_GROUP;
	}

}
