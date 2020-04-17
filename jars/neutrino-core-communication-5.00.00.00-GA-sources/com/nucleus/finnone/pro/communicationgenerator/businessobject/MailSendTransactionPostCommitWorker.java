package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationConstants;
import com.nucleus.mail.MimeMailMessageBuilder;

@Named("mailSendTransactionPostCommitWorker")
public class MailSendTransactionPostCommitWorker extends CommunicationGeneratorPostCommitWorker {
	
	@Inject
    @Named("mailMessageIntegrationService")
    private MailMessageIntegrationService mailMessageIntegrationService;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void work(Object argument) {
		if (argument instanceof MimeMailMessageBuilder) {
			try {
				mailMessageIntegrationService.sendMailMessageToIntegration(
						((MimeMailMessageBuilder)argument).getMimeMessage(), getUniqueId(argument), true);
				/*MailMessageSendResponsePojo mailMessageSendResponsePojo = mailMessageIntegrationService.sendMailMessageToIntegration(
						((MimeMailMessageBuilder)argument).getMimeMessage(), getUniqueId(argument), true);
				processMessageExchangeRecord(mailMessageSendResponsePojo);*/
			} catch (MessagingException | IOException mioe) {
				handleFailure(argument);
			}
		}
	}
	
	@Override
	public String getUniqueId(Object argument) {
		return ((MimeMailMessageBuilder) argument).getUniqueRequestId();
	}

	@Override
	public Class<?> getCachedMessageExchangeRecordClass() {
		return CommunicationConstants.CACHED_MAIL_CLASS;
	}

	@Override
	public String getRetryAttemptsConfigKey() {
		return CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY;
	}

}
