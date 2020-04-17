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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;

import org.apache.wss4j.dom.engine.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.apache.wss4j.dom.handler.WSHandlerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

import com.nucleus.ws.core.inbound.config.InBoundServiceInfoPojo;
import com.nucleus.ws.core.inbound.config.IntegrationEndpointAccessException;
import com.nucleus.ws.core.inbound.config.SpringWSEndpointRegistery;
import com.nucleus.ws.core.inbound.config.WSEndpointInfo;
import com.nucleus.ws.core.inbound.config.user.IntegrationEndpointUserDetails;

/**
 * @author Nucleus Software Exports Limited
 */
public class NeutrinoWebServiceAuthorizationInterceptor extends NeutrinoAbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger("com.nucleus.ws.inbound.security");

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {

        InBoundServiceInfoPojo infoPojo = (InBoundServiceInfoPojo) messageContext.getProperty(CURRENT_SERVICE_INFO_POJO);
        if (infoPojo.getSecured()) {
            doValidate(messageContext, endpoint);
        }
        return true;
    }

    private void doValidate(MessageContext messageContext, Object endpoint) {

        Object recvResults = messageContext.getProperty(WSHandlerConstants.RECV_RESULTS);
        if (recvResults != null && recvResults instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<WSHandlerResult> handlerResults = (List<WSHandlerResult>) recvResults;

            if (!handlerResults.isEmpty()) {
                WSHandlerResult handlerResult = handlerResults.get(0);
                List<WSSecurityEngineResult> securityEngineResults = handlerResult.getResults();
                if (securityEngineResults != null && !securityEngineResults.isEmpty()) {
                    WSSecurityEngineResult engineResult = securityEngineResults.get(0);
                    if (engineResult.get(WSSecurityEngineResult.TAG_PRINCIPAL) != null) {
                        X500Principal x500Principal = (X500Principal) engineResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
                        String distinguishedNameRFC2253 = x500Principal.getName();
                        String username = getUsername(distinguishedNameRFC2253);
                        messageContext.setProperty(CURRENT_REQUEST_SYSTEM_USER_ID, username);
                        String serviceId = getServiceId(endpoint);
                        LOGGER.info("Principal [{}] trying to access service endpoint [{}]", distinguishedNameRFC2253,
                                serviceId);
                        IntegrationEndpointUserDetails endpointUserDetails = integrationConfigurationService
                                .loadUserByUsername(username);
                        messageContext.setProperty(CURRENT_PRINCIPAL, endpointUserDetails);
                        Set<String> allowedAuthorities = getAllowedAuthoritiesForEndpoint(endpoint);

                        boolean isUserAuthorizedToAccessEndpoint = CollectionUtils.containsAny(
                                endpointUserDetails.getAuthorities(), allowedAuthorities);

                        if (!isUserAuthorizedToAccessEndpoint) {
                            throw new IntegrationEndpointAccessException(
                                    "Principal [%s] not authorized to access service endpoint [%s]", username, serviceId);
                        }
                    }

                }
            }
        }
    }

    private Set<String> getAllowedAuthoritiesForEndpoint(Object endpoint) {
        if (endpoint instanceof MethodEndpoint) {
            WSEndpointInfo endpointInfo = SpringWSEndpointRegistery.getInfoForEndpoint((MethodEndpoint) endpoint);
            return endpointInfo.getAuthoritiesAllowed();
        }

        return new HashSet<String>();
    }

    private String getServiceId(Object endpoint) {
        if (endpoint instanceof MethodEndpoint) {
            WSEndpointInfo endpointInfo = SpringWSEndpointRegistery.getInfoForEndpoint((MethodEndpoint) endpoint);
            return endpointInfo.getServiceId();
        }

        return endpoint.toString();
    }

    private String getUsername(String distinguishedName) {
        LdapName ln = null;
        try {
            ln = new LdapName(distinguishedName);
        } catch (InvalidNameException e) {
            LOGGER.error("Invalid distinguishedName found in supplied certificate.", e);
        }

        for (Rdn rdn : ln.getRdns()) {
            if (rdn.getType().equalsIgnoreCase("UID")) {
                return (String) rdn.getValue();
            }
        }
        return "";
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    // do nothing
    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    // do nothing
    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
    }

}
