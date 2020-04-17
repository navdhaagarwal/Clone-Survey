package com.nucleus.rules.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import com.nucleus.core.common.EntityUtil;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.persistence.BaseDaoImpl;
import com.nucleus.persistence.DaoUtils;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.Parameter;

@Named("parameterDao")
public class ParameterDaoImpl extends BaseDaoImpl<Parameter> implements ParameterDao {

	private static final String QUERY_STRING = "select par FROM %s par Where par.masterLifeCycleData.approvalStatus in (:statusList) AND (par.entityLifeCycleData.snapshotRecord IS NULL OR par.entityLifeCycleData.snapshotRecord = false) AND par.activeFlag = true";

	List<String> allParameterTypesList = null;

	@Override
	public List<String> findAllParameterTypes() {
		if (allParameterTypesList == null) {
			allParameterTypesList = new ArrayList<>();
			Set<EntityType<?>> allEntityTypes = getEntityManager().getMetamodel().getEntities();
			for (EntityType<?> et : allEntityTypes) {
				if (Parameter.class.isAssignableFrom(et.getJavaType())) {
					allParameterTypesList.add(et.getJavaType().getName());
				}
			}
		}
		return allParameterTypesList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<? extends Parameter>> getTypeBasedMapOfParameters() {
		List<String> typeList = this.findAllParameterTypes();
		Map<String, List<? extends Parameter>> parameterEntitiesByTypeMap = new HashMap<>();

		for (String typeName : typeList) {
			try {
				parameterEntitiesByTypeMap.put(typeName,
						findAllParameters((Class<? extends Parameter>) Class.forName(typeName)));
			} catch (ClassNotFoundException e) {
				throw new SystemException("ClassNotFoundException : " + typeName, e);
			}
		}

		return parameterEntitiesByTypeMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Parameter> List<T> findAllParameters(Class<T> entityClassName) {
		NeutrinoValidator.notNull(entityClassName, "Parameter Class Name can't be null");
		String sortableField = "";
		String sortsubQuery = "";
		sortableField = EntityUtil.getSortableField(entityClassName);

		if (sortableField != "") {
			sortsubQuery = "ORDER BY baseEntity." + sortableField + " ASC";
		}

		String qlString = "FROM " + entityClassName.getSimpleName()
				+ " baseEntity WHERE ((baseEntity.entityLifeCycleData.snapshotRecord is null OR baseEntity.entityLifeCycleData.snapshotRecord = false) AND (baseEntity.activeFlag = true) AND (baseEntity.masterLifeCycleData.approvalStatus in :statusList)) "
				+ sortsubQuery;

		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
		qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

		return DaoUtils.executeQuery(getEntityManager(), qry);
	}

	@Override
	public List<Integer> getApprovedRecordStatusList() {
		List<Integer> statusList = new ArrayList<>();
		statusList.add(ApprovalStatus.APPROVED);
		statusList.add(ApprovalStatus.APPROVED_MODIFIED);
		statusList.add(ApprovalStatus.APPROVED_DELETED);
		statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		return statusList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Parameter> T findApprovedParameterByName(String parameterName, Class<T> entityClass) {
		NeutrinoValidator.notNull(parameterName, "Name can't be null");
		NeutrinoValidator.notNull(entityClass, "Entity Class can't be null");
		String qlString = "FROM " + entityClass.getSimpleName()
				+ " baseEntity WHERE ((baseEntity.name = :name) AND (baseEntity.entityLifeCycleData.snapshotRecord is null OR baseEntity.entityLifeCycleData.snapshotRecord = false) AND (baseEntity.activeFlag = true) AND (baseEntity.masterLifeCycleData.approvalStatus in :statusList))";
		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter("name", parameterName);
		qry.setParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);

		try {
			List<T> resultList = qry.getResultList();
			if (!resultList.isEmpty()) {
				return resultList.get(0);
			}
		} catch (NoResultException e) {

		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Parameter> List<T> getAllParametersFromDB(Class<T> parameterClass) {
		if (parameterClass == null) {
			NamedQueryExecutor<Parameter> executor = new NamedQueryExecutor<Parameter>("Rules.findAllParameters")
					.addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
					.addQueryHint(QueryHint.QUERY_HINT_FETCHSIZE, 500);
			return (List<T>) executeQuery(executor);
		}

		String qlString = String.format(QUERY_STRING, parameterClass.getName());
		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
		qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
		return qry.getResultList();
	}

}
