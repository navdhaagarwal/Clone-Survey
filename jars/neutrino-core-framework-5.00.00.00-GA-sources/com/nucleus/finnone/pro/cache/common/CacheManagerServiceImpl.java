package com.nucleus.finnone.pro.cache.common;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;

@Named("cacheManagerService")
public class CacheManagerServiceImpl implements CacheManagerService {

	@Inject
	private BeanAccessHelper beanAccessHelper;

	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;

	@Inject
	@Named("masterCacheService")
	private MasterCacheService masterCacheService;

	@Value("${neutrino.cache.build.thread.pool}")
	private int activeTaskCount;

	@Value("${neutrino.cache.build.scheduler.interval.in.millis}")
	private long schedulerIntervalInMilis;

	private ThreadPoolExecutor cacheBuildExecutorService;

	public long getSchedulerIntervalInMilis() {
		return schedulerIntervalInMilis;
	}

	@PostConstruct
	public void initScheduler() {
		cacheBuildExecutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(activeTaskCount);
	}

	@Override
	public void buildCaches(Long tenantId) {
		if (cacheManager.getIsContextInitializedForBuild() && cacheBuildExecutorService != null) {
			for (String cacheGroupName : cacheManager.getSetOfCacheGroupNames()) {
				if (checkForAvailabilityOfThread()) {
					Map<String, String> individualCacheBuildStatus = masterCacheService
							.checkCacheForBuild(cacheGroupName, tenantId);
					if (individualCacheBuildStatus != null) {
						callBuildCacheTask(cacheGroupName, tenantId, individualCacheBuildStatus);
					}

				}
			}
		}
	}

	private boolean checkForAvailabilityOfThread() {
		return cacheBuildExecutorService.getActiveCount() < cacheBuildExecutorService.getCorePoolSize();
	}

	private void callBuildCacheTask(String cacheGroupName, Long tenantId,
			Map<String, String> individualCacheBuildStatus) {
		ICacheBuildTask cacheBuildTask = beanAccessHelper.getBean("cacheBuildTask", CacheBuildTask.class);
		cacheBuildTask.populateDataForTask(cacheGroupName, tenantId, individualCacheBuildStatus);
		cacheBuildExecutorService.submit(cacheBuildTask);
	}

}
