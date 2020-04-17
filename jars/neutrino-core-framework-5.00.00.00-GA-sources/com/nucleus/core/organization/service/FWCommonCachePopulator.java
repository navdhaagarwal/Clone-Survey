package com.nucleus.core.organization.service;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.constants.FWCommonCacheKeys;
import com.nucleus.logging.BaseLoggers;

@Named("fwCommonCachePopulator")
public class FWCommonCachePopulator extends FWCachePopulator {

	@Inject
	@Named("organizationService")
	public OrganizationService organizationService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : FWCommonCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		if (FWCommonCacheKeys.ROOT_ORGANIZATION.equals(key)) {
			return organizationService.getRootOrganizationFromDB().getId();
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		put(FWCommonCacheKeys.ROOT_ORGANIZATION, organizationService.getRootOrganizationFromDB().getId());
	}

	@Override
	public void update(Action action, Object object) {
		BaseLoggers.flowLogger.debug("Update Called : FWCommonCachePopulator");
		throw new SystemException(UPDATE_ERROR_MSG + getNeutrinoCacheName());
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.FW_COMMON_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.FW_COMMON_CACHE_GROUP;
	}

}
