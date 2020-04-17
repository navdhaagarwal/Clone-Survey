package com.nucleus.tag.entriesTaskTagVo;

import com.nucleus.entity.BaseEntity;


public class EntriesAndTaskTagVo {

    private BaseEntity entityUri;
    private String taskId;

    public BaseEntity getEntityUri() {
        return entityUri;
    }

    public void setEntityUri(BaseEntity entityUri) {
        this.entityUri = entityUri;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

}
