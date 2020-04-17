package com.nucleus.persistence;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.FlushModeType;

import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.query.constants.QueryHint;

@Named("childMasterDao")
public class ChildMasterDaoImpl extends EntityDaoImpl implements ChildMasterDao {
	
	@Inject
	@Named("baseMasterDao")
	private BaseMasterDao baseMasterDao;
	 
	private static final String CHILD_ALIAS_CLAUSE 		= " c.";
	private static final String SELECT_CLAUSE			= " SELECT c FROM ";
	private static final String CHILD_WHERE_CLAUSE 		= " c WHERE p.id= ";
	private static final String OR_CLAUSE				= " OR ";
	private static final String AND_CLAUSE				= " AND ";
	private static final String JOIN_CLAUSE				= " p JOIN p.";
	private static final String ORDER_BY_CLAUSE			= " ORDER BY ";
    private static final String DEFAULT_ORDER 			= " c.entityLifeCycleData.uuid, c.id";
    private static final String CHILD_DELETED_CLAUSE 	= " AND c.masterLifeCycleData.approvalStatus NOT IN (5, 14)";
    
	private String getSearchClause(GridVO gridVO) {
		StringBuilder searchClause = new StringBuilder();
		if (gridVO == null || ValidatorUtils.hasNoEntry(gridVO.getSearchMap())) {
			return " ";
		}
		Map<String, Object> searchMap = gridVO.getSearchMap();
		searchClause.append(AND_CLAUSE + "(");
		String key;
    	for (Map.Entry<String, Object> entry : searchMap.entrySet()) {
    		key = entry.getKey();
    		searchClause.append(" LOWER(")
    			.append(CHILD_ALIAS_CLAUSE)
    			.append(key)
    			.append(") LIKE LOWER(:")
    			.append(getModifiedParameter(key))
    			.append(")")
    			.append(OR_CLAUSE);
    	}
    	searchClause.setLength(searchClause.length() - OR_CLAUSE.length());
    	searchClause.append(")");
		return searchClause.toString();
	}

	/**
	 * We are modifying parameter because hibernate does not support parameter with dot (.) eg - '(:first.last)'
	 * So (:first.last) would be changed to (:first_last)
	 * @param key
	 * @return
	 */
	private String getModifiedParameter(String key) {
		return key.replace('.', '_');
	}

	private String getOrderClause(GridVO gridVO) {
		StringBuilder sb = new StringBuilder();
		if (gridVO == null || (gridVO.getSortColName() == null && gridVO.getSortDir() == null)) {
			sb.append(ORDER_BY_CLAUSE).append(DEFAULT_ORDER);
			return sb.toString();
		}
		sb.append(ORDER_BY_CLAUSE)
		  .append(CHILD_ALIAS_CLAUSE)
		  .append(gridVO.getSortColName())
		  .append(" ")
		  .append(gridVO.getSortDir());
		return sb.toString();
	}
	
	private <Q> void addSearchParameters(JPAQueryExecutor<Q> jPAQueryExecutor, Map<String, Object> searchMap) {
		if (ValidatorUtils.hasNoEntry(searchMap)) {
			return ;
		}
		for (Map.Entry<String, Object> entry : searchMap.entrySet()) {
			jPAQueryExecutor.addParameter(getModifiedParameter(entry.getKey()), "%"+entry.getValue()+"%");
		}
	}
	
	private String getChildRecordsQuery(String attributeName, Class<?> entityClass, GridVO gridVO, BaseMasterEntity parentEntity) {
    	StringBuilder queryBuilder = new StringBuilder();
    	if (gridVO.isEntityCountRequired()) {
    		queryBuilder.append("SELECT COUNT(c.id) FROM ");
    	} else {
    		queryBuilder.append(SELECT_CLAUSE);
    	}
    	queryBuilder.append(entityClass.getName())
    		.append(JOIN_CLAUSE)
    		.append(attributeName)
    		.append(CHILD_WHERE_CLAUSE)
    		.append(parentEntity.getId())
    		.append(CHILD_DELETED_CLAUSE)
    		.append(getSearchClause(gridVO));
    	if (!gridVO.isEntityCountRequired()) {
    		//we need order by clause if we want all entities.
    		//So in case of count query this code won't get executed.
    		queryBuilder.append(getOrderClause(gridVO));
    	}
    	return queryBuilder.toString();
    }
	
	private String getChildRecordsQueryForModifiedParent(String childAttributeName, Class<?> entityClass, GridVO gridVO,
			BaseMasterEntity parentEntity, Long lastApprovedParentRecordId) {
		StringBuilder queryBuilder = new StringBuilder();
    	if (gridVO.isEntityCountRequired()) {
    		queryBuilder.append("SELECT COUNT(c.id) FROM ");
    	} else {
    		queryBuilder.append(SELECT_CLAUSE);
    	}
    	queryBuilder.append(entityClass.getName())
    		.append(JOIN_CLAUSE)
    		.append(childAttributeName)
    		.append(CHILD_WHERE_CLAUSE)
    		.append(parentEntity.getId())
    		.append(CHILD_DELETED_CLAUSE)
    		.append(getSearchClause(gridVO))
    		.append(OR_CLAUSE + " c.id IN (")
    		.append("SELECT c.id FROM ");
    	queryBuilder.append(entityClass.getName())
					.append(JOIN_CLAUSE)
					.append(childAttributeName)
					.append(CHILD_WHERE_CLAUSE)
					.append(lastApprovedParentRecordId)
					.append(AND_CLAUSE)
					.append("c.entityLifeCycleData.uuid NOT IN (")
					.append(getUuidQuery(childAttributeName, entityClass, parentEntity) + ")")
					.append(CHILD_DELETED_CLAUSE)
					.append(getSearchClause(gridVO));
    	queryBuilder.append(")");
    	if (!gridVO.isEntityCountRequired()) {
    		//we need order by clause if we want all entities.
    		//So in case of count this code won't get executed.
    		queryBuilder.append(getOrderClause(gridVO));
    	}
    	return queryBuilder.toString();
	}
	
	private BaseMasterEntity getLastApprovedParentRecord(BaseMasterEntity parentEntity) {
		return baseMasterDao.getLastApprovedEntityByUnapprovedEntityId(parentEntity.getEntityId());
	}

	private Object getUuidQuery(String childAttributeName, Class<?> entityClass, BaseMasterEntity parentEntity) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT c.entityLifeCycleData.uuid FROM ")
					.append(entityClass.getName())
					.append(JOIN_CLAUSE)
					.append(childAttributeName)
					.append(CHILD_WHERE_CLAUSE)
					.append(parentEntity.getId());
		return queryBuilder.toString();
	}

	private String getQuery(String childAttributeName, Class<?> entityClass, GridVO gridVO,
			BaseMasterEntity parentEntity) {
		BaseMasterEntity lastApprovedParentRecord = getLastApprovedParentRecord(parentEntity);
		if (lastApprovedParentRecord == null || lastApprovedParentRecord.getId().equals(parentEntity.getId())) {
			//Parent is not modified.
			return getChildRecordsQuery(childAttributeName, entityClass, gridVO, parentEntity);
		} else {
			//Parent is modified. A child is added, edited or deleted.
			return getChildRecordsQueryForModifiedParent(childAttributeName, entityClass, gridVO, parentEntity, lastApprovedParentRecord.getId());
		}
	}
	
	@Override
	public <T extends BaseMasterEntity> List<T> loadPaginatedData(String childAttributeName, Class<?> entityClass,
			GridVO gridVO, T parentEntity) {
		String query = getQuery(childAttributeName, entityClass, gridVO, parentEntity);
		JPAQueryExecutor<T> jPAQueryExecutor = new JPAQueryExecutor<>(query);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        addSearchParameters(jPAQueryExecutor, gridVO.getSearchMap());
        return executeQuery(jPAQueryExecutor, gridVO.getiDisplayStart(), gridVO.getiDisplayLength());
	}
	
	@Override
	public Integer getTotalRecordSize(String childAttributeName, Class<?> entityClass, BaseMasterEntity parentEntity) {
		GridVO gridVO = new GridVO();
		gridVO.setEntityCountRequired(true);
		String countQuery = getQuery(childAttributeName, entityClass, gridVO, parentEntity);
		JPAQueryExecutor<Long> jPAQueryExecutor = new JPAQueryExecutor<>(countQuery);
		jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
		Long result = executeQueryForSingleValue(jPAQueryExecutor);
		result = result == null ? 0L : result;
		return result.intValue();
	}
	
	@Override
	public Integer getSearchRecordsCount(String childAttributeName, Class<?> entityClass, GridVO gridVO, BaseMasterEntity parentEntity) {
		gridVO.setEntityCountRequired(true);		//This helps in getting count(*) query string.
		String query = getQuery(childAttributeName, entityClass, gridVO, parentEntity);
		JPAQueryExecutor<Long> jPAQueryExecutor = new JPAQueryExecutor<>(query);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        addSearchParameters(jPAQueryExecutor, gridVO.getSearchMap());
        Long result = executeQueryForSingleValue(jPAQueryExecutor);
        result = result == null ? 0L : result;
		return result.intValue();
	}
}
