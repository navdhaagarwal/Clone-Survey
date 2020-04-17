package com.nucleus.finnone.pro.cache.common;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.exceptions.OperationNotSupportedException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.dao.CustomCacheDao;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.service.CacheCommonService;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;

public abstract class NeutrinoCachePopulator {

	
	public enum Action {
		UPDATE, DELETE, INSERT
	}

	@Inject
	@Named("customCacheDao")
	private CustomCacheDao dao;

	@Inject
	@Named("cacheCommonService")
	private CacheCommonService cacheCommonService;

	private NeutrinoCache neutrinoCache;

	private NeutrinoCache nullValuesNeutrinoCache;

	private Boolean isIdBasedCacheableEntity = Boolean.FALSE;

	private String cacheGroupName;

	protected String getLocalCacheType() {
		return CacheManager.NA;
	}

	void setNeutrinoCache(NeutrinoCache neutrinoCache) {
		this.neutrinoCache = neutrinoCache;
	}

	void setNullValuesNeutrinoCache(NeutrinoCache nullValuesNeutrinoCache) {
		this.nullValuesNeutrinoCache = nullValuesNeutrinoCache;
	}

	public abstract String getNeutrinoCacheName();

	public abstract String getCacheRegionName();

	/*
	 * To be overridden by individual implementations. If NULL value is return by
	 * this method, build won't be triggered for that specific Populator
	 */
	public String getCacheGroupName() {
		if (cacheGroupName == null) {
			cacheGroupName = new StringBuilder(getCacheRegionName()).append(FWCacheConstants.CACHE_IDENTIFER_DELIMITER)
					.append(getNeutrinoCacheName()).toString();
		}
		return cacheGroupName;
	}

	protected boolean fallbackRequired() {
		return true;
	}

	public Boolean isIdBasedCacheableEntity() {
		return isIdBasedCacheableEntity;
	}

	protected void setIsIdBasedCacheableEntity(Boolean isIdBasedCacheableEntity) {
		this.isIdBasedCacheableEntity = isIdBasedCacheableEntity;
	}

	public CustomCacheDao getDao() {
		return dao;
	}

	@SuppressWarnings("unchecked")
	public Object findEntityById(Object id) {
		return dao.findBaseEntityById((Long) id, ((EntityNeutrinoCachePopulator) this).getEntityClass());
	}

	public abstract void init();

	public Object acquireLock(Object key) {
		return neutrinoCache.acquireLock(key);
	}

	public Object acquireLock(Object key, Long waitTime, Long leaseTime) {
		return neutrinoCache.acquireLock(key, waitTime, leaseTime);
	}

	public Object acquireLock(Object key, int attempts) {
		Object lock = null;
		if (attempts < 1) {
			attempts = 1;
		}
		for (int i = 0; i < attempts; i++) {
			lock = neutrinoCache.acquireLock(key);
			if (lock != null) {
				break;
			}
		}
		return lock;
	}

	public Object acquireLock(Object key, int attempts, Long waitTime, Long leaseTime) {

		Object lock = null;
		if (attempts < 1) {
			attempts = 1;
		}
		for (int i = 0; i < attempts; i++) {
			lock = neutrinoCache.acquireLock(key, waitTime, leaseTime);
			if (lock != null) {
				break;
			}
		}
		return lock;

	}

	public void releaseLock(Object lock) {
		neutrinoCache.releaseLock(lock);
	}

	@PostConstruct
	public void start() {
		String cacheRegionName = getCacheRegionName();
		String neutrinoCacheName = getNeutrinoCacheName();
		if (cacheRegionName != null && neutrinoCacheName != null && !cacheRegionName.isEmpty()
				&& !neutrinoCacheName.isEmpty()) {
			CacheManager cacheManager = NeutrinoSpringAppContextUtil.getBeanByName(FWCacheConstants.CACHE_MANAGER,
					CacheManager.class);
			NeutrinoCache tempNeutrinoCache = cacheManager.createNeutrinoCacheInstance(getCacheRegionName(),
					getNeutrinoCacheName(), getLocalCacheType());
			NeutrinoCache tempNullValuesNeutrinoCache = cacheManager.createNeutrinoCacheInstance(getCacheRegionName(),
					new StringBuilder(getNeutrinoCacheName()).append(FWCacheConstants.KEY_DELIMITER)
							.append(FWCacheConstants.EMPTY_CACHE_NAME).toString(),
					getLocalCacheType());
			cacheManager.registerNeutrinoCache(getNeutrinoCacheName(), getCacheRegionName(), tempNeutrinoCache,
					tempNullValuesNeutrinoCache, this);
		}
		this.init();
	}

	public abstract Object fallback(Object key);

	public abstract void build(Long tenantId);

	public abstract void update(Action action, Object object);

	public void update(Action action, Object key, Object value) throws OperationNotSupportedException{
		throw new OperationNotSupportedException();
	}

	protected void put(Object key, Object value) {
		NeutrinoValidator.notNull(key, "NULL Keys are not allowed in Neutrino Cache");
		if (value == null) {
			nullValuesNeutrinoCache.put(key, FWCacheConstants.EMPTY_VALUE);
			return;
		}
		neutrinoCache.put(key, value);
	}

	public Object get(Object key) {
		Object value = neutrinoCache.get(key);
		if (value == null && fallbackRequired() && !nullValuesNeutrinoCache.containsKey(key)) {
			value = cacheCommonService.fallback(this, key);
			this.put(key, value);
		}

		if (isIdBasedCacheableEntity() && value != null) {
			value = findEntityById(value);
		}
		return value;
	}

	public Set<Object> keySet() {
		return neutrinoCache.keySet();
	}
	public Set<Object> keySet(String keyPattern) {
		return neutrinoCache.keySet(keyPattern);
	}

	protected void clear() {
		neutrinoCache.clear();
		nullValuesNeutrinoCache.clear();
	}

	protected void putAll(Object map) {
		neutrinoCache.putAll(map);
	}

	public int size() {
		return neutrinoCache.size() + nullValuesNeutrinoCache.size();
	}

	public boolean containsKey(Object key) {
		return (neutrinoCache.containsKey(key) || nullValuesNeutrinoCache.containsKey(key));
	}

	public boolean isEmpty() {
		return (neutrinoCache.isEmpty() && nullValuesNeutrinoCache.isEmpty());
	}

	protected Object putIfAbsent(Object key, Object value) {
		NeutrinoValidator.notNull(key, "NULL Keys are not allowed in Neutrino Cache");
		if (value == null) {
			return nullValuesNeutrinoCache.putIfAbsent(key, FWCacheConstants.EMPTY_VALUE);
		}
		return neutrinoCache.putIfAbsent(key, value);
	}

	protected void remove(Object key) {
		neutrinoCache.remove(key);
		nullValuesNeutrinoCache.remove(key);
	}

	protected void removeAll(Object set) {
		neutrinoCache.removeAll(set);
		nullValuesNeutrinoCache.removeAll(set);
	}

	public Set<Entry<Object, Object>> entrySet() {
		return neutrinoCache.entrySet();
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
	public Set<Entry<Object, Object>> entrySet(String keyPattern) {
		return neutrinoCache.entrySet(keyPattern);
	}
	
	
	/**
	 * This update method to be used wherever TRANSACTION is 
	 * NOT required for the update mechanism of the POPULATOR
	 */
	public void update(Map<String, ImpactedCache> impactedCacheMap, Action action, Object object) {
		this.update(action, object);
		if (!ValidatorUtils.hasNoEntry(impactedCacheMap)) {
			ImpactedCache impactedCache = impactedCacheMap.get(new StringBuilder(getCacheRegionName())
					.append(FWCacheConstants.KEY_DELIMITER).append(getNeutrinoCacheName()).toString());
			if (ValidatorUtils.notNull(impactedCache)) {
				cacheCommonService.removeImpactedCacheEntryImplicitly(impactedCache);
			}
		}
	}
	
	/**
	 * This update method to be used wherever TRANSACTION is 
	 * required for the update mechanism of the POPULATOR
	 * For example: ProcessingStageCachePopulator
	 */
	@Transactional
	public void updateWithTransaction(Map<String, ImpactedCache> impactedCacheMap, Action action, Object object) {
		this.update(impactedCacheMap, action, object);
	}

	public String getCacheAsJson(ObjectMapper mapperObj) throws IOException {
		return neutrinoCache.getCacheAsJson(mapperObj);
	}
	
	void removeExpiredKeys(){
		neutrinoCache.removeExpiredKeys();
		nullValuesNeutrinoCache.removeExpiredKeys();
		}

}
