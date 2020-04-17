package com.nucleus.core.common;

import java.util.List;

import org.springframework.http.HttpHeaders;

/**
 * @author shivendra.kumar
 *
 */
public class NeutrinoHttpHeaders extends HttpHeaders{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6669360164742750640L;
		
	public static final String CORRELATION_ID = "correlation_id";
	
	public static final String ASYNC = "async";
	
	public static final String TENANT_ID = "tenant_id";
	
	public static final String ACCESS_TOKEN = "access_token";
	
	public static final String VERSION = "version";

	public String getCorrelationId() {
		List<String> correlationList = super.get(CORRELATION_ID);
		return correlationList != null ? correlationList.get(0) : null;
	}

	public void setCorrelationId(String correlationId) {
		super.set(CORRELATION_ID, correlationId);
	}

	public Boolean getAsync() {
		List<String> asyncList = super.get(ASYNC);
		return asyncList != null ? Boolean.getBoolean(asyncList.get(0)) : Boolean.FALSE;
	}

	public void setAsync(Boolean async) {
		super.set(ASYNC,String.valueOf(async));
	}

	public Long getTenantId() {
		List<String> tenantIdList = super.get(TENANT_ID);
		return tenantIdList !=null ? Long.valueOf(tenantIdList.get(0)) : null;
	}

	public void setTenantId(Long tenantId) {
		super.set(TENANT_ID, String.valueOf(tenantId));
	}

	public String getAccessToken() {
		List<String> accessTokenList = super.get(ACCESS_TOKEN);
		return accessTokenList !=null ? accessTokenList.get(0) : null;
	}

	public void setAccessToken(String accessToken) {
		super.set(ACCESS_TOKEN, accessToken);
	}

	public Double getVersion() {
		List<String> versionList = super.get(VERSION);
		return versionList !=null ? Double.valueOf(versionList.get(0)) : null;
	}

	public void setVersion(Double version) {
		super.set(VERSION, String.valueOf(version));
	}

	
	


	
	

}
