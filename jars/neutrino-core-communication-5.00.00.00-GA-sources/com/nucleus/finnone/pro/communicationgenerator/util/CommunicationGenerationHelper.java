package com.nucleus.finnone.pro.communicationgenerator.util;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.FILE_NAME_SEPARATOR_SYMBOL;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STRING_LENGTH_FOUR_THOUSAND;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.NeutrinoUUIDGenerator;
import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.enity.ConfigurationGroup;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.misc.util.StringUtil;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestHistory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationCommonService;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGenerationDetailVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGroupCriteriaVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.RequestVO;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.finnone.pro.general.util.documentgenerator.TableDataVO;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;


@Named("communicationGenerationHelper")
public class  CommunicationGenerationHelper{
	
	@Inject
	@Named("messageSource")
	private MessageSource messageSource;
	
	@Inject
	@Named("communicationCommonService")
	private ICommunicationCommonService communicationCommonService;
	
	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;
	
	@Inject
	@Named("userService")
	private UserService userService;
	
	@Inject
	@Named("communicationCacheService")
	ICommunicationCacheService communicationCacheService;
	
	@Autowired
    private Environment env;
	
	private final NeutrinoUUIDGenerator uuidGenerator = new NeutrinoUUIDGenerator(ProductInformationLoader.getProductCode() + "-");
		
	public static final String INDIVIDUAL = "INDIVIDUAL";
	public static final String NON_INDIVIDUAL = "NON_INDIVIDUAL";
	public static final String NEUTRINO_SYSTEM_USER = "system";

	private static final String COMM_PREVIEW_FILE_ERROR_MSG = "Communication Preview Path property - {communication.preview.temp.doc.path} or Template_Path property is empty.";

	private static final String TEMPLATE_PATH = "template_path";

	private static final String COMMUNICATION_PREVIEW_TEMP_DOC_PATH = "communication.preview.temp.doc.path";

	private static final String ERROR_MESSAGE_COMMUNICATION = "Error in writing communication template file while generating communication";
	
	public CommunicationGenerationDetailVO prepareCommunicationGenerationDetail(SourceProduct module,Long applicablePrimaryEntityId, Boolean isOnDemandGeneration,String subjectURI,Character status,String communicationCode,String eventCode){
		CommunicationGenerationDetailVO communicationGenerationDetailVO=new CommunicationGenerationDetailVO();
		communicationGenerationDetailVO.setApplicablePrimaryEntityId(applicablePrimaryEntityId);
		communicationGenerationDetailVO.setOnDemandGeneration(isOnDemandGeneration);
		communicationGenerationDetailVO.setSubjectURI(subjectURI);
		communicationGenerationDetailVO.setSubjectEntityId(EntityId.fromUri(subjectURI).getLocalId());
		communicationGenerationDetailVO.setStatus(status);
		communicationGenerationDetailVO.setCommunicationCode(communicationCode);
		communicationGenerationDetailVO.setEventCode(eventCode);
		communicationGenerationDetailVO.setSourceProduct(module);
		return communicationGenerationDetailVO;
		
	}
	
	public void initializeCommunicationRequestDetailFromCache(CommunicationRequestDetail communicationRequestDetail)
	{
		CommunicationTemplate communicationTemplate = communicationCacheService
				.getCommunicationTemplate(communicationRequestDetail.getCommunicationTemplateId());
		communicationRequestDetail.setCommunicationTemplate(communicationTemplate);		
		communicationRequestDetail.setSourceProduct(communicationTemplate.getCommunication().getSourceProduct());
	
	}
	
	public void updateRequestVoWithUserUri(RequestVO requestVO)
	{
		if (isNull(requestVO.getCreatedByUri())) {
    	SecurityContext securityContext=SecurityContextHolder.getContext();
    	UserInfo userInfo=(UserInfo)securityContext.getAuthentication().getPrincipal();
    	requestVO.setCreatedByUri(userInfo.getUserReference().getUri());
		}
		
	}
	
	public CommunicationEventRequestLog prepareCommEventRequestData(RequestVO requestVO) {
		
		CommunicationEventRequestLog communicationEventRequestLog = new CommunicationEventRequestLog();
		communicationEventRequestLog.setStatus(requestVO.getStatus());
		communicationEventRequestLog.setPreviewFlag(requestVO.getPreviewFlag() == null ? Boolean.FALSE : requestVO.getPreviewFlag());
		communicationEventRequestLog.setEventCode(requestVO.getEventCode());
		communicationEventRequestLog.setSubjectURI(requestVO.getSubjectURI());
		communicationEventRequestLog.setSubjectReferenceNumber(requestVO.getSubjectReferenceNumber());
		communicationEventRequestLog.setSubjectReferenceType(requestVO.getSubjectReferenceType());
		communicationEventRequestLog.setAdditionalData(requestVO.getAdditionalData());
		communicationEventRequestLog.setSourceProduct(requestVO.getSourceProduct());
		communicationEventRequestLog.setApplicablePrimaryEntityURI(requestVO.getApplicablePrimaryEntityURI());	
		communicationEventRequestLog.setReferenceDate(requestVO.getReferenceDate());
		communicationEventRequestLog.setRequestReferenceId(requestVO.getRequestReferenceId());
		communicationEventRequestLog.setGenerateMergedFile(requestVO.getGenerateMergedFile());
		communicationEventRequestLog.setEventRequestLogId(uuidGenerator.generateUuid());
		splitAndStoreAdditionalJsonData(communicationEventRequestLog, requestVO.getAdditionalDataString());
		if (requestVO.getCreatedByUri() == null) {
			User user=userService.findUserByUsername(NEUTRINO_SYSTEM_USER);
			if (user != null && user.getUri() != null) {
				requestVO.setCreatedByUri(user.getUri());
			}  
		}
		communicationEventRequestLog.getEntityLifeCycleData().setCreatedByUri(requestVO.getCreatedByUri());
		communicationEventRequestLog.setRequestType(requestVO.getRequestType());
		return communicationEventRequestLog;
	}
	
	protected void splitAndStoreAdditionalJsonData(CommunicationEventRequestLog communicationEventRequestLog,String additionalDataJsonString){
		List<String> additionalDataStringList = StringUtil.split(additionalDataJsonString,STRING_LENGTH_FOUR_THOUSAND);
		if(hasElements(additionalDataStringList)){
			for(int i=0;i<additionalDataStringList.size();i++){
				String value = additionalDataStringList.get(i);
				switch (i) {
				case 0:
					communicationEventRequestLog.setJsonAdditionalField1(value);
					communicationEventRequestLog.setJsonAdditionalField2(null);
					communicationEventRequestLog.setJsonAdditionalField3(null);
					break;
                case 1:
                	communicationEventRequestLog.setJsonAdditionalField2(value);
                	communicationEventRequestLog.setJsonAdditionalField3(null);
					break;
                case 2:
                	communicationEventRequestLog.setJsonAdditionalField3(value);
					break;	
				default:
					throw new IllegalArgumentException("Additional Json Data is exceeding the limit of 12000 characters.");
				}
			}
		}
	}
	
	public CommunicationEventRequestHistory prepareCommEventRequestHistoryData(CommunicationEventRequestLog communicationEventRequestLog){
		CommunicationEventRequestHistory communicationEventRequestHistory = new CommunicationEventRequestHistory();
		communicationEventRequestHistory.setStatus(CommunicationGeneratorConstants.STATUS_COMPLETED);
		communicationEventRequestHistory.setPreviewFlag(communicationEventRequestLog.getPreviewFlag() == null ? Boolean.FALSE : communicationEventRequestLog.getPreviewFlag());
		communicationEventRequestHistory.setDeliveryPriority(communicationEventRequestLog.getDeliveryPriority());
		communicationEventRequestHistory.setRequestType(communicationEventRequestLog.getRequestType());
		communicationEventRequestHistory.setEventCode(communicationEventRequestLog.getEventCode());
		communicationEventRequestHistory.setJsonAdditionalField1(communicationEventRequestLog.getJsonAdditionalField1());
		communicationEventRequestHistory.setJsonAdditionalField2(communicationEventRequestLog.getJsonAdditionalField2());
		communicationEventRequestHistory.setJsonAdditionalField3(communicationEventRequestLog.getJsonAdditionalField3());
		communicationEventRequestHistory.setSubjectURI(communicationEventRequestLog.getSubjectURI());
		communicationEventRequestHistory.setSubjectReferenceNumber(communicationEventRequestLog.getSubjectReferenceNumber());
		communicationEventRequestHistory.setSubjectReferenceType(communicationEventRequestLog.getSubjectReferenceType());
		communicationEventRequestHistory.setCommunicationCode(communicationEventRequestLog.getCommunicationCode());
		if (ValidatorUtils.notNull(communicationEventRequestLog.getAdditionalData())) {
			communicationEventRequestHistory.setAdditionalData(communicationEventRequestLog.getAdditionalData());
		}
		communicationEventRequestHistory.setSourceProduct(communicationEventRequestLog.getSourceProduct());
		communicationEventRequestHistory.setApplicablePrimaryEntityURI(communicationEventRequestLog.getApplicablePrimaryEntityURI());
		communicationEventRequestHistory.setReferenceDate(communicationEventRequestLog.getReferenceDate());
		communicationEventRequestHistory.setRequestReferenceId(communicationEventRequestLog.getRequestReferenceId());
		communicationEventRequestHistory.setGenerateMergedFile(communicationEventRequestLog.getGenerateMergedFile());
		communicationEventRequestHistory.setEventRequestLogId(communicationEventRequestLog.getEventRequestLogId());
		communicationEventRequestHistory.getEntityLifeCycleData().setCreatedByUri(communicationEventRequestLog.getEntityLifeCycleData().getCreatedByUri());
		return communicationEventRequestHistory;
	}
	
	public void writeDownloadedContent(byte[] fileContent,String fileNameWithPath)
	{
		BufferedOutputStream bs = null;
		FileOutputStream fs=null;
		try {
			
			fs = new FileOutputStream(new File(fileNameWithPath));
		    bs = new BufferedOutputStream(fs);
		    bs.write(fileContent);
		    bs.close();
		    bs = null;
		} catch (Exception e) {
		    
			if (bs != null) try { bs.close(); } catch (Exception ex) {
				BaseLoggers.flowLogger.error(ex.getMessage());
			}
			Message message = new Message();
			message.setI18nCode("fmsg.00002270");
			message.setMessageArguments(e.getMessage());
			message.setType(MessageType.ERROR);
			throw ExceptionBuilder.getInstance(BusinessException.class, "fmsg.00002270" ,e.getMessage()).setMessages(CoreUtility.addToList(new ArrayList<Message>(), message)).build();
		} finally {
            IOUtils.closeQuietly(fs);
        }
	}
	
	
	
	public List<CommunicationErrorLoggerDetail> createProcessErrorLogForCommunicationProcess(List<Message> errorMessages,Long transactionRefNo,String eventCode){		

		List<CommunicationErrorLoggerDetail> processesErrorLogDetailList = new ArrayList<CommunicationErrorLoggerDetail>();

		for(Message errorMessage:errorMessages){
			CommunicationErrorLoggerDetail processesErrorLogDetail= new CommunicationErrorLoggerDetail();
			processesErrorLogDetail.setErrorDescription(getMessageDescription(errorMessage, Locale.US));
			processesErrorLogDetail.setErrorMessageId(errorMessage.getI18nCode());
			processesErrorLogDetail.setErrorMessageparameters(getMessageArgumentString(errorMessage.getMessageArguments()));
			processesErrorLogDetail.setErrorType(1L);
			processesErrorLogDetail.setTransactionEvent(eventCode);
			processesErrorLogDetail.setTransactionProcessId(transactionRefNo);

			processesErrorLogDetailList.add(processesErrorLogDetail);
		}
		
		return processesErrorLogDetailList;
	}

	public String getMessageDescription(Message message, Locale locale) {
		Locale localeupdated = null;
		if (locale == null) {
			localeupdated=configurationService.getSystemLocale();
		} else {
			localeupdated = locale;
		}
		return messageSource.getMessage(message.getI18nCode(), message.getMessageArguments(), message.getI18nCode(), localeupdated);
	}
	
	private String getMessageArgumentString(String[] messageArgumentsList){
		String messageArguments = "";

		if(messageArgumentsList!=null && messageArgumentsList.length>0){
			int count=0;
			for(String messageArg : messageArgumentsList){
				if(count>0){
					messageArguments = messageArguments + ","+messageArg ; 
					count++;
				}
				else
				{
					messageArguments= messageArg;
					count++;
				}						
			}
		}
		return messageArguments;
	}
	
	public String convertCase(String value ,String displayCase){
		
		if(notNull(value) && !"".equals(value) && notNull(displayCase) && !"".equals(displayCase)){
			
			if(CommunicationGeneratorConstants.INITCAP_CASE_PARAMETER.equalsIgnoreCase(displayCase)){
				value = convertValueToInitCap(value);
			}
			else if(CommunicationGeneratorConstants.UPPER_CASE_PARAMETER.equalsIgnoreCase(displayCase)){
				value =  value.toUpperCase();
			}
				
			else if(CommunicationGeneratorConstants.LOWER_CASE_PARAMETER.equalsIgnoreCase(displayCase)){
				value =  value.toLowerCase();
			}			
		}
		return value;
		
	}
	
	private String convertValueToInitCap(String value){
		
		char[] chars = value.toLowerCase().toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) {
		    if (!found && Character.isLetter(chars[i])) {
		      chars[i] = Character.toUpperCase(chars[i]);
		      found = true;
		    } else if (Character.isWhitespace(chars[i])) { 
		      found = false;
		    }
		 }
		 return String.valueOf(chars);
	}


	public static List<Integer> getApprovalStatusList(){
	
		List<Integer> approvalStatusList=new ArrayList<Integer>();
		approvalStatusList.add(ApprovalStatus.APPROVED);
		approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
		approvalStatusList.add(ApprovalStatus.APPROVED_DELETED);
		approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		return approvalStatusList;
		
	}
	
	/**
	 * @param contextMap
	 * @param entityURI    Either subjectURI or primaryEntityURI as an argument.
	 * @param mapKey
	 * @return
	 */
	public Map<String,Object> addEntityInContextMap(Map<String, Object> contextMap, Map<String, Object> localCacheMapForTemplate, String entityURI, String mapKey) {
		Object beanObject = null;
		if ((beanObject = localCacheMapForTemplate.get(mapKey)) != null) {
			contextMap.put("contextObject" + beanObject.getClass().getSimpleName(), beanObject);                
		} else if (notNull(entityURI)) {
			EntityId entityId = EntityId.fromUri(entityURI);
			beanObject = communicationCommonService.findById(entityId.getLocalId(), entityId.getEntityClass());
			if (beanObject != null) {
				String classSimpleName = HibernateUtils.getSimpleNameWithoutInitializingProxy(beanObject);
				contextMap.put("contextObject"+classSimpleName, beanObject);                
			 }
		}
		return contextMap;
	}

	@Transactional(noRollbackFor = { SystemException.class, ServiceInputException.class, BusinessException.class,
			BaseException.class, NullPointerException.class })
	public CommunicationGroupCriteriaVO prepareDataForCommunicationGroupCriteriaVO(Map<String, Object> contextMap,
			CommunicationRequestDetail communicationRequestDetail) {
		CommunicationGroupCriteriaVO communicationGroupCriteriaVO = new CommunicationGroupCriteriaVO(); 
		CommunicationTemplate communicationTemplate = communicationCacheService
				.getCommunicationTemplate(communicationRequestDetail.getCommunicationTemplateId());
		CommunicationName communicationName = communicationCacheService
				.getCommunicationName(communicationTemplate.getCommunicationMasterId());
		communicationGroupCriteriaVO.setCommunicationTemplate(communicationTemplate);
		communicationGroupCriteriaVO.setCommunicationName(communicationName);
		if(contextMap.get(CommunicationGeneratorConstants.TABLE_DATA_LIST)!=null && contextMap.get(CommunicationGeneratorConstants.VARIABLE_LIST)!=null)
		{
			communicationGroupCriteriaVO.setTableDataVOList((List<TableDataVO>)contextMap.get(CommunicationGeneratorConstants.TABLE_DATA_LIST));
			communicationGroupCriteriaVO.setVariableList((List<String>)contextMap.get(CommunicationGeneratorConstants.VARIABLE_LIST));
			contextMap.remove(CommunicationGeneratorConstants.TABLE_DATA_LIST);
			contextMap.remove(CommunicationGeneratorConstants.VARIABLE_LIST);
		}
		communicationGroupCriteriaVO.setDataMap(contextMap);
		communicationGroupCriteriaVO.setCommunicationTemplateId(communicationRequestDetail.getCommunicationTemplateId());
		communicationGroupCriteriaVO.setCommunicationCode(communicationRequestDetail.getCommunicationCode());
		return communicationGroupCriteriaVO;
	}
	
	public CommunicationGroupCriteriaVO prepareDataForCommunicationGroupCriteriaVO(Map<String,Object> contextMap,String communicationCode, Long templateId) {
		CommunicationGroupCriteriaVO communicationGroupCriteriaVO = new CommunicationGroupCriteriaVO(); 
		if(contextMap.get(CommunicationGeneratorConstants.TABLE_DATA_LIST)!=null && contextMap.get(CommunicationGeneratorConstants.VARIABLE_LIST)!=null)
		{
			communicationGroupCriteriaVO.setTableDataVOList((List<TableDataVO>)contextMap.get(CommunicationGeneratorConstants.TABLE_DATA_LIST));
			communicationGroupCriteriaVO.setVariableList((List<String>)contextMap.get(CommunicationGeneratorConstants.VARIABLE_LIST));
			contextMap.remove(CommunicationGeneratorConstants.TABLE_DATA_LIST);
			contextMap.remove(CommunicationGeneratorConstants.VARIABLE_LIST);
		}
		communicationGroupCriteriaVO.setDataMap(contextMap);
		communicationGroupCriteriaVO.setCommunicationTemplateId(templateId);
		communicationGroupCriteriaVO.setCommunicationCode(communicationCode);
		return communicationGroupCriteriaVO;
	}
	
	public Map<String, Integer> getRetryAttemptConfigurations() {

		Integer letterRetriedAttempts = CommunicationGeneratorConstants.DEFAULT_RETRY_ATTEMPTS;
		Integer smsRetriedAttempts = CommunicationGeneratorConstants.DEFAULT_RETRY_ATTEMPTS;
		Integer emailRetriedAttempts = CommunicationGeneratorConstants.DEFAULT_RETRY_ATTEMPTS;
		Integer pushRetriedAttempts = CommunicationGeneratorConstants.DEFAULT_RETRY_ATTEMPTS;
		Map<String, Integer> retryAttemptConfigurations = new HashMap<String, Integer>();
		retryAttemptConfigurations.put(
				CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY,
				smsRetriedAttempts);
		retryAttemptConfigurations
				.put(CommunicationGeneratorConstants.LETTER_RETRY_ATTEMPT_CONFIG_KEY,
						letterRetriedAttempts);
		retryAttemptConfigurations.put(
				CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY,
				emailRetriedAttempts);
		retryAttemptConfigurations.put(
                CommunicationGeneratorConstants.PUSH_RETRY_ATTEMPT_CONFIG_KEY,
                pushRetriedAttempts);
		ConfigurationGroup configurationGroup = configurationService
				.getConfigurationGroupFor(SystemEntity.getSystemEntityId());

		if (isNull(configurationGroup)
				|| !hasElements(configurationGroup.getConfiguration())) {
			return retryAttemptConfigurations;
		}

		List<Configuration> configurations = configurationGroup
				.getConfiguration();		
		Hibernate.initialize(configurations);
		for (Configuration configuration : configurations) {

			if (notNull(configuration.getPropertyKey())
					&& notNull(configuration.getPropertyValue())) {
				if (CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY
						.equals(configuration.getPropertyKey())) {
					try {
						retryAttemptConfigurations
								.put(CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY,
										Integer.parseInt(configuration
												.getPropertyValue()));
					} catch (NumberFormatException ne) {
						// do not do anything
					}
				}
				if (CommunicationGeneratorConstants.LETTER_RETRY_ATTEMPT_CONFIG_KEY
						.equals(configuration.getPropertyKey())) {
					try {
						retryAttemptConfigurations
								.put(CommunicationGeneratorConstants.LETTER_RETRY_ATTEMPT_CONFIG_KEY,
										Integer.parseInt(configuration
												.getPropertyValue()));
					} catch (NumberFormatException ne) {
						// do not do anything
					}
				}
				if (CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY
						.equals(configuration.getPropertyKey())) {
					try {
						retryAttemptConfigurations
								.put(CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY,
										Integer.parseInt(configuration
												.getPropertyValue()));
					} catch (NumberFormatException ne) {
						// do not do anything
					}
				}
			}
		}
		return retryAttemptConfigurations;

	}
	
	public Map<String, Integer> getRetryAttemptConfigurationsFromCache() {
		
		Map<String , Integer> retryConfigMap = communicationCacheService.getRetryAttemptsConfiguration();
		
		if(retryConfigMap == null) {
			retryConfigMap = new HashMap<>();
		}
		
		if (retryConfigMap.get(CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY) == null) {
			retryConfigMap.put(CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY, CommunicationGeneratorConstants.DEFAULT_RETRY_ATTEMPTS);
		}
		if (retryConfigMap.get(CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY) == null) {
			retryConfigMap.put(CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY, CommunicationGeneratorConstants.DEFAULT_RETRY_ATTEMPTS);
		}
		if (retryConfigMap.get(CommunicationGeneratorConstants.LETTER_RETRY_ATTEMPT_CONFIG_KEY) == null) {
			retryConfigMap.put(CommunicationGeneratorConstants.LETTER_RETRY_ATTEMPT_CONFIG_KEY, CommunicationGeneratorConstants.DEFAULT_RETRY_ATTEMPTS);
		}
		if (retryConfigMap.get(CommunicationGeneratorConstants.WHATSAPP_RETRY_ATTEMPT_CONFIG_KEY) == null) {
			retryConfigMap.put(CommunicationGeneratorConstants.WHATSAPP_RETRY_ATTEMPT_CONFIG_KEY, CommunicationGeneratorConstants.DEFAULT_RETRY_ATTEMPTS);
		}
		
		return retryConfigMap;
	}
	
	
	public int getBatchSizeFromConfiguration(String communicationSchedularBatch,String communicationQuery,int defaultBatchSize)
	{
		String batchSizeCount=configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), communicationSchedularBatch)
				.getPropertyValue();
		int batchSize=0;
		if(isNull(batchSizeCount)) {
			batchSize=defaultBatchSize;
		}else {
			batchSize=Integer.parseInt(batchSizeCount);
		}
		return batchSize;
	}

	
	public String generateDownloadableFileName(String communicationCode,String subjectReferenceNumber ){
		StringBuilder fileName = new StringBuilder();
		fileName.append(communicationCode)
                .append(FILE_NAME_SEPARATOR_SYMBOL)
                .append(subjectReferenceNumber)
                .append(FILE_NAME_SEPARATOR_SYMBOL);
                
		return fileName.toString();
	}
	public String generateDownloadableFileName(String communicationCode,String subjectReferenceNumber,String  subjectReferenceType){
		StringBuilder fileName = new StringBuilder();
		fileName.append(communicationCode)
                .append(FILE_NAME_SEPARATOR_SYMBOL)
                .append(subjectReferenceType)
                .append(FILE_NAME_SEPARATOR_SYMBOL)
                .append(subjectReferenceNumber)
                .append(FILE_NAME_SEPARATOR_SYMBOL);
                
		return fileName.toString();
	}
	
	public void logAndThrowException(Boolean throwException, Message message, String... args) {
		BaseLoggers.exceptionLogger
				.error(messageSource.getMessage(message.getI18nCode(), args, userService.getUserLocale()));
		if (throwException) {
			throw ExceptionBuilder.getInstance(BusinessException.class).setMessage(message)
					.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
		}
	}
	
	public String getCommunicationPreviewDocPath() {
		String location = env.getProperty(COMMUNICATION_PREVIEW_TEMP_DOC_PATH);

		if (StringUtils.isEmpty(location)) {
			String templatePath = env.getProperty(TEMPLATE_PATH);
			if (StringUtils.isNotEmpty(templatePath)) {
				location = StringUtils.appendIfMissing(templatePath, "/", "\\") + "Preview/";
			}
		}
		if (StringUtils.isEmpty(location)) {
			BaseLoggers.exceptionLogger.error(COMM_PREVIEW_FILE_ERROR_MSG);
			throw new SystemException(COMM_PREVIEW_FILE_ERROR_MSG);
		}
		File file = new File(location);
		if (!file.isDirectory()) {
			BaseLoggers.exceptionLogger.error(
					"There is not any temp folder for Communication Preview Documents on the server at location : {}",
					location);
			throw new SystemException("There is not any temp folder for Communication Preview Documents.");
		}
		return StringUtils.appendIfMissing(location, "/", "\\");
	}
}
