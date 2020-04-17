package com.nucleus.snapshotinfo;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.metadata.Metadata;

/**
 * @author amit.parashar
 * 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class SnapshotInfo extends BaseEntity {

    @Transient
    private static final long serialVersionUID = -1790223186104541477L;

    private String            sourceRecordUri;

    private String            snapshotRecordUri;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Metadata          metadata;

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public EntityId getSourceRecordEntityId() {
        return EntityId.fromUri(sourceRecordUri);
    }

    public void setSourceRecordEntityId(EntityId sourceRecordEntityId) {
        this.sourceRecordUri = sourceRecordEntityId.getUri();
    }

    public EntityId getSnapshotRecordEntityId() {
        return EntityId.fromUri(snapshotRecordUri);
    }

    public void setSnapshotRecordEntityId(EntityId snapshotRecordEntityId) {
        this.snapshotRecordUri = snapshotRecordEntityId.getUri();
    }

}
