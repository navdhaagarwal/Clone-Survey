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
package com.nucleus.ws.security.config;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.nucleus.address.Country;
import com.nucleus.address.State;
import com.nucleus.contact.EMailInfo;
import com.nucleus.contact.PhoneNumber;

/**
 * @author Nucleus Software Exports Limited
 */
public class SystemUserCreateForm implements Serializable {

    private static final long serialVersionUID = 1655236028294081768L;

    // X.500 AttributeType
    private String            commonName;
    private String            localityName;
    private State             stateOrProvince;
    private String            organizationName;
    private String            organizationalUnitName;
    private Country           country;
    private String            streetAddress;
    private String            domainComponent;
    private String            username;                               // UID

    private String            password;

    private String            contactPersonName;
    private PhoneNumber       contactPersonPhone;
    private EMailInfo         contactPersonEmail;

    private String            remoteIpAddressRangeStart;

    private String            remoteIpAddressRangeEnd;

    private String[]          authorities;

    // throttle
    private Boolean           throttleRequests;
    private Boolean           rejectRequestsOnRateExceed;
    private Integer           maximumRequestsPerPeriod;
    private Long              timePeriodMillis;

    private Boolean           fullDayAccessAllowed;
    private DateTime          allowAccessFromDayTime;

    private DateTime          allowAccessToDayTime;

    // certificate props
    private String            certificateSerialNumber;

    private DateTime          certificateIssueDate;

    private DateTime          certificateExpirationDate;
    private String            certificateIssuedByUser;

    private String            certificateExportType;

    private DateTime          certificateRenewedOn;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public PhoneNumber getContactPersonPhone() {
        return contactPersonPhone;
    }

    public void setContactPersonPhone(PhoneNumber contactPersonPhone) {
        this.contactPersonPhone = contactPersonPhone;
    }

    public EMailInfo getContactPersonEmail() {
        return contactPersonEmail;
    }

    public void setContactPersonEmail(EMailInfo contactPersonEmail) {
        this.contactPersonEmail = contactPersonEmail;
    }

    public String[] getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String[] authorities) {
        this.authorities = authorities;
    }

    public Boolean getThrottleRequests() {
        return throttleRequests;
    }

    public void setThrottleRequests(Boolean throttleRequests) {
        this.throttleRequests = throttleRequests;
    }

    public Boolean getRejectRequestsOnRateExceed() {
        return rejectRequestsOnRateExceed;
    }

    public void setRejectRequestsOnRateExceed(Boolean rejectRequestsOnRateExceed) {
        this.rejectRequestsOnRateExceed = rejectRequestsOnRateExceed;
    }

    public Integer getMaximumRequestsPerPeriod() {
        return maximumRequestsPerPeriod;
    }

    public void setMaximumRequestsPerPeriod(Integer maximumRequestsPerPeriod) {
        this.maximumRequestsPerPeriod = maximumRequestsPerPeriod;
    }

    public Long getTimePeriodMillis() {
        return timePeriodMillis;
    }

    public void setTimePeriodMillis(Long timePeriodMillis) {
        this.timePeriodMillis = timePeriodMillis;
    }

    public DateTime getAllowAccessFromDayTime() {
        return allowAccessFromDayTime;
    }

    public void setAllowAccessFromDayTime(DateTime allowAccessFromDayTime) {
        this.allowAccessFromDayTime = allowAccessFromDayTime;
    }

    public DateTime getAllowAccessToDayTime() {
        return allowAccessToDayTime;
    }

    public void setAllowAccessToDayTime(DateTime allowAccessToDayTime) {
        this.allowAccessToDayTime = allowAccessToDayTime;
    }

    public String getCertificateSerialNumber() {
        return certificateSerialNumber;
    }

    public void setCertificateSerialNumber(String certificateSerialNumber) {
        this.certificateSerialNumber = certificateSerialNumber;
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

    public String getCertificateIssuedByUser() {
        return certificateIssuedByUser;
    }

    public void setCertificateIssuedByUser(String certificateIssuedByUser) {
        this.certificateIssuedByUser = certificateIssuedByUser;
    }

    public String getCertificateExportType() {
        return certificateExportType;
    }

    public void setCertificateExportType(String certificateExportType) {
        this.certificateExportType = certificateExportType;
    }

    public State getStateOrProvince() {
        return stateOrProvince;
    }

    public void setStateOrProvince(State stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getRemoteIpAddressRangeStart() {
        return remoteIpAddressRangeStart;
    }

    public void setRemoteIpAddressRangeStart(String remoteIpAddressRangeStart) {
        this.remoteIpAddressRangeStart = remoteIpAddressRangeStart;
    }

    public String getRemoteIpAddressRangeEnd() {
        return remoteIpAddressRangeEnd;
    }

    public void setRemoteIpAddressRangeEnd(String remoteIpAddressRangeEnd) {
        this.remoteIpAddressRangeEnd = remoteIpAddressRangeEnd;
    }

    public DateTime getCertificateRenewedOn() {
        return certificateRenewedOn;
    }

    public void setCertificateRenewedOn(DateTime certificateRenewedOn) {
        this.certificateRenewedOn = certificateRenewedOn;
    }

    public Boolean getFullDayAccessAllowed() {
        return fullDayAccessAllowed;
    }

    public void setFullDayAccessAllowed(Boolean fullDayAccessAllowed) {
        this.fullDayAccessAllowed = fullDayAccessAllowed;
    }

}
