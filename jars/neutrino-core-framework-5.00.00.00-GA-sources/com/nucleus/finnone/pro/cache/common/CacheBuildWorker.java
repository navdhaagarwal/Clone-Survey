package com.nucleus.finnone.pro.cache.common;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.mutitenancy.service.MultiTenantService;

@Named("cacheBuildWorker")
public class CacheBuildWorker {

	@Inject
	@Named("multiTenantService")
	private MultiTenantService multiTenantService;

	@Inject
	@Named("cacheManagerService")
	private CacheManagerService cacheManagerService;

	public void work() {
		Long tenantId = multiTenantService.getDefaultTenantId();
		cacheManagerService.buildCaches(tenantId);
	}
}
