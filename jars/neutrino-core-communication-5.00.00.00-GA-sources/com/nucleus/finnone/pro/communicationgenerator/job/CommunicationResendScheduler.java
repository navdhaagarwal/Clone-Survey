package com.nucleus.finnone.pro.communicationgenerator.job;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.scheduler.NeutrinoScheduler;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.CommunicationGeneratorBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationGeneratorDAO;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.message.entity.MessageExchangeRecord;

public abstract class CommunicationResendScheduler implements NeutrinoScheduler {
	
	@Inject
    @Named("communicationGeneratorDAO")
    public ICommunicationGeneratorDAO communicationGeneratorDAO;
	
	@Inject
	@Named("communicationGenerationHelper")
	public CommunicationGenerationHelper communicationGenerationHelper;
	
	@Inject
    @Named("communicationGeneratorBusinessObject")
	public CommunicationGeneratorBusinessObject communicationGeneratorBusinessObject;
		
	private static final int DEFAULT_BATCH_SIZE = 1000;	
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void execute() {
		// get all the message exchange record with following status:
		// failed_delivery, failed_sending, failed_at_integration, failed
		// create parallel streams for sending message to integration
		// handle those with retry attempt exceeded
		//recreate the task : TO DO recreate the task only if there are failed messages
		resendMessageExchangeRecord();
	}
	
	private void resendMessageExchangeRecord() {
		int batchSize = DEFAULT_BATCH_SIZE;
        Long startId = 0l;
        List<? extends MessageExchangeRecord> messageExchangeRecordBatch = communicationGeneratorDAO
				.getUndeliveredMessageExchangeRecord(getCachedMessageExchangeRecordClass(), startId, batchSize);
        while (hasElements(messageExchangeRecordBatch)) {
        	messageExchangeRecordBatch.stream().forEach(this::sendMessage);
        	startId = messageExchangeRecordBatch.get(messageExchangeRecordBatch.size() - 1).getId();
        	messageExchangeRecordBatch.clear();
        	messageExchangeRecordBatch = communicationGeneratorDAO
        				.getUndeliveredMessageExchangeRecord(getCachedMessageExchangeRecordClass(), startId, batchSize);
        }
	}
		
	public abstract Class<?> getCachedMessageExchangeRecordClass();
	
	public abstract void sendMessage(MessageExchangeRecord messageExchangeRecord);
}
