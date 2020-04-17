/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.entity;


/**
 * @author Nucleus Software India Pvt Ltd
 * TODO -> amit.parashar Add documentation to class
 */
public class EntityLifeCycleDataBuilder {

    EntityLifeCycleData lifeCycleData = new EntityLifeCycleData();

    public EntityLifeCycleDataBuilder setUuid(String uuid) {
        lifeCycleData.setUuid(uuid);
        return this;
    }

    public EntityLifeCycleDataBuilder setSnapshotRecord(boolean snapshotRecord) {
        lifeCycleData.setSnapshotRecord(snapshotRecord);
        return this;
    }

    public EntityLifeCycleDataBuilder setCreatedByEntityId(EntityId createdByEntityId) {
        lifeCycleData.setCreatedByEntityId(createdByEntityId);
        return this;
    }

    public EntityLifeCycleData getEntityLifeCycleData() {
        return lifeCycleData;
    }

}
