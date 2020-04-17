package com.nucleus.finnone.pro.cache.common.infinispan;

import java.io.IOException;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.common.CacheManager;
import com.nucleus.finnone.pro.cache.common.NeutrinoCacheRegion;
import com.nucleus.logging.BaseLoggers;

public class NeutrinoCacheRegionInfinispanImpl extends NeutrinoCacheRegion {

	private String configFile;
	private EmbeddedCacheManager embeddedCacheManager;

	public NeutrinoCacheRegionInfinispanImpl(CacheManager neutrinoCacheManager, String configFile) {
		super(neutrinoCacheManager);
		this.configFile = configFile;
		try {
			embeddedCacheManager = new DefaultCacheManager(configFile);
		} catch (IOException e) {
			BaseLoggers.flowLogger.error(e.getMessage());
			throw new SystemException(e);
		}

	}

	public final String getConfigFile() {
		return configFile;
	}

	public EmbeddedCacheManager getEmbeddedCacheManager() {
		return embeddedCacheManager;
	}

}
