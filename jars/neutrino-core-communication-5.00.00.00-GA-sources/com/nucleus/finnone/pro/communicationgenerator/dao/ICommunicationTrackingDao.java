package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.List;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.message.entity.MessageExchangeRecordHistory;

public interface ICommunicationTrackingDao {
	
	public long getCountOfCommunicationHistoriesByEventRequestLogId(String eventRequestLogId);

	public List<CommunicationGenerationDetailHistory> getAllCommunicationHistoriesByEventRequestLogId(String eventRequestLogId);
	
	public long getCountOfSentMessagesByEventRequestLogId(String eventRequestLogId);

	public <T extends MessageExchangeRecordHistory> List<T> getAllMessageRecordHistoryByEventRequestLogId(String eventRequestLogId);

	public String getAttachmentStorageIds(String uniqueRequestId);

	public <T extends MessageExchangeRecordHistory> T getMessageRecordHistoryByUniqueId(String uniqueRequestId);

	public CommunicationGenerationDetailHistory getCommunicationHistoryByUniqueRequestId(String uniqueRequestId);

	public MessageExchangeRecordHistory getLatestMessageHistoryByEventRequestLogId(String eventRequestLogId);

	public MessageExchangeRecordHistory getLatestMessageHistoryByParentUniqueRequestId(String uniqueRequestId);

	public CommunicationGenerationDetailHistory getCommunicationHistoryByUniqueBarcodeReferenceNumber(String barcodeReferenceNumber);
	
}
