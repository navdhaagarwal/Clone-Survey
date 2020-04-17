package com.nucleus.finnone.pro.cache.common.infinispan;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import org.infinispan.Cache;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Named("neutrinoCacheInfinispanService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NeutrinoCacheInfinispanServiceImpl implements NeutrinoCacheInfinispanService {

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void put(Cache<Object, Object> cache, Object key, Object value, Long timeToLiveInMinutes) {
		if(timeToLiveInMinutes<=0) {
			cache.put(key, value);	
		} else {
			cache.put(key, value, timeToLiveInMinutes, TimeUnit.MINUTES);	
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Object putIfAbsent(Cache<Object, Object> cache, Object key, Object value, Long timeToLiveInMinutes) {
		if(timeToLiveInMinutes<=0) {
			return cache.putIfAbsent(key, value);	
		} else {
			return cache.putIfAbsent(key, value, timeToLiveInMinutes, TimeUnit.MINUTES);	
		}
		
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void remove(Cache<Object, Object> cache, Object key) {
		cache.remove(key);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void clear(Cache<Object, Object> cache) {
		cache.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void putAll(Cache<Object, Object> cache, Object map, Long timeToLiveInMinutes) {
		if(timeToLiveInMinutes<=0) {
			cache.putAll((Map<Object, Object>) map);
		} else {
			cache.putAll((Map<Object, Object>) map, timeToLiveInMinutes, TimeUnit.MINUTES);	
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeAll(Cache<Object, Object> cache, Object set) {
		for (Object key : (Set<Object>) set) {
			cache.remove(key);
		}

	}

}
