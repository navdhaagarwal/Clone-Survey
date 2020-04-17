package com.nucleus.finnone.pro.cache.common;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.codehaus.jackson.map.ObjectMapper;

import com.nucleus.logging.BaseLoggers;

public abstract class NeutrinoCache {

	private static final String ERROR_MSG = "Error in NeutrinoCache.";
	
	private String neutrinoCacheName;

	private boolean isLocalCache;
	
	private boolean jtaEnabled;

	private Map<Object, Object> localCache;
	
	private Long timeToLiveInMinutes;
	
	private Long timeToLiveInSeconds;

	protected NeutrinoCache(String neutrinoCacheName, String localCacheType, Long timeToLiveInMinutes) {
		this.neutrinoCacheName = neutrinoCacheName;
		this.timeToLiveInMinutes = timeToLiveInMinutes;
		this.timeToLiveInSeconds = timeToLiveInMinutes*60;
		if(localCacheType.equals(CacheManager.LOCAL)) {
			this.isLocalCache = true;
			localCache = new ConcurrentHashMap<>();
		}
	}

	protected void setJtaEnabled(Boolean jtaEnabled) {
		this.jtaEnabled = jtaEnabled;
	}
	
	protected Boolean isJtaEnabled() {
		return jtaEnabled;
	}
	
	protected Long getTimeToLiveInMinutes() {
		return timeToLiveInMinutes;
	}
	
	protected Long getTimeToLiveInSeconds() {
		return timeToLiveInSeconds;
	}

	protected void setTimeToLiveInMinutes(Long timeToLiveInMinutes) {
		this.timeToLiveInMinutes = timeToLiveInMinutes;
		this.timeToLiveInSeconds = timeToLiveInMinutes*60;
	}

	protected final String getNeutrinoCacheName() {
		return neutrinoCacheName;
	}

	protected boolean isLocalCache() {
		return isLocalCache;
	}

	public final Object get(Object key) {
		try {
			if (isLocalCache()) {
				return getFromLocal(key);
			}
			return getFromCache(key);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "get", e);
			throw e;
		}
	}
	

	private final Object getFromLocal(Object key) {
		return localCache.get(key);
	}

	public final void put(Object key, Object value) {
		try {
			if (isLocalCache()) {
				putInLocal(key, value);
				return;
			}
			putInCache(key, value);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "put", e);
			throw e;
		}
	}
	
	private void putInLocal(Object key, Object value) {
		localCache.put(key, value);
	}

	public final Set<Object> keySet() {
		try {
			if (isLocalCache()) {
				return keySetFromLocal();
			}
			return keySetFromCache();
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "keySet", e);
			throw e;
		}
		
	}

	public final Set<Object> keySet(String keyPattern) {
		try {
			if (isLocalCache()) {
				return keySetFromLocal(keyPattern);
			}
			return keySetFromCache(keyPattern);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "keySet", e);
			throw e;
		}

	}

	private Set<Object> keySetFromLocal() {
		return keySetFromLocal(null);
	}

	private Set<Object> keySetFromLocal(String keyPattern) {
		if (keyPattern == null) {
			return localCache.keySet();
		}
		return localCache.keySet().stream().filter(entry -> ((String) entry).contains(keyPattern.replaceAll("\\*", ""))).collect(Collectors.toSet());
	}

	public final void clear() {
		try {
			if (isLocalCache()) {
				clearFromLocal();
				return;
			}
			clearFromCache();
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "clear", e);
			throw e;
		}
	}

	private void clearFromLocal() {
		localCache.clear();
	}

	public final void putAll(Object map) {
		try {
			if (isLocalCache()) {
				putAllInLocal(map);
				return;
			}
			putAllInCache(map);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "putAll", e);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	private void putAllInLocal(Object map) {
		Map<Object, Object> addMap = (Map<Object, Object>) map;
		localCache.putAll(addMap);
	}

	public final int size() {
		try {
			if (isLocalCache()) {
				return sizeOfLocal();
			}
			return sizeOfCache();
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "size", e);
			throw e;
		}
	}

	private int sizeOfLocal() {
		return localCache.size();
	}

	public final boolean containsKey(Object key) {
		try {
			if (isLocalCache()) {
				return containsKeyInLocal(key);
			}
			return containsKeyInCache(key);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "containsKey", e);
			throw e;
		}
	}

	private boolean containsKeyInLocal(Object key) {
		return localCache.containsKey(key);
	}

	public final boolean isEmpty() {
		try {
			if (isLocalCache()) {
				return isLocalEmpty();
			}
			return isCacheEmpty();
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "isEmpty", e);
			throw e;
		}
	}

	private boolean isLocalEmpty() {
		return localCache.isEmpty();
	}

	public final Object putIfAbsent(Object key, Object value) {
		try {
			if (isLocalCache()) {
				return putInLocalIfAbsent(key, value);
			}
			return putInCacheIfAbsent(key, value);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "putIfAbsent", e);
			throw e;
		}
	}

	private Object putInLocalIfAbsent(Object key, Object value) {
		return localCache.putIfAbsent(key, value);
	}

	public final void remove(Object key) {
		try {
			if (isLocalCache()) {
				removeFromLocal(key);
				return;
			}
			removeFromCache(key);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "remove", e);
			throw e;
		}
	}

	private void removeFromLocal(Object key) {
		localCache.remove(key);
	}

	public final void removeAll(Object set) {
		try {
			if (isLocalCache()) {
				removeAllFromLocal(set);
				return;
			}
			removeAllFromCache(set);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "removeAll", e);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	private void removeAllFromLocal(Object set) {
		for (Object key : (Set<Object>) set) {
			localCache.remove(key);
		}
	}

	public final Set<Entry<Object, Object>> entrySet() {
		try {
			if (isLocalCache()) {
				return entrySetOfLocal();
			}
			return entrySetOfCache();
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "entrySet", e);
			throw e;
		}
	}
	
	/**
	 * This method does not support all patterns due to limitation of implementation.
	 * REDIS directly supports entry set method with patterns. But Infinispan and Local cache does not support 
	 * this method. Different implementation use different regex so not all patterns are allowed here.
	 * 
	 * Only allowed pattern here is with asterisk '*' like *abc*. We can have only contains or substring search.
	 * 
	 * @param pattern
	 * @return
	 */
	public final Set<Entry<Object, Object>> entrySet(String pattern) {
		try {
			if (isLocalCache()) {
				return entrySetOfLocal(pattern);
			}
			return entrySetOfCache(pattern);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(ERROR_MSG + "entrySet", e);
			throw e;
		}
	}

	private Set<Entry<Object, Object>> entrySetOfLocal(String pattern) {
		if (pattern == null) {
			return localCache.entrySet();
		}
		return localCache.entrySet().stream().filter(entry -> ((String) entry.getKey()).contains(pattern.replaceAll("\\*", ""))).collect(Collectors.toSet());
	}

	private Set<Entry<Object, Object>> entrySetOfLocal() {
		return entrySetOfLocal(null);
	}
	
	protected abstract Set<Entry<Object, Object>> entrySetOfCache(String pattern);
	
	protected abstract Object getFromCache(Object key);
	
	public abstract void putInCache(Object key, Object value);

	protected abstract Set<Object> keySetFromCache();

	protected abstract Set<Object> keySetFromCache(String keyPattern);

	public abstract void clearFromCache();

	public abstract void putAllInCache(Object map);

	public abstract int sizeOfCache();

	public abstract boolean containsKeyInCache(Object key);

	public abstract boolean isCacheEmpty();

	public abstract Object putInCacheIfAbsent(Object key, Object value);

	public abstract void removeFromCache(Object key);

	public abstract void removeAllFromCache(Object set);

	public abstract Set<Entry<Object, Object>> entrySetOfCache();
	
	public abstract Object acquireLock(Object key);
	
	public abstract Object acquireLock(Object key, Long waitTime, Long leaseTime);
	
	public abstract Object releaseLock(Object lock);

	public abstract String getCacheAsJson(ObjectMapper mapperObj) throws IOException;
	
	protected abstract void removeExpiredKeys();

}
