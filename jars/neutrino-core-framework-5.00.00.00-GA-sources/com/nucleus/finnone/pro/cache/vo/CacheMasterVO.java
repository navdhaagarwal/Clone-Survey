package com.nucleus.finnone.pro.cache.vo;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.nucleus.finnone.pro.cache.common.MasterCacheService;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.logging.BaseLoggers;

public class CacheMasterVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String cacheGroupName;

	private String buildStatus;

	private DateTime lastUpdatedTime;

	private DateTime lastSuccessTime;

	private Integer failedAttempt;
	
	private Boolean forceBuild;
	
	private String ipAddress;
	
	private Map<String,String> individualCacheBuildStatus;

	

	public CacheMasterVO(String cacheGroupName, Set<NeutrinoCachePopulator> neutrinoCachePopulatorSet) {
		super();
		this.cacheGroupName = cacheGroupName;
		this.buildStatus = MasterCacheService.NOT_INITIATED;
		this.failedAttempt = 0;
		this.forceBuild = false;
		
		this.individualCacheBuildStatus = new HashMap<>();
		for (NeutrinoCachePopulator neutrinoCachePopulator : neutrinoCachePopulatorSet) {
			individualCacheBuildStatus.put(neutrinoCachePopulator.getNeutrinoCacheName(),
					MasterCacheService.NOT_INITIATED);
		}
		
		try {
			this.ipAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			BaseLoggers.flowLogger.error(e.getMessage());
		}
	}

	public String getCacheGroupName() {
		return cacheGroupName;
	}

	public void setCacheGroupName(String cacheGroupName) {
		this.cacheGroupName = cacheGroupName;
	}

	public String getBuildStatus() {
		return buildStatus;
	}

	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}

	public DateTime getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	public void setLastUpdatedTime(DateTime lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

	public DateTime getLastSuccessTime() {
		return lastSuccessTime;
	}

	public void setLastSuccessTime(DateTime lastSuccessTime) {
		this.lastSuccessTime = lastSuccessTime;
	}

	public Integer getFailedAttempt() {
		return failedAttempt;
	}

	public void setFailedAttempt(Integer failedAttempt) {
		this.failedAttempt = failedAttempt;
	}

	public Boolean getForceBuild() {
		return forceBuild;
	}

	public void setForceBuild(Boolean forceBuild) {
		this.forceBuild = forceBuild;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Map<String, String> getIndividualCacheBuildStatus() {
		return individualCacheBuildStatus;
	}

	public void setIndividualCacheBuildStatus(Map<String, String> individualCacheBuildStatus) {
		this.individualCacheBuildStatus = individualCacheBuildStatus;
	}
	
	public void updateIndividualCacheBuildStatus(String cacheName, String buildStatus) {
		this.individualCacheBuildStatus.put(cacheName, buildStatus);
	} 
	
}
