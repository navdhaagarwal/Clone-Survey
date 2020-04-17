/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.config.persisted.service;

import com.nucleus.config.persisted.configconvertors.ConfigConvertorFactory;
import com.nucleus.config.persisted.configconvertors.IConfigConvertor;
import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.enity.ConfigurationGroup;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.misc.util.BeanUtils;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.ConfigurationUpdatedEvent;
import com.nucleus.event.EventTypes;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.service.CacheCommonService;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mobile.MobileNumberValidationBean;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;
import net.bull.javamelody.MonitoredWithSpring;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.CONFIGURATION_DISTINCT_MODIFIABLE_PROPERTYKEY;
import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.CONFIGURATION_DISTINCT_PROPERTKEY;

/**
 * The Class ConfigurationServiceImpl.
 * 
 * @author Nucleus Software Exports Limited
 * @description This service deals with basic configuration related requirements
 */
@Component("configurationService")
@DependsOn("consolidatedDatabaseSeed")
@MonitoredWithSpring(name = "configurationService_IMPL_")
public class ConfigurationServiceImpl extends BaseServiceImpl implements
		ConfigurationService {

	@Inject
	@Named("entityUriConfigCachePopulator")
	private NeutrinoCachePopulator entityUriConfigCachePopulator;
	
	@Inject
	@Named("entityUriConfigVOCachePopulator")
	private NeutrinoCachePopulator entityUriConfigVOCachePopulator;
	
	@Inject
	@Named("cacheCommonService")
	private CacheCommonService cacheCommonService;
	
	@Inject
	@Named("configGroupCacheAssociatedEntityPopulator")
	private NeutrinoCachePopulator configGroupCacheAssociatedEntityPopulator;
	
	
	@Inject
	@Named("configurationGroupIdCachePopulator")
	private NeutrinoCachePopulator configurationGroupIdCachePopulator;
	
	@Inject
	@Named("configDistinctPropKeyCachePopulator")
	private NeutrinoCachePopulator configDistinctPropKeyCachePopulator;
	
	@Inject
	@Named("configDistModifiablePropKeyCachePopulator")
	private NeutrinoCachePopulator configDistModifiablePropKeyCachePopulator;
	
	@Inject
	@Named("customMobileNumberValidationBean")
	private MobileNumberValidationBean mobileNumberValidationBean;

	/** The Constant QUERY_FOR_ENTITY_AND_KEY. */
	private static final String QUERY_FOR_ENTITY_AND_KEY = "Configuration.getConfigurationForAssociatedConfigurationGroupAndKey";

	/** The Constant QUERY_FOR_CONFIGURATION_GROUP. */
	private static final String QUERY_FOR_CONFIGURATION_GROUP = "Configuration.getConfigurationGroupForAssociatedEntity";

	/** The Constant QUERY_FOR_ALL_DISTINCT_PROPERTY. */
	private static final String QUERY_FOR_ALL_DISTINCT_PROPERTY = "Configuration.getConfigurationForDistinctPropertyKey";

	/** The Constant QUERY_FOR_DISTINCT_USER_MODIFIABLE_PROPERTY. */
	private static final String QUERY_FOR_DISTINCT_USER_MODIFIABLE_PROPERTY = "Configuration.getDistinctUserModifiablePropertyKeys";

	private static final String QUERY_FOR_CONFIG_GROUP_FROM_ID = "Configuration.getConfigurationGroupUriForId";

	private static final String MIN_MON_PAY_PERCT = "cas.finance.min.month.pay.prcnt";

	private static final String QUERY_FOR_ALL_CONFIGURATION_GROUP = "Configuration.getAllConfigurationGroup";

	private static final String SYSTEM_ASSOCIATED_ENTITY_URI = "com.nucleus.entity.SystemEntity:1";
	private static final String USER_MODIFIABLE_CONFIGURATIONS = "MODIFIABLE";
	private static final String NON_MODIFIABLE_CONFIGURATIONS = "NON-MODIFIABLE";
	private static final String ALL_DISTINCT_CONFIGURATIONS = "ALL_DISTINCT_CONFIGURATIONS";
	private static final String CONFIGURATION_MAP 			=  "configurationMap";
	private static final String CONFIGURATION_VO_MAP 			=  "configurationVOMap";
	private static final String ENTITY_URI 			=  "entityUri";
	public static final String CONFIGURATION_GROUP_OBJECT = "CONFIGURATION_GROUP_OBJECT";

	/**
	 * Gets the configuration for.
	 * 
	 * @param associatedEntity
	 *            the associated entity
	 * @param key
	 *            the key
	 * @return the configuration for given key and associatedEnitity
	 */
	@SuppressWarnings("unchecked")
	private Configuration getConfigurationFor(EntityId associatedEntity,
			String key) {

		// 
		Configuration configuration = null;
		if (entityUriConfigCachePopulator != null && entityUriConfigCachePopulator.get(associatedEntity.getUri()) != null) {
			Map<String, Map<String, Configuration>> propkeyConfigMap = (Map<String, Map<String, Configuration>>) entityUriConfigCachePopulator.get(associatedEntity.getUri());
			Map<String, Configuration> userModifiableConfigurations = propkeyConfigMap.get(USER_MODIFIABLE_CONFIGURATIONS);
			Map<String, Configuration> nonModifiableConfigurations = propkeyConfigMap.get(NON_MODIFIABLE_CONFIGURATIONS);
			
			if (userModifiableConfigurations != null && null != userModifiableConfigurations.get(key)) {
				configuration = userModifiableConfigurations.get(key);
			}else if (nonModifiableConfigurations != null && null != nonModifiableConfigurations.get(key)) {
				configuration = nonModifiableConfigurations.get(key);
			}
		}
		if(configuration==null){
			NamedQueryExecutor<Configuration> executor = new NamedQueryExecutor<Configuration>(
					QUERY_FOR_ENTITY_AND_KEY)
					.addParameter(ENTITY_URI, associatedEntity.getUri())
					.addParameter("key", key);
			configuration= entityDao.executeQueryForSingleValue(executor);
		}
		return configuration;
	}

	

	/*
	 * Gets all the configuration records for an entity.
	 * 
	 * (non-Javadoc) @see
	 * com.nucleus.config.persisted.service.ConfigurationService
	 * #getFinalConfigurationForEntity(com.nucleus.entity.EntityId)
	 */
	@Override
	public Map<String, ConfigurationVO> getFinalConfigurationForEntity(
			EntityId targetEntity) {
		NeutrinoValidator.notNull(targetEntity,
				"Target entityId could not be null");

		// get configvo map from cache
		Map<String, ConfigurationVO> configurationVOMap = getConfigVOMapFromCache(
				targetEntity, ALL_DISTINCT_CONFIGURATIONS);
		if (configurationVOMap != null && !configurationVOMap.isEmpty()) {
			return configurationVOMap;
		}

		EntityId currentEntity = targetEntity;
		
		List<String> distinctPropertyList = new ArrayList<>(
				getAllDistinctProperties());

		Map<String, ConfigurationVO> finalConfigurationVOMap = new LinkedHashMap<>();

		for (;;) {
			List<Configuration> configList;
			ConfigurationGroup configurationGroup = getConfigurationGroupFor(currentEntity);
			if (ValidatorUtils.notNull(configurationGroup)
					&& ValidatorUtils.hasElements(configurationGroup
							.getConfiguration())) {
				configList = configurationGroup.getConfiguration();
				for (Configuration config : configList) {
					if (distinctPropertyList.contains(config.getPropertyKey())) {
						IConfigConvertor configConvertor = ConfigConvertorFactory
								.getConvertorFromConfiguration(config);
						ConfigurationVO configVO = configConvertor
								.fromConfiguration(config);
						configVO.setAssociatedEntityId(currentEntity);
						finalConfigurationVOMap.put(configVO.getPropertyKey(),
								configVO);
						distinctPropertyList.remove(config.getPropertyKey());
					}
				}
			}
			if (!distinctPropertyList.isEmpty()) {
				ConfigurationGroup currentEntityConfigurationGroup = getConfigurationGroupFor(currentEntity);
				if (currentEntityConfigurationGroup != null
						&& currentEntityConfigurationGroup
								.getParentConfigurationGroup() != null) {
					currentEntity = currentEntityConfigurationGroup
							.getParentConfigurationGroup()
							.getAssociatedEntityId();
					continue;
				}
			}
			break;
		}
		return finalConfigurationVOMap;
	}

	/*
	 * Gets only user modifiable configuration records for an entity.
	 * 
	 * (non-Javadoc) @see
	 * com.nucleus.config.persisted.service.ConfigurationService
	 * #getFinalUserModifiableConfigurationForEntity
	 * (com.nucleus.entity.EntityId)
	 */
	@Override
	/*
	 * @MonitoredWithSpring(name = "CSI_FETCH_USER_CONF_FOR_ENTITY")
	 */public Map<String, ConfigurationVO> getFinalUserModifiableConfigurationForEntity(
			EntityId targetEntity) {
		NeutrinoValidator.notNull(targetEntity,
				"Target entityId could not be null");
		// get configvo map from cache

		Map<String, ConfigurationVO> configurationVOMap = getConfigVOMapFromCache(
				targetEntity, USER_MODIFIABLE_CONFIGURATIONS);
		if (configurationVOMap != null && !configurationVOMap.isEmpty()) {
			return configurationVOMap;
		}

		EntityId currentEntity = targetEntity;

		List<String> distinctPropertyList = new ArrayList<>(
				getAllDistinctMOdifiableProperties());

		Map<String, ConfigurationVO> finalConfigurationVOMap = new LinkedHashMap<>();

		for (;;) {

			List<Configuration> configList;
			ConfigurationGroup configurationGroup = getConfigurationGroupFor(currentEntity);
			if (ValidatorUtils.notNull(configurationGroup)
					&& ValidatorUtils.hasElements(configurationGroup
							.getConfiguration())) {
				configList = configurationGroup.getConfiguration();
				for (Configuration config : configList) {
					if (distinctPropertyList.contains(config.getPropertyKey())) {
						IConfigConvertor configConvertor = ConfigConvertorFactory
								.getConvertorFromConfiguration(config);
						ConfigurationVO configVO = configConvertor
								.fromConfiguration(config);
						configVO.setAssociatedEntityId(currentEntity);
						if (configVO.isUserModifiable()) {
							finalConfigurationVOMap.put(
									configVO.getPropertyKey(), configVO);
						}

						distinctPropertyList.remove(config.getPropertyKey());
					}
				}
			}
			if (!distinctPropertyList.isEmpty()) {
				ConfigurationGroup currentEntityConfigurationGroup = getConfigurationGroupFor(currentEntity);
				if (currentEntityConfigurationGroup != null
						&& currentEntityConfigurationGroup
						.getParentConfigurationGroup() != null) {
					currentEntity = currentEntityConfigurationGroup
							.getParentConfigurationGroup()
							.getAssociatedEntityId();
					continue;
				}
			}
			break;
		}
		return finalConfigurationVOMap;
	}

	/*
	 * Update configuration of an entity using the value object.
	 * 
	 * (non-Javadoc) @see
	 * com.nucleus.config.persisted.service.ConfigurationService
	 * #syncConfiguration(com.nucleus.entity.EntityId, java.util.Collection)
	 */
	@Override
	public void syncConfiguration(EntityId configuredEntity,
			Collection<ConfigurationVO> configurationVO) {
		NeutrinoValidator.notNull(configuredEntity,
				"Configured entityId could not be null");
		NeutrinoValidator.notEmpty(configurationVO,
				"ConfigurationVO list could not be null or empty");
		Boolean isConfigurationChanged = false;
		ConfigurationGroup configGroup = getConfigurationGroupFor(
				configuredEntity, false);
		List<Configuration> configuration = configGroup.getConfiguration();
		if (null == configuration) {
			configuration = new ArrayList<>();
		}
		for (ConfigurationVO configVO : configurationVO) {
			IConfigConvertor configConvertor = ConfigConvertorFactory
					.getConvertorFromVO(configVO);

			boolean isChangedValue = configConvertor
					.isConfigurationChanged(configVO);
			if (!isChangedValue) {
				continue;
			}
			if (!isConfigurationChanged) {
				isConfigurationChanged = true;
			}
			if (configVO.getAssociatedEntityId() != null && configVO.getAssociatedEntityId().equals(configuredEntity)) {
				Configuration configTargetEntity = configConvertor
						.toConfiguration(configVO);
				configTargetEntity.setId(configVO.getId());
				entityDao.update(configTargetEntity);
			} else {
				Configuration configTargetEntity = configConvertor
						.toConfiguration(configVO);
				entityDao.persist(configTargetEntity);

				configuration.add(configTargetEntity);
				configGroup.setConfiguration(configuration);
				entityDao.persist(configGroup);
			}
		}
		if (isConfigurationChanged && ValidatorUtils.notNull(configGroup)) {
			ConfigurationUpdatedEvent event = new ConfigurationUpdatedEvent();
			event.setEventType(String
					.valueOf(EventTypes.CONFIGURATION_UPDATED_EVENT));
			event.setOwnerEntityId(configGroup.getEntityId());
			eventBus.fireEvent(event);
		}

	}

	/*
	 * Populate configuration for new entity using the configuration(s) of the
	 * source entity. - copy new configuration group for newEntityId by making
	 * sourceEntityId parent.
	 * 
	 * (non-Javadoc) @see
	 * com.nucleus.config.persisted.service.ConfigurationService
	 * #populateConfigurationForNewEntity(com.nucleus.entity.EntityId,
	 * com.nucleus.entity.EntityId)
	 */
	@Override
	/* @MonitoredWithSpring(name = "CSI_FETCH_CONF_FOR_ENTITY") */
	public void populateConfigurationForNewEntity(EntityId sourceEntityId,
			EntityId newEntityId) {

		populateAndReturnConfigurationForNewEntity(sourceEntityId, newEntityId);
	}

	/**
	 * Populate and return configuration for new entity.
	 * 
	 * @param sourceEntityId
	 *            the source entity id
	 * @param newEntityId
	 *            the new entity id
	 * @return the configuration group
	 */
	private ConfigurationGroup populateAndReturnConfigurationForNewEntity(
			EntityId sourceEntityId, EntityId newEntityId) {
		NeutrinoValidator.notNull(sourceEntityId,
				"Source entityId could not be null");
		NeutrinoValidator
				.notNull(newEntityId, "New entityId could not be null");
		ConfigurationGroup cg = new ConfigurationGroup();

		cg.setAssociatedEntityId(newEntityId);
		ConfigurationGroup parentConfigGroup = getConfigurationGroupFor(sourceEntityId);
		if (parentConfigGroup == null) {
			throw new SystemException(
					"Configuration group not found for entity: "
							+ sourceEntityId.getUri());
		}
		cg.setParentConfigurationGroup(parentConfigGroup);
		if (entityDao.entityExists(newEntityId)) {
			entityDao.persist(cg);
			ConfigurationUpdatedEvent event = new ConfigurationUpdatedEvent();
			event.setEventType(String
					.valueOf(EventTypes.CONFIGURATION_UPDATED_EVENT));
			event.setOwnerEntityId(cg.getEntityId());
			eventBus.fireEvent(event);
		}
		return cg;
	}

	/*
	 * Gets the configuration for an entity and a property key.
	 * 
	 * (non-Javadoc) @see
	 * com.nucleus.config.persisted.service.ConfigurationService
	 * #getConfigurationPropertyFor(com.nucleus.entity.EntityId,
	 * java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ConfigurationVO getConfigurationPropertyFor(
			EntityId associatedEntity, String key) {
		ConfigurationVO configVO = null;
		if (entityUriConfigVOCachePopulator != null
				&& entityUriConfigVOCachePopulator.get(associatedEntity.getUri()) != null) {
			Map<String, Map<String, ConfigurationVO>> propkeyConfigvoMap = (Map<String, Map<String, ConfigurationVO>>) entityUriConfigVOCachePopulator
					.get(associatedEntity.getUri());

			Map<String, ConfigurationVO> userModifiableConfigurationVO = propkeyConfigvoMap
					.get(USER_MODIFIABLE_CONFIGURATIONS);
			Map<String, ConfigurationVO> nonModifiableConfigurationVO = propkeyConfigvoMap
					.get(NON_MODIFIABLE_CONFIGURATIONS);

			if (userModifiableConfigurationVO != null && userModifiableConfigurationVO.get(key) != null) {
				configVO = userModifiableConfigurationVO.get(key);
			} else if (nonModifiableConfigurationVO != null && nonModifiableConfigurationVO.get(key) != null) {
				configVO = nonModifiableConfigurationVO.get(key);
			}
		}

		// else fallback
		if (configVO == null) {
			NeutrinoValidator.notNull(associatedEntity, "Associated entityId could not be null");
			NeutrinoValidator.notNull(key, "Property key could not be null");
			Configuration config = getConfigurationFor(associatedEntity, key);
			ConfigurationGroup configGroup = null;
			if (config == null) {
				configGroup = getConfigurationGroupFor(associatedEntity);

				for (;;) {
					ConfigurationGroup cg = configGroup.getParentConfigurationGroup();
					if (cg == null || getConfigurationFor(cg.getAssociatedEntityId(), key) != null) {
						break;
					}
					configGroup = configGroup.getParentConfigurationGroup();
				}
				return null;
			}
			IConfigConvertor configConvertor = ConfigConvertorFactory.getConvertorFromConfiguration(config);
			configVO = configConvertor.fromConfiguration(config);
			if (configGroup != null) {
				configVO.setAssociatedEntityId(configGroup.getParentConfigurationGroup().getAssociatedEntityId());
			} else {
				configVO.setAssociatedEntityId(associatedEntity);
			}
		}

		return configVO;
	}

	/**
	 * Gets the configuration group for.
	 * 
	 * @param associatedEntity
	 *            the associated entity
	 * @return the configuration group for
	 */
	@Override
	public ConfigurationGroup getConfigurationGroupFor(EntityId associatedEntity) {
		ConfigurationGroup configurationGroup = getConfigurationGroupFor(associatedEntity.getUri());
		if (configurationGroup == null) {
			configurationGroup = populateAndReturnConfigurationForNewEntity(SystemEntity.getSystemEntityId(),
					associatedEntity);
		}
		return configurationGroup;
	}

	@Override
	public ConfigurationGroup getConfigurationGroupFor(
			EntityId associatedEntity, boolean fromCache) {

		if (fromCache) {
			return getConfigurationGroupFor(associatedEntity);
		}
		ConfigurationGroup configurationGroup = getConfiguratioGroupForFromDB(associatedEntity.getUri());
		
		if (configurationGroup == null) {
            ConfigurationGroup configurationGroupFromCache = getConfigurationGroupFor(associatedEntity.getUri());
            if(configurationGroupFromCache == null){
                return populateAndReturnConfigurationForNewEntity(
			SystemEntity.getSystemEntityId(), associatedEntity);
            }
            return configurationGroupFromCache;  		
		}
		return configurationGroup;
	}

	public ConfigurationGroup getConfigurationGroupFor(
			String associatedEntityUri) {

		if (ValidatorUtils.notNull(configGroupCacheAssociatedEntityPopulator)
				&& ValidatorUtils.notNull(configGroupCacheAssociatedEntityPopulator.get(associatedEntityUri))) {
			Long configurationGroupId = (Long) configGroupCacheAssociatedEntityPopulator.get(associatedEntityUri);
			return entityDao.find(ConfigurationGroup.class, configurationGroupId);
		}
		return getConfiguratioGroupForFromDB(associatedEntityUri);
	}
	
	public ConfigurationGroup getConfiguratioGroupForFromDB(String associatedEntityUri) {
		NamedQueryExecutor<ConfigurationGroup> executor = new NamedQueryExecutor<ConfigurationGroup>(
				QUERY_FOR_CONFIGURATION_GROUP).addParameter(ENTITY_URI, associatedEntityUri);

		return entityDao.executeQueryForSingleValue(executor);
	}

	@Override
	public void clearEntireCache() {
		entityDao.clearEntireCache();
	}


	// ===================================================================================================
	// for generic configuration store and update--> non entities
	// ===================================================================================================
	@Override
	public <T> boolean configurationExistsForNonEntity(Class<T> clazz, String id) {
		return ValidatorUtils.notNull(getConfigurationGroupFor(getNonEntityUri(clazz, id))) ? true : false;
	}

	@Override
	public <T> T loadConfigurationForNonEntity(Class<T> clazz, String id) {
		return loadConfigurationForNonEntity(clazz, id, null);

	}

	@Override
	public <T> T loadConfigurationForNonEntity(Class<T> clazz, String id,
			String parentUri) {

		Map<String, String> properties = getFinalPropertyValueMap(
				getNonEntityUri(clazz, id), parentUri);
		T configPojo = BeanUtils.instantiate(clazz);
		BeanUtils.populate(properties, configPojo);
		return configPojo;

	}

	@Override
	public <T> void saveOrUpdateConfigurationForNonEntity(T configPojo,
			String id) {

		saveOrUpdateConfigurationForNonEntity(configPojo, id, null);

	}

	@Override
	public <T> void saveOrUpdateConfigurationForNonEntity(T configPojo,
			String id, String parentUri) {

		Map<String, String> allProperties = BeanUtils.describe(configPojo);
		saveOrUpdatePropertyValues(getNonEntityUri(configPojo.getClass(), id),
				allProperties, parentUri);
	}

	@Override
	public Object getSinglePropertyValueForNonEntity(Class<?> clazz, String id,
			String propertyKey) {

		String stringVal = getSinglePropertyValue(getNonEntityUri(clazz, id),
				propertyKey, null);

		return BeanUtils.fromString(stringVal,
				BeanUtils.findPropertyType(propertyKey, clazz));

	}

	@Override
	public Object getSinglePropertyValueForNonEntity(Class<?> clazz, String id,
			String propertyKey, String parentUri) {

		String stringVal = getSinglePropertyValue(getNonEntityUri(clazz, id),
				propertyKey, parentUri);
		return BeanUtils.fromString(stringVal,
				BeanUtils.findPropertyType(propertyKey, clazz));

	}

	private String getSinglePropertyValue(String associatedEntityUri,
			String propertyKey, String parentAssociatedEntityUri) {

		if (StringUtils.isNoneBlank(associatedEntityUri)
				&& StringUtils.isNoneBlank(propertyKey)) {
			ConfigurationGroup configurationGroup = getConfigurationGroupFor(associatedEntityUri);
			if (configurationGroup == null && parentAssociatedEntityUri != null) {
				configurationGroup = getConfigurationGroupFor(parentAssociatedEntityUri);
			}
			while (configurationGroup != null) {
				if (configurationGroup.getConfiguration() != null) {
					for (Configuration configu : configurationGroup
							.getConfiguration()) {
						if (propertyKey.equalsIgnoreCase(configu
								.getPropertyKey())) {
							return configu.getPropertyValue();
						}
					}
				}
				configurationGroup = configurationGroup
						.getParentConfigurationGroup();
			}
		}
		return null;

	}

	private Map<String, String> getFinalPropertyValueMap(
			String associatedEntityUri, String parentAssociatedEntityUri) {

		Map<String, String> finalMap = new HashMap<>();
		if (StringUtils.isNoneBlank(associatedEntityUri)) {
			ConfigurationGroup configurationGroup = getConfigurationGroupFor(associatedEntityUri);
			if (configurationGroup == null && parentAssociatedEntityUri != null) {
				configurationGroup = getConfigurationGroupFor(parentAssociatedEntityUri);
			}

			while (configurationGroup != null) {
				if (configurationGroup.getConfiguration() != null) {
					for (Configuration configu : configurationGroup
							.getConfiguration()) {
						if (!finalMap.containsKey(configu.getPropertyKey())) {
							finalMap.put(configu.getPropertyKey(),
									configu.getPropertyValue());
						}
					}
				}
				configurationGroup = configurationGroup
						.getParentConfigurationGroup();
			}
		}
		return finalMap;
	}

	private void saveOrUpdatePropertyValues(String associatedEntityUri,
			Map<String, String> allProperties, String parentEntityUri) {

		// check if configuration exists for entity if not create one
		ConfigurationGroup configurationGroup = getConfigurationGroupFor(EntityId.fromUri(associatedEntityUri), false);
		ConfigurationGroup cg = null;
		// create and persist a new group
		if (configurationGroup == null) {
			cg = new ConfigurationGroup();
			cg.setAssociatedEntityUri(associatedEntityUri);

			// parent is optional
			if (parentEntityUri != null) {
				ConfigurationGroup parentConfigGroup = getConfigurationGroupForNonEntity(parentEntityUri);
				if (parentConfigGroup == null) {
					throw new SystemException(
							"Configuration group not found for entity: "
									+ parentEntityUri);
				}
				preserveNewOrChangedProperties(parentConfigGroup, allProperties);
				cg.setParentConfigurationGroup(parentConfigGroup);
			}
			cg.setConfiguration(new ArrayList<Configuration>());
			for (Map.Entry<String, String> entry : allProperties.entrySet()) {
				Configuration configuration = new Configuration();
				configuration.setPropertyKey(entry.getKey());
				configuration.setPropertyValue(entry.getValue());
				cg.getConfiguration().add(configuration);
			}
			entityDao.persist(cg);
		} else {
			// update existing group
			// update value of existing property(here we don't check explicitly
			// whether property value was changed or not)
			for (Configuration configu : configurationGroup.getConfiguration()) {
				if (allProperties.containsKey(configu.getPropertyKey())) {
					configu.setPropertyValue(allProperties.get(configu
							.getPropertyKey()));
					allProperties.remove(configu.getPropertyKey());
				}
			}

			// now check for properties which are in parents and are not
			// changed.
			// if such a property is changed it becomes a new property for
			// current(leaf node) config group.
			preserveNewOrChangedProperties(
					configurationGroup.getParentConfigurationGroup(),
					allProperties);
			// create new for remaining in allProperties
			for (Map.Entry<String, String> entry : allProperties.entrySet()) {
				Configuration configuration = new Configuration();
				configuration.setPropertyKey(entry.getKey());
				configuration.setPropertyValue(entry.getValue());
				configurationGroup.getConfiguration().add(configuration);
			}
			entityDao.update(configurationGroup);
		}
		ConfigurationUpdatedEvent event = new ConfigurationUpdatedEvent();
		event.setEventType(String
				.valueOf(EventTypes.CONFIGURATION_UPDATED_EVENT));
		if (cg != null){
			event.setOwnerEntityId(cg.getEntityId());
		}else{
			event.setOwnerEntityId(configurationGroup.getEntityId());
		}

		eventBus.fireEvent(event);

	}

	// properties which are not changed and are not new should not be
	// persisted/updated
	private void preserveNewOrChangedProperties(ConfigurationGroup parentConfigGroup, Map<String, String> allProps) {

		while (parentConfigGroup != null) {
			if (parentConfigGroup.getConfiguration() != null) {
				for (Configuration configu : parentConfigGroup.getConfiguration()) {
					if (allProps.containsKey(configu.getPropertyKey()) && allProps.get(configu.getPropertyKey()) != null
							&& allProps.get(configu.getPropertyKey()).equalsIgnoreCase(configu.getPropertyValue())) {
						allProps.remove(configu.getPropertyKey());
					}
				}
			}
			parentConfigGroup = parentConfigGroup.getParentConfigurationGroup();
		}
	}

	private ConfigurationGroup getConfigurationGroupForNonEntity(
			String entityUri) {
		return getConfigurationGroupFor(entityUri);
	}

	private String getNonEntityUri(Class<?> class1, String id) {
		return class1.getName().concat(":").concat(id);

	}

	public void saveConfiguration(Configuration configuration) {
		entityDao.saveOrUpdate(configuration);		
	}

	// "Configuration.getPropertyValueFromPropertyKey"
	@Override
	public String getPropertyValueByPropertyKey(String propertyKey,
			String queryName) {

		// get the property value from cache for system

		NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(
				queryName).addParameter("propertyKey", propertyKey).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		return entityDao.executeQueryForSingleValue(executor);

	}

	@Override
	public Configuration getPropertyObjectByPropertyKey() {
		// get the property value from cache for system

		NamedQueryExecutor<Configuration> executor = new NamedQueryExecutor<Configuration>(
				"Configuration.getObjectFromPropertyKey").addParameter(
				"propertyKey", "notifyEvents");
		return entityDao.executeQueryForSingleValue(executor);

	}

	public Locale getSystemLocale() {
		Locale locale = null;
		ConfigurationVO preferences = getConfigurationPropertyFor(
				SystemEntity.getSystemEntityId(), "config.user.locale");
		String[] localeString = preferences.getText().split("_");
		locale = new Locale(localeString[0], localeString[1]);
	
		return locale;
	}

	public Boolean checkLoanManagementSystem() {
		ConfigurationVO configVO = getConfigurationPropertyFor(
				EntityId.fromUri(SYSTEM_ASSOCIATED_ENTITY_URI),
				"config.current.loan.mgmt.sys");
		return "NEOLMS".equalsIgnoreCase(configVO.getPropertyValue());
	}

	public Boolean isWaiveDeferChoiceRequired() {
		ConfigurationVO configVO = getConfigurationPropertyFor(
				EntityId.fromUri(SYSTEM_ASSOCIATED_ENTITY_URI),
				"config.waive.defer.choice.required");
		return "true".equalsIgnoreCase(configVO.getPropertyValue());
	}

	public Boolean isHijriDueDayRequired() {
		ConfigurationVO configVO = getConfigurationPropertyFor(
				EntityId.fromUri(SYSTEM_ASSOCIATED_ENTITY_URI),
				"config.hijri.dueday.required");
		return "true".equalsIgnoreCase(configVO.getPropertyValue());
	}

	public Boolean iscreditCardDetailsIsRegional() {
		ConfigurationVO configVO = getConfigurationPropertyFor(
				EntityId.fromUri(SYSTEM_ASSOCIATED_ENTITY_URI),
				"config.regional.creditCardDetailsIsRegional");
		return "true".equalsIgnoreCase(configVO.getPropertyValue());
	}

	public BigDecimal getMinMonthlyPaymentPercent() {
		ConfigurationVO configVO = getConfigurationPropertyFor(
				EntityId.fromUri(SYSTEM_ASSOCIATED_ENTITY_URI),
				MIN_MON_PAY_PERCT);
		if (configVO != null && configVO.getPropertyValue() != null) {
			return new BigDecimal(configVO.getPropertyValue());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void buildConfigPropKeyByEntityUriCache() {

		BaseLoggers.flowLogger.debug(" Start ConfigurationServiceImpl:: buildConfigPropKeyByEntityUriCache ");

		Map<String, ConfigurationVO> systemUserModifiableConfigurationVO = getFinalUserModifiableConfigurationForEntity(
				SystemEntity.getSystemEntityId());

		List<ConfigurationGroup> listOfConfigurationGroup = getAllConfigurationGroups();

		if (listOfConfigurationGroup != null) {
			Map<String, Map<String, Map<String, Configuration>>> configurationPropKeyByEntityUriMap = new HashMap<>();
			for (ConfigurationGroup configurationgroup : listOfConfigurationGroup) {
				Map<String, Object> preparedMap = prepareConfigurationMap(configurationgroup,
						new HashMap<String, ConfigurationVO>(systemUserModifiableConfigurationVO));
				if (ValidatorUtils.notNull(preparedMap.get(CONFIGURATION_MAP))) {
					configurationPropKeyByEntityUriMap.put(configurationgroup.getAssociatedEntityUri(),
							(Map<String, Map<String, Configuration>>) preparedMap.get(CONFIGURATION_MAP));
				}
			}
			entityUriConfigCachePopulator.update(Action.UPDATE, configurationPropKeyByEntityUriMap);
		}

		BaseLoggers.flowLogger.debug(" End ConfigurationServiceImpl:: buildConfigPropKeyByEntityUriCache ");

	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void buildConfigVOPropKeyByEntityUriCache() {

		BaseLoggers.flowLogger.debug(" Start ConfigurationServiceImpl:: buildConfigVOPropKeyByEntityUriCache ");

		Map<String, ConfigurationVO> systemUserModifiableConfigurationVO = getFinalUserModifiableConfigurationForEntity(
				SystemEntity.getSystemEntityId());
		List<ConfigurationGroup> listOfConfigurationGroup = getAllConfigurationGroups();

		if (listOfConfigurationGroup != null) {
			Map<String, Map<String, Map<String, ConfigurationVO>>> configurationVOPropKeyByEntityUriMap = new HashMap<>();
			for (ConfigurationGroup configurationgroup : listOfConfigurationGroup) {
				Map<String, Object> preparedMap = prepareConfigurationMap(configurationgroup,
						new HashMap<String, ConfigurationVO>(systemUserModifiableConfigurationVO));
				if(ValidatorUtils.notNull(preparedMap.get(CONFIGURATION_VO_MAP)))
				{
					configurationVOPropKeyByEntityUriMap.put(configurationgroup.getAssociatedEntityUri(),
							(Map<String, Map<String, ConfigurationVO>>) preparedMap.get(CONFIGURATION_VO_MAP));
				}
			}

			entityUriConfigVOCachePopulator.update(Action.UPDATE, configurationVOPropKeyByEntityUriMap);

		}

		BaseLoggers.flowLogger.debug(" End ConfigurationServiceImpl:: buildConfigVOPropKeyByEntityUriCache ");

	}

	private Map<String, Object> prepareConfigurationMap(
			ConfigurationGroup configurationgroup,
			Map<String, ConfigurationVO> systemModifiableVO) {

		List<Configuration> tempConfigurationList = configurationgroup
				.getConfiguration();
		Map<String, Object> preparedMap = new HashMap<>();

		try {
			Map<String, Configuration> nonModifiableConfigurations = new ConcurrentHashMap<>();
			Map<String, ConfigurationVO> nonModifiableConfigurationVO = new ConcurrentHashMap<>();

			Map<String, Configuration> userModifiableConfigurations = new ConcurrentHashMap<>();
			Map<String, ConfigurationVO> userModifiableConfigurationVO = new ConcurrentHashMap<>();

			for (Configuration configuration : tempConfigurationList) {

				IConfigConvertor configConvertor = ConfigConvertorFactory
						.getConvertorFromConfiguration(configuration);
				ConfigurationVO configVO = configConvertor
						.fromConfiguration(configuration);
				configVO.setAssociatedEntityId(configurationgroup
						.getAssociatedEntityId());
				if (systemModifiableVO.containsKey(configuration
						.getPropertyKey())) {
					systemModifiableVO.remove(configuration.getPropertyKey());
				}

				if (configuration.isUserModifiable()) {
					userModifiableConfigurations.put(
							configuration.getPropertyKey(), configuration);

					userModifiableConfigurationVO.put(
							configuration.getPropertyKey(), configVO);

				} else {
					nonModifiableConfigurations.put(
							configuration.getPropertyKey(), configuration);

					nonModifiableConfigurationVO.put(
							configuration.getPropertyKey(), configVO);

				}
			}

			userModifiableConfigurationVO.putAll(systemModifiableVO);

			Map<String, Map<String, Configuration>> configurationMap = new ConcurrentHashMap<>();
			Map<String, Map<String, ConfigurationVO>> configurationVOMap = new ConcurrentHashMap<>();
			configurationMap.put(USER_MODIFIABLE_CONFIGURATIONS,
					userModifiableConfigurations);
			configurationVOMap.put(USER_MODIFIABLE_CONFIGURATIONS,
					userModifiableConfigurationVO);

			configurationMap.put(NON_MODIFIABLE_CONFIGURATIONS,
					nonModifiableConfigurations);
			configurationVOMap.put(NON_MODIFIABLE_CONFIGURATIONS,
					nonModifiableConfigurationVO);
			preparedMap.put(CONFIGURATION_MAP, configurationMap);

			preparedMap.put(CONFIGURATION_VO_MAP, configurationVOMap);

			preparedMap.put("configurationGroup", configurationgroup);

		} catch (Exception e) {
			BaseLoggers.flowLogger
					.error(" Exception in buildConfigurationCache for configuration group ",
							e);
		}
		return preparedMap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateConfigurationCache(Map<String, Object> dataMap) {
		ConfigurationGroup configurationgroup = (ConfigurationGroup) dataMap.get(CONFIGURATION_GROUP_OBJECT);
		String associatedEntityUri = configurationgroup.getAssociatedEntityUri();
		Map<String, ImpactedCache> impactedCacheMap = (Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP);
		
		try {
			Map<String, ConfigurationVO> userModifiableConfigurationsFromSystem = null;
			if (!SYSTEM_ASSOCIATED_ENTITY_URI.equals(configurationgroup.getAssociatedEntityUri())) {
				userModifiableConfigurationsFromSystem = new HashMap<>(
						getConfigVOMapFromCache(SystemEntity.getSystemEntityId(), USER_MODIFIABLE_CONFIGURATIONS));
			} else {
				userModifiableConfigurationsFromSystem = new ConcurrentHashMap<>();
			}

			
			
			Map<String, Object> preparedMap = prepareConfigurationMap(configurationgroup,
					new ConcurrentHashMap<String, ConfigurationVO>(userModifiableConfigurationsFromSystem));
			
			
			if (ValidatorUtils.notNull(preparedMap.get(CONFIGURATION_MAP))) {
				Map<String, Map<String, Map<String, Configuration>>> configurationPropKeyByEntityUriMap = new HashMap<>();
				configurationPropKeyByEntityUriMap.put(configurationgroup.getAssociatedEntityUri(),
						(Map<String, Map<String, Configuration>>) preparedMap.get(CONFIGURATION_MAP));
				entityUriConfigCachePopulator.update(Action.DELETE, configurationgroup.getAssociatedEntityUri());
				entityUriConfigCachePopulator.update(impactedCacheMap, Action.UPDATE, configurationPropKeyByEntityUriMap);
				
			}
			else {
				entityUriConfigCachePopulator.update(impactedCacheMap, Action.DELETE, configurationgroup.getAssociatedEntityUri());
			}

			
				
			
			
			if (ValidatorUtils.notNull( preparedMap.get(CONFIGURATION_VO_MAP))) {
				Map<String, Map<String, Map<String, ConfigurationVO>>> configurationVOPropKeyByEntityUriMap = new HashMap<>();
				configurationVOPropKeyByEntityUriMap.put(configurationgroup.getAssociatedEntityUri(),
						(Map<String, Map<String, ConfigurationVO>>) preparedMap.get(CONFIGURATION_VO_MAP));
				entityUriConfigVOCachePopulator.update(Action.DELETE, configurationgroup.getAssociatedEntityUri());
				entityUriConfigVOCachePopulator.update(impactedCacheMap, Action.UPDATE, configurationVOPropKeyByEntityUriMap);
				
			}
			else {
				entityUriConfigVOCachePopulator.update(impactedCacheMap, Action.DELETE, configurationgroup.getAssociatedEntityUri());
			}
			
			
			

			Map<String, Long> configurationGroupByEntityUriMap = new HashMap<>();
			configurationGroupByEntityUriMap.put(associatedEntityUri, configurationgroup.getId());
			configGroupCacheAssociatedEntityPopulator.update(Action.DELETE, configurationgroup.getAssociatedEntityUri());
			configGroupCacheAssociatedEntityPopulator.update(impactedCacheMap, Action.UPDATE, configurationGroupByEntityUriMap);
			

			Map<Long, ConfigurationGroup> configurationGroupByIdMap = new HashMap<>();
			configurationGroupByIdMap.put(configurationgroup.getId(), configurationgroup);
			configurationGroupIdCachePopulator.update(Action.DELETE, configurationgroup.getId());
			configurationGroupIdCachePopulator.update(impactedCacheMap, Action.UPDATE, configurationGroupByIdMap);
			
			if (configurationgroup.getAssociatedEntityUri().equals(SYSTEM_ASSOCIATED_ENTITY_URI)) {

				List<String> distictPropertyKey = getDistinctPropertyKeyForCache();
				List<String> distictModifiablePropertyKey = getDistinctModifiablePropertyKeyForCache();

				configDistinctPropKeyCachePopulator.update(impactedCacheMap, Action.UPDATE, distictPropertyKey);
				configDistModifiablePropKeyCachePopulator.update(impactedCacheMap, Action.UPDATE, distictModifiablePropertyKey);

			} else if (!ValidatorUtils.hasNoEntry(impactedCacheMap)) {
				ImpactedCache impactedCache = impactedCacheMap
						.get(new StringBuilder(configDistinctPropKeyCachePopulator.getCacheRegionName())
								.append(FWCacheConstants.KEY_DELIMITER)
								.append(configDistinctPropKeyCachePopulator.getNeutrinoCacheName()).toString());

				removeImpactedCacheEntry(impactedCache);
				impactedCache = impactedCacheMap
						.get(new StringBuilder(configDistModifiablePropKeyCachePopulator.getCacheRegionName())
								.append(FWCacheConstants.KEY_DELIMITER)
								.append(configDistModifiablePropKeyCachePopulator.getNeutrinoCacheName()).toString());
				removeImpactedCacheEntry(impactedCache);
			}
			
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(
					" Exception in ConfigurationServiceImpl updateConfigurationCache for one of Configuration Group ",
					e);
		}
	}
	
	private void removeImpactedCacheEntry(ImpactedCache impactedCache)
	{
		if (ValidatorUtils.notNull(impactedCache)) {
			cacheCommonService.removeImpactedCacheEntryImplicitly(impactedCache);
		}
	}

	@Override
	public List<String> getDistinctPropertyKeyForCache() {
		NamedQueryExecutor<String> configurationCriteria = new NamedQueryExecutor<String>(
				QUERY_FOR_ALL_DISTINCT_PROPERTY).addQueryHint(
				QueryHint.QUERY_HINT_FETCHSIZE, 500);

		return entityDao.executeQuery(configurationCriteria);
	}

	@Override
	public List<String> getDistinctModifiablePropertyKeyForCache() {
		NamedQueryExecutor<String> configurationCriteria = new NamedQueryExecutor<String>(
				QUERY_FOR_DISTINCT_USER_MODIFIABLE_PROPERTY);

		return entityDao.executeQuery(configurationCriteria);
	}

	@Override
	public ConfigurationGroup getConfigurationGroupFromId(Long configid) {
		NamedQueryExecutor<ConfigurationGroup> configurationCriteria = new NamedQueryExecutor<ConfigurationGroup>(
				QUERY_FOR_CONFIG_GROUP_FROM_ID).addParameter(
				"configurationGroupId", configid).addQueryHint(
				QueryHint.QUERY_HINT_FETCHSIZE, 500);

		return entityDao.executeQueryForSingleValue(configurationCriteria);
	}

	@Override
	public List<ConfigurationGroup> getAllConfigurationGroups() {
		BaseLoggers.flowLogger.debug(" Start getAllConfigurationGroups ");

		NamedQueryExecutor<ConfigurationGroup> executor = new NamedQueryExecutor<ConfigurationGroup>(
				QUERY_FOR_ALL_CONFIGURATION_GROUP).addQueryHint(
				QueryHint.QUERY_HINT_FETCHSIZE, 500);

		return entityDao.executeQuery(executor);
	}



	
	

	@SuppressWarnings("unchecked")
	public List<String> getAllDistinctProperties() {
		if (ValidatorUtils.notNull(configDistinctPropKeyCachePopulator)) {
			return (List<String>) configDistinctPropKeyCachePopulator.get(CONFIGURATION_DISTINCT_PROPERTKEY);
		}
		return new ArrayList<>(getDistinctPropertyKeyForCache());
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllDistinctMOdifiableProperties() {
		if (ValidatorUtils.notNull(configDistModifiablePropKeyCachePopulator)) {
			return (List<String>) configDistModifiablePropKeyCachePopulator.get(CONFIGURATION_DISTINCT_MODIFIABLE_PROPERTYKEY);
		}
		return new ArrayList<>(getDistinctModifiablePropertyKeyForCache());
	}

	@SuppressWarnings("unchecked")
	public Map<String, ConfigurationVO> getConfigVOMapFromCache(
			EntityId targetEntity, String propertyType) {
		
		if (entityUriConfigVOCachePopulator != null
				&& entityUriConfigVOCachePopulator.get(targetEntity.getUri()) != null) {
			Map<String, ConfigurationVO> userModifiableConfigurationVOMap = ((Map<String, Map<String, ConfigurationVO>>)entityUriConfigVOCachePopulator
					.get(targetEntity.getUri())).get(
							USER_MODIFIABLE_CONFIGURATIONS);
			Map<String, ConfigurationVO> nonModifiableConfigurationVOMap = ((Map<String, Map<String, ConfigurationVO>>)entityUriConfigVOCachePopulator
					.get(targetEntity.getUri())).get(
							NON_MODIFIABLE_CONFIGURATIONS);
			if (ALL_DISTINCT_CONFIGURATIONS.equals(propertyType)) {

				Map<String, ConfigurationVO> combinedConfigurationVOMap = new ConcurrentHashMap<>();

				combinedConfigurationVOMap
						.putAll(userModifiableConfigurationVOMap != null ? userModifiableConfigurationVOMap
								: new ConcurrentHashMap<String, ConfigurationVO>());
				combinedConfigurationVOMap
						.putAll(nonModifiableConfigurationVOMap != null ? nonModifiableConfigurationVOMap
								: new ConcurrentHashMap<String, ConfigurationVO>());

				return combinedConfigurationVOMap;
			}

			else if (USER_MODIFIABLE_CONFIGURATIONS.equals(propertyType)) {
				return userModifiableConfigurationVOMap;
			}

		}

		return null;
	}
	
    

    @Override
    public Boolean validateMobileNumber(String ISDCode, String mobileNumber) {
		return mobileNumberValidationBean.validateMobileNumber(ISDCode,mobileNumber);
	}

}
