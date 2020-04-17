package com.nucleus.security.core.session;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.session.SessionInformation;

import com.nucleus.core.CustomSizeLinkedHashMap;

public class NeutrinoSessionInformation extends SessionInformation {

	
	public static final String LOGOUT_TYPE_BY_USER="LOGOUT_TYPE_BY_USER";
	public static final String LOGOUT_TYPE_BY_INACTIVITY="LOGOUT_TYPE_BY_INACTIVITY";
	public static final String LOGOUT_TYPE_BY_ADMIN="LOGOUT_TYPE_BY_ADMIN";
	public static final String LOGOUT_TYPE_ON_DIFF_DEVICE_BROWSER="LOGOUT_TYPE_ON_DIFF_DEVICE_BROWSER";
	public static final String LOGOUT_TYPE_ON_SESSION_TIME_OUT="LOGOUT_TYPE_ON_SERVER_TIME_OUT";
	public static final String LOGOUT_TYPE_BY_SSO_LOGOUT="LOGOUT_TYPE_BY_SSO_LOGOUT";
	public static final String LOGOUT_TYPE_BY_PAGE_REFRESH="LOGOUT_TYPE_BY_PAGE_REFRESH";
	public static final String LOGOUT_TYPE_BY_MALICIOUS_URL_ACCESS="LOGOUT_TYPE_BY_MALICIOUS_URL_ACCESS";
	public static final String LOGOUT_TYPE_BY_INVALID_CONFIGURATION="LOGOUT_TYPE_BY_INVALID_CONFIGURATION";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String logOutType;
	private String forceLogOutIP;
	private Long logOutBy;
	private String serviceTicketId;
	private String loginIp;
	private String logoutMessage;

	private CustomSizeLinkedHashMap<String, Long> urlToTimeStampMap;
	
	public String getServiceTicketId() {
		return serviceTicketId;
	}


	public void setServiceTicketId(String serviceTicketId) {
		this.serviceTicketId = serviceTicketId;
	}


	public NeutrinoSessionInformation(Object principal, String sessionId, Date lastRequest) {
		super(principal, sessionId, lastRequest);
	}


	public String getLogOutType() {
		return logOutType;
	}


	public void setLogOutType(String logOutType) {
		this.logOutType = logOutType;
	}


	public String getForceLogOutIP() {
		return forceLogOutIP;
	}


	public void setForceLogOutIP(String forceLogOutIP) {
		this.forceLogOutIP = forceLogOutIP;
	}


	public Long getLogOutBy() {
		return logOutBy;
	}


	public void setLogOutBy(Long logOutBy) {
		this.logOutBy = logOutBy;
	}
	
	public CustomSizeLinkedHashMap<String, Long> getUrlToTimeStampMap() {
		return urlToTimeStampMap;
	}


	public void setUrlToTimeStampMap(CustomSizeLinkedHashMap<String, Long> urlToTimeStampMap) {
		this.urlToTimeStampMap = urlToTimeStampMap;
	}
	

	public String getLoginIp() {
		return loginIp;
	}


	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}
	
	public String getLogoutMessage() {
		return logoutMessage;
	}


	public void setLogoutMessage(String logoutMessage) {
		this.logoutMessage = logoutMessage;
	}

	public void updateLogoutInfo(String logoutEvent,String logoutMessage, Long logOutBy, String forceLogOutIP) {
    	if(StringUtils.isNotEmpty(logoutEvent)){
			this.setLogOutType(logoutEvent);
		}
		
		if(logOutBy != null){
			this.setLogOutBy(logOutBy);
		}
		
		if(StringUtils.isNotEmpty(forceLogOutIP)) {
			this.setForceLogOutIP(forceLogOutIP);
		}
		
		if(StringUtils.isNotEmpty(logoutMessage)) {
			this.setLogoutMessage(logoutMessage);
		}
    }
	
}
