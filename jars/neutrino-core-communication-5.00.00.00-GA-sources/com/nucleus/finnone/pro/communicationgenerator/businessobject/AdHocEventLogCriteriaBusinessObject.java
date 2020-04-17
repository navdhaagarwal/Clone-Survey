package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventCodeType;
import com.nucleus.core.event.service.EventExecutionService;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.communicationgenerator.dao.IAdHocEventLogCriteriaDAO;
import com.nucleus.finnone.pro.communicationgenerator.util.ServiceSelectionCriteria;
import com.nucleus.finnone.pro.communicationgenerator.vo.RequestVO;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;

@Named("adHocEventLogCriteriaBusinessObject")
public class AdHocEventLogCriteriaBusinessObject implements
        IAdHocEventLogCriteriaBusinessObject {

    @Inject
    @Named("adHocEventLogCriteriaDAO")
    private IAdHocEventLogCriteriaDAO adHocEventLogCriteriaDAO;

    @Inject
    @Named("eventExecutionService")
    private EventExecutionService eventExecutionService;

    @Inject
    @Named("communicationEventLoggerBusinessObject")
    private ICommunicationEventLoggerBusinessObject communicationEventLoggerBusinessObject;

    @Inject
    @Named("communicationDataPreparationBusinessObject")
    private ICommunicationDataPreparationBusinessObject communicationDataPreparationBusinessObject;

    private static final String CONTEXT_OBJECT = "contextObject";

    private static final String APPLICABLE_EVENT_CODE = "eventCode";

    @Override
    public List<Object> getEntitiesFromRootContextObject(String rootElement) {
        return adHocEventLogCriteriaDAO
                .getEntitiesFromRootContextObject(rootElement);
    }

    @Override
    public List<EventCode> getEventCodesBasedOnModule(
            SourceProduct sourceProduct) {
        return adHocEventLogCriteriaDAO
                .getEventCodesBasedOnModule(sourceProduct);
    }

    @Override
    public List<EventCode> getEventCodesBasedOnModuleAndEventCodeType(
            SourceProduct sourceProduct, EventCodeType eventCodeType) {
        return adHocEventLogCriteriaDAO
                .getEventCodesBasedOnModuleAndEventCodeType(sourceProduct,
                        eventCodeType);
    }

    @Override
    public List<BaseEntity> fetchEntitiesBasedOnBatchSize(int batchCount,
            int batchSize, String rootContextObject) {
        return adHocEventLogCriteriaDAO.fetchEntitiesBasedOnBatchSize(
                batchCount, batchSize, rootContextObject);
    }

    @Override
    public int fetchTotalRecordSize(String rootContextObject) {
        return adHocEventLogCriteriaDAO.fetchTotalRecordSize(rootContextObject);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void fireEventForAllApplicableEntitiesAndLogEvent(String eventCode,
            SourceProduct module, String rootContextObject,
            BaseEntity batchObject, String requestReferenceId,
            Boolean generateMergedFile) {
        Map<String, Object> contextMap = new HashMap<String, Object>();
        contextMap.put(CONTEXT_OBJECT + rootContextObject, batchObject);
        contextMap.put(APPLICABLE_EVENT_CODE, eventCode);
        Map<String, Object> localCacheMap = new HashMap<String, Object>();
        communicationDataPreparationBusinessObject
                .prepareContextMapForTemplateSelectionOrCommunicationGenerationOrAdhocAndBulk(
                        module, contextMap, localCacheMap,
                        ServiceSelectionCriteria.ADHOC_BULK_COMMUNICATION);
        eventExecutionService.fireEventExecution(eventCode, contextMap, null);
        boolean ruleGroupResult = (Boolean) contextMap.get("RulegroupResult");
        logApplicableCommunicationEvent(ruleGroupResult, eventCode,
                batchObject, module, requestReferenceId, generateMergedFile);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void logApplicableCommunicationEvent(boolean ruleResult,
            String eventCode, BaseEntity batchObject, SourceProduct module,
            String requestReferenceId, Boolean generateMergedFile) {
        if (ruleResult) {
            RequestVO requestVO = new RequestVO();
            requestVO.setEventCode(eventCode);
            requestVO.setGenerateMergedFile(generateMergedFile);
            requestVO.setSourceProduct(module);
            requestVO.setSubjectURI(batchObject.getUri());
            requestVO.setSubjectReferenceNumber(batchObject.getId().toString());
            requestVO.setRequestReferenceId(requestReferenceId);
            requestVO.setAdditionalData(null);
            requestVO.setApplicablePrimaryEntityURI(null);
            requestVO.setReferenceDate(null);

            communicationEventLoggerBusinessObject
                    .logCommunicationEvent(requestVO);
        }
        BaseLoggers.flowLogger.info("Applicable communication for" + eventCode
                + " and " + batchObject.getUri() + " was " + ruleResult);

    }
	
	@Override
	public String fetchNumberOfDuplicateSchedulersOfAdhocCommunication(
			String schedulerName, SourceProduct sourceProduct,Long id,String uuid) {
		 return adHocEventLogCriteriaDAO.fetchNumberOfDuplicateSchedulersOfAdhocCommunication(schedulerName, sourceProduct,id,uuid);
	}
	
}
