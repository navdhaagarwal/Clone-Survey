/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.user.event;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.event.GenericEvent;

/**
 * @author Nucleus Software Exports Limited
 * TODO -> souvik.das Add documentation to class
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UserEvent extends GenericEvent {

    private static final long   serialVersionUID = 3481729122269592590L;
    private static final String USER_NAME        = "USER_NAME";
    private static final String ASSOCIATED_USER  = "ASSOCIATED_USER";
    public static final String  USER             = "USER";
    public static final String  USER_PSWD         = "USER_PWD";
    public static final String  USER_EMAIL       = "USER_EMAIL";
    public static final String  USER_PROFILE     = "USER_PROFILE";

    protected UserEvent() {
        super();
    }

    public UserEvent(int eventType) {
        super(eventType);

    }

    /**
     * Sets the USER_NAME.
     *
     * @param teamName the new USER_NAME
     */
    public void setUserName(String userName) {
        addPersistentProperty(USER_NAME, userName);
    }

    /**
     * Gets the USER_NAME.
     *
     * @return the USER_NAME
     */
    public String getUserName() {
        return getPersistentProperty(USER_NAME);
    }

    /**
     * Sets the ASSOCIATED_USER.
     *
     * @param associatedUsers the new ASSOCIATED_USER
     */
    public void setAssociatedUser(String associatedUser) {
        addPersistentProperty(ASSOCIATED_USER, associatedUser);
    }

    /**
     * Gets the ASSOCIATED_USER.
     *
     * @return the ASSOCIATED_USER
     */
    public String getAssociatedUser() {
        return getPersistentProperty(ASSOCIATED_USER);
    }

    /**
     * Instantiates a new maker checker event.
     *
     * @param <T> the generic type
     * @param eventType the event type
     * @param successFlag true for 'success' and false for 'failure'
     * @param userEntityId the user entity id
     * @param entity the entity
     * @param associatedEntityUris the associated entity uris
     */
    public <T extends BaseEntity> UserEvent(int eventType, boolean successFlag, EntityId userEntityId, T entity) {
        super(eventType);

        addPersistentProperty(SUCCESS_FLAG, successFlag ? "success" : "failure");
        setAssociatedUserUri(userEntityId.getUri());
        setStandardEventPropertiesUsingEntity(entity);

    }
}
