package com.nucleus.finnone.pro.cache.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.service.CacheCommonService;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;
import com.nucleus.logging.BaseLoggers;

public class CustomCacheEntityListener {

	public static final String FIELD_NAME_TO_VALUE_MAP = "FIELD_NAME_TO_VALUE_MAP";
	public static final String FIELD_NAME_TO_OLD_VALUE_MAP = "FIELD_NAME_TO_OLD_VALUE_MAP";
	public static final String FIELD_NAME_TO_IMPACTED_CACHE_VO_MAP = "FIELD_NAME_TO_IMPACTED_CACHE_VO_MAP";
	public static final String IMPACTED_CACHE_VO_TO_IMPACTED_CACHE_MAP = "IMPACTED_CACHE_VO_TO_IMPACTED_CACHE_MAP";

	private static final Map<Class<?>, Map<String, Set<ImpactedCacheVO>>> entityToFieldNameImpactedCacheVOMap = new HashMap<>();
	private static final Map<Class<?>, Set<String>> entityToFieldNameMap = new HashMap<>();
	private static final Map<Class<?>, Map<String, Method>> entityToFieldNameGetterMethodMap = new HashMap<>();

	public static void markImpactedCacheVOsForEntity(Class<?> entityClass, String fieldName,
			Set<ImpactedCacheVO> impactedCacheVOSet) {

		Map<String, Set<ImpactedCacheVO>> fieldNameToImpactedCacheVOMap = entityToFieldNameImpactedCacheVOMap.get(entityClass);
		if (fieldNameToImpactedCacheVOMap == null) {
			fieldNameToImpactedCacheVOMap = new HashMap<>();
		}
		fieldNameToImpactedCacheVOMap.put(fieldName, impactedCacheVOSet);

		Set<String> fieldNameSet = new HashSet<>();
		fieldNameSet.add(fieldName);

		entityToFieldNameImpactedCacheVOMap.put(entityClass, fieldNameToImpactedCacheVOMap);
		entityToFieldNameMap.put(entityClass, fieldNameSet);
	}

	public static void markGetterMethodsForAnnotatedMethods(Class<?> entityClass, String fieldName, Method method) {
		Map<String, Method> fieldNameToGetterMethodMap = entityToFieldNameGetterMethodMap.get(entityClass);
		if (fieldNameToGetterMethodMap == null) {
			fieldNameToGetterMethodMap = new HashMap<>();
		}
		fieldNameToGetterMethodMap.put(fieldName, method);
		entityToFieldNameGetterMethodMap.put(entityClass, fieldNameToGetterMethodMap);
	}

	private static Map<String, Set<ImpactedCacheVO>> retrieveFieldAndImpactedCacheVOMapByClass(Class<?> entityClass) {
		return new HashMap<>(entityToFieldNameImpactedCacheVOMap.get(entityClass));
	}

	private CacheCommonService cacheCommonService;
	private TransactionPostCommitWork cacheUpdatePostCommitWork;

	private void init() {
		if (cacheCommonService == null) {
			cacheCommonService = NeutrinoSpringAppContextUtil.getBeanByName("cacheCommonService",
					CacheCommonService.class);
			cacheUpdatePostCommitWork = NeutrinoSpringAppContextUtil.getBeanByName("cacheUpdatePostCommitWork",
					TransactionPostCommitWork.class);
		}
	}

	private Boolean checkEntity(BaseEntity entity) {
		Map<String, Set<ImpactedCacheVO>> map = entityToFieldNameImpactedCacheVOMap.get(entity.getClass());
		if (map == null || map.isEmpty()) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@PostPersist
	@PostUpdate
	@PostRemove
	public void entityPostAction(BaseEntity entity) {
		if (checkEntity(entity)) {
			updateImpactedCachesInPostTransaction(entity, createImpactedCacheEntry(entity.getClass()));
		}

	}

	private Map<ImpactedCacheVO, ImpactedCache> createImpactedCacheEntry(Class<?> entityClass) {
		init();
		Map<String, Set<ImpactedCacheVO>> fieldNameToImpactedCacheVOMap = entityToFieldNameImpactedCacheVOMap.get(entityClass);
		Map<ImpactedCacheVO, ImpactedCache> impactedCacheMap = new HashMap<>();

		for (Set<ImpactedCacheVO> impactedCacheNameSet : fieldNameToImpactedCacheVOMap.values()) {
			for (ImpactedCacheVO impactedCacheVO : impactedCacheNameSet) {
				impactedCacheMap.put(impactedCacheVO,
						cacheCommonService.createImpactedCacheEntry(impactedCacheVO));
			}
		}
		return impactedCacheMap;
	}

	@SuppressWarnings("unchecked")
	private <T extends BaseEntity> void updateImpactedCachesInPostTransaction(BaseEntity entity,
			Map<ImpactedCacheVO, ImpactedCache> impactedCacheVOToImpactedCacheMap) {
		init();
		Method getterMethod = null;
		Class<T> entityClass = (Class<T>) entity.getClass();
		Map<String, Object> fieldNameToFieldValueMap = new HashMap<>();
		Map<String, Object> fieldNameToOldFieldValueMap = new HashMap<>();
		for (String fieldName : entityToFieldNameMap.get(entityClass)) {
			getterMethod = entityToFieldNameGetterMethodMap.get(entityClass).get(fieldName);
			try {
				fieldNameToFieldValueMap.put(fieldName, getterMethod.invoke(entity, null));
				fieldNameToOldFieldValueMap.put(fieldName,
						cacheCommonService.getCurrentFieldValue(fieldName, entity.getId(), entityClass));

			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				BaseLoggers.flowLogger.error(e.getMessage());
				throw new SystemException(e);
			}
		}

		Map<String, Object> argumentsMap = new HashMap<>();
		argumentsMap.put(FIELD_NAME_TO_VALUE_MAP, fieldNameToFieldValueMap);
		argumentsMap.put(FIELD_NAME_TO_OLD_VALUE_MAP, fieldNameToOldFieldValueMap);
		argumentsMap.put(FIELD_NAME_TO_IMPACTED_CACHE_VO_MAP, retrieveFieldAndImpactedCacheVOMapByClass(entityClass));
		argumentsMap.put(IMPACTED_CACHE_VO_TO_IMPACTED_CACHE_MAP, impactedCacheVOToImpactedCacheMap);
		TransactionPostCommitWorker.handlePostCommit(cacheUpdatePostCommitWork, argumentsMap, true);

	}
	
}
