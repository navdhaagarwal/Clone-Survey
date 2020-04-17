package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.List;
import java.util.Map;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.entity.Entity;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGenerationDetailVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGroupCriteriaVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.GeneratedContentVO;
import com.nucleus.mail.entity.MailMessageExchangeRecordHistory;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.message.entity.MessageExchangeRecord;
import com.nucleus.message.entity.ShortMessageRecordHistory;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationGeneratorBusinessObject {
    void updateParameterValuesByParameterMaster(
            List<CommunicationParameter> communicationParameters,
            Map<String, Object> reportDataMap,
            Map<String, String> reportImageMap);

    List<DataPreparationServiceMethodVO> findAdditionalMethodsForCommunicationDataPreperation(
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

    void moveCommunicationEventRequestToHistory(
            CommunicationEventRequestLog communicationEventRequestLog);

    void deleteCommunicationEventRequest(
            CommunicationEventRequestLog communicationEventRequestLog);

    List<CommunicationDataPreparationDetail> getActiveApprovedDetailBasedOnServiceSouceAndModule(
            SourceProduct module, Long serviceSelectionId);

    List<CommunicationRequestDetail> getCommunicationGenerationDetail(
            CommunicationGenerationDetailVO communicationGenerationDetailVO);

    Boolean checkIfRetriedAttempstExhausted(
            CommunicationRequestDetail communicationRequestDetail,
            Map<String, Integer> retryAttemptConfigurations,CommunicationName communicationName);

    void deleteGeneratedCommunicationRequest(
            CommunicationRequestDetail communicationRequestDetail);

    CommunicationGenerationDetailHistory moveCommunicationRequestToHistory(
    		CommunicationGenerationDetailHistory communicationGenerationDetailHistory);

    void moveRequestToHistoryAndDeleteGeneratedRequestInNewTransaction(
            CommunicationRequestDetail communicationRequestDetail, Map<String, Object> additionalDataToBeUpdatedInHistory);

    void prepareDataAndLogError(List<Message> errorMessages,
            CommunicationRequestDetail communicationRequestDetail);

    boolean checkIfCommunicationGenerationAllowed(
            CommunicationRequestDetail communicationRequestDetail,
            Map<String, Object> contextMap,Map<String, Object> localCacheMap);

    GeneratedContentVO generateCommunication(
            CommunicationGroupCriteriaVO communicationGroupCriteriaVO);

    GeneratedContentVO generateAndWriteOrSendCommunication(
            CommunicationGroupCriteriaVO communicationGroupCriteriaVO,
            CommunicationRequestDetail communicationRequestDetail,Map<String, Object> contextMap);

    void updateCommunicationRequestWithApplicableEmailAndPhone(
            CommunicationRequestDetail communicationRequestDetail,
            Map<String, Object> contextMap);

    List<String> getDistinctRequestReferenceId(String communicationCode,
            SourceProduct sourceProduct, Boolean generatedMergeFile);

    long getCommunicationGenerationDetailTotalRecordsSize(
            CommunicationGenerationDetailVO communicationGenerationDetailVO);

    List<CommunicationRequestDetail> getCommunicationGenerationDetail(
            CommunicationGenerationDetailVO communicationGenerationDetailVO,
            int startIndex, int batchSize);

    CommunicationName getCommunicationFromCommunicationCode(
            String communicationCode);



    public void updateCommunicationRequestsAndMoveToHistory(
            List<CommunicationRequestDetail> communicationRequestDetails);

    public CommunicationGenerationDetailHistory updateCommunicationRequestAndMoveToHistory(
            CommunicationRequestDetail communicationRequestDetail,
            String generatedText,CommunicationGroupCriteriaVO communicationGroupCriteriaVO);

    public void updateRetriedAttemptsandLogErrorForCommunicationRequests(
            List<CommunicationRequestDetail> communicationRequestDetails);
    
    void updateCommunicationRequestAndMoveToHistoryInNewTransaction(CommunicationRequestDetail communicationRequestDetail,String generatedText,CommunicationGroupCriteriaVO communicationGroupCriteriaVO);

    public List<CommunicationRequestDetail>getAttachmentsForEmail(long parentId);
    
    public void detach(Entity entity);

	List<Long> getCommunicationGenerationDetailIds(CommunicationGenerationDetailVO communicationGenerationDetailVO,
			int startIndex, int batchSize);
	
    List<Long> getCommunicationGenerationDetailForCommunicationCode(
            CommunicationGenerationDetailVO communicationGenerationDetailVO,
            int startIndex, int batchSize);


    List<CommunicationRequestDetail> getCommunicationGenerationDetailObjForCommunicationCode(
            CommunicationGenerationDetailVO communicationGenerationDetailVO,
            int startIndex, int batchSize);

    
	void moveRequestToHistoryAndDeleteGeneratedRequest(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> additionalDataToBeUpdatedInHistory);

	List<CommunicationRequestDetail> getCommunicationGenerationDetailObjForCommunicationCodeByRefId(
			CommunicationGenerationDetailVO communicationGenerationDetailVO, int i, int batchSize);

	public void updateCommunicationRequest(CommunicationRequestDetail communicationRequestDetail);

	public String[] getAttachmentStorageIdArray(String attachmentStorageIds);

	public void updateCommunicationRequestAndMoveToHistory(CommunicationRequestDetail communicationRequestDetail,
			String status);

	public void createMimeMessageBuilderAndSendMail(MailMessageExchangeRecordHistory mailRecordHistory);

	public void sendSMSFromMessageRecordHistory(ShortMessageRecordHistory shortMessageHistory);

	public void updateCommunicationRequest(CommunicationRequestDetail communicationRequestDetail,
			Character communicationRequestDetailStatus);

	void updateCommunicationRequestAndMoveToHistory(CommunicationRequestDetail communicationRequestDetail,
			String status, boolean isEmailType);

	void moveMessageToHistoryAndDeleteGeneratedMessageRecord(MessageExchangeRecord messageExchangeRecord,
			String status);

	GeneratedContentVO generateCommunicationForPreview(CommunicationGroupCriteriaVO communicationGroupCriteriaVO,
			CommunicationRequestDetail communicationRequestDetail, Map<String, Object> contextMap);
}
