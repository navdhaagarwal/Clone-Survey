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

package com.nucleus.master;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.makerchecker.GridVO;

/**
 * @author Nucleus Software Exports Limited Interface provides generic methods
 *         to access master entities.
 * 
 */
public interface BaseMasterService {

    /**
     * Returns all 'approved' entities for a given entity type
     * 
     * @param entityClass
     * @return list of BaseMasterEntity
     */
    <T extends BaseMasterEntity> List<T> getLastApprovedEntities(Class<T> entityClass);
   

    /**
     * Returns all 'relevant' entities for a given entity type for a given user
     * with available user actions.
     * 
     * @param entityClass
     * @param userUri
     * @return list of BaseMasterEntity
     */
    <T extends BaseMasterEntity> List<T> loadAllEntities(Class<T> entityClass, String userUri);

    /**
     * Returns the master entity by Id.
     * 
     * @param geoEntityclass
     * @param id
     * @return
     */
    <T extends BaseMasterEntity> T getMasterEntityById(Class<T> geoEntityclass, Long id);

    /**
     * Returns the master entity by Id with the actions (for e.g. : approve ,
     * reject, send back, edit ) that a user can take.
     * 
     * @param geoEntityclass
     * @param id
     * @param userUri
     * @return
     */
    <T extends BaseMasterEntity> T getMasterEntityWithActionsById(Class<T> geoEntityclass, Long id, String userUri);

    /**
     * Return the last Approved version of an entity using the entityId of an
     * lastunapproved version.
     * 
     * @param lastUnapprovedEntityID
     * @return
     */
    BaseMasterEntity getLastApprovedEntityByUnapprovedEntityId(EntityId lastUnapprovedEntityID);

    /**
     * Returns last unapproved entity for a given approved Entity ID
     * 
     * @param approvedEntityID
     * @return
     */
    BaseMasterEntity getLastUnApprovedEntityByApprovedEntityId(EntityId approvedEntityID);

    /**
     * Checks whether an approved entity exists with given property name and
     * value for a given entity.
     */
    <T extends BaseMasterEntity> Boolean hasEntity(Class<T> entityClass, String propertyName, Object propertyValue);

    /**
     * Checks whether an approved entity exists with given property name(s) and
     * value(s) queryMap, for a given entity.
     * 
     * @return List of properties having given property value. Generally used
     *         while creating a new master entity to avoid duplication.
     */
    <T extends BaseMasterEntity> List<String> hasEntity(Class<T> entityClass, Map<String, Object> queryMap);

    /**
     * 'LIKE' searches the existing approved entity with given property name(s)
     * and value(s). Generally used for data table search. This method also
     * returns the associated actions applicable to a user.
     */
    <T extends BaseMasterEntity> List<T> findEntity(Class<T> entityClass, Integer iDisplayStart, Integer iDisplayLength,
            Map<String, Object> queryMap);

    <T extends BaseMasterEntity> List<T> findEntityWithoutPagination(Class<T> entityClass, Map<String, Object> queryMap);

    /**
     * 'EQUAL IGNORE CASE' searches the existing approved entity with given
     * property name(s) and value(s).
     */
    <T extends BaseMasterEntity> List<T> findEntityUsingEqualMatch(Class<T> entityClass, Map<String, Object> queryMap);

    <T extends BaseMasterEntity> List<T> loadPaginatedData(Class<T> entityName, String userUri, Integer iDisplayStart,
            Integer iDisplayLength, String sortColName, String sortDir);

    public int getTotalRecordSize(Class<Serializable> entityClass, String uri);

//    public List<BusinessPartner> findApprovedEntityByBusinessPartnerType(String value);

    /**
     * Checks whether an approved entity exists with given property name(s) and
     * value(s) queryMap, but this entity should not be the one that we are
     * modifying.
     * 
     * @param entityClass
     *            : Entity over which this search is being done
     * @param queryMap
     *            : Map of column names and their values
     * @param id
     *            : Id of the entity
     * @return List of properties having given property value. Generally used
     *         while modifying an approved master entity to avoid duplication.
     */
    public <T extends BaseMasterEntity> List<String> getDuplicateColumnNames(Class<T> entityClass,
            Map<String, Object> queryMap, Long id);

    /**
     * Returns all 'approved' and 'active 'entities for a given entity type
     * 
     * @param entityClass
     * @return
     */
    <T extends BaseMasterEntity> List<T> getAllApprovedAndActiveEntities(Class<T> entityClass);
    
    
    /** 
     * @param entityClass 
     * @param isQueryCacheEnabled boolean true/false
     * @return Returns a list of entities and enable/disable Query cache on-demand based on isQueryCacheAble value
     */ 
    @Deprecated
    public <T extends BaseMasterEntity> List<T> getAllApprovedAndActiveEntities(Class<T> entityClass,boolean isQueryCacheEnabled);

    

    public <T> List<Map<String, Object>> getAllApprovedAndActiveSelectedListEntities(Class<T> entityClass,
            String... columnNameList);

    /**
     * Checks whether an approved entity exists with given property name(s) and
     * value(s) queryMap, for a given child entity.
     * 
     * @return List of properties having given property value. Generally used
     *         while creating a new master entity to avoid duplication.
     */
    <T extends BaseMasterEntity> List<String> hasEntityCheckForChildEntity(Class<T> entityClass, Map<String, Object> queryMap);

    /**
     * Gets the entity by entity id.
     * 
     * @param <T>
     *            the generic type
     * @param entityId
     *            the entity id
     * @return the master entity by entity id
     */
    public <T extends Entity> T getEntityByEntityId(EntityId entityId);

    public <T extends BaseMasterEntity> Integer getApprovalStatusOfMasterEntityById(Class<T> entityClass, Long id);

    public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap);

    public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap, boolean andCondition);

    public <T extends BaseMasterEntity> T findById(Class<T> entityClass, Long id);

    /**
     * Save or update entity.
     *
     * @param <T> the generic type
     * @param entity the entity
     * @return the t
     */
    public <T extends Entity> T saveOrUpdateEntity(T entity);

    public <T extends BaseMasterEntity> Boolean hasApprovedEntity(Class<T> entityClass, String propertyName,
            Object propertyValue);
    
    Long getApprovalTaskIdbyRefUUID(String  uuid);
    
    
    public <T extends BaseMasterEntity> List<T> retrieveTypes(Class<T> entityClass);
    
    
    
  
    
   
    public <T extends BaseMasterEntity> List<T> retrieveTypes(Class<T> entityClass, boolean booleanForUpdate);

    public <T extends Entity> List<T> retrieveMasterForMobileFlagY(Class<T> entityClass, String authorizationBusinessDate);   
    
    public <T extends BaseEntity> List<T> findAllBaseEntitiesParameter(Class<T> genericEntityClassName, boolean forUpdate, String authorizationBusinessDate) ;


    /**
     * Method used for searching, sorting and load data
     * @param gridVO
     * @param entityClass
     * @param userUri
     * @return
     */
    <T extends BaseMasterEntity> List<T> loadPaginatedData(GridVO gridVO, Class<T> entityClass, String userUri);
    <T extends BaseMasterEntity> List<T> getApprovedAndActiveEntityForAllPropertyNameValueMap(Class<T> entityClass,Map<String,Object> propertyNameValueMap);
    <T extends BaseMasterEntity> List<T> getApprovedAndActiveEntityForAnyMatchedPropertyNameValueMap(Class<T> entityClass,Map<String,Object> propertyNameValueMap);
    <T extends Entity> List<T> findEntitiesByCriteria(Class<T> entityClass,CriteriaMapVO criteriaMapVO);
    
    /**
     * Method to get total records that match the specified search criteria.
     * 
     * @param gridVO
     * @param entityName
     * @param userUri
     * @return
     */
    <T extends BaseMasterEntity> Long getSearchRecordsCount(GridVO gridVO, Class<T> entityName, String userUri);


	<T extends BaseMasterEntity> List<T> setApplicableWorkFlowActions(Boolean isAuthorizedMakerForEntity, Boolean isAuthorizedCheckerForEntity,
			Class<T> entityClass, String userUri, List<T> baseMasterEntities,
			Map<Long, BaseMasterEntity> _idToEntities);
	 public <T> List<Map<String, Object>> getApprovedAndActiveSelectedListEntitiesForGivenCriteria(Class<T> entityClass,
			Map<String, Object> whereList, String[] columnNameList);

    Integer getWorkflowTotalRecordSize(Class<Serializable> entityClass, String uri, boolean isDynamicWorkflow);


    public <T extends BaseMasterEntity> boolean hasEntity(Class<T> entityClass, Map<String, Object> propertyNameValueMap, Long cityId);

    public <T> List<Map<String, Object>> getAllApprovedAndActiveSelectedListEntities(Class<T> entityClass, List<Integer> statusList,Map<String,Object> whereList,
            String... columnNameList);
}
