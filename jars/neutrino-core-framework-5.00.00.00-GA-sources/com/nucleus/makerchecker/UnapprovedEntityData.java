package com.nucleus.makerchecker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(indexes={@Index(name="changedEntityUri_index",columnList="changedEntityUri"),@Index(name="approval_flow_fk_index",columnList="approval_flow_fk")})
public class UnapprovedEntityData extends BaseEntity {

    private static final long serialVersionUID = 4005389762132296061L;
    private String            changedEntityUri;
    private String            originalEntityUri;
    private String            userUri;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          timestamp;
    private String            refUUId;

    public EntityId getChangedEntityId() {
        return EntityId.fromUri(changedEntityUri);
    }

    public void setChangedEntityId(EntityId changedEntityUri) {
        this.changedEntityUri = changedEntityUri.getUri();
    }

    public EntityId getUserEntityId() {
        return EntityId.fromUri(userUri);
    }

    public void setUserEntityId(EntityId userEntityId) {
        this.userUri = userEntityId.getUri();
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public EntityId getOriginalEntityId() {
        return EntityId.fromUri(originalEntityUri);
    }

    public void setOriginalEntityId(EntityId originalEntityId) {
        this.originalEntityUri = originalEntityId.getUri();
    }

    public String getRefUUId() {
        return refUUId;
    }

    public void setRefUUId(String refUUId) {
        this.refUUId = refUUId;
    }

    public static Long getEntityId(com.nucleus.entity.Entity entity) {
        Long id = null;
        if (entity instanceof HibernateProxy) {
            LazyInitializer lazyInitializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
            id = (Long) lazyInitializer.getIdentifier();
        } else if (entity != null) {
            id = (Long) entity.getId();
        }
        return id;
    }

    public EntityId getEntityId() {
        return new EntityId((Class<? extends BaseEntity>) Hibernate.getClass(this), getEntityId(this));
    }

    /* (non-Javadoc) @see com.nucleus.entity.Entity#getUri() */
    @Override
    public String getUri() {
        return null;
    }

    /* (non-Javadoc) @see com.nucleus.entity.Entity#loadLazyFields() */
    @Override
    public void loadLazyFields() {
    }

}