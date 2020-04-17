package com.nucleus.finnone.pro.cache.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.cache.vo.CacheMasterVO;

@Entity
@Synonym(grant = "ALL")
public class CacheBuildHistory extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private String cacheGroupName;

	private String buildStatus;

	@Column
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime lastUpdatedTime;

	@Column
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime lastSuccessTime;

	private Integer failedAttempt;

	private Boolean forceBuild;

	private String ipAddress;

	@Column(length = 2000)
	private String individualCacheBuildStatus;

	public CacheBuildHistory(CacheMasterVO cacheMasterVO) {
		super();
		this.cacheGroupName = cacheMasterVO.getCacheGroupName();
		this.buildStatus = cacheMasterVO.getBuildStatus();
		this.failedAttempt = cacheMasterVO.getFailedAttempt();
		this.forceBuild = cacheMasterVO.getForceBuild();
		this.ipAddress = cacheMasterVO.getIpAddress();
		this.lastUpdatedTime = cacheMasterVO.getLastUpdatedTime();
		this.lastSuccessTime = cacheMasterVO.getLastSuccessTime();
		this.individualCacheBuildStatus = cacheMasterVO.getIndividualCacheBuildStatus().toString();
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

	public String getIndividualCacheBuildStatus() {
		return individualCacheBuildStatus;
	}

	public void setIndividualCacheBuildStatus(String individualCacheBuildStatus) {
		this.individualCacheBuildStatus = individualCacheBuildStatus;
	}

}
