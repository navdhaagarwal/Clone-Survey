package com.nucleus.finnone.pro.communicationgenerator.service;

import com.nucleus.cfi.ws.client.stub.communication.async.CommAsyncRequest;
import com.nucleus.cfi.ws.client.stub.communication.async.CommAsyncResponse;
import com.nucleus.message.entity.MessageExchangeRecord;

public interface ICommunicationAsyncCallbackService {
	
	<T extends MessageExchangeRecord> CommAsyncResponse processCommunicationCallback(CommAsyncRequest commAsyncRequest);

}
