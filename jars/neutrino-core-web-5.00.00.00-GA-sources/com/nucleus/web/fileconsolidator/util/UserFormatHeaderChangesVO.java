package com.nucleus.web.fileconsolidator.util;

import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadUserFormat;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadUserFormatDetail;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserFormatHeaderChangesVO implements Serializable {

    String userFormatId;

    List<UserFormatDetailChangesVO> userFormatDetailChanges;

    Boolean activeFlag;

    public String getUserFormatId() {
        return userFormatId;
    }

    public void setUserFormatId(String userFormatId) {
        this.userFormatId = userFormatId;
    }

    public List<UserFormatDetailChangesVO> getUserFormatDetailChanges() {
        return userFormatDetailChanges;
    }

    public void setUserFormatDetailChanges(List<UserFormatDetailChangesVO> userFormatDetailChanges) {
        this.userFormatDetailChanges = userFormatDetailChanges;
    }

    public Boolean getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

}
