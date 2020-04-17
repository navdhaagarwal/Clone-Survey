package com.nucleus.event;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UserSecurityTrailEvent extends GenericEvent {

    private static final long   serialVersionUID         = 8490775165438153989L;

    private static final String ENTITY_USERNAME          = "ENTITY_USERNAME";
    private static final String ENTITY_SESSION_ID        = "ENTITY_SESSION_ID";
	
	private static final String ENTITY_MODULE_ACCESSED	  = "ENTITY_MODULE_ACCESSED";    
	private static final String ENTITY_REMOTE_IP_ADDRESS = "ENTITY_REMOTE_IP_ADDRESS";
    private static final String HEADER_USER_AGENT        = "HEADER_USER_AGENT";
    public static final String ENTITY_USER_LOGOUT_TYPE  = "ENTITY_USER_LOGOUT_TYPE";
    public static final String ENTITY_USER_LOGOUT_BY  = "ENTITY_USER_LOGOUT_BY";
    private static final String ENTITY_USER_CHANGED_PROTECTION_SESSIONID  = "ENTITY_USER_CHANGED_PROTECTION_SESSIONID";
    private static final String ENTITY_USER_FORCE_LOG_OUT_IP  = "ENTITY_USER_FORCE_LOG_OUT_IP";
    private static final String LOGGED_IN_USERS_FOR_HC_MODE  = "LOGGED_IN_USERS_FOR_HC_MODE";

    protected UserSecurityTrailEvent() {
        super();
    }

	public void setModuleNameForEvent(String moduleName) {
        addPersistentProperty(ENTITY_MODULE_ACCESSED, moduleName);
    }

    public String getModuleNameForEvent() {
        return getPersistentProperty(ENTITY_MODULE_ACCESSED);
    }
    
    
    public UserSecurityTrailEvent(int eventType) {
        super(eventType);
    }

    public String getUsername() {
        return getPersistentProperty(ENTITY_USERNAME);
    }

    public void setUsername(String username) {
        addPersistentProperty(ENTITY_USERNAME, username);
    }

    public String getRemoteIpAddress() {
        return getPersistentProperty(ENTITY_REMOTE_IP_ADDRESS);
    }

    public void setRemoteIpAddress(String remoteIpAddress) {
        addPersistentProperty(ENTITY_REMOTE_IP_ADDRESS, remoteIpAddress);
    }

    public String getSessionId() {
        return getPersistentProperty(ENTITY_SESSION_ID);
    }

    public void setSessionId(String sessionId) {
        addPersistentProperty(ENTITY_SESSION_ID, sessionId);
    }

    public String getHeaderUserAgent() {
        return getPersistentProperty(HEADER_USER_AGENT);
    }

    public void setHeaderUserAgent(String headerUserAgent) {
        addPersistentProperty(HEADER_USER_AGENT, headerUserAgent);
    }
    //
    public String getLogOutType() {
        return getPersistentProperty(ENTITY_USER_LOGOUT_TYPE);
    }

    public void setLogOutType(String logOutType) {
        addPersistentProperty(ENTITY_USER_LOGOUT_TYPE, logOutType);
    }
    
    public String getLogOutBy() {
        return getPersistentProperty(ENTITY_USER_LOGOUT_BY);
    }

    public void setLogOutBy(String logOutBy) {
        addPersistentProperty(ENTITY_USER_LOGOUT_BY, logOutBy);
    }
    
    public String getChangedProtectionSessionId() {
        return getPersistentProperty(ENTITY_USER_CHANGED_PROTECTION_SESSIONID);
    }

    public void setChangedProtectionSessionId(String changedProtectionSessionId) {
        addPersistentProperty(ENTITY_USER_CHANGED_PROTECTION_SESSIONID, changedProtectionSessionId);
    }
    
    public String getForceLogOutIP() {
        return getPersistentProperty(ENTITY_USER_FORCE_LOG_OUT_IP);
    }

    public void setForceLogOutIP(String forceLogOutIP) {
        addPersistentProperty(ENTITY_USER_FORCE_LOG_OUT_IP, forceLogOutIP);
    }

    public void setLoggedInUsersForHcMode(String loggedInUsersForHcMode) {
        addPersistentProperty(LOGGED_IN_USERS_FOR_HC_MODE, loggedInUsersForHcMode);
    }
}