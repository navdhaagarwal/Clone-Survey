package com.nucleus.finnone.pro.cache.common;

import org.springframework.context.annotation.DependsOn;

import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;

@DependsOn(FWCacheConstants.FW_CACHE_REGION)
public abstract class FWCachePopulator extends NeutrinoCachePopulator {

	protected static final String UPDATE_ERROR_MSG = "Update not allowed for POPULATOR of CACHE : ";

	public String getCacheRegionName() {
		return FWCacheConstants.FW_CACHE_REGION;
	}

}
