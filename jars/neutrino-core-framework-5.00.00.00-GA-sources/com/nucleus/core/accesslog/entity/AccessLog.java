/**
 * 
 */
package com.nucleus.core.accesslog.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author chandan.alwala
 *
 */

public class AccessLog implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int LENGTH_FOUR_THOUSAND = 4000;
	public static final String ACCESS_LOG_CORRELATION_ID = "AccessLog";

	private String sessionId;
	private String userName;
	private String uri;
	private String uriFragment;
	private String queryString;
	private String queryStringFragment;
	private String remotehost;
	private LocalDateTime requestDateTime;
	private String serverIp;
	private String module;
	private String method;
	private String webUriRepository;
	private int statusCode;
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUri() {
		if(uriFragment==null){
			return uri;
		}else if(uri==null){
			return uri;
		}else{
			return uri.concat(uriFragment);
		}
		
	}
	public void setUri(String uri) {
		if(uri==null){
			this.uri =null;
			this.uriFragment=null;
			return;
		}
		if(uri.length()<=LENGTH_FOUR_THOUSAND){
			this.uri = uri;
		}else{
			this.uri = uri.substring(0,LENGTH_FOUR_THOUSAND);
			this.uriFragment=uri.substring(LENGTH_FOUR_THOUSAND);
		}		
	}
	
	public String getUriFragment() {
		return uriFragment;
	}
	public void setUriFragment(String uriFragment) {
		this.uriFragment = uriFragment;
	}
	
	public String getRemotehost() {
		return remotehost;
	}
	public void setRemotehost(String remotehost) {
		this.remotehost = remotehost;
	}
	
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getQueryString() {
		if(queryStringFragment==null){
			return queryString;
		}else if(queryString==null){
			return queryString;
		}else{ 
			return queryString.concat(queryStringFragment);
		}
	}
	public void setQueryString(String queryString) {
		this.queryString = queryString;
		if(queryString==null){
			this.queryString =null;
			this.queryStringFragment=null;
			return;
		}
		if(queryString.length()<=LENGTH_FOUR_THOUSAND){
			this.queryString = queryString;
		}else{
			this.queryString = queryString.substring(0,LENGTH_FOUR_THOUSAND);
			this.queryStringFragment=queryString.substring(LENGTH_FOUR_THOUSAND);
		}		
	}
	
	public String getQueryStringFragment() {
		return queryStringFragment;
	}
	public void setQueryStringFragment(String queryStringFragment) {
		this.queryStringFragment = queryStringFragment;
	}
	
	public LocalDateTime getRequestDateTime() {
		return requestDateTime;
	}

	public void setRequestDateTime(LocalDateTime requestDateTime) {
		this.requestDateTime = requestDateTime;
	}
	public String getWebUriRepository() {
		return webUriRepository;
	}
	public void setWebUriRepository(String webUriRepository) {
		this.webUriRepository = webUriRepository;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}		
}
