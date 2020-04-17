package com.nucleus.finnone.pro.cache.common.infinispan;

import org.infinispan.Cache;

public interface NeutrinoCacheInfinispanService {

	void put(Cache<Object, Object> cache, Object key, Object value, Long timeToLiveInHours);

	Object putIfAbsent(Cache<Object, Object> cache, Object key, Object value, Long timeToLiveInHours);

	void remove(Cache<Object, Object> cache, Object key);

	void removeAll(Cache<Object, Object> cache, Object set);

	void clear(Cache<Object, Object> cache);

	void putAll(Cache<Object, Object> cache, Object map, Long timeToLiveInHours);

}
