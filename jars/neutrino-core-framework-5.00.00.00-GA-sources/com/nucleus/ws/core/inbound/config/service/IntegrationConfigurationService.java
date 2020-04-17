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
package com.nucleus.ws.core.inbound.config.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.nucleus.ws.core.inbound.config.InBoundServiceInfoPojo;
import com.nucleus.ws.core.inbound.config.WSEndpointInfo;
import com.nucleus.ws.core.inbound.config.msg.IntegrationMessageExchange;
import com.nucleus.ws.core.inbound.config.msg.IntegrationMessageSearchCriteria;
import com.nucleus.ws.core.inbound.config.user.IntegrationEndpointUserDetails;

/**
 * @author Nucleus Software Exports Limited
 */
public interface IntegrationConfigurationService {

    InBoundServiceInfoPojo getInboundServiceConfig(String serviceId);
    
    List<InBoundServiceInfoPojo> getInboundServiceConfigGrid();

    ArrayList<String> getAllowedAuthoritiesForAllEndpoints();

    ArrayList<WSEndpointInfo> getAllDetectedServices();

    IntegrationEndpointUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    String getServiceId(Object endpoint);

    void saveMessage(IntegrationMessageExchange messageExchange);

    String getCurrentUsername();

    IntegrationMessageExchange getIntegrationMessageExchangeById(Long messageId);

    void updateInboundServiceConfig(InBoundServiceInfoPojo infoPojo);

    List<Map<String, Object>> getMessagesByCriteria(IntegrationMessageSearchCriteria criteria);

}
