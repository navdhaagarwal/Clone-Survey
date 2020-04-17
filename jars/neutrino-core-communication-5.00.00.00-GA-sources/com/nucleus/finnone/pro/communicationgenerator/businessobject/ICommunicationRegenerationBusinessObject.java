package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.message.entity.MessageExchangeRecordHistory;

public interface ICommunicationRegenerationBusinessObject {

	public void saveMessageRecordHistory(MessageExchangeRecordHistory mailRecordHistory);

	public CommunicationGenerationDetailHistory getCommunicationHistoryByUniqueRequestId(String uniqueRequestId);

	public MessageExchangeRecordHistory getMessageRecordHistoryByUniqueId(String uniqueRequestId);

}
