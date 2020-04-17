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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.server.endpoint.MethodEndpoint;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.dynamicQuery.entity.QueryContext;
import com.nucleus.core.dynamicQuery.service.DynamicQueryTranslatorService;
import com.nucleus.core.dynamicQuery.support.DynamicQueryWrapper;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserService;
import com.nucleus.ws.core.inbound.config.InBoundServiceInfoPojo;
import com.nucleus.ws.core.inbound.config.SpringWSEndpointRegistery;
import com.nucleus.ws.core.inbound.config.WSEndpointInfo;
import com.nucleus.ws.core.inbound.config.msg.IntegrationMessageExchange;
import com.nucleus.ws.core.inbound.config.msg.IntegrationMessageSearchCriteria;
import com.nucleus.ws.core.inbound.config.user.IntegrationEndpointUserDetails;
import com.nucleus.ws.core.inbound.config.user.IntegrationEndpointUserDetailsService;

/**
 * @author Nucleus Software Exports Limited
 */
@Named("integrationConfigurationService")
public class IntegrationConfigurationServiceImpl extends BaseServiceImpl implements IntegrationConfigurationService {

    private static final String                   IN_BOUND_INTEGRATION_CONFIG_ROOT_URI = InBoundServiceInfoPojo.class
                                                                                               .getName() + ":root";
    private static final String                   INTERFACE_TYPE_SOAP_WS               = "SOAP_WEB_SERVICE";

    private static final String                   MESSAGE_BY_ID                        = "FROM IntegrationMessageExchange ime WHERE ime.id = :messageId";

    @Inject
    @Named("configurationService")
    public ConfigurationService                   configurationService;

    @Inject
    @Named("integrationEndpointUserDetailsService")
    private IntegrationEndpointUserDetailsService endpointUserDetailsService;

    @Inject
    @Named("couchDataStoreDocumentService")
    private DatastorageService                    couchDatastoreService;

    @Inject
    @Named("userService")
    private UserService                           userService;

    @Inject
    @Named(value = "dynamicQueryTranslatorService")
    DynamicQueryTranslatorService                 queryTranslatorService;

    @Override
    public InBoundServiceInfoPojo getInboundServiceConfig(String serviceId) {

        InBoundServiceInfoPojo inBoundServiceInfoPojo = new InBoundServiceInfoPojo();
        ArrayList<WSEndpointInfo> services = SpringWSEndpointRegistery.getAllDetectedServices();
        for (WSEndpointInfo wsEndpointInfo : services) {
            if (wsEndpointInfo.getServiceId().equalsIgnoreCase(serviceId)) {
                inBoundServiceInfoPojo = configurationService.loadConfigurationForNonEntity(InBoundServiceInfoPojo.class,
                        wsEndpointInfo.getServiceId(), IN_BOUND_INTEGRATION_CONFIG_ROOT_URI);
                inBoundServiceInfoPojo.setServiceId(wsEndpointInfo.getServiceId());
                inBoundServiceInfoPojo.setAuthoritiesAllowed(wsEndpointInfo.getAuthoritiesAllowed().toArray(new String[0]));
                break;
            }
        }
        return inBoundServiceInfoPojo;

    }

    @Override
    public void updateInboundServiceConfig(InBoundServiceInfoPojo infoPojo) {
        configurationService.saveOrUpdateConfigurationForNonEntity(infoPojo, infoPojo.getServiceId(),
                IN_BOUND_INTEGRATION_CONFIG_ROOT_URI);
    }

    @Override
    public ArrayList<String> getAllowedAuthoritiesForAllEndpoints() {
        return new ArrayList<String>(SpringWSEndpointRegistery.getAllowedAuthoritiesForAllEndpoints());
    }

    @Override
    public ArrayList<WSEndpointInfo> getAllDetectedServices() {
        return SpringWSEndpointRegistery.getAllDetectedServices();
    }

    @Override
    public IntegrationEndpointUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return endpointUserDetailsService.loadUserByUsername(username);
    }

    @Override
    public String getServiceId(Object endpoint) {
        if (endpoint instanceof MethodEndpoint) {
            WSEndpointInfo endpointInfo = SpringWSEndpointRegistery.getInfoForEndpoint((MethodEndpoint) endpoint);
            return endpointInfo.getServiceId();
        }

        return endpoint.toString();
    }

    // private-------------------

    // This should be always called in an independent TXN.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMessage(IntegrationMessageExchange messageExchange) {
        if (messageExchange.getRequestMessage() != null) {
            String storeId = couchDatastoreService.saveDocument(
                    new ByteArrayInputStream(messageExchange.getRequestMessage()), "RequestMessage",
                    MediaType.TEXT_XML_VALUE);
            messageExchange.setRequestMessageStoreId(storeId);
        }
        if (messageExchange.getResponseMessage() != null) {
            String storeId = couchDatastoreService.saveDocument(
                    new ByteArrayInputStream(messageExchange.getResponseMessage()), "ResponseMessage",
                    MediaType.TEXT_XML_VALUE);
            messageExchange.setResponseMessageStoreId(storeId);
        }
        if (messageExchange.getFaultMessage() != null) {
            String storeId = couchDatastoreService.saveDocument(new ByteArrayInputStream(messageExchange.getFaultMessage()),
                    "FaultMessage", MediaType.TEXT_XML_VALUE);
            messageExchange.setFaultMessageStoreId(storeId);
        }

        entityDao.persist(messageExchange);
    }

    @Override
    public String getCurrentUsername() {
        if (userService.getCurrentUser() != null) {
            return userService.getCurrentUser().getUsername();
        }
        return null;
    }

    @Override
    public IntegrationMessageExchange getIntegrationMessageExchangeById(Long messageId) {

        JPAQueryExecutor<IntegrationMessageExchange> jpaQueryExecutor = new JPAQueryExecutor<IntegrationMessageExchange>(
                MESSAGE_BY_ID);
        jpaQueryExecutor.addParameter("messageId", messageId);
        IntegrationMessageExchange exchange = entityDao.executeQueryForSingleValue(jpaQueryExecutor);
        if (exchange.getRequestMessageStoreId() != null) {
            byte[] doc = couchDatastoreService.retriveDocumentAsByteArray(exchange.getRequestMessageStoreId());
            if (doc != null) {
                exchange.setRequestMessage(doc);
            }
        }
        if (exchange.getResponseMessageStoreId() != null) {
            byte[] doc = couchDatastoreService.retriveDocumentAsByteArray(exchange.getResponseMessageStoreId());
            if (doc != null) {
                exchange.setResponseMessage(doc);
            }
        }
        if (exchange.getFaultMessageStoreId() != null) {
            byte[] doc = couchDatastoreService.retriveDocumentAsByteArray(exchange.getFaultMessageStoreId());
            if (doc != null) {
                exchange.setFaultMessage(doc);
            }
        }
        return exchange;

    }

    @Override
    public List<Map<String, Object>> getMessagesByCriteria(IntegrationMessageSearchCriteria messageSearchCriteria) {

        if (StringUtils.isNoneBlank(messageSearchCriteria.getQueryWhereClause())) {
            QueryContext queryContext = entityDao.find(QueryContext.class, messageSearchCriteria.getQueryContextId());

            Set<Long> consolidatedIds = new HashSet<Long>();
            Collections.addAll(consolidatedIds, messageSearchCriteria.getSelectedTokenIds());
            consolidatedIds.remove(null);
            DynamicQueryWrapper queryWrapper = queryTranslatorService.processQuery(
                    messageSearchCriteria.getQueryWhereClause(), queryContext, new ArrayList<Long>(consolidatedIds), false);

            List<Map<String, Object>> messageList = entityDao.executeQuery(queryWrapper
                    .getMapQueryExecuterWithAllParameterAdded());
            return messageList;
        }
        return new ArrayList<Map<String, Object>>();
    }

    @Override
    public List<InBoundServiceInfoPojo> getInboundServiceConfigGrid() {

        ArrayList<WSEndpointInfo> services = getAllDetectedServices();
        List<InBoundServiceInfoPojo> pojos = new ArrayList<InBoundServiceInfoPojo>();

        for (WSEndpointInfo wsEndpointInfo : services) {
            // can be improved in terms of sql queries
            InBoundServiceInfoPojo serviceInfoPojo = configurationService.loadConfigurationForNonEntity(
                    InBoundServiceInfoPojo.class, wsEndpointInfo.getServiceId(), IN_BOUND_INTEGRATION_CONFIG_ROOT_URI);
            serviceInfoPojo.setServiceId(wsEndpointInfo.getServiceId());
            serviceInfoPojo.setAuthoritiesAllowed(wsEndpointInfo.getAuthoritiesAllowed().toArray(new String[0]));
            pojos.add(serviceInfoPojo);
        }
        return pojos;
    }

}
