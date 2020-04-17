package com.nucleus.finnone.pro.cache.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@Synonym(grant="ALL")
public class ImpactedCacheHistory extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String cacheName;

	private String regionName;
	
	private String groupName;

	@ManyToOne
	private CacheUpdateType cacheUpdateType;

	@Column(updatable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime cacheUpdateTimeStamp;

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public DateTime getCacheUpdateTimeStamp() {
		return cacheUpdateTimeStamp;
	}

	public void setCacheUpdateTimeStamp(DateTime cacheUpdateTimeStamp) {
		this.cacheUpdateTimeStamp = cacheUpdateTimeStamp;
	}

	public CacheUpdateType getCacheUpdateType() {
		return cacheUpdateType;
	}

	public void setCacheUpdateType(CacheUpdateType cacheUpdateType) {
		this.cacheUpdateType = cacheUpdateType;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public ImpactedCacheHistory prepareFromImpactedCache(ImpactedCache impactedCache, CacheUpdateType cacheUpdateType) {
		this.cacheName = impactedCache.getCacheName();
		this.regionName = impactedCache.getRegionName();
		this.groupName = impactedCache.getGroupName();
		this.cacheUpdateTimeStamp = impactedCache.getEntityLifeCycleData().getCreationTimeStamp();
		this.cacheUpdateType = cacheUpdateType;
		return this;
	}

}
