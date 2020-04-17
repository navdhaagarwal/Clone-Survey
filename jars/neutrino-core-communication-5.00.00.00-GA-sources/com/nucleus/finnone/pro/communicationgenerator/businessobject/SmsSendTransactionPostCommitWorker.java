package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.cfi.sms.pojo.SmsMessage;
import com.nucleus.cfi.sms.service.ShortMessageIntegrationService;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationConstants;

@Named("smsSendTransactionPostCommitWorker")
public class SmsSendTransactionPostCommitWorker extends CommunicationGeneratorPostCommitWorker{
	
	@Inject
    @Named("shortMessageIntegrationService")
    private ShortMessageIntegrationService shortMessageIntegrationService;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void work(Object argument) {
		if (argument instanceof SmsMessage) {
			shortMessageIntegrationService
			.sendShortMessage((SmsMessage) argument);
			/*MessageSendResponsePojo messageSendResponsePojo = shortMessageIntegrationService
					.sendShortMessage((SmsMessage) argument);
			processMessageExchangeRecord(messageSendResponsePojo);*/
		}
	}
	
	@Override
	public Class<?> getCachedMessageExchangeRecordClass() {
		return CommunicationConstants.CACHED_SMS_CLASS;
	}

	@Override
	public String getUniqueId(Object argument) {
		return ((SmsMessage) argument).getUniqueRequestId();
	}

	@Override
	public String getRetryAttemptsConfigKey() {
		return CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY;
	}

}
