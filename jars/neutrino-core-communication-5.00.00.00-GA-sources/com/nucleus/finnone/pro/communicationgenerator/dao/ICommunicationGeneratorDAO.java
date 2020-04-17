package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.List;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestHistory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGenerationDetailVO;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.message.entity.MessageExchangeRecord;
import com.nucleus.message.entity.MessageExchangeRecordHistory;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationGeneratorDAO extends EntityDao {
    List<CommunicationParameter> findAdditionalMethodsForCommunicationDataPreperation(
            String communicationCode);

    List<CommunicationTemplate> getTemplateByCommunicationMasterId(
            Long communicationMasterId);


    List<Object[]> getAttributeValueForGenericParameter(
            Class<? extends GenericParameter> entityClass, String columnName);

    List<Object[]> getAttributeValueForBaseMasterEntity(
            Class<? extends BaseMasterEntity> entityClass, String columnName,
            String dependentColumn, Long dependentColumnValue);

    CommunicationEventRequestLog markCommEventRequestComplete(
            CommunicationEventRequestLog communicationEventRequestLog);

    void saveCommunicationEventRequestHistory(
            CommunicationEventRequestHistory communicationEventRequestHistory);

    void deleteCommunicationEventRequest(
            CommunicationEventRequestLog communicationEventRequestLog);

    List<CommunicationDataPreparationDetail> getActiveApprovedDetailBasedOnServiceSouceAndModule(
            SourceProduct module, Long serviceSelectionId);

    void deleteGeneratedCommunicationRequest(
            CommunicationRequestDetail communicationRequestDetail);

    List<String> getDistinctRequestReferenceId(String communicationCode,
            SourceProduct sourceProduct, Boolean generateMergedFile);

    int getCommunicationGenerationDetailTotalRecordsSize(
            CommunicationGenerationDetailVO communicationGenerationDetailVO);

    List<CommunicationRequestDetail> getCommunicationGenerationDetail(
            CommunicationGenerationDetailVO communicationGenerationDetailVO,
            int startIndex, int batchSize);

    CommunicationName getCommunicationFromCommunicationCode(
            String communicationCode);

    int getCommunicationGenerationDetailTotalRecordsSizeForMergedFile(
            CommunicationGenerationDetailVO communicationGenerationDetailVO);

    List<CommunicationRequestDetail> getAttachmentsForEmail(long parentId);

	List<Long> getCommunicationGenerationDetailIds(CommunicationGenerationDetailVO communicationGenerationDetailVO,
			int startIndex, int batchSize);
	
	List<Long> getCommunicationGenerationDetailForCommunication(CommunicationGenerationDetailVO communicationGenerationDetailVO,
			int startIndex, int batchSize);
	
	List<CommunicationRequestDetail> getCommunicationGenerationDetailObjForCommunication(CommunicationGenerationDetailVO communicationGenerationDetailVO,
			int startIndex, int batchSize);

	List<CommunicationRequestDetail> getCommunicationGenerationDetailObjForCommunicationByRefId(
			CommunicationGenerationDetailVO communicationGenerationDetailVO, int startIndex, int batchSize);

	void deleteMessageExchangeRecord(MessageExchangeRecord messageExchangeRecord);

	public CommunicationRequestDetail getCommunicationGenerationDetailByUniqueId(String uniqueId);

	public Long getCountOfFailedMessages();
	
	<T extends MessageExchangeRecord> T getMessageExchangeRecordByUniqueId(Class<?> exchangeRecordClass, String uniqueId);
	
	<T extends MessageExchangeRecord> void saveMessageExchangeRecord(T messageExchangeRecord);

	<T extends MessageExchangeRecord> List<T> getUndeliveredMessageExchangeRecord(Class<?> entityClass, Long startId, int batchSize);
	
	<T extends MessageExchangeRecord> void updateMessageExchangeRecord(T messageExchangeRecord);

	MessageExchangeRecordHistory getMessageExchangeRecordHistoryByUniqueId(String uniqueId);

	void updateMessageExchangeRecordHistory(MessageExchangeRecordHistory messageExchnageRecordHistory);
	
}
