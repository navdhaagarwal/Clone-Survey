package com.nucleus.finnone.pro.communicationgenerator.service;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.FAILED_STATUS;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.FILE_NAME_SEPARATOR_SYMBOL;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.CommunicationExceptionLoggerHelper;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationCommonBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationErrorLoggerBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationGeneratorBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationDataPreparationService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationErrorLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationEventLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGroupCriteriaVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.GeneratedContentVO;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.UserService;

@Named("communicationGenerationDelegate")
public class CommunicationGenerationDelegate implements ICommunicationGenerationDelegate {

	@Inject
	// @Named("communicationGeneratorBusinessObject")
	private ICommunicationGeneratorBusinessObject communicationGeneratorBusinessObject;

	@Inject
	@Named("communicationGenerationHelper")
	private CommunicationGenerationHelper communicationGenerationHelper;

	@Inject
	@Named("communicationDataPreparationService")
	private ICommunicationDataPreparationService communicationDataPreparationService;

	@Inject
	@Named("communicationEventLoggerService")
	private ICommunicationEventLoggerService communicationEventLoggerService;

	@Inject
	@Named("communicationDataPreparationWrapper")
	private CommunicationDataPreparationWrapper communicationDataPreparationWrapper;

	@Inject
	@Named("communicationErrorLoggerService")
	private ICommunicationErrorLoggerService communicationErrorLoggerService;

	@Inject
	@Named("communicationErrorLoggerBusinessObject")
	private ICommunicationErrorLoggerBusinessObject communicationErrorLoggerBusinessObject;

	@Inject
	@Named("beanAccessHelper")
	private BeanAccessHelper beanAccessHelper;

	@Inject
	@Named("communicationCommonBusinessObject")
	private ICommunicationCommonBusinessObject communicationCommonBusinessObject;

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	@Inject
	@Named("couchDataStoreDocumentService")
	private DatastorageService dataStorageService;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Inject
	@Named("communicationCacheService")
	ICommunicationCacheService communicationCacheService;

	@Inject
	@Named("userService")
	private UserService userService;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public GeneratedContentVO generateCommunication(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> localCacheMap, Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap,
			boolean onDemand, boolean generateContentOnly) {
		if (BooleanUtils.isTrue(communicationRequestDetail.getPreviewFlag())) {
			return generateCommunicationForPreviewInNewTransaction(communicationRequestDetail, localCacheMap,
					additionalMethodsMap,onDemand);
		}
		return generateCommunicationInNewTransaction(communicationRequestDetail, localCacheMap, additionalMethodsMap,
				onDemand, generateContentOnly);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public GeneratedContentVO generateSchedularBasedCommunicationInNewTransaction(
			CommunicationRequestDetail communicationRequestDetail, Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap, boolean onDemand,
			boolean generateContentOnly) {
		Map<String, Integer> retryAttemptConfigurations = communicationGenerationHelper
				.getRetryAttemptConfigurationsFromCache();

		return checkAndGenerateCommunication(communicationRequestDetail, localCacheMap, additionalMethodsMap, onDemand,
				generateContentOnly, retryAttemptConfigurations);
	}

	private GeneratedContentVO checkAndGenerateCommunication(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> localCacheMap, Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap,
			boolean onDemand, boolean generateContentOnly, Map<String, Integer> retryAttemptConfigurations) {
		Boolean retriedAttemptsExhausted = communicationGeneratorBusinessObject.checkIfRetriedAttempstExhausted(
				communicationRequestDetail, retryAttemptConfigurations,
				communicationRequestDetail.getCommunicationTemplate().getCommunication());
		GeneratedContentVO generatedContentVO = null;
		if (retriedAttemptsExhausted) {
			Map<String, Object> additionalDataToBeUpdatedInHistory = new HashMap<>();
			additionalDataToBeUpdatedInHistory.put(CommunicationGeneratorConstants.STATUS, FAILED_STATUS);
			communicationGeneratorBusinessObject.moveRequestToHistoryAndDeleteGeneratedRequest(
					communicationRequestDetail, additionalDataToBeUpdatedInHistory);
		} else {
			generatedContentVO = generateCommunicationInNewTransaction(communicationRequestDetail, localCacheMap,
					additionalMethodsMap, onDemand, generateContentOnly);
		}
		return generatedContentVO;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public GeneratedContentVO generateCommunicationInNewTransaction(
			CommunicationRequestDetail communicationRequestDetail, Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap, boolean onDemand,
			boolean generateContentOnly) {
		GeneratedContentVO generatedContentVO = null;
		Map<String, Object> contextMap = createContextMapForCommGeneration(communicationRequestDetail, onDemand);
		if (onDemand) {
			updateContextMapForOnDemand(communicationRequestDetail, contextMap);
		}
		contextMap.putAll(communicationDataPreparationService
				.getInitializedReferencedObjects(communicationRequestDetail, localCacheMap));
		putInitializedDataInContextMap(contextMap, localCacheMap);
		boolean communicationGenerationAllowed = communicationGeneratorBusinessObject
				.checkIfCommunicationGenerationAllowed(communicationRequestDetail, contextMap, localCacheMap);
		if (!communicationGenerationAllowed) {
			Message message = new Message(CommunicationGeneratorConstants.COMMN_GEN_NOT_ALLOWED,
					Message.MessageType.ERROR,
					communicationRequestDetail.getCommunicationTemplate().getCommunication().getCommunicationName());
			if (onDemand) {
				throw ExceptionBuilder.getInstance(BusinessException.class).setMessage(message)
						.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();

			} else {
				// log error in table
				List<Message> errorMessages = Arrays.asList(message);
				communicationErrorLoggerService.updateCommunicationAndCommunicationProcessErrorLoggerDetail(
						communicationRequestDetail, errorMessages, localCacheMap);
				return generatedContentVO;
			}
		}
		try {
			updateCommunicationRequestDetails(communicationRequestDetail, localCacheMap, additionalMethodsMap,
					contextMap);
			CommunicationGroupCriteriaVO communicationGroupCriteriaVO = createCommGroupCriteriaVo(
					communicationRequestDetail, localCacheMap, contextMap);
			String fileName;
			String subjectReferenceType = communicationRequestDetail.getSubjectReferenceType();
			if (subjectReferenceType != null && !subjectReferenceType.isEmpty()) {

				fileName = communicationGenerationHelper.generateDownloadableFileName(
						communicationRequestDetail.getCommunicationCode(),
						communicationRequestDetail.getSubjectReferenceNumber(),
						communicationRequestDetail.getSubjectReferenceType());
			}

			else {
				fileName = communicationGenerationHelper.generateDownloadableFileName(
						communicationRequestDetail.getCommunicationCode(),
						communicationRequestDetail.getSubjectReferenceNumber());
			}

			if (onDemand) {
				communicationGroupCriteriaVO.setGenerateContentOnly(generateContentOnly);
				generateEmailAttachments(communicationGroupCriteriaVO, communicationRequestDetail, localCacheMap,
						additionalMethodsMap,true);
				generatedContentVO = communicationGeneratorBusinessObject.generateAndWriteOrSendCommunication(
						communicationGroupCriteriaVO, communicationRequestDetail, contextMap);
				if (CommunicationType.LETTER.equals(communicationGroupCriteriaVO.getCommunicationTemplate()
						.getCommunication().getCommunicationType().getCode())) {
					communicationRequestDetail.setLetterStorageId(
							saveLetterIntoStorageForOnDemand(generatedContentVO, communicationRequestDetail));
					fileName = fileName.concat(String.valueOf(communicationRequestDetail.getId()))
							.concat(CommunicationGeneratorConstants.PDF_EXTENSION);
					communicationRequestDetail.setAttachmentName(fileName);
					generatedContentVO.setFileName(communicationRequestDetail.getAttachmentName());
					communicationGeneratorBusinessObject.updateCommunicationRequestAndMoveToHistory(
							communicationRequestDetail, generatedContentVO.getGeneratedText(), null);
				} else {
					communicationGeneratorBusinessObject.updateCommunicationRequest(communicationRequestDetail,
							CommunicationRequestDetail.PROCESSED);
				}
			} else {
				communicationGroupCriteriaVO.setGenerateContentOnly(
						communicationRequestDetail.getGenerateMergedFile() || generateContentOnly);
				generateEmailAttachments(communicationGroupCriteriaVO, communicationRequestDetail, localCacheMap,
						additionalMethodsMap,false);
				generatedContentVO = communicationGeneratorBusinessObject.generateAndWriteOrSendCommunication(
						communicationGroupCriteriaVO, communicationRequestDetail, contextMap);
				communicationGroupCriteriaVO.setGenerateContentOnly(false);
			}
			if (!onDemand && !generateContentOnly) {
				if (CommunicationType.LETTER.equals(communicationGroupCriteriaVO.getCommunicationTemplate()
						.getCommunication().getCommunicationType().getCode())) {
					communicationRequestDetail
							.setLetterStorageId(saveLetterIntoStorage(generatedContentVO, communicationRequestDetail));

					fileName = fileName.concat(String.valueOf(communicationRequestDetail.getId()))
							.concat(CommunicationGeneratorConstants.PDF_EXTENSION);
					communicationRequestDetail.setAttachmentName(fileName);
					generatedContentVO.setFileName(communicationRequestDetail.getAttachmentName());
					communicationGeneratorBusinessObject.updateCommunicationRequestAndMoveToHistory(
							communicationRequestDetail, generatedContentVO.getGeneratedText(),
							communicationGroupCriteriaVO);
				} else {
					communicationGeneratorBusinessObject.updateCommunicationRequest(communicationRequestDetail,
							CommunicationRequestDetail.PROCESSED);
				}
			}

		} catch (BaseException baseException) {
			if (!(baseException.isLogged())) {
				handleBaseException(baseException);
			}
			if (onDemand) {
				throw baseException;
			} else {
				communicationErrorLoggerService.updateCommunicationAndCommunicationProcessErrorLoggerDetail(
						communicationRequestDetail, baseException.getMessages(), localCacheMap);
			}
		} catch (Exception exception) {
			BaseLoggers.exceptionLogger.error("Exception in prepareDataForCommunicationGeneration", exception);
			if (onDemand) {
				if (exception instanceof TransactionSystemException) {
					handleTransactionSystemException(exception);
				} else {
					throwBusinessException(exception, "Error in Generating Letter");
				}
				CommunicationExceptionLoggerHelper.throwSystemException(exception, "Error in Generating Letter");
			} else {
				List<Message> errorMessages = new ArrayList<>();
				Message message = new Message(CommunicationGeneratorConstants.COMMUNICATION_GENERATION_ERROR,
						Message.MessageType.ERROR);
				Message exceptionMessage = new Message();
				exceptionMessage.setMessageArguments(exception.getMessage() + " id " + Thread.currentThread().getId()
						+ " Name " + Thread.currentThread().getName());
				errorMessages.add(exceptionMessage);
				if (exception instanceof TransactionSystemException) {
					TransactionSystemException systemException = (TransactionSystemException) exception;
					if (BaseException.class.isAssignableFrom(systemException.getApplicationException().getClass())) {
						BaseException be = (BaseException) systemException.getApplicationException();
						errorMessages.addAll(be.getMessages());
					} else {
						errorMessages.add(message);
					}
				} else {
					errorMessages.add(message);
				}
				communicationErrorLoggerService.updateCommunicationAndCommunicationProcessErrorLoggerDetail(
						communicationRequestDetail, errorMessages, localCacheMap);
			}
		}
		return generatedContentVO;
	}

	private void throwBusinessException(Exception exception, String message) {
		throw ExceptionBuilder.getInstance(BusinessException.class,
				CommunicationGeneratorConstants.LETTER_GENERATION_ERROR, message)
				.setMessage(CommunicationGeneratorConstants.LETTER_GENERATION_ERROR)
				.setOriginalException(exception).build();
	}

	private CommunicationGroupCriteriaVO createCommGroupCriteriaVo(
			CommunicationRequestDetail communicationRequestDetail, Map<String, Object> localCacheMap,
			Map<String, Object> contextMap) {
		CommunicationGroupCriteriaVO communicationGroupCriteriaVO = communicationGenerationHelper
				.prepareDataForCommunicationGroupCriteriaVO(contextMap, communicationRequestDetail);
		communicationGroupCriteriaVO.setSchedularInstanceId(
				(String) localCacheMap.get(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID));
		return communicationGroupCriteriaVO;
	}

	private void updateCommunicationRequestDetails(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> localCacheMap, Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap,
			Map<String, Object> contextMap) {
		communicationDataPreparationService.prepareDataForCommunicationGeneration(communicationRequestDetail,
				contextMap, localCacheMap, additionalMethodsMap);
		communicationGeneratorBusinessObject
				.updateCommunicationRequestWithApplicableEmailAndPhone(communicationRequestDetail, contextMap);
	}

	private void updateContextMapForOnDemand(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> contextMap) {
		String userUri = communicationRequestDetail.getEntityLifeCycleData().getCreatedByUri();
		contextMap.put("generatedByUserName", userService.getUserNameByUserUri(userUri));
		contextMap.put("generatedByFullName",
				userService.getUserFullNameForUserId(EntityId.fromUri(userUri).getLocalId()));
	}

	private Map<String, Object> createContextMapForCommGeneration(CommunicationRequestDetail communicationRequestDetail,
			boolean onDemand) {
		Map<String, Object> contextMap = new HashMap<>();
		contextMap.put("subjectURI", communicationRequestDetail.getSubjectURI());
		contextMap.put("applicablePrimaryEntityURI", communicationRequestDetail.getApplicablePrimaryEntityURI());
		contextMap.put("subjectReferenceNumber", communicationRequestDetail.getSubjectReferenceNumber());
		contextMap.put("subjectReferenceType", communicationRequestDetail.getSubjectReferenceType());
		contextMap.put("onDemandFlag", onDemand);
		contextMap.put("additionalData", communicationRequestDetail.getAdditionalData());
		contextMap.put("templateReferenceNumber",
				communicationRequestDetail.getCommunicationTemplate().getTemplateReferenceNumber());
		contextMap.put("communicationReferenceNumber", communicationRequestDetail.getCommunicationTemplate()
				.getCommunication().getCommunicationReferenceNumber());
		return contextMap;
	}

	private void putInitializedDataInContextMap(Map<String, Object> contextMap, Map<String, Object> localCacheMap) {
		for (Map.Entry<String, Object> entry : localCacheMap.entrySet()) {
			if (entry.getKey().equals("subjectURI") && entry.getKey().equals("applicablePrimaryEntityURI")) {
				continue;
			}
			contextMap.put(entry.getKey(), entry.getValue());
		}
	}

	private String saveLetterIntoStorageForOnDemand(GeneratedContentVO generatedContentVO,
			CommunicationRequestDetail communicationRequestDetail) {
		if (communicationRequestDetail.isSkipStorageForLetter()
				|| ValidatorUtils.isNull(generatedContentVO.getGeneratedContent())) {
			return null;
		}
		return saveLetterIntoStorage(generatedContentVO, communicationRequestDetail);
	}

	private String saveLetterIntoStorage(GeneratedContentVO generatedContentVO,
			CommunicationRequestDetail communicationRequestDetail) {
		StringBuilder fileName = new StringBuilder();
		fileName.append(generatedContentVO.getLocation()).append(communicationRequestDetail.getCommunicationCode())
				.append(FILE_NAME_SEPARATOR_SYMBOL).append(communicationRequestDetail.getSubjectReferenceNumber())
				.append(FILE_NAME_SEPARATOR_SYMBOL).append(communicationRequestDetail.getId())
				.append(CommunicationGeneratorConstants.PDF_EXTENSION);
		return dataStorageService.saveDocument(new ByteArrayInputStream(generatedContentVO.getGeneratedContent()),
				fileName.toString(), "PDF");
	}

	private void generateEmailAttachments(CommunicationGroupCriteriaVO communicationGroupCriteriaVO,
			CommunicationRequestDetail communicationRequestDetail, Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap, boolean onDemand) {

		if (!isEmailWithAttachments(communicationGroupCriteriaVO)
				|| ValidatorUtils.isNull(communicationRequestDetail.getId())) {
			return;
		}
		/*List<CommunicationRequestDetail> attachments = communicationGeneratorBusinessObject
				.getAttachmentsForEmail(communicationRequestDetail.getId());*/

		List<CommunicationRequestDetail> attachments = null;

			attachments = communicationGeneratorBusinessObject
					.getAttachmentsForEmail(communicationRequestDetail.getId());



		Map<CommunicationRequestDetail, GeneratedContentVO> requestDtlAndContentMap = new HashMap<>();
		for (CommunicationRequestDetail attachment : attachments) {
			entityDao.detach(attachment);
			communicationGenerationHelper.initializeCommunicationRequestDetailFromCache(attachment);
			GeneratedContentVO contentVO = generateCommunication(attachment, localCacheMap, additionalMethodsMap, onDemand,
					true);
			if (contentVO == null) {
				BaseLoggers.flowLogger.error("Not able to generate attachments for communicationRequestDetail id : "
						+ communicationRequestDetail.getId());
				Message message = new Message(CommunicationGeneratorConstants.ERROR_IN_ATTACHMENT_GEN,
						Message.MessageType.ERROR);
				throw ExceptionBuilder.getInstance(BusinessException.class).setMessage(message)
						.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
			}
			requestDtlAndContentMap.put(attachment, contentVO);
		}
		communicationGroupCriteriaVO.setRequestDtlAndContentMap(requestDtlAndContentMap);
	}

	private boolean isEmailWithAttachments(CommunicationGroupCriteriaVO communicationGroupCriteriaVO) {
		return ((CommunicationType.EMAIL
				.equals(communicationGroupCriteriaVO.getCommunicationName().getCommunicationType().getCode()) ||
				CommunicationType.WHATSAPP
				.equals(communicationGroupCriteriaVO.getCommunicationName().getCommunicationType().getCode()))
				&& ValidatorUtils.hasElements(communicationGroupCriteriaVO.getCommunicationName().getAttachments()));
	}

	public void setCommunicationGeneratorBusinessObject(
			ICommunicationGeneratorBusinessObject communicationGeneratorBusinessObject) {
		this.communicationGeneratorBusinessObject = communicationGeneratorBusinessObject;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private GeneratedContentVO generateCommunicationForPreviewInNewTransaction(
			CommunicationRequestDetail communicationRequestDetail, Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap, boolean onDemand) {
		GeneratedContentVO generatedContentVO = null;
		Map<String, Object> contextMap = createContextMapForCommGeneration(communicationRequestDetail, true);
		updateContextMapForOnDemand(communicationRequestDetail, contextMap);
		contextMap.putAll(communicationDataPreparationService
				.getInitializedReferencedObjects(communicationRequestDetail, localCacheMap));
		putInitializedDataInContextMap(contextMap, localCacheMap);
		try {
			updateCommunicationRequestDetails(communicationRequestDetail, localCacheMap, additionalMethodsMap,
					contextMap);
			CommunicationGroupCriteriaVO communicationGroupCriteriaVO = createCommGroupCriteriaVo(
					communicationRequestDetail, localCacheMap, contextMap);
			communicationGroupCriteriaVO.setGenerateContentOnly(false);
			generateEmailAttachments(communicationGroupCriteriaVO, communicationRequestDetail, localCacheMap,
					additionalMethodsMap,onDemand);
			generatedContentVO = communicationGeneratorBusinessObject.generateCommunicationForPreview(
					communicationGroupCriteriaVO, communicationRequestDetail, contextMap);
			communicationGeneratorBusinessObject.updateCommunicationRequestAndMoveToHistory(communicationRequestDetail,
					generatedContentVO.getGeneratedText(), null);

		} catch (BaseException baseException) {
			if (!(baseException.isLogged())) {
				handleBaseException(baseException);
			}
			throw baseException;

		} catch (Exception exception) {
			BaseLoggers.exceptionLogger.error("Exception in prepareDataForCommunicationGeneration", exception);
			if (exception instanceof TransactionSystemException) {
				handleTransactionSystemException(exception);
			} else {
				throwBusinessException(exception, "Error in Letter Preview");
			}
			CommunicationExceptionLoggerHelper.throwSystemException(exception, "Error in Letter Preview");

		}
		return generatedContentVO;
	}

	private void handleBaseException(BaseException baseException) {
		BaseLoggers.exceptionLogger.error("BaseException in prepareDataForCommunicationGeneration",
				baseException);
		baseException.setLogged(true);
	}

	private void handleTransactionSystemException(Exception exception) {
		TransactionSystemException systemException = (TransactionSystemException) exception;
		if (systemException.getApplicationException() instanceof BusinessException) {
			BaseException be = (BaseException) systemException.getApplicationException();
			be.setLogged(true);
			throw be;
		}
	}
	
}
