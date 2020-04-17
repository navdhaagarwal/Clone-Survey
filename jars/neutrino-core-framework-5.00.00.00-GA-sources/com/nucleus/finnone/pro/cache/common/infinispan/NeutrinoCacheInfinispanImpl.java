package com.nucleus.finnone.pro.cache.common.infinispan;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.infinispan.AdvancedCache;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.util.concurrent.locks.impl.InfinispanLock;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.finnone.pro.cache.common.NeutrinoCache;
import com.nucleus.logging.BaseLoggers;

public class NeutrinoCacheInfinispanImpl extends NeutrinoCache {

	private static final String ERROR_MSG = "Error in NeutrinoCacheInfinispanImpl.";

	private AdvancedCache<Object, Object> cache;
	private NeutrinoCacheInfinispanService neutrinoCacheInfinispanService;

	public NeutrinoCacheInfinispanImpl(String neutrinoCacheName, EmbeddedCacheManager embeddedCacheManager, String localCacheType,
			Long timeToLiveInMinutes) {
		super(neutrinoCacheName, localCacheType, timeToLiveInMinutes);
		this.neutrinoCacheInfinispanService = NeutrinoSpringAppContextUtil
				.getBeanByName("neutrinoCacheInfinispanService", NeutrinoCacheInfinispanService.class);
		if (!isLocalCache()) {
			this.cache = embeddedCacheManager.getCache(neutrinoCacheName).getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD, Flag.FAIL_SILENTLY);
		}
	}

	@Override
	public final void putInCache(Object key, Object value) {
		this.neutrinoCacheInfinispanService.put(cache, key, value, getTimeToLiveInMinutes());
	}

	@Override
	public final Object getFromCache(Object key) {
		return this.cache.get(key);
	}

	@Override
	public Set<Object> keySetFromCache() {
		return this.cache.keySet();
	}

	@Override
	public Set<Object> keySetFromCache(String keyPattern) {
		Set<Object> matchingEntries = new HashSet<>();
		String tempPattern = keyPattern.replaceAll("\\*", "");
		for (Object key : keySetFromCache()) {
			if (((String) key).contains(tempPattern)) {
				matchingEntries.add(key);
			}
		}
		return matchingEntries;
	}

	@Override
	public Object putInCacheIfAbsent(Object key, Object value) {
		return this.neutrinoCacheInfinispanService.putIfAbsent(this.cache, key, value, getTimeToLiveInMinutes());
	}

	@Override
	public void removeFromCache(Object key) {
		this.neutrinoCacheInfinispanService.remove(cache, key);
	}

	@Override
	public Set<Map.Entry<Object, Object>> entrySetOfCache() {
		return this.cache.entrySet();
	}

	@Override
	public Set<Map.Entry<Object, Object>> entrySetOfCache(String pattern) {
		Set<Map.Entry<Object, Object>> matchingEntries = new HashSet<>();
		String tempPattern = pattern.replaceAll("\\*", "");
		for (Map.Entry<Object, Object> entry : entrySetOfCache()) {
			if (((String) entry.getKey()).contains(tempPattern)) {
				matchingEntries.add(entry);
			}
		}
		return matchingEntries;
	}


	@Override
	public boolean containsKeyInCache(Object key) {
		return this.cache.containsKey(key);
	}

	@Override
	public boolean isCacheEmpty() {
		return this.cache.isEmpty();
	}

	@Override
	public void clearFromCache() {
		this.neutrinoCacheInfinispanService.clear(this.cache);
	}

	@Override
	public int sizeOfCache() {
		return this.cache.size();
	}

	@Override
	public String getCacheAsJson(ObjectMapper mapperObj) throws IOException {
		return mapperObj.writeValueAsString(this.cache);
	}

	@Override
	public void putAllInCache(Object map) {
		this.neutrinoCacheInfinispanService.putAll(this.cache, map, getTimeToLiveInMinutes());
	}

	@Override
	public void removeAllFromCache(Object set) {
		this.neutrinoCacheInfinispanService.removeAll(this.cache, set);

	}

	@Override
	public Object acquireLock(Object key) {
		if (isJtaEnabled()) {
			try {
				Boolean lockAcquired = false;
				lockAcquired = cache.lock(key);
				if (lockAcquired) {
					return cache.getLockManager().getLock(key);
				}
			} catch (Exception e) {
				BaseLoggers.flowLogger.error(ERROR_MSG + "acquireLock", e);
			}
		}
		return null;
	}

	@Override
	public Object releaseLock(Object lock) {
		if (isJtaEnabled() && lock != null) {
			try {
				InfinispanLock infiLock = (InfinispanLock) lock;
				infiLock.release(infiLock.getLockOwner());
			} catch (Exception e) {
				BaseLoggers.flowLogger.error(ERROR_MSG + "releaseLock", e);
			}
		}
		return null;
	}

	@Override
	public Object acquireLock(Object key, Long waitTime, Long leaseTime) {
		return this.acquireLock(key);
	}
	
	@Override
	protected void removeExpiredKeys() {
		

	}

}
