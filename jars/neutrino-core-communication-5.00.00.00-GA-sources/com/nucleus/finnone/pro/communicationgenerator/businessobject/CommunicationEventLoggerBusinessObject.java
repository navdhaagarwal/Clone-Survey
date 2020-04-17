package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STATUS_INITIATED;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationLocalCacheConstants.PRIMARY_EMAIL_ADDRESS;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationLocalCacheConstants.CC_EMAIL_ADDRESS;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationLocalCacheConstants.BCC_EMAIL_ADDRESS;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationLocalCacheConstants.PRIMARY_PHONE_NUMBER;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.NeutrinoUUIDGenerator;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventExecutionResult;
import com.nucleus.core.event.service.EventExecutionService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationEventLoggerDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationAttachment;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationCommonService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationErrorLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventLoggerHelper;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationConstants;
import com.nucleus.finnone.pro.communicationgenerator.util.ServiceSelectionCriteria;
import com.nucleus.finnone.pro.communicationgenerator.vo.OnDemandCommunicationRequestDetailVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.RequestVO;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationEventLoggerBusinessObject")
public class CommunicationEventLoggerBusinessObject implements
        ICommunicationEventLoggerBusinessObject {

    @Inject
    @Named("communicationEventLoggerDAO")
    ICommunicationEventLoggerDAO communicationEventLoggerDAO;

    @Inject
    @Named("communicationEventLoggerHelper")
    private CommunicationEventLoggerHelper communicationEventLoggerHelper;

    @Inject
    @Named("communicationDataPreparationBusinessObject")
    private ICommunicationDataPreparationBusinessObject communicationDataPreparationBusinessObject;

    @Inject
    @Named("communicationCommonService")
    private ICommunicationCommonService communicationCommonService;

    @Inject
    @Named(value = "eventExecutionService")
    private EventExecutionService eventExecutionService;

    @Inject
    @Named("communicationGenerationHelper")
    private CommunicationGenerationHelper communicationGenerationHelper;

    @Inject
    @Named("communicationGeneratorBusinessObject")
    private ICommunicationGeneratorBusinessObject communicationGeneratorBusinessObject;

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    @Inject
    @Named("communicationErrorLoggerService")
    private ICommunicationErrorLoggerService communicationErrorLoggerService;
    
    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Inject
    @Named("communicationCacheService")
    private ICommunicationCacheService communicationCacheService;
    
	private final NeutrinoUUIDGenerator uuidGenerator = new NeutrinoUUIDGenerator(ProductInformationLoader.getProductCode() + "-");
    
    private static final String CONTEXT_OBJECT_COMMUNICATION_TEMPLATE_MAP = "contextObjectCommunicationTemplateMap";

    private static final String CONTEXT_OBJECT_ADDITIONALDATA = "contextObjectAdditionalData";


    @Override
    public CommunicationRequestDetail createCommunicationGenerationDetail(
            CommunicationRequestDetail communicationGenerationDetail) {

        return communicationEventLoggerDAO
                .createCommunicationGenerationDetail(communicationGenerationDetail);
    }

    @Override
    public CommunicationRequestDetail updateCommunicationGenerationDetail(
            CommunicationRequestDetail communicationRequestDetail) {
        return communicationEventLoggerDAO
                .updateCommunicationGenerationDetail(communicationRequestDetail);
    }

    @Override
    public CommunicationEventRequestLog createCommunicationEventRequest(
            CommunicationEventRequestLog communicationEventRequestLog) {
        return communicationEventLoggerDAO
                .createCommunicationEventRequest(communicationEventRequestLog);
    }

    @Override
    public List<CommunicationEventRequestLog> searchCommEventsBasedOnCriteria(
            CommunicationEventRequestLog communicationEventRequestLog) {
        return communicationEventLoggerDAO
                .searchCommEventsBasedOnCriteria(communicationEventRequestLog);
    }
    
    @Override
    public List<CommunicationEventRequestLog> searchCommEventsBasedOnCriteria(
            CommunicationEventRequestLog communicationEventRequestLog, String criteria) {
        return communicationEventLoggerDAO
                .searchCommEventsBasedOnCriteria(communicationEventRequestLog, criteria);
    }
    
    @SuppressWarnings("unchecked")
	@Override
    @Transactional
    public Map<CommunicationTemplate, List<CommunicationTemplate>> fetchCommunicationTemplatesBasedOnRuleExecution(
            CommunicationEventRequestLog communicationEventRequestLog,
            Boolean isOnDemandGeneration, String communicationCode,
            Map<String, Object> localCacheMapForTemplate) {
		Map<CommunicationTemplate, List<CommunicationTemplate>> generatedMappedTemplates = new HashMap<>();
		Map<String, Object> contextMap = new HashMap<>();
        communicationGenerationHelper.addEntityInContextMap(
                contextMap, localCacheMapForTemplate, communicationEventRequestLog.getSubjectURI(), "subjectURI");
        communicationGenerationHelper.addEntityInContextMap(
                contextMap, localCacheMapForTemplate, communicationEventRequestLog.getApplicablePrimaryEntityURI(), "applicablePrimaryEntityURI");
        contextMap.put(CONTEXT_OBJECT_COMMUNICATION_TEMPLATE_MAP,
                new HashMap<Object, Object>());
        contextMap.put(CONTEXT_OBJECT_ADDITIONALDATA,
                communicationEventRequestLog.getAdditionalData());
        // Call to product processor service
        SourceProduct module = communicationEventRequestLog.getSourceProduct();
        contextMap.put("subjectReferenceNumber",
                communicationEventRequestLog.getSubjectReferenceNumber());
        contextMap.put("subjectReferenceType",
                communicationEventRequestLog.getSubjectReferenceType());
        contextMap = communicationDataPreparationBusinessObject
                .prepareContextMapForTemplateSelectionOrCommunicationGenerationOrAdhocAndBulk(
                        module, contextMap, localCacheMapForTemplate,
                        ServiceSelectionCriteria.TEMPLATE_SELECTION);
        EventExecutionResult eventExecutionResult = eventExecutionService.fireEventExecution(
                        communicationEventRequestLog.getEventCode(),
                        contextMap, null);
        if (notNull(eventExecutionResult)) {
            Map<Object, Object> templateMap = (Map<Object, Object>) contextMap
                    .get(CONTEXT_OBJECT_COMMUNICATION_TEMPLATE_MAP);
            processTemplateMap(communicationEventRequestLog,templateMap,generatedMappedTemplates,isOnDemandGeneration,communicationCode);
      }
        return generatedMappedTemplates;
    }

    private void processTemplateMap(CommunicationEventRequestLog communicationEventRequestLog,
    		Map<Object, Object> templateMap,
    		Map<CommunicationTemplate, List<CommunicationTemplate>> generatedMappedTemplates, boolean isOnDemand,
    		String communicationCode) {
    	if (isOnDemand && notNull(templateMap.get(communicationCode))) {
    		CommunicationTemplate parentTemplate = communicationCacheService
    				.getCommunicationTemplate((Long) templateMap.get(communicationCode));
    		mapParentAndChildTemplates(communicationEventRequestLog, templateMap, generatedMappedTemplates,
    				communicationCode, isOnDemand, parentTemplate);
    		return;
    	}
    	if (isOnDemand && ValidatorUtils.isNull(templateMap.get(communicationCode))) {
    		return;
    	}
    	for (Map.Entry<Object, Object> mapItem : templateMap.entrySet()) {
    		String key = mapItem.getKey().toString();
    		Object parentTemplateId = mapItem.getValue();
    		if (isAttachmentKey(key) || parentTemplateId == null) {	
    			if (parentTemplateId == null) {
    				logErrorOrExceptionWhileProcessingTemplateMap(false,null,"Wrong communication code " + key + " is mapped in object graph");
    			}
    			continue;
    		}
    		CommunicationTemplate parentTemplate = communicationCommonService.findById((Long)parentTemplateId, CommunicationTemplate.class);
    		mapParentAndChildTemplates(communicationEventRequestLog,templateMap,generatedMappedTemplates,parentTemplate.getCommunication().getCommunicationCode(),isOnDemand,parentTemplate);
    	}
    }
    
    private void mapParentAndChildTemplates(CommunicationEventRequestLog communicationEventRequestLog, 
    		Map<Object, Object> templateMap, Map<CommunicationTemplate, List<CommunicationTemplate>> generatedMappedTemplates,
    		String communicationCode, boolean isOnDemand, CommunicationTemplate parentTemplate) {
    	if (parentTemplate == null) {
    		Message message = new Message(
    				CommunicationGeneratorConstants.NO_TEMPLATE,
    				Message.MessageType.ERROR,
    				genericParameterService.findByCode(communicationEventRequestLog.getEventCode(), EventCode.class).getDescription(),
    				communicationGeneratorBusinessObject.getCommunicationFromCommunicationCode(communicationCode).getCommunicationName());

    		logErrorOrExceptionWhileProcessingTemplateMap(isOnDemand, message, null);
    		return;
    	}    
    	generatedMappedTemplates.put(parentTemplate, null);
    	if (ValidatorUtils.hasNoElements(parentTemplate.getCommunication().getAttachments())) {
    		return;
    	}
    	StringBuilder attachmentKey = new StringBuilder();
    	for (CommunicationAttachment attachment : parentTemplate.getCommunication().getAttachments()) {
    		attachmentKey.append(communicationCode);
    		attachmentKey.append(CommunicationConstants.ATTACHMENT_SEPARATOR);
    		attachmentKey.append(attachment.getAttachedCommunication().getCommunicationCode());
    		String attachmentKeyStr=attachmentKey.toString();
    		if (templateMap.containsKey(attachmentKeyStr)) {
    			CommunicationTemplate attachmentTemplate = communicationCommonService.findById((Long)templateMap.get(attachmentKeyStr), CommunicationTemplate.class);
    			updateTemplateMap(generatedMappedTemplates,parentTemplate,attachmentTemplate);
    		} else {
    			generatedMappedTemplates.remove(parentTemplate);
    			Message message = new Message(
    					CommunicationGeneratorConstants.NO_ATTACHED_TEMPLATE,
    					Message.MessageType.ERROR,attachment.getAttachedCommunication().getCommunicationCode(),
    					genericParameterService.findByCode(communicationEventRequestLog.getEventCode(), EventCode.class).getDescription(),
    					communicationGeneratorBusinessObject.getCommunicationFromCommunicationCode(communicationCode).getCommunicationName());
    			logErrorOrExceptionWhileProcessingTemplateMap(isOnDemand, message, "Attached Template not mapped for "+ communicationCode);
    		}
    		attachmentKey.delete(0, attachmentKey.length());
    	}
    }
      
    private boolean isAttachmentKey(String key) {
         int indexOfSeparetor = key.indexOf(CommunicationConstants.ATTACHMENT_SEPARATOR);
         return indexOfSeparetor > 0;
	}

	private void logErrorOrExceptionWhileProcessingTemplateMap(boolean isOnDemand,Message exceptionMessage,String message) {
    	if (isOnDemand) {
    		  throw ExceptionBuilder
              .getInstance(BusinessException.class)
              .setMessage(exceptionMessage)
              .setSeverity(
                      ExceptionSeverityEnum.SEVERITY_MEDIUM
                              .getEnumValue()).build();
    	}
    	BaseLoggers.flowLogger.error(message);	
    }
  
    private void updateTemplateMap(Map<CommunicationTemplate, List<CommunicationTemplate>>generatedMappedTemplates,CommunicationTemplate parent,CommunicationTemplate attachment) {
        if (generatedMappedTemplates.containsKey(parent)&&ValidatorUtils.hasElements(generatedMappedTemplates.get(parent))) {
            generatedMappedTemplates.get(parent).add(attachment);
        } else {
            List<CommunicationTemplate> attachments = new ArrayList<>();
            attachments.add(attachment);
            generatedMappedTemplates.put(parent, attachments);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<CommunicationRequestDetail> logCommunicationRequest(
            Map<CommunicationTemplate, List<CommunicationTemplate>> communicationTemplatesMap,
            CommunicationEventRequestLog communicationEventRequestLog, Map<String, Object> localCacheMapForTemplate) {
        List<CommunicationRequestDetail> eligibleCommunicationRequestDetails = new ArrayList<>();
        for (Map.Entry<CommunicationTemplate,List<CommunicationTemplate>> communicationTemplatesEntry:communicationTemplatesMap.entrySet()) {
             prepareAndLogCommunicationRequestDetailIncludingAttached(eligibleCommunicationRequestDetails,communicationTemplatesEntry.getKey(),communicationTemplatesEntry.getValue(),communicationEventRequestLog,localCacheMapForTemplate);
        }
        return eligibleCommunicationRequestDetails;
    }

    private void prepareAndLogCommunicationRequestDetailIncludingAttached(
            List<CommunicationRequestDetail> eligibleCommunicationRequestDetails,
            CommunicationTemplate mainTemplate,
            List<CommunicationTemplate> attachedTemplates,CommunicationEventRequestLog communicationEventRequestLog, Map<String, Object> localCacheMapForTemplate) {
        
        CommunicationRequestDetail communicationRequestDetailMain =prepareAndLogCommunicationRequestDetail(mainTemplate,communicationEventRequestLog,localCacheMapForTemplate); 
        communicationRequestDetailMain=createCommunicationGenerationDetail(communicationRequestDetailMain);
        eligibleCommunicationRequestDetails.add(communicationRequestDetailMain);
        if (ValidatorUtils.hasNoElements(attachedTemplates)) {
        	return;
        }
        for (CommunicationTemplate communicationTemplate : attachedTemplates) {
            CommunicationRequestDetail communicationRequestDetailAttached =prepareAndLogCommunicationRequestDetail(communicationTemplate,communicationEventRequestLog,localCacheMapForTemplate);
            communicationRequestDetailAttached.setParentCommunicationRequestDetail(communicationRequestDetailMain);
            communicationRequestDetailAttached.setParentCommunicationRequestDetailId(communicationRequestDetailMain.getId());
            communicationRequestDetailAttached=createCommunicationGenerationDetail(communicationRequestDetailAttached);
            eligibleCommunicationRequestDetails.add(communicationRequestDetailAttached);
        }
        
        
    }

    private CommunicationRequestDetail prepareAndLogCommunicationRequestDetail(CommunicationTemplate communicationTemplate,CommunicationEventRequestLog communicationEventRequestLog, Map<String, Object> localCacheMapForTemplate) {    
    	String phoneNumber = null;
    	String primaryEmail = null;
    	String ccEmail = null;
    	String bccEmail = null;
    	if (localCacheMapForTemplate != null && localCacheMapForTemplate.get(PRIMARY_EMAIL_ADDRESS) != null) {
    		primaryEmail = (String)localCacheMapForTemplate.get(PRIMARY_EMAIL_ADDRESS);
    	}
    	if (localCacheMapForTemplate!=null && localCacheMapForTemplate.get(PRIMARY_PHONE_NUMBER) != null) {
    		phoneNumber = (String)localCacheMapForTemplate.get(PRIMARY_PHONE_NUMBER);
    	}
    	if (localCacheMapForTemplate != null && localCacheMapForTemplate.get(CC_EMAIL_ADDRESS) != null) {
    		ccEmail = (String)localCacheMapForTemplate.get(CC_EMAIL_ADDRESS);
    	}
    	if (localCacheMapForTemplate != null && localCacheMapForTemplate.get(BCC_EMAIL_ADDRESS) != null) {
    		bccEmail = (String)localCacheMapForTemplate.get(BCC_EMAIL_ADDRESS);
    	}
		CommunicationRequestDetail communicationRequestDetail = communicationEventLoggerHelper
				.prepareCommunicationRequestDetailObject(communicationEventRequestLog.getSubjectURI(),
                communicationEventRequestLog.getApplicablePrimaryEntityURI(),
                communicationEventRequestLog.getEventCode(),
                communicationTemplate, phoneNumber, primaryEmail, ccEmail, bccEmail,
                communicationEventRequestLog.getAdditionalData(),
                communicationEventRequestLog.getSourceProduct(),
                communicationEventRequestLog.getSubjectReferenceNumber(),
                communicationEventRequestLog.getSubjectReferenceType(),
                communicationEventRequestLog.getEntityLifeCycleData().getCreationTimeStamp(),
                communicationEventRequestLog.getReferenceDate());
    	
    	if(BooleanUtils.isTrue(communicationEventRequestLog.getPreviewFlag())) {
    		communicationRequestDetail.setStatus(CommunicationGeneratorConstants.STATUS_PREVIEW);
    		communicationRequestDetail.setPreviewFlag(communicationEventRequestLog.getPreviewFlag());
    	}
    	
    	if(ValidatorUtils.notNull(communicationEventRequestLog.getDeliveryPriority())) {
    		communicationRequestDetail.setDeliveryPriority(communicationEventRequestLog.getDeliveryPriority());
    	}
    	
    	if(ValidatorUtils.notNull(communicationEventRequestLog.getRequestType())) {
    		communicationRequestDetail.setRequestType(communicationEventRequestLog.getRequestType());
    	}
    	
		if (ValidatorUtils.notNull(communicationEventRequestLog.getEventRequestLogId())) {
			communicationRequestDetail.setEventRequestLogId(communicationEventRequestLog.getEventRequestLogId());
		}
        communicationRequestDetail.setRequestReferenceId(communicationEventRequestLog.getRequestReferenceId());
        communicationRequestDetail.setGenerateMergedFile(communicationEventRequestLog.getGenerateMergedFile()); 
        communicationRequestDetail.setJsonAdditionalField1(communicationEventRequestLog.getJsonAdditionalField1());
        communicationRequestDetail.setJsonAdditionalField2(communicationEventRequestLog.getJsonAdditionalField2());
        communicationRequestDetail.setJsonAdditionalField3(communicationEventRequestLog.getJsonAdditionalField3());
        communicationRequestDetail.getEntityLifeCycleData().setCreatedByUri(communicationEventRequestLog.getEntityLifeCycleData().getCreatedByUri());
        return communicationRequestDetail;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void markCommEventRequestCompleteAndMoveToHistory(
            CommunicationEventRequestLog communicationEventRequestLog) {
    	CommunicationEventRequestLog updatedCommEventRequestLog = markCommEventRequestComplete(communicationEventRequestLog);
        moveCommunicationEventRequestToHistory(updatedCommEventRequestLog);
        deleteCommunicationEventRequest(updatedCommEventRequestLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void markCommEventRequestCompleteAndMoveToHistoryInNewTransaction(
            CommunicationEventRequestLog communicationEventRequestLog) {
        markCommEventRequestCompleteAndMoveToHistory(communicationEventRequestLog);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CommunicationEventRequestLog markCommEventRequestComplete(
            CommunicationEventRequestLog communicationEventRequestLog) {
        return communicationGeneratorBusinessObject
                .markCommEventRequestComplete(communicationEventRequestLog);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void moveCommunicationEventRequestToHistory(
            CommunicationEventRequestLog communicationEventRequestLog) {
        communicationGeneratorBusinessObject
                .moveCommunicationEventRequestToHistory(communicationEventRequestLog);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteCommunicationEventRequest(
            CommunicationEventRequestLog communicationEventRequestLog) {
        communicationGeneratorBusinessObject
                .deleteCommunicationEventRequest(communicationEventRequestLog);
    }

    @Override
    public List<CommunicationErrorLogDetail> prepareErrorLogData(
            CommunicationEventRequestLog communicationEventRequestLog,
            String communicationCode, List<Message> errorMessages) {
        List<CommunicationErrorLogDetail> communicationErrorLogDetails = new ArrayList<>();
        Locale locale = configurationService.getSystemLocale();
        CommunicationErrorLogDetail communicationErrorLogDetail = null;
        for (Message errorMessage : errorMessages) {
            communicationErrorLogDetail = new CommunicationErrorLogDetail();
            String messageDescription = communicationGenerationHelper
                    .getMessageDescription(errorMessage, locale);
            communicationErrorLogDetail.setErrorDescription(messageDescription);
            communicationErrorLogDetail.setErrorMessageId(errorMessage
                    .getI18nCode());
            if (errorMessage.getMessageArguments() != null) {
                communicationErrorLogDetail
                        .setErrorMessageparameters(errorMessage
                                .getMessageArgumentsString(errorMessage
                                        .getMessageArguments()));
            }
            communicationErrorLogDetail.setErrorType(1L);
            communicationErrorLogDetail
                    .setApplicablePrimaryEntityUri(communicationEventRequestLog
                            .getApplicablePrimaryEntityURI());
            if (communicationEventRequestLog.getApplicablePrimaryEntityURI() != null)
                communicationErrorLogDetail
                        .setApplicablePrimaryEntityID(EntityId.fromUri(
                                communicationEventRequestLog
                                        .getApplicablePrimaryEntityURI())
                                .getLocalId());
            communicationErrorLogDetail
                    .setSubjectUri(communicationEventRequestLog.getSubjectURI());
            communicationErrorLogDetail.setSubjectId(EntityId.fromUri(
                    communicationEventRequestLog.getSubjectURI()).getLocalId());
            communicationErrorLogDetail
                    .setSubjectReferenceNumber(communicationEventRequestLog
                            .getSubjectReferenceNumber());
            communicationErrorLogDetail
            		.setSubjectReferenceType(communicationEventRequestLog
            				.getSubjectReferenceType());
            communicationErrorLogDetail
                    .setCommunicationEventCode(communicationEventRequestLog
                            .getEventCode());
            communicationErrorLogDetail.setCommunicationCode(communicationCode);
            communicationErrorLogDetail.setSubjectType('L');
            communicationErrorLogDetail
                    .setReferenceDate(communicationEventRequestLog
                            .getReferenceDate());
            communicationErrorLogDetails.add(communicationErrorLogDetail);
        }
        return communicationErrorLogDetails;
    }

    
    
    @Transactional(propagation=Propagation.REQUIRED)
    public List<CommunicationRequestDetail> moveEventRequestLifecycleToNextStage(
            Map<CommunicationTemplate, List<CommunicationTemplate>> communicationTemplateMap,
            CommunicationEventRequestLog communicationEventRequestLog,
            Boolean isOnDemandGeneration, String communicationCode, Map<String, Object> localCacheMapForTemplate) {
        List<CommunicationRequestDetail> communicationRequestDetails=logCommunicationForEvent( communicationTemplateMap,
                 communicationEventRequestLog,
                isOnDemandGeneration,  communicationCode,localCacheMapForTemplate);
        markCommEventRequestCompleteAndMoveToHistory(communicationEventRequestLog);
        return communicationRequestDetails;
    }
    
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public List<CommunicationRequestDetail> moveEventRequestLifecycleToNextStageInNewTransaction(
             Map<CommunicationTemplate, List<CommunicationTemplate>> communicationTemplateMap,
            CommunicationEventRequestLog communicationEventRequestLog,
            Boolean isOnDemandGeneration, String communicationCode) {
       
        return moveEventRequestLifecycleToNextStage(communicationTemplateMap,communicationEventRequestLog,isOnDemandGeneration,communicationCode,null);
    }
    
	@Transactional(propagation = Propagation.REQUIRED)
	public List<CommunicationRequestDetail> moveEventRequestLifecycleToNextStage(
			Map<String, Map<CommunicationTemplate, List<CommunicationTemplate>>> communicationTemplateMap,
			CommunicationEventRequestLog communicationEventRequestLog, Boolean isOnDemandGeneration,
			Map<String, Object> localCacheMapForTemplate) {
		List<CommunicationRequestDetail> communicationRequestDetails = new ArrayList<>();
		if (communicationTemplateMap != null && !communicationTemplateMap.isEmpty()) {
			for (Map.Entry<String, Map<CommunicationTemplate, List<CommunicationTemplate>>> entry : communicationTemplateMap
					.entrySet()) {
				communicationRequestDetails.addAll(logCommunicationForEvent(entry.getValue(), communicationEventRequestLog,
						isOnDemandGeneration, entry.getKey(), localCacheMapForTemplate));
			}
		}
		markCommEventRequestCompleteAndMoveToHistory(communicationEventRequestLog);
		return communicationRequestDetails;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<CommunicationRequestDetail> moveEventRequestLifecycleToNextStageInNewTransaction(
			Map<String, Map<CommunicationTemplate, List<CommunicationTemplate>>> communicationTemplateMap,
			CommunicationEventRequestLog communicationEventRequestLog, Boolean isOnDemandGeneration) {

		return moveEventRequestLifecycleToNextStage(communicationTemplateMap, communicationEventRequestLog,
				isOnDemandGeneration, null);
	}
    
    @Transactional
    public List<CommunicationRequestDetail> logCommunicationForEvent(
            Map<CommunicationTemplate, List<CommunicationTemplate>> communicationTemplateMap,
            CommunicationEventRequestLog communicationEventRequestLog,
            Boolean isOnDemandGeneration, String communicationCode, Map<String, Object> localCacheMapForTemplate) {
        List<CommunicationRequestDetail> eligibleCommunicationRequestDetails = new ArrayList<>();

        if (ValidatorUtils.hasAnyEntry(communicationTemplateMap)) {
			eligibleCommunicationRequestDetails = logCommunicationRequest(
                    communicationTemplateMap, communicationEventRequestLog,localCacheMapForTemplate);

        } else if (isOnDemandGeneration) {
            Message message = new Message(
                    CommunicationGeneratorConstants.NO_TEMPLATE,
                    Message.MessageType.ERROR,
                    genericParameterService.findByCode(communicationEventRequestLog.getEventCode(), EventCode.class).getDescription(),
                    communicationGeneratorBusinessObject.getCommunicationFromCommunicationCode(communicationCode).getCommunicationName());
            throw ExceptionBuilder
                    .getInstance(BusinessException.class)
                    .setMessage(message)
                    .setSubjectReferenceNumber(
                            communicationEventRequestLog
                                    .getSubjectReferenceNumber())
                    .setSeverity(
                            ExceptionSeverityEnum.SEVERITY_MEDIUM
                                    .getEnumValue()).build();

        } else {
            List<Message> errorMessages = new ArrayList<>();
            errorMessages.add(CoreUtility.prepareMessage(
                    CommunicationGeneratorConstants.NO_TEMPLATE_BULK,
                    Message.MessageType.ERROR,
                    communicationEventRequestLog.getEventCode()));
            List<CommunicationErrorLogDetail> communicationErrorLogDetail = prepareErrorLogData(
                    communicationEventRequestLog, communicationCode,
                    errorMessages);
            communicationErrorLoggerService
                    .createCommunicationProcessErrorLoggerDetail(communicationErrorLogDetail);
        }
        return eligibleCommunicationRequestDetails;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void logCommunicationEvent(String eventCode, String subjectURI,
            AdditionalData additionalData, SourceProduct module,
            String applicablePrimaryEntityURI, String subjectReferenceNumber,
            Date referenceDate) {

        RequestVO requestVO = new RequestVO();
        requestVO.setEventCode(eventCode);
        requestVO.setSourceProduct(module);
        requestVO.setSubjectURI(subjectURI);
        requestVO.setSubjectReferenceNumber(subjectReferenceNumber);
        requestVO.setAdditionalData(additionalData);
        requestVO.setApplicablePrimaryEntityURI(applicablePrimaryEntityURI);
        requestVO.setReferenceDate(referenceDate);
        requestVO.setStatus(STATUS_INITIATED);
        CommunicationEventRequestLog communicationEventRequestLog = communicationGenerationHelper
                .prepareCommEventRequestData(requestVO);
        createCommunicationEventRequest(communicationEventRequestLog);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CommunicationRequestDetail setCommunicationGenerationDetailFromOnDemandVO(
            OnDemandCommunicationRequestDetailVO onDemandCommunicationRequestDetailVO) {
    	return createAndSaveCommunicationRequestDetail(onDemandCommunicationRequestDetailVO, false, null);
    }

    private CommunicationRequestDetail createAndSaveCommunicationRequestDetail(
			OnDemandCommunicationRequestDetailVO crdVo, boolean isAttachmentLetter, Character status) {
		CommunicationRequestDetail crd = new CommunicationRequestDetail();
		//No communicationEventRequestLog is logged in this case so we need to add a different uniqueId that will be used for tracking the service.
		crd.setEventRequestLogId(uuidGenerator.generateUuid());
		crd.setAdditionalData(crdVo.getAdditionalData());
		if (notNull(crdVo.getAdditionalData()) && !isAttachmentLetter) {
			persistAdditionalData(crdVo.getAdditionalData());
			crd.setAdditionalFieldTxnId(crdVo.getAdditionalData().getId());
		}
		crd.setAlternatePhoneNumber(crdVo.getAlternatePhoneNumber());
		crd.setApplicablePrimaryEntityId(crdVo.getApplicablePrimaryEntityId());
		crd.setApplicablePrimaryEntityURI(crdVo.getApplicablePrimaryEntityURI());
		crd.setCommunicationCode(crdVo.getCommunicationCode());
		crd.setCommunicationEventCode(crdVo.getEventCode());
		crd.setCommunicationTemplateCode(crdVo.getCommunicationTemplateCode());
		crd.setCommunicationText(crdVo.getCommunicationText());
		crd.setEventLogTimeStamp(crdVo.getEventLogTimeStamp());
		crd.setIssueReissueFlag(crdVo.getIssueReissueFlag());
		crd.setProcessDate(crdVo.getProcessDate());
		crd.setReferenceDate(crdVo.getReferenceDate());
		crd.setRegenerationReasonCode(crdVo.getRegenerationReasonCode());
		crd.setRetriedAttemptsDone(crdVo.getRetriedAttemptsDone());
		crd.setSourceProduct(crdVo.getSourceProduct());
		crd.setStatus(status == null ? crdVo.getStatus() : status);
		crd.setSubjectId(crdVo.getSubjectId());
		crd.setSubjectReferenceNumber(crdVo.getSubjectReferenceNumber());
		crd.setSubjectReferenceType(crdVo.getSubjectReferenceType());
		crd.setSubjectURI(crdVo.getSubjectURI());
		if (!isAttachmentLetter) {
			crd.setCommunicationTemplate(crdVo.getCommunicationTemplate());
			crd.setCommunicationTemplateId(crdVo.getCommunicationTemplate().getId());
			//Email or SMS communication type specific data.
			crd.setPrimaryEmailAddress(crdVo.getPrimaryEmailAddress());
			crd.setCcEmailAddress(crdVo.getCcEmailAddress());
			crd.setBccEmailAddress(crdVo.getBccEmailAddress());
			crd.setPhoneNumber(crdVo.getPrimaryPhoneNumber());
			communicationEventLoggerDAO.createCommunicationGenerationDetail(crd);
		}
		if (!isAttachmentLetter && crdVo.getAttachedCommunicationTemplates() != null ) {
			for (CommunicationTemplate communicationTemplate : crdVo.getAttachedCommunicationTemplates()) {
				CommunicationRequestDetail attachedCRD = createAndSaveCommunicationRequestDetail(crdVo, true, null);
				if (notNull(crdVo.getAdditionalData())) {
					AdditionalData additionalData = new AdditionalData(crdVo.getAdditionalData());
					persistAdditionalData(additionalData);
					attachedCRD.setAdditionalFieldTxnId(additionalData.getId());
				}
				attachedCRD.setCommunicationTemplate(communicationTemplate);
				attachedCRD.setCommunicationTemplateId(communicationTemplate.getId());
				attachedCRD.setParentCommunicationRequestDetail(crd);
				attachedCRD.setParentCommunicationRequestDetailId(crd.getId());
				communicationEventLoggerDAO.createCommunicationGenerationDetail(crd);
			}
		}
		return crd;
	}

	@Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void logCommunicationEvent(RequestVO requestVO) {
        requestVO.setStatus(STATUS_INITIATED);
        CommunicationEventRequestLog communicationEventRequestLog = communicationGenerationHelper
                .prepareCommEventRequestData(requestVO);
        createCommunicationEventRequest(communicationEventRequestLog);
    }

    public void detach(Entity entity) {
    	communicationGeneratorBusinessObject.detach(entity);
    }

    @Override
	public void persistAdditionalData(AdditionalData additionalData) {
    	if (additionalData.getId() == null) {
			//save the additional data first.
    		communicationEventLoggerDAO.persist(additionalData);
		}
	}

	@Override
	public CommunicationRequestDetail setCommunicationGenerationDetailFromPreviewOnDemandVO(
			OnDemandCommunicationRequestDetailVO onDemandCommunicationRequestDetailVO) {
		return createAndSaveCommunicationRequestDetail(onDemandCommunicationRequestDetailVO, false,
				CommunicationGeneratorConstants.STATUS_PREVIEW);
	}
}