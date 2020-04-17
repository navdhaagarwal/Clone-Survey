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
package com.nucleus.cfi.common.config;

import java.io.Serializable;

/**
 * @author Nucleus Software Exports Limited
 */
public class OutBoundServiceInfoPojo implements Serializable {

    private static final long serialVersionUID           = 2254745886986157115L;

    private String            serviceId;

    private String            serviceDescription;

    private String            serverIP;
    private String            serverPort;

    private Boolean           saveRequestAlways          = false;
    private Boolean           saveResponseOrFaultAlways  = false;
    private Boolean           saveRequestResponseOnError = true;

    private Boolean           active                     = false;

    private Boolean           secured                    = false;

    private String            wsdlStoreId;
    private String            sampleRequestStoreId;
    private String            sampleResponseStoreId;

    private String            wsdlURL;
    private String            serviceURL;

    private String            interfaceType;

    // for random message search-->xpath for an element to be used as search term in a SOAP-xml message
    private String            serachTag1Xpath;
    private String            serachTag2Xpath;
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
    public String getServerIP() {
        return serverIP;
    }
    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }
    public String getServerPort() {
        return serverPort;
    }
    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }
    public Boolean getSaveRequestAlways() {
        return saveRequestAlways;
    }
    public void setSaveRequestAlways(Boolean saveRequestAlways) {
        this.saveRequestAlways = saveRequestAlways;
    }
    public Boolean getSaveResponseOrFaultAlways() {
        return saveResponseOrFaultAlways;
    }
    public void setSaveResponseOrFaultAlways(Boolean saveResponseOrFaultAlways) {
        this.saveResponseOrFaultAlways = saveResponseOrFaultAlways;
    }
    public Boolean getSaveRequestResponseOnError() {
        return saveRequestResponseOnError;
    }
    public void setSaveRequestResponseOnError(Boolean saveRequestResponseOnError) {
        this.saveRequestResponseOnError = saveRequestResponseOnError;
    }
    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
    public Boolean getSecured() {
        return secured;
    }
    public void setSecured(Boolean secured) {
        this.secured = secured;
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
    public String getInterfaceType() {
        return interfaceType;
    }
    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }
    public String getSerachTag1Xpath() {
        return serachTag1Xpath;
    }
    public void setSerachTag1Xpath(String serachTag1Xpath) {
        this.serachTag1Xpath = serachTag1Xpath;
    }
    public String getSerachTag2Xpath() {
        return serachTag2Xpath;
    }
    public void setSerachTag2Xpath(String serachTag2Xpath) {
        this.serachTag2Xpath = serachTag2Xpath;
    }

}
