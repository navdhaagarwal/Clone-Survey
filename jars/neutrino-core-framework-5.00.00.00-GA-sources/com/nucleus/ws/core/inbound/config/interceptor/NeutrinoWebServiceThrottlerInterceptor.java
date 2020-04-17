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
package com.nucleus.ws.core.inbound.config.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalTime;
import org.springframework.ws.context.MessageContext;

import com.nucleus.core.misc.util.IpAddressUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.ws.core.inbound.config.InBoundServiceInfoPojo;
import com.nucleus.ws.core.inbound.config.IntegrationEndpointAccessException;
import com.nucleus.ws.core.inbound.config.SpringWsUtils;
import com.nucleus.ws.core.inbound.config.user.IntegrationEndpointUserDetails;

/**
 * @author Nucleus Software Exports Limited
 */
public class NeutrinoWebServiceThrottlerInterceptor extends NeutrinoAbstractInterceptor {

    private static final String SERV_AVAIL_MSG      = "Service is only available from time  %s to %s";
    private static final String USER_AVAIL_MSG      = "Principal [%s] is only allowed to access this service from time %s to %s";
    private static final String USER_IP_ALLOWED_MSG = "Principal [%s] is only allowed to access this service from ip addresses range [%s to %s] or from fixed addess %s";

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {

        InBoundServiceInfoPojo infoPojo = (InBoundServiceInfoPojo) messageContext.getProperty(CURRENT_SERVICE_INFO_POJO);
        if (infoPojo.getSecured()) {
            doValidate(messageContext, endpoint, infoPojo);
        }
        return true;

    }

    private void doValidate(MessageContext messageContext, Object endpoint, InBoundServiceInfoPojo infoPojo) {

        // now check for service availability
        if (!infoPojo.getFullDayAvailable() && infoPojo.getAvailableFromDayTime() != null
                && infoPojo.getAvailableToDayTime() != null) {
            LocalTime localTimeFrom = infoPojo.getAvailableFromDayTime().toLocalTime();
            LocalTime localTimeTo = infoPojo.getAvailableToDayTime().toLocalTime();
            LocalTime now = LocalTime.now();

            if (now.compareTo(localTimeFrom) < 0 || now.compareTo(localTimeTo) > 0) {
                throw new IntegrationEndpointAccessException(String.format(SERV_AVAIL_MSG, localTimeFrom, localTimeTo));
            }
        }

        // also check if user is allowed to access at this time
        IntegrationEndpointUserDetails userDetails = (IntegrationEndpointUserDetails) messageContext
                .getProperty(CURRENT_PRINCIPAL);
        if (!userDetails.isFullDayAccessAllowed() && userDetails.getAllowAccessFromDayTime() != null
                && userDetails.getAllowAccessToDayTime() != null) {
            LocalTime localTimeFrom = userDetails.getAllowAccessFromDayTime().toLocalTime();
            LocalTime localTimeTo = userDetails.getAllowAccessToDayTime().toLocalTime();
            LocalTime now = LocalTime.now();

            if (now.compareTo(localTimeFrom) < 0 || now.compareTo(localTimeTo) > 0) {
                throw new IntegrationEndpointAccessException(String.format(USER_AVAIL_MSG, userDetails.getUsername(),
                        localTimeFrom, localTimeTo));
            }
        }

        // validate message size
        // ===>already validated in first interceptor to avoid any processing of such message (to save resources).

        // validate source-ip range allowed for this user
        if (userDetails.getAllowedRemoteIpAddressRange() != null) {
            HttpServletRequest request = SpringWsUtils.getCurrentHttpServletRequest();
            if (request != null) {
                String remoteAddress = request.getRemoteAddr();
                if (StringUtils.isBlank(remoteAddress)) {
                    throw new IllegalStateException("Unable to extract remoteIpAddress from current HttpServletRequest");
                }
                String fromIp = userDetails.getAllowedRemoteIpAddressRange().getFromIpAddress();
                String toIp = userDetails.getAllowedRemoteIpAddressRange().getToIpAddress();
                String fixedIpAddress = userDetails.getAllowedRemoteIpAddressRange().getIpaddress();
                boolean passFixed = false;
                boolean passRange = false;

                // if no ip address range and fixed address provided explicitly allow all(it works as a flag--
                // isRestrictedIpAccess)
                if (StringUtils.isBlank(fixedIpAddress) && StringUtils.isBlank(fromIp) && StringUtils.isBlank(toIp)) {
                    passFixed = true;
                    passRange = true;
                } else {
                    passFixed = remoteAddress.equalsIgnoreCase(fixedIpAddress);
                    if (StringUtils.isNotBlank(fromIp) && StringUtils.isNotBlank(toIp)) {
                        passRange = IpAddressUtils.isInRange(fromIp, toIp, remoteAddress);
                    }
                }

                if (!(passFixed || passRange)) {
                    throw new IntegrationEndpointAccessException(String.format(USER_IP_ALLOWED_MSG,
                            userDetails.getUsername(), fromIp == null ? "NA" : fromIp, toIp == null ? "NA" : toIp,
                            userDetails.getAllowedRemoteIpAddressRange().getIpaddress()));
                }

            } else {
                throw new IllegalStateException(
                        "Unable to extract remoteIpAddress from current WebServiceConnection.HttpServletRequest is null");
            }
            // TODO: finally implement throttling.M requests per N time units
            if (userDetails.isThrottleRequests()) {
                BaseLoggers.flowLogger.info("Throttling is enabled for Principal {}", userDetails.getUsername());
            }

        }
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
    }

}
