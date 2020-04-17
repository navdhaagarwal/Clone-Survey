package com.nucleus.web.fileconsolidator.util;

import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadUserFormatDetail;
import com.nucleus.master.BaseMasterUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class UserFormatDetailChangesVO implements Serializable {

    public static final String MODIFICATION_OPERATION = BaseMasterUtils.MODIFICATION_OPERATION;
    public static final String DELETION_OPERATION = "D";

    String userFormatDetailId;

    String fieldOrder;

    String optionalMandatoryFlag;

    String trackingFieldFlag;

    String trackingFieldOrder;

    String operationType;

    //For add:

    String baseFormatDetailId;

    String recordIdentifierId;

    String paddingStrategyId;


    public String getUserFormatDetailId() {
        return userFormatDetailId;
    }

    public void setUserFormatDetailId(String userFormatDetailId) {
        this.userFormatDetailId = userFormatDetailId;
    }

    public String getFieldOrder() {
        return fieldOrder;
    }

    public void setFieldOrder(String fieldOrder) {
        this.fieldOrder = fieldOrder;
    }

    public String getOptionalMandatoryFlag() {
        return optionalMandatoryFlag;
    }

    public void setOptionalMandatoryFlag(String optionalMandatoryFlag) {
        this.optionalMandatoryFlag = optionalMandatoryFlag;
    }

    public String getTrackingFieldFlag() {
        return trackingFieldFlag;
    }

    public void setTrackingFieldFlag(String trackingFieldFlag) {
        this.trackingFieldFlag = trackingFieldFlag;
    }

    public String getTrackingFieldOrder() {
        return trackingFieldOrder;
    }

    public void setTrackingFieldOrder(String trackingFieldOrder) {
        this.trackingFieldOrder = trackingFieldOrder;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getBaseFormatDetailId() {
        return baseFormatDetailId;
    }

    public void setBaseFormatDetailId(String baseFormatDetailId) {
        this.baseFormatDetailId = baseFormatDetailId;
    }

    public String getRecordIdentifierId() {
        return recordIdentifierId;
    }

    public void setRecordIdentifierId(String recordIdentifierId) {
        this.recordIdentifierId = recordIdentifierId;
    }

    public String getPaddingStrategyId() {
        return paddingStrategyId;
    }

    public void setPaddingStrategyId(String paddingStrategyId) {
        this.paddingStrategyId = paddingStrategyId;
    }
}
