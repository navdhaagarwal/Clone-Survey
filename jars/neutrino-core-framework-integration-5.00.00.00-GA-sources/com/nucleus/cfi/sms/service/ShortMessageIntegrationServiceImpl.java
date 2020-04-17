/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.cfi.sms.service;

import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.cfi.integration.common.AbstractIntegrationService;
import com.nucleus.cfi.integration.common.TransactionHeaderMessageCallback;
import com.nucleus.cfi.sms.pojo.ShortMessageSendResponsePojo;
import com.nucleus.cfi.sms.pojo.SmsMessage;
import com.nucleus.cfi.ws.client.stub.shortMessageService.ShortMessageSendRequest;
import com.nucleus.cfi.ws.client.stub.shortMessageService.ShortMessageSendResponse;
import com.nucleus.core.initialization.ProductInformationLoader;


/**
 * @author Nucleus Software Exports Limited
 *
 */
@Named("shortMessageIntegrationService")
public class ShortMessageIntegrationServiceImpl extends AbstractIntegrationService implements ShortMessageIntegrationService {

	public static final String FAILED = "FAILED";
	
	public static final String FAILED_TO_SEND ="FAILED_TO_SEND";
	
	@Value("${cfi.ws.client.url.shortMessageServiceURL}")
	@Override
	public void setWebServiceUrl(String webServiceUrl) {
		LOGGER.info("Setting web service url to {}", webServiceUrl);
		this.webServiceUrl = webServiceUrl;
	}

	private void setMessageStatusinMessageResponse(
			ShortMessageSendResponse shortMessageSendResponse,
			ShortMessageSendResponsePojo shortMessageSendResponsePojo,
			Exception exception) {
		if (exception != null && exception.getMessage() != null) {
			if (exception.getMessage().length() > 255)
				shortMessageSendResponsePojo.setMessageStatus(exception.getMessage().substring(0,255));        		
			else
				shortMessageSendResponsePojo.setMessageStatus(exception.getMessage()); 
		} else if (shortMessageSendResponse.getMessageStatus() != null) {
			if (shortMessageSendResponse.getMessageStatus().length() > 255)
				shortMessageSendResponsePojo.setMessageStatus(shortMessageSendResponse.getMessageStatus().substring(0,255));
			else
				shortMessageSendResponsePojo.setMessageStatus(shortMessageSendResponse.getMessageStatus());
		}
	}

	@Override
	public ShortMessageSendResponsePojo sendShortMessage(SmsMessage smsMessage, boolean isAsync) {
		ShortMessageSendRequest shortMessageSendRequest = new ShortMessageSendRequest();
		shortMessageSendRequest.setTo(smsMessage.getTo());
		shortMessageSendRequest.setFrom(smsMessage.getFrom());
		shortMessageSendRequest.setBody(smsMessage.getBody());
		shortMessageSendRequest.setUniqueId(smsMessage.getUniqueRequestId());
        shortMessageSendRequest.setMessageOriginatorId(ProductInformationLoader.getProductCode());
		ShortMessageSendResponsePojo shortMessageSendResponsePojo = new ShortMessageSendResponsePojo();
		shortMessageSendResponsePojo.setUniqueId(smsMessage.getUniqueRequestId());
		ShortMessageSendResponse shortMessageSendResponse = new ShortMessageSendResponse();
		try {
			shortMessageSendRequest.setCorrelationId(smsMessage.getUniqueRequestId());
			shortMessageSendRequest.setAsyncRequest(isAsync);
			Object smsSendResponse = webServiceTemplate.marshalSendAndReceive(webServiceUrl,
							shortMessageSendRequest, new TransactionHeaderMessageCallback(
							getNamespaceURIForJaxbObject(shortMessageSendRequest)));
			if (smsSendResponse instanceof ShortMessageSendResponse) {
				processSyncShortMessageSendResponse(shortMessageSendRequest, (ShortMessageSendResponse)smsSendResponse, shortMessageSendResponsePojo);
			} else {
				//This block is for Async SMS send processing. So "smsSendResponse instanceof CommAsyncRequest" would be true here.
				//All we get here is an acknowledgement from integration. It can be processed if needed.
				//process acknowledgement here if you want.
				return null;
			}
		} catch (Exception e) {
			shortMessageSendResponsePojo.setDeliveryStatus(FAILED_TO_SEND);
			setMessageStatusinMessageResponse(shortMessageSendResponse, shortMessageSendResponsePojo, e);
		}
		return shortMessageSendResponsePojo;
	}

	private void processSyncShortMessageSendResponse(ShortMessageSendRequest shortMessageSendRequest,
													 ShortMessageSendResponse shortMessageSendResponse,
													 ShortMessageSendResponsePojo shortMessageSendResponsePojo) {
		LOGGER.info("Response from shortMessageService for SMS sent to {}", shortMessageSendRequest.getTo());
		if (shortMessageSendResponse.getReceiptTimestamp() != null) {
			shortMessageSendResponsePojo.setReceiptTimestamp(new DateTime(shortMessageSendResponse.getReceiptTimestamp()
					.toGregorianCalendar()));
		}
		shortMessageSendResponsePojo.setMessageReceiptId(shortMessageSendResponse.getMessageReceiptId());
		shortMessageSendResponsePojo.setDeliveryStatus(shortMessageSendResponse.getDeliveryStatus().value());
		setMessageStatusinMessageResponse(shortMessageSendResponse,shortMessageSendResponsePojo,null);		
	}

	@Override
	public ShortMessageSendResponsePojo sendShortMessage(SmsMessage smsMessage) {
		return sendShortMessage(smsMessage, false);
	}

	@Override
	public void setRemoteSystemWebServiceUrl(String remoteSystemwebServiceUrl) {

	}

	@Override
	public ShortMessageSendResponsePojo sendShortMessageAsynchronously(SmsMessage smsMessage) {
		return sendShortMessage(smsMessage, true);
	}

}
