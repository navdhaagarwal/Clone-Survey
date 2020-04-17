package com.nucleus.finnone.pro.communicationgenerator.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationConstants;
import com.nucleus.message.entity.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.cfi.ws.client.stub.communication.async.CommAsyncRequest;
import com.nucleus.cfi.ws.client.stub.communication.async.CommAsyncResponse;
import com.nucleus.cfi.ws.client.stub.communication.async.CommunicationAcknowledgement;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.CommunicationGeneratorBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationGeneratorDAO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.mail.entity.MailMessageExchangeRecord;
import com.nucleus.persistence.EntityDao;

@Named("communicationAsyncCallbackService")
public class CommunicationAsyncCallbackService implements ICommunicationAsyncCallbackService {

	@Inject
    @Named("communicationGeneratorDAO")
	protected ICommunicationGeneratorDAO communicationGeneratorDAO;
	
	@Inject
	@Named("communicationGeneratorBusinessObject")
	protected CommunicationGeneratorBusinessObject communicationGeneratorBusinessObject;
	
    @Inject
    @Named("entityDao")
    protected EntityDao                    entityDao;    
	
	@Override
	@Transactional
	public <T extends MessageExchangeRecord> CommAsyncResponse processCommunicationCallback(CommAsyncRequest commAsyncRequest) {
		String correlationId = commAsyncRequest.getCommunicationAcknowledgement().getCorrelationId();
    	T messageExchangeRecord = communicationGeneratorDAO.getMessageExchangeRecordByUniqueId(
    			MessageExchangeRecord.class,
    			correlationId);
    	if (ValidatorUtils.notNull(messageExchangeRecord)) {
    		updateAsyncCommunicationResponse(messageExchangeRecord, commAsyncRequest);
    	} else {
    		MessageExchangeRecordHistory messageRecordHistory = communicationGeneratorDAO.
    				getMessageExchangeRecordHistoryByUniqueId(correlationId);
			if (ValidatorUtils.notNull(messageRecordHistory)) {
				if (commAsyncRequest.isIsErrorResponse()) {
					messageRecordHistory.setDeliveryStatus(MessageDeliveryStatus.FAILED_AT_INTEGRATION);
				} else {
					messageRecordHistory.setDeliveryTimestamp(new DateTime(
							commAsyncRequest.getCommunicationAcknowledgement().getReceiptTimestamp().toGregorianCalendar()));
					messageRecordHistory.setDeliveryStatus(MessageDeliveryStatus.DELIVERED);
				}
				communicationGeneratorDAO.update(messageRecordHistory);
			}
    	}
    	CommAsyncResponse commAsyncResponse = new CommAsyncResponse();
    	commAsyncResponse.setCallbackResponse("Success!");
		return commAsyncResponse;
	}
	
	private <T extends MessageExchangeRecord> void updateAsyncCommunicationResponse(T messageExchangeRecord,
			CommAsyncRequest commAsyncRequest) {
		String retryAttemptsConfigKey = null;
		CommunicationAcknowledgement commAcknowledgement = commAsyncRequest.getCommunicationAcknowledgement();
    	if (messageExchangeRecord instanceof MailMessageExchangeRecord) {
    		retryAttemptsConfigKey = CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY;
    	} else if(messageExchangeRecord instanceof ShortMessageExchangeRecord){
    		retryAttemptsConfigKey = CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY;
    		((ShortMessageExchangeRecord) messageExchangeRecord).setStatusMessage(commAcknowledgement != null ? commAcknowledgement.getExceptionMessage() : null);
    	}
    	else if (messageExchangeRecord  instanceof WhatsAppExchangeRecord) {
    		retryAttemptsConfigKey = CommunicationGeneratorConstants.WHATSAPP_RETRY_ATTEMPT_CONFIG_KEY;
			((WhatsAppExchangeRecord) messageExchangeRecord).setStatusMessage(commAcknowledgement != null ?  commAcknowledgement.getExceptionMessage() : null);

		}
    	else if(messageExchangeRecord instanceof PushNotificationExchangeRecord){
            retryAttemptsConfigKey = CommunicationGeneratorConstants.PUSH_RETRY_ATTEMPT_CONFIG_KEY;
            ((PushNotificationExchangeRecord) messageExchangeRecord).setPushStatusMessage(commAcknowledgement != null ? commAcknowledgement.getExceptionMessage() : null);
        }
    	if (commAsyncRequest.isIsErrorResponse()) {
    		messageExchangeRecord.setRetryAttemptsConfigKey(retryAttemptsConfigKey);
			if ( !(messageExchangeRecord instanceof PushNotificationExchangeRecord)
			        &&
			        communicationGeneratorBusinessObject.checkAndDeleteIfRetryAttemptExceeded(
					messageExchangeRecord, messageExchangeRecord.getRetriedAttemptsDone() + 1)) {
				return ;
			}
			messageExchangeRecord.setRetriedAttemptsDone(messageExchangeRecord.getRetriedAttemptsDone() + 1);
			messageExchangeRecord.setDeliveryStatus(MessageDeliveryStatus.FAILED_AT_INTEGRATION);
    	} else {
    		messageExchangeRecord.setDeliveryTimestamp(new DateTime(
    				commAsyncRequest.getCommunicationAcknowledgement().getReceiptTimestamp().toGregorianCalendar()));
    		messageExchangeRecord.setDeliveryStatus(MessageDeliveryStatus.DELIVERED);
    	}
    	updateRespInPhoneNumber(messageExchangeRecord);
		communicationGeneratorDAO.update(messageExchangeRecord);
    	return ;
	}

	private <T extends MessageExchangeRecord> void updateRespInPhoneNumber(T messageExchangeRecord) {
		if (StringUtils.isNumeric(messageExchangeRecord.getExtIdentifier())) {
			PhoneNumber phoneNumber = entityDao.find(PhoneNumber.class,
					Long.parseLong(messageExchangeRecord.getExtIdentifier()));
			if (null != phoneNumber) {
				phoneNumber.setVerCodeDeliveryStatus(messageExchangeRecord.getDeliveryStatus());
				if (messageExchangeRecord instanceof ShortMessageExchangeRecord) {
					phoneNumber.setVerCodeDelStatusMessage(
							((ShortMessageExchangeRecord) messageExchangeRecord).getStatusMessage());
				}
				entityDao.update(phoneNumber);
			}
		}
	}

}
