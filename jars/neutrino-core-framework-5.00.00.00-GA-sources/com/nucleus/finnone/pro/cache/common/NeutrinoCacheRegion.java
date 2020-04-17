package com.nucleus.finnone.pro.cache.common;

import com.nucleus.logging.BaseLoggers;

public class NeutrinoCacheRegion {

	public NeutrinoCacheRegion(CacheManager neutrinoCacheManager) {
		super();
		BaseLoggers.flowLogger
				.debug("NeutrinoCacheRegion created within Cache Manager : " + neutrinoCacheManager.getClass());
	}

}
