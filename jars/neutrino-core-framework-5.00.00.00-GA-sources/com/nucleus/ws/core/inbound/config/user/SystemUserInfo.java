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
package com.nucleus.ws.core.inbound.config.user;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.user.IPAddressRange;

/**
 * @author Nucleus Software Exports Limited
 */
public class SystemUserInfo implements IntegrationEndpointUserDetails {

    private static final long serialVersionUID = -2021838365514966157L;
    private String            username;
    private String            password;
    private String            distinguishedName;

    private IPAddressRange    allowedIpAddressRange;

    private String            authoritiesAsCsv;                        // CSV

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

    public SystemUserInfo(SystemUser systemUser) {
        super();
        this.username = systemUser.getUsername();
        this.password = systemUser.getPassword();
        this.distinguishedName = systemUser.getDistinguishedName();
        this.allowedIpAddressRange = systemUser.getAllowedIpAddressRange();
        this.authoritiesAsCsv = systemUser.getAuthoritiesAsCsv();
        this.throttleRequests = systemUser.getThrottleRequests();
        this.rejectRequestsOnRateExceed = systemUser.getRejectRequestsOnRateExceed();
        this.maximumRequestsPerPeriod = systemUser.getMaximumRequestsPerPeriod();
        this.timePeriodMillis = systemUser.getTimePeriodMillis();
        this.fullDayAccessAllowed = systemUser.getFullDayAccessAllowed();
        this.allowAccessFromDayTime = systemUser.getAllowAccessFromDayTime();
        this.allowAccessToDayTime = systemUser.getAllowAccessToDayTime();
        this.certificateSerialNumber = systemUser.getCertificateSerialNumber();
        this.certificateIssueDate = systemUser.getCertificateIssueDate();
        this.certificateExpirationDate = systemUser.getCertificateExpirationDate();
        this.certificateIssuedByUser = systemUser.getCertificateIssuedByUser();
    }

    // ~~~================
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getDistinguishedName() {
        return distinguishedName;
    }

    @Override
    public Set<String> getAuthorities() {
        return new HashSet<String>(Arrays.asList(StringUtils.commaDelimitedListToStringArray(authoritiesAsCsv)));
    }

    @Override
    public boolean isThrottleRequests() {
        return throttleRequests == null ? false : throttleRequests;
    }

    @Override
    public boolean isRejectRequestsOnRateExceed() {
        return rejectRequestsOnRateExceed == null ? false : rejectRequestsOnRateExceed;
    }

    @Override
    public int getMaximumRequestsPerPeriod() {
        return maximumRequestsPerPeriod;
    }

    @Override
    public Long getTimePeriodMillis() {
        return timePeriodMillis;
    }

    @Override
    public DateTime getAllowAccessFromDayTime() {
        return allowAccessFromDayTime;
    }

    @Override
    public DateTime getAllowAccessToDayTime() {
        return allowAccessToDayTime;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getCertificateSerialNumber() {
        return certificateSerialNumber;
    }

    @Override
    public DateTime getCertificateIssueDate() {
        return certificateIssueDate;
    }

    @Override
    public DateTime getCertificateExpirationDate() {
        return certificateExpirationDate;
    }

    @Override
    public String getCertificateIssuedByUser() {
        return certificateIssuedByUser;
    }

    @Override
    public PublicKey getPublicKey() {
        throw new SystemException("Method not supported");
    }

    @Override
    public boolean isFullDayAccessAllowed() {
        return fullDayAccessAllowed == null ? false : fullDayAccessAllowed;
    }

    public IPAddressRange getAllowedIpAddressRange() {
        return allowedIpAddressRange;
    }

    public void setAllowedIpAddressRange(IPAddressRange allowedIpAddressRange) {
        this.allowedIpAddressRange = allowedIpAddressRange;
    }

    @Override
    public IPAddressRange getAllowedRemoteIpAddressRange() {
        return allowedIpAddressRange;
    }

}
