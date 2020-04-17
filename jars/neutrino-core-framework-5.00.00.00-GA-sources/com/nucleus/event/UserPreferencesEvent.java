package com.nucleus.event;

import com.nucleus.event.GenericEvent;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UserPreferencesEvent extends GenericEvent {

    private static final long serialVersionUID = 8046265248909557734L;

    private static final String ENTITY_USERNAME = "ENTITY_USERNAME";
    private static final String ENTITY_SESSION_ID = "ENTITY_SESSION_ID";
    private static final String ENTITY_MODULE_ACCESSED = "ENTITY_MODULE_ACCESSED";
    private static final String ENTITY_REMOTE_IP_ADDRESS = "ENTITY_REMOTE_IP_ADDRESS";

    protected UserPreferencesEvent() {
        super();
    }

    public UserPreferencesEvent(int eventType) {
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

    public void setModuleNameForEvent(String moduleName) {
        addPersistentProperty(ENTITY_MODULE_ACCESSED, moduleName);
    }

    public String getModuleNameForEvent() {
        return getPersistentProperty(ENTITY_MODULE_ACCESSED);
    }


}