package com.nucleus.persistence;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Embedded;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import com.nucleus.core.sql.SqlUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ReflectionUtils;

import com.nucleus.approval.ProcessDrivenFlowStates;
import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.common.EntityUtil;
import com.nucleus.core.genericparameter.dao.GenericParameterDao;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.EntityUpdateInfo;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.makerchecker.MakerCheckerApprovalFlow;
import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.CriteriaMapVO;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;
import com.nucleus.task.TaskStatus;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

import net.bull.javamelody.MonitoredWithSpring;

@Named("baseMasterDao")
public class BaseMasterDaoImpl extends BaseDaoImpl<BaseMasterEntity> implements BaseMasterDao {

    private static final String BASE_MASTER_ENTITY    	= "BaseMasterEntity";
    private static final String RULE_SET_ENTITY       	= "RuleSet";
    private static final String ID    				  	= "id";
    private static final String DESCRIPTION    			= "description";
    private static final String SEARCH_MAP_KEY			= "searchMap";
    private static final String PAGINATED_QYERY_KEY		= "paginatedQuery";
    private static final String GENERIC_PARAMETER       	= "GenericParameter";
    private static final String USERS       	= "User";
    private static final String WORKFLOW_CONFIGURATION = "WorkflowConfiguration";
    private static final String COMMUNICATION_NAME = "CommunicationName";
    
    //To get the approved/edited versions of records fetched.
    private static final String ORDER_CLAUSE = ", s.entityLifeCycleData.uuid, s.id";
    
    //To get the user grid data excluding current logged in user.
    private static final String NOT_CURRENT_LOGGED_IN_USER = "AND s.username  !='";
    
    /**
     * this String (APPROVAL_STATUS_ORDER) is used to sort the records so that records with pending approval will come at top of the grid.
     */
    private static final String APPROVAL_STATUS_ORDER = "ORDER BY  CASE WHEN (s.masterLifeCycleData.approvalStatus='2' OR s.masterLifeCycleData.approvalStatus='6') " +
            "THEN 1 ELSE 2 END";
    @Inject
    @Named("neutrinoExecutionContextHolder")
    protected INeutrinoExecutionContextHolder         neutrinoExecutionContextHolder;
    
    @Inject
    @Named("genericParameterDao")
    private GenericParameterDao genericDao;

    @SuppressWarnings("unchecked")
    @Override
    public List<UnapprovedEntityData> getAllUnapproved(Class<? extends BaseMasterEntity> entityClass) {
        Query qry = getEntityManager().createNamedQuery("UnapprovedEntityData.allUnapprovedEntitiesByClass");
        qry.setParameter("entityClass", entityClass.getName() + "%");
        return DaoUtils.executeQuery(getEntityManager(), qry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UnapprovedEntityData> getAllUnapprovedVersionsOfEntity(EntityId entityId) {
        Query qry = getEntityManager()
                .createNamedQuery("UnapprovedEntityData.unapprovedVersionsOfEntityByOriginalEntityUri");
        qry.setParameter("originalEntityUri", entityId.getUri());
        return DaoUtils.executeQuery(getEntityManager(), qry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UnapprovedEntityData> getAllUnapprovedVersionsOfEntityByUUID(String uUID, BaseMasterEntity lastApprovedEntity) {
        String namedQuery = "";
        Query qry = null;
        if (lastApprovedEntity == null) {
            namedQuery = "UnapprovedEntityData.unapprovedVersionsOfEntityByUUID";
            qry = getEntityManager().createNamedQuery(namedQuery);
        } else {
            namedQuery = "UnapprovedEntityData.unapprovedVersionsOfEntityByUUIDAfterLastApproval";
            qry = getEntityManager().createNamedQuery(namedQuery);
            qry.setParameter("originalEntityUri", lastApprovedEntity.getUri());
        }
        qry.setParameter("refUUId", uUID);
        qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);

        List<UnapprovedEntityData> unapprovedEntityDatas = DaoUtils.executeQuery(getEntityManager(), qry);
        if (CollectionUtils.isEmpty(unapprovedEntityDatas) && lastApprovedEntity != null) {
            String lastApprovedEntityUri = getUriForLastApprovedEntity(lastApprovedEntity.getId(),
                    lastApprovedEntity.getClass());
            qry.setParameter("originalEntityUri", lastApprovedEntityUri);
            unapprovedEntityDatas = DaoUtils.executeQuery(getEntityManager(), qry);

        }
        return unapprovedEntityDatas;
    }

    @SuppressWarnings("rawtypes")
    private String getUriForLastApprovedEntity(Long id, Class clazz) {
        Class nameOfClass = getClassName(clazz);
        EntityId userEntityId = new EntityId(nameOfClass, id);
        return userEntityId.getUri();
    }

    @SuppressWarnings("rawtypes")
    private Class getClassName(Class clazz) {
        Class superClass = clazz.getSuperclass();
        if (superClass.getSimpleName().equalsIgnoreCase(BASE_MASTER_ENTITY)) {
            return clazz;
        } else {
            return getClassName(superClass);
        }

    }
    private UserInfo getCurrentUser() {
        
        UserInfo  userInfo= neutrinoExecutionContextHolder.getLoggedInUserDetails();
        if(userInfo!=null){
     	   return userInfo;
        }
        SecurityContext securityContext = SecurityContextHolder.getContext();
         if (securityContext != null && null != securityContext.getAuthentication()) {
             Object principal = securityContext.getAuthentication().getPrincipal();
             if (UserInfo.class.isAssignableFrom(principal.getClass())) {
                 userInfo = (UserInfo) principal;
             }
         }
         return userInfo;
     }
    @Override
    public List<Long> getEntityIdsForInProgressMakerChecker(Class<? extends BaseMasterEntity> entityClass) {
        Query qry = getEntityManager().createNamedQuery("MakerCheckerApprovalFlow.EntitiesByClassandState");
        qry.setParameter("entityClass", entityClass.getName() + "%");
        qry.setParameter("currentState", ProcessDrivenFlowStates.IN_PROGRESS);
        @SuppressWarnings("unchecked")
        List<MakerCheckerApprovalFlow> makerCheckerList = DaoUtils.executeQuery(getEntityManager(), qry);
        List<Long> changedEntityIdList = new ArrayList<Long>();
        for (MakerCheckerApprovalFlow makercheckerApprovalFlow : makerCheckerList) {
            EntityId entityId = EntityId.fromUri(makercheckerApprovalFlow.getChangedEntityUri());
            changedEntityIdList.add(entityId.getLocalId());
        }
        return changedEntityIdList;
    }

    @Override
    public <T extends BaseMasterEntity> List<T> getEntitiesByStatus(Class<T> entityClass, List<Integer> statusList) {
        if (entityClass == null || statusList == null) {
            return null;
        }
        String sortableField = "";
        String sortsubQuery = "";
        sortableField = EntityUtil.getSortableField(entityClass);
        if (sortableField != "") {
            sortsubQuery = "ORDER BY s." + sortableField + " ASC";
        }
        String qlString = "FROM "
                + entityClass.getName()
                + " s WHERE s.masterLifeCycleData.approvalStatus IN :approvalStatus AND (s.entityLifeCycleData.snapshotRecord is null or s.entityLifeCycleData.snapshotRecord = false) "
                + sortsubQuery;
        Query qry = getEntityManager().createQuery(qlString);
        qry.setParameter("approvalStatus", statusList);
        qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<T> result = DaoUtils.executeQuery(getEntityManager(), qry);
        return getVersionedList(entityClass, result);
    }
    @Override
    public <T> List<Map<String, Object>> getAllApprovedAndActiveSelectedListData(Class<T> entityClass,
            List<Integer> statusList, String... columnNameList) {
        StringBuilder dynamicQuery = new StringBuilder();
        String sortableField = "";
        String sortsubQuery = "";
        List<Map<String, Object>> applicationDataForTasks = null;
        String qlString = null;
        sortableField = getSortableField(entityClass);
        if (sortableField != "") {
            sortsubQuery = "ORDER BY lower(entityClass." + sortableField + ") ASC";
        }
        String QuerySelect = "Select new Map(entityClass.id as id";
        dynamicQuery.append(QuerySelect);
        if (columnNameList != null) {
            for (String columnName : columnNameList) {
                dynamicQuery.append(", ");
                dynamicQuery.append("entityClass.").append(columnName).append(" as ").append(columnName);
            }
        }
        String QueryFromWhere = ") " + "FROM " + entityClass.getName() + " entityClass WHERE ";

        dynamicQuery.append(QueryFromWhere);
        String dynamicQueryForBaseMaster = "entityClass.masterLifeCycleData.approvalStatus IN :approvalStatus  and entityClass.activeFlag = true and ";
        String dynamicQueryForBaseEntity = "(entityClass.entityLifeCycleData.snapshotRecord is null or entityClass.entityLifeCycleData.snapshotRecord = false)";
        String dynamicQueryForGenericParameter = "((entityClass.entityLifeCycleData.snapshotRecord is null or entityClass.entityLifeCycleData.snapshotRecord = false) and (entityClass.entityLifeCycleData.persistenceStatus = 0))";
        if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
            qlString = dynamicQuery.append(dynamicQueryForBaseMaster).append(dynamicQueryForBaseEntity).append(sortsubQuery)
                    .toString();
            JPAQueryExecutor<Map<String, Object>> jpaQueryExecutor = new JPAQueryExecutor<Map<String, Object>>(qlString);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            applicationDataForTasks = executeQuery(jpaQueryExecutor.addParameter("approvalStatus", statusList));

        }
        // This is done for generic parameter to show only those records that has persistence status =
        // PersistenceStatus.Active = 0
        else if (GenericParameter.class.isAssignableFrom(entityClass)) {
            qlString = dynamicQuery.append(dynamicQueryForGenericParameter).append(sortsubQuery).toString();
            JPAQueryExecutor<Map<String, Object>> jpaQueryExecutor = new JPAQueryExecutor<Map<String, Object>>(qlString);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            applicationDataForTasks = executeQuery(jpaQueryExecutor);
        } else {
            qlString = dynamicQuery.append(dynamicQueryForBaseEntity).append(sortsubQuery).toString();
            JPAQueryExecutor<Map<String, Object>> jpaQueryExecutor = new JPAQueryExecutor<Map<String, Object>>(qlString);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            applicationDataForTasks = executeQuery(jpaQueryExecutor);
        }

        return applicationDataForTasks;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseMasterEntity> List<T> getAllApprovedAndActiveEntities(Class<T> entityClass,List<Integer> statusList) {
    	if (entityClass == null || statusList == null) {
            return null;
        }
        String sortableField = "";
        String sortsubQuery = "";
        sortableField = EntityUtil.getSortableField(entityClass);
        if (sortableField != "") {
            sortsubQuery = "ORDER BY s." + sortableField + " ASC";
        }

        String qlString = "FROM "
                + entityClass.getName()
                + " s WHERE s.masterLifeCycleData.approvalStatus IN :approvalStatus  and s.activeFlag = true and (s.entityLifeCycleData.snapshotRecord is null or s.entityLifeCycleData.snapshotRecord = false) "
                + sortsubQuery;
        Query qry = getEntityManager().createQuery(qlString);
        qry.setParameter("approvalStatus", statusList);
        qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, isQueryCacheApplicableOrNot(entityClass));
        return DaoUtils.executeQuery(getEntityManager(), qry);
    }
    
    
	@Override
	@Deprecated
	public <T extends BaseMasterEntity> List<T> getAllApprovedAndActiveEntities(Class<T> entityClass,List<Integer> statusList, boolean isQueryCacheEnabled) {
    	 return getAllApprovedAndActiveEntities(entityClass, statusList);
	}
    
    
    private <T> String getSortableField(Class<T> entityClassName) {
        String sortableField = "";
        if (entityClassName != null) {
            Field[] fields = EntityUtil.getAllDeclaredFields(entityClassName, true, 0);
            if (fields != null) {
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Sortable.class)) {
                        field.setAccessible(true);
                        sortableField = field.getName();
                    }
                }
            }
        }

        return sortableField;

    }

    @Override
    public <T> List<Map<String, Object>> getAllApprovedAndActiveSelectedListData(Class<T> entityClass,
            List<Integer> statusList,Map<String, Object> whereList,String... columnNameList) {
        StringBuilder dynamicQuery = new StringBuilder();
        String sortableField = "";
        String sortsubQuery = "";
        List<Map<String, Object>> applicationDataForTasks = null;
        String qlString = null;
        sortableField = getSortableField(entityClass);

        if (sortableField != "") {
            sortsubQuery = " ORDER BY lower(entityClass." + sortableField + ") ASC";
        }
        String QuerySelect = "Select new Map(entityClass.id as id";
        dynamicQuery.append(QuerySelect);
        if (columnNameList != null) {
            for (String columnName : columnNameList) {
                dynamicQuery.append(", ");
                dynamicQuery.append("entityClass.").append(columnName).append(" as ").append(columnName);
            }
        }
        String QueryFromWhere = ") " + "FROM " + entityClass.getName() + " entityClass WHERE ";
        
        dynamicQuery.append(QueryFromWhere);
        String dynamicQueryForBaseMaster = "entityClass.masterLifeCycleData.approvalStatus IN :approvalStatus  and entityClass.activeFlag = true and ";
        String dynamicQueryForBaseEntity = "(entityClass.entityLifeCycleData.snapshotRecord is null or entityClass.entityLifeCycleData.snapshotRecord = false)";
        String dynamicQueryForGenericParameter = "((entityClass.entityLifeCycleData.snapshotRecord is null or entityClass.entityLifeCycleData.snapshotRecord = false) and (entityClass.entityLifeCycleData.persistenceStatus = 0))";
        
        if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
            dynamicQuery.append(dynamicQueryForBaseMaster).append(dynamicQueryForBaseEntity);
        }
        else if (GenericParameter.class.isAssignableFrom(entityClass)) {
            dynamicQuery.append(dynamicQueryForGenericParameter);
        } else {
            dynamicQuery.append(dynamicQueryForBaseEntity);
        }   
        if(whereList!=null && !whereList.isEmpty()){
			for(Entry<String, Object> s:whereList.entrySet()){
        		dynamicQuery.append(" and entityClass.").append(s.getKey()).append("=:").append(s.getKey());
        	}
        }
        qlString = dynamicQuery.append(sortsubQuery).toString();
        JPAQueryExecutor<Map<String, Object>> jpaQueryExecutor = new JPAQueryExecutor<>(qlString);
        if(whereList!=null && !whereList.isEmpty()){
			for(Entry<String, Object> s:whereList.entrySet()){
				jpaQueryExecutor.addParameter(s.getKey(), s.getValue());
        	}
        }
        if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            try{
            applicationDataForTasks = executeQuery(jpaQueryExecutor.addParameter("approvalStatus", statusList));
            }catch (Exception e) {
                BaseLoggers.exceptionLogger.error(e.getMessage(), e);
			}

        }
        // This is done for generic parameter to show only those records that has persistence status =
        // PersistenceStatus.Active = 0
        else /*if (GenericParameter.class.isAssignableFrom(entityClass)) {
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            applicationDataForTasks = executeQuery(jpaQueryExecutor);
        } else*/ {
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            applicationDataForTasks = executeQuery(jpaQueryExecutor);
        }       
        return applicationDataForTasks;
    }

    @Override
    public BaseMasterEntity getLastApprovedEntityByUnapprovedEntityId(EntityId entityId) {
        if (entityId == null) {
            return null;
        }
        BaseEntity unApprovedEntity = (BaseEntity) find(entityId.getEntityClass(), entityId.getLocalId());
        String qlString = "select s FROM " + entityId.getEntityClass().getName() + " s "
                + " inner join s.entityLifeCycleData eld"
                + " WHERE eld.uuid = :uuid and s.masterLifeCycleData.approvalStatus IN :approvalStatusList";
        Query qry = getEntityManager().createQuery(qlString);
        qry.setParameter("uuid", unApprovedEntity.getEntityLifeCycleData().getUuid());
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        qry.setParameter("approvalStatusList", statusList);
        BaseMasterEntity approvedEntity = null;
        if (qry.getResultList().size() > 0) {
            approvedEntity = (BaseMasterEntity) DaoUtils.executeQuery(getEntityManager(), qry).get(0);
        }
        return approvedEntity;

    }
    
    /**
     * Deprecated due to performance cost.
     * 
     */
    @Override
    @Deprecated
	public <T extends BaseMasterEntity> List<Object[]> getEntitiesWithActionsByStatus(Class<T> entityClass,
			List<Integer> statusList, List<String> authCodes) {
		return getEntitiesWithActionsByStatus(entityClass, statusList, authCodes, null);
	}

    @Override
    public <T extends BaseMasterEntity> List<Object[]> getEntitiesWithActionsByStatus(Class<T> entityClass,
            List<Integer> statusList, List<String> authCodes, List<String> uuids) {
        StringBuilder sb = new StringBuilder();
        sb.append("select s,t FROM ")
          .append(entityClass.getName())
          .append(" s  inner join  s.entityLifeCycleData eld  , ApprovalTask t ")
          .append(" WHERE ((s.masterLifeCycleData.approvalStatus IN (:approvalStatusList)) and ");
        if (uuids != null && !uuids.isEmpty()) {
        	sb.append("eld.uuid in (:uuids) and ");
        } 
        sb.append(" ((eld.uuid =t.refUUId and t.makerCheckerAssigneeUri IN (:assigneeAuthCodes) and t.taskStatus= :taskStatus) OR (t IS NULL))) ORDER BY t.createDate");	
        Query qry = getEntityManager().createQuery(sb.toString());
        qry.setParameter("approvalStatusList", statusList);
        qry.setParameter("assigneeAuthCodes", authCodes);
        qry.setParameter("taskStatus", TaskStatus.PENDING);
        if (uuids != null && !uuids.isEmpty()) {
            qry.setParameter("uuids", uuids);
        }
        qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        return DaoUtils.executeQuery(getEntityManager(), qry);
    }

    @Override
    public <T extends BaseMasterEntity> List<Object[]> getEntitiyWithActionsByStatus(Class<T> entityClass,
            List<Integer> statusList, List<String> authCodes, Long enId) {

        String qlString = "select s,t FROM "
                + entityClass.getName()
                + " s "
                + " inner join  s.entityLifeCycleData eld  , ApprovalTask t "
                + " WHERE ((s.id =:enid) and (s.masterLifeCycleData.approvalStatus IN (:approvalStatusList)) and ((eld.uuid =t.refUUId and t.makerCheckerAssigneeUri IN (:assigneeAuthCodes) and t.taskStatus= :taskStatus) OR (t IS NULL)))";
        Query qry = getEntityManager().createQuery(qlString);
        qry.setParameter("enid", enId);
        qry.setParameter("approvalStatusList", statusList);
        qry.setParameter("assigneeAuthCodes", authCodes);
        qry.setParameter("taskStatus", TaskStatus.PENDING);
        return DaoUtils.executeQuery(getEntityManager(), qry);
    }

    @Override
    public boolean updateReferences(List<EntityUpdateInfo> entityUpdateInfos, BaseEntity intialEntity,
            BaseEntity lastUpdateEntity) {
        for (EntityUpdateInfo entityUpdateInfo : entityUpdateInfos) {
            String entityName = entityUpdateInfo.getUpdateEntityName();
            String fieldName = entityUpdateInfo.getUpdateFieldName();
            String approvalStatus = entityUpdateInfo.getIncludeApprovalStatuses();
            String qlString = "UPDATE " + entityName + " s SET s." + fieldName
                    + "= :newIdValue WHERE s.masterLifeCycleData.approvalStatus =:apStat";
            Query qry = getEntityManager().createQuery(qlString);
            qry.setParameter("newIdValue", lastUpdateEntity);
            qry.setParameter("apStat", new Integer(approvalStatus));

            qry.executeUpdate();
        }
        return true;
    }

    @Override
    public <T extends BaseMasterEntity> Boolean hasEntity(Class<T> entityClass, String propertyName, Object propertyValue) {
        if (entityClass == null || propertyName == null) {
            return null;
        }
        Boolean hasEntity = false;
        StringBuilder queryBuilder = new StringBuilder("select count(*) FROM ");
        queryBuilder.append(entityClass.getName())
                .append(" s  WHERE s.")
                .append(propertyName)
                .append(" =:propValue and s.masterLifeCycleData.approvalStatus IN :approvalStatusList");

        Query qry = getEntityManager().createQuery(queryBuilder.toString());
        qry.setParameter("propValue", propertyValue);
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED);
        qry.setParameter("approvalStatusList", statusList);
        qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        List<Long> countList = DaoUtils.executeQuery(getEntityManager(), qry);
        if (countList.get(0) > 0L) {
            hasEntity = true;
        }
        return hasEntity;
    }

    @Override
    public <T extends BaseMasterEntity> Boolean hasEntity(Class<T> entityClass, Map<String, Object> propertyValueMap, Long id) {
        if (entityClass == null || propertyValueMap == null) {
            return null;
        }
        Boolean hasEntity = false;

        // To get approved entity using entity id of unapproved-modified entity.
        BaseMasterEntity baseMasterEntity = null;
        String qlString = "select count(*) FROM " + entityClass.getName() + " s ";
        if (propertyValueMap != null && propertyValueMap.size() > 0) {
            Iterator<Entry<String, Object>> it = propertyValueMap.entrySet().iterator();
            int counter = 1;
            Map<String,Object> parameterMap = new HashMap<>();
            while (it.hasNext()) {
                Entry<String, Object> entryObj = it.next();
                String propertyName = entryObj.getKey();
                Object propertyValue = entryObj.getValue();
                String propertyBindVar = propertyName.replace('.', '_');
                if (counter == 1) {
                    qlString += " WHERE lower(s."
                            + propertyName
                            
                            + ") = lower(:"
                            + propertyName
                            + ") and s.masterLifeCycleData.approvalStatus IN :approvalStatusList and ( s.entityLifeCycleData.snapshotRecord IS NULL "
                            + "OR s.entityLifeCycleData.snapshotRecord = false )";
                } else {
                    qlString += " and lower(s." + propertyName + ") = lower(:" + propertyBindVar + ")";
                }
                counter++;
                parameterMap.put(propertyBindVar, propertyValue);
            }

            // Check if creating or editing an entity (Avoid checking of duplicate columns with itself
            // if editing an entity)
            if (id != null) {
                qlString += " and id != :id";
                baseMasterEntity = getLastApprovedEntityByUnapprovedEntityId(new EntityId(entityClass, id));
                parameterMap.put("id", id);
            }

            // To avoid checking for duplicate columns against original entity of this UNAPPROVED_MODIFIED entity.
            if (baseMasterEntity != null) {
                qlString += " and s.entityLifeCycleData.uuid != :uuid";
                parameterMap.put("uuid", baseMasterEntity.getEntityLifeCycleData().getUuid());
            }
            Query qry = getEntityManager().createQuery(qlString);
            List<Integer> statusList = new ArrayList<Integer>();
            statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
            statusList.add(ApprovalStatus.APPROVED_MODIFIED);
            statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
            statusList.add(ApprovalStatus.APPROVED_DELETED);
            statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
            statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
            statusList.add(ApprovalStatus.APPROVED);
            parameterMap.put("approvalStatusList", statusList);
            setQueryBindParameters(qry,parameterMap);
            qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
            List<Long> countList = DaoUtils.executeQuery(getEntityManager(), qry);
            if (countList.get(0) > 0L) {
                hasEntity = true;
            }
        }
        return hasEntity;
    }

	private void setQueryBindParameters(Query qry, Map<String, Object> parameterMap) {
		for (Entry<String, Object> entry : parameterMap.entrySet()) {
			qry.setParameter(entry.getKey(), entry.getValue());
		}

	}

	@Override
    public <Q extends BaseMasterEntity> List<Q> getPaginatedEntitiesForGridByStatus(Class<Q> entityClass,
            List<Integer> genericStatusList, String specificStatusList, String viewerUri, Integer iDisplayStart,
            Integer iDisplayLength, String sortDir, String sortColName) {
        GridVO gridVO = new GridVO();
        gridVO.setiDisplayStart(iDisplayStart);
        gridVO.setiDisplayLength(iDisplayLength);
        gridVO.setSortDir(sortDir);
        gridVO.setSortColName(sortColName);
        gridVO.setSearchMap(null);
        return getPaginatedEntitiesForGridByStatus(gridVO ,entityClass, viewerUri ,genericStatusList, specificStatusList);
    }

    public Integer getWorkflowTotalRecords(Class<Serializable> entityClass, List<Integer> statusList, boolean isDynamicWorkflow) {

        if (entityClass == null || statusList == null) {
            return 0;
        }
        String qlString = totalRecordsQuery(entityClass);

        if(WORKFLOW_CONFIGURATION.equalsIgnoreCase(entityClass.getSimpleName())) {
            qlString = qlString + " AND s.isDynamicWorkflow =:isDynamicWorkflow AND s.entityLifeCycleData.persistenceStatus = 0 AND s.sourceProduct =:productCode ";
        }

        Query qry = getEntityManager().createQuery(qlString);
        qry.setParameter("approvalStatus", statusList);
        
        if(GENERIC_PARAMETER.equalsIgnoreCase(entityClass.getSimpleName())){
            qry.setParameter("genericParameterTypes", getGenericParameterTypes());
        }
        
        qry.setParameter("emptyParentPersistenceStatus", PersistenceStatus.EMPTY_PARENT);
        
        if(WORKFLOW_CONFIGURATION.equalsIgnoreCase(entityClass.getSimpleName())) {
            qry.setParameter("isDynamicWorkflow", isDynamicWorkflow);
            qry.setParameter("productCode", ProductInformationLoader.getProductCode());        
        }
        
        qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        @SuppressWarnings("unchecked")
        List<Long> countList = DaoUtils.executeQuery(getEntityManager(), qry);
        return countList.get(0).intValue();
    }


	
    
	@Override
	@MonitoredWithSpring(name = "BMDI_FETCH_NOTES_FOR_ENTITY_WATCH_BY_USER_AFTER_NOTE")
	public int getTotalRecords(Class<Serializable> entityClass, List<Integer> statusList) {
		if (entityClass == null || statusList == null) {
			return 0;
		}
		
		String qlString = totalRecordsQuery(entityClass);
		if (USERS.equalsIgnoreCase(entityClass.getSimpleName())) {
			UserInfo user = getCurrentUser();
			
			StringBuilder clause=new StringBuilder(qlString);
			qlString = clause.append( NOT_CURRENT_LOGGED_IN_USER ).append(user.getUsername().toLowerCase()).append("\' ").toString();
			
			
		}
		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter("approvalStatus", statusList);

		if (GENERIC_PARAMETER.equalsIgnoreCase(entityClass.getSimpleName())) {
			qry.setParameter("genericParameterTypes", getGenericParameterTypes());
		}

		

		qry.setParameter("emptyParentPersistenceStatus", PersistenceStatus.EMPTY_PARENT);

		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
		@SuppressWarnings("unchecked")
		List<Long> countList = DaoUtils.executeQuery(getEntityManager(), qry);
		int size = countList.get(0).intValue();
		return size;
	}


    private String totalRecordsQuery(Class<Serializable> entityClass) {

        String qlString = "Select count(*) FROM "
                + entityClass.getName()
                + " s WHERE s.masterLifeCycleData.approvalStatus IN :approvalStatus AND (s.entityLifeCycleData.snapshotRecord is null or s.entityLifeCycleData.snapshotRecord = false)"
                + " AND (s.entityLifeCycleData.persistenceStatus !=:emptyParentPersistenceStatus) ";


        //Only for RuleSet Master, needs to update in the future
        if (RULE_SET_ENTITY.equalsIgnoreCase(entityClass.getSimpleName())) {
            qlString = getQueryForRuleSetMaster(qlString, entityClass.getName());
        }
        if(GENERIC_PARAMETER.equalsIgnoreCase(entityClass.getSimpleName())){
            qlString = qlString + "AND s.class IN (:genericParameterTypes) ";
        }
        
        return qlString;
    }

    // Method to get the approved/edited versions of records fetched.
    @Override
    @SuppressWarnings("unchecked")
    public <Q extends BaseMasterEntity> List<Q> getVersionedList(Class<Q> entityClass, List<Q> records) {

        List<Q> versionedRecords = new ArrayList<Q>();
        Map<String, Q> versionedRecordsMap=new HashMap<String, Q>();
        Map<String, Q> recordsMap=new HashMap<String, Q>();
        for (BaseMasterEntity bme : records) {
        	recordsMap.put(bme.getUri(), (Q) bme);
        }
        String qlString = "";
        for (BaseMasterEntity bme : records) {
        	String bmeUri=bme.getUri();
            if (versionedRecordsMap.get(bmeUri) == null) {
            	versionedRecordsMap.put(bmeUri, (Q) bme);
                versionedRecords.add((Q) bme);
                Integer approvalStatusCondtion=null;
                if (bme.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
                	qlString = "SELECT s FROM " + entityClass.getName() + " s WHERE s.entityLifeCycleData.uuid = :uuid AND s.masterLifeCycleData.approvalStatus = :approvalStatus";
                	approvalStatusCondtion=ApprovalStatus.UNAPPROVED_MODIFIED;
                } else if (bme.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
                	qlString = "SELECT s FROM " + entityClass.getName() + " s WHERE s.entityLifeCycleData.uuid = :uuid AND s.masterLifeCycleData.approvalStatus = :approvalStatus";
                	approvalStatusCondtion=ApprovalStatus.APPROVED_MODIFIED;
				}
                if (!qlString.equalsIgnoreCase("")) {
                    JPAQueryExecutor<Q> jPAQueryExecutor = new JPAQueryExecutor<Q>(qlString);
                    jPAQueryExecutor.addParameter("uuid",bme.getEntityLifeCycleData().getUuid());
                    jPAQueryExecutor.addParameter("approvalStatus",approvalStatusCondtion);
                    jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
                    jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
                    BaseMasterEntity versionedRecord = executeQueryForSingleValue(jPAQueryExecutor);
                    if (versionedRecord != null && recordsMap.get(versionedRecord.getUri()) == null) {
                    	versionedRecordsMap.put(versionedRecord.getUri(), (Q) versionedRecord);
                        versionedRecords.add((Q) versionedRecord);
                    }  else if (versionedRecord != null && recordsMap.get(versionedRecord.getUri())!=null) {
                    	BaseMasterEntity baseMasterEntity=recordsMap.get(versionedRecord.getUri());
                    	versionedRecordsMap.put(baseMasterEntity.getUri(), (Q) baseMasterEntity);
                        versionedRecords.add((Q) baseMasterEntity);
                    }
                    qlString = "";
                }
            }

        }
        return versionedRecords;

    }

    @Override
    public <T extends BaseMasterEntity> List<T> getAllEntitiesForGridByStatus(Class<T> entityClass,
            List<Integer> genericStatusList, String specificStatusList, String viewerUri) {
    	boolean needOfEntitiesCountOnly = false;
		String countQuery = getAllEntitiesQuery(entityClass, genericStatusList, specificStatusList, viewerUri, needOfEntitiesCountOnly);
        Query qry = getEntityManager().createQuery(countQuery);
        qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        List<T> result = DaoUtils.executeQuery(getEntityManager(), qry);
        return getVersionedList(entityClass, result);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap) {
        return findMasterByCode(entityClass, variablesMap, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap, boolean andCondition) {
        NeutrinoValidator.notNull(entityClass, "Application Id can not be null for upadting persistence status.");
        String qlString = "FROM "
                + entityClass.getSimpleName()
                + " masterEntity WHERE "
                + "(masterEntity.entityLifeCycleData.snapshotRecord is null OR masterEntity.entityLifeCycleData.snapshotRecord = false)";
        StringBuilder dynamicQuery = new StringBuilder(qlString);
        
        Map<String,Object> parameterMap = new HashMap<>();
        int count=0;
        
        StringBuilder paramName=new StringBuilder();
        if (ValidatorUtils.hasAnyEntry(variablesMap)) {
            for (Entry<String, Object> paramEntry : variablesMap.entrySet()) {
                paramName.append("param"+count);
                if(andCondition) {
                    dynamicQuery.append(" AND ");
                } else{
                    dynamicQuery.append(" OR ");
                }
                if(ValidatorUtils.notNull(paramEntry)
                        && ValidatorUtils.notNull(paramEntry.getValue())
                        && List.class.isAssignableFrom(paramEntry.getValue().getClass())){
                    dynamicQuery.append("masterEntity." + paramEntry.getKey() + " IN :"+paramName);
                }else{
                    dynamicQuery.append("masterEntity." + paramEntry.getKey() + " = :"+paramName);
                }
                parameterMap.put(paramName.toString(), (Object) paramEntry.getValue());
                paramName.setLength(0);
                count++;
            }
        }

        Query qry = getEntityManager().createQuery(dynamicQuery.toString()).setHint(QueryHint.QUERY_HINT_CACHEABLE,Boolean.TRUE);
        
        for (Entry<String, Object> entry : parameterMap.entrySet()) {
            qry.setParameter(entry.getKey(), entry.getValue());
        }

        List<T> entities = DaoUtils.executeQuery(getEntityManager(), qry);
        if (CollectionUtils.isNotEmpty(entities)) {
            return entities.get(0);
        }
        return null;
    }


        @Override
    public <T extends BaseMasterEntity> Boolean hasApprovedEntity(Class<T> entityClass, String propertyName,
            Object propertyValue) {
        if (entityClass == null || propertyName == null) {
            return null;
        }
        Boolean hasEntity = false;

        StringBuilder queryBuilder = new StringBuilder("select count(*) FROM ");
        queryBuilder.append(entityClass.getName())
                .append(" s WHERE s.")
                .append(propertyName)
                .append(" =:propValue and s.masterLifeCycleData.approvalStatus IN :approvalStatusList");

        Query qry = getEntityManager().createQuery(queryBuilder.toString());
        qry.setParameter("propValue", propertyValue);
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        qry.setParameter("approvalStatusList", statusList);
        qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        List<Long> countList = DaoUtils.executeQuery(getEntityManager(), qry);
        if (countList.get(0) > 0L) {
            hasEntity = true;
        }
        return hasEntity;
    }
    
       
    @Override
    public <T extends BaseMasterEntity> List<T> getApprovedAndActiveEntityForAllPropertyNameAndValues(Class<T> entityClass,Map<String,Object> propertyNameValueMap){
    	validateEntityClassAndPropertyNameValueMap(entityClass,propertyNameValueMap);
    	StringBuilder queryString=new StringBuilder();
    	Map<String,Object> paramKeyValueMap=new HashMap<String,Object>();
    	queryString.append("select s FROM " + entityClass.getName() + " s " + " WHERE ");  
    	int i=0;
    	for (Map.Entry<String,Object> entry : propertyNameValueMap.entrySet())
    	{
    		if(propertyNameValueMap.size()>1 && i!=propertyNameValueMap.size()-1){	
    			queryString.append("s."+entry.getKey()+ " = :param"+i+" and "); 
    		}else{
    			queryString.append("s."+entry.getKey()+ " = :param"+i); 
    		}
    		paramKeyValueMap.put("param"+i, entry.getValue());
    		i++;
    	} 
    	
    	
    	queryString.append(" and s.masterLifeCycleData.approvalStatus IN :approvalStatusList and s.activeFlag = true ");
        Query qry = getEntityManager().createQuery(queryString.toString());
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        qry.setParameter("approvalStatusList", statusList);
        for (Map.Entry<String,Object> entry : paramKeyValueMap.entrySet())
    	{
        	 qry.setParameter(entry.getKey(), entry.getValue());
    	}
        qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);      
        return DaoUtils.executeQuery(getEntityManager(), qry);
    }
    
   public <T> void validateEntityClassAndPropertyNameValueMap(Class<T> entityClass,Map<String,Object> propertyNameValueMap){
	   if (ValidatorUtils.isNull(entityClass)) {
   		throw ExceptionBuilder
           .getInstance(ServiceInputException.class)
           .setExceptionCode("invalid.entityClass")
           .setMessage(
                   new Message("invalid.entityClass", MessageType.ERROR))
           .setLogMessage("Entity Class cannot be null.").build();
       }
   	
   	if(MapUtils.isEmpty(propertyNameValueMap)){
   		throw ExceptionBuilder
           .getInstance(ServiceInputException.class)
           .setExceptionCode("invalid.propertyNameValueMap")
           .setMessage(
                   new Message("invalid.entityClass", MessageType.ERROR))
           .setLogMessage("PropertyNameValueMap cannot be null.").build();
   	}
    }
    
    @Override
    public <T extends BaseMasterEntity> List<T> getApprovedAndActiveEntityForAnyMatchedPropertyNameAndValue(Class<T> entityClass,Map<String,Object> propertyNameValueMap){
    	validateEntityClassAndPropertyNameValueMap(entityClass,propertyNameValueMap);
    	StringBuilder queryString=new StringBuilder();
    	Map<String,Object> paramKeyValueMap=new HashMap<String,Object>();
    	queryString.append("select s FROM " + entityClass.getName() + " s " + " WHERE ");  
    	int i=0;
    	for (Map.Entry<String,Object> entry : propertyNameValueMap.entrySet())
    	{
    		if(propertyNameValueMap.size()>1 && i!=propertyNameValueMap.size()-1){	
    			queryString.append("s."+entry.getKey()+ " = :param"+i+" or "); 
    		}else{
    			queryString.append("s."+entry.getKey()+ " = :param"+i); 
    		}
    		paramKeyValueMap.put("param"+i, entry.getValue());
    		i++;
    	}    	
    	queryString.append(" and s.masterLifeCycleData.approvalStatus IN :approvalStatusList and s.activeFlag = true ");
        Query qry = getEntityManager().createQuery(queryString.toString());
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        qry.setParameter("approvalStatusList", statusList);
        for (Map.Entry<String,Object> entry : paramKeyValueMap.entrySet())
    	{
        	 qry.setParameter(entry.getKey(), entry.getValue());
    	}
        qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);      
        return DaoUtils.executeQuery(getEntityManager(), qry);
    }
    
    @Override
    public Long getApprovalTaskIdByRefUUID(String  uuid){
        
        Long taskId=null;
        
        Query namedQueryScheme=getEntityManager().createNamedQuery("getApprovalTaskIdbyRefUUID");
        namedQueryScheme.setParameter("uuid", uuid);
        
        List<Long> approvalTaskIdsList = namedQueryScheme.getResultList();
        
        if(approvalTaskIdsList!=null && approvalTaskIdsList.size()>0){
            taskId=approvalTaskIdsList.get(0);
        }
        return taskId;
    }
    
    private Class<?> getSortColumnType(Class<?> entityClass, String sortColumnName){
        
        String[] sortColFields=null;
        Class<?> sortColType=entityClass;
        if(!(sortColumnName==null || ("").equals(sortColumnName))){
            sortColFields = sortColumnName.split("\\.");
            for(String sortColField : sortColFields){
                sortColType = ReflectionUtils.findField(sortColType, sortColField).getType();
            }
        }
        
        return sortColType;
    }
        @Override
    public <T extends BaseMasterEntity> List<T> findAllBaseParameter(Class<T> genericEntityClassName, boolean forUpdate) {
        NeutrinoValidator.notNull(genericEntityClassName, "Generic Parameter Class Name can't be null");
        String sortableField = "";
        sortableField = EntityUtil.getSortableField(genericEntityClassName);
        if (sortableField != "") {
        }
        String qlString = "FROM "
                + genericEntityClassName.getSimpleName()
                + " s WHERE ((s.entityLifeCycleData.snapshotRecord is null OR s.entityLifeCycleData.snapshotRecord = false) AND s.entityLifeCycleData.persistenceStatus = 0))";
               
        Query qry = getEntityManager().createQuery(qlString);
        if (!forUpdate) {
            qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
            qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
            qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        }
        List<T> entities = DaoUtils.executeQuery(getEntityManager(), qry);
        return entities;
    }

    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseMasterEntity> List<T> findAllBaseParameter(Class<T> genericEntityClassName) {
        return findAllBaseParameter(genericEntityClassName, false);
    }
    

     @Override
      public <T extends Entity> List<T> retrieveMasterForMobileFlagY(Class<T> entityClass, String authorizationBusinessDate) {
       
       List<Integer> statusList = new ArrayList<Integer>();
       statusList.add(ApprovalStatus.APPROVED);
       statusList.add(ApprovalStatus.APPROVED_MODIFIED);
       statusList.add(ApprovalStatus.APPROVED_DELETED);
       
        NeutrinoValidator.notNull(entityClass, "Entity Class Name can't be null");
        String queryString = "FROM " + entityClass.getSimpleName() + " masterEntity WHERE " 
                                     + "masterEntity.offlineFlag=true AND masterEntity.activeFlag = true " 
                                     + "AND masterEntity.masterLifeCycleData.approvalStatus IN :approvalStatus";
        

        if (notNull(authorizationBusinessDate) && !"".equals(authorizationBusinessDate))
          queryString += " AND masterEntity.entityLifeCycleData.lastUpdatedTimeStamp >=:authorizationBusinessDate";

           Query query = getEntityManager().createQuery(queryString);
            query.setParameter("approvalStatus", statusList);
        
        if (notNull(authorizationBusinessDate)) 
          query.setParameter("authorizationBusinessDate", new DateTime(authorizationBusinessDate, org.joda.time.DateTimeZone.UTC));
        
        List<T> entities =DaoUtils.executeQuery(getEntityManager(), query);
        return entities;
      }

     @Override
      public <T extends BaseEntity> List<T> findAllBaseEntitiesParameter(Class<T> genericEntityClassName, boolean forUpdate, String authorizationBusinessDate) {
       
       List<Integer> statusList = new ArrayList<Integer>();
       statusList.add(ApprovalStatus.APPROVED);
       statusList.add(ApprovalStatus.APPROVED_MODIFIED);
       statusList.add(ApprovalStatus.APPROVED_DELETED);
       
        NeutrinoValidator.notNull(genericEntityClassName, "Generic Parameter Class Name can't be null");
        String qlString = "FROM " + genericEntityClassName.getSimpleName() + " s WHERE (s.offlineFlag = true and s.isActive = 'y')";
        
        if (notNull(authorizationBusinessDate) && !"".equals(authorizationBusinessDate))
          qlString += " AND s.entityLifeCycleData.lastUpdatedTimeStamp >=:authorizationBusinessDate";
        
        Query qry = getEntityManager().createQuery(qlString);
        
        if (notNull(authorizationBusinessDate)) 
          qry.setParameter("authorizationBusinessDate", new DateTime(authorizationBusinessDate, org.joda.time.DateTimeZone.UTC));
        
        List<T> entities = DaoUtils.executeQuery(getEntityManager(), qry);
        return entities;
      }

     
    private String getLeftOuterJoinQuery(Class<?> entityClass, String[] joinColumnList,Map<String, String> processedJoinColumns) {
        StringBuilder finalQuery = new StringBuilder();
        StringBuilder leftOuterJoinMapping = new StringBuilder("s.");
        
        for (int i = 0; i < joinColumnList.length - 1; i++) {
                Field field =ReflectionUtils.findField(entityClass, joinColumnList[i]);
                if(field==null)
                {
                	break;
                }
                if (ValidatorUtils.notNull(field) && field.isAnnotationPresent(Embedded.class)) {
                      break;
                }
                leftOuterJoinMapping.append(field.getName());
                String alias=leftOuterJoinMapping.toString().replace('.','_');
                if (ValidatorUtils.notNull(field) &&!processedJoinColumns.containsKey(leftOuterJoinMapping.toString())) {
                	processedJoinColumns.put(leftOuterJoinMapping.toString(), alias);
                	
                	finalQuery.append(" left outer join " + leftOuterJoinMapping.toString() + " " + alias);
                    
                }
                
                
            
        }
        return finalQuery.toString();
    }
    
    @Override
    public <Q extends BaseMasterEntity> List<Q> getPaginatedEntitiesForGridByStatus(GridVO gridVO, Class<Q> entityClass, String viewerUri,
            List<Integer> genericStatusList, String specificStatusList) {//0,2,3,7,12,4,6
        
    	Map<String, Object> searchParametersAndQueryMap = getPaginatedEntityQueryAndSearchMap(gridVO, entityClass, viewerUri, genericStatusList, specificStatusList);
		String entityQuery = (String) searchParametersAndQueryMap.get(PAGINATED_QYERY_KEY);
		@SuppressWarnings("unchecked")
		Map<String, Object> searchMap = (Map<String, Object>) searchParametersAndQueryMap.get(SEARCH_MAP_KEY);
        JPAQueryExecutor<Q> jPAQueryExecutor = new JPAQueryExecutor<Q>(entityQuery);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        addSearchParameters(jPAQueryExecutor, searchMap, gridVO.isContainsSearchEnabled());
        return executeQuery(jPAQueryExecutor, gridVO.getiDisplayStart(), gridVO.getiDisplayLength());

    }
    private <Q> void addSearchParameters(JPAQueryExecutor<Q> jPAQueryExecutor, Map<String, Object> searchMap, boolean containsSearchEnabled) {

    	
    	
    	if (ValidatorUtils.hasNoEntry(searchMap)) {
    		return ;
    	}
    	for (Map.Entry<String, Object> entry : searchMap.entrySet()) {
    		if (containsSearchEnabled) {
    		    if(entry.getValue() instanceof Integer) {
                    jPAQueryExecutor.addParameter(getSearchParam(entry.getKey()), entry.getValue());
                } else if(entry.getKey().equals("paramType")){
    		        jPAQueryExecutor.addParameter(getSearchParam(entry.getKey()),entry.getValue());
                } else {
                    jPAQueryExecutor.addParameter(getSearchParam(entry.getKey()), "%" + SqlUtils.escapeWildcards(entry.getValue().toString())+ "%");
                }
    		} else {
                if(entry.getValue() instanceof Integer) {
                    jPAQueryExecutor.addParameter(getSearchParam(entry.getKey()), entry.getValue());
                } else if(entry.getKey().equals("paramType")){
                    jPAQueryExecutor.addParameter(getSearchParam(entry.getKey()),entry.getValue());
                }else {
                    jPAQueryExecutor.addParameter(getSearchParam(entry.getKey()), SqlUtils.escapeWildcards(entry.getValue().toString()) + "%");
                }
    		}
    	}
    }

	private String getLeftOuterJoinClause(Class<?> entityClass,Map<String, Object> joinColumnMap,Map<String, String> joinProcessedColumnMap)
    {
    	StringBuilder leftOuterJoins=new StringBuilder("");
    	for(Map.Entry<String, Object> entry:joinColumnMap.entrySet())
    	{
    	     String[] joinColumns = entry.getKey().split("\\.");
    	     if(ValidatorUtils.notNull(joinColumns)&&joinColumns.length>1)
    	     {
    	        leftOuterJoins.append(getLeftOuterJoinQuery(entityClass, joinColumns,joinProcessedColumnMap));
    	     }
    	}
    	return leftOuterJoins.toString();
    }
    private String getOrderByQuery(String selectClause, String fromClause, Class<?> entityClass,
            String approvalStatusClause, String snapShotClause,
            String sortColName, String sortDir,String searchClause,Map<String, String>joinColumnAliasMap) {
        String orderByQuery = "";
        String sortColAlias="s."+sortColName;
        if(ValidatorUtils.hasAnyEntry(joinColumnAliasMap))
        {
            sortColAlias=updateColumnWithAlias(sortColAlias, joinColumnAliasMap);
        }
        if(String.class.equals(getSortColumnType(entityClass,sortColName))
                || Character.class.equals(getSortColumnType(entityClass,sortColName))){

            if("entityLifeCycleData.lastUpdatedByUri".equals(sortColName)) {
                sortColAlias = "usr.username";
            }
            if("masterLifeCycleData.reviewedByUri".equals(sortColName) || "viewProperties.reviewedBy".equals(sortColName)) {
                sortColAlias = "usr1.username";
            }

            if("masterLifeCycleData.approvalStatus".equals(sortColName)) {
                orderByQuery = selectClause + fromClause + " WHERE " + approvalStatusClause
                        + snapShotClause + searchClause + " ORDER BY LOWER( " + sortColAlias + ") " + sortDir + ORDER_CLAUSE;
            } else {
                orderByQuery = selectClause + fromClause + " WHERE " + approvalStatusClause
                        + snapShotClause + searchClause + APPROVAL_STATUS_ORDER + " , LOWER( " + sortColAlias + ") " + sortDir + ORDER_CLAUSE;
            }
            
        }
        else {
            if("masterLifeCycleData.approvalStatus".equals(sortColName)) {
                orderByQuery = selectClause + fromClause + " WHERE" + approvalStatusClause
                        + snapShotClause + searchClause + " ORDER BY " + sortColAlias + " " + sortDir + ORDER_CLAUSE;
            } else {
                orderByQuery = selectClause + fromClause + " WHERE" + approvalStatusClause
                        + snapShotClause + searchClause + APPROVAL_STATUS_ORDER + " , " + sortColAlias + " " + sortDir + ORDER_CLAUSE;
            }
            
        }
        
        return orderByQuery;
    }
    
    private String getSearchClause(Map<String, Object> searchMap,boolean leftOuterJoin,Map<String, String> leftOuterJoinMap) {
        
        
        if(ValidatorUtils.hasNoEntry(searchMap))
        {
        	return "";
        }
        StringBuilder searchClause = new StringBuilder(" AND ( ");
        int  length =searchMap.size();
        for (Map.Entry<String, Object> entry : searchMap.entrySet()) {
            String end=") ";
            String colParameter=entry.getKey();
            String alias;
            if("entityLifeCycleData.createdByUri".equals(entry.getKey()) || "entityLifeCycleData.lastUpdatedByUri".equals(entry.getKey())) {
                alias = "usr.username";
            } else if("masterLifeCycleData.reviewedByUri".equals(entry.getKey()) || "viewProperties.reviewedBy".equals(entry.getKey())) {
                alias = "usr1.username";
            } else {
                alias = "s." + colParameter;
            }
            if(leftOuterJoin)
            {
               alias=updateColumnWithAlias(alias,leftOuterJoinMap);
            }
            if(entry.getValue() instanceof Integer) {
                searchClause.append(" (" + alias + ") = (:" + getSearchParam(colParameter) + end);
            }
            else if(entry.getKey().equals("paramType")){
                searchClause.append(" (" + alias + ") IN (:" + getSearchParam(colParameter) + end);
            }
            else {
                searchClause.append(" LOWER(" + alias + ") LIKE LOWER(:" + getSearchParam(colParameter) + end+SqlUtils.getEscapeClause());
            }
            if(length > 1) {
                searchClause.append(" OR ");
            }
            
            length--;
        }
        searchClause.append(" ) ");
        return searchClause.toString();
    }
    private String getSearchParam(String searchParam) {
    	
		return searchParam.replace('.', '_');
	}

	private String updateColumnWithAlias(String alias,Map<String, String>leftOuterJoinMap)
    {
    	int lastIndex=alias.lastIndexOf('.');
        String key=alias;
        if(lastIndex!=-1)
        {
        	key=alias.substring(0,lastIndex);
        	if(leftOuterJoinMap.containsKey(key))
        	{
                alias=leftOuterJoinMap.get(key)+alias.substring(lastIndex,alias.length());	
        	}
        }
        return alias;
    }
    
    
    private String getQueryForRuleSetMaster(String qlString, String entityName) {
        String queryString = qlString;
        String targetValue = entityName.concat("[ ]+s[ ]+WHERE");
        String replacementValue = entityName.concat(" s WHERE dtype='RuleSet' and ");
        queryString = queryString.replaceAll(targetValue, replacementValue);
        return queryString;
    }

	@Override
	public <T extends Entity> List<T> findEntitiesByCriteria(
			Class<T> entityClass, CriteriaMapVO criteriaMapVO) {

		if (ValidatorUtils.isNull(entityClass)) {
			throw ExceptionBuilder
					.getInstance(ServiceInputException.class)
					.setExceptionCode("invalid.entityClass")
					.setMessage(
							new Message("invalid.entityClass",
									MessageType.ERROR))
					.setLogMessage("Entity Class cannot be null.").build();
		}

		StringBuilder queryString = new StringBuilder();
		queryString.append("select masterEntity FROM " + entityClass.getName() + " masterEntity "
				+ " WHERE (masterEntity.entityLifeCycleData.snapshotRecord is null OR masterEntity.entityLifeCycleData.snapshotRecord = false)");
		return createQueryStringBasedOnCriteriaMapVO(queryString,criteriaMapVO);


	}

	private <T extends Entity> List<T> createQueryStringBasedOnCriteriaMapVO(
			StringBuilder queryString, CriteriaMapVO criteriaMapVO) {
		Map<String, Object> paramKeyValueMap = new HashMap<String, Object>();
		Query qry = null;
		int paramVariable = 0;
		if (ValidatorUtils.hasAnyEntry(criteriaMapVO
				.getEqualClauseCriteriaMap())) {
			queryString.append(" and ");
			paramVariable=createQueryForEqualClauseCriteriaMap(criteriaMapVO.getEqualClauseCriteriaMap(), queryString, paramVariable, paramKeyValueMap);
		}

		if (ValidatorUtils.hasAnyEntry(criteriaMapVO
				.getLessThanClauseCriteriaMap())) {
			queryString.append(" and ");
			paramVariable=createQueryForInputClauseCriteriaMap(criteriaMapVO.getLessThanClauseCriteriaMap(), queryString, paramVariable, paramKeyValueMap," < ");
		}

		if (ValidatorUtils.hasAnyEntry(criteriaMapVO
				.getLessThanEqualClauseCriteriaMap())) {
			queryString.append(" and ");
			paramVariable=createQueryForInputClauseCriteriaMap(criteriaMapVO.getLessThanEqualClauseCriteriaMap(), queryString, paramVariable, paramKeyValueMap," <= ");

		}

		if (ValidatorUtils.hasAnyEntry(criteriaMapVO
				.getGreaterThanClauseCriteriaMap())) {
			queryString.append(" and ");
			paramVariable=createQueryForInputClauseCriteriaMap(criteriaMapVO.getGreaterThanClauseCriteriaMap(), queryString, paramVariable, paramKeyValueMap," > ");
		}

		if (ValidatorUtils.hasAnyEntry(criteriaMapVO
				.getGreaterThanEqualClauseCriteriaMap())) {
			queryString.append(" and ");
			paramVariable=createQueryForInputClauseCriteriaMap(criteriaMapVO.getGreaterThanEqualClauseCriteriaMap(), queryString, paramVariable, paramKeyValueMap," >= ");
		}

		if (ValidatorUtils.hasAnyEntry(criteriaMapVO
				.getNotEqualClauseCriteriaMap())) {
			queryString.append(" and ");
			paramVariable = createQueryForNotEqualClauseCriteriaMap(
					criteriaMapVO, queryString, paramVariable, paramKeyValueMap);
		}
		qry = getEntityManager().createQuery(queryString.toString());
		for (Map.Entry<String, Object> entry : paramKeyValueMap.entrySet()) {
			qry.setParameter(entry.getKey(), entry.getValue());
		}
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		return DaoUtils.executeQuery(getEntityManager(), qry);
	}

	private int createQueryForNotEqualClauseCriteriaMap(
			CriteriaMapVO criteriaMapVO, StringBuilder queryString,
			int paramVariable, Map<String, Object> paramKeyValueMap) {
		Map<String,Object> notEqualClauseCriteriaMap=criteriaMapVO.getNotEqualClauseCriteriaMap();
		int paramVar=paramVariable;
		int i = 0;
		for (Map.Entry<String, Object> entry : notEqualClauseCriteriaMap.entrySet()) {
			if (notEqualClauseCriteriaMap.size() > 1
					&& i != notEqualClauseCriteriaMap.size() - 1) {
				queryString.append("masterEntity." + entry.getKey() + " not in (:param)" + paramVar
						+ " and ");
			} else {
				queryString.append("masterEntity." + entry.getKey() + " not in (:param)" + paramVar);
			}
			paramKeyValueMap.put("param" + paramVar, entry.getValue());
			i++;
			paramVar++;
		}	
		return paramVar;
	}
       
    	private int createQueryForEqualClauseCriteriaMap(Map<String,Object> equalClauseCriteriaMap, StringBuilder queryString,
    			int paramVariable, Map<String, Object> paramKeyValueMap) {
    		int paramVar=paramVariable;
    		int i = 0;
    		for (Map.Entry<String, Object> entry : equalClauseCriteriaMap.entrySet()) {
    			if (equalClauseCriteriaMap.size() > 1
    					&& i != equalClauseCriteriaMap.size() - 1) {
    				
    				if(entry.getValue() instanceof List){
    					queryString.append("masterEntity." + entry.getKey() +" in :param" + paramVar
        						+ " and ");
    				}else{
    					queryString.append("masterEntity." + entry.getKey() +" =:param" + paramVar
        						+ " and ");
    				}
    				
    			} else {
    				if(entry.getValue() instanceof List){
    					queryString.append("masterEntity." + entry.getKey() +" in :param" + paramVar);
    				}else{
    					queryString.append("masterEntity." + entry.getKey() +" = :param" + paramVar);
    				}    				
    			}
    			paramKeyValueMap.put("param" + paramVar, entry.getValue());
    			i++;
    			paramVar++;
    		}	
    		return paramVar;
    	}
        
	private int createQueryForInputClauseCriteriaMap(Map<String,Object> criteriaMapObject, StringBuilder queryString,
			int paramVariable, Map<String, Object> paramKeyValueMap,String clauseCondition) {
		int paramVar=paramVariable;
		int i = 0;
		for (Map.Entry<String, Object> entry : criteriaMapObject.entrySet()) {
			if (criteriaMapObject.size() > 1
					&& i != criteriaMapObject.size() - 1) {
				queryString.append("masterEntity." + entry.getKey() +clauseCondition+ " :param" + paramVar
						+ " and ");
			} else {
				queryString.append("masterEntity." + entry.getKey() +clauseCondition+ " :param" + paramVar);
			}
			paramKeyValueMap.put("param" + paramVar, entry.getValue());
			i++;
			paramVar++;
		}	
		return paramVar;
	}
	
	@Override
	public <T extends BaseMasterEntity> Long getAllEntitiesCountForGridByStatus(Class<T> entityClass,
			List<Integer> genericStatusList, String specificStatusList, String viewerUri) {
		boolean needOfEntitiesCountOnly = true;
		String countQuery = getAllEntitiesQuery(entityClass, genericStatusList, specificStatusList, viewerUri, needOfEntitiesCountOnly);
		JPAQueryExecutor<Long> jPAQueryExecutor = new JPAQueryExecutor<Long>(countQuery);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
		return executeQueryForSingleValue(jPAQueryExecutor);
	}

	private <T extends BaseMasterEntity> String getAllEntitiesQuery(Class<T> entityClass, List<Integer> genericStatusList, String specificStatusList,
			String viewerUri, boolean needOfEntitiesCountOnly) {
		if (entityClass == null || genericStatusList == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer("(");
        for (int i = 0 ; i < genericStatusList.size() ; i++) {
            sb.append(genericStatusList.get(i));
            if (i != genericStatusList.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");

        // removed parameterized query : TBD

        String approvalStatusClause = " s.masterLifeCycleData.approvalStatus IN " + sb
                + " OR  (s.masterLifeCycleData.approvalStatus IN (" + specificStatusList
                + ") AND s.entityLifeCycleData.createdByUri='" + viewerUri + "') ";
        String snapShotClause = "AND (s.entityLifeCycleData.snapshotRecord is null or s.entityLifeCycleData.snapshotRecord = false) ";

        String sortableField = "";
        String sortsubQuery = "";
        sortableField = EntityUtil.getSortableField(entityClass);
        if (sortableField != "") {
            sortsubQuery = "ORDER BY s." + sortableField + " ASC";
        }
        String selectClause = "";
        if (needOfEntitiesCountOnly) {
        	selectClause = "SELECT count(*) ";
        }
        String qlString = selectClause + "FROM " + entityClass.getName() + " s WHERE " + approvalStatusClause + snapShotClause
                + sortsubQuery;
        
        //Only for RuleSet Master, needs to update in the future
        if (RULE_SET_ENTITY.equalsIgnoreCase(entityClass.getSimpleName())) {
            qlString = getQueryForRuleSetMaster(qlString, entityClass.getName());
        }
		return qlString;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends BaseMasterEntity> Long getPaginatedEntitiesCountForGridByStatus(GridVO gridVO,
			Class<T> entityClass, String viewerUri, List<Integer> genericStatusList, String specificStatusList) {
		gridVO.setEntityCountRequired(true);    //This helps in getting count(*) query string.
		Map<String, Object> searchParametersAndQueryMap = getPaginatedEntityQueryAndSearchMap(gridVO, entityClass, viewerUri, genericStatusList, specificStatusList);
		String countQuery = (String) searchParametersAndQueryMap.get(PAGINATED_QYERY_KEY);
		Map<String, Object> searchMap = (Map<String, Object>) searchParametersAndQueryMap.get(SEARCH_MAP_KEY);
		JPAQueryExecutor<Long> jPAQueryExecutor = new JPAQueryExecutor<Long>(countQuery);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        addSearchParameters(jPAQueryExecutor, searchMap, gridVO.isContainsSearchEnabled());
		return executeQueryForSingleValue(jPAQueryExecutor);
	}

	private <T extends BaseMasterEntity> Map<String, Object> getPaginatedEntityQueryAndSearchMap(GridVO gridVO, Class<T> entityClass,
			String viewerUri, List<Integer> genericStatusList, String specificStatusList) {
		Map<String, Object> searchParametersAndQueryMap = new HashMap<>(2);
		StringBuffer sb = new StringBuffer("(");
        for (int i = 0 ; i < genericStatusList.size() ; i++) {
            sb.append(genericStatusList.get(i));
            if (i != genericStatusList.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        String qlString = "";
        String SELECTCLAUSE = null;
        if (gridVO.isEntityCountRequired()) {
        	SELECTCLAUSE = "select count(*) ";
        } else {
        	SELECTCLAUSE = "select s ";
        }

        //Get Grid Info from GridVO
        String sortColName = gridVO.getSortColName();
        String sortDir =  gridVO.getSortDir();
        Map<String, Object> searchMap = gridVO.getSearchMap();
        
        //Search Query
        String searchClause = "";
        String leftOuterJoinClause="";
        Map<String, String> joinColumnAliasMap=new HashMap<String, String>();
        if(searchMap!=null){
            boolean isSortColumnSearchable=true;
            if(sortColName!=null&&!searchMap.containsKey(sortColName))
            {
            	isSortColumnSearchable=false;
            	searchMap.put(sortColName, sortColName);
            }
            
            leftOuterJoinClause = getLeftOuterJoinClause(entityClass, searchMap,joinColumnAliasMap);
            if(!isSortColumnSearchable)
            {
            	searchMap.remove(sortColName);
            }

            String dynamicWorkflowClause = "";
            if(searchMap.containsKey("isDynamicWorkflow")) {
                dynamicWorkflowClause = " AND s.isDynamicWorkflow = "+searchMap.get("isDynamicWorkflow") + " AND s.entityLifeCycleData.persistenceStatus = 0 AND s.sourceProduct = '"+ProductInformationLoader.getProductCode()+ "' ";
                searchMap.remove("isDynamicWorkflow");
            }
            if(searchMap.containsKey("isProductFilteringRequired")) {
                dynamicWorkflowClause = " AND s.sourceProduct = '"+ProductInformationLoader.getProductCode()+ "' ";
                searchMap.remove("isProductFilteringRequired");
            }
            searchClause=getSearchClause(searchMap, true, joinColumnAliasMap);
            searchClause = searchClause + dynamicWorkflowClause;
        }
      
        if (USERS.equalsIgnoreCase(entityClass.getSimpleName())) {
			UserInfo user = getCurrentUser();
			StringBuilder clause=new StringBuilder(searchClause);
			searchClause = clause.append( NOT_CURRENT_LOGGED_IN_USER ).append(user.getUsername().toLowerCase()).append("\' ").toString();
		}
        
        String approvalStatusClause = "";
        if (StringUtils.isBlank(searchClause)) {
        	  approvalStatusClause = " (s.masterLifeCycleData.approvalStatus IN " + sb + " ) ";
        }else{
        	 approvalStatusClause = " (s.masterLifeCycleData.approvalStatus IN " + sb
                    + " OR  ((s.masterLifeCycleData.approvalStatus IN (" + specificStatusList
                    + ")) AND (s.entityLifeCycleData.createdByUri = '" + viewerUri + "'))) ";
        }
       
        //Special case in case of communication name
        if (COMMUNICATION_NAME.equalsIgnoreCase(entityClass.getSimpleName())) {
        	approvalStatusClause = approvalStatusClause + " AND s.templateBased=true ";
		}
        
        String snapShotClause = "AND (s.entityLifeCycleData.snapshotRecord is null or s.entityLifeCycleData.snapshotRecord = false) AND (s.entityLifeCycleData.persistenceStatus != "
                + PersistenceStatus.EMPTY_PARENT + " ) ";
        //This is poor approach -> need to change to table
        if("GenericParameter".equalsIgnoreCase(entityClass.getSimpleName())){
            approvalStatusClause = approvalStatusClause + "AND s.class IN ("+getGenericParameterTypeCommaSeperated()+") ";

        }else if (RULE_SET_ENTITY.equalsIgnoreCase(entityClass.getSimpleName())) {
            approvalStatusClause = approvalStatusClause + "AND s.class IN ("+" RuleSet "+") ";
        }
		if (sortColName == null) {

			if (StringUtils.isBlank(searchClause)) {
				qlString = SELECTCLAUSE + "FROM " + entityClass.getName() + " s " + leftOuterJoinClause + " WHERE"
						+ approvalStatusClause + snapShotClause + searchClause + APPROVAL_STATUS_ORDER + ORDER_CLAUSE;
			} else {
				qlString = SELECTCLAUSE + "FROM " + entityClass.getName() + " s " + leftOuterJoinClause
						+ " left outer join com.nucleus.user.User usr on s.entityLifeCycleData.lastUpdatedByUri = (CONCAT('com.nucleus.user.User:', usr.id)) or (s.entityLifeCycleData.createdByUri = (CONCAT('com.nucleus.user.User:', usr.id)) and s.entityLifeCycleData.lastUpdatedByUri is null) "
                        + " left outer join com.nucleus.user.User usr1 on (s.masterLifeCycleData.reviewedByUri = (CONCAT('com.nucleus.user.User:', usr1.id))) "
                        + " WHERE" + approvalStatusClause + snapShotClause + searchClause + APPROVAL_STATUS_ORDER
						+ ORDER_CLAUSE;
			}

		} else {
            if (StringUtils.isBlank(sortDir)) {
                sortDir = "";
            } else {
                sortDir = sortDir + " NULLS LAST ";
            }
            String FROMCLAUSE = "FROM " + entityClass.getName() + " s "+leftOuterJoinClause+" , com.nucleus.user.User usr ";
            String fromLeftClause;
			if (entityClass.equals(User.class)) {
				if (StringUtils.isBlank(searchClause)) {
					fromLeftClause = "FROM " + entityClass.getName() + " s " + leftOuterJoinClause + " ";
				} else {
					fromLeftClause = "FROM " + entityClass.getName() + " s " + leftOuterJoinClause
							+ " left outer join com.nucleus.user.User usr on s.entityLifeCycleData.createdByUri = (CONCAT('com.nucleus.user.User:', usr.id)) ";
				}
			} else {
				if (StringUtils.isBlank(searchClause) && !sortColName.equals("entityLifeCycleData.lastUpdatedByUri")) {
					fromLeftClause = "FROM " + entityClass.getName() + " s " + leftOuterJoinClause + " ";
				} else {
					fromLeftClause = "FROM " + entityClass.getName() + " s " + leftOuterJoinClause
							+ " left outer join com.nucleus.user.User usr on s.entityLifeCycleData.lastUpdatedByUri = (CONCAT('com.nucleus.user.User:', usr.id)) or (s.entityLifeCycleData.createdByUri = (CONCAT('com.nucleus.user.User:', usr.id)) and s.entityLifeCycleData.lastUpdatedByUri is null) "
                            + " left outer join com.nucleus.user.User usr1 on (s.masterLifeCycleData.reviewedByUri = (CONCAT('com.nucleus.user.User:', usr1.id))) ";
				}
			}
           if (sortColName.equals("viewProperties.createdBy")) {
                qlString = SELECTCLAUSE + FROMCLAUSE + " WHERE" + approvalStatusClause + snapShotClause + searchClause
                        + " AND SUBSTRING(s.entityLifeCycleData.createdByUri,23)=usr.id " + APPROVAL_STATUS_ORDER + " , usr.username " + sortDir + ORDER_CLAUSE;
            } else if (sortColName.equals("viewProperties.approvalStatus")) {
                sortColName = "masterLifeCycleData.approvalStatus";
                qlString = SELECTCLAUSE + FROMCLAUSE + " WHERE " + approvalStatusClause + snapShotClause + searchClause + APPROVAL_STATUS_ORDER + " , "
                        + sortColName + " " + sortDir + ORDER_CLAUSE;
            } else if (sortColName.equals("entityLifeCycleData.createdByUri")) {
                qlString = SELECTCLAUSE + FROMCLAUSE + " WHERE" + approvalStatusClause + snapShotClause + searchClause
                        + " AND SUBSTRING(s.entityLifeCycleData.createdByUri,23)=usr.id " + APPROVAL_STATUS_ORDER + " , usr.username " + sortDir + ORDER_CLAUSE;
            } else {
                qlString =  getOrderByQuery(SELECTCLAUSE, fromLeftClause, entityClass, approvalStatusClause, snapShotClause, sortColName, sortDir ,searchClause,joinColumnAliasMap);
            }

        }
        
        //Only for RuleSet Master, needs to update in the future

        if(gridVO.isEntityCountRequired() && null!=qlString && qlString.indexOf("ORDER BY")!=-1) {
        		qlString = qlString.substring(0, qlString.indexOf("ORDER BY"));
        		BaseLoggers.flowLogger.debug("order by clause removed for count query {}", qlString);
        }
        searchParametersAndQueryMap.put(SEARCH_MAP_KEY, searchMap);
        searchParametersAndQueryMap.put(PAGINATED_QYERY_KEY, qlString);
		return searchParametersAndQueryMap;
	}

    private String getGenericParameterTypeCommaSeperated(){
        return getGenericParameterTypes().stream().map(new Function<String, String>() {

            @Override
            public String apply(String t) {

                return "'"+t+"'";
            }
        }).collect(Collectors.joining(","));
    }

    private List<String> getGenericParameterTypes(){
        return genericDao.findAllViewableGenericParameterTypesFromDB();
    }


    public Object getColumnValueFromEntity(Class entity,Long id,String colName){
    	StringBuilder queryBuilder = new StringBuilder("Select ");
        queryBuilder.append(colName)
                .append(" From ")
                .append(entity.getSimpleName())
                .append(" Where id=:id");

    	Query qry = getEntityManager().createQuery(queryBuilder.toString());
    	qry.setParameter("id", id);
    	List result = DaoUtils.executeQuery(getEntityManager(), qry);
    	if(result!=null && CollectionUtils.isNotEmpty(result)){
    		return result.get(0);
    	}
    	return null;
    }
}
