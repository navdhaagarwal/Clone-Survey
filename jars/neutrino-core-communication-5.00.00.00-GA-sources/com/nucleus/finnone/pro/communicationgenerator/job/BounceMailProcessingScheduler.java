package com.nucleus.finnone.pro.communicationgenerator.job;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.scheduler.NeutrinoScheduler;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationGeneratorDAO;
import com.nucleus.finnone.pro.communicationgenerator.service.BounceMailProcessorService;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.service.MessageProcessor;
import com.nucleus.message.entity.MessageDeliveryStatus;
import com.nucleus.message.entity.MessageExchangeRecordHistory;

@Named("bounceMailProcessingScheduler")
public class BounceMailProcessingScheduler implements NeutrinoScheduler {
	
	@Inject 
	@Named("bounceMailProcessorService")
	private BounceMailProcessorService bounceMailProcessorService;
	
	private ICommunicationGeneratorDAO communicationGeneratorDAO;
	
	private MessageProcessor<Boolean> bounceMessageProcessor = this::process;
	
	private void updateMessageExchangeRecordHistory(String uniqueIdentifier, MessageDeliveryStatus messageStatus) {
		MessageExchangeRecordHistory messageExchnageRecordHistory = communicationGeneratorDAO.getMessageExchangeRecordHistoryByUniqueId(uniqueIdentifier);
		if (ValidatorUtils.notNull(messageExchnageRecordHistory)) {
			messageExchnageRecordHistory.setDeliveryStatus(messageStatus);
			communicationGeneratorDAO.updateMessageExchangeRecordHistory(messageExchnageRecordHistory);
		}		
	}
	
	@Transactional
	public boolean process(Message message) {
		try {
			String uniqueId = ((MimeMessage) message).getMessageID();
			updateMessageExchangeRecordHistory(uniqueId, MessageDeliveryStatus.MAIL_BOUNCED);
			return true;
		} catch (MessagingException e) {
			BaseLoggers.exceptionLogger.debug("Error while retrieving message id from mimeMessage.");
			return false;
		}
	}
		
	@Override
	public void execute() {
		bounceMailProcessorService.processUnreadMessagesInBatchAndMarkFlag(Flag.SEEN, bounceMessageProcessor);
		//bounceMailProcessorService.deleteMessagesPermanently();
	}
	
}
