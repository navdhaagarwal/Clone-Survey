package com.nucleus.ws.core.inbound.config.user;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Set;

import org.joda.time.DateTime;

import com.nucleus.user.IPAddressRange;

public interface IntegrationEndpointUserDetails extends Serializable {

    String getUsername();

    String getDistinguishedName();

    IPAddressRange getAllowedRemoteIpAddressRange();

    Set<String> getAuthorities();

    boolean isThrottleRequests();

    boolean isRejectRequestsOnRateExceed();

    int getMaximumRequestsPerPeriod();

    Long getTimePeriodMillis();

    DateTime getAllowAccessFromDayTime();

    DateTime getAllowAccessToDayTime();

    /**
     * Returns the password used to authenticate the user.
     * only used in case there is no certificate based authentication.
     *
     * @return the password
     */
    String getPassword();

    String getCertificateSerialNumber();

    DateTime getCertificateIssueDate();

    DateTime getCertificateExpirationDate();

    String getCertificateIssuedByUser();

    PublicKey getPublicKey();

    boolean isFullDayAccessAllowed();

}