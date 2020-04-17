/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import io.swagger.annotations.ApiModelProperty;

@Embeddable
public class EntityLifeCycleData implements Serializable {

    private static final long serialVersionUID = 4347141230144613993L;

    @ApiModelProperty(hidden=true)
    private Boolean           snapshotRecord;

    private String            createdByUri;

    @Column(updatable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          creationTimeStamp;

    private String            lastUpdatedByUri;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          lastUpdatedTimeStamp;

    @ApiModelProperty(hidden=true)
    private String            uuid;

    private Boolean           systemModifiableOnly;

    @ApiModelProperty(hidden=true)
    private Integer           persistenceStatus;
    
	/*
	 * Transient boolean field for dirty checking in case of @PreUpdate. Fields
	 * updated in @PreUpdate are not included in update query
	 * when @DynamicUpdate is used. There is an open hibernate bug for the same:
	 * https://hibernate.atlassian.net/browse/HHH-9754
	 */

    @ApiModelProperty(hidden=true)
    @Transient
    private Boolean dirtyFlag;

    /**
     * @return the persistenceStatus
     */
    public Integer getPersistenceStatus() {
        return persistenceStatus;
    }

    /**
     * @param persistenceStatus
     *            the persistenceStatus to set
     */
    public void setPersistenceStatus(Integer persistenceStatus) {
        this.persistenceStatus = persistenceStatus;
    }

    /**
     * @return the systemModifiableOnly
     */
    public Boolean getSystemModifiableOnly() {
        return systemModifiableOnly;
    }

    /**
     * @param systemModifiableOnly
     *            the systemModifiableOnly to set
     */
    public void setSystemModifiableOnly(Boolean systemModifiableOnly) {
        this.systemModifiableOnly = systemModifiableOnly;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid
     *            the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the snapshotRecord
     */
    public Boolean getSnapshotRecord() {
        return snapshotRecord;
    }

    /**
     * @param snapshotRecord
     *            the snapshotRecord to set
     */
    public void setSnapshotRecord(Boolean snapshotRecord) {
        this.snapshotRecord = snapshotRecord;
    }

    public EntityId getCreatedByEntityId() {
        return EntityId.fromUri(createdByUri);
    }

    public void setCreatedByEntityId(EntityId createdByEntityId) {
        this.createdByUri = createdByEntityId.getUri();
    }

    /**
     * @return the createdByUri
     */
    public String getCreatedByUri() {
        return createdByUri;
    }

    /**
     * @return the creationTimeStamp
     */
    public DateTime getCreationTimeStamp() {
        return creationTimeStamp;
    }

    /**
     * @param createdByUri
     *            the createdByUri to set
     */
    public void setCreatedByUri(String createdByUri) {
        this.createdByUri = createdByUri;
    }

    /**
     * @param creationTimeStamp
     *            the creationTimeStamp to set
     */
    public void setCreationTimeStamp(DateTime creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
    }

    public String getLastUpdatedByUri() {
        return lastUpdatedByUri;
    }

    public void setLastUpdatedByUri(String lastUpdatedByUri) {
        this.lastUpdatedByUri = lastUpdatedByUri;
    }

    public DateTime getLastUpdatedTimeStamp() {
        return lastUpdatedTimeStamp;
    }

    public void setLastUpdatedTimeStamp(DateTime lastUpdatedTimeStamp) {
        this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
    }

	public Boolean getDirtyFlag() {
		return dirtyFlag;
	}

	public void setDirtyFlag(Boolean dirtyCheck) {
		this.dirtyFlag = dirtyCheck;
	}

}
