/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.ws.core.inbound.config.user;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.contact.EMailInfo;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.user.IPAddressRange;

/**
 * @author Nucleus Software Exports Limited
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class SystemUser extends BaseMasterEntity {

    private static final long serialVersionUID = 5807426196507868769L;

    @Column(unique = true, updatable = false)
    private String            username;
    private String            password;
    private String            distinguishedName;
    private String            contactPersonName;
    @ManyToOne(cascade = CascadeType.ALL)
    private PhoneNumber       contactPersonPhone;
    @ManyToOne(cascade = CascadeType.ALL)
    private EMailInfo         contactPersonEmail;

    @Embedded
    private IPAddressRange    allowedIpAddressRange;

    private String            authoritiesAsCsv;                       // CSV

    // throttle
    private Boolean           throttleRequests;
    private Boolean           rejectRequestsOnRateExceed;
    private Integer           maximumRequestsPerPeriod;
    private Long              timePeriodMillis;

    private Boolean           fullDayAccessAllowed;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          allowAccessFromDayTime;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          allowAccessToDayTime;

    // certificate props
    private String            certificateSerialNumber;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          certificateIssueDate;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          certificateExpirationDate;
    private String            certificateIssuedByUser;

    // for auditing(optional)
    private Integer           certificateIssued;
    @Lob
    private byte[]            clientCertificate;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          certificateRenewedOn;

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

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
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

    public String getAuthoritiesAsCsv() {
        return authoritiesAsCsv;
    }

    public void setAuthoritiesAsCsv(String authoritiesAsCsv) {
        this.authoritiesAsCsv = authoritiesAsCsv;
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

    public Integer getCertificateIssued() {
        return certificateIssued;
    }

    public void setCertificateIssued(Integer certificateIssued) {
        this.certificateIssued = certificateIssued;
    }

    public byte[] getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(byte[] clientCertificate) {
        this.clientCertificate = clientCertificate;
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

    public IPAddressRange getAllowedIpAddressRange() {
        return allowedIpAddressRange;
    }

    public void setAllowedIpAddressRange(IPAddressRange allowedIpAddressRange) {
        this.allowedIpAddressRange = allowedIpAddressRange;
    }

}
