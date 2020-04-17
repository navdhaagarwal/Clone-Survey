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
package com.nucleus.core.util.security.cert;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * @author Nucleus Software Exports Limited
 */
public class SubjectInfo implements Serializable {

    private static final long serialVersionUID = -5567282389763209058L;

    // X.500 AttributeType
    private String            commonName;
    private String            localityName;
    private String            stateOrProvinceName;
    private String            organizationName;
    private String            organizationalUnitName;
    private String            countryCode;
    private String            streetAddress;
    private String            domainComponent;
    private String            uid;

    private String            emailAddress;

    private DateTime          certificateIssueDate;
    private DateTime          certificateExpirationDate;

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getLocalityName() {
        return localityName;
    }

    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    public String getStateOrProvinceName() {
        return stateOrProvinceName;
    }

    public void setStateOrProvinceName(String stateOrProvinceName) {
        this.stateOrProvinceName = stateOrProvinceName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationalUnitName() {
        return organizationalUnitName;
    }

    public void setOrganizationalUnitName(String organizationalUnitName) {
        this.organizationalUnitName = organizationalUnitName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getDomainComponent() {
        return domainComponent;
    }

    public void setDomainComponent(String domainComponent) {
        this.domainComponent = domainComponent;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public DateTime getCertificateIssueDate() {
        return certificateIssueDate;
    }

    public void setCertificateIssueDate(DateTime certificateIssueDate) {
        this.certificateIssueDate = certificateIssueDate;
    }

    public DateTime getCertificateExpirationDate() {
        return certificateExpirationDate;
    }

    public void setCertificateExpirationDate(DateTime certificateExpirationDate) {
        this.certificateExpirationDate = certificateExpirationDate;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
