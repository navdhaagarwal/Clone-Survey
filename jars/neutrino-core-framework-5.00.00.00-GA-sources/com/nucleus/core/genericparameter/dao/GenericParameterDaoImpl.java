package com.nucleus.core.genericparameter.dao;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.common.EntityUtil;
import com.nucleus.core.genericparameter.entity.DynamicGenericParameter;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.entity.GenericParameterMetaData;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.persistence.BaseDaoImpl;
import com.nucleus.persistence.DaoUtils;
import com.nucleus.query.constants.QueryHint;

@Named("genericParameterDao")
@Singleton
public class GenericParameterDaoImpl extends BaseDaoImpl<GenericParameter> implements GenericParameterDao {

	private static final String FROM = "FROM ";
	private static final String APPROVAL_STATUS = "approvalStatus";
	private static final String ORDER_BY = "ORDER BY baseEntity.";
	private static final String ERROR_MSG = "Entity Class can't be null";
	private static final String ERROR_MSG_1 = "Generic Parameter Class Name can't be null";
	private static final String ERROR_MSG_2 = "invalid.propertyNameValueMap";
	
	@Inject
	@Named("fwCacheHelper")
	private FWCacheHelper fwCacheHelper;

	@Value(value = "#{'${non.editable.generic.parameter.classes}'}")
	private String nonEditablegenericParameters;

	List<String> allGenericParameterTypesList = null;
	List<String> allGenericParameterTypesExcludeNonEditableList = null;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> T findByCode(String code, Class<T> entityClass) {
		NeutrinoValidator.notNull(code, "Code can't be null");
		NeutrinoValidator.notNull(entityClass, ERROR_MSG);
		String qlString = FROM + entityClass.getSimpleName()
				+ " s WHERE s.code = :code AND s.masterLifeCycleData.approvalStatus in :approvalStatus";
		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter("code", code);
		qry.setParameter(APPROVAL_STATUS, getApprovedRecordStatusList());
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);

		try {
			return (T) qry.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> T findByName(String name, Class<T> entityClass) {
		NeutrinoValidator.notNull(name, "Name can't be null");
		NeutrinoValidator.notNull(entityClass, ERROR_MSG);
		String qlString = FROM + entityClass.getSimpleName()
				+ " s WHERE s.name = :name AND s.masterLifeCycleData.approvalStatus in :approvalStatus";
		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter("name", name);
		qry.setParameter(APPROVAL_STATUS, getApprovedRecordStatusList());
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);

		try {
			return (T) qry.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> List<T> findByAuthorities(List<String> authCodes, Class<T> entityClass) {
		NeutrinoValidator.notEmpty(authCodes, "List can't be empty or null");
		NeutrinoValidator.notNull(entityClass, ERROR_MSG);
		String queryString = new StringBuilder("select DISTINCT gp FROM ").append(entityClass.getSimpleName()).append(
				" gp inner join gp.authorities ath WHERE ath.authCode in :authCodes AND gp.masterLifeCycleData.approvalStatus IN (0,3))")
				.toString();
		Query qry = getEntityManager().createQuery(queryString);
		qry.setParameter("authCodes", authCodes);
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);

		return qry.getResultList();
	}

	@Override
	public <T extends GenericParameter> T findById(Long id, Class<T> entityClass) {
		return super.find(entityClass, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GenericParameter> findAllTypes(GenericParameter genericParameter, String associatedName) {
		NeutrinoValidator.notNull(genericParameter, "Generic Parameter can't be null");
		NeutrinoValidator.notNull(genericParameter.getId(), "Generic Parameter Id can't be null");
		NeutrinoValidator.notEmpty(associatedName, "Entity Class can't be empty or null");
		Query qry = getEntityManager().createNamedQuery("Generic.AssociatedGenericParameter");
		qry.setParameter("genericParameter", genericParameter);
		qry.setParameter("associatedName", associatedName);
		qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		return qry.getResultList();
	}

	@Override
	public List<String> findAllGenericParameterTypes() {
		return this.findAllGenericParameterTypes(true);
	}

	@Override
	public List<String> findAllGenericParameterTypesFromDB() {
		List<String> allGenericParameterTypesExcludeNonViewableList = new ArrayList<>();
		Set<String> exclusionList = createExclusionListForGenericParameter(Arrays.asList(new Integer[] { 1, 2 }));
		Set<EntityType<?>> allEntityTypes = getEntityManager().getMetamodel().getEntities();
		for (EntityType<?> et : allEntityTypes) {
			if (GenericParameter.class.isAssignableFrom(et.getJavaType())
					&& !("GenericParameter").equals(et.getJavaType().getSimpleName())
					&& !exclusionList.contains(et.getJavaType().getSimpleName().toLowerCase())) {
				allGenericParameterTypesExcludeNonViewableList.add(et.getJavaType().getName());
			}
		}
		return allGenericParameterTypesExcludeNonViewableList;
	}

	@Override
	public List<String> findAllGenericParameterTypes(Boolean excludeNonEditableGenricParameters) {
		if (allGenericParameterTypesList == null) {
			allGenericParameterTypesList = new ArrayList<>();
			allGenericParameterTypesExcludeNonEditableList = new ArrayList<>();

			Set<EntityType<?>> allEntityTypes = getEntityManager().getMetamodel().getEntities();
			for (EntityType<?> et : allEntityTypes) {
				if (GenericParameter.class.isAssignableFrom(et.getJavaType())
						&& !("GenericParameter").equals(et.getJavaType().getSimpleName())) {
					allGenericParameterTypesList.add(et.getJavaType().getName());
					allGenericParameterTypesExcludeNonEditableList.add(et.getJavaType().getName());
				}
			}
			if (!(nonEditablegenericParameters.isEmpty())) {
				String[] entityNames = nonEditablegenericParameters.split(",");
				if (entityNames != null) {
					for (int i = 0; i < entityNames.length; i++) {
						if (allGenericParameterTypesExcludeNonEditableList.contains(entityNames[i])) {

							allGenericParameterTypesExcludeNonEditableList.remove(entityNames[i]);
						}
					}
				}
			}
		}

		if (excludeNonEditableGenricParameters) {
			return new ArrayList<>(allGenericParameterTypesExcludeNonEditableList);
		}
		return new ArrayList<>(allGenericParameterTypesList);
	}
	
	private List<String> findAllGenericParameterTypesLocal() {
		List<String> allGenericParameterTypesLocal = new ArrayList<>();

		Set<EntityType<?>> allEntityTypes = getEntityManager().getMetamodel().getEntities();
		for (EntityType<?> et : allEntityTypes) {
			if (GenericParameter.class.isAssignableFrom(et.getJavaType())
					&& !("GenericParameter").equals(et.getJavaType().getSimpleName())) {
				allGenericParameterTypesLocal.add(et.getJavaType().getName());
			}
		}
		return allGenericParameterTypesLocal;
	}

	public String getNonEditablegenericParameters() {
		return nonEditablegenericParameters;
	}

	public void setNonEditablegenericParameters(String nonEditablegenericParameters) {
		this.nonEditablegenericParameters = nonEditablegenericParameters;
	}

	@Override
	public <T extends GenericParameter> List<T> findChildrenByParentCode(String parentCode, Class<T> childEntityClass) {
		NeutrinoValidator.notEmpty(parentCode, "Parent Code can't be null or empty");
		NeutrinoValidator.notNull(childEntityClass, "Child Entity Class can't be null");
		String[] splitParentCode = parentCode.split(" ");
		List<String> list = new ArrayList<>();
		list = Arrays.asList(splitParentCode);
		String qlString = "FROM " + childEntityClass.getSimpleName()
				+ " s WHERE s.entityLifeCycleData.persistenceStatus = 0 and s.activeFlag = true and s.masterLifeCycleData.approvalStatus in :approvalStatus and s.parentCode in :parentCode order by name";
		JPAQueryExecutor<T> queryExecutor = new JPAQueryExecutor<T>(qlString);
		queryExecutor.addParameter("parentCode", list);
		queryExecutor.addParameter(APPROVAL_STATUS, getApprovedRecordStatusList());
		queryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		return executeQuery(queryExecutor);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> List<T> findBillingDueDayByName(String name, Class<T> entityClass) {
		NeutrinoValidator.notEmpty(name, "Name can't be null or empty");
		NeutrinoValidator.notNull(entityClass, ERROR_MSG);
		String qlString = FROM + entityClass.getSimpleName()
				+ " baseEntity WHERE ((baseEntity.entityLifeCycleData.snapshotRecord is null OR baseEntity.entityLifeCycleData.snapshotRecord = false) AND baseEntity.entityLifeCycleData.persistenceStatus = 0 AND baseEntity.activeFlag = true AND baseEntity.masterLifeCycleData.approvalStatus in :approvalStatus AND baseEntity.parentCode = :name)";
		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter("name", name);
		qry.setParameter(APPROVAL_STATUS, getApprovedRecordStatusList());
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
		qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		return DaoUtils.executeQuery(getEntityManager(), qry);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> List<T> findAllGenericParameter(Class<T> genericEntityClassName,
			boolean forUpdate) {
		NeutrinoValidator.notNull(genericEntityClassName, ERROR_MSG_1);
		String sortableField = "";
		String sortsubQuery = "";
		sortableField = EntityUtil.getSortableField(genericEntityClassName);

		if (sortableField != "") {
			sortsubQuery = ORDER_BY + sortableField + " ASC";
		}

		String qlString = FROM + genericEntityClassName.getSimpleName()
				+ " baseEntity LEFT JOIN FETCH baseEntity.authorities WHERE ((baseEntity.entityLifeCycleData.snapshotRecord is null OR baseEntity.entityLifeCycleData.snapshotRecord = false) AND (baseEntity.entityLifeCycleData.persistenceStatus is not null) AND (baseEntity.masterLifeCycleData.approvalStatus in :approvalStatus)) "
				+ sortsubQuery;

		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter(APPROVAL_STATUS, getApprovedRecordStatusList());
		if (!forUpdate) {
			qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
			qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
		}
		return DaoUtils.executeQuery(getEntityManager(), qry);
	}

	
	@Override
	public <T extends GenericParameter> List<T> findAllGenericParameter(Class<T> genericEntityClassName) {
		return findAllGenericParameter(genericEntityClassName, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> List<T> findGenericParameterBasedOnOfflineFlag(Class<T> genericEntityClassName,
			boolean forUpdate, String authorizationBusinessDate) {
		NeutrinoValidator.notNull(genericEntityClassName, ERROR_MSG_1);
		String sortableField = "";
		String sortsubQuery = "";
		sortableField = EntityUtil.getSortableField(genericEntityClassName);
		if (sortableField != "") {
			sortsubQuery = ORDER_BY + sortableField + " ASC";
		}
		String queryString = FROM + genericEntityClassName.getSimpleName()
				+ " baseEntity WHERE ((baseEntity.entityLifeCycleData.snapshotRecord is null OR baseEntity.entityLifeCycleData.snapshotRecord = false) AND baseEntity.masterLifeCycleData.approvalStatus in :approvalStatus"
				+ " AND baseEntity.offlineFlag = true) ";

		if (notNull(authorizationBusinessDate) && !"".equals(authorizationBusinessDate))
			queryString += " AND baseEntity.entityLifeCycleData.lastUpdatedTimeStamp >=:authorizationBusinessDate ";

		queryString += sortsubQuery;

		Query query = getEntityManager().createQuery(queryString);
		query.setParameter(APPROVAL_STATUS, getApprovedRecordStatusList());
		if (!forUpdate) {
			query.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
			query.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
			query.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		}

		if (notNull(authorizationBusinessDate))
			query.setParameter("authorizationBusinessDate",
					new DateTime(authorizationBusinessDate, org.joda.time.DateTimeZone.UTC));
		return DaoUtils.executeQuery(getEntityManager(), query);
	}

	@Override
	public <T> List<Map<String, Object>> findGenericParameterBasedOnFieldValue(Class<T> genericEntityClassName,
			Map<String, Object> propertyNameEqualsValueMap, Map<String, Object> propertyNameNotEqualsValueMap,
			String[] searchColumnList) {

		validateEntityClassSearchColumnsAndPropertyNameValueMap(genericEntityClassName, propertyNameEqualsValueMap,
				propertyNameNotEqualsValueMap, searchColumnList);
		StringBuilder queryString = new StringBuilder();
		queryString.append("select new Map(");
		if (ValidatorUtils.notNull(searchColumnList) && searchColumnList.length > 0) {
			addMapDataToQuery(addQueryColumns(searchColumnList), queryString);
		}
		queryString.append(") from " + genericEntityClassName.getSimpleName() + " s " + " where ");
		if (MapUtils.isNotEmpty(propertyNameEqualsValueMap)) {
			return findGenericParameterForEqualsValueMap(queryString, propertyNameEqualsValueMap);
		} else if (MapUtils.isNotEmpty(propertyNameNotEqualsValueMap)) {
			return findGenericParameterForNotEqualsValueMap(queryString, propertyNameNotEqualsValueMap);
		}
		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> findGenericParameterForNotEqualsValueMap(StringBuilder queryString,
			Map<String, Object> propertyNameNotEqualsValueMap) {
		Map<String, Object> paramNameValueMap = new HashMap<String, Object>();
		int i = 0;
		for (Map.Entry<String, Object> entry : propertyNameNotEqualsValueMap.entrySet()) {
			if (propertyNameNotEqualsValueMap.size() > 1 && i != propertyNameNotEqualsValueMap.size() - 1) {
				queryString.append("s." + entry.getKey() + " not in (:param" + i + ") and ");
			} else {
				queryString.append("s." + entry.getKey() + " not in (:param" + i + ")");
			}
			paramNameValueMap.put("param" + i, entry.getValue());
			i++;
		}
		queryString.append(
				" and s.entityLifeCycleData.persistenceStatus = 0 and s.activeFlag = true and s.masterLifeCycleData.approvalStatus in:approvalStatus");
		Query qry = getEntityManager().createQuery(queryString.toString());
		qry.setParameter(APPROVAL_STATUS, getApprovedRecordStatusList());
		setParameterValuesInQuery(qry, paramNameValueMap);
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		return (List<Map<String, Object>>) DaoUtils.executeQuery(getEntityManager(), qry);
	}

	private void setParameterValuesInQuery(Query qry, Map<String, Object> paramNameValueMap) {
		for (Map.Entry<String, Object> entry : paramNameValueMap.entrySet()) {
			qry.setParameter(entry.getKey(), entry.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> findGenericParameterForEqualsValueMap(StringBuilder queryString,
			Map<String, Object> propertyNameEqualsValueMap) {
		Map<String, Object> paramNameValueMap = new HashMap<String, Object>();
		int i = 0;
		for (Map.Entry<String, Object> entry : propertyNameEqualsValueMap.entrySet()) {
			if (propertyNameEqualsValueMap.size() > 1 && i != propertyNameEqualsValueMap.size() - 1) {

				if (entry.getValue() instanceof List) {
					queryString.append("s." + entry.getKey() + " in :param" + i + " and ");
				} else {
					queryString.append("s." + entry.getKey() + " = :param" + i + " and ");
				}

			} else {
				if (entry.getValue() instanceof List) {
					queryString.append("s." + entry.getKey() + " in :param" + i);
				} else {
					queryString.append("s." + entry.getKey() + " = :param" + i);
				}
			}
			paramNameValueMap.put("param" + i, entry.getValue());
			i++;
		}
		queryString.append(
				" and s.entityLifeCycleData.persistenceStatus = 0 and s.activeFlag = true and s.masterLifeCycleData.approvalStatus in :approvalStatus");
		Query qry = getEntityManager().createQuery(queryString.toString());
		qry.setParameter(APPROVAL_STATUS, getApprovedRecordStatusList());
		setParameterValuesInQuery(qry, paramNameValueMap);
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		return (List<Map<String, Object>>) DaoUtils.executeQuery(getEntityManager(), qry);

	}

	public <T> void validateEntityClassSearchColumnsAndPropertyNameValueMap(Class<T> entityClass,
			Map<String, Object> propertyNameEqualsValueMap, Map<String, Object> propertyNameNotEqualsValueMap,
			String[] searchColumnList) {
		if (ValidatorUtils.isNull(entityClass)) {
			throw ExceptionBuilder.getInstance(ServiceInputException.class).setExceptionCode("invalid.entityClass")
					.setMessage(new Message("invalid.entityClass", MessageType.ERROR))
					.setLogMessage("Entity Class cannot be null.").build();
		}

		if (MapUtils.isEmpty(propertyNameEqualsValueMap) && MapUtils.isEmpty(propertyNameNotEqualsValueMap)) {
			throw ExceptionBuilder.getInstance(ServiceInputException.class)
					.setExceptionCode(ERROR_MSG_2)
					.setMessage(new Message(ERROR_MSG_2, MessageType.ERROR))
					.setLogMessage(
							"Both propertyNameEqualsValueMap and propertyNameNotEqualsValueMap to fetch GenericParameter cannot be null.")
					.build();
		}
		if (MapUtils.isNotEmpty(propertyNameEqualsValueMap) && MapUtils.isNotEmpty(propertyNameNotEqualsValueMap)) {
			throw ExceptionBuilder.getInstance(ServiceInputException.class)
					.setExceptionCode(ERROR_MSG_2)
					.setMessage(new Message(ERROR_MSG_2, MessageType.ERROR))
					.setLogMessage(
							"Both propertyNameEqualsValueMap and propertyNameNotEqualsValueMap cannot be passed simultaneously to fetch GenericParameter.")
					.build();
		}
		if (ValidatorUtils.isNull(searchColumnList) || searchColumnList.length == 0) {
			throw ExceptionBuilder.getInstance(ServiceInputException.class).setExceptionCode("invalid.searchColumnList")
					.setMessage(new Message("invalid.searchColumnList", MessageType.ERROR))
					.setLogMessage("SearchColumnList values cannot be null.").build();
		}
	}

	private Map<String, String> addQueryColumns(String... columnNames) {
		Map<String, String> selectedProperties = new LinkedHashMap<String, String>();
		for (String columnName : columnNames) {
			String alias = columnName;
			if (columnName.contains(".")) {
				alias = columnName.replace(".", "");
			}
			selectedProperties.put(columnName, alias);
		}
		return selectedProperties;
	}

	private void addMapDataToQuery(Map<String, String> selectedProperties, StringBuilder queryString) {
		Iterator<Entry<String, String>> iterator = selectedProperties.entrySet().iterator();
		for (; iterator.hasNext();) {
			Entry<String, String> nextEntry = iterator.next();
			queryString.append(nextEntry.getKey()).append(" as ").append(nextEntry.getValue());
			if (iterator.hasNext()) {
				queryString.append(",");
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<? extends GenericParameter>> getDTypeBasedMapOfGenericParameter() {

		List<String> dTypeEntityList = this.findAllGenericParameterTypesLocal();
		List<? extends GenericParameter> genericParameterList = null;
		Map<String, List<? extends GenericParameter>> genericParameterEntitiesByTypeMap = new HashMap<>();

		for (String dTypeEntityName : dTypeEntityList) {
			try {
				genericParameterList = findAllGenericParameter(
						(Class<? extends GenericParameter>) Class.forName(dTypeEntityName), false);
				genericParameterEntitiesByTypeMap.put(dTypeEntityName, genericParameterList);
			} catch (ClassNotFoundException e) {
				throw new SystemException("ClassNotFoundException : " + dTypeEntityName, e);
			}
		}

		return genericParameterEntitiesByTypeMap;

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<Long>> getDTypeBasedMapOfGenericParameterIds() {

		List<String> dTypeEntityList = this.findAllGenericParameterTypesLocal();
		List<? extends GenericParameter> genericParameterList = null;
		Map<String, List<Long>> genericParameterIdsByTypeMap = new HashMap<>();

		for (String dTypeEntityName : dTypeEntityList) {
			try {
				List<Long> idList = new ArrayList<>();
				genericParameterList = findAllGenericParameter(
						(Class<? extends GenericParameter>) Class.forName(dTypeEntityName), false);
				if (ValidatorUtils.hasElements(genericParameterList)) {
					for (GenericParameter genericParameter : genericParameterList) {
						idList.add(genericParameter.getId());
					}
				}
				genericParameterIdsByTypeMap.put(dTypeEntityName, idList);
			} catch (ClassNotFoundException e) {
				throw new SystemException("ClassNotFoundException : " + dTypeEntityName, e);
			}
		}

		return genericParameterIdsByTypeMap;

	}
	
		
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getDTypeBasedListOfGenericParameterIds(String dTypeName) {

		List<Long> genericParameterIdList = new ArrayList<>();
		try {
			List<? extends GenericParameter> allGenericParametersList = findAllGenericParameter(
					(Class<? extends GenericParameter>) Class.forName(dTypeName), false);
			if (ValidatorUtils.hasElements(allGenericParametersList)) {
				for (GenericParameter genericParameter : allGenericParametersList) {
					genericParameterIdList.add(genericParameter.getId());
				}
			}
			return genericParameterIdList;
		} catch (ClassNotFoundException e) {
			throw new SystemException("ClassNotFoundException : " + dTypeName, e);
		}

	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> T findByDefaultValue(Class<T> entityClass) {

		NeutrinoValidator.notNull(entityClass, ERROR_MSG);
		String qlString = FROM + entityClass.getSimpleName()
				+ " s WHERE s.defaultFlag = true and s.masterLifeCycleData.approvalStatus in :approvalStatus";
		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter(APPROVAL_STATUS, getApprovedRecordStatusList());
		qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);

		try {
			return (T) qry.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> T findByDefaultValue(Class<T> entityClass, String dType) {

		NeutrinoValidator.notNull(entityClass, ERROR_MSG);
		String qlString = FROM + entityClass.getSimpleName()
				+ " s WHERE s.defaultFlag = true and s.dynamicParameterName = :dType and s.masterLifeCycleData.approvalStatus in :approvalStatus";
		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter(APPROVAL_STATUS, getApprovedRecordStatusList());
		qry.setParameter("dType", dType);
		qry.setHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		qry.setHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);

		try {
			return (T) qry.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	private List<Integer> getApprovedRecordStatusList() {
		List<Integer> statusList = new ArrayList<>();
		statusList.add(ApprovalStatus.APPROVED);
		statusList.add(ApprovalStatus.APPROVED_MODIFIED);
		return statusList;
	}

	@SuppressWarnings("unchecked")
	public GenericParameterMetaData getDTypeMetaData(String simpleName) {
		StringBuilder queryString = new StringBuilder();
		queryString.append("select meta from GenericParameterMetaData meta where meta.dType= :dType");
		Query qry = getEntityManager().createQuery(queryString.toString());
		qry.setParameter("dType", simpleName);
		qry.setHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
		List<GenericParameterMetaData> purposeList = DaoUtils.executeQuery(getEntityManager(), qry);
		return CollectionUtils.isNotEmpty(purposeList) ? purposeList.get(0) : new GenericParameterMetaData();
	}

	@Override
	public List<String> findAllViewableGenericParameterTypesFromDB() {
		List<String> allGenericParameterTypesExcludeNonViewableList = new ArrayList<>();
		Set<String> exclusionList = createExclusionListForGenericParameter(Arrays.asList(new Integer[] { 2 }));
		Set<EntityType<?>> allEntityTypes = getEntityManager().getMetamodel().getEntities();
		for (EntityType<?> et : allEntityTypes) {
			if (GenericParameter.class.isAssignableFrom(et.getJavaType())
					&& !("GenericParameter").equals(et.getJavaType().getSimpleName())
					&& !exclusionList.contains(et.getJavaType().getSimpleName().toLowerCase())) {
				allGenericParameterTypesExcludeNonViewableList.add(et.getName());
			}
		}
		return allGenericParameterTypesExcludeNonViewableList;
	}

	@SuppressWarnings("unchecked")
	private Set<String> createExclusionListForGenericParameter(List<Integer> exclusionStatus) {
		String sourceProduct = ProductInformationLoader.getProductCode();
		String qry = "select gpmd.dType,gpmd.sourceType,gpmd.dTypeActionFlag from GenericParameterMetaData gpmd";
		Query query = getEntityManager().createQuery(qry).setHint("org.hibernate.cacheable", true);
		List<Object[]> ob = (List<Object[]>) DaoUtils.executeQuery(getEntityManager(), query);
		Set<String> exclusionList = new HashSet<>();
		for (Object[] entry : ob) {

			if (exclusionStatus.contains((Integer) entry[2])) {
				exclusionList.add(((String) entry[0]).toLowerCase());
				continue;
			}
			if (StringUtils.isBlank((String) entry[1])) {
				continue;
			}
			List<String> sourceProducts = Arrays.asList(((String) entry[1]).split(","));
			boolean belongs = false;
			for (String prod : sourceProducts) {
				if (prod.equalsIgnoreCase(sourceProduct.trim())) {
					belongs = true;
					break;
				}

			}
			if (!belongs) {
				exclusionList.add(((String) entry[0]).trim().toLowerCase());
			}
		}
		return exclusionList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllDynamicGenericParameter() {
		Query qry = getEntityManager().createNamedQuery("Generic.getAllDynamicGenericParameter");
		List<String> ob=(List<String>)DaoUtils.executeQuery(getEntityManager(), qry);
		if (null != ob && !ob.isEmpty()) {
			return ob;
		}
		return Collections.EMPTY_LIST;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class findGenericParameterTypes(String dTypeSimpleName) {
		Set<EntityType<?>> allEntityTypes = getEntityManager().getMetamodel().getEntities();
		Set<String> exclusionList = createExclusionListForGenericParameter(Arrays.asList(new Integer[]{2}));
		for (EntityType<?> et : allEntityTypes) {
			if (GenericParameter.class.isAssignableFrom(et.getJavaType())
					&& !("GenericParameter").equalsIgnoreCase(et.getJavaType().getSimpleName())
					&& dTypeSimpleName.equalsIgnoreCase(et.getJavaType().getSimpleName())) {
				if(exclusionList.contains(et.getJavaType().getSimpleName().toLowerCase())){
					return DynamicGenericParameter.class;
				}else{
					return et.getJavaType();
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<? extends GenericParameter> populateDTypeOfGenericParameterForDuplication(String dTypeName) {

		try {
			return findAllGenericParameterForDuplication(
					(Class<? extends GenericParameter>) Class.forName(dTypeName));
		} catch (ClassNotFoundException e) {
			throw new SystemException("ClassNotFoundException : " + dTypeName, e);
		}

	}

	@SuppressWarnings("unchecked")
	private <T extends GenericParameter> List<T> findAllGenericParameterForDuplication(
			Class<T> genericEntityClassName) {
		NeutrinoValidator.notNull(genericEntityClassName, ERROR_MSG_1);
		String sortableField = "";
		String sortsubQuery = "";
		sortableField = EntityUtil.getSortableField(genericEntityClassName);

		if (sortableField != "") {
			sortsubQuery = ORDER_BY + sortableField + " ASC";
		}

		String qlString = FROM + genericEntityClassName.getSimpleName()
				+ " baseEntity LEFT JOIN FETCH baseEntity.authorities WHERE ((baseEntity.entityLifeCycleData.snapshotRecord is null OR baseEntity.entityLifeCycleData.snapshotRecord = false) AND (baseEntity.masterLifeCycleData.approvalStatus in :approvalStatus)) "
				+ sortsubQuery;

		Query qry = getEntityManager().createQuery(qlString);
		qry.setParameter(APPROVAL_STATUS, getRecordStatusListForDuplication());
		return DaoUtils.executeQuery(getEntityManager(), qry);
	}

	private List<Integer> getRecordStatusListForDuplication() {
		List<Integer> statusList = new ArrayList<>();
		statusList.add(ApprovalStatus.APPROVED);
		statusList.add(ApprovalStatus.APPROVED_MODIFIED);
		statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
		statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
		statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
		statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		statusList.add(ApprovalStatus.APPROVED_DELETED);
		return statusList;
	}

}
