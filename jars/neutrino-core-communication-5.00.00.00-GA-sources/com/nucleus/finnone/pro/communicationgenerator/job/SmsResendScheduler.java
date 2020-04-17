package com.nucleus.finnone.pro.communicationgenerator.job;

import javax.inject.Named;

import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationConstants;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.message.entity.MessageExchangeRecord;
import com.nucleus.message.entity.ShortMessageExchangeRecord;

@Named("smsResendScheduler")
public class SmsResendScheduler extends CommunicationResendScheduler {
	
	@Override
	public Class<?> getCachedMessageExchangeRecordClass() {
		return CommunicationConstants.CACHED_SMS_CLASS;
	}

	@Override
	public void sendMessage(MessageExchangeRecord messageExchangeRecord) {
		try {
			communicationGeneratorDAO.detach(messageExchangeRecord);
			communicationGeneratorBusinessObject.sendMessage((ShortMessageExchangeRecord) messageExchangeRecord);
		} catch (Exception e) {
			//log it
			BaseLoggers.exceptionLogger.error("Communication resend scheduler failed for shortMessageExchangeRecord id: " + messageExchangeRecord.getId(), e);	
		}	
	}

}
