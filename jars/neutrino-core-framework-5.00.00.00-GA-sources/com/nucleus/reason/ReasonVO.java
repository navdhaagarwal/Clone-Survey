package com.nucleus.reason;

import org.joda.time.DateTime;

/**
 * Created by rohit.chhabra on 3/15/2018.
 */
public class ReasonVO {

    private String name;
    private String code;
    private String description;
    private String remarks;
    private Integer daysToBlock;
    private String userEvent;
    private DateTime creationTimeStamp;
    private Long userEntityId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getDaysToBlock() {
        return daysToBlock;
    }

    public void setDaysToBlock(Integer daysToBlock) {
        this.daysToBlock = daysToBlock;
    }

    public String getUserEvent() {
        return userEvent;
    }

    public void setUserEvent(String userEvent) {
        this.userEvent = userEvent;
    }

    public DateTime getCreationTimeStamp() {
        return creationTimeStamp;
    }

    public void setCreationTimeStamp(DateTime creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
    }

    public Long getUserEntityId() {
        return userEntityId;
    }

    public void setUserEntityId(Long userEntityId) {
        this.userEntityId = userEntityId;
    }
}
