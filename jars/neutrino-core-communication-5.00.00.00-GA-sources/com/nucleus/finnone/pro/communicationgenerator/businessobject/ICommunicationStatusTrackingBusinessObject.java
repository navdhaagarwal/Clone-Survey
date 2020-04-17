package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.List;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.message.entity.MessageExchangeRecordHistory;

public interface ICommunicationStatusTrackingBusinessObject {

	List<CommunicationGenerationDetailHistory> getAllCommunicationHistoriesByEventRequestLogId(
			String eventRequestLogId);

	<T extends MessageExchangeRecordHistory> T getMessageRecordHistoryByUniqueId(String uniqueRequestId);

	<T extends MessageExchangeRecordHistory> List<T> getAllMessageRecordHistoryByEventRequestLogId(String eventRequestLogId);

	String getAttachmentStorageIds(String uniqueRequestId);

	CommunicationGenerationDetailHistory getCommunicationHistoryByUniqueRequestId(String uniqueRequestId);

	MessageExchangeRecordHistory getLatestMessageHistoryByEventRequestLogId(String eventRequestLogId);

	MessageExchangeRecordHistory getLatestMessageHistoryByParentUniqueRequestId(String uniqueRequestId);

}
