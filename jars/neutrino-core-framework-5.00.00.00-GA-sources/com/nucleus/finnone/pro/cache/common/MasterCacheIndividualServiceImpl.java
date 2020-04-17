package com.nucleus.finnone.pro.cache.common;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;
import com.nucleus.logging.BaseLoggers;

@Named("masterCacheIndividualService")
public class MasterCacheIndividualServiceImpl implements MasterCacheIndividualService {

	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;
	
	@Inject
	@Named("masterCacheIndividual")
	private NeutrinoCachePopulator masterCacheIndividual;

	@Value("${neutrino.cache.build.initiated.timeout.seconds}")
	private int buildInitiatedTimeOut;

	@Value("${transaction.management.jtaEnabled}")
	private Boolean jtaEnabled;

	@PostConstruct
	public void init() {
		BaseLoggers.flowLogger.debug("Init called for : MasterCacheIndividualServiceImpl");
	}

	@Override
	@Transactional
	public Boolean checkIndividualCacheForBuild(ImpactedCacheVO impactedCacheVO, Long tenantId) {
		String cacheName = impactedCacheVO.getCacheName();
		String regionName = impactedCacheVO.getRegionName();

		String cacheIdentifier = new StringBuilder(regionName).append(FWCacheConstants.KEY_DELIMITER).append(cacheName)
				.toString();
		
		Boolean buildFlag = false;
		Object lock = masterCacheIndividual.acquireLock(cacheIdentifier);
		if (lock == null && jtaEnabled) {
			return buildFlag;
		}
		
		if (!masterCacheIndividual.containsKey(cacheIdentifier)) {
			masterCacheIndividual.put(cacheIdentifier,
					new ImpactedCacheVO(cacheName, regionName,
							cacheManager.getNeutrinoCachePopulatorInstance(impactedCacheVO.getRegionName(),
									impactedCacheVO.getCacheName()).getCacheGroupName(),
							DateTime.now(), MasterCacheIndividualService.FREE));
		}

		ImpactedCacheVO impactedCacheVOFromMaster = (ImpactedCacheVO) masterCacheIndividual.get(cacheIdentifier);
		if (impactedCacheVOFromMaster != null) {
			String cacheBuildStatus = impactedCacheVOFromMaster.getBuildStatus();

			if (cacheBuildStatus.equals(MasterCacheIndividualService.FREE)
					|| (cacheBuildStatus.equals(MasterCacheIndividualService.BUSY) && impactedCacheVOFromMaster
							.getLastAttemptedTime().plusSeconds(buildInitiatedTimeOut).isBefore(DateTime.now()))) {

				impactedCacheVOFromMaster.setLastAttemptedTime(DateTime.now());
				impactedCacheVOFromMaster.setBuildStatus(MasterCacheIndividualService.BUSY);
				masterCacheIndividual.put(cacheIdentifier, impactedCacheVOFromMaster);
				buildFlag = true;
			}

		}

		masterCacheIndividual.releaseLock(lock);

		return buildFlag;
	}

	@Override
	@Transactional
	public void updateTimeInMasterCacheIndividual(String cacheIdentifier) {
		BaseLoggers.flowLogger.debug("Update Timestamp in master Cache Individual for : " + cacheIdentifier);
		Object lock = masterCacheIndividual.acquireLock(cacheIdentifier);
		if (lock == null && jtaEnabled) {
			BaseLoggers.flowLogger.error(
					"Unable to acquire lock. Unable to Update Master Cache Individual For TimeStamp Update : " + cacheIdentifier);
			return;
		}
		ImpactedCacheVO impactedCacheVO = (ImpactedCacheVO) masterCacheIndividual.get(cacheIdentifier);
		if (impactedCacheVO != null) {
			impactedCacheVO.setLastAttemptedTime(DateTime.now());
			impactedCacheVO.setBuildStatus(MasterCacheIndividualService.BUSY);
			masterCacheIndividual.put(cacheIdentifier, impactedCacheVO);
		}
		masterCacheIndividual.releaseLock(lock);
	}

	@Override
	@Transactional
	public void updateMasterCacheIndividualForCompletion(String cacheIdentifier) {
		BaseLoggers.flowLogger.debug("Update Master Cache Individual For Task Completion : " + cacheIdentifier);
		Object lock = masterCacheIndividual.acquireLock(cacheIdentifier, 3);
		if (lock == null && jtaEnabled) {
			BaseLoggers.flowLogger
					.error("Unable to acquire lock. Unable to Update Master Cache Individual For Completion : " + cacheIdentifier);
			return;
		}
		ImpactedCacheVO impactedCacheVO = (ImpactedCacheVO) masterCacheIndividual.get(cacheIdentifier);
		if (impactedCacheVO != null) {
			impactedCacheVO.setLastAttemptedTime(DateTime.now());
			impactedCacheVO.setBuildStatus(MasterCacheIndividualService.FREE);
			masterCacheIndividual.put(cacheIdentifier, impactedCacheVO);
		}
		masterCacheIndividual.releaseLock(lock);
	}

}
