package com.nucleus.security.core.session;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * 
 * @author Nucleus Software Exports Limited
 * This class represents the Then part of the Rule
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Table(name = "SESSION_MODULE_MAPPING")
@Synonym(grant="ALL")
public class SessionModuleMapping extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String            sessionId;

    private String            module;

    private Long            userId;

    private String            priority;

    private String          markedForLogout;


    /**
     * Getter for property 'sessionId'.
     *
     * @return Value for property 'sessionId'.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Setter for property 'sessionId'.
     *
     * @param sessionId Value to set for property 'sessionId'.
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Getter for property 'module'.
     *
     * @return Value for property 'module'.
     */
    public String getModule() {
        return module;
    }

    /**
     * Setter for property 'module'.
     *
     * @param module Value to set for property 'module'.
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Getter for property 'userId'.
     *
     * @return Value for property 'userId'.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Setter for property 'userId'.
     *
     * @param userId Value to set for property 'userId'.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Getter for property 'priority'.
     *
     * @return Value for property 'priority'.
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Setter for property 'priority'.
     *
     * @param priority Value to set for property 'priority'.
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Getter for property 'markedForLogout'.
     *
     * @return Value for property 'markedForLogout'.
     */
    public String getMarkedForLogout() {
        return markedForLogout;
    }

    /**
     * Setter for property 'markedForLogout'.
     *
     * @param markedForLogout Value to set for property 'markedForLogout'.
     */
    public void setMarkedForLogout(String markedForLogout) {
        this.markedForLogout = markedForLogout;
    }
}
