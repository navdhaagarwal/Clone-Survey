package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.cfi.integration.common.MessageSendResponsePojo;
import com.nucleus.core.transaction.TransactionPostCommitWorkFailureHandler;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationGeneratorDAO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.message.entity.MessageDeliveryStatus;
import com.nucleus.message.entity.MessageExchangeRecord;
import com.nucleus.message.entity.MessageExchangeRecordHistory;

public abstract class CommunicationGeneratorPostCommitWorker implements TransactionPostCommitWorkFailureHandler {

	public static final String FAILED = "FAILED";

	public static final String DELIVERED = "DELIVERED";

	public static final String FAILED_TO_SEND = "FAILED_TO_SEND";

	@Inject
    @Named("communicationGeneratorDAO")
	protected ICommunicationGeneratorDAO communicationGeneratorDAO;
	
	@Inject
	@Named("communicationGeneratorBusinessObject")
	protected CommunicationGeneratorBusinessObject communicationGeneratorBusinessObject;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleFailure(Object argument) {
		MessageExchangeRecord messageExchangeRecord = communicationGeneratorDAO
				.getMessageExchangeRecordByUniqueId(getCachedMessageExchangeRecordClass(), getUniqueId(argument));
		messageExchangeRecord.setDeliveryStatus(MessageDeliveryStatus.FAILED_SENDING_TO_INTEGRATION);
		communicationGeneratorDAO.update(messageExchangeRecord);
	}
	
	public <T extends MessageExchangeRecord> void processMessageExchangeRecord(MessageSendResponsePojo messageSendResponsePojo) {
		T messageExchangeRecord = communicationGeneratorDAO
				.getMessageExchangeRecordByUniqueId(getCachedMessageExchangeRecordClass(), messageSendResponsePojo.getUniqueId());
		if (!DELIVERED.equals(messageSendResponsePojo.getDeliveryStatus())) {
			messageExchangeRecord.setRetryAttemptsConfigKey(getRetryAttemptsConfigKey());
			if (communicationGeneratorBusinessObject.checkAndDeleteIfRetryAttemptExceeded(
					messageExchangeRecord, messageExchangeRecord.getRetriedAttemptsDone() + 1)) {
				return;
			}
			messageExchangeRecord
					.setRetriedAttemptsDone(messageExchangeRecord.getRetriedAttemptsDone() + 1);
			if (FAILED.equals(messageSendResponsePojo.getDeliveryStatus())) {
				messageExchangeRecord.setDeliveryStatus(MessageDeliveryStatus.FAILED_AT_INTEGRATION);

			} else if (FAILED_TO_SEND.equals(messageSendResponsePojo.getDeliveryStatus())) {
				messageExchangeRecord.setDeliveryStatus(MessageDeliveryStatus.FAILED_SENDING_TO_INTEGRATION);
			}
			communicationGeneratorDAO.update(messageExchangeRecord);
		} else if (ValidatorUtils.notNull(messageExchangeRecord)) {
			messageExchangeRecord.setDeliveryTimestamp(messageSendResponsePojo.getReceiptTimestamp());
			communicationGeneratorDAO.update(messageExchangeRecord);
		} else {
			MessageExchangeRecordHistory messageRecordHistory = communicationGeneratorDAO.getMessageExchangeRecordHistoryByUniqueId(messageSendResponsePojo.getUniqueId());
			if (ValidatorUtils.notNull(messageRecordHistory)) {
				messageRecordHistory.setDeliveryTimestamp(messageSendResponsePojo.getReceiptTimestamp());
				communicationGeneratorDAO.update(messageRecordHistory);
			}
		}
	}
	
	public abstract String getRetryAttemptsConfigKey();

	public abstract Class<?> getCachedMessageExchangeRecordClass();
	
	public abstract String getUniqueId(Object argument);
}
