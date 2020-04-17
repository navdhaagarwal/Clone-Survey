package com.nucleus.finnone.pro.cache.entity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@Cacheable
@Synonym(grant="ALL")
public class ImpactedCache extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String cacheName;

	private String regionName;
	
	private String groupName;
	
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime lastAttemptedTime;

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
	
	public DateTime getLastAttemptedTime() {
		return lastAttemptedTime;
	}

	public void setLastAttemptedTime(DateTime lastAttemptedTime) {
		this.lastAttemptedTime = lastAttemptedTime;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
}
