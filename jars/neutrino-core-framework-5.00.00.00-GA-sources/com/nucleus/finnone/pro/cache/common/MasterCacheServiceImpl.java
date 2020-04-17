package com.nucleus.finnone.pro.cache.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.service.CacheCommonService;
import com.nucleus.finnone.pro.cache.vo.CacheBuildStatusVO;
import com.nucleus.finnone.pro.cache.vo.CacheMasterVO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("masterCacheService")
public class MasterCacheServiceImpl implements MasterCacheService {

	@Inject
	@Named("masterCache")
	private NeutrinoCachePopulator masterCache;

	@Inject
	@Named("cacheCommonService")
	private CacheCommonService cacheCommonService;

	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;

	@Value("${neutrino.cache.build.initiated.timeout.seconds}")
	private int buildInitiatedTimeOut;

	@Value("${neutrino.cache.build.max.failed.attempts}")
	private int maxFailedAttempts;
	
	@Value("${transaction.management.jtaEnabled}")
	private Boolean jtaEnabled;

	protected static final int[] fibonacciSeries = { 1, 1, 2, 3, 5, 8, 13 };

	@PostConstruct
	public void init() {
		if (maxFailedAttempts >= fibonacciSeries.length || maxFailedAttempts < 1) {
			maxFailedAttempts = fibonacciSeries.length - 1;
		}
	}

	@Override
	public int getMaxFailedAttempts() {
		return maxFailedAttempts;
	}

	@Override
	@Transactional
	public Map<String, String> checkCacheForBuild(String cacheGroupName, Long tenantId) {

		Map<String, String> individualCacheBuildStatus = null;
		
		Object lock = masterCache.acquireLock(cacheGroupName);
		if (lock == null && jtaEnabled) {
			return individualCacheBuildStatus;
		}
		
		if (!masterCache.containsKey(cacheGroupName)) {
			CacheMasterVO cacheMasterVO = new CacheMasterVO(cacheGroupName,
					cacheManager.getPopulatorsFromGroupName(cacheGroupName));
			cacheCommonService.createCacheBuildHistoryEntry(cacheMasterVO);
			masterCache.put(cacheGroupName, cacheMasterVO);
		}
		
		CacheMasterVO cacheMasterVO = (CacheMasterVO) masterCache.get(cacheGroupName);
		if(cacheMasterVO != null) {
			String cacheBuildStatus = cacheMasterVO.getBuildStatus();
			Boolean buildFlag = false;
			
			if (cacheMasterVO.getForceBuild()) {
				buildFlag = true;
			} else if (cacheBuildStatus.equals(MasterCacheService.COMPLETED) && cacheMasterVO.getLastSuccessTime()
					.plusMinutes(cacheManager.getReaperIntervalForGroupName(cacheGroupName)).isBefore(DateTime.now())) {
				buildFlag = true;
			} else if (cacheBuildStatus.equals(MasterCacheService.NOT_INITIATED)) {
				buildFlag = true;
			} else if (cacheBuildStatus.equals(MasterCacheService.INITIATED)
					&& cacheMasterVO.getLastUpdatedTime().plusSeconds(buildInitiatedTimeOut).isBefore(DateTime.now())) {
				buildFlag = true;
			} else if (cacheBuildStatus.equals(MasterCacheService.ERROR) && cacheMasterVO.getLastUpdatedTime()
					.plusMinutes(fibonacciSeries[cacheMasterVO.getFailedAttempt()] * 2).isBefore(DateTime.now())) {
				buildFlag = true;
			} else if (!cacheBuildStatus.equals(MasterCacheService.INITIATED)
					&& cacheCommonService.checkForFailedImpactedCacheByGroupName(cacheGroupName)) {
				buildFlag = true;
			}

			if (buildFlag) {
				cacheMasterVO.setLastUpdatedTime(DateTime.now());
				cacheMasterVO.setForceBuild(false);
				cacheMasterVO.setBuildStatus(MasterCacheService.INITIATED);
				updateIpAddress(cacheMasterVO);
				individualCacheBuildStatus = cacheMasterVO.getIndividualCacheBuildStatus();
				cacheCommonService.updateImpactedCacheLastUpdatedTime(cacheGroupName);
				cacheCommonService.createCacheBuildHistoryEntry(cacheMasterVO);
				masterCache.put(cacheGroupName, cacheMasterVO);
			}
		}
		masterCache.releaseLock(lock);
		
		return individualCacheBuildStatus;
	}

	@Override
	@Transactional
	public void updateTimeInMasterCache(String cacheGroupName) {
		BaseLoggers.flowLogger.debug("Update Timestamp in master Cache for : " + cacheGroupName);
		Object lock = masterCache.acquireLock(cacheGroupName);
		if (lock == null && jtaEnabled) {
			BaseLoggers.flowLogger
					.error("Unable to acquire lock. Unable to Update Master Cache For TimeStamp Update : " + cacheGroupName);
			return;
		}
		CacheMasterVO cacheMasterVO = (CacheMasterVO) masterCache.get(cacheGroupName);
		if(cacheMasterVO != null) {
			cacheMasterVO.setLastUpdatedTime(DateTime.now());
			masterCache.put(cacheGroupName, cacheMasterVO);
		}
		masterCache.releaseLock(lock);
	}

	@Override
	@Transactional
	public void updateMasterCacheForCompletion(String cacheGroupName, Map<String, String> individualCacheBuildStatus) {
		BaseLoggers.flowLogger.debug("Update Master Cache For Completion : " + cacheGroupName);
		Object lock = masterCache.acquireLock(cacheGroupName, 3);
		if (lock == null && jtaEnabled) {
			BaseLoggers.flowLogger
					.error("Unable to acquire lock. Unable to Update Master Cache For Completion : " + cacheGroupName);
			return;
		}
		CacheMasterVO cacheMasterVO = (CacheMasterVO) masterCache.get(cacheGroupName);
		if (cacheMasterVO != null) {
			cacheMasterVO.setLastUpdatedTime(DateTime.now());
			cacheMasterVO.setLastSuccessTime(DateTime.now());
			cacheMasterVO.setFailedAttempt(0);
			cacheMasterVO.setBuildStatus(MasterCacheService.COMPLETED);
			cacheMasterVO.setIndividualCacheBuildStatus(individualCacheBuildStatus);
			updateIpAddress(cacheMasterVO);
			masterCache.put(cacheGroupName, cacheMasterVO);
			cacheCommonService.createCacheBuildHistoryEntry(cacheMasterVO);
		}
		masterCache.releaseLock(lock);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void updateMasterCacheForError(String cacheGroupName, Map<String, String> individualCacheBuildStatus) {
		BaseLoggers.flowLogger.debug("Update Master Cache For Error : " + cacheGroupName);
		Object lock = masterCache.acquireLock(cacheGroupName, 3);
		if (lock == null && jtaEnabled) {
			BaseLoggers.flowLogger
					.error("Unable to acquire lock. Unable to Update Master Cache For Error : " + cacheGroupName);
			return;
		}
		CacheMasterVO cacheMasterVO = (CacheMasterVO) masterCache.get(cacheGroupName);
		if (cacheMasterVO != null) {
			cacheMasterVO.setLastUpdatedTime(DateTime.now());
			if (cacheMasterVO.getFailedAttempt() < maxFailedAttempts) {
				cacheMasterVO.setFailedAttempt((cacheMasterVO.getFailedAttempt() + 1));
			}
			cacheMasterVO.setBuildStatus(MasterCacheService.ERROR);
			cacheMasterVO.setIndividualCacheBuildStatus(individualCacheBuildStatus);
			updateIpAddress(cacheMasterVO);
			cacheCommonService.createCacheBuildHistoryEntry(cacheMasterVO);
			masterCache.put(cacheGroupName, cacheMasterVO);
		}
		masterCache.releaseLock(lock);
	}
	
	@Override
	public List<CacheMasterVO> getCacheGroupStatusFromMasterCache(String regionName) {
		CacheMasterVO cacheMasterVO = null;
		List<CacheMasterVO> cacheMasterVOList = new ArrayList<>();
		for (String cacheGroupName : cacheManager.getCacheGroupsFromCacheRegionName(regionName)) {
			cacheMasterVO = (CacheMasterVO) masterCache.get(cacheGroupName);
			if (cacheMasterVO != null) {
				cacheMasterVOList.add(cacheMasterVO);
			}
		}

		return cacheMasterVOList;
	}
	
	@Override
	public String getBuildStatusForCacheGroup(String cacheGroupName) {
		String buildStatus = NOT_INITIATED;
		CacheMasterVO cacheMasterVO = (CacheMasterVO) masterCache.get(cacheGroupName);
		if (cacheMasterVO != null) {
			buildStatus = cacheMasterVO.getBuildStatus();
		}
		return buildStatus;
	}
	
	@Override
	public List<Object> getIndividualCacheStatusFromCacheGroup(String groupName) {
		List<Object> cacheBuildStatusVOList = new ArrayList<>();
		CacheMasterVO cacheMasterVO = (CacheMasterVO) masterCache.get(groupName);
		if (cacheMasterVO != null) {
			Map<String, String> map = cacheMasterVO.getIndividualCacheBuildStatus();
			for (Map.Entry<String, String> entry : map.entrySet()) {
				cacheBuildStatusVOList.add(new CacheBuildStatusVO(entry.getKey(), entry.getValue()));
			}
		}

		return cacheBuildStatusVOList;
	}
	
	@Override
	@Transactional
	public Boolean markCacheGroupListForRefresh(List<String> cacheGroupNames) {

		if (!ValidatorUtils.hasElements(cacheGroupNames)) {
			return false;
		}
		Boolean cachesMarkedForRefresh = true;

		for (String cacheGroupName : cacheGroupNames) {
			Object lock = masterCache.acquireLock(cacheGroupName, 3);
			if (lock == null && jtaEnabled) {
				BaseLoggers.flowLogger
						.error("Unable to acquire lock. Unable to Update Master Cache For Error : " + cacheGroupName);
				cachesMarkedForRefresh = false;
				continue;
			}
			CacheMasterVO cacheMasterVO = (CacheMasterVO) masterCache.get(cacheGroupName);
			if (cacheMasterVO != null) {
				if (!cacheMasterVO.getBuildStatus().equals(MasterCacheService.INITIATED)
						&& !cacheMasterVO.getForceBuild()) {
					cacheMasterVO.setLastUpdatedTime(DateTime.now());
					cacheMasterVO.setForceBuild(true);
					updateIpAddress(cacheMasterVO);
					cacheCommonService.createCacheBuildHistoryEntry(cacheMasterVO);
					masterCache.put(cacheGroupName, cacheMasterVO);
				}
			} else {
				cachesMarkedForRefresh = false;
			}
			masterCache.releaseLock(lock);
		}

		return cachesMarkedForRefresh;
	}

	private void updateIpAddress(CacheMasterVO cacheMasterVO) {
		try {
			cacheMasterVO.setIpAddress(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			BaseLoggers.flowLogger.error(e.getMessage(), e);
		}
	}

}
