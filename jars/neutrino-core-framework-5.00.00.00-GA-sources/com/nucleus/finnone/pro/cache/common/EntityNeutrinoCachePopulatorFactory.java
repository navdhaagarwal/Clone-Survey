package com.nucleus.finnone.pro.cache.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Cacheable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.annotation.CustomCache;
import com.nucleus.finnone.pro.cache.annotation.CustomCaches;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;
import com.nucleus.logging.BaseLoggers;

@Named("entityNeutrinoCachePopulatorFactory")
public class EntityNeutrinoCachePopulatorFactory {

	private static final String GET = "get";

	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;

	@PersistenceContext
	private EntityManager em;

	private FieldCallback fieldCallback;

	@PostConstruct
	public void init() {
		fieldCallback = new FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalAccessException {
				CustomCaches customCaches = field.getAnnotation(CustomCaches.class);
				if (customCaches != null) {
					Set<ImpactedCacheVO> impactedCacheNameSet = new HashSet<>();

					Class<?> clas = field.getDeclaringClass();
					Method method = null;

					String fieldName = field.getName();
					String methodName = new StringBuilder(GET).append(StringUtils.capitalize(fieldName)).toString();

					try {
						method = clas.getMethod(methodName, null);
					} catch (NoSuchMethodException | SecurityException e) {
						BaseLoggers.flowLogger.error(e.getMessage());
						throw new SystemException(e);
					}

					BaseLoggers.flowLogger
							.debug("Inside : EntityNeutrinoCachePopulatorFactory : FieldCallback : DoWith :"
									+ field.getDeclaringClass() + " : for Field : " + field.getName());
					ImpactedCacheVO impactedCacheVO = null;
					for (CustomCache annotation : customCaches.caches()) {
						createCustomCacheForField(field.getDeclaringClass(), field, annotation);
						impactedCacheVO = new ImpactedCacheVO();
						impactedCacheVO.setCacheName(annotation.name());
						impactedCacheVO.setRegionName(annotation.regionName());
						impactedCacheNameSet.add(impactedCacheVO);
					}
					CustomCacheEntityListener.markImpactedCacheVOsForEntity(field.getDeclaringClass(), fieldName,
							impactedCacheNameSet);
					CustomCacheEntityListener.markGetterMethodsForAnnotatedMethods(field.getDeclaringClass(), fieldName,
							method);
				}

			}

		};
	}

	public void process() {
		BaseLoggers.flowLogger.debug("Inside : EntityNeutrinoCachePopulatorFactory : process");
		Set<EntityType<?>> entities = em.getMetamodel().getEntities();
		for (EntityType<?> en : entities) {
			Class<?> clas = en.getJavaType();
			Cacheable cacheable = AnnotationUtils.findAnnotation(clas, Cacheable.class);
			if (cacheable == null) {
				continue;
			}
			BaseLoggers.flowLogger.debug("Inside : EntityNeutrinoCachePopulatorFactory : process : " + clas.getName());
			ReflectionUtils.doWithFields(clas, fieldCallback);
		}

	}

	@SuppressWarnings("rawtypes")
	private void createCustomCacheForField(Class clas, Field field, CustomCache customCache) {
		EntityNeutrinoCachePopulator entityNeutrinoCachePopulator = NeutrinoSpringAppContextUtil
				.getBeanByType(EntityNeutrinoCachePopulator.class);
		entityNeutrinoCachePopulator.initConfig(clas, field, customCache);
		NeutrinoCache neutrinoCache = cacheManager.createNeutrinoCacheInstance(
				entityNeutrinoCachePopulator.getCacheRegionName(), entityNeutrinoCachePopulator.getNeutrinoCacheName(), entityNeutrinoCachePopulator.getLocalCacheType());
		NeutrinoCache nullValuesNeutrinoCache = cacheManager.createNeutrinoCacheInstance(
				entityNeutrinoCachePopulator.getCacheRegionName(),
				new StringBuilder(entityNeutrinoCachePopulator.getNeutrinoCacheName())
						.append(FWCacheConstants.KEY_DELIMITER).append(FWCacheConstants.EMPTY_CACHE_NAME).toString(),
				entityNeutrinoCachePopulator.getLocalCacheType());
		cacheManager.registerNeutrinoCache(entityNeutrinoCachePopulator.getNeutrinoCacheName(),
				entityNeutrinoCachePopulator.getCacheRegionName(), neutrinoCache, nullValuesNeutrinoCache,
				entityNeutrinoCachePopulator);
	}

}
