package com.nucleus.finnone.pro.communicationgenerator.service;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STATUS_INITIATED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.CommunicationEventLoggerBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationDataPreparationBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationCategory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationCriteriaType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestBase;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventTemplateMapping;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationCommonService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationConstants;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.finnone.pro.communicationgenerator.util.ServiceSelectionCriteria;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.rules.service.CompiledExpressionBuilder;
import com.nucleus.rules.service.RuleConstants;

@Named("communicationEventLoggerDelegate")
public class CommunicationEventLoggerDelegate implements ICommunicationEventLoggerDelegate{

	
	
	@Inject
	@Named("communicationGeneratorService")
	private CommunicationGeneratorService communicationGeneratorService;

	@Inject
	@Named("communicationEventLoggerBusinessObject")
	private CommunicationEventLoggerBusinessObject communicationEventLoggerBusinessObject;
	
	@Inject
	@Named("communicationGenerationHelper")
	private CommunicationGenerationHelper communicationGenerationHelper;
	
    @Inject
    @Named("communicationCommonService")
    private ICommunicationCommonService communicationCommonService;
	
    @Inject
    @Named("communicationDataPreparationBusinessObject")
    private ICommunicationDataPreparationBusinessObject communicationDataPreparationBusinessObject;
	
    @Inject
    @Named("compiledExpressionBuilder")
    private CompiledExpressionBuilder compiledExpressionBuilder;

	@Inject
	@Named("communicationEventMappingService")
	private CommunicationEventMappingService communicationEventMappingService;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<CommunicationRequestDetail> selectTemplateAndGenerateCommReqDtlsForBulk(Boolean isOnDemandGeneration,
			String eventCode, SourceProduct module, String subjectURI, String applicablePrimaryEntityURI,
			Long deliveryPriority, String subjectReferenceNumber, List<CommunicationName> communicationNameList) {
		List<CommunicationRequestDetail> communicationRequestDetails = new ArrayList<>();
		CommunicationEventMappingHeader commEventMapping = fetchCommEventMapForEventAndModule(false, eventCode, module);

		/* Return in case Event is Immediate in case of Bulk/Batch processing. */
		if (commEventMapping == null || CommunicationCategory.COMM_CATEGORY_IMMEDIATE
				.equals(commEventMapping.getCommunicationCategory().getCode())) {
			return communicationRequestDetails;
		}
		String commCriteria = communicationEventMappingService
				.getCommunicationCriteriaType(commEventMapping.getSourceProductId(), commEventMapping.getEventCodeId());

		if (CommunicationCriteriaType.RULE_CRITERIA.equals(commCriteria)) {
			/*
			 * Criteria Type as RULE_CRITERIA for batch processing -> Using Rule Based
			 * Approach
			 */
			commReqLogBulkProcessForRuleCriteria(eventCode, module, subjectURI, applicablePrimaryEntityURI,
					communicationRequestDetails, commEventMapping, deliveryPriority);
		} else {
			/*
			 * Criteria Type as QUERY_CRITERIA or BOTH_CRITERIA for batch processing -> Using
			 * Query Based Approach
			 */
			commReqLogBulkProcessForQueryCriteria(isOnDemandGeneration, eventCode, module, subjectURI,
					applicablePrimaryEntityURI, deliveryPriority, subjectReferenceNumber, communicationNameList,
					communicationRequestDetails, commEventMapping);
		}
		return communicationRequestDetails;
	}

	private void commReqLogBulkProcessForQueryCriteria(Boolean isOnDemandGeneration, String eventCode,
			SourceProduct module, String subjectURI, String applicablePrimaryEntityURI, Long deliveryPriority,
			String subjectReferenceNumber, List<CommunicationName> communicationNameList,
			List<CommunicationRequestDetail> communicationRequestDetails,
			CommunicationEventMappingHeader commEventMapping) {
		List<CommunicationEventMappingDetail> commEvtMapDtls = commEventMapping
				.getCommunicationEvtMapDtls(deliveryPriority);

		if (CollectionUtils.isNotEmpty(commEvtMapDtls)) {
			CommunicationEventRequestLog communicationEventRequestLog = new CommunicationEventRequestLog.CommunicationEventSearchBuilder()
					.setEventCode(eventCode).setSourceProduct(module).setStatus(STATUS_INITIATED)
					.getSubjectReferenceNumber(subjectReferenceNumber).setSubjectURI(subjectURI)
					.setApplicablePrimaryEntityURI(applicablePrimaryEntityURI).build();
			Map<CommunicationEventTemplateMapping, List<CommunicationEventRequestLog>> eventTemplateReqLogMap = new HashMap<>();
			for (CommunicationEventMappingDetail commEvtMapDtl : commEvtMapDtls) {
				eventTemplateReqLogMap
						.putAll(generateEventTemplateCommReqLogMap(communicationEventRequestLog, commEvtMapDtl));

			}
			selectTemplateAndCreateCommReqDtlForBulkQueryCriteria(isOnDemandGeneration, communicationNameList,
					communicationEventRequestLog, eventTemplateReqLogMap, communicationRequestDetails);
		}
	}


	private void commReqLogBulkProcessForRuleCriteria(String eventCode, SourceProduct module, String subjectURI,
			String applicablePrimaryEntityURI, List<CommunicationRequestDetail> communicationRequestDetails,
			CommunicationEventMappingHeader commEventMapping, Long deliveryPriority) {
		List<CommunicationEventRequestLog> communicationEventRequestLogs = null;
		CommunicationEventRequestLog communicationEventRequestLog = new CommunicationEventRequestLog.CommunicationEventSearchBuilder()
				.setEventCode(eventCode).setSourceProduct(module).setStatus(STATUS_INITIATED).setSubjectURI(subjectURI)
				.setApplicablePrimaryEntityURI(applicablePrimaryEntityURI).build();
		communicationEventRequestLogs = communicationEventLoggerBusinessObject
				.searchCommEventsBasedOnCriteria(communicationEventRequestLog);
		if (CollectionUtils.isEmpty(communicationEventRequestLogs)) {
			return;
		}
		Map<String, Object> localCacheMapForTemplate = new HashMap<>();
		List<CommunicationEventMappingDetail> commEvtMapDtls = commEventMapping
				.getCommunicationEvtMapDtls(deliveryPriority);
		for (CommunicationEventRequestLog commEventReqLog : communicationEventRequestLogs) {
			commEventReqLog.setRequestType(CommunicationConstants.BULK);
			communicationRequestDetails.addAll(selectTemplateAndCreateCommReqDtlForBulkRuleCriteria(commEvtMapDtls,
					eventCode, module, null, localCacheMapForTemplate, commEventReqLog));
		}
	}

	private CommunicationEventMappingHeader fetchCommEventMapForEventAndModule(Boolean isOnDemandGeneration,
			String eventCode, SourceProduct module) {
		CommunicationEventMappingHeader commEventMapping = communicationEventMappingService
				.getCommunicationEventMapping(eventCode, module);
		if (commEventMapping == null) {
			Message message = new Message("msg.8000017", Message.MessageType.ERROR,
					module.getName(), eventCode);
			communicationGenerationHelper.logAndThrowException(isOnDemandGeneration, message, module.getName(),
					eventCode);
		}
		return commEventMapping;
	}
	
	private void selectTemplateAndCreateCommReqDtlForBulkQueryCriteria(Boolean isOnDemandGeneration,
			List<CommunicationName> communicationNameList, CommunicationEventRequestLog communicationEventRequestLog,
			Map<CommunicationEventTemplateMapping, List<CommunicationEventRequestLog>> eventTemplateReqLogMap,
			List<CommunicationRequestDetail> communicationRequestDetails) {
		Map<CommunicationEventRequestLog, List<Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping>>> evtRqtTemplateMap = inverseEventTemplateReqLogMap(
				communicationNameList, eventTemplateReqLogMap);
		if (!evtRqtTemplateMap.isEmpty()) {
			for (Map.Entry<CommunicationEventRequestLog, List<Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping>>> entry : evtRqtTemplateMap
					.entrySet()) {
				CommunicationEventRequestLog evtRequestLog = entry.getKey();
				evtRequestLog.setRequestType(CommunicationConstants.BULK);
				processCommEvtTemplateMapping(false, evtRequestLog,
						entry.getValue().stream().flatMap(item -> item.entrySet().stream())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
						communicationRequestDetails, null);
			}
		}
	}

	private Map<CommunicationEventRequestLog, List<Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping>>> inverseEventTemplateReqLogMap(
			List<CommunicationName> communicationNameList,
			Map<CommunicationEventTemplateMapping, List<CommunicationEventRequestLog>> eventTemplateReqLogMap) {
		Map<CommunicationEventRequestLog, List<Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping>>> evtRqtTemplateMap = new HashMap<>();
		if (eventTemplateReqLogMap != null && !eventTemplateReqLogMap.isEmpty()) {
			for (Map.Entry<CommunicationEventTemplateMapping, List<CommunicationEventRequestLog>> entry : eventTemplateReqLogMap
					.entrySet()) {
				Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping> map = new HashMap<>();
				map.put(entry.getKey().getCommunicationEventMappingDetail(), entry.getKey());
				List<CommunicationEventRequestLog> list = entry.getValue();
				for (CommunicationEventRequestLog obj : list) {
					if (evtRqtTemplateMap.containsKey(obj)) {
						evtRqtTemplateMap.get(obj).add(map);
					} else {
						List<Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping>> dtlMapList = new ArrayList<>();
						dtlMapList.add(map);
						evtRqtTemplateMap.put(obj, dtlMapList);
					}
				}
				communicationNameList.add(entry.getKey().getCommunicationEventMappingDetail().getCommunicationName());
			}
		}
		return evtRqtTemplateMap;
	}

	private Map<String, Map<CommunicationTemplate, List<CommunicationTemplate>>> generateCommTemplateMapAndSetDeliveryPriority(
			Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping> map,
			CommunicationEventRequestBase communicationEventRequestLog) {
		Map<String, Map<CommunicationTemplate, List<CommunicationTemplate>>> commCodeTemplateMap = new HashMap<>();
		for (Map.Entry<CommunicationEventMappingDetail, CommunicationEventTemplateMapping> entry : map.entrySet()) {
			CommunicationEventTemplateMapping commEvtMapping = entry.getValue();
			Map<CommunicationTemplate, List<CommunicationTemplate>> generatedCommnTemplateMap = new HashMap<>();
			if (commEvtMapping.getAttachmentIds() == null || commEvtMapping.getAttachmentIds().size() == 0) {
				generatedCommnTemplateMap.put(commEvtMapping.getCommunicationTemplate(), null);
			} else {
				List<CommunicationTemplate> attachmentTemplates = new ArrayList<>();
				for (Long attachmentId : commEvtMapping.getAttachmentIds()) {
					attachmentTemplates
							.add(communicationCommonService.findById(attachmentId, CommunicationTemplate.class));
				}
				generatedCommnTemplateMap.put(commEvtMapping.getCommunicationTemplate(), attachmentTemplates);
			}
			commCodeTemplateMap.put(entry.getKey().getCommunicationName().getCommunicationCode(),
					generatedCommnTemplateMap);
			communicationEventRequestLog.setDeliveryPriority(entry.getKey().getPriority());
		}

		return commCodeTemplateMap;
	}

	private Map<CommunicationEventTemplateMapping, List<CommunicationEventRequestLog>> generateEventTemplateCommReqLogMap(
			CommunicationEventRequestLog communicationEventRequestLog, CommunicationEventMappingDetail commEvtMapDtl) {
		List<CommunicationEventRequestLog> commEvtReqLogTemporaryList = new ArrayList<>();
		Map<CommunicationEventTemplateMapping, List<CommunicationEventRequestLog>> eventTemplateReqLogMap = new HashMap<>();
		//Hibernate.initialize(commEvtMapDtl.getCommunicationEventTemplateMappings());
		List<CommunicationEventTemplateMapping> commEvtTemplateMappings = commEvtMapDtl
				.getCommunicationEventTemplateMappings();
		for (CommunicationEventTemplateMapping commEvtTemplateMapping : commEvtTemplateMappings) {
			List<CommunicationEventRequestLog> commEventRequestLogs = searchCommEventsBasedOnCriteria(
					communicationEventRequestLog, commEvtTemplateMapping.getDecodedCriteria());
			if (CollectionUtils.isNotEmpty(commEventRequestLogs)) {
				commEventRequestLogs.removeAll(commEvtReqLogTemporaryList);
				eventTemplateReqLogMap.put(commEvtTemplateMapping, commEventRequestLogs);
				commEvtReqLogTemporaryList.addAll(commEventRequestLogs);
			}
		}
		return eventTemplateReqLogMap;
	}
	
	private List<CommunicationEventRequestLog> searchCommEventsBasedOnCriteria(
			CommunicationEventRequestLog communicationEventRequestLog, String decodedCriteria) {
		return communicationEventLoggerBusinessObject.searchCommEventsBasedOnCriteria(communicationEventRequestLog,
				decodedCriteria);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<CommunicationRequestDetail> selectTemplateAndGenerateCommForOnDemandReq(Boolean isOnDemandGeneration,
			String eventCode, SourceProduct module, String communicationCode,
			Map<String, Object> localCacheMapForTemplate, CommunicationEventRequestLog commEventRequestLog) {
		List<CommunicationRequestDetail> communicationRequestDetails = new ArrayList<>();
		CommunicationEventMappingHeader commEventMapping = fetchCommEventMapForEventAndModule(isOnDemandGeneration,
				eventCode, module);
		//On-Demand Generation should be allowed only for RULE_CRITERIA and BOTH_CRITERIA.
		validateCriteriaType(isOnDemandGeneration, eventCode, commEventMapping);
		CommunicationEventMappingDetail commEvtMapDtl = getCommEvtMapDetail(commEventMapping, communicationCode);
		if (commEvtMapDtl != null) {
			CommunicationEventTemplateMapping commEvtTempMapping = null;
			Map<String, Object> contextMap = createContextMap(module, localCacheMapForTemplate, commEventRequestLog);
			commEvtTempMapping = executeRuleBasedTemplateSearch(module, localCacheMapForTemplate, commEventRequestLog,
					commEvtMapDtl, contextMap);
			Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping> map = null;
			if (commEvtTempMapping != null) {
				map = new HashMap<>();
				map.put(commEvtTempMapping.getCommunicationEventMappingDetail(), commEvtTempMapping);
			}
			processCommEvtTemplateMapping(isOnDemandGeneration, commEventRequestLog, map, communicationRequestDetails,
					localCacheMapForTemplate);
		}
		return communicationRequestDetails;
	}

	private List<CommunicationRequestDetail> selectTemplateAndCreateCommReqDtlForBulkRuleCriteria(
			List<CommunicationEventMappingDetail> commEvtMapDtls, String eventCode, SourceProduct module,
			String communicationCode, Map<String, Object> localCacheMapForTemplate,
			CommunicationEventRequestLog commEventRequestLog) {
		List<CommunicationRequestDetail> communicationRequestDetails = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(commEvtMapDtls)) {
			Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping> map = new HashMap<>();
			Map<String, Object> contextMap = createContextMap(module, localCacheMapForTemplate, commEventRequestLog);
			for (CommunicationEventMappingDetail commEvtMapDtlObj : commEvtMapDtls) {
				CommunicationEventTemplateMapping commEvtTempMapping = executeRuleBasedTemplateSearch(module,
						localCacheMapForTemplate, commEventRequestLog, commEvtMapDtlObj, contextMap);
				if (commEvtTempMapping != null) {
					map.put(commEvtMapDtlObj, commEvtTempMapping);
				}
			}
			if (!map.isEmpty()) {
				processCommEvtTemplateMapping(false, commEventRequestLog, map, communicationRequestDetails,
						localCacheMapForTemplate);
			}
		}
		return communicationRequestDetails;
	}

	private void validateCriteriaType(Boolean isOnDemandGeneration, String eventCode,
			CommunicationEventMappingHeader commEventMapping) {
		String communicationCriteria = communicationEventMappingService
				.getCommunicationCriteriaType(commEventMapping.getSourceProductId(), commEventMapping.getEventCodeId());
		if (CommunicationCriteriaType.QUERY_CRITERIA.equals(communicationCriteria)) {
			Message message = new Message("msg.8000018", Message.MessageType.ERROR, eventCode,
					communicationCriteria);
			communicationGenerationHelper.logAndThrowException(isOnDemandGeneration, message);
		}
	}

	// Note: This method to be used for Immediate Communication Generation and
	// Fallback for Immediate only.
	private List<CommunicationRequestDetail> selectTemplateAndCreateCommReqDtlForImmediateReq(
			Boolean isOnDemandGeneration, SourceProduct module, CommunicationEventMappingHeader commEventMapping,
			Map<String, Object> localCacheMapForTemplate, CommunicationEventRequestLog commEventRequestLog,
			Long deilveryPriority) {
		List<CommunicationEventMappingDetail> commEvtMapDtls = commEventMapping
				.getCommunicationEvtMapDtls(deilveryPriority);
		List<CommunicationRequestDetail> communicationRequestDetails = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(commEvtMapDtls)) {
			Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping> map = new HashMap<>();
			Map<String, Object> contextMap = createContextMap(module, localCacheMapForTemplate, commEventRequestLog);
			for (CommunicationEventMappingDetail commEvtMapDtlObj : commEvtMapDtls) {
				CommunicationEventTemplateMapping commEvtTempMapping = executeRuleBasedTemplateSearch(module,
						localCacheMapForTemplate, commEventRequestLog, commEvtMapDtlObj, contextMap);
				if (commEvtTempMapping != null) {
					map.put(commEvtMapDtlObj, commEvtTempMapping);
				}
			}
			if (!map.isEmpty()) {
				processCommEvtTemplateMapping(isOnDemandGeneration, commEventRequestLog, map,
						communicationRequestDetails, localCacheMapForTemplate);
			}
		}
		return communicationRequestDetails;
	}


	private CommunicationEventTemplateMapping executeRuleBasedTemplateSearch(SourceProduct module,
			Map<String, Object> localCacheMapForTemplate, CommunicationEventRequestLog commEventRequestLog,
			CommunicationEventMappingDetail commEvtMapDtlObj,
			Map<String, Object> contextMap) {
		List<CommunicationEventTemplateMapping> commEvtTempMappings = commEvtMapDtlObj
				.getCommunicationEventTemplateMappings();
		if (CollectionUtils.isNotEmpty(commEvtTempMappings)) {
			return executeRuleAndGetCommEvtTempMap(module, localCacheMapForTemplate, commEventRequestLog,
					commEvtTempMappings, contextMap);
		}
		return null;
	}

	private void processCommEvtTemplateMapping(Boolean isOnDemandGeneration,
			CommunicationEventRequestLog commEventRequestLog,
			Map<CommunicationEventMappingDetail, CommunicationEventTemplateMapping> map,
			List<CommunicationRequestDetail> communicationRequestDetails,
			Map<String, Object> localCacheMapForTemplate) {
		if (map == null || map.isEmpty()) {
			String subjectReferenceNumber = commEventRequestLog.getSubjectReferenceNumber();
			String eventCode = commEventRequestLog.getEventCode();
			Message message = new Message(CommunicationConstants.ERROR_COMM_TEMPL_MAP_EMPTY, MessageType.ERROR,
					subjectReferenceNumber, eventCode);
			communicationGenerationHelper.logAndThrowException(isOnDemandGeneration, message, subjectReferenceNumber,
					eventCode);
		}
		Map<String, Map<CommunicationTemplate, List<CommunicationTemplate>>> commCodeGeneratedTemplateMap = generateCommTemplateMapAndSetDeliveryPriority(
				map, commEventRequestLog);
		if (isOnDemandGeneration) {
			communicationRequestDetails.addAll(communicationEventLoggerBusinessObject
					.moveEventRequestLifecycleToNextStage(commCodeGeneratedTemplateMap, commEventRequestLog,
							isOnDemandGeneration, localCacheMapForTemplate));
		} else {
			communicationRequestDetails
					.addAll(communicationEventLoggerBusinessObject.moveEventRequestLifecycleToNextStageInNewTransaction(
							commCodeGeneratedTemplateMap, commEventRequestLog, isOnDemandGeneration));
		}
	}
	
	private CommunicationEventTemplateMapping executeRuleAndGetCommEvtTempMap(SourceProduct module,
			Map<String, Object> localCacheMapForTemplate, CommunicationEventRequestLog commEventRequestLog,
			List<CommunicationEventTemplateMapping> commEvtTempMappings, Map<String, Object> contextMap) {
		for (CommunicationEventTemplateMapping commEvtTempMapping : commEvtTempMappings) {
			char result = compiledExpressionBuilder.evaluateRule(commEvtTempMapping.getRuleId(), contextMap);
			if (result == RuleConstants.RULE_RESULT_PASS) {
				return commEvtTempMapping;
			}
		}
		return null;
	}

	private Map<String, Object> createContextMap(SourceProduct module, Map<String, Object> localCacheMapForTemplate,
			CommunicationEventRequestLog commEventRequestLog) {
		Map<String, Object> contextMap = new HashMap<>();
		communicationGenerationHelper.addEntityInContextMap(contextMap, localCacheMapForTemplate,
				commEventRequestLog.getSubjectURI(), "subjectURI");
		communicationGenerationHelper.addEntityInContextMap(contextMap, localCacheMapForTemplate,
				commEventRequestLog.getApplicablePrimaryEntityURI(), "applicablePrimaryEntityURI");
		contextMap.put("contextObjectCommunicationTemplateMap", new HashMap<Object, Object>());
		contextMap.put("contextObjectAdditionalData", commEventRequestLog.getAdditionalData());
		// Call to product processor service
		contextMap.put("subjectReferenceNumber", commEventRequestLog.getSubjectReferenceNumber());
		contextMap.put("subjectReferenceType", commEventRequestLog.getSubjectReferenceType());
		contextMap = communicationDataPreparationBusinessObject
				.prepareContextMapForTemplateSelectionOrCommunicationGenerationOrAdhocAndBulk(module, contextMap,
						localCacheMapForTemplate, ServiceSelectionCriteria.TEMPLATE_SELECTION);
		return contextMap;
	}

	private CommunicationEventMappingDetail getCommEvtMapDetail(CommunicationEventMappingHeader commEventMapping,
			String communicationCode) {
		List<CommunicationEventMappingDetail> commEvtMapDtls = commEventMapping.getCommunicationEventMappingDetails();
		if (CollectionUtils.isNotEmpty(commEvtMapDtls)) {
			for (CommunicationEventMappingDetail commEvtMapDtl : commEvtMapDtls) {
				if (StringUtils.equals(commEvtMapDtl.getCommunicationName().getCommunicationCode(),
						communicationCode)) {
					return commEvtMapDtl;
				}
			}
		}
		BaseLoggers.exceptionLogger.error(
				"Business Exception while fetching CommunicationEventMappingDetail for On-Demand Communication Generation.");
		throw ExceptionBuilder
				.getInstance(BusinessException.class, CommunicationGeneratorConstants.COMMUNICATION_GENERATION_ERROR,
						"Error while fetching CommunicationEventMappingDetail")
				.setMessage(CommunicationGeneratorConstants.COMMUNICATION_GENERATION_ERROR).build();
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void selectTemplateAndGenerateCommForImmediateReq(CommunicationEventRequestLog commEventRequestLog) {
		CommunicationEventMappingHeader commEventMapping = communicationEventMappingService
				.getCommunicationEventMapping(commEventRequestLog.getEventCode(),
						commEventRequestLog.getSourceProduct());
		if (commEventMapping == null || !CommunicationCategory.COMM_CATEGORY_IMMEDIATE
				.equals(commEventMapping.getCommunicationCategory().getCode())) {
			return;
		}
		String communicationCriteriaType = communicationEventMappingService
				.getCommunicationCriteriaType(commEventMapping.getSourceProductId(), commEventMapping.getEventCodeId());
		// NA if Criteria Type is QUERY_CRITERIA for IMMEDIATE Communication Category
		if (CommunicationCriteriaType.QUERY_CRITERIA.equals(communicationCriteriaType)) {
			return;
		}
		Map<String, Object> localCacheMapForTemplate = new HashMap<>();
		List<CommunicationRequestDetail> communicationRequestDetails = selectTemplateAndCreateCommReqDtlForImmediateReq(
				false, commEventRequestLog.getSourceProduct(), commEventMapping, localCacheMapForTemplate,
				commEventRequestLog, commEventRequestLog.getDeliveryPriority());
		if (CollectionUtils.isNotEmpty(communicationRequestDetails)) {
			communicationGeneratorService.generateCommunicationAndMoveToHistory(communicationRequestDetails,
					new HashMap<String, Object>(), new HashMap<String, List<DataPreparationServiceMethodVO>>(), false);
		}
	}
}