package com.nucleus.web.communicationCallback;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.nucleus.cfi.ws.client.stub.communication.async.CommAsyncRequest;
import com.nucleus.cfi.ws.client.stub.communication.async.CommAsyncResponse;
import com.nucleus.finnone.pro.communicationgenerator.service.ICommunicationAsyncCallbackService;

@Endpoint
public class AsyncCommunicationCallbackEndpoint {
	
    public static final String NAMESPACE = "http://www.nucleus.com/schemas/commAsyncService";
    
    public static final String COMMUNICATION_ASYNC_REQUEST = "commAsyncRequest";
    
    @Inject
    @Named("communicationAsyncCallbackService")
	private ICommunicationAsyncCallbackService communicationAsyncCallbackService;
    
    @PayloadRoot(localPart = COMMUNICATION_ASYNC_REQUEST, namespace = NAMESPACE)
    @ResponsePayload
    public CommAsyncResponse handleCommAsynchronously(
            @RequestPayload final CommAsyncRequest commAsyncRequest) {
    	return communicationAsyncCallbackService.processCommunicationCallback(commAsyncRequest);
    }

	
}
