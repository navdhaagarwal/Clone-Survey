/**
 * @FileName: BaseMasterDao.java
 * @Author: amit.parashar
 * @Copyright: Nucleus Software Exports Ltd
 * @Description:
 * @Program-specification-Referred:
 * @Revision:
 *            --------------------------------------------------------------------------------------------------------------
 *            --
 * @Version | @Last Revision Date | @Name | @Function/Module affected | @Modifications Done
 *          ----------------------------------------------------------------------------------------------------------------
 *          | Jun 20, 2012 | amit.parashar | |
 */

package com.nucleus.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.makerchecker.EntityUpdateInfo;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.CriteriaMapVO;

public interface BaseMasterDao extends BaseDao<BaseMasterEntity> {

    public List<UnapprovedEntityData> getAllUnapproved(Class<? extends BaseMasterEntity> entityClass);

    public List<UnapprovedEntityData> getAllUnapprovedVersionsOfEntity(EntityId entityId);

    public List<Long> getEntityIdsForInProgressMakerChecker(Class<? extends BaseMasterEntity> entityClass);

    // used for dropdown menus.
    <T extends BaseMasterEntity> List<T> getEntitiesByStatus(Class<T> entityClass, List<Integer> statusList);

    // used for grids.
    <T extends BaseMasterEntity> List<T> getAllEntitiesForGridByStatus(Class<T> entityClass, List<Integer> statusList,
            String specificStatusList, String viewerUri);

    public BaseMasterEntity getLastApprovedEntityByUnapprovedEntityId(EntityId entityId);

    // returns AllUnapprovedVersionsOfEntityByUUID since last approved version.
    public List<UnapprovedEntityData> getAllUnapprovedVersionsOfEntityByUUID(String uUID, BaseMasterEntity lastApprovedEntity);
    
    @Deprecated
    public <T extends BaseMasterEntity> List<Object[]> getEntitiesWithActionsByStatus(Class<T> entityClass,
            List<Integer> statusList, List<String> authCodes);
    
    public <T extends BaseMasterEntity> List<Object[]> getEntitiesWithActionsByStatus(Class<T> entityClass,
            List<Integer> statusList, List<String> authCodes, List<String> uuids);

    public <T extends BaseMasterEntity> List<Object[]> getEntitiyWithActionsByStatus(Class<T> entityClass,
            List<Integer> statusList, List<String> authCodes, Long entityId);

    public boolean updateReferences(List<EntityUpdateInfo> entityUpdateInfos, BaseEntity intialEntityId,
            BaseEntity lastUpdateId);

    public <T extends BaseMasterEntity> Boolean hasEntity(Class<T> entityClass, String propertyName, Object propertyValue);

    public <T extends BaseMasterEntity> Boolean hasEntity(Class<T> entityClass, Map<String, Object> propertyValueMap, Long id);

    public <Q extends BaseMasterEntity> List<Q> getPaginatedEntitiesForGridByStatus(Class<Q> entityName,
            List<Integer> genericStatusList, String specificstatusList, String viewerUri, Integer iDisplayStart,
            Integer iDisplayLength, String sortColName, String sortDir);

    public int getTotalRecords(Class<Serializable> entityClass, List<Integer> ststusList);

    public <Q extends BaseMasterEntity> List<Q> getVersionedList(Class<Q> entityClass, List<Q> records);

    <T extends BaseMasterEntity> List<T> getAllApprovedAndActiveEntities(Class<T> entityClass, List<Integer> statusList);
    
    /** 
     * @param entityClass 
     * @param isQueryCacheEnabled boolean true/false
     * @param statusList
     * @return Returns a list of entities and enable/disable Query cache on-demand based on isQueryCacheAble value
     */
    @Deprecated
    public <T extends BaseMasterEntity> List<T> getAllApprovedAndActiveEntities(Class<T> entityClass, List<Integer> statusList,boolean isQueryCacheEnabled);

    public <T> List<Map<String, Object>> getAllApprovedAndActiveSelectedListData(Class<T> entityClass,
            List<Integer> statusList,Map<String, Object> whereList, String... columnNameList);

    public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap);

    public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap, boolean andCondition);

 // check whether given master has approved entry with given property name and property value
    public <T extends BaseMasterEntity> Boolean hasApprovedEntity(Class<T> entityClass, String propertyName,
            Object propertyValue);
    public Long getApprovalTaskIdByRefUUID(String  uuid);
    
    
    public <T extends BaseMasterEntity> List<T> findAllBaseParameter(Class<T> genericEntityClassName);
    /**
     * 
     * Method to load generic parameter based on entity class name and update flag to specify if the loaded data is readonly or not
     * @param genericEntityClassName
     * @return
     */
    public <T extends BaseMasterEntity> List<T> findAllBaseParameter(Class<T> genericEntityClassName, boolean forUpdate);
    
    public <T extends Entity> List<T> retrieveMasterForMobileFlagY(Class<T> entityClass, String authorizationBusinessDate);
    
    public <T extends BaseEntity> List<T> findAllBaseEntitiesParameter(Class<T> genericEntityClassName, boolean forUpdate, String authorizationBusinessDate) ;

    
    /**
     * 
     * Method used for searching, sorting and load data
     * @param gridVO
     * @param entityName
     * @param viewerUri
     * @param genericStatusList
     * @param specificstatusList
     * @return
     */
    <Q extends BaseMasterEntity> List<Q> getPaginatedEntitiesForGridByStatus(GridVO gridVO, Class<Q> entityName, String viewerUri,
            List<Integer> genericStatusList, String specificStatusList);
   
    <T extends BaseMasterEntity> List<T> getApprovedAndActiveEntityForAllPropertyNameAndValues(Class<T> entityClass,Map<String,Object> propertyNameValueMap);
    
    <T extends BaseMasterEntity> List<T> getApprovedAndActiveEntityForAnyMatchedPropertyNameAndValue(Class<T> entityClass,Map<String,Object> propertyNameValueMap);
    <T extends Entity> List<T> findEntitiesByCriteria(Class<T> entityClass,CriteriaMapVO criteriaMapVO);
    
    /**
     * To get the count of all entities with status.
     * @param entityClass
     * @param genericStatusList
     * @param specificStatusList
     * @param viewerUri
     * @return
     */
    <T extends BaseMasterEntity> Long getAllEntitiesCountForGridByStatus(Class<T> entityClass, List<Integer> genericStatusList,
			String specificStatusList, String viewerUri);

	/**
	 * To get the count of all entities with search criteria.
	 * This count will be needed for pagination on searched records.
	 * @param gridVO
	 * @param entityClass
	 * @param viewerUri
	 * @param genericStatusList
	 * @param specificStatusList
	 * @return
	 */
	<T extends BaseMasterEntity> Long getPaginatedEntitiesCountForGridByStatus(GridVO gridVO, Class<T> entityClass, String viewerUri,
			List<Integer> genericStatusList, String specificStatusList);

	public <T> List<Map<String, Object>> getAllApprovedAndActiveSelectedListData(Class<T> entityClass, List<Integer> statusList,
			String[] columnNameList);

    Integer getWorkflowTotalRecords(Class<Serializable> entityClass, List<Integer> statusList, boolean isDynamicWorkflow);
    
    public Object getColumnValueFromEntity(Class entity,Long id,String colName);
}
