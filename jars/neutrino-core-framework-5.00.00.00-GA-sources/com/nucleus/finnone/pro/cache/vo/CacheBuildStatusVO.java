package com.nucleus.finnone.pro.cache.vo;

import java.io.Serializable;

public class CacheBuildStatusVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String cacheName;

	private String buildStatus;

	public CacheBuildStatusVO(String cacheName, String buildStatus) {
		super();
		this.cacheName = cacheName;
		this.buildStatus = buildStatus;

	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public String getBuildStatus() {
		return buildStatus;
	}

	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}

}
