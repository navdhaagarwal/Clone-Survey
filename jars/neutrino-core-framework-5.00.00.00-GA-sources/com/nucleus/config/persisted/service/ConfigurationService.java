/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.config.persisted.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.enity.ConfigurationGroup;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.entity.EntityId;
import com.nucleus.service.BaseService;

/**
 * The Interface ConfigurationService. This would provide methods to retrieve Configuration record(s) already
 * existing in the database. This interface also provides methods to update a non-SystemEntity configuration
 * as well as populate configuration for a new entity. 
 *
 * @author Nucleus Software Exports Limited
 */
public interface ConfigurationService extends BaseService {

    /**
     * Gets all the configuration records for an entity. 
     *
     * @param associatedEntity the associated entity
     * @return the configuration for
     */
    public Map<String, ConfigurationVO> getFinalConfigurationForEntity(EntityId associatedEntity);

    /**
     * Update configuration of an entity using the value object.
     *
     * @param configuredEntity the entity id
     * @param configurationVO the list of configuration vo
     */
    public void syncConfiguration(EntityId configuredEntity, Collection<ConfigurationVO> configurationVOList);

    /**
     * Populate configuration for new entity using the configuration(s) of the source entity.
     *
     * @param sourceEntityId the source entity id
     * @param newEntityId the new entity id
     */
    public void populateConfigurationForNewEntity(EntityId sourceEntityId, EntityId newEntityId);

    /**
     * Gets the configuration for an entity and a property key.
     *
     * @param associatedEntity the associated entity
     * @param propertyKey the key
     * @return the configuration for
     */
    public ConfigurationVO getConfigurationPropertyFor(EntityId associatedEntity, String propertyKey);

    /**
     * Gets only user modifiable  configuration records for an entity. 
     * @param targetEntity
     * @return
     */
    public Map<String, ConfigurationVO> getFinalUserModifiableConfigurationForEntity(EntityId targetEntity);

    public void clearEntireCache();

    // for generic data store
    public <T> T loadConfigurationForNonEntity(Class<T> clazz, String id);

    public <T> T loadConfigurationForNonEntity(Class<T> clazz, String id, String parentUri);

    public <T> void saveOrUpdateConfigurationForNonEntity(T configPojo, String id);

    public <T> void saveOrUpdateConfigurationForNonEntity(T configPojo, String id, String parentUri);

    public Object getSinglePropertyValueForNonEntity(Class<?> clazz, String id, String propertyKey);

    public Object getSinglePropertyValueForNonEntity(Class<?> clazz, String id, String propertyKey, String parentUri);

    public <T> boolean configurationExistsForNonEntity(Class<T> clazz, String id);
    public void saveConfiguration(Configuration configuration);
    public String getPropertyValueByPropertyKey(String propertyKey,String queryName);
    public Configuration getPropertyObjectByPropertyKey();

	public ConfigurationGroup getConfigurationGroupFor(EntityId entityId);	
	public ConfigurationGroup getConfigurationGroupFor(EntityId associatedEntity, boolean fromCache);
	
	Locale getSystemLocale();

	public Boolean checkLoanManagementSystem();
	
	public Boolean isWaiveDeferChoiceRequired();
	
	public Boolean isHijriDueDayRequired();
	
	
	public Boolean iscreditCardDetailsIsRegional();
	
	public BigDecimal getMinMonthlyPaymentPercent();
	
	public void buildConfigPropKeyByEntityUriCache();

	void updateConfigurationCache(Map<String, Object> dataMap);

	
	public ConfigurationGroup getConfigurationGroupFromId(Long configId) ;
	public Boolean validateMobileNumber(String ISDCode, String mobileNumber);

	public List<String> getDistinctModifiablePropertyKeyForCache();

	public List<String> getDistinctPropertyKeyForCache();

	public List<ConfigurationGroup> getAllConfigurationGroups();

	public void buildConfigVOPropKeyByEntityUriCache();
	

}
