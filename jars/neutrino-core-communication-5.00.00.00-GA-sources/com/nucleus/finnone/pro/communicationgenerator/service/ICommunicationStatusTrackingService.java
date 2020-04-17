package com.nucleus.finnone.pro.communicationgenerator.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationStatusTrackingWrapper;
import com.nucleus.message.entity.MessageExchangeRecordHistory;

public interface ICommunicationStatusTrackingService {

	public List<CommunicationGenerationDetailHistory> getAllCommunicationStatusByEventRequestLogId(String eventRequestLogId);

	public CommunicationGenerationDetailHistory getCommunicationHistoryByEventRequestLogId(String eventRequestLogId);

	public <T extends MessageExchangeRecordHistory> T getSentCommunicationByUniqueRequestId(String uniqueRequestId);

	public <T extends MessageExchangeRecordHistory> List<T> getAllSentCommunicationByEventRequestLogId(String eventRequestLogId);

	public Map<String, byte[]> getAllAttachmentsInCommunicationByUniqueRequestId(String uniqueRequestId);

	public Map<String, byte[]> getAllAttachmentsInCommunication(CommunicationGenerationDetailHistory communicationDetailHistory);

	public <T extends MessageExchangeRecordHistory> T getSentCommunicationByEventRequestId(String eventRequestLogId);

	public List<CommunicationStatusTrackingWrapper> getStatusTrackingWrapperByEventRequestId(String eventRequestLogId);
	
	public CommunicationStatusTrackingWrapper getStatusTrackingWrapperByUniqueRequestId(String uniqueRequestId);
	
	public MessageExchangeRecordHistory getLatestSentCommunicationByEventRequestLogId(String eventRequestLogId);
	
	public MessageExchangeRecordHistory getLatestSentCommunicationByUniqueRequestId(String uniqueRequestId);

	CommunicationStatusTrackingWrapper getStatusTrackingWrapper(CommunicationGenerationDetailHistory communicationGenerationHistory);

	public File retieveDocumentById(String attachmentId);

}
