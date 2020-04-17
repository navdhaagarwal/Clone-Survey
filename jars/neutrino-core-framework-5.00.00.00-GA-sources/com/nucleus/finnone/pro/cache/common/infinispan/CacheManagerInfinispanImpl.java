package com.nucleus.finnone.pro.cache.common.infinispan;

import org.infinispan.manager.EmbeddedCacheManager;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.finnone.pro.cache.common.CacheManager;
import com.nucleus.finnone.pro.cache.common.NeutrinoCache;
import com.nucleus.finnone.pro.cache.common.NeutrinoCacheRegion;

public class CacheManagerInfinispanImpl extends CacheManager {

	@Override
	protected final NeutrinoCache createNeutrinoCacheInstance(String cacheRegion, String cacheName, String localCacheType) {
		NeutrinoCacheRegion neutrinoCacheRegion = NeutrinoSpringAppContextUtil.getBeanByName(cacheRegion,
				NeutrinoCacheRegion.class);
		EmbeddedCacheManager embeddedCacheManager = ((NeutrinoCacheRegionInfinispanImpl) neutrinoCacheRegion)
				.getEmbeddedCacheManager();
		return new NeutrinoCacheInfinispanImpl(cacheName, embeddedCacheManager, localCacheType, getTimeToLiveInMinutesGlobal());
	}

}
