package com.nucleus.finnone.pro.cache.common;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.beans.config.PropertiesBeanPostProcessor;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.mutitenancy.service.MultiTenantService;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

public abstract class CacheManager {
	public static final String NA = "NA";
	public static final String LOCAL = "LOCAL";
	private static final String TTL_PREFIX = "neutrino.cache.ttl.in.minutes.";
	private static final String REAPER_PREFIX = "neutrino.cache.reaper.interval.in.minutes.";

	private static final Map<String, NeutrinoCache> namedCacheMap = new ConcurrentHashMap<>();
	private static final Map<String, NeutrinoCache> namedCacheWithRegionNameMap = new ConcurrentHashMap<>();
	private static final Map<String, NeutrinoCachePopulator> namedCachePopulatorMap = new ConcurrentHashMap<>();
	private static final Map<String, Set<NeutrinoCachePopulator>> cacheGroupMap = new ConcurrentHashMap<>();
	private static final Map<String, Set<String>> cacheRegionToCacheGroupMap = new ConcurrentHashMap<>();
	private static final Map<String, Integer> cacheGroupReaperIntervalMap = new ConcurrentHashMap<>();
	private static final Map<String, Long> cacheGroupTTLMap = new ConcurrentHashMap<>();
	private static final Set<String> cacheNames = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private static final Set<String> cacheRegionNames = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private Boolean isContextInitializedForBuild;
	
	@Value("${transaction.management.jtaEnabled}")
	private Boolean jtaEnabled;
	
	@Value("${neutrino.cache.ttl.in.minutes}")
	private Long timeToLiveInMinutesGlobal;

	@Value("${neutrino.cache.reaper.interval.in.minutes}")
	private int reaperIntervalGlobal;

	@Inject
	@Named("multiTenantService")
	private MultiTenantService multiTenantService;

	@Inject
	@Named("cacheManagerService")
	private CacheManagerService cacheManagerService;

	protected Boolean isJtaEnabled() {
		return jtaEnabled;
	}
	
	protected Long getTimeToLiveInMinutesGlobal() {
		return timeToLiveInMinutesGlobal;
	}

	public Boolean getIsContextInitializedForBuild() {
		if (isContextInitializedForBuild == null) {
			return false;
		}
		return isContextInitializedForBuild;
	}

	public void setIsContextInitializedForBuild(Boolean isContextInitializedForBuild) {
		this.isContextInitializedForBuild = isContextInitializedForBuild;
	}

	public Set<String> getCacheNames() {
		return cacheNames;
	}
	
	public Set<String> getCacheRegionNames() {
		return cacheRegionNames;
	}

	@Deprecated
	public NeutrinoCache getNeutrinoCacheInstance(String cacheName) {
		if (cacheName == null || cacheName.isEmpty()) {
			return null;
		}
		NeutrinoCache neutrinoCache = namedCacheMap.get(cacheName);
		if (neutrinoCache == null) {
			BaseLoggers.flowLogger.error("No UNIQUE Neutrino Cache with name " + cacheName + " found.");
			throw new BusinessException("No UNIQUE Neutrino Cache with name " + cacheName + " found.");
		}
		return neutrinoCache;
	}

	@Deprecated
	public NeutrinoCache getNeutrinoCacheInstance(String regionName, String cacheName) {
		if (regionName == null || regionName.isEmpty() || cacheName == null || cacheName.isEmpty()) {
			return null;
		}
		return namedCacheWithRegionNameMap.get(regionName + FWCacheConstants.KEY_DELIMITER + cacheName);
	}

	public NeutrinoCachePopulator getNeutrinoCachePopulatorInstance(String regionName, String cacheName) {
		if (regionName == null || regionName.isEmpty() || cacheName == null || cacheName.isEmpty()) {
			return null;
		}
		return namedCachePopulatorMap.get(regionName + FWCacheConstants.KEY_DELIMITER + cacheName);
	}

	public Set<NeutrinoCachePopulator> getPopulatorsFromGroupName(String cacheGroupName) {
		if (cacheGroupName == null || cacheGroupName.isEmpty()) {
			return Collections.emptySet();
		}
		return cacheGroupMap.get(cacheGroupName);
	}
	
	public Set<String> getCacheGroupsFromCacheRegionName(String regionName) {
		if (regionName == null || regionName.isEmpty()) {
			return Collections.emptySet();
		}
		return cacheRegionToCacheGroupMap.get(regionName);
	}

	public int getReaperIntervalForGroupName(String cacheGroupName) {
		if (cacheGroupName == null || cacheGroupName.isEmpty()) {
			return reaperIntervalGlobal;
		}
		return cacheGroupReaperIntervalMap.get(cacheGroupName);
	}

	public Set<String> getSetOfCacheGroupNames() {
		return cacheGroupMap.keySet();
	}

	protected void registerNeutrinoCache(String cacheName, String regionName, NeutrinoCache neutrinoCache,
			NeutrinoCache nullValuesNeutrinoCache, NeutrinoCachePopulator neutrinoCachePopulator) {
		registerCacheRegionName(regionName);
		neutrinoCachePopulator.setNeutrinoCache(neutrinoCache);
		neutrinoCachePopulator.setNullValuesNeutrinoCache(nullValuesNeutrinoCache);

		neutrinoCache.setJtaEnabled(jtaEnabled);
		nullValuesNeutrinoCache.setJtaEnabled(jtaEnabled);
		
		String cacheIdentifier = regionName + FWCacheConstants.KEY_DELIMITER + cacheName;
		cacheNames.add(cacheIdentifier);
		
		initializeTTLForIndividualCache(neutrinoCache, nullValuesNeutrinoCache, cacheIdentifier);
		processCacheGroupForPopulator(regionName,neutrinoCache,neutrinoCachePopulator,nullValuesNeutrinoCache);

		if (namedCacheMap.containsKey(cacheName)) {
			namedCacheMap.remove(cacheName);
		} else {
			namedCacheMap.put(cacheName, neutrinoCache);
		}

		namedCacheWithRegionNameMap.put(cacheIdentifier, neutrinoCache);
		namedCachePopulatorMap.put(cacheIdentifier, neutrinoCachePopulator);
		BaseLoggers.flowLogger
				.error("CacheManager : registering NeutrinoCache : " + cacheName + " for Region : " + regionName);

	}
	
	private void registerCacheRegionName(String regionName) {
		if(!cacheRegionNames.contains(regionName)) {
			cacheRegionNames.add(regionName);
		}
	}
	
	private void processCacheGroupForPopulator(String regionName, NeutrinoCache neutrinoCache, NeutrinoCachePopulator neutrinoCachePopulator, NeutrinoCache nullValuesNeutrinoCache) {
		String cacheGroupName = neutrinoCachePopulator.getCacheGroupName();
		if (cacheGroupName != null && !cacheGroupName.isEmpty()) {
			Set<NeutrinoCachePopulator> neutrinoCachePopulatorSet = cacheGroupMap.get(cacheGroupName);
			if (neutrinoCachePopulatorSet == null) {
				neutrinoCachePopulatorSet = ConcurrentHashMap.newKeySet();
				cacheGroupMap.put(cacheGroupName, neutrinoCachePopulatorSet);
			}
			Set<String> cacheGroupSet = cacheRegionToCacheGroupMap.get(regionName);
			if(cacheGroupSet == null) {
				cacheGroupSet = ConcurrentHashMap.newKeySet();
				cacheRegionToCacheGroupMap.put(regionName, cacheGroupSet);
			}
			if(!cacheGroupSet.contains(cacheGroupName)) {
				cacheGroupSet.add(cacheGroupName);
			}
			neutrinoCachePopulatorSet.add(neutrinoCachePopulator);
			initializeReaperAndTTL(neutrinoCache, nullValuesNeutrinoCache, cacheGroupName);
		}
	}
	
	private void initializeTTLForIndividualCache(NeutrinoCache neutrinoCache, NeutrinoCache nullValuesNeutrinoCache,
			String cacheIdentifier) {
		Long timeToLiveInMinutes = timeToLiveInMinutesGlobal;
		String tempStr = (String) PropertiesBeanPostProcessor
				.getPropValue(new StringBuilder(TTL_PREFIX).append(cacheIdentifier).toString());
		if (tempStr != null && !tempStr.isEmpty()) {
			timeToLiveInMinutes = Long.parseLong(tempStr);
		}
		neutrinoCache.setTimeToLiveInMinutes(timeToLiveInMinutes);
		nullValuesNeutrinoCache.setTimeToLiveInMinutes(timeToLiveInMinutes);
	}

	private void initializeReaperAndTTL(NeutrinoCache neutrinoCache, NeutrinoCache nullValuesNeutrinoCache,
			String cacheGroupName) {
		Long timeToLiveInMinutes = timeToLiveInMinutesGlobal;
		int reaperInterval = reaperIntervalGlobal;
		
		if (cacheGroupTTLMap.containsKey(cacheGroupName)) {
			timeToLiveInMinutes = cacheGroupTTLMap.get(cacheGroupName);
		} else {
			String tempStr = (String) PropertiesBeanPostProcessor
					.getPropValue(new StringBuilder(TTL_PREFIX).append(cacheGroupName).toString());
			if (tempStr != null && !tempStr.isEmpty()) {
				timeToLiveInMinutes = Long.parseLong(tempStr);				
			}
			cacheGroupTTLMap.put(cacheGroupName, timeToLiveInMinutes);
		}
		neutrinoCache.setTimeToLiveInMinutes(timeToLiveInMinutes);
		nullValuesNeutrinoCache.setTimeToLiveInMinutes(timeToLiveInMinutes);

		if (!cacheGroupReaperIntervalMap.containsKey(cacheGroupName)) {
			String tempStr = (String) PropertiesBeanPostProcessor
					.getPropValue(new StringBuilder(REAPER_PREFIX).append(cacheGroupName).toString());
			if (tempStr != null && !tempStr.isEmpty()) {
				reaperInterval = Integer.parseInt(tempStr);
			}
			if (reaperInterval <= 0) {
				throw new SystemException(
						"REAPER INTERVAL cannot be less than or equal to 0 : CACHE GROUP NAME: " + cacheGroupName);
			}
			cacheGroupReaperIntervalMap.put(cacheGroupName, reaperInterval);
		}

	}

	public void startCacheManager() {
		NeutrinoSpringAppContextUtil
				.getBeanByName("entityNeutrinoCachePopulatorFactory", EntityNeutrinoCachePopulatorFactory.class)
				.process();
		isContextInitializedForBuild = true;
	}

	protected abstract NeutrinoCache createNeutrinoCacheInstance(String cacheRegion, String cacheName, String localCacheType);

}
