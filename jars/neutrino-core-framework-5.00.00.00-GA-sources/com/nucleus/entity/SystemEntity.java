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
package com.nucleus.entity;

import java.io.Serializable;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;

/**
 * A singleton type System Entity class to hold read-only configurations. The system/global configurations 
 * would always be referred using the instance of this class. This class would always be statically initialized
 * at load-time and will not allow any further modification to id using setter.
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public final class SystemEntity extends BaseEntity implements Serializable {

    private static final long         serialVersionUID = 3537676708950297608L;

    private final static SystemEntity systemEntity     = new SystemEntity();

    SystemEntity() {
        super(1L);
    }

    public static SystemEntity getInstance() {
        return systemEntity;
    }

    public static EntityId getSystemEntityId() {
        return systemEntity.getEntityId();
    }

    @Override
    public void setId(Serializable id) {
        // No action
    }

}
