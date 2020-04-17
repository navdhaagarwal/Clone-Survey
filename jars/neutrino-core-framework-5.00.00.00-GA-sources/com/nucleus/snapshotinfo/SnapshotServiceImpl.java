package com.nucleus.snapshotinfo;

import java.util.List;

import javax.inject.Named;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.entity.EntityId;
import com.nucleus.metadata.Metadata;
import com.nucleus.service.BaseServiceImpl;

/**
 * @author amit.parashar
 * 
 */
@Named("snapshotService")
public class SnapshotServiceImpl extends BaseServiceImpl implements SnapshotService {

    @Override
    public SnapshotInfo createSnapshot(BaseEntity sourceEntity, Metadata metadata) {
        SnapshotInfo snapshotInfo = new SnapshotInfo();
        BaseEntity clonedEntity = sourceEntity.cloneYourself(CloneOptionConstants.SNAPSHOT_CLONING_OPTION);
        clonedEntity.getEntityLifeCycleData().setCreatedByUri(getCurrentUser().getUserEntityId().getUri());
        entityDao.persist(clonedEntity);
        snapshotInfo.getEntityLifeCycleData().setCreatedByUri(getCurrentUser().getUserEntityId().getUri());
        snapshotInfo.setSourceRecordEntityId(sourceEntity.getEntityId());
        snapshotInfo.setSnapshotRecordEntityId(clonedEntity.getEntityId());
        snapshotInfo.setMetadata(metadata);
        entityDao.persist(snapshotInfo);
        return snapshotInfo;
    }

    @Override
    public SnapshotInfo createSnapshot(EntityId entityId, Metadata metadata) {
        BaseEntity entity = (BaseEntity) entityDao.find(entityId.getEntityClass(), entityId.getLocalId());
        return createSnapshot(entity, metadata);
    }

    @Override
    public List<SnapshotInfo> getSnapshotInfoForEntity(EntityId entityId) {
        NeutrinoValidator.notNull(entityId, "EntityId cannot be null");
        return entityDao.executeQuery(new NamedQueryExecutor<SnapshotInfo>("SnapshotInfo.findSnapshotsForEntity")
                .addParameter("sourceRecordUri", entityId.getUri()));
    }

    @Override
    public BaseEntity getEntityFromSnapshot(SnapshotInfo snapshotInfo) {
        return (BaseEntity) entityDao.get(snapshotInfo.getSourceRecordEntityId());
    }
}
