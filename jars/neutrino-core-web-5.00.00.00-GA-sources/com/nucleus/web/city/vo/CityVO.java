package com.nucleus.web.city.vo;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.address.*;

import java.io.Serializable;

public class CityVO implements Serializable {
    private String            cityCode;
    private String            cityName;
    private District district;
    private State             state;
    private Country country;
    private String            stdCode;
    private String            cityMICRCode;
    private LocationType locationType;
    private CityType cityCategorization;
    private boolean           highRiskArea;
    private String status;
    private boolean activeFlag = true;
    private ReasonsActiveInactiveMapping reasonActInactMap;
    private Long        id;
    private String      cityStateName;
    private CityRiskCategory cityRiskCategory;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCityStateName() {
        return cityStateName;
    }

    public void setCityStateName(String cityStateName) {
        this.cityStateName = cityStateName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getStdCode() {
        return stdCode;
    }

    public void setStdCode(String stdCode) {
        this.stdCode = stdCode;
    }

    public String getCityMICRCode() {
        return cityMICRCode;
    }

    public void setCityMICRCode(String cityMICRCode) {
        this.cityMICRCode = cityMICRCode;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public CityType getCityCategorization() {
        return cityCategorization;
    }

    public void setCityCategorization(CityType cityCategorization) {
        this.cityCategorization = cityCategorization;
    }

    public boolean isHighRiskArea() {
        return highRiskArea;
    }

    public void setHighRiskArea(boolean highRiskArea) {
        this.highRiskArea = highRiskArea;
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

    public CityRiskCategory getCityRiskCategory() {
        return cityRiskCategory;
    }

    public void setCityRiskCategory(CityRiskCategory cityRiskCategory) {
        this.cityRiskCategory = cityRiskCategory;
    }
}
