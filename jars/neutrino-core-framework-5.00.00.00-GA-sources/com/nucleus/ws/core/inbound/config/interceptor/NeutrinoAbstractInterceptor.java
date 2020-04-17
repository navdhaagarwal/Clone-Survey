package com.nucleus.ws.core.inbound.config.interceptor;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.xml.transform.TransformerObjectSupport;

import com.nucleus.ws.core.inbound.config.WebServiceConstants;
import com.nucleus.ws.core.inbound.config.service.IntegrationConfigurationService;

public abstract class NeutrinoAbstractInterceptor extends TransformerObjectSupport implements EndpointInterceptor,
        WebServiceConstants {

    protected IntegrationConfigurationService integrationConfigurationService;

    public void setIntegrationConfigurationService(IntegrationConfigurationService integrationConfigurationService) {
        this.integrationConfigurationService = integrationConfigurationService;
    }

}
