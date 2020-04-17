package com.nucleus.finnone.pro.cache.common.redis;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.redisson.api.BatchOptions;
import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RMapAsync;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.api.RedissonClient;

import com.nucleus.finnone.pro.cache.common.NeutrinoCache;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

public class NeutrinoCacheRedisImpl extends NeutrinoCache {

	private static final String ERROR_MSG = "Error in NeutrinoCacheRedisImpl.";

	private RedissonClient redisson;
	private BatchOptions batchOptions;
	private RMap<Object, Object> redisCache;
	private RScoredSortedSet<Object> timeoutSet;

	public NeutrinoCacheRedisImpl(String neutrinoCacheName, RedissonClient redisson, String localCacheType,
			Long timeToLiveInMinutes, BatchOptions batchOptions) {
		super(neutrinoCacheName, localCacheType, timeToLiveInMinutes);
		this.batchOptions = batchOptions;
		this.redisson = redisson;
		if (!isLocalCache()) {
			redisCache = this.redisson.getMap(neutrinoCacheName);
			timeoutSet = this.redisson.getScoredSortedSet(getTimeoutSetName());
		}
	}

	@Override
	public final void putInCache(Object key, Object value) {
		if (getTimeToLiveInMinutes() > 0) {
			RBatch batch = redisson.createBatch(batchOptions);
			RMapAsync<Object, Object> cache = batch.getMap(this.getNeutrinoCacheName());
			RScoredSortedSetAsync<Object> timeoutSet = batch.getScoredSortedSet(getTimeoutSetName());
			cache.fastPutAsync(key, value);
			timeoutSet.addAsync(calculateTimeForTTL(), key);
			batch.execute();
		} else {
			redisCache.fastPut(key, value);
		}
	}

	@Override
	protected final Object getFromCache(Object key) {
		return redisCache.get(key);
	}

	@Override
	public final void clearFromCache() {
		redisCache.clear();
	}

	@Override
	public Set<Object> keySetFromCache() {
		return redisCache.keySet();
	}

	@Override
	public Set<Object> keySetFromCache(String keyPattern) {
		return redisCache.keySet(keyPattern);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void putAllInCache(Object map) {
		Map<Object, Object> addMap = (Map<Object, Object>) map;

			RBatch batch = redisson.createBatch(batchOptions);
			RMapAsync<Object, Object> cache = batch.getMap(this.getNeutrinoCacheName());

			if (getTimeToLiveInMinutes() > 0) {
				Map<Object, Double> timeoutMap = new HashMap<>();
				for (Object key : addMap.keySet()) {
					timeoutMap.put(key, calculateTimeForTTL());
				}

				RScoredSortedSetAsync<Object> timeoutSet = batch.getScoredSortedSet(getTimeoutSetName());
				timeoutSet.addAllAsync(timeoutMap);
			}

			cache.putAllAsync(addMap,10000);
			batch.execute();

		



	}

	@Override
	public int sizeOfCache() {
		return redisCache.size();
	}

	@Override
	public boolean containsKeyInCache(Object key) {
		return redisCache.containsKey(key);
	}

	@Override
	public boolean isCacheEmpty() {
		return redisCache.isEmpty();
	}

	@Override
	public Object putInCacheIfAbsent(Object key, Object value) {
		Object oldValue = null;
		if (getTimeToLiveInMinutes() > 0) {
			RBatch batch = redisson.createBatch(batchOptions);
			RMapAsync<Object, Object> cache = batch.getMap(this.getNeutrinoCacheName());
			RScoredSortedSetAsync<Object> timeoutSet = batch.getScoredSortedSet(getTimeoutSetName());
			RFuture<Object> futureObject = cache.putIfAbsentAsync(key, value);
			timeoutSet.addAsync(calculateTimeForTTL(), key);
			batch.execute();
			try {
				oldValue = futureObject.get();
			} catch (InterruptedException | ExecutionException e) {
				BaseLoggers.flowLogger.error(ERROR_MSG + "putInCacheIfAbsent", e);
			}
		}

		else {

			oldValue = redisCache.putIfAbsent(key, value);
		}
		return oldValue;
	}

	@Override
	public void removeFromCache(Object key) {
		RBatch batch = redisson.createBatch(batchOptions);
		RMapAsync<Object, Object> cache = batch.getMap(this.getNeutrinoCacheName());
		RScoredSortedSetAsync<Object> timeoutSet = batch.getScoredSortedSet(getTimeoutSetName());
		cache.removeAsync(key);
		timeoutSet.removeAsync(key);
		batch.execute();
	}

	@Override
	protected Set<Entry<Object, Object>> entrySetOfCache(String pattern) {
		return redisCache.entrySet(pattern);
	}

	@Override
	public Set<Entry<Object, Object>> entrySetOfCache() {
		return redisCache.readAllEntrySet();
	}

	@Override
	public String getCacheAsJson(ObjectMapper mapperObj) throws IOException {
		return mapperObj.writeValueAsString(redisCache);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeAllFromCache(Object set) {
		RBatch batch = redisson.createBatch(batchOptions);
		RMapAsync<Object, Object> cache = batch.getMap(this.getNeutrinoCacheName());
		RScoredSortedSetAsync<Object> timeoutSet = batch.getScoredSortedSet(getTimeoutSetName());

		for (Object key : (Set<Object>) set) {
			cache.removeAsync(key);
			timeoutSet.removeAsync(key);
		}
		batch.execute();

	}

	@Override
	public Object acquireLock(Object key) {
		try {
			RLock lock = redisCache.getLock(key);
			Boolean lockAcquired = false;
			lockAcquired = lock.tryLock(3, 10, TimeUnit.SECONDS);
			if (lockAcquired) {
				return lock;
			}
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "acquireLock", e);
		}
		return null;
	}

	@Override
	public Object acquireLock(Object key, Long waitTime, Long leaseTime) {
		try {
			RLock lock = redisCache.getLock(key);
			Boolean lockAcquired = false;
			lockAcquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
			if (lockAcquired) {
				return lock;
			}
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "acquireLock", e);
		}
		return null;
	}

	@Override
	public Object releaseLock(Object lock) {
		try {
			if (lock != null) {
				((RLock) lock).unlock();
			}
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "releaseLock", e);
		}

		return null;
	}

	@Override
	protected void removeExpiredKeys() {
		if (!isLocalCache()) {

			Set<Object> expiredKeysSet = new HashSet<Object>(
					timeoutSet.valueRange(Double.NEGATIVE_INFINITY, true, getCurrentTimeInSeconds(), true));
			removeAll(expiredKeysSet);
		}
	}

	private Long getCurrentTimeInSeconds() {
		return DateTime.now().getMillis() / 1000L;
	}

	private Double calculateTimeForTTL() {
		return ((Long) (getCurrentTimeInSeconds() + getTimeToLiveInSeconds())).doubleValue();
	}

	private String getTimeoutSetName() {
		return (this.getNeutrinoCacheName() + FWCacheConstants.KEY_DELIMITER + FWCacheConstants.TIMEOUT_SET);
	}

}
