package com.nucleus.cas.parentChildDeletionHandling;

import java.io.Serializable;

public class DependencyUsageVO implements Serializable{

    private String master;
    private String masterName;
    private String createdBy;
    private String creationTime;
    private String updatedBy;
    private String updationTime;
    private String status;

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdationTime() {
        return updationTime;
    }

    public void setUpdationTime(String updationTime) {
        this.updationTime = updationTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
