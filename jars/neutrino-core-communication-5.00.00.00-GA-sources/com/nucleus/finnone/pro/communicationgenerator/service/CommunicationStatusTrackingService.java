package com.nucleus.finnone.pro.communicationgenerator.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Hibernate;

import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationGeneratorBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationStatusTrackingBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationStatusTrackingWrapper;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.entity.MailMessageExchangeRecordHistory;
import com.nucleus.message.entity.MessageExchangeRecordHistory;
import com.nucleus.service.BaseServiceImpl;

@Named("communicationStatusTrackingService")
public class CommunicationStatusTrackingService extends BaseServiceImpl implements ICommunicationStatusTrackingService {
	
	@Inject
    @Named("couchDataStoreDocumentService")
    private DatastorageService dataStorageService;
	
	@Inject
	@Named("communicationGeneratorBusinessObject")
	private ICommunicationGeneratorBusinessObject communicationGeneratorBusinessObject;
	
	@Inject
	@Named("communicationStatusTrackingBusinessObject")
	private ICommunicationStatusTrackingBusinessObject communicationStatusTrackingBusinessObject;
	
	private Map<String, byte[]> getAllAttachments(String attachmentStorageIds) {
		String []attachmentIdArray = communicationGeneratorBusinessObject.getAttachmentStorageIdArray(attachmentStorageIds);
		Map<String, byte[]> attachments = new HashMap<>(attachmentIdArray.length);
		for (String attachmentId : attachmentIdArray) {
			File file = retieveDocumentById(attachmentId);
			try {
				attachments.put(file.getName(), Files.readAllBytes(file.toPath()));
			} catch (IOException ioe) {
				BaseLoggers.exceptionLogger.error("Exception occured while getting file for attachmentId : " + attachmentId, ioe);
			}
		}
		return attachments;
	}
	
	private boolean isMailTypeCommunication(String communicationCode) {
		return CommunicationType.EMAIL.equals(communicationCode);
	}
	
	private CommunicationStatusTrackingWrapper createStatusTrackingWrapper(CommunicationGenerationDetailHistory communicationGenerationHistory) {
		NeutrinoValidator.notNull(communicationGenerationHistory);
		Hibernate.initialize(communicationGenerationHistory.getCommunicationTemplate());
		CommunicationTemplate communicationTemplate = communicationGenerationHistory.getCommunicationTemplate();
		String communicationCode = communicationTemplate.getCommunication().getCommunicationType().getCode();
		if (CommunicationType.LETTER.equals(communicationCode)) {
			return createLetterStatusTrackingWrapper(communicationGenerationHistory);
		} else {
			MessageExchangeRecordHistory messageRecord = getSentCommunicationByUniqueRequestId(communicationGenerationHistory.getUniqueRequestId());
			return createSentCommunicationStatusWrapper(messageRecord, isMailTypeCommunication(communicationCode));
		}
	}

	private CommunicationStatusTrackingWrapper createSentCommunicationStatusWrapper(
			MessageExchangeRecordHistory messageRecordHistory, boolean isMailTypeCommunication) {
		NeutrinoValidator.notNull(messageRecordHistory);
		CommunicationStatusTrackingWrapper statusTrackingWrapper = new CommunicationStatusTrackingWrapper();
		statusTrackingWrapper.setMessageRecordHistory(messageRecordHistory);
		if (isMailTypeCommunication) {
			statusTrackingWrapper.setMailTypeCommunication(true);
			statusTrackingWrapper.setEmailAttachments(getAllAttachments(((MailMessageExchangeRecordHistory) messageRecordHistory).getAttachmentStorageIds()));
		}
		return statusTrackingWrapper;
	}

	private CommunicationStatusTrackingWrapper createLetterStatusTrackingWrapper(
			CommunicationGenerationDetailHistory communicationGenerationHistory) {
		CommunicationStatusTrackingWrapper statusTrackingWrapper = new CommunicationStatusTrackingWrapper();
		statusTrackingWrapper.setLetterTypeCommunication(true);
		try {
			statusTrackingWrapper.setStoredLetter(Files.readAllBytes(retieveDocumentById(communicationGenerationHistory.getLetterStorageId()).toPath()));
		} catch (IOException ioe) {
			throw new SystemException("Could not fetch document for attachment id.", ioe);
		}
		return statusTrackingWrapper;
	}
	
	@Override
	public File retieveDocumentById(String attachmentId) {
		File retriveDocument = dataStorageService.retriveDocument(attachmentId);
		try {
			if (retriveDocument == null) {
				throw new SystemException("Document does not exist for given attachment id: " + attachmentId);
			}
			return retriveDocument;
		} catch (Exception e) {
			//BaseLoggers.exceptionLogger.error("Exception while parsing attachment id : " + attachmentId, e);
			throw new SystemException("Could not fetch document for attachment id: " + attachmentId, e);
		}
	}
	
	@Override
	public List<CommunicationGenerationDetailHistory> getAllCommunicationStatusByEventRequestLogId(
			String eventRequestLogId) {
		NeutrinoValidator.notNull(eventRequestLogId);
		return communicationStatusTrackingBusinessObject.getAllCommunicationHistoriesByEventRequestLogId(eventRequestLogId);
	}

	@Override
	public <T extends MessageExchangeRecordHistory> T getSentCommunicationByUniqueRequestId(String uniqueRequestId) {
		NeutrinoValidator.notNull(uniqueRequestId);
		return communicationStatusTrackingBusinessObject.getMessageRecordHistoryByUniqueId(uniqueRequestId);
	}

	@Override
	public <T extends MessageExchangeRecordHistory> List<T> getAllSentCommunicationByEventRequestLogId(String eventRequestLogId) {
		return communicationStatusTrackingBusinessObject.getAllMessageRecordHistoryByEventRequestLogId(eventRequestLogId);
	}

	@Override
	public CommunicationGenerationDetailHistory getCommunicationHistoryByEventRequestLogId(String eventRequestLogId) {
		List<CommunicationGenerationDetailHistory> communicationHistories = getAllCommunicationStatusByEventRequestLogId(eventRequestLogId);
		if (ValidatorUtils.hasNoElements(communicationHistories)) {
			return null;
		}
		if (communicationHistories.size() > 1) {
			throw new BusinessException("Multiple communication histories found for CommunicationEventRequestLog id : " + eventRequestLogId);
		}
		return communicationHistories.get(0);
	}

	@Override
	public Map<String, byte[]> getAllAttachmentsInCommunicationByUniqueRequestId(String uniqueRequestId) {
		NeutrinoValidator.notNull(uniqueRequestId);
		String attachmentStorageIds = communicationStatusTrackingBusinessObject.getAttachmentStorageIds(uniqueRequestId);
		return getAllAttachments(attachmentStorageIds);
	}

	@Override
	public Map<String, byte[]> getAllAttachmentsInCommunication(
			CommunicationGenerationDetailHistory communicationDetailHistory) {
		NeutrinoValidator.notNull(communicationDetailHistory);
		return getAllAttachmentsInCommunicationByUniqueRequestId(communicationDetailHistory.getUniqueRequestId());
	}

	@Override
	public <T extends MessageExchangeRecordHistory> T getSentCommunicationByEventRequestId(String eventRequestLogId) {
		List<T> messageRecordHistories = getAllSentCommunicationByEventRequestLogId(eventRequestLogId);
		if (ValidatorUtils.hasNoElements(messageRecordHistories)) {
			return null;
		}
		if (messageRecordHistories.size() > 1) {
			throw new SystemException("Multiple MessageExchangeRecordHistory found for CommunicationEventRequestLog id : " + eventRequestLogId);
		}
		return messageRecordHistories.get(0);
	}
	
	@Override
	public List<CommunicationStatusTrackingWrapper> getStatusTrackingWrapperByEventRequestId(String eventRequestLogId) {
		List<CommunicationGenerationDetailHistory> communicationHistories = getAllCommunicationStatusByEventRequestLogId(eventRequestLogId);
		List<CommunicationStatusTrackingWrapper> communicationTrackingWrappers = new ArrayList<>();
		for (CommunicationGenerationDetailHistory communicationHistory : communicationHistories) {
			communicationTrackingWrappers.add(createStatusTrackingWrapper(communicationHistory));
		}
		return communicationTrackingWrappers;
	}
	
	@Override
	public CommunicationStatusTrackingWrapper getStatusTrackingWrapperByUniqueRequestId(String uniqueRequestId) {
		CommunicationGenerationDetailHistory communicationGenerationHistory = communicationStatusTrackingBusinessObject.getCommunicationHistoryByUniqueRequestId(uniqueRequestId);
		return createStatusTrackingWrapper(communicationGenerationHistory);
	}
	
	@Override
	public CommunicationStatusTrackingWrapper getStatusTrackingWrapper(CommunicationGenerationDetailHistory communicationGenerationHistory) {
		return createStatusTrackingWrapper(communicationGenerationHistory);
	}
	
	@Override
	public MessageExchangeRecordHistory getLatestSentCommunicationByEventRequestLogId(String eventRequestLogId) {
		return communicationStatusTrackingBusinessObject.getLatestMessageHistoryByEventRequestLogId(eventRequestLogId);
	}

	@Override
	public MessageExchangeRecordHistory getLatestSentCommunicationByUniqueRequestId(String uniqueRequestId) {
		return communicationStatusTrackingBusinessObject.getLatestMessageHistoryByParentUniqueRequestId(uniqueRequestId);
	}
	
}
