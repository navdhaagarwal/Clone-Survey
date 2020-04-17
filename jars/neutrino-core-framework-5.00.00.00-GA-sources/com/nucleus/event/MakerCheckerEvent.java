/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.event;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;

/**
 * The Class MakerCheckerEvent.
 *
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class MakerCheckerEvent extends GenericEvent {

    private static final long serialVersionUID = -9115394697303620554L;

    /**
     * Instantiates a new maker checker event.
     */
    protected MakerCheckerEvent() {
        super();
    }

    /**
     * Instantiates a new maker checker event.
     *
     * @param eventType the event type
     */
    protected MakerCheckerEvent(int eventType) {
        super(eventType);
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
    public <T extends BaseEntity> MakerCheckerEvent(int eventType, boolean successFlag, EntityId userEntityId, T entity,
    		String entityDescription) {
        super(eventType);

        addPersistentProperty(SUCCESS_FLAG, successFlag ? "success" : "failure");
        setAssociatedUserUri(userEntityId.getUri());
        setStandardEventPropertiesUsingEntity(entity);
        addPersistentProperty(ENTITY_NAME, entityDescription);

    }

}