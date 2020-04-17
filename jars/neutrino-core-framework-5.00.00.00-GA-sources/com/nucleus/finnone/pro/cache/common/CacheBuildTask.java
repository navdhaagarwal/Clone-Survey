package com.nucleus.finnone.pro.cache.common;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.service.CacheCommonService;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;
import com.nucleus.logging.BaseLoggers;

@Named("cacheBuildTask")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CacheBuildTask implements ICacheBuildTask {

	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;

	@Inject
	@Named("cacheCommonService")
	private CacheCommonService cacheCommonService;

	@Inject
	@Named("masterCacheService")
	private MasterCacheService masterCacheService;

	private String cacheGroupName;
	private Set<NeutrinoCachePopulator> neutrinoCachePopulatorSet;
	private Long tenantId;
	private Map<String, String> individualCacheBuildStatus;
	

	@Override
	@Transactional(readOnly = true)
	public String call() throws Exception {
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		String currentNeutrinoCacheName = null;
		CacheBuildMonitorTask task = new CacheBuildMonitorTask();
		
		try {
			exec.scheduleAtFixedRate(task, 2, 4, TimeUnit.SECONDS);
			for (NeutrinoCachePopulator neutrinoCachePopulator : neutrinoCachePopulatorSet) {
				BaseLoggers.flowLogger.debug(
						"Build  called for NeutrinoCachePopulator : " + neutrinoCachePopulator.getNeutrinoCacheName());
				currentNeutrinoCacheName = neutrinoCachePopulator.getNeutrinoCacheName();
				cacheCommonService.buildImpactedCache(new ImpactedCacheVO(neutrinoCachePopulator.getNeutrinoCacheName(),
						neutrinoCachePopulator.getCacheRegionName()), tenantId, true);
				individualCacheBuildStatus.put(currentNeutrinoCacheName, MasterCacheService.COMPLETED);
			}
			task.flag = false;
			masterCacheService.updateMasterCacheForCompletion(cacheGroupName, individualCacheBuildStatus);

		} catch (Throwable e) {
			individualCacheBuildStatus.put(currentNeutrinoCacheName, MasterCacheService.ERROR);
			BaseLoggers.flowLogger.error("Error Occurred while Building Cache Group : " + cacheGroupName, e);			
			task.flag = false;
			masterCacheService.updateMasterCacheForError(cacheGroupName, individualCacheBuildStatus);
		} finally {
			BaseLoggers.flowLogger.debug("Shutting down timer for : " + cacheGroupName);
			exec.shutdown();
		}

		return cacheGroupName;
	}

	@Override
	public void populateDataForTask(String cacheGroupName, Long tenantId,
			Map<String, String> individualCacheBuildStatus) {
		this.tenantId = tenantId;
		this.cacheGroupName = cacheGroupName;
		this.neutrinoCachePopulatorSet = cacheManager.getPopulatorsFromGroupName(cacheGroupName);
		this.individualCacheBuildStatus = individualCacheBuildStatus;
	}

	private class CacheBuildMonitorTask implements Runnable {

		private Boolean flag = true;

		@Override
		public void run() {
			try {
				if (flag) {
					masterCacheService.updateTimeInMasterCache(cacheGroupName);
				}
			} catch (Throwable e) {
				BaseLoggers.flowLogger
						.debug("Exception caught during execution of CacheBuildMonitorTask for CacheGroupName : "
								+ cacheGroupName);
			}
		}
	}

}
