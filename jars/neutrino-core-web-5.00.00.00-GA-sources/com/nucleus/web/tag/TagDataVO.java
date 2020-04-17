package com.nucleus.web.tag;

import java.io.Serializable;

public class TagDataVO implements Serializable {

    private static final long serialVersionUID = 6709737767779133366L;
    private Serializable      id;
    private String            identificationNumber;
    private String            entityUri;
    private String            createdByUri;
    private String            createdDate;
    private String            customerName;
    private String            stage;
    private String            entityUrl;
    private String            error;

    public String getEntityUrl() {
        return entityUrl;
    }

    public void setEntityUrl(String entityUrl) {
        this.entityUrl = entityUrl;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getEntityUri() {
        return entityUri;
    }

    public void setEntityUri(String entityUri) {
        this.entityUri = entityUri;
    }

    public String getCreatedByUri() {
        return createdByUri;
    }

    public void setCreatedByUri(String createdByUri) {
        this.createdByUri = createdByUri;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
