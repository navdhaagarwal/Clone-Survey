package com.nucleus.finnone.pro.communicationgenerator.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.NeutrinoUUIDGenerator;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationGeneratorBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationRegenerationBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.entity.MailMessageExchangeRecordHistory;
import com.nucleus.message.entity.MessageDeliveryStatus;
import com.nucleus.message.entity.MessageExchangeRecordHistory;
import com.nucleus.message.entity.ShortMessageRecordHistory;
import com.nucleus.service.BaseServiceImpl;

@Named("communicationRegenerationService")
public class CommunicationRegenerationService extends BaseServiceImpl implements ICommunicationRegenerationService {
	
	@Inject
	@Named("communicationRegenerationBusinessObject")
	private ICommunicationRegenerationBusinessObject communicationRegenerationBusinessObject;
	
	@Inject
	@Named("communicationStatusTrackingService")
	private ICommunicationStatusTrackingService communicationStatusTrackingService;
	
	@Inject
    @Named("communicationGeneratorBusinessObject")
    private ICommunicationGeneratorBusinessObject communicationGeneratorBusinessObject;
	
	private NeutrinoUUIDGenerator uuidGenerator = new NeutrinoUUIDGenerator(ProductInformationLoader.getProductCode() + "-");
	
	private void validateStoredLetter(CommunicationGenerationDetailHistory communicationHistory) {
		if (communicationHistory.getLetterStorageId() == null) {
			throw new SystemException("Letter is not stored for uniqueRequestId: " + communicationHistory.getUniqueRequestId());
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T extends MessageExchangeRecordHistory> T createCloneMessageRecordHistory(
			MessageExchangeRecordHistory messageRecordHistory) {
		MessageExchangeRecordHistory clonedMessageRecordHistory = (MessageExchangeRecordHistory) 
				messageRecordHistory.cloneYourself(CloneOptionConstants.SNAPSHOT_CLONING_OPTION);
		clonedMessageRecordHistory.setParentUniqueRequestId(messageRecordHistory.getUniqueRequestId());
		clonedMessageRecordHistory.setUniqueRequestId(uuidGenerator.generateUuid());
		clonedMessageRecordHistory.setDeliveryStatus(MessageDeliveryStatus.SENT_TO_INTEGRATION);
		return (T) clonedMessageRecordHistory;
	}
	
	private void resendMailCommunication(MessageExchangeRecordHistory messageRecordHistory) {
		MessageExchangeRecordHistory mailRecordHistory = createCloneMessageRecordHistory(messageRecordHistory);
		communicationRegenerationBusinessObject.saveMessageRecordHistory(mailRecordHistory);
		communicationGeneratorBusinessObject.createMimeMessageBuilderAndSendMail((MailMessageExchangeRecordHistory) mailRecordHistory);
	}
	
	private void resendSmsCommunication(MessageExchangeRecordHistory messageRecordHistory) {
		MessageExchangeRecordHistory smsRecordHistory = createCloneMessageRecordHistory(messageRecordHistory);
		communicationRegenerationBusinessObject.saveMessageRecordHistory(smsRecordHistory);
		communicationGeneratorBusinessObject.sendSMSFromMessageRecordHistory((ShortMessageRecordHistory) smsRecordHistory);
	}
	
	@Override
	public Map<String, byte[]> regenerateStoredLetterByUniqueRequestId(String uniqueRequestId) {
		NeutrinoValidator.notNull(uniqueRequestId);
		CommunicationGenerationDetailHistory communicationHistory = communicationRegenerationBusinessObject.getCommunicationHistoryByUniqueRequestId(uniqueRequestId);
		if (communicationHistory == null) {
			throw new SystemException("No CommunicationGenerationDetailHistory found for uniqueRequestId: " + uniqueRequestId);
		}
		validateStoredLetter(communicationHistory);
		File retievedDocument = communicationStatusTrackingService.retieveDocumentById(communicationHistory.getLetterStorageId());
		Map<String, byte[]> fileNameAndContent = new HashMap<>();
		try {
			String fileName = communicationHistory.getAttachmentName()!=null?communicationHistory.getAttachmentName():retievedDocument.getName();
			fileNameAndContent.put(fileName, Files.readAllBytes(retievedDocument.toPath()));
		} catch (IOException ioe) {
			BaseLoggers.exceptionLogger.error("Exception occured while regenerating communication letter for uniqueRequestId: " + uniqueRequestId, ioe);
			return Collections.emptyMap();
		}
		return fileNameAndContent;
	}

	@Override
	public boolean resendCommunication(String uniqueRequestId) {
		MessageExchangeRecordHistory messageRecordHistory = communicationRegenerationBusinessObject.getMessageRecordHistoryByUniqueId(uniqueRequestId);
		if (messageRecordHistory == null) {
			throw new SystemException("No CommunicationGenerationDetailHistory found for uniqueRequestId: " + uniqueRequestId);
		}
		try {
			if (messageRecordHistory instanceof MailMessageExchangeRecordHistory) {
				resendMailCommunication(messageRecordHistory);
			} else {
				resendSmsCommunication(messageRecordHistory);
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Exception occured while resending communication for uniqueRequestId: " + uniqueRequestId, e);
			return false;
		}
		return true;
	}

}
