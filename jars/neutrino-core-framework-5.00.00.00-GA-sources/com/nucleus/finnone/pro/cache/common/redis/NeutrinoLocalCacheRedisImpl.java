package com.nucleus.finnone.pro.cache.common.redis;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.redisson.api.BatchOptions;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.ReconnectionStrategy;
import org.redisson.api.LocalCachedMapOptions.SyncStrategy;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import com.nucleus.finnone.pro.cache.common.NeutrinoCache;
import com.nucleus.logging.BaseLoggers;

public class NeutrinoLocalCacheRedisImpl extends NeutrinoCache {
	private LocalCachedMapOptions<Object, Object> localCachedMapOptions;
	private RLocalCachedMap<Object, Object> cache;

	public NeutrinoLocalCacheRedisImpl(String neutrinoCacheName, RedissonClient redisson, Long timeToLiveInMinutes,
			BatchOptions batchOptions, String localCacheType) {
		super(neutrinoCacheName, localCacheType, timeToLiveInMinutes);
		this.localCachedMapOptions = LocalCachedMapOptions.defaults().reconnectionStrategy(ReconnectionStrategy.CLEAR)
				.syncStrategy(SyncStrategy.INVALIDATE);

		if (!isLocalCache())
			this.cache = redisson.getLocalCachedMap(getNeutrinoCacheName(), this.localCachedMapOptions);
	}

	public final void putInCache(Object key, Object value) {
		this.cache.fastPut(key, value);
	}

	protected final Object getFromCache(Object key) {
		return this.cache.get(key);

	}

	public final void clearFromCache() {
		this.cache.clear();
	}

	@Override
	public Set<Object> keySetFromCache() {
		return this.cache.keySet();
	}

	@Override
	public Set<Object> keySetFromCache(String keyPattern) {
		return this.cache.keySet(keyPattern);
	}

	@SuppressWarnings("unchecked")
	public void putAllInCache(Object map) {
		Map<Object, Object> addMap = (Map<Object, Object>) map;
		this.cache.putAll(addMap, 10000);
	}

	public int sizeOfCache() {
		return this.cache.size();
	}

	public boolean containsKeyInCache(Object key) {
		return this.cache.containsKey(key);
	}

	public boolean isCacheEmpty() {
		return this.cache.isEmpty();
	}

	public Object putInCacheIfAbsent(Object key, Object value) {
		return this.cache.putIfAbsent(key, value);
	}

	public void removeFromCache(Object key) {
		this.cache.remove(key);
	}

	protected Set<Map.Entry<Object, Object>> entrySetOfCache(String pattern) {
		return this.cache.entrySet(pattern);
	}

	public Set<Map.Entry<Object, Object>> entrySetOfCache() {
		return this.cache.readAllEntrySet();
	}

	public String getCacheAsJson(ObjectMapper mapperObj) throws IOException {
		return mapperObj.writeValueAsString(this.cache);
	}

	@SuppressWarnings("unchecked")
	public void removeAllFromCache(Object set) {
		Set<Object> keySet = (Set<Object>) set;
		keySet.stream().forEach(key -> this.cache.fastRemove(key));
	}

	public Object acquireLock(Object key) {
		RLock lock;
		try {
			lock = this.cache.getLock(key);
			Boolean lockAcquired = false;
			lockAcquired = lock.tryLock(3, 10, TimeUnit.SECONDS);
			if (lockAcquired)
				return lock;
		} catch (Exception e) {
			BaseLoggers.flowLogger.error("Error in NeutrinoCacheRedisImpl.acquireLock", e);
		}
		return null;
	}

	public Object acquireLock(Object key, Long waitTime, Long leaseTime) {
		RLock lock;
		try {
			lock = this.cache.getLock(key);
			Boolean lockAcquired = false;
			lockAcquired = lock.tryLock(waitTime.longValue(), leaseTime.longValue(), TimeUnit.SECONDS);
			if (lockAcquired)
				return lock;
		} catch (Exception e) {
			BaseLoggers.flowLogger.error("Error in NeutrinoCacheRedisImpl.acquireLock", e);
		}
		return null;
	}

	public Object releaseLock(Object lock) {
		try {
			if (lock != null)
				((RLock) lock).unlock();
		} catch (Exception e) {
			BaseLoggers.flowLogger.error("Error in NeutrinoCacheRedisImpl.releaseLock", e);
		}

		return null;
	}

	@Override
	protected void removeExpiredKeys() {
		BaseLoggers.flowLogger.debug("Remove Expired keys called for: " + getNeutrinoCacheName());
	}
}