package com.nucleus.finnone.pro.cache.common;

import javax.inject.Named;

import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

@Named("masterCache")
public class MasterCachePopulator extends FWCachePopulator{

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.MASTER_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return null;
	}

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : MasterCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		return null;
	}

	@Override
	public void build(Long tenantId) {
		BaseLoggers.flowLogger.debug("Build Called : MasterCachePopulator");
		
	}

	@Override
	public void update(Action action, Object object) {
		BaseLoggers.flowLogger.debug("Update Called : MasterCachePopulator");
		throw new SystemException(UPDATE_ERROR_MSG + getNeutrinoCacheName());
	}
	
	@Override
	protected boolean fallbackRequired() {
		return false;
	}	

}
