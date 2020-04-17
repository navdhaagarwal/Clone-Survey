package com.nucleus.snapshotinfo;

import java.util.List;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.metadata.Metadata;
import com.nucleus.service.BaseService;

/**
 * @author amit.parashar
 * @author praveen.jain
 */
public interface SnapshotService extends BaseService {

    /**
     * Creates and saves snapshot for the entity with passed metadata.
     * @param sourceEntity Source entity which is to be replicated into snapshot.
     * @param metadata Metadata to be associated with entity.
     * @return The {@code SnapshotInfo} object associated with snapshot. 
     */
    public SnapshotInfo createSnapshot(BaseEntity sourceEntity, Metadata metadata);

    /**
     * Creates and saves snapshot for the entity with passed metadata.
     * @param entityId {@link EntityId} of the entity which is to be replicated into snapshot.
     * @param metadata Metadata to be associated with entity.
     * @return The {@code SnapshotInfo} object associated with snapshot. 
     */
    public SnapshotInfo createSnapshot(EntityId entityId, Metadata metadata);

    /**
     * Returns {@link SnapshotInfo} for given entityId
     */
    public List<SnapshotInfo> getSnapshotInfoForEntity(EntityId entityId);

    /**
     * Returns instance of entity for passed snapshot info.
     */
    public BaseEntity getEntityFromSnapshot(SnapshotInfo snapshotInfo);

}
