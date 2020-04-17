package com.nucleus.finnone.pro.communicationgenerator.service;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.SEMI_COLON;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STATUS_INITIATED;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationLocalCacheConstants.BCC_EMAIL_ADDRESS;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationLocalCacheConstants.CC_EMAIL_ADDRESS;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationLocalCacheConstants.PRIMARY_EMAIL_ADDRESS;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationLocalCacheConstants.PRIMARY_PHONE_NUMBER;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.jadira.usertype.spi.utils.lang.StringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationEventLoggerBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationCategory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationEventLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationConstants;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.finnone.pro.communicationgenerator.vo.GeneratedContentVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.OnDemandCommunicationRequestDetailVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.OnDemandRequestVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.RequestVO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.UserInfo;

@Service("communicationEventLoggerService")
public class CommunicationEventLoggerService implements ICommunicationEventLoggerService {

	@Inject
	@Named("communicationGeneratorService")
	private CommunicationGeneratorService communicationGeneratorService;
	@Inject
	@Named("communicationEventCodeRequestTypeCachePopulator")
	private NeutrinoCachePopulator communicationEventCodeRequestTypeCachePopulator;
	@Inject
	@Named("communicationEventLoggerBusinessObject")
	private ICommunicationEventLoggerBusinessObject communicationEventLoggerBusinessObject;

	@Inject
	@Named("communicationGenerationHelper")
	private CommunicationGenerationHelper communicationGenerationHelper;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Inject
	@Named("communicationEventLoggerDelegate")
	private ICommunicationEventLoggerDelegate communicationEventLoggerDelegate;

	@Inject
	@Named("communicationEventMappingService")
	private CommunicationEventMappingService communicationEventMappingService;

	@Inject
	@Named("communicationEventMappingWorker")
	private TransactionPostCommitWork communicationEventMappingWorker;



	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<CommunicationRequestDetail> createCommunicationGenerationDetail(
			List<CommunicationRequestDetail> communicationRequestDetails) {
		for (CommunicationRequestDetail communicationRequestDetail : communicationRequestDetails) {
			communicationEventLoggerBusinessObject.createCommunicationGenerationDetail(communicationRequestDetail);
		}
		return communicationRequestDetails;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public CommunicationRequestDetail updateCommunicationGenerationDetail(
			CommunicationRequestDetail communicationRequestDetail) {
		return communicationEventLoggerBusinessObject.updateCommunicationGenerationDetail(communicationRequestDetail);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public CommunicationEventRequestLog logCommunicationEvent(RequestVO requestVO) {
		return logCommunicationEvent(requestVO, false);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public CommunicationEventRequestLog logCommunicationEventInNewTransaction(RequestVO requestVO) {
		CommunicationEventRequestLog commEvtReqLog = createCommunicationEventRequest(communicationGenerationHelper
				.prepareCommEventRequestData(requestVO));
		commEvtReqLog.setDeliveryPriority(requestVO.getDeliveryPriority());
		return commEvtReqLog;
	}

	protected CommunicationEventRequestLog logCommunicationEvent(RequestVO requestVO, Boolean isOnDemandGeneration) {
		CommunicationEventRequestLog communicationEventRequestLog = communicationGenerationHelper
				.prepareCommEventRequestData(requestVO);
		CommunicationEventRequestLog commEvtReqLog = createCommunicationEventRequest(communicationEventRequestLog);

		// Communication Generation Flow is triggered in new Thread for IMMEDIATE
		// events. Will not work for On-Demand
		if (!isOnDemandGeneration && isImmediateCommunication(commEvtReqLog.getEventCode())) {
			commEvtReqLog.setRequestType(CommunicationConstants.IMMEDIATE);
			TransactionPostCommitWorker.handlePostCommit(communicationEventMappingWorker, commEvtReqLog, true);
		}
		return commEvtReqLog;
	}

	private boolean isImmediateCommunication(String eventCode) {
		String requestType =(String)communicationEventCodeRequestTypeCachePopulator.get(eventCode);
		return CommunicationCategory.COMM_CATEGORY_IMMEDIATE.equals(requestType);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public CommunicationEventRequestLog logCommunicationEvent(String eventCode, String subjectURI,
			AdditionalData additionalData, SourceProduct module, String applicablePrimaryEntityURI,
			String subjectReferenceNumber, Date referenceDate) {
		RequestVO requestVO = new RequestVO();
		requestVO.setEventCode(eventCode);
		requestVO.setSourceProduct(module);
		requestVO.setSubjectURI(subjectURI);
		requestVO.setSubjectReferenceNumber(subjectReferenceNumber);
		requestVO.setAdditionalData(additionalData);
		requestVO.setApplicablePrimaryEntityURI(applicablePrimaryEntityURI);
		requestVO.setReferenceDate(referenceDate);
		requestVO.setStatus(STATUS_INITIATED);
		return logCommunicationEvent(requestVO);
	}

	
	/* 4July */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public CommunicationEventRequestLog logCommunicationEvent(String eventCode, String subjectURI,
			AdditionalData additionalData, SourceProduct module, String applicablePrimaryEntityURI,
			String subjectReferenceNumber, String subjectReferenceType, Date referenceDate) {
		RequestVO requestVO = new RequestVO();
		requestVO.setEventCode(eventCode);
		requestVO.setSourceProduct(module);
		requestVO.setSubjectURI(subjectURI);
		requestVO.setSubjectReferenceNumber(subjectReferenceNumber);
		requestVO.setSubjectReferenceType(subjectReferenceType);
		requestVO.setAdditionalData(additionalData);
		requestVO.setApplicablePrimaryEntityURI(applicablePrimaryEntityURI);
		if (referenceDate != null) {
			requestVO.setReferenceDate(referenceDate);
		} else {
			requestVO.setReferenceDate(new Date());
		}
		requestVO.setStatus(STATUS_INITIATED);
		return logCommunicationEvent(requestVO);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public CommunicationEventRequestLog logCommunicationEvent(String eventCode, String subjectURI,
			AdditionalData additionalData, SourceProduct module, String applicablePrimaryEntityURI,
			String subjectReferenceNumber) {
		return logCommunicationEvent(eventCode, subjectURI, additionalData, module, applicablePrimaryEntityURI,
				subjectReferenceNumber, new Date());
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public CommunicationEventRequestLog createCommunicationEventRequest(
			CommunicationEventRequestLog communicationEventRequestLog) {
		return communicationEventLoggerBusinessObject.createCommunicationEventRequest(communicationEventRequestLog);
	}

	private List<CommunicationRequestDetail> identifyAndLogCommunicationForEvent(Boolean isOnDemandGeneration,
			String eventCode, String communicationCode, SourceProduct module,
			Map<String, Object> localCacheMapForTemplate, String subjectURI, String applicablePrimaryEntityURI,
			AdditionalData additionalData, Date referenceDate, CommunicationEventRequestLog commEventRequestLog) {
		if (module == null && StringUtils.isEmpty(eventCode)) {
			Message message = new Message("msg.8000019", Message.MessageType.ERROR);
			communicationGenerationHelper.logAndThrowException(isOnDemandGeneration, message);
		}
		return communicationEventLoggerDelegate.selectTemplateAndGenerateCommForOnDemandReq(isOnDemandGeneration,
				eventCode, module, communicationCode, localCacheMapForTemplate, commEventRequestLog);
	}

	private List<CommunicationName> identifyAndLogCommunicationEventForBulk(String eventCode, SourceProduct module,
			String subjectURI, String applicablePrimaryEntityURI, String subjectReferenceNumber,
			Long deliveryPriority) {
		List<CommunicationName> communicationNameList = new ArrayList<>();
		if (module == null && StringUtils.isEmpty(eventCode)) {
			Message message = new Message("msg.8000019", Message.MessageType.ERROR);
			communicationGenerationHelper.logAndThrowException(false, message);
			return communicationNameList;
		}
		communicationEventLoggerDelegate.selectTemplateAndGenerateCommReqDtlsForBulk(false, eventCode, module,
				subjectURI, applicablePrimaryEntityURI, deliveryPriority, subjectReferenceNumber,
				communicationNameList);
		return communicationNameList;
	}

	@Override
	public void generateCommunicationForCallback(RequestVO requestVO,
			CommunicationEventRequestLog communicationEventRequestLog) {
		if (requestVO != null && requestVO.getSourceProduct() == null
				&& StringUtils.isEmpty(requestVO.getEventCode())) {
			Message message = new Message("msg.8000019", Message.MessageType.ERROR);
			communicationGenerationHelper.logAndThrowException(false, message);
			return;
		}
		if (CommunicationConstants.IMMEDIATE.equals(requestVO.getRequestType())) {
			communicationEventLoggerDelegate.selectTemplateAndGenerateCommForImmediateReq(communicationEventRequestLog);
		} else {
			identifyAndLogCommunicationEventForBulk(requestVO.getEventCode(), requestVO.getSourceProduct(),
					requestVO.getSubjectURI(), requestVO.getApplicablePrimaryEntityURI(),
					requestVO.getSubjectReferenceNumber(), requestVO.getDeliveryPriority());
		}
	}

	@Transactional
	public List<CommunicationEventRequestLog> searchCommEventsBasedOnCriteria(
			CommunicationEventRequestLog communicationEventRequestLog) {
		return communicationEventLoggerBusinessObject.searchCommEventsBasedOnCriteria(communicationEventRequestLog);
	}

	@Override
	@Transactional
	public GeneratedContentVO logAndGenerateCommunicationForOnDemand(String eventCode, String communicationCode,
			SourceProduct module, String subjectURI, String applicablePrimaryEntityURI, AdditionalData additionalData,
			String subjectReferenceNumber, Date referenceDate) {
		RequestVO requestVO = new RequestVO();
		requestVO.setEventCode(eventCode);
		requestVO.setSourceProduct(module);
		requestVO.setSubjectURI(subjectURI);
		requestVO.setSubjectReferenceNumber(subjectReferenceNumber);
		requestVO.setAdditionalData(additionalData);
		requestVO.setApplicablePrimaryEntityURI(applicablePrimaryEntityURI);
		requestVO.setReferenceDate(referenceDate);
		requestVO.setStatus(STATUS_INITIATED);
		communicationGenerationHelper.updateRequestVoWithUserUri(requestVO);
		CommunicationEventRequestLog communicationEventRequestLog = logCommunicationEvent(requestVO, true);
		List<CommunicationRequestDetail> communicationRequestDetails = identifyAndLogCommunicationForEvent(true,
				eventCode, communicationCode, module, null, subjectURI, applicablePrimaryEntityURI, additionalData,
				referenceDate, communicationEventRequestLog);
		return communicationGeneratorService.generateCommunicationOnDemand(communicationRequestDetails.get(0));
	}

	@Override
	@Transactional
	public GeneratedContentVO logAndGenerateCommunicationForOnDemand(String eventCode, String communicationCode,
			SourceProduct module, String subjectURI, String applicablePrimaryEntityURI, AdditionalData additionalData,
			String subjectReferenceNumber) {
		return logAndGenerateCommunicationForOnDemand(eventCode, communicationCode, module, subjectURI,
				applicablePrimaryEntityURI, additionalData, subjectReferenceNumber, new Date());
	}

	@Transactional
	protected Map<CommunicationTemplate, List<CommunicationTemplate>> identifyCommunicationTemplateForEvent(
			Boolean isOnDemandGeneration, String communicationCode, Map<String, Object> localCacheMapForTemplate,
			CommunicationEventRequestLog communicationEventRequestLog) {
		return communicationEventLoggerBusinessObject.fetchCommunicationTemplatesBasedOnRuleExecution(
				communicationEventRequestLog, isOnDemandGeneration, communicationCode, localCacheMapForTemplate);
	}

	@Override
	public void logAndGenerateCommRequestsForLoggedEvents(List<EventCode> eventCodeList, SourceProduct module) {
		logAndGenerateCommRequestsForLoggedEvents(eventCodeList, module, null);

	}

	@Override
	public void logAndGenerateCommRequestsForLoggedEvents(List<EventCode> eventCodeList, SourceProduct module,
			Map<Object, Object> parameters) {
		try {
			logCommRequestsForLoggedEvents(eventCodeList, module);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error("Exception in Communication Event Request Schedular " + e);
		}

		Map<Object, Object> paramMap = new HashMap<>();
		if (parameters != null) {
			paramMap.put(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID,
					parameters.get(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID));
		}
		// Generating Communications for CRDs for Given communication Code having
		// INITIATED status For Bulk Requests
		communicationGeneratorService.logAndGenerateCommunicationsForCommunicationRequests(module, paramMap);

	}

	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	private void logCommRequestsForLoggedEvents(List<EventCode> eventCodeList, 
			SourceProduct module) {
		if (CollectionUtils.isNotEmpty(eventCodeList)) {
			for (EventCode eventCode : eventCodeList) {
				identifyAndLogCommunicationEventForBulk(eventCode.getCode(), module, null, null, null, null);
			}
		}
	}

	@Override
	@Transactional
	public GeneratedContentVO logAndGenerateCommunicationForOnDemand(OnDemandRequestVO onDemandRequestVO) {

		RequestVO requestVO = createRequestVO(onDemandRequestVO);
		Map<String, Object> localCacheMap = createLocalCacheMap(onDemandRequestVO, requestVO);
		requestVO.setStatus(STATUS_INITIATED);
		CommunicationEventRequestLog communicationEventRequestLog = logCommunicationEvent(requestVO, true);
		communicationEventRequestLog.setRequestType(CommunicationConstants.ON_DEMAND);
		List<CommunicationRequestDetail> communicationRequestDetails = identifyAndLogCommunicationForEvent(true,
				onDemandRequestVO.getEventCode(), onDemandRequestVO.getCommunicationCode(),
				onDemandRequestVO.getSourceProduct(), localCacheMap, onDemandRequestVO.getSubjectURI(),
				onDemandRequestVO.getApplicablePrimaryEntityURI(), onDemandRequestVO.getAdditionalData(),
				onDemandRequestVO.getReferenceDate(), communicationEventRequestLog);
		for (CommunicationRequestDetail communicationRequestDetail : communicationRequestDetails) {
			communicationRequestDetail.setIssueReissueFlag(onDemandRequestVO.getIssueReissueFlag());
			communicationRequestDetail.setRegenerationReasonCode(onDemandRequestVO.getRegenerationReasonCode());
		}
		if (onDemandRequestVO.getOnDemandAttachments() != null) {
			communicationRequestDetails.get(0).setOnDemandAttachments(onDemandRequestVO.getOnDemandAttachments());
		} else if (ValidatorUtils.hasElements(onDemandRequestVO.getFilePaths())) {
			// below code helps to store file paths in db.
			communicationRequestDetails.get(0).setAttachmentFilePaths(onDemandRequestVO.getFilePaths().stream()
					.filter(filePath -> !filePath.isEmpty()).collect(Collectors.joining(SEMI_COLON)));
		}
		communicationRequestDetails.get(0).setSkipStorageForLetter(onDemandRequestVO.isSkipStorageForLetter());
		return communicationGeneratorService.generateCommunicationOnDemand(communicationRequestDetails.get(0),
				onDemandRequestVO.getInitializedData(), onDemandRequestVO.isReturnGeneratedLetterContentOnly());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public GeneratedContentVO logAndGenerateCommunicationByTemplateForOnDemand(
			OnDemandCommunicationRequestDetailVO onDemandCommunicationRequestDetailVO) {
		CommunicationRequestDetail communicationRequestDetail = createCommReqDtlForCommByTemplate(
				onDemandCommunicationRequestDetailVO);
		return communicationGeneratorService.generateCommunicationOnDemand(communicationRequestDetail,
				onDemandCommunicationRequestDetailVO.getInitializedData(),
				onDemandCommunicationRequestDetailVO.isReturnGeneratedLetterContentOnly());
	}

	@Override
	@Transactional
	public GeneratedContentVO logAndGenerateCommunicationForOnDemandPreview(OnDemandRequestVO onDemandRequestVO) {

		RequestVO requestVO = createRequestVO(onDemandRequestVO);
		Map<String, Object> localCacheMap = createLocalCacheMap(onDemandRequestVO, requestVO);
		requestVO.setStatus(CommunicationGeneratorConstants.STATUS_PREVIEW);
		requestVO.setPreviewFlag(Boolean.TRUE);
		CommunicationEventRequestLog communicationEventRequestLog = logCommunicationEvent(requestVO, true);
		communicationEventRequestLog.setRequestType(CommunicationConstants.ON_DEMAND);
		List<CommunicationRequestDetail> communicationRequestDetails = identifyAndLogCommunicationForEvent(true,
				onDemandRequestVO.getEventCode(), onDemandRequestVO.getCommunicationCode(),
				onDemandRequestVO.getSourceProduct(), localCacheMap, onDemandRequestVO.getSubjectURI(),
				onDemandRequestVO.getApplicablePrimaryEntityURI(), onDemandRequestVO.getAdditionalData(),
				onDemandRequestVO.getReferenceDate(), communicationEventRequestLog);
		for (CommunicationRequestDetail communicationRequestDetail : communicationRequestDetails) {
			communicationRequestDetail.setIssueReissueFlag(onDemandRequestVO.getIssueReissueFlag());
			communicationRequestDetail.setRegenerationReasonCode(onDemandRequestVO.getRegenerationReasonCode());
		}
		if (onDemandRequestVO.getOnDemandAttachments() != null) {
			communicationRequestDetails.get(0).setOnDemandAttachments(onDemandRequestVO.getOnDemandAttachments());
		} else if (ValidatorUtils.hasElements(onDemandRequestVO.getFilePaths())) {
			// below code helps to store file paths in db.
			communicationRequestDetails.get(0).setAttachmentFilePaths(onDemandRequestVO.getFilePaths().stream()
					.filter(filePath -> !filePath.isEmpty()).collect(Collectors.joining(SEMI_COLON)));
		}
		communicationRequestDetails.get(0).setSkipStorageForLetter(onDemandRequestVO.isSkipStorageForLetter());
		return communicationGeneratorService.generateCommunicationOnDemand(communicationRequestDetails.get(0),
				onDemandRequestVO.getInitializedData(), onDemandRequestVO.isReturnGeneratedLetterContentOnly());
	}

	private Map<String, Object> createLocalCacheMap(OnDemandRequestVO onDemandRequestVO, RequestVO requestVO) {
		Map<String, Object> localCacheMap = new HashMap<>();
		if (onDemandRequestVO.getInitializedData() != null) {
			localCacheMap.putAll(onDemandRequestVO.getInitializedData());
		}
		localCacheMap.put(PRIMARY_EMAIL_ADDRESS, onDemandRequestVO.getPrimaryEmailAddress());
		localCacheMap.put(PRIMARY_PHONE_NUMBER, onDemandRequestVO.getPrimaryPhoneNumber());
		localCacheMap.put(BCC_EMAIL_ADDRESS, onDemandRequestVO.getBccEmailAddress());
		localCacheMap.put(CC_EMAIL_ADDRESS, onDemandRequestVO.getCcEmailAddress());
		communicationGenerationHelper.updateRequestVoWithUserUri(requestVO);
		return localCacheMap;
	}

	private RequestVO createRequestVO(OnDemandRequestVO onDemandRequestVO) {
		RequestVO requestVO = new RequestVO();
		requestVO.setEventCode(onDemandRequestVO.getEventCode());
		requestVO.setSourceProduct(onDemandRequestVO.getSourceProduct());
		requestVO.setSubjectURI(onDemandRequestVO.getSubjectURI());
		requestVO.setSubjectReferenceNumber(onDemandRequestVO.getSubjectReferenceNumber());
		requestVO.setSubjectReferenceType(onDemandRequestVO.getSubjectReferenceType());
		requestVO.setAdditionalData(onDemandRequestVO.getAdditionalData());
		requestVO.setAdditionalDataString(onDemandRequestVO.getAdditionalDataString());
		requestVO.setApplicablePrimaryEntityURI(onDemandRequestVO.getApplicablePrimaryEntityURI());
		requestVO.setReferenceDate(onDemandRequestVO.getReferenceDate());
		requestVO.setCreatedByUri(onDemandRequestVO.getCreatedByUri());
		return requestVO;
	}

	@Override
	public RequestVO createRequestVO(String subjectURI, String eventCode, SourceProduct module,
			String subjectReferenceNumber, String subjectReferenceType, String applicablePrimaryEntityURI, Date referenceDate, 
			AdditionalData additionalData) {
		RequestVO requestVO = new RequestVO();
		requestVO.setEventCode(eventCode);
		requestVO.setSourceProduct(module);
		requestVO.setSubjectURI(subjectURI);
		requestVO.setSubjectReferenceNumber(subjectReferenceNumber);
		requestVO.setSubjectReferenceType(subjectReferenceType);
		requestVO.setAdditionalData(additionalData);
		requestVO.setApplicablePrimaryEntityURI(applicablePrimaryEntityURI);
		requestVO.setReferenceDate(referenceDate);
		requestVO.setStatus(STATUS_INITIATED);
		return requestVO;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public GeneratedContentVO logAndGenerateCommunicationByTemplateForPreviewOnDemand(
			OnDemandCommunicationRequestDetailVO onDemandCommunicationRequestDetailVO) {
		CommunicationRequestDetail communicationRequestDetail = createCommReqDtlForCommByTemplate(
				onDemandCommunicationRequestDetailVO);
		communicationRequestDetail.setRequestType(CommunicationConstants.ON_DEMAND);
		communicationRequestDetail
				.setOnDemandAttachments(onDemandCommunicationRequestDetailVO.getOnDemandAttachments());
		communicationRequestDetail.setPreviewFlag(true);
		return communicationGeneratorService.generateCommunicationOnDemand(communicationRequestDetail,
				onDemandCommunicationRequestDetailVO.getInitializedData(),
				onDemandCommunicationRequestDetailVO.isReturnGeneratedLetterContentOnly());
	}

	private CommunicationRequestDetail createCommReqDtlForCommByTemplate(
			OnDemandCommunicationRequestDetailVO onDemandCommunicationRequestDetailVO) {
		String systemUserURI = "com.nucleus.user.User:9015";
		CommunicationRequestDetail communicationRequestDetail = communicationEventLoggerBusinessObject
				.setCommunicationGenerationDetailFromOnDemandVO(onDemandCommunicationRequestDetailVO);
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (onDemandCommunicationRequestDetailVO.getCreatedByUri() != null) {
			communicationRequestDetail.getEntityLifeCycleData()
					.setCreatedByUri(onDemandCommunicationRequestDetailVO.getCreatedByUri());
		} else if (securityContext.getAuthentication() != null) {
			UserInfo userInfo = (UserInfo) securityContext.getAuthentication().getPrincipal();
			communicationRequestDetail.getEntityLifeCycleData().setCreatedByUri(userInfo.getUserReference().getUri());
		} else {
			communicationRequestDetail.getEntityLifeCycleData().setCreatedByUri(systemUserURI);
		}
		return communicationRequestDetail;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public GeneratedContentVO saveBytesAsPdfForPreview(byte[] bytes, String fileName) {
		GeneratedContentVO generatedContentVo = new GeneratedContentVO();
		StringBuilder fileNameWithPath = new StringBuilder();
		String filePath = communicationGenerationHelper.getCommunicationPreviewDocPath();
		fileNameWithPath.append(filePath).append(fileName);
		communicationGenerationHelper.writeDownloadedContent(bytes,
				fileNameWithPath.toString());
		generatedContentVo.setCommunicationType(CommunicationType.LETTER);
		generatedContentVo.setLocation(filePath);
		generatedContentVo.setFileName(fileName);
		return generatedContentVo;
	}

}
