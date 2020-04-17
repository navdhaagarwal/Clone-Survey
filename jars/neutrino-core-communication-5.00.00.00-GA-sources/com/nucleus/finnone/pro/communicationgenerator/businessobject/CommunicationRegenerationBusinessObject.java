package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.communicationgenerator.dao.CommunicationTrackingDao;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.message.entity.MessageExchangeRecordHistory;

@Named("communicationRegenerationBusinessObject")
public class CommunicationRegenerationBusinessObject implements ICommunicationRegenerationBusinessObject {
	
	@Inject
	@Named("communicationTrackingDao")
	private CommunicationTrackingDao communicationTrackingDao;

	@Override
	public void saveMessageRecordHistory(MessageExchangeRecordHistory messageRecordHistory) {
		communicationTrackingDao.persist(messageRecordHistory);
	}

	@Override
	public CommunicationGenerationDetailHistory getCommunicationHistoryByUniqueRequestId(String uniqueRequestId) {
		return communicationTrackingDao.getCommunicationHistoryByUniqueRequestId(uniqueRequestId);
	}

	@Override
	public MessageExchangeRecordHistory getMessageRecordHistoryByUniqueId(String uniqueRequestId) {
		return communicationTrackingDao.getMessageRecordHistoryByUniqueId(uniqueRequestId);
	}

}
