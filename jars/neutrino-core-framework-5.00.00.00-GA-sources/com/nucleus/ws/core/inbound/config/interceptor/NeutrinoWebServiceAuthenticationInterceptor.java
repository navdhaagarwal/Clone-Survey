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

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.WsSecurityValidationException;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

import com.nucleus.ws.core.inbound.config.InBoundServiceInfoPojo;
import com.nucleus.ws.core.inbound.config.IntegrationEndpointAccessException;
import com.nucleus.ws.core.inbound.config.WebServiceConstants;

/**
 * @author Nucleus Software Exports Limited
 */
public class NeutrinoWebServiceAuthenticationInterceptor extends Wss4jSecurityInterceptor implements WebServiceConstants {

    private static final String SERV_INACTIVE_MSG = "Service is not currently active";

    @Override
    protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
            throws WsSecurityValidationException {

        InBoundServiceInfoPojo infoPojo = (InBoundServiceInfoPojo) messageContext.getProperty(CURRENT_SERVICE_INFO_POJO);

        // first of all check if service is active
        if (!infoPojo.getActive()) {
            throw new IntegrationEndpointAccessException(SERV_INACTIVE_MSG);
        }

        if (infoPojo.getSecured()) {
            super.validateMessage(soapMessage, messageContext);
        }
    }
}
