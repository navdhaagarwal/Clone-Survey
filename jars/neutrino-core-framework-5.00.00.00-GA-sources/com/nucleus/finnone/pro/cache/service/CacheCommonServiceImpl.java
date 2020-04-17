package com.nucleus.finnone.pro.cache.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.mutitenancy.service.MultiTenantService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.cache.common.CacheManager;
import com.nucleus.finnone.pro.cache.common.IndividualCacheBuildTask;
import com.nucleus.finnone.pro.cache.common.MasterCacheService;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.dao.CustomCacheDao;
import com.nucleus.finnone.pro.cache.entity.CacheBuildHistory;
import com.nucleus.finnone.pro.cache.entity.CacheUpdateType;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.entity.ImpactedCacheHistory;
import com.nucleus.finnone.pro.cache.vo.CacheMasterVO;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;

@Named("cacheCommonService")
public class CacheCommonServiceImpl extends BaseServiceImpl implements CacheCommonService {

	@Inject
	private BeanAccessHelper beanAccessHelper;

	@Inject
	@Named("customCacheDao")
	private CustomCacheDao customCacheDao;

	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;
	
	@Inject
	@Named("masterCacheService")
	private MasterCacheService masterCacheService;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("multiTenantService")
	private MultiTenantService multiTenantService;

	@Value("${neutrino.cache.impacted.cache.refresh.thread.pool}")
	private int activeTaskCount;
	
	@Value("${neutrino.cache.impacted.cache.buffer.time.in.minutes}")
	private int impactedCacheBufferTime;

	private ThreadPoolExecutor individualCacheBuildExecutorService;

	@PostConstruct
	public void initScheduler() {
		individualCacheBuildExecutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(activeTaskCount);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public CacheBuildHistory createCacheBuildHistoryEntry(CacheMasterVO cacheMasterVO) {
		return customCacheDao.persistCacheBuildHistoryEntry(cacheMasterVO);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ImpactedCache createImpactedCacheEntry(ImpactedCacheVO impactedCacheVO) {
		return customCacheDao.persistImpactedCache(impactedCacheVO);
	}

	@Override
	@Transactional
	public void removeImpactedCacheEntryImplicitly(ImpactedCache impactedCache) {
		ImpactedCacheHistory impactedCacheHistory = new ImpactedCacheHistory();
		impactedCacheHistory.prepareFromImpactedCache(impactedCache,
				genericParameterService.findByCode(CacheUpdateType.IMPLICIT, CacheUpdateType.class));
		customCacheDao.moveImpactedCacheToHistory(impactedCache, impactedCacheHistory);
	}

	@Override
	@Transactional
	public void removeImpactedCacheEntry(ImpactedCache impactedCache, CacheUpdateType cacheUpdateType) {
		ImpactedCacheHistory impactedCacheHistory = new ImpactedCacheHistory();
		impactedCacheHistory.prepareFromImpactedCache(impactedCache, cacheUpdateType);
		customCacheDao.moveImpactedCacheToHistory(impactedCache, impactedCacheHistory);
	}

	@Override
	@Transactional
	public void removeImpactedCacheEntries(Set<ImpactedCache> impactedCaches) {
		if (ValidatorUtils.hasNoElements(impactedCaches)) {
			throw new SystemException("Set of ImpactedCaches is Empty or Null");
		}
		CacheUpdateType cacheUpdateType = genericParameterService.findByCode(CacheUpdateType.IMPLICIT,
				CacheUpdateType.class);
		for (ImpactedCache impactedCache : impactedCaches) {
			removeImpactedCacheEntry(impactedCache, cacheUpdateType);
		}
	}

	@Override
	public void updateImpactedCachesInPostTransaction(Map<String, Object> fieldNameToFieldValueMap,
			Map<String, Object> fieldNameToOldFieldValueMap,
			Map<String, Set<ImpactedCacheVO>> fieldNameToImpactedCacheVOMap,
			Map<ImpactedCacheVO, ImpactedCache> impactedCacheVOToImpactedCacheMap) {

		for (Map.Entry<String, Object> entry : fieldNameToFieldValueMap.entrySet()) {
			updateImpactedCachesForField(fieldNameToImpactedCacheVOMap.get(entry.getKey()),
					impactedCacheVOToImpactedCacheMap, entry.getValue(),
					fieldNameToOldFieldValueMap.get(entry.getKey()));
		}

	}

	@Transactional
	@Override
	public void updateImpactedCachesForField(Set<ImpactedCacheVO> impactedCacheVOSet,
			Map<ImpactedCacheVO, ImpactedCache> impactedCacheVOToImpactedCacheMap, Object fieldValue,
			Object oldFieldValue) {
		NeutrinoCachePopulator cachePopulator;
		for (ImpactedCacheVO impactedCacheVO : impactedCacheVOSet) {
			cachePopulator = cacheManager.getNeutrinoCachePopulatorInstance(impactedCacheVO.getRegionName(),
					impactedCacheVO.getCacheName());
			if (oldFieldValue != null) {
				cachePopulator.update(Action.DELETE, oldFieldValue);
			}
			cachePopulator.update(Action.UPDATE, fieldValue);
			removeImpactedCacheEntryImplicitly(impactedCacheVOToImpactedCacheMap.get(impactedCacheVO));
			impactedCacheVOToImpactedCacheMap.remove(impactedCacheVO);
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <T extends BaseEntity> Object getCurrentFieldValue(String fieldName, Long id, Class<T> entityClass)
			throws IllegalAccessException, InvocationTargetException {
		return customCacheDao.getCurrentFieldValue(entityClass, id, fieldName);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Map<String, ImpactedCache> getImpactedCachesFromCacheNames(String regionName, String... cacheNames) {
		Map<String, ImpactedCache> impactedCacheMap = new HashMap<>();
		for (String cacheName : cacheNames) {
			ImpactedCacheVO impactedCacheVO = new ImpactedCacheVO();
			impactedCacheVO.setCacheName(cacheName);
			impactedCacheVO.setRegionName(regionName);
			impactedCacheMap.put(
					new StringBuilder(regionName).append(FWCacheConstants.KEY_DELIMITER).append(cacheName).toString(),
					createImpactedCacheEntry(impactedCacheVO));
		}
		return impactedCacheMap;
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(NeutrinoCachePopulator neutrinoCachePopulator, Object key) {
		return neutrinoCachePopulator.fallback(key);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ImpactedCacheVO> getFailedImpactedCacheVOs() {
		List<ImpactedCacheVO> impactedCacheVOs = customCacheDao
				.getFailedImpactedCache(DateTime.now().minusMinutes(impactedCacheBufferTime));
		if (ValidatorUtils.hasElements(impactedCacheVOs)) {
			return impactedCacheVOs;
		}
		return Collections.emptyList();
	}

	@Override
	@Transactional
	public void buildImpactedCache(ImpactedCacheVO impactedCacheVO, Long tenantId, Boolean buildAnyway)
			throws Exception {
		buildImpactedCache(impactedCacheVO, tenantId, null, buildAnyway);
	}

	@Override
	@Transactional
	public void buildImpactedCache(ImpactedCacheVO impactedCacheVO, Long tenantId, DateTime currentTime,
			Boolean buildAnyway) throws Exception {
		NeutrinoValidator.notNull(impactedCacheVO, "Impacted Cache VO cannot be NULL");

		if (buildAnyway || ValidatorUtils
				.hasElements(customCacheDao.getImpactedCachesByImpactedCacheVO(impactedCacheVO, currentTime))) {
			try {
				callIndividualCacheBuildTask(impactedCacheVO, tenantId, currentTime, buildAnyway).get();
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage());
				BaseLoggers.exceptionLogger
						.error("Error Occurred in Building Neutrino Cache : " + impactedCacheVO.toString(), e);
				throw e;
			}
		}
	}

	@Override
	public void buildImpactedCaches(String[] cacheIdentifierSet) {
		if (cacheIdentifierSet == null || cacheIdentifierSet.length <= 0) {
			return;
		}
		ImpactedCacheVO impactedCacheVO = null;
		Long tenantId = multiTenantService.getDefaultTenantId();
		for (String cacheIdentifier : cacheIdentifierSet) {
			try {
				String[] keyArray = cacheIdentifier.split(FWCacheConstants.CACHE_IDENTIFER_DELIMITER);
				impactedCacheVO = new ImpactedCacheVO(keyArray[1], keyArray[0]);
				callIndividualCacheBuildTask(impactedCacheVO, tenantId, null, false);
			} catch (Exception e) {
				BaseLoggers.exceptionLogger
						.error("ERROR OCCURRED WHILE BUILDING IMPACTED CACHE : " + impactedCacheVO.toString(), e);
			}
		}

	}

	@Override
	@Transactional
	public Boolean updateImpactedCacheLastUpdatedTime(String[] cacheIdentifierSet) {
		if (cacheIdentifierSet == null || cacheIdentifierSet.length <= 0) {
			return false;
		}
		ImpactedCacheVO impactedCacheVO = null;
		DateTime currentTime = DateTime.now();
		for (String cacheIdentifier : cacheIdentifierSet) {
			try {
				String[] keyArray = cacheIdentifier.split(FWCacheConstants.CACHE_IDENTIFER_DELIMITER);
				impactedCacheVO = new ImpactedCacheVO(keyArray[1], keyArray[0]);
				customCacheDao.updateImpactedCacheLastUpdatedTime(impactedCacheVO, currentTime);
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error("Error occurred while updating LAST UPDATED TIME of IMPACTED CACHE : "
						+ (impactedCacheVO != null ? impactedCacheVO.toString() : null), e);
				return false;
			}
		}
		return true;
	}
	
	@Override
	@Transactional
	public void updateImpactedCacheLastUpdatedTime(String groupName) {
		NeutrinoValidator.notEmpty(groupName, "Cache Group Name cannot be NULL or EMPTY");

		try {
			customCacheDao.updateImpactedCacheLastUpdatedTimeByGroup(groupName, DateTime.now());
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error(
					"Error occurred while updating LAST UPDATED TIME of IMPACTED CACHEs for Cache Group : " + groupName,
					e);
			throw e;
		}
	}
	
	@Override
	@Transactional
	public Boolean checkForFailedImpactedCacheByGroupName(String groupName) {
		try {
			Long impactedCacheCount = customCacheDao.getFailedImpactedCacheCountByGroupName(groupName,
					DateTime.now().minusMinutes(impactedCacheBufferTime));
			if (impactedCacheCount > 0) {
				return true;
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error(
					"Error occurred while checking for existence of Failed Impacted Cache for Cache Group Name : "
							+ groupName,
					e);
		}

		return false;
	}
	
	@Override
	public Map<String, String> getBuildStatusForCacheGroups(Set<String> cacheGroupNames) {
		NeutrinoValidator.notEmpty(cacheGroupNames, "Cache Group Names Set is NULL or EMPTY");
		Map<String, String> statusMap = new HashMap<>();
		for (String cacheGroupName : cacheGroupNames) {
			statusMap.put(cacheGroupName, masterCacheService.getBuildStatusForCacheGroup(cacheGroupName));
		}
		return statusMap;
	}

	private Future<String> callIndividualCacheBuildTask(ImpactedCacheVO impactedCacheVO, Long tenantId,
			DateTime currentTime, Boolean buildAnyway) {
		IndividualCacheBuildTask cacheBuildTask = beanAccessHelper.getBean("individualCacheBuildTask",
				IndividualCacheBuildTask.class);
		cacheBuildTask.populateDataForTask(impactedCacheVO, currentTime, tenantId, buildAnyway);
		return individualCacheBuildExecutorService.submit(cacheBuildTask);
	}

}
