package com.nucleus.finnone.pro.cache.common;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.mutitenancy.service.MultiTenantService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.dao.CustomCacheDao;
import com.nucleus.finnone.pro.cache.entity.CacheUpdateType;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.entity.ImpactedCacheHistory;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("individualCacheBuildTask")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndividualCacheBuildTaskImpl implements IndividualCacheBuildTask {

	private DateTime currentTime;
	private ImpactedCacheVO impactedCacheVO;
	private Long tenantId;
	private Boolean buildAnyway;
	private String cacheIdentifier;

	@Inject
	@Named("customCacheDao")
	private CustomCacheDao customCacheDao;

	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("multiTenantService")
	private MultiTenantService multiTenantService;

	@Inject
	@Named("masterCacheIndividualService")
	private MasterCacheIndividualService masterCacheIndividualService;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String call() throws Exception {

		List<ImpactedCache> impactedCaches = customCacheDao.getImpactedCachesByImpactedCacheVO(impactedCacheVO,
				currentTime);
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		IndividualCacheBuildMonitorTask task = new IndividualCacheBuildMonitorTask();

		try {
			if (buildAnyway || (ValidatorUtils.hasElements(impactedCaches)
					&& masterCacheIndividualService.checkIndividualCacheForBuild(impactedCacheVO, tenantId))) {
				masterCacheIndividualService.updateTimeInMasterCacheIndividual(cacheIdentifier);

				exec.scheduleAtFixedRate(task, 2, 4, TimeUnit.SECONDS);
				NeutrinoCachePopulator ncp = cacheManager.getNeutrinoCachePopulatorInstance(
						impactedCacheVO.getRegionName(), impactedCacheVO.getCacheName());
				ncp.build(tenantId);
				ncp.removeExpiredKeys();

				CacheUpdateType cacheUpdateType = genericParameterService.findByCode(CacheUpdateType.EXPLICIT,
						CacheUpdateType.class);
				for (ImpactedCache impactedCache : impactedCaches) {
					ImpactedCacheHistory impactedCacheHistory = new ImpactedCacheHistory();
					impactedCacheHistory.prepareFromImpactedCache(impactedCache, cacheUpdateType);
					customCacheDao.moveImpactedCacheToHistory(impactedCache, impactedCacheHistory);
				}
				task.flag = false;
				masterCacheIndividualService.updateMasterCacheIndividualForCompletion(cacheIdentifier);
			}
		} finally {
			BaseLoggers.flowLogger.debug("Shutting down timer for : " + cacheIdentifier);
			exec.shutdown();

		}

		return null;
	}

	@Override
	public void populateDataForTask(ImpactedCacheVO impactedCacheVO, DateTime currentTime, Long tenantId,
			Boolean buildAnyway) {
		NeutrinoValidator.notNull(impactedCacheVO, "Impacted Cache VO cannot be NULL");
		NeutrinoValidator.notNull(tenantId, "Tenant ID cannot be NULL");

		this.impactedCacheVO = impactedCacheVO;
		this.buildAnyway = buildAnyway;
		this.cacheIdentifier = new StringBuilder(impactedCacheVO.getRegionName()).append(FWCacheConstants.KEY_DELIMITER)
				.append(impactedCacheVO.getCacheName()).toString();

		if (tenantId == null) {
			this.tenantId = multiTenantService.getDefaultTenantId();
		} else {
			this.tenantId = tenantId;
		}

		if (currentTime == null) {
			this.currentTime = DateTime.now();
		} else {
			this.currentTime = currentTime;
		}
	}

	private class IndividualCacheBuildMonitorTask implements Runnable {

		private Boolean flag = true;

		@Override
		public void run() {
			try {
				if (flag) {
					masterCacheIndividualService.updateTimeInMasterCacheIndividual(cacheIdentifier);
				}
			} catch (Throwable e) {
				BaseLoggers.flowLogger.debug(
						"Exception caught during execution of IndividualCacheBuildMonitorTask for CacheGroupName : "
								+ cacheIdentifier);
			}
		}
	}

}
