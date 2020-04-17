/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
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

/**
 * Interface to represent a logical ID for an object. This object should be supported by all entities which implement {@link Entity} interface.
 * @author Nucleus Software Exports Limited
 */
public interface IEntityId extends Serializable {

    /**
     * The String representation for ID. Typically this representation is used for soft foreign key references in table.
     */
    public String getUri();

    /**
     * The actual id of the object. Typically this is PK or composite key of the object
     */
    public Serializable getLocalId();

    /**
     * The entity class of this object
     */
    public Class<? extends Entity> getEntityClass();

}
