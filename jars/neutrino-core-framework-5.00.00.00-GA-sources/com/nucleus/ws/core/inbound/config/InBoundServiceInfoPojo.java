/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.ws.core.inbound.config;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * @author Nucleus Software Exports Limited
 */
public class InBoundServiceInfoPojo implements Serializable {

    private static final long serialVersionUID           = 2254745886986157115L;

    private String            serviceId;

    private String            serviceDescription;

    private String            exposedAtIP;
    private String            exposedAtPort;

    private Boolean           saveRequestAlways          = false;
    private Boolean           saveResponseOrFaultAlways  = false;
    private Boolean           saveRequestResponseOnError = true;

    private Boolean           active                     = false;

    private Boolean           secured                    = false;

    private String[]          authoritiesAllowed;

    private String            wsdlStoreId;
    private String            sampleRequestStoreId;
    private String            sampleResponseStoreId;

    private String            wsdlURL;
    private String            serviceURL;

    private String            serviceEndpoint;

    private String            interfaceType;

    private Boolean           fullDayAvailable           = true;
    private DateTime          availableFromDayTime;
    private DateTime          availableToDayTime;

    private Long              maxPayloadSizeAllowedInBytes;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getExposedAtIP() {
        return exposedAtIP;
    }

    public void setExposedAtIP(String exposedAtIP) {
        this.exposedAtIP = exposedAtIP;
    }

    public String getExposedAtPort() {
        return exposedAtPort;
    }

    public void setExposedAtPort(String exposedAtPort) {
        this.exposedAtPort = exposedAtPort;
    }

    public Boolean getSaveRequestAlways() {
        return saveRequestAlways == null ? false : saveRequestAlways;
    }

    public void setSaveRequestAlways(Boolean saveRequestAlways) {
        this.saveRequestAlways = saveRequestAlways;
    }

    public Boolean getSaveResponseOrFaultAlways() {
        return saveResponseOrFaultAlways == null ? false : saveResponseOrFaultAlways;
    }

    public void setSaveResponseOrFaultAlways(Boolean saveResponseOrFaultAlways) {
        this.saveResponseOrFaultAlways = saveResponseOrFaultAlways;
    }

    public Boolean getSaveRequestResponseOnError() {
        return saveRequestResponseOnError == null ? false : saveRequestResponseOnError;
    }

    public void setSaveRequestResponseOnError(Boolean saveRequestResponseOnError) {
        this.saveRequestResponseOnError = saveRequestResponseOnError;
    }

    public Boolean getActive() {
        return active == null ? false : active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getSecured() {
        return secured == null ? false : secured;
    }

    public void setSecured(Boolean secured) {
        this.secured = secured;
    }

    public String[] getAuthoritiesAllowed() {
        return authoritiesAllowed;
    }

    public void setAuthoritiesAllowed(String[] authoritiesAllowed) {
        this.authoritiesAllowed = authoritiesAllowed;
    }

    public String getWsdlStoreId() {
        return wsdlStoreId;
    }

    public void setWsdlStoreId(String wsdlStoreId) {
        this.wsdlStoreId = wsdlStoreId;
    }

    public String getSampleRequestStoreId() {
        return sampleRequestStoreId;
    }

    public void setSampleRequestStoreId(String sampleRequestStoreId) {
        this.sampleRequestStoreId = sampleRequestStoreId;
    }

    public String getSampleResponseStoreId() {
        return sampleResponseStoreId;
    }

    public void setSampleResponseStoreId(String sampleResponseStoreId) {
        this.sampleResponseStoreId = sampleResponseStoreId;
    }

    public String getWsdlURL() {
        return wsdlURL;
    }

    public void setWsdlURL(String wsdlURL) {
        this.wsdlURL = wsdlURL;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public Boolean getFullDayAvailable() {
        return fullDayAvailable == null ? false : fullDayAvailable;
    }

    public void setFullDayAvailable(Boolean fullDayAvailable) {
        this.fullDayAvailable = fullDayAvailable;
    }

    public DateTime getAvailableFromDayTime() {
        return availableFromDayTime;
    }

    public void setAvailableFromDayTime(DateTime availableFromDayTime) {
        this.availableFromDayTime = availableFromDayTime;
    }

    public DateTime getAvailableToDayTime() {
        return availableToDayTime;
    }

    public void setAvailableToDayTime(DateTime availableToDayTime) {
        this.availableToDayTime = availableToDayTime;
    }

    public Long getMaxPayloadSizeAllowedInBytes() {
        return maxPayloadSizeAllowedInBytes;
    }

    public void setMaxPayloadSizeAllowedInBytes(Long maxPayloadSizeAllowedInBytes) {
        this.maxPayloadSizeAllowedInBytes = maxPayloadSizeAllowedInBytes;
    }

}
