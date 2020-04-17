/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.event;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.metadata.Metadata;

/**
 * Database entity corresponding to logged event. Each logged event will be typically derived from an {@link Event} 
 * @author Nucleus Software India Pvt Ltd
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class PersistedEvent extends BaseEntity {

    private static final long serialVersionUID = 1031302190965411386L;

    private String            userUri;

    private String            ownerUri;

    private int               eventType;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          eventTimestamp;

    @ManyToOne(cascade = { CascadeType.PERSIST })
    private Metadata          eventMetadata;

    public DateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(DateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public Metadata getEventMetadata() {
        return eventMetadata;
    }

    public void setEventMetadata(Metadata eventMetadata) {
        this.eventMetadata = eventMetadata;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public void setOwnerEntityId(EntityId ownerEntityId) {
        this.ownerUri = ownerEntityId.getUri();
    }

    public EntityId getOwnerEntityId() {
        return EntityId.fromUri(ownerUri);
    }

    public void setUserEntityId(EntityId userEntityId) {
        this.userUri = userEntityId.getUri();
    }

    public EntityId getUserEntityId() {
        return EntityId.fromUri(userUri);
    }

}
