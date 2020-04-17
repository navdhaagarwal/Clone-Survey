package com.nucleus.finnone.pro.cache.common.redis;

import java.util.concurrent.TimeUnit;

import org.redisson.api.BatchOptions;
import org.redisson.api.BatchOptions.ExecutionMode;
import org.redisson.api.RedissonClient;

import com.nucleus.finnone.pro.cache.common.CacheManager;
import com.nucleus.finnone.pro.cache.common.NeutrinoCache;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;

public class CacheManagerRedisImpl extends CacheManager {

	public static final String LOCAL_REDIS = "LOCAL_REDIS";
		
	private RedissonClient redisson;
	private BatchOptions batchOptions;

	
	public CacheManagerRedisImpl(RedissonClient redisson, Long syncSlavesTimeout, Long responseTimeout,
			Long retryInterval, Integer retryAttempts, Long timeout) {
		this.redisson = redisson;
		batchOptions = BatchOptions.defaults().responseTimeout(responseTimeout, TimeUnit.SECONDS)
				.retryAttempts(retryAttempts).retryInterval(retryInterval, TimeUnit.SECONDS)
				.executionMode(ExecutionMode.IN_MEMORY_ATOMIC);
	}

	@Override
	protected NeutrinoCache createNeutrinoCacheInstance(String cacheRegion, String cacheName, String localCacheType) {
		if (localCacheType.equals(LOCAL_REDIS)) {
			return new NeutrinoLocalCacheRedisImpl(cacheRegion + FWCacheConstants.KEY_DELIMITER + cacheName, redisson,
					getTimeToLiveInMinutesGlobal(), batchOptions, localCacheType);
		}
		return new NeutrinoCacheRedisImpl(cacheRegion + FWCacheConstants.KEY_DELIMITER + cacheName, redisson,
				localCacheType, getTimeToLiveInMinutesGlobal(), batchOptions);
	}

}
