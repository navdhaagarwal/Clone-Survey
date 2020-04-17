package com.nucleus.finnone.pro.communicationgenerator.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventCodeType;
import com.nucleus.core.event.service.EventExecutionService;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.IAdHocEventLogCriteriaBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.IAdHocEventLogCriteriaService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationEventLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.task.ICommunicationEventLogTaskSupervisor;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.eventdefinition.service.EventDefinitionService;
import com.nucleus.rules.model.SourceProduct;

@Service("adHocEventLogCriteriaService")
public class AdHocEventLogCriteriaService implements
  IAdHocEventLogCriteriaService {

 @Inject
 @Named("eventDefinitionService")
 private EventDefinitionService eventDefinitionService;

 @Inject
 @Named("eventExecutionService")
 private EventExecutionService eventExecutionService;

 @Inject
 @Named("adHocEventLogCriteriaBusinessObject")
 private IAdHocEventLogCriteriaBusinessObject adHocEventLogCriteriaBusinessObject;

 @Inject
 @Named("communicationEventLoggerService")
 private ICommunicationEventLoggerService communicationEventLoggerService;

 @Inject
 @Named("communicationEventLogTaskSupervisor")
 private ICommunicationEventLogTaskSupervisor communicationEventLogTaskSupervisor;

 private static final String CONTEXT_OBJECT = "contextObject";
 private static final String EXCEPTION_LOG_COMMUNICATION_EVENT = "Exception in logApplicableCommunicationEvent";

 @Transactional(propagation = Propagation.REQUIRED)
 public Set<String> retrieveRootContextObjectFromEventCode(String eventCode) {
	 // need to work on below return statement. 
	 return eventDefinitionService
			    .getRootContextObjectFromEventCode(eventCode).keySet();
 }

 @Deprecated
 @Transactional(propagation = Propagation.REQUIRED)
 public void logApplicableCommunicationEventBasedOnEventCode(
   EventCode eventCode, SourceProduct module) {
  try {
   if (notNull(eventCode)) {
    Set<String> rootElement = retrieveRootContextObjectFromEventCode(eventCode
      .getCode());
    if (rootElement.size() == 1) {
     List<Object> entities = adHocEventLogCriteriaBusinessObject
       .getEntitiesFromRootContextObject(rootElement
         .iterator().next());
     fireEventForAllApplicableEntitiesAndLogEvent(
       eventCode.getCode(), module, rootElement.iterator()
         .next(), entities);
    }
   }
  } catch (Exception e) {
   BaseLoggers.flowLogger.info(EXCEPTION_LOG_COMMUNICATION_EVENT, e);
  }
 }

 @Transactional(propagation = Propagation.REQUIRED)
 public void fireEventForAllApplicableEntitiesAndLogEvent(String eventCode,
   SourceProduct module, String rootElement, List<Object> entities) {
  for (Object object : entities) {
   Map<String, Object> contextMap = new HashMap<String, Object>();
   contextMap.put(CONTEXT_OBJECT + rootElement, object);
   eventExecutionService.fireEventExecution(eventCode, contextMap,
     null);
   boolean ruleGroupResult = (Boolean) contextMap
     .get("RulegroupResult");
   logApplicableCommunicationEvent(ruleGroupResult, eventCode, object,
     module);
  }
 }

 @Transactional(propagation = Propagation.REQUIRED)
 public void logApplicableCommunicationEvent(boolean ruleResult,
   String eventCode, Object object, SourceProduct module) {
  if (ruleResult) {
   String[] parts = (object.toString()).split("\\:");
   communicationEventLoggerService.logCommunicationEvent(eventCode,
     object.toString(), null, module, null, parts[1], null);
  }
 }

 @Override
 @Transactional(propagation = Propagation.REQUIRED)
 public List<EventCode> getEventCodesBasedOnModule(
   SourceProduct sourceProduct) {
  return adHocEventLogCriteriaBusinessObject
    .getEventCodesBasedOnModule(sourceProduct);
 }

 @Override
 @Transactional(propagation = Propagation.REQUIRED)
 public List<EventCode> getEventCodesBasedOnModuleAndEventCodeType(
   SourceProduct sourceProduct, EventCodeType eventCodeType) {
  return adHocEventLogCriteriaBusinessObject
    .getEventCodesBasedOnModuleAndEventCodeType(sourceProduct,
      eventCodeType);
 }

 @Deprecated
 @Override
 @Transactional(propagation = Propagation.REQUIRES_NEW)
 public void logApplicableCommunicationEventsBasedOnEventCodes(
   List<EventCode> eventCodeList, SourceProduct module) {

  if (CollectionUtils.isNotEmpty(eventCodeList)) {
   for (EventCode eventCode : eventCodeList) {
    logApplicableCommunicationEventBasedOnEventCode(eventCode,
      module);
   }
  }
 }

 @Override
 public List<BaseEntity> fetchObjectsBasedOnBatchSize(int batchCount,
   int batchSize, String rootContextObject) {
  return adHocEventLogCriteriaBusinessObject
    .fetchEntitiesBasedOnBatchSize(batchCount, batchSize,
      rootContextObject);
 }

 @Override
 public int fetchTotalRecordSize(String rootContextObject) {
  return adHocEventLogCriteriaBusinessObject
    .fetchTotalRecordSize(rootContextObject);
 }

 @Override
 @Transactional(propagation = Propagation.REQUIRED)
 public void logApplicableCommunicationEventBasedOnEventCodesInBatch(
   EventCode eventCode, SourceProduct sourceProduct,
   String requestReferenceId, Boolean generateMergedFile) {
  try {
   if (notNull(eventCode)) {
    Set<String> rootContextObject = retrieveRootContextObjectFromEventCode(eventCode
      .getCode());
    if (rootContextObject.size() == 1) {
     callCommunicationEventLogTaskSupervisor(
       eventCode.getCode(), sourceProduct,
       rootContextObject.iterator().next(),
       requestReferenceId, generateMergedFile);
    } else {
     BaseLoggers.flowLogger
       .error("Multiple root context object available "
         + rootContextObject
         + ". not executing AdhocSchedular for Event Code "
         + eventCode);
    }
   }
  } catch (Exception e) {
   BaseLoggers.flowLogger.info(EXCEPTION_LOG_COMMUNICATION_EVENT, e);
  }
 }

 public void callCommunicationEventLogTaskSupervisor(String eventCode,
   SourceProduct module, String rootContextObject,
   String requestReferenceId, Boolean generateMergedFile) {
  communicationEventLogTaskSupervisor.submitObjectsToLogEvents(eventCode,
    module, rootContextObject, requestReferenceId,
    generateMergedFile);
 }

 @Override
 @Transactional(propagation = Propagation.REQUIRES_NEW)
 public void logApplicableCommunicationEventsBasedOnEventCodesInBatch(
   List<EventCode> eventCodeList, SourceProduct module,
   Boolean generateMergedFile) {

  String requestReferenceId = UUID.randomUUID().toString();
  if (CollectionUtils.isNotEmpty(eventCodeList)) {
   for (EventCode eventCode : eventCodeList) {
    logApplicableCommunicationEventBasedOnEventCodesInBatch(
      eventCode, module, requestReferenceId,
      generateMergedFile);
   }
  }
 }

	@Override
	public String fetchNumberOfDuplicateSchedulersOfAdhocCommunication(
			String schedulerName, SourceProduct sourceProduct,Long id,String uuid) {
		return adHocEventLogCriteriaBusinessObject.fetchNumberOfDuplicateSchedulersOfAdhocCommunication(schedulerName, sourceProduct,id,uuid);
	}
}
