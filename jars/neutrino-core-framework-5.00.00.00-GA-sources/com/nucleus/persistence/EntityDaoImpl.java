package com.nucleus.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.persistence.Cacheable;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.apache.commons.collections4.ListUtils;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.nucleus.core.common.EntityUtil;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.persistence.jdbc.PersistenceUtils;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.dao.query.QueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.sequence.DatabaseSequenceGenerator;
import com.nucleus.query.constants.QueryHint;

/**
 * The Class EntityDaoImpl.
 */
@Named("entityDao")
public class EntityDaoImpl implements EntityDao {

    private static final int          ORACLE_BATCH_SIZE = 900;
    
    private static final Set<String> cacheableEntities = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<String> nonCacheableEntities = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @PersistenceContext
    private EntityManager             em;

    @Autowired
    private DataSource                dataSource;

    @Autowired
    private DatabaseSequenceGenerator sequenceGenerator;

    protected JdbcTemplate            jdbcTemplate;

    /**
     * Post contruct.
     */
    @PostConstruct
    protected void postContruct() {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    @Override
    public Boolean contains(Entity entity) {
    	return em.contains(entity);
    }

    /**
     * Gets the entity manager.
     *
     * @return the entity manager
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Gets the jdbc template.
     *
     * @return the jdbc template
     */
    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public <T extends Entity> T find(Class<T> entityClass, Serializable id) {
        if (entityClass == null || id == null) {
            throw new SystemException("Entity class or id cannot be null");
        }
        return em.find(entityClass, id);
    }

    @Override
    public <T extends Entity> T load(Class<T> entityClass, Serializable id) {
        if (entityClass == null || id == null) {
            throw new SystemException("Entity class or id cannot be null");
        }
        try {
            return em.getReference(entityClass, id);
        } catch (EntityNotFoundException enfe) {
            return null;
        }
    }

    @Override
    public void persist(Entity entity) {
        em.persist(entity);
    }

    @Override
    public void delete(Entity baseEntity) {
        em.remove(baseEntity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> T get(EntityId entityId) {
        if (entityId == null) {
            throw new SystemException("EntityId cannot be null");
        }
        return (T) find(entityId.getEntityClass(), entityId.getLocalId());
    }

    @Override
    public void flush() {
        em.flush();
    }

    
    
    /**
     * Deprecated due to performance hit. By default query cache should be disabled.
     * 
     * @see com.nucleus.persistence.EntityDao#findAll(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> List<T> findAll(Class<T> entityClass) {
    	NeutrinoValidator.notNull(entityClass, "Entity cannot be null");
		String sortableField = "";
		String sortsubQuery = "";
		sortableField = EntityUtil.getSortableField(entityClass);
		if (sortableField != "") {
			sortsubQuery = "ORDER BY baseEntity." + sortableField + " ASC";
		}
				
		String queryString = "FROM " + entityClass.getSimpleName()
				+ " baseEntity WHERE (baseEntity.entityLifeCycleData.snapshotRecord is null OR baseEntity.entityLifeCycleData.snapshotRecord = false)"
				+ sortsubQuery;
		Query query = getEntityManager().createQuery(queryString);
		query.setHint(QueryHint.QUERY_HINT_CACHEABLE, isQueryCacheApplicableOrNot(entityClass));
		return DaoUtils.executeQuery(em, query);
    }
    
	protected <T extends Entity> boolean isQueryCacheApplicableOrNot(Class<T> entityClass) {
		if (cacheableEntities.contains(entityClass.getName())) {
			return true;
		}
		if (nonCacheableEntities.contains(entityClass.getName())) {
			return false;
		}
		if (entityClass.getAnnotation(Cacheable.class) != null || entityClass.getAnnotation(Cache.class) != null) {
			cacheableEntities.add(entityClass.getName());
			return true;
		} else {
			nonCacheableEntities.add(entityClass.getName());
			return false;
		}
	}

    /*
     * (non-Javadoc) @see com.nucleus.persistence.EntityDao#findAllWithSpecificProjections(java.lang.Class, java.lang.String[])
     *  entityClass is name of the entity which need to be queried 
     *  columnNameList is column names of the entity which need to be returned
     *  returns list of object array,which contains data of columns same as in columnNameList
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> List<Object[]> findAllWithSpecifiedColumns(Class<T> entityClass, String... columnNameList) {
        NeutrinoValidator.notNull(entityClass, "Entity cannot be null");
        StringBuilder dynamicQuery = new StringBuilder();
        String sortableField = "";
        String sortsubQuery = "";
        sortableField = EntityUtil.getSortableField(entityClass);
        if (sortableField != "") {
            sortsubQuery = "ORDER BY baseEntity." + sortableField + " ASC";
        }
        String qlString = "FROM "
                + entityClass.getSimpleName()
                + " baseEntity WHERE (baseEntity.entityLifeCycleData.snapshotRecord is null OR baseEntity.entityLifeCycleData.snapshotRecord = false)"
                + sortsubQuery;

        String QuerySelect = "Select baseEntity.id as id";
        dynamicQuery.append(QuerySelect);
        if (columnNameList != null) {
            for (String columnName : columnNameList) {
                dynamicQuery.append(", ");
                dynamicQuery.append("baseEntity.").append(columnName).append(" as ").append(columnName);
            }
        }
        dynamicQuery.append(" ");
        dynamicQuery.append(qlString);
        qlString = dynamicQuery.toString();
        Query qry = getEntityManager().createQuery(qlString);
        qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<Object[]> entities = DaoUtils.executeQuery(em, qry);
        return entities;
    }

    @Override
    public boolean entityExists(EntityId entityId) {
        if (entityId == null) {
            throw new SystemException("EntityId cannot be null");
        }
        Query query = em.createQuery("select count(entity.id) from " + entityId.getEntityClass().getSimpleName()
                + " entity where entity.id = ?1");
        query.setParameter(1, entityId.getLocalId());
        return ((Number) query.getSingleResult()).intValue() > 0;
    }

    @Override
    public <T extends Entity> T update(T entity) {
        if (entity.getId() != null) {
            return em.merge(entity);
        } else {
            throw new SystemException("Cannot update an un-persisted entity. Please persist the entity before updating it.");
        }
    }

    @Override
    public <T extends Entity> T saveOrUpdate(T entity) {
        T managedEntity = null;
        if (entity.getId() == null) {
            persist(entity);
            managedEntity = entity;
        } else {
            managedEntity = update(entity);
        }
        return managedEntity;
    }

    @Override
    public <T extends Entity> void detach(T entity) {
        em.detach(entity);
    }

    @Override
    public <T> List<T> executeQuery(QueryExecutor<T> executor) {
        return executeQuery(executor, null, null);
    }

    @Override
    public <T> List<T> executeQuery(QueryExecutor<T> executor, Integer startIndex, Integer pageSize) {
        return executor.executeQuery(em, startIndex, pageSize);
    }

    @Override
    public <T> Long executeTotalRowsQuery(QueryExecutor<T> executor) {
        return executor.executeTotalRowsQuery(em);
    }

    @Override
    public <T> T executeQueryForSingleValue(QueryExecutor<T> executor) {
        List<T> result = executeQuery(executor);
        if (result.size() == 1) {
            return result.get(0);
        } else if (result.size() > 1) {
            throw new SystemException("Query execution didn't return single result : " + executor.toString());
        } else {
            return null;
        }
    }

    @Override
    public <T extends Entity> List<T> searchEntityOnFieldAndValue(Class<T> entityClass, String field, String value) {

        javax.persistence.criteria.CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> fromClause = criteriaQuery.from(entityClass);
        criteriaQuery.select(fromClause);

        List<Predicate> criteria = new ArrayList<Predicate>();
        Predicate valuePredicate/* approvedPredicate*/;

        valuePredicate = criteriaBuilder.like(fromClause.<String> get(field), "%" + value + "%");
        criteria.add(valuePredicate);

        // approvedPredicate = criteriaBuilder.equal(fromClause.<Integer> get("masterLifeCycleData.approvalStatus"),
        // Integer.valueOf(0));
        // criteria.add(approvedPredicate);

        Predicate[] predicates = new Predicate[criteria.size()];
        criteria.toArray(predicates);
        criteriaQuery.where(predicates);

        return em.createQuery(criteriaQuery).getResultList();
    }


    /* (non-Javadoc) @see com.nucleus.persistence.EntityDao#getNextValue(java.lang.String) */
    @Override
    public Long getNextValue(String sequenceName) {
        return sequenceGenerator.getNextValue(sequenceName);
    }

    @Override
    public Long getNextValue(String sequenceName, int seqIncr) {
        return sequenceGenerator.getNextValue(sequenceName, seqIncr);
    }

    @Override
    public int updateQuery(String queryString, Map<String, Object> map) {
        NeutrinoValidator.notNull(queryString, "QueryString can not be null for updating");
        Query qry = getEntityManager().createQuery(queryString);
        for (String key : map.keySet()) {
            qry.setParameter(key, map.get(key));
        }
        return qry.executeUpdate();
    }

    @Override
    public void clearEntireCache() {
        em.getEntityManagerFactory().getCache().evictAll();
    }

    @Override
    public void clearEntityLevelCache(Class clazz) {
        em.getEntityManagerFactory().getCache().evict(clazz);
    }

    // batch to avoid oracle in clause 1000 limit issue

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<Map<String, Object>> executeSingleInClauseHQLQuery(String hqlQuery, String inParamName, Collection<?> values) {

        // first of all remove nulls
        if (values != null) {
            values = ListUtils.removeAll(values, Collections.singletonList(null));
        }
        List<Map<String, Object>> completeResultList = new ArrayList<Map<String, Object>>();
        if (values != null && !values.isEmpty()) {

            List idList = new ArrayList(values);
            int fromIndex = 0;
            int toIndex = idList.size() > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : idList.size();
            while (toIndex <= idList.size() && fromIndex < toIndex) {
                List idSubList = new ArrayList(idList.subList(fromIndex, toIndex));
                PersistenceUtils.resizeListWithAutoFill(idSubList);
                if (!idSubList.isEmpty()) {
                    JPAQueryExecutor<Map<String, Object>> executor = new JPAQueryExecutor<Map<String, Object>>(hqlQuery);
                    executor.addParameter(inParamName, idSubList);
                    List<Map<String, Object>> resultListForBatch = executeQuery(executor);
                    if (resultListForBatch != null) {
                        completeResultList.addAll(resultListForBatch);
                    }
                    fromIndex = toIndex;
                    int difference = idList.size() - toIndex;
                    if (difference <= 0) {
                        break;
                    }
                    int batchSize = difference > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : difference;
                    toIndex = toIndex + batchSize;
                }
            }
        }
        return completeResultList;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> List<T> executeSingleInClauseHQLQuery(String hqlQuery, String inParamName, Collection<?> values,
            Class<T> returnClass) {

        // first of all remove nulls
        if (values != null) {
            values = ListUtils.removeAll(values, Collections.singletonList(null));
        }
        int initialSize = values.size();
        List<T> completeResultList = new ArrayList<T>(initialSize > 10 ? initialSize : 10);
        if (values != null && !values.isEmpty()) {
            List idList = new ArrayList(values);
            int fromIndex = 0;
            int toIndex = idList.size() > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : idList.size();
            while (toIndex <= idList.size() && fromIndex < toIndex) {
                List idSubList = new ArrayList(idList.subList(fromIndex, toIndex));
                PersistenceUtils.resizeListWithAutoFill(idSubList);
                if (!idSubList.isEmpty()) {
                    int fetchSize = idSubList.size();
                    fetchSize = fetchSize <= 50 ? fetchSize >= 10 ? fetchSize : 10 : 50;

                    Query query = em.createQuery(hqlQuery, returnClass);
                    query.setParameter(inParamName, idSubList);
                    query.setHint(QueryHint.QUERY_HINT_FETCHSIZE, fetchSize);
                    List<T> resultListForBatch = query.getResultList();
                    if (resultListForBatch != null) {
                        completeResultList.addAll(resultListForBatch);
                    }
                    fromIndex = toIndex;
                    int difference = idList.size() - toIndex;
                    if (difference <= 0) {
                        break;
                    }
                    int batchSize = difference > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : difference;
                    toIndex = toIndex + batchSize;
                }
            }
        }
        return completeResultList;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<Map<String, Object>> executeSingleInClauseHQLQuery(String hqlQuery, String inParamName,
            Collection<?> values, Map<String, ?> otherParams) {

        // first of all remove nulls
        if (values != null) {
            values = ListUtils.removeAll(values, Collections.singletonList(null));
        }
        List<Map<String, Object>> completeResultList = new ArrayList<Map<String, Object>>();
        if (values != null && !values.isEmpty()) {

            List idList = new ArrayList(values);
            int fromIndex = 0;
            int toIndex = idList.size() > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : idList.size();
            while (toIndex <= idList.size() && fromIndex < toIndex) {
                List idSubList = new ArrayList(idList.subList(fromIndex, toIndex));
                PersistenceUtils.resizeListWithAutoFill(idSubList);
                if (!idSubList.isEmpty()) {
                    JPAQueryExecutor<Map<String, Object>> executor = new JPAQueryExecutor<Map<String, Object>>(hqlQuery);
                    executor.addParameter(inParamName, idSubList);

                    // add other params
                    if (otherParams != null && !otherParams.isEmpty()) {
                        for (Entry<String, ?> param : otherParams.entrySet()) {
                            executor.addParameter(param.getKey(), param.getValue());
                        }
                    }

                    List<Map<String, Object>> resultListForBatch = executeQuery(executor);
                    if (resultListForBatch != null) {
                        completeResultList.addAll(resultListForBatch);
                    }
                    fromIndex = toIndex;
                    int difference = idList.size() - toIndex;
                    if (difference <= 0) {
                        break;
                    }
                    int batchSize = difference > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : difference;
                    toIndex = toIndex + batchSize;
                }
            }
        }
        return completeResultList;
    }



    @Override
    public <T extends Entity> T saveOrHibernateUpdate(T entity) {
        if (entity.getId() == null) {
            persist(entity);
        } else {
            getSession().update(entity);
        }
        return entity;
    }
    
    @Override
    public <T extends Entity> T hibernateSaveOrUpdate(T entity) {
       
            getSession().saveOrUpdate(entity);
        
        return entity;
    }
    private Session getSession() {

        Session s = (Session) getEntityManager().unwrap(Session.class);
        return s;
    }
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void executeSingleInClauseHQLQueryForUpdateDelete(String hqlQuery,
			String inParamName, Collection<?> values) {

		// first of all remove nulls
		if (values != null) {
			values = ListUtils.removeAll(values,
					Collections.singletonList(null));
		}
		if (values != null && !values.isEmpty()) {
			List idList = new ArrayList(values);
			int fromIndex = 0;
			int toIndex = idList.size() > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE
					: idList.size();
			while (toIndex <= idList.size() && fromIndex < toIndex) {
				List idSubList = new ArrayList(idList.subList(fromIndex,
						toIndex));
				PersistenceUtils.resizeListWithAutoFill(idSubList);
				if (!idSubList.isEmpty()) {
					int fetchSize = idSubList.size();
					fetchSize = fetchSize <= 50 ? fetchSize >= 10 ? fetchSize
							: 10 : 50;

					Query query = em.createQuery(hqlQuery);
					query.setParameter(inParamName, idSubList);
					query.setHint(QueryHint.QUERY_HINT_FETCHSIZE, fetchSize);
					query.executeUpdate();

					fromIndex = toIndex;
					int difference = idList.size() - toIndex;
					if (difference <= 0) {
						break;
					}
					int batchSize = difference > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE
							: difference;
					toIndex = toIndex + batchSize;
				}
			}
		}
	}

	@Override
	public BaseMasterEntity findByUUID(
			Class entityClass, String uuid) {
				String entityName=entityClass.getSimpleName();
				StringBuilder query=new StringBuilder();
				query.append("select ")
					.append(" from ")
					.append(entityName)
					.append(" where ")
					.append(entityName)
					.append(".entityLifeCycleData.uuid=:uuid ")
					.append(entityName)
					.append(".masterLifeCycleData.approvalStatus IN (:approvalStatusList)");
				Query typedQuery = getEntityManager().createQuery(query.toString());
				List<Integer> aprrovalStatusList = new ArrayList<Integer>();
				aprrovalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
				aprrovalStatusList.add(ApprovalStatus.APPROVED);
				typedQuery.setParameter("approvalStatusList",aprrovalStatusList);
				typedQuery.setParameter("uuid",uuid);
				List<BaseMasterEntity> results=(List<BaseMasterEntity>) typedQuery.getResultList();
				if(ValidatorUtils.hasElements(results))
				{
					return results.get(0);
				}
		return null;
	}

    @Override
    public BaseMasterEntity findByMasterUUID(
            Class entityClass, String uuid) {
        String entityClassName=entityClass.getSimpleName();
        String entityName = entityClassName.substring(0,1).toLowerCase()
                + entityClassName.substring(1);
        StringBuilder query=new StringBuilder();
        query.append("select ")
                .append(entityName)
                .append(" from ")
                .append(entityClassName)
                .append(" ")
                .append(entityName)
                .append(" where ")
                .append(entityName)
                .append(".entityLifeCycleData.uuid=:uuid ")
                .append(" AND ")
                .append(entityName)
                .append(".masterLifeCycleData.approvalStatus IN (:approvalStatusList)");
        Query typedQuery = getEntityManager().createQuery(query.toString());
        List<Integer> aprrovalStatusList = new ArrayList<Integer>();
        aprrovalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        aprrovalStatusList.add(ApprovalStatus.APPROVED);
        typedQuery.setParameter("approvalStatusList",aprrovalStatusList);
        typedQuery.setParameter("uuid",uuid);
        List<BaseMasterEntity> results=(List<BaseMasterEntity>) typedQuery.getResultList();
        if(ValidatorUtils.hasElements(results))
        {
            return results.get(0);
        }
        return null;
    }


	@Override
	public <T> List<T> executeSingleInClauseHQLQuery(String hqlQuery, String inParamName, Collection<?> values,
			Map<String, ?> otherParams, Class<T> returnClass) {

        // first of all remove nulls
        if (values != null) {
            values = ListUtils.removeAll(values, Collections.singletonList(null));
        }
        List<T> completeResultList = new ArrayList<>();
        if (values != null && !values.isEmpty()) {
            List idList = new ArrayList(values);
            int fromIndex = 0;
            int toIndex = idList.size() > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : idList.size();
            while (toIndex <= idList.size() && fromIndex < toIndex) {
                List idSubList = new ArrayList(idList.subList(fromIndex, toIndex));
                PersistenceUtils.resizeListWithAutoFill(idSubList);
                if (!idSubList.isEmpty()) {
                	NamedQueryExecutor<T> executor = new NamedQueryExecutor<>(
                            hqlQuery);
                    executor.addParameter(inParamName, idSubList);
                    
                    // add other params
                    if (otherParams != null && !otherParams.isEmpty()) {
                        for (Entry<String, ?> param : otherParams.entrySet()) {
                        	executor.addParameter(param.getKey(), param.getValue());
                        }
                    }
                    List<T> resultListForBatch = executeQuery(executor);
                    if (resultListForBatch != null) {
                        completeResultList.addAll(resultListForBatch);
                    }
                    fromIndex = toIndex;
                    int difference = idList.size() - toIndex;
                    if (difference <= 0) {
                        break;
                    }
                    int batchSize = difference > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : difference;
                    toIndex = toIndex + batchSize;
                }
            }
        }
        return completeResultList;
    }

}