package com.nucleus.web.trustedsource;

public class APIMappingDetailsVO {

	private Long apiId;
	private String apiCode;
	private String timeUnit;
	private Integer accessCount;
	private Boolean isAllowed;
	
	public String getApiCode() {
		return apiCode;
	}
	public Long getApiId() {
		return apiId;
	}
	public void setApiId(Long apiId) {
		this.apiId = apiId;
	}
	public void setApiCode(String apiCode) {
		this.apiCode = apiCode;
	}
	public String getTimeUnit() {
		return timeUnit;
	}
	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}
	public Integer getAccessCount() {
		return accessCount;
	}
	public void setAccessCount(Integer accessCount) {
		this.accessCount = accessCount;
	}
	public Boolean getIsAllowed() {
		return isAllowed;
	}
	public void setIsAllowed(Boolean isAllowed) {
		this.isAllowed = isAllowed;
	}

	
	
}
