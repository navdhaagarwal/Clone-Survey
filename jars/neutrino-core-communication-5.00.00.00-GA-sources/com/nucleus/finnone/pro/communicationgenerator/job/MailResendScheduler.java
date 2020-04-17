package com.nucleus.finnone.pro.communicationgenerator.job;

import javax.inject.Named;

import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationConstants;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.entity.MailMessageExchangeRecord;
import com.nucleus.message.entity.MessageExchangeRecord;

@Named("mailResendScheduler")
public class MailResendScheduler extends CommunicationResendScheduler {
	
	@Override
	public Class<?> getCachedMessageExchangeRecordClass() {
		return CommunicationConstants.CACHED_MAIL_CLASS;
	}

	@Override
	public void sendMessage(MessageExchangeRecord messageExchangeRecord) {
		try {
			communicationGeneratorDAO.detach(messageExchangeRecord);
			communicationGeneratorBusinessObject.createMimeMessageBuilderAndMailMessage((MailMessageExchangeRecord) messageExchangeRecord);
		} catch (Exception e) {
			 BaseLoggers.exceptionLogger.error("Mail resend scheduler failed for mailMessageExchangeRecord id : " + messageExchangeRecord.getId(), e);	
    	}		
	}

}
