package com.nucleus.master;

import static com.nucleus.core.common.NeutrinoComparators.CREATION_TIME_STAMP_COMPARATOR;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.approval.ApprovalTask;
import com.nucleus.authority.Authority;
import com.nucleus.authority.AuthorityCodes;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.CriteriaQueryExecutor;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.MapQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.ActionConfiguration;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.makerchecker.MakerCheckerApprovalFlow;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.makerchecker.MasterApprovalFlowConstants;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.persistence.BaseMasterDao;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserService;

import net.bull.javamelody.MonitoredWithSpring;

/**
 * @author amit.parashar
 * 
 */
@Named("baseMasterService")
@MonitoredWithSpring(name = "BaseMaster_Service_IMPL_")
public class BaseMasterServiceImpl extends BaseServiceImpl implements BaseMasterService {

    @Inject
    @Named("baseMasterDao")
    protected BaseMasterDao             baseMasterDao;

    @Inject
    @Named(value = "masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;

    @Inject
    @Named("userService")
    UserService                         userService;

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService         makerCheckerService;

    private String[] escapeCharacters = {"_","%"};
    
    private final static Integer		ORACLE_LIMIT_FOR_IN_CLAUSE_ELEMENTS	= 998;

    @Override
    public <T extends BaseMasterEntity> List<T> getLastApprovedEntities(Class<T> entityClass) {
         return baseMasterDao.getEntitiesByStatus(entityClass, ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
    }

    @Override
    @MonitoredWithSpring(name = "BMSI_FETCH_APP_ACTI_ENTITY")
    public <T extends BaseMasterEntity> List<T> getAllApprovedAndActiveEntities(Class<T> entityClass) {
    return baseMasterDao.getAllApprovedAndActiveEntities(entityClass, ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        
    }


    @Override
    @Deprecated
	public <T extends BaseMasterEntity> List<T> getAllApprovedAndActiveEntities(Class<T> entityClass,boolean isQueryCacheEnabled) {
		return baseMasterDao.getAllApprovedAndActiveEntities(entityClass, ApprovalStatus.APPROVED_RECORD_STATUS_LIST, isQueryCacheEnabled);
	}

    @Override
    @MonitoredWithSpring(name = "BMSI_FETCH_APP_ACTI_SEL_LIST_ENTITY")
    public <T> List<Map<String, Object>> getAllApprovedAndActiveSelectedListEntities(Class<T> entityClass,
            String... columnNameList) {
        return baseMasterDao.getAllApprovedAndActiveSelectedListData(entityClass, ApprovalStatus.APPROVED_RECORD_STATUS_LIST, null,columnNameList);
    }
    @Override
    @MonitoredWithSpring(name = "BMSI_FETCH_APP_ACTI_SEL_LIST_ENTITY")
    public <T> List<Map<String, Object>> getApprovedAndActiveSelectedListEntitiesForGivenCriteria(Class<T> entityClass,Map<String,Object> whereList,String... columnNameList) {
        return baseMasterDao.getAllApprovedAndActiveSelectedListData(entityClass, ApprovalStatus.APPROVED_RECORD_STATUS_LIST,whereList, columnNameList);
    }
    
    @Override
    @MonitoredWithSpring(name = "BMSI_FETCH_APP_ACTI_SEL_LIST_ENTITY")
    public <T> List<Map<String, Object>> getAllApprovedAndActiveSelectedListEntities(Class<T> entityClass,List<Integer> statusList,Map<String,Object> whereList,
            String... columnNameList) {
        return baseMasterDao.getAllApprovedAndActiveSelectedListData(entityClass,statusList, whereList,columnNameList);
    }

    @Override
    public <T extends BaseMasterEntity> List<T> loadAllEntities(Class<T> entityClass, String userUri) {
        return loadPaginatedData(entityClass, userUri, null, null, null, null);
    }

    @Override
    public <T extends BaseMasterEntity> T getMasterEntityById(Class<T> geoEntityclass, Long id) {
        return baseMasterDao.find(geoEntityclass, id);

    }

    @Override
    public BaseMasterEntity getLastApprovedEntityByUnapprovedEntityId(EntityId lastUnapprovedEntityID) {
        BaseMasterEntity originalEntity = baseMasterDao.getLastApprovedEntityByUnapprovedEntityId(lastUnapprovedEntityID);
        return originalEntity;
    }

    @Override
    public BaseMasterEntity getLastUnApprovedEntityByApprovedEntityId(EntityId approvedEntityID) {
        BaseMasterEntity approvedEntity = baseMasterDao.get(approvedEntityID);
        List<UnapprovedEntityData> unapprovedChanges = baseMasterDao.getAllUnapprovedVersionsOfEntityByUUID(approvedEntity
                .getEntityLifeCycleData().getUuid(), approvedEntity);
        if (unapprovedChanges != null && unapprovedChanges.size() > 0) {
            UnapprovedEntityData ued = Collections.max(unapprovedChanges,CREATION_TIME_STAMP_COMPARATOR);
            return baseMasterDao.get(ued.getChangedEntityId());
        } else {
            return null;
        }
    }

    @Override
    public <T extends BaseMasterEntity> Boolean hasEntity(Class<T> entityClass, String propertyName, Object propertyValue) {
        return baseMasterDao.hasEntity(entityClass, propertyName, propertyValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseMasterEntity> T getMasterEntityWithActionsById(Class<T> entityClass, Long id, String userUri) {
        return makerCheckerService.getMasterEntityWithActionsById(entityClass,id,userUri);
    }

    protected void setNonWorkflowEntityAction(BaseMasterEntity bma, Boolean isAuthorozedMakerForEntity) {
    	setNonWorkflowEntityAction(bma.getClass().getSimpleName(), bma, isAuthorozedMakerForEntity);
	}

    private void setNonWorkflowEntityAction(String entityName, BaseMasterEntity bma,
    		Boolean isAuthorozedMakerForEntity) {
    	makerCheckerService.setNonWorkflowEntityAction(entityName, bma, isAuthorozedMakerForEntity);
    }

	@Override
    public <T extends BaseMasterEntity> List<String> hasEntity(Class<T> entityClass, Map<String, Object> queryMap) {
        MapQueryExecutor executor = new MapQueryExecutor(entityClass);
        List<String> existingcolNames = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        int counter = 0;
        if (queryMap != null) {
            for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
                if (counter > 0) {
                    builder.append("  or ");
                }
				String key = entry.getKey();
				String propertyBindVar = key.replace('.', '_');
                executor.addQueryColumns(key);
                builder.append("lower(".concat(key).concat(")= :")
                        .concat(propertyBindVar));
                Object value=entry.getValue();
                executor.addBoundParameter(propertyBindVar, (value instanceof String) ? String.valueOf(value).toLowerCase().trim():value);
                counter++;
            }
            builder.append(")");
            executor.addOrClause(builder.toString());
            executor.addAndClause("masterLifeCycleData.approvalStatus in :approvalStatusList");
            executor.addAndClause("(entityLifeCycleData.snapshotRecord is null or entityLifeCycleData.snapshotRecord = false)");
            executor.addBoundParameter("approvalStatusList", Arrays.asList(ApprovalStatus.UNAPPROVED_ADDED,ApprovalStatus.APPROVED_MODIFIED,ApprovalStatus.UNAPPROVED_MODIFIED,
                    ApprovalStatus.WORFLOW_IN_PROGRESS,ApprovalStatus.APPROVED_DELETED_IN_PROGRESS,ApprovalStatus.APPROVED_DELETED,ApprovalStatus.APPROVED));
            executor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);

            List<Map<String, ?>> result = baseMasterDao.executeQuery(executor);
            for (Map<String, ?> singleResultMap : result) {
                for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
                    if (!existingcolNames.contains(entry.getKey())) {
                        if (singleResultMap.containsKey(entry.getKey())) {
                            Object val = singleResultMap.get(entry.getKey());
                            if (null != val && null != entry.getValue()
                                    && val.toString().equalsIgnoreCase(entry.getValue().toString().trim())) {
                                existingcolNames.add(entry.getKey());
                            }
                        }
                    }
                }
            }
        }
        return existingcolNames;
    }

    @SuppressWarnings("unchecked")
    @Override
    @MonitoredWithSpring(name = "BMSI_FETCH_PAGINATED_DATA")
    public <T extends BaseMasterEntity> List<T> loadPaginatedData(Class<T> entityClass, String userUri,
            Integer iDisplayStart, Integer iDisplayLength, String sortDir, String sortColName) {
    	GridVO gridVO = new GridVO();
        gridVO.setiDisplayStart(iDisplayStart);
        gridVO.setiDisplayLength(iDisplayLength);
        gridVO.setSortDir(sortDir);
        gridVO.setSortColName(sortColName);
        gridVO.setSearchMap(null);
    	return loadPaginatedData(gridVO, entityClass, userUri);
    }

    @Override
    public Integer getWorkflowTotalRecordSize(Class<Serializable> entityClass, String userUri, boolean isDynamicWorkflow) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList = getStatusListBasedOnAuthority(statusList, isAuthorizedMakerForEntity(entityClass));
        return baseMasterDao.getWorkflowTotalRecords(entityClass, statusList, isDynamicWorkflow);
    }

    @Override
    public int getTotalRecordSize(Class<Serializable> entityClass, String uri) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList = getStatusListBasedOnAuthority(statusList, isAuthorizedMakerForEntity(entityClass));
        return baseMasterDao.getTotalRecords(entityClass, statusList);
    }

    private Boolean isAuthorizedMakerForEntity(Class<Serializable> entityClass) {
        Set<Authority> authorities = getCurrentUser().getUserAuthorities();

        Boolean isAuthorizedMakerForEntity = false;

        for (Authority authority : authorities) {
            if (authority.getAuthCode().equalsIgnoreCase(
                    AuthorityCodes.MAKER + "_" + entityClass.getSimpleName().toUpperCase())) {
                isAuthorizedMakerForEntity = true;
                break;
            }
        }
        return isAuthorizedMakerForEntity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseMasterEntity> List<T> findEntity(Class<T> entityClass, Integer iDisplayStart,
            Integer iDisplayLength, Map<String, Object> queryMap) {
        if (entityClass == null || queryMap == null) {
            return null;
        }
        HashMap<Long, BaseMasterEntity> _idToEntities = new HashMap<Long, BaseMasterEntity>();
        CriteriaQueryExecutor<T> criteriaQueryExecutor = new CriteriaQueryExecutor<T>(entityClass);
        for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
            boolean containsEscapeChars = checkForEscapeChar(entry.getValue());
            if(containsEscapeChars){
                String str = replaceWithEscapeChar(entry.getValue());
                criteriaQueryExecutor.addOrClause(entry.getKey(), CriteriaQueryExecutor.LIKE_OPERATOR_WITH_ESCAPE, str);
            }
            else{
                criteriaQueryExecutor.addOrClause(entry.getKey(), CriteriaQueryExecutor.LIKE_OPERATOR, entry.getValue());
            }
        }
        List<Integer> statusList = new ArrayList<Integer>();

        Boolean isAuthorizedMakerForEntity = loggedInUserisAnAuthorizedMakerForEntity(entityClass);

        if (isAuthorizedMakerForEntity) {
            statusList.add(ApprovalStatus.CLONED);
        }

        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED);
        criteriaQueryExecutor.addAndClause("masterLifeCycleData.approvalStatus", CriteriaQueryExecutor.IN_OPERATOR,
                statusList);

        /*
         * This is a temporary work around for
         * supporting grouped search through CriteriaRequestExecutor.
         * This can be further optmized by changing the 
         * executor used.For the time this is implemented
         * 
         */
        criteriaQueryExecutor.addAndOrClause("entityLifeCycleData.snapshotRecord", CriteriaQueryExecutor.EQUALS_OPERATOR,
                null);
        criteriaQueryExecutor.addAndOrClause("entityLifeCycleData.snapshotRecord", CriteriaQueryExecutor.EQUALS_OPERATOR,
                false);

        List<Integer> persistenceStatusList = new ArrayList<Integer>();
        persistenceStatusList.add(PersistenceStatus.EMPTY_PARENT);
        criteriaQueryExecutor.addAndClause("entityLifeCycleData.persistenceStatus", CriteriaQueryExecutor.NOT_IN_OPERATOR,
                persistenceStatusList);

        /*criteriaQueryExecutor
                .addAndClause("entityLifeCycleData.snapshotRecord", CriteriaQueryExecutor.EQUALS_OPERATOR, null);
        criteriaQueryExecutor
                .addOrClause("entityLifeCycleData.snapshotRecord", CriteriaQueryExecutor.EQUALS_OPERATOR, false);*/
        criteriaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        List<T> bmeList = baseMasterDao.executeQuery(criteriaQueryExecutor, iDisplayStart, iDisplayLength);
        // bmeList = baseMasterDao.getVersionedList(entityClass, bmeList);

        Boolean isAuthorizedCheckerForEntity = loggedInUserisAnAuthorizedCheckerForEntity(entityClass);
        for (BaseMasterEntity bma : bmeList) {
            setNonWorkflowEntityAction(entityClass.getSimpleName(), bma, isAuthorizedMakerForEntity);
            _idToEntities.put(bma.getId(), bma);
        }
        bmeList = setApplicableWorkFlowActions(isAuthorizedMakerForEntity, isAuthorizedCheckerForEntity, entityClass,
                getCurrentUser().getUserEntityId().getUri(), bmeList, _idToEntities);
        return bmeList;
    }

    @Override
    public <T extends BaseMasterEntity> List<T> findEntityWithoutPagination(Class<T> entityClass,
            Map<String, Object> queryMap) {
        if (entityClass == null || queryMap == null) {
            return null;
        }
        HashMap<Long, BaseMasterEntity> _idToEntities = new HashMap<Long, BaseMasterEntity>();
        CriteriaQueryExecutor<T> criteriaQueryExecutor = new CriteriaQueryExecutor<T>(entityClass);
        for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
            criteriaQueryExecutor.addOrClause(entry.getKey(), CriteriaQueryExecutor.LIKE_OPERATOR, entry.getValue());
        }
        List<Integer> statusList = new ArrayList<Integer>();

        Boolean isAuthorizedMakerForEntity = loggedInUserisAnAuthorizedMakerForEntity(entityClass);

        if (isAuthorizedMakerForEntity) {
            statusList.add(ApprovalStatus.CLONED);
        }

        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED);
        criteriaQueryExecutor.addAndClause("masterLifeCycleData.approvalStatus", CriteriaQueryExecutor.IN_OPERATOR,
                statusList);

        /*
         * This is a temporary work around for
         * supporting grouped search through CriteriaRequestExecutor.
         * This can be further optmized by changing the 
         * executor used.For the time this is implemented
         * 
         */
        criteriaQueryExecutor.addAndOrClause("entityLifeCycleData.snapshotRecord", CriteriaQueryExecutor.EQUALS_OPERATOR,
                null);
        criteriaQueryExecutor.addAndOrClause("entityLifeCycleData.snapshotRecord", CriteriaQueryExecutor.EQUALS_OPERATOR,
                false);

        List<Integer> persistenceStatusList = new ArrayList<Integer>();
        persistenceStatusList.add(PersistenceStatus.EMPTY_PARENT);
        criteriaQueryExecutor.addAndClause("entityLifeCycleData.persistenceStatus", CriteriaQueryExecutor.NOT_IN_OPERATOR,
                persistenceStatusList);

        /*criteriaQueryExecutor
                .addAndClause("entityLifeCycleData.snapshotRecord", CriteriaQueryExecutor.EQUALS_OPERATOR, null);
        criteriaQueryExecutor
                .addOrClause("entityLifeCycleData.snapshotRecord", CriteriaQueryExecutor.EQUALS_OPERATOR, false);*/
        criteriaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        List<T> bmeList = baseMasterDao.executeQuery(criteriaQueryExecutor);
        // bmeList = baseMasterDao.getVersionedList(entityClass, bmeList);

        Boolean isAuthorizedCheckerForEntity = loggedInUserisAnAuthorizedCheckerForEntity(entityClass);
        for (BaseMasterEntity bma : bmeList) {
            setNonWorkflowEntityAction(entityClass.getSimpleName(), bma, isAuthorizedMakerForEntity);
            _idToEntities.put(bma.getId(), bma);
        }
        bmeList = setApplicableWorkFlowActions(isAuthorizedMakerForEntity, isAuthorizedCheckerForEntity, entityClass,
                getCurrentUser().getUserEntityId().getUri(), bmeList, _idToEntities);
        return bmeList;
    }

    

    @Override
    public <T extends BaseMasterEntity> List<T> findEntityUsingEqualMatch(Class<T> entityClass, Map<String, Object> queryMap) {
        if (entityClass == null || queryMap == null) {
            return null;
        }
        CriteriaQueryExecutor<T> criteriaQueryExecutor = new CriteriaQueryExecutor<T>(entityClass);
        for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
            criteriaQueryExecutor.addOrClause(entry.getKey(), CriteriaQueryExecutor.EQUALS_OPERATOR, entry.getValue());
        }
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        criteriaQueryExecutor.addAndClause("masterLifeCycleData.approvalStatus", CriteriaQueryExecutor.IN_OPERATOR,
                statusList);
        criteriaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        List<T> bmeList = baseMasterDao.executeQuery(criteriaQueryExecutor);
        return bmeList;
    }

    @Override
    public <T extends BaseMasterEntity> List<String> getDuplicateColumnNames(Class<T> entityClass,
            Map<String, Object> queryMap, Long id) {
        MapQueryExecutor executor = new MapQueryExecutor(entityClass);
        List<String> existingcolNames = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        int counter = 0;
        if (queryMap != null) {
            for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
                if (counter > 0) {
                    builder.append("  or ");
                }
                String key = entry.getKey();
                String propertyBindVar = key.replace('.', '_');
                executor.addQueryColumns(key);
                builder.append("lower(".concat(key).concat(")= :")
                        .concat(propertyBindVar));
                Object value=entry.getValue();
                executor.addBoundParameter(propertyBindVar, (value instanceof String) ? String.valueOf(value).toLowerCase().trim():value);
                counter++;
            }
            builder.append(")");

            // Just to check if record in UNAPPROVED_MODIFIED is being modified again.
            BaseMasterEntity baseMasterEntity = getLastApprovedEntityByUnapprovedEntityId(new EntityId(entityClass, id));

            executor.addOrClause(builder.toString());
            executor.addAndClause("masterLifeCycleData.approvalStatus in :approvalStatusList");
            executor.addAndClause("(entityLifeCycleData.snapshotRecord is null or entityLifeCycleData.snapshotRecord = false)");
            // Added to prevent checking a record against itself
            if(id!=null)
            {
            executor.addAndClause("Not (id = :id)");
            executor.addBoundParameter("id",id);
            }
            // To prevent adding column names in duplicate column list(existingcolNames) from original entity if record in
            // UNAPPROVED_MODIFIED state is modified again.

            if (baseMasterEntity != null) {
                executor.addAndClause("Not (entityLifeCycleData.uuid = :uuid)");
                executor.addBoundParameter("uuid",baseMasterEntity.getEntityLifeCycleData().getUuid());
            }
            executor.addBoundParameter("approvalStatusList", Arrays.asList(
            		ApprovalStatus.UNAPPROVED_ADDED,
            		ApprovalStatus.APPROVED_MODIFIED,
            		ApprovalStatus.UNAPPROVED_MODIFIED,
            		ApprovalStatus.WORFLOW_IN_PROGRESS,
            		ApprovalStatus.APPROVED_DELETED_IN_PROGRESS,
            		ApprovalStatus.APPROVED_DELETED,
            		ApprovalStatus.APPROVED));
           executor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            List<Map<String, ?>> result = baseMasterDao.executeQuery(executor);
            for (Map<String, ?> singleResultMap : result) {
                for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
                    if (!existingcolNames.contains(entry.getKey())) {
                        if (singleResultMap.containsKey(entry.getKey())) {
                            Object val = singleResultMap.get(entry.getKey());
                            if (null != val && null != entry.getValue()
                                    && val.toString().equalsIgnoreCase(entry.getValue().toString().trim())) {
                                existingcolNames.add(entry.getKey());
                            }
                        }
                    }
                }
            }
        }
        return existingcolNames;
    }

    private List<Integer> getStatusListBasedOnAuthority(List<Integer> statusList, Boolean isAuthorizedMakerForEntity) {
        if (isAuthorizedMakerForEntity) {
            statusList.add(ApprovalStatus.CLONED);
        }
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        return statusList;

    }

    @Override
    public <T extends BaseMasterEntity> List<String> hasEntityCheckForChildEntity(Class<T> entityClass,
            Map<String, Object> queryMap) {

        MapQueryExecutor executor = new MapQueryExecutor(entityClass);
        List<String> existingcolNames = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        int counter = 0;
        if (queryMap != null) {
            for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
                if (counter > 0) {
                    builder.append("  or ");
                }
                String key = entry.getKey();
                
                executor.addQueryColumns(key);
                builder.append(key + " = :" + key);
                executor.addBoundParameter(key, entry.getValue());
                counter++;
            }
            builder.append(")");
            executor.addOrClause(builder.toString());
            executor.addAndClause("masterLifeCycleData.approvalStatus in :approvalStatusList");
            executor.addOrClause("masterLifeCycleData.approvalStatus is null");
            executor.addAndClause("entityLifeCycleData.snapshotRecord is null or entityLifeCycleData.snapshotRecord = false");
            
            executor.addBoundParameter("approvalStatusList",Arrays.asList(ApprovalStatus.UNAPPROVED_ADDED,ApprovalStatus.APPROVED_MODIFIED,ApprovalStatus.UNAPPROVED_MODIFIED,
            ApprovalStatus.WORFLOW_IN_PROGRESS,ApprovalStatus.APPROVED_DELETED_IN_PROGRESS,ApprovalStatus.APPROVED_DELETED,ApprovalStatus.APPROVED)); 
            
            executor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);

            List<Map<String, ?>> result = baseMasterDao.executeQuery(executor);
            for (Map<String, ?> singleResultMap : result) {
                for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
                    if (!existingcolNames.contains(entry.getKey())) {
                        if (singleResultMap.containsKey(entry.getKey())) {
                            Object val = singleResultMap.get(entry.getKey());
                            if (null != val && null != entry.getValue()
                                    && val.toString().equalsIgnoreCase(entry.getValue().toString().trim())) {
                                existingcolNames.add(entry.getKey());
                            }
                        }
                    }
                }
            }
        }
        return existingcolNames;
    }

    protected <T extends BaseMasterEntity> boolean loggedInUserisAnAuthorizedMakerForEntity(Class<T> entityClass) {
        Set<Authority> authorities = getCurrentUser().getUserAuthorities();
        Boolean isAuthorizedMakerForEntity = false;
        for (Authority authority : authorities) {
            if (authority.getAuthCode().equalsIgnoreCase(
                    AuthorityCodes.MAKER + "_" + entityClass.getSimpleName().toUpperCase())) {
                isAuthorizedMakerForEntity = true;
                break;
            }
        }
        return isAuthorizedMakerForEntity;
    }

    protected <T extends BaseMasterEntity> boolean loggedInUserisAnAuthorizedCheckerForEntity(Class<T> entityClass) {
        Set<Authority> authorities = getCurrentUser().getUserAuthorities();
        Boolean isAuthorizedCheckerForEntity = false;
        for (Authority authority : authorities) {
            if (authority.getAuthCode().equalsIgnoreCase(
                    AuthorityCodes.CHECKER + "_" + entityClass.getSimpleName().toUpperCase())) {
                isAuthorizedCheckerForEntity = true;
                break;
            }
        }
        return isAuthorizedCheckerForEntity;
    }


    protected <T extends BaseMasterEntity> boolean loggedInUserIsAuthorizedMakerOrCheckerForEntity(String authorityCode, String entityName) {
        Set<Authority> authorities = getCurrentUser().getUserAuthorities();
        Boolean isAuthorizedForEntity = false;
        for (Authority authority : authorities) {
            if (authority.getAuthCode().equalsIgnoreCase(
                    authorityCode + "_" + entityName.toUpperCase())) {
                isAuthorizedForEntity = true;
                break;
            }
        }
        return isAuthorizedForEntity;
    }


    /*  private <T extends BaseMasterEntity> List<String> getApplicableAuthList(boolean isAuthorizedMakerForEntity,
              boolean isAuthorizedCheckerForEntity, Class<T> entityClass) {
          List<String> authList = new ArrayList<String>(2);
          if (isAuthorizedMakerForEntity) {
              authList.add(AuthorityCodes.MAKER + "_" + entityClass.getSimpleName().toUpperCase());
          }
          if (isAuthorizedCheckerForEntity) {
              authList.add(AuthorityCodes.CHECKER + "_" + entityClass.getSimpleName().toUpperCase());
          }
          return authList;
      }*/



    @Override
    public <T extends Entity> T getEntityByEntityId(EntityId entityId) {
        Entity entity = baseMasterDao.get(entityId);
        return (T) entity;
    }
    
    @Override
    public <T extends BaseMasterEntity> List<T> setApplicableWorkFlowActions(Boolean isAuthorizedMakerForEntity,
            Boolean isAuthorizedCheckerForEntity, Class<T> entityClass, String userUri, List<T> baseMasterEntities,
            Map<Long, BaseMasterEntity> _idToEntities) {
        return makerCheckerService.setApplicableWorkFlowActions(isAuthorizedMakerForEntity,isAuthorizedCheckerForEntity,entityClass,userUri,baseMasterEntities,_idToEntities);
    }





	@Override
    public <T extends BaseMasterEntity> Integer getApprovalStatusOfMasterEntityById(Class<T> entityClass, Long id) {
        NeutrinoValidator.notNull(entityClass, "Entity class cannot be null");
        NeutrinoValidator.notNull(id, "BaseMasterEntity's id cannot be null");
        String queryString = "SELECT bme.masterLifeCycleData.approvalStatus FROM " + entityClass.getName()
                + " bme WHERE bme.id= :id";
        JPAQueryExecutor<Integer> jpaQueryExecutor = new JPAQueryExecutor<Integer>(queryString);
        jpaQueryExecutor.addParameter("id", id);
        return baseMasterDao.executeQueryForSingleValue(jpaQueryExecutor);

    }

    @Override
    public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap) {

        return baseMasterDao.findMasterByCode(entityClass, variablesMap);

    }

    @Override
    public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap, boolean andCondition) {

        return baseMasterDao.findMasterByCode(entityClass, variablesMap, andCondition);

    }
    
    @Override
    public <T extends Entity> List<T> findEntitiesByCriteria(Class<T> entityClass,CriteriaMapVO criteriaMapVO){
    	
    	return baseMasterDao.findEntitiesByCriteria(entityClass,criteriaMapVO);
    	
    }

    @Override
    public <T extends BaseMasterEntity> T findById(Class<T> entityClass, Long id) {
        return entityDao.find(entityClass, id);
    }

    @Override
    public <T extends Entity> T saveOrUpdateEntity(T entity) {
        NeutrinoValidator.notNull(entity, "Entity cannot be null");
        entityDao.saveOrUpdate(entity);
        return entity;
    }
    
    @Override
    public <T extends BaseMasterEntity> Boolean hasApprovedEntity(Class<T> entityClass, String propertyName,
            Object propertyValue) {
        return baseMasterDao.hasApprovedEntity(entityClass, propertyName, propertyValue);
    }
        
    @Override
    public Long getApprovalTaskIdbyRefUUID(String  uuid){
    	
    	return baseMasterDao.getApprovalTaskIdByRefUUID(uuid);
    }
    public String replaceWithEscapeChar(Object o) {
        String s = (String) o;
        String escape = new String("!");
        for (String escapeChar : escapeCharacters) {
            if (s.contains(escapeChar)) {
                s = s.replaceAll(escapeChar, (escape + escapeChar));
            }
        }

        return s;
    }
    
    public boolean checkForEscapeChar(Object o) {
        boolean flag = false;
        if (o instanceof String) {
            String s = (String) o;
            for (String escapeChar : escapeCharacters) {
                if (s.contains(escapeChar)) {
                    flag=true;
                    break;
                }
            }
        }
        return flag;
    }
    
    @Override
    @MonitoredWithSpring(name = "GPSI_FETCH_GENERIC1_TYPES")
    public <T extends BaseMasterEntity> List<T> retrieveTypes(Class<T> entityClass) {
    	return retrieveTypes(entityClass,false);
    }
    
    @Override
    @MonitoredWithSpring(name = "GPSI_FETCH_GENERIC1_TYPES")
    public <T extends BaseMasterEntity> List<T> retrieveTypes(Class<T> entityClass,boolean forUpdate) {
        if (entityClass == null) {
            throw new InvalidDataException("Entity class cannot be null");
        }
        return baseMasterDao.findAllBaseParameter(entityClass,forUpdate);
    }

	@Override
	public <T extends BaseEntity> List<T> findAllBaseEntitiesParameter(Class<T> genericEntityClassName, boolean forUpdate, String authorizationBusinessDate) {
		return baseMasterDao.findAllBaseEntitiesParameter(genericEntityClassName, forUpdate, authorizationBusinessDate);
	}

	@Override
	public <T extends Entity> List<T> retrieveMasterForMobileFlagY(Class<T> entityClass, String authorizationBusinessDate) {
		List<T> entities = baseMasterDao.retrieveMasterForMobileFlagY(entityClass, authorizationBusinessDate);
		for(Entity entity : entities){
		  entity =  HibernateUtils.initializeAndUnproxy(entity);
		}
        return entities;
	}


	@Override
	public <T extends BaseMasterEntity> List<T> loadPaginatedData(GridVO gridVO, Class<T> entityClass, String userUri) {

        Map<String, Object> searchMap = gridVO.getSearchMap();
        String entityAuthorityName = null;
        String dynamicWorkflowEntityClassName = "DynamicWorkflowConfiguration";
        boolean isDynamicWorkflow = false;
        if("WorkflowConfiguration".equalsIgnoreCase(entityClass.getSimpleName())) {
            if(searchMap!= null && !searchMap.isEmpty() && searchMap.containsKey("isDynamicWorkflow")) {
                if(Boolean.parseBoolean(searchMap.get("isDynamicWorkflow").toString())) {
                    entityAuthorityName = "DYNAMICWORKFLOWCONFIGURATION";
                    isDynamicWorkflow = true;
                }
            }
        }


		checkIfUserIdIsNotEqual(userUri);
        Map<Long, BaseMasterEntity> _idToEntities = new HashMap<Long, BaseMasterEntity>();
        Boolean isAuthorizedMakerForEntity;
        Boolean isAuthorizedCheckerForEntity;
        if(StringUtils.isBlank(entityAuthorityName)) {
            isAuthorizedMakerForEntity = loggedInUserisAnAuthorizedMakerForEntity(entityClass);
            isAuthorizedCheckerForEntity = loggedInUserisAnAuthorizedCheckerForEntity(entityClass);
        } else {
            isAuthorizedMakerForEntity = loggedInUserIsAuthorizedMakerOrCheckerForEntity(AuthorityCodes.MAKER, entityAuthorityName);
            isAuthorizedCheckerForEntity = loggedInUserIsAuthorizedMakerOrCheckerForEntity(AuthorityCodes.CHECKER, entityAuthorityName);
        }
        List<Integer> genericStatusList = new ArrayList<Integer>();
        genericStatusList = getStatusListBasedOnAuthority(genericStatusList, isAuthorizedMakerForEntity);
        String specificStatusList = "" + ApprovalStatus.UNAPPROVED_ADDED + "," + ApprovalStatus.UNAPPROVED_MODIFIED;
        List<T> baseMasterEntities = null;
        if (gridVO.getiDisplayStart() == null && gridVO.getiDisplayLength() == null && StringUtils.isBlank(gridVO.getSortDir()) 
        		&& StringUtils.isBlank(gridVO.getSortColName()) && ValidatorUtils.hasNoEntry(gridVO.getSearchMap())) {
            baseMasterEntities = baseMasterDao.getAllEntitiesForGridByStatus(entityClass, genericStatusList,
                    specificStatusList, getCurrentUser().getUserEntityId().getUri());
        } else {
            baseMasterEntities = baseMasterDao.getPaginatedEntitiesForGridByStatus(gridVO, entityClass, getCurrentUser().getUserEntityId().getUri(), 
            		genericStatusList, specificStatusList);
        }
        for (BaseMasterEntity bma : baseMasterEntities) {
            if (bma.getEntityLifeCycleData().getSystemModifiableOnly() == null
                    || !(bma.getEntityLifeCycleData().getSystemModifiableOnly())) {

                if(isDynamicWorkflow) {
                    setNonWorkflowEntityAction(dynamicWorkflowEntityClassName, bma, isAuthorizedMakerForEntity);
                } else {
                    setNonWorkflowEntityAction(entityClass.getSimpleName(), bma, isAuthorizedMakerForEntity);
                }
            }
            _idToEntities.put(bma.getId(), bma);
        }

        baseMasterEntities = setApplicableWorkFlowActions(isAuthorizedMakerForEntity, isAuthorizedCheckerForEntity,
                entityClass, userUri, baseMasterEntities, _idToEntities);
        return baseMasterEntities;

    
	}
	
	@Override
	public <T extends BaseMasterEntity> boolean hasEntity(Class<T> entityClass, Map<String, Object> propertyNameValueMap, Long objId){
	    return baseMasterDao.hasEntity(entityClass, propertyNameValueMap, objId);
	}


	@Override
	public <T extends BaseMasterEntity> List<T> getApprovedAndActiveEntityForAllPropertyNameValueMap(
			Class<T> entityClass, Map<String, Object> propertyNameValueMap) {
		  return baseMasterDao.getApprovedAndActiveEntityForAllPropertyNameAndValues(entityClass, propertyNameValueMap);
	}

	@Override
	public <T extends BaseMasterEntity> List<T> getApprovedAndActiveEntityForAnyMatchedPropertyNameValueMap(
			Class<T> entityClass, Map<String, Object> propertyNameValueMap) {
		return baseMasterDao.getApprovedAndActiveEntityForAnyMatchedPropertyNameAndValue(entityClass, propertyNameValueMap);
	}
	
	@Override
	public <T extends BaseMasterEntity> Long getSearchRecordsCount(GridVO gridVO, Class<T> entityClass, String userUri) {

		checkIfUserIdIsNotEqual(userUri);
        Boolean isAuthorizedMakerForEntity = loggedInUserisAnAuthorizedMakerForEntity(entityClass);
        List<Integer> genericStatusList = new ArrayList<>();
        genericStatusList = getStatusListBasedOnAuthority(genericStatusList, isAuthorizedMakerForEntity);
        String specificStatusList = Integer.toString(ApprovalStatus.UNAPPROVED_ADDED) + "," + ApprovalStatus.UNAPPROVED_MODIFIED;
        Long searchRecordsCount;
        //gridVO.getiDisplayLength() && gridVo.getiDisplayStart() should always be null in search records count.
        if (StringUtils.isBlank(gridVO.getSortDir())
        		&& StringUtils.isBlank(gridVO.getSortColName())	
        		&& ValidatorUtils.hasNoEntry(gridVO.getSearchMap())) {
        	searchRecordsCount = baseMasterDao.getAllEntitiesCountForGridByStatus(entityClass, genericStatusList,
                    specificStatusList, getCurrentUser().getUserEntityId().getUri());
        } else {
        	searchRecordsCount = baseMasterDao.getPaginatedEntitiesCountForGridByStatus(gridVO, entityClass, getCurrentUser().getUserEntityId().getUri(), 
            		genericStatusList, specificStatusList);
        }
		return searchRecordsCount;
	}
	
	private void checkIfUserIdIsNotEqual(String userUri) {
		EntityId userEntityId = EntityId.fromUri(userUri);
        if (!(getCurrentUser().getId().equals(userEntityId.getLocalId()))) {
            throw new SystemException("The user id :" + userEntityId.getLocalId()
                    + " requesting all entities. It does not match with logged in user's ID :" + getCurrentUser().getId());
        }
	}
}
