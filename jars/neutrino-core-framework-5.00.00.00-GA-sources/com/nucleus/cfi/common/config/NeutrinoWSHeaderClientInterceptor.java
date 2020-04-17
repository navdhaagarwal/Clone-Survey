package com.nucleus.cfi.common.config;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.oauth.service.RESTfulAuthenticationService;

@Named("neutrinoWSHeaderClientInterceptor")
public class NeutrinoWSHeaderClientInterceptor implements ClientInterceptor {
	
	@Inject
	@Named("oauthauthenticationService")
	private RESTfulAuthenticationService oauthauthenticationService;

	@Value("${soap.service.trusted.client.id}")
	private String clientID;

	@Override
	public boolean handleRequest(MessageContext messageContext) {
		
		TransportContext context = TransportContextHolder.getTransportContext();
		HeadersAwareSenderWebServiceConnection connection = (HeadersAwareSenderWebServiceConnection) context
				.getConnection();
		
		BaseLoggers.flowLogger.error("Web service request called from :" +ProductInformationLoader.getProductName());
		BaseLoggers.flowLogger.error("Client Id is :" + clientID);
		
		try {

			connection.addRequestHeader("access_token", oauthauthenticationService.getSecurityToken(clientID));
		} catch (Exception e) {
			StringBuilder errorMsg = new StringBuilder("Error while Adding access_token in HTTP Header for SOAP,")
					.append(e.getMessage());
			BaseLoggers.exceptionLogger.error(errorMsg.toString());
			return false;
		}
		return true;
	}

	@Override
	public boolean handleResponse(MessageContext messageContext) {

		return true;
	}

	@Override
	public boolean handleFault(MessageContext messageContext) {

		return true;
	}

	@Override
	public void afterCompletion(MessageContext messageContext, Exception ex) {
		BaseLoggers.integrationLogger.info("Inside afterCompletion method");

	}

	

}
