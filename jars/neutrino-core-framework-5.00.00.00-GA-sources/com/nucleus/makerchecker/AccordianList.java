package com.nucleus.makerchecker;

public class AccordianList {

    private String accordianName;

    private Integer accordianOrder;

    private Boolean overrideAcc;

    private OverrideConfig overrideConfig;

    private String supportedProperty;

    public String getAccordianName() {
        return accordianName;
    }

    public void setAccordianName(String accordianName) {
        this.accordianName = accordianName;
    }

    public Integer getAccordianOrder() {
        return accordianOrder;
    }

    public void setAccordianOrder(Integer accordianOrder) {
        this.accordianOrder = accordianOrder;
    }

    public Boolean getOverrideAcc() {
        return overrideAcc;
    }

    public void setOverrideAcc(Boolean overrideAcc) {
        this.overrideAcc = overrideAcc;
    }

    public OverrideConfig getOverrideConfig() {
        return overrideConfig;
    }

    public void setOverrideConfig(OverrideConfig overrideConfig) {
        this.overrideConfig = overrideConfig;
    }

    public String getSupportedProperty() {
        return supportedProperty;
    }

    public void setSupportedProperty(String supportedProperty) {
        this.supportedProperty = supportedProperty;
    }
}