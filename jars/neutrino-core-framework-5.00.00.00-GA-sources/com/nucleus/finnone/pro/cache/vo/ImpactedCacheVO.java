package com.nucleus.finnone.pro.cache.vo;

import java.io.Serializable;

import org.joda.time.DateTime;

public class ImpactedCacheVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String cacheName;

	private String regionName;
	
	private String groupName;

	private String buildStatus;

	private DateTime lastAttemptedTime;

	public ImpactedCacheVO() {
		super();
	}

	public ImpactedCacheVO(String cacheName, String regionName) {
		this(cacheName, regionName, null, null, null);
	}

	public ImpactedCacheVO(String cacheName, String regionName, String groupName) {
		this(cacheName, regionName, groupName, null, null);
	}

	public ImpactedCacheVO(String cacheName, String regionName, String groupName, DateTime lastAttemptedTime) {
		this(cacheName, regionName, groupName, lastAttemptedTime, null);
	}
	
	public ImpactedCacheVO(String cacheName, String regionName, String groupName, DateTime lastAttemptedTime, String buildStatus) {
		super();
		this.cacheName = cacheName;
		this.regionName = regionName;
		this.groupName = groupName;
		this.lastAttemptedTime = lastAttemptedTime;
		this.buildStatus = buildStatus;
	}
	
	

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

	public String getBuildStatus() {
		return buildStatus;
	}

	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public String toString() {
		return "ImpactedCacheVO [cacheName=" + cacheName + ", regionName=" + regionName + ", groupName=" + groupName
				+ ", buildStatus=" + buildStatus + ", lastAttemptedTime=" + lastAttemptedTime + "]";
	}
	
}
