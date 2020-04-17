/*package com.nucleus.cfi.push.service;

import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.cfi.integration.common.AbstractIntegrationService;
import com.nucleus.cfi.integration.common.TransactionHeaderMessageCallback;
import com.nucleus.cfi.push.pojo.PushNotification;
import com.nucleus.cfi.push.pojo.PushNotificationResponsePojo;
import com.nucleus.cfi.ws.client.stub.communication.async.CommAsyncRequest;
import com.nucleus.cfi.ws.client.stub.pushNotificationService.PushNotificationRequest;
import com.nucleus.cfi.ws.client.stub.pushNotificationService.PushNotificationResponse;

@Named("pushNotificationIntegrationService")
public class PushNotificationIntegrationServiceImpl extends AbstractIntegrationService
        implements PushNotificationIntegrationService {
    public static final String FAILED         = "FAILED";
    public static final String FAILED_TO_SEND = "FAILED_TO_SEND";

    @Value("${cfi.ws.client.url.pushNotificationServiceURL}")
    @Override
    public void setWebServiceUrl(String webServiceUrl) {
        LOGGER.info("Setting web service url to {}", webServiceUrl);
        this.webServiceUrl = webServiceUrl;
    }
    private void setPushNotificationStatusinMessageResponse(PushNotificationResponse pushNotificationResponse,
            PushNotificationResponsePojo pushNotificationResponsePojo, Exception exception) {
        if (exception != null && exception.getMessage() != null) {
            if (exception.getMessage().length() > 255)
                pushNotificationResponsePojo.setMessageStatus(exception.getMessage().substring(0, 255));
            else
                pushNotificationResponsePojo.setMessageStatus(exception.getMessage());
        } else if (pushNotificationResponse.getMessageStatus() != null) {
            if (pushNotificationResponse.getMessageStatus().length() > 255)
                pushNotificationResponsePojo
                        .setMessageStatus(pushNotificationResponse.getMessageStatus().substring(0, 255));
            else
                pushNotificationResponsePojo.setMessageStatus(pushNotificationResponse.getMessageStatus());
        }
    }

    private PushNotificationResponsePojo sendPushNotification(PushNotification pushNotification, boolean isAsync) {
        PushNotificationRequest pushNotificationRequest = new PushNotificationRequest();
        pushNotificationRequest.setBody(pushNotification.getBody());
        pushNotificationRequest.setUniqueId(pushNotification.getUniqueRequestId());
        pushNotificationRequest.setDeviceId(pushNotification.getDeviceId());
        PushNotificationResponsePojo pushNotificationResponsePojo = new PushNotificationResponsePojo();
        pushNotificationResponsePojo.setUniqueId(pushNotification.getUniqueRequestId());
        PushNotificationResponse pushNotificationResponse = new PushNotificationResponse();
        try {
            pushNotificationRequest.setCorrelationId(pushNotification.getUniqueRequestId());
            pushNotificationRequest.setAsyncRequest(isAsync);
            Object pushNotificationResponse1 = webServiceTemplate.marshalSendAndReceive(webServiceUrl, pushNotificationRequest,
                    new TransactionHeaderMessageCallback(getNamespaceURIForJaxbObject(pushNotificationRequest)));
            if (pushNotificationResponse1 instanceof PushNotificationResponse) {
                processSyncPushNotificationResponse(pushNotificationRequest, (PushNotificationResponse) pushNotificationResponse1,
                        pushNotificationResponsePojo);
            } else if(pushNotificationResponse1 instanceof CommAsyncRequest){
                // This block is for Async push notification processing. So "pushnotificationresponse instanceof CommAsyncRequest" would be
                // true here.
                // All we get here is an acknowledgement from integration. It can be processed if needed.
                // process acknowledgement here if you want.
                return null;
            }else{
                return null;
            }
        } catch (Exception e) {
            pushNotificationResponsePojo.setDeliveryStatus(FAILED_TO_SEND);
            setPushNotificationStatusinMessageResponse(pushNotificationResponse, pushNotificationResponsePojo, e);
        }
        return pushNotificationResponsePojo;
    }

    private void processSyncPushNotificationResponse(PushNotificationRequest pushNotificationRequest,
            PushNotificationResponse pushNotificationResponse, PushNotificationResponsePojo pushNotificationResponsePojo) {
        LOGGER.info("Response from pushNotificationService for notification sent to {}", pushNotificationRequest.getDeviceId());
        if (pushNotificationResponse.getReceiptTimestamp() != null) {
            pushNotificationResponsePojo
                    .setReceiptTimestamp(new DateTime(pushNotificationResponse.getReceiptTimestamp().toGregorianCalendar()));
        }
        pushNotificationResponsePojo.setMessageReceiptId(pushNotificationResponse.getMessageReceiptId());
        pushNotificationResponsePojo.setDeliveryStatus(pushNotificationResponse.getDeliveryStatus().value());
        setPushNotificationStatusinMessageResponse(pushNotificationResponse, pushNotificationResponsePojo, null);
    }

    @Override
    public PushNotificationResponsePojo sendPushNotification(PushNotification pushNotification) {
        return sendPushNotification(pushNotification, false);
    }

    @Override
    public void setRemoteSystemWebServiceUrl(String remoteSystemwebServiceUrl) {

    }

    @Override
    public PushNotificationResponsePojo sendPushNotificationAsynchronously(PushNotification pushNotification) {
        return sendPushNotification(pushNotification, true);
    }
    
}
*/