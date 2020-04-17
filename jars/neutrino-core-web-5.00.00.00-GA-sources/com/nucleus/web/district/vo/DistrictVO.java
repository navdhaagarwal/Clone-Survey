package com.nucleus.web.district.vo;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.address.State;

import java.io.Serializable;

public class DistrictVO implements Serializable {
    private String            districtCode;
    private String            districtName;
    private String            districtAbbreviation;
    private State state;
    private String status;
    private boolean activeFlag = true;
    private ReasonsActiveInactiveMapping reasonActInactMap;

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getDistrictAbbreviation() {
        return districtAbbreviation;
    }

    public void setDistrictAbbreviation(String districtAbbreviation) {
        this.districtAbbreviation = districtAbbreviation;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }
}
