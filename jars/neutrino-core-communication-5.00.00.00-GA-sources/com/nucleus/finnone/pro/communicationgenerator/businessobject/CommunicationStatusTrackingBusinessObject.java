package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationGeneratorDAO;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationTrackingDao;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.message.entity.MessageExchangeRecordHistory;

@Named("communicationStatusTrackingBusinessObject")
public class CommunicationStatusTrackingBusinessObject implements ICommunicationStatusTrackingBusinessObject {
	
	@Inject
	@Named("communicationTrackingDao")
	private ICommunicationTrackingDao communicationTrackingDao;
	
	@Inject
	@Named("communicationGeneratorDAO")
	private ICommunicationGeneratorDAO communicationGeneratorDao;

	@Override
	public List<CommunicationGenerationDetailHistory> getAllCommunicationHistoriesByEventRequestLogId(
			String eventRequestLogId) {
		return communicationTrackingDao.getAllCommunicationHistoriesByEventRequestLogId(eventRequestLogId);
	}

	@Override
	public <T extends MessageExchangeRecordHistory> T getMessageRecordHistoryByUniqueId(String uniqueRequestId) {
		return communicationTrackingDao.getMessageRecordHistoryByUniqueId(uniqueRequestId);
	}

	@Override
	public <T extends MessageExchangeRecordHistory> List<T> getAllMessageRecordHistoryByEventRequestLogId(
			String eventRequestLogId) {
		return communicationTrackingDao.getAllMessageRecordHistoryByEventRequestLogId(eventRequestLogId);
	}

	@Override
	public String getAttachmentStorageIds(String uniqueRequestId) {
		return communicationTrackingDao.getAttachmentStorageIds(uniqueRequestId);
	}

	@Override
	public CommunicationGenerationDetailHistory getCommunicationHistoryByUniqueRequestId(String uniqueRequestId) {
		return communicationTrackingDao.getCommunicationHistoryByUniqueRequestId(uniqueRequestId);
	}

	@Override
	public MessageExchangeRecordHistory getLatestMessageHistoryByEventRequestLogId(String eventRequestLogId) {
		return communicationTrackingDao.getLatestMessageHistoryByEventRequestLogId(eventRequestLogId);
	}

	@Override
	public MessageExchangeRecordHistory getLatestMessageHistoryByParentUniqueRequestId(String uniqueRequestId) {
		return communicationTrackingDao.getLatestMessageHistoryByParentUniqueRequestId(uniqueRequestId);
	}
	
}
