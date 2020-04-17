package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.APPLICABLE_CURRENCY;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.FILE_NAME_SEPARATOR_SYMBOL;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.ON_DEMAND_FLAG;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.SEMI_COLON;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.SMS_ALT_PHONE_NUMBERS;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.SMS_PRIMARY_PHONE_NUMBERS;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STATUS_COMPLETED;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.TO_BCC_EMAIL_ADDRESSES;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.TO_CC_EMAIL_ADDRESSES;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.TO_EMAIL_ADDRESSES;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import com.nucleus.cfi.message.service.WhatsappIntegrationService;
import com.nucleus.cfi.message.vo.GenericMessage;
import com.nucleus.cfi.message.vo.GenericMessageResponse;
import com.nucleus.cfi.message.vo.MessageChannels;
import com.nucleus.cfi.whatsApp.pojo.WhatsAppMessage;
import com.nucleus.cfi.whatsApp.pojo.WhatsAppMessageSendResponse;
import com.nucleus.message.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.exception.VelocityException;
import org.joda.time.DateTime;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.nucleus.NeutrinoUUIDGenerator;
import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.cfi.sms.pojo.SmsMessage;
import com.nucleus.cfi.sms.service.ShortMessageIntegrationService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.barcode.generator.service.BarcodeGeneratorService;
import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.datastore.service.DocumentMetaData;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.currency.Currency;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationGeneratorDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestHistory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.service.CommunicationDataPreparationWrapper;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.CommunicationBlockStatusVerifier;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationErrorLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationEventLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationGenerationCompletionCallback;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationNameService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationConstants;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.finnone.pro.communicationgenerator.util.MoneyFormatConfigurationKey;
import com.nucleus.finnone.pro.communicationgenerator.util.ServiceSelectionCriteria;
import com.nucleus.finnone.pro.communicationgenerator.util.WeakReferencedConfigurationHelper;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGenerationDetailVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGroupCriteriaVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.GeneratedContentVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.MailMessageContentVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.RequestVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.SmsMessageContentVO;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.general.util.CoreDateUtility;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.finnone.pro.general.util.documentgenerator.DocumentGeneratorException;
import com.nucleus.finnone.pro.general.util.documentgenerator.DocxDocumentGenerator;
import com.nucleus.finnone.pro.general.util.documentgenerator.IDocumentGenerator;
import com.nucleus.finnone.pro.general.util.documentgenerator.TableDataVO;
import com.nucleus.finnone.pro.general.util.email.EmailException;
import com.nucleus.finnone.pro.general.util.email.constants.EmailConstatnts;
import com.nucleus.finnone.pro.general.util.sms.SMSException;
import com.nucleus.finnone.pro.general.util.sms.SmsVO;
import com.nucleus.finnone.pro.general.util.templatemerging.TemplateMergingUtility;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.mail.MimeMailMessageBuilder;
import com.nucleus.mail.entity.MailMessageExchangeRecord;
import com.nucleus.mail.entity.MailMessageExchangeRecordHistory;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.money.MoneyService;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.template.TemplateService;
import com.nucleus.xml.parser.XmlParserConfigurer;

@Named("communicationGeneratorBusinessObject")
public class CommunicationGeneratorBusinessObject implements
        ICommunicationGeneratorBusinessObject {

	private static final String ERROR_MESSAGE_COMMUNICATION = "Error in writing communication template file while generating communication";

	@Inject
    @Named("communicationGeneratorDAO")
    private ICommunicationGeneratorDAO communicationGeneratorDAO;

    @Inject
    @Named("communicationGenerationHelper")
    private CommunicationGenerationHelper communicationGenerationHelper;
    
    @Inject
    @Named("communicationDataPreparationBusinessObject")
    private ICommunicationDataPreparationBusinessObject communicationDataPreparationBusinessObject;
    
    @Inject
    @Named("communicationErrorLoggerBusinessObject")
    private ICommunicationErrorLoggerBusinessObject communicationErrorLoggerBusinessObject;
    
    @Inject
    @Named("communicationDataPreparationWrapper")
    private CommunicationDataPreparationWrapper communicationDataPreparationWrapper;
    
    @Inject
    private BeanAccessHelper beanAccessHelper;
    
    @Inject
    @Named("communicationEncryptionBusinessObject")
    private ICommunicationEncryptionBusinessObject communicationEncryptionBusinessObject;
    
    @Inject
    @Named("templateService")
    private TemplateService templateService;    

    @Inject
    @Named("communicationEventLoggerBusinessObject")
    private CommunicationEventLoggerBusinessObject communicationEventLoggerBusinessObject;
    
    @Inject
    @Named("templateMergingUtility")
    private TemplateMergingUtility templateMergingUtility;
    
    @Inject
    @Named("communicationCommonBusinessObject")
    private ICommunicationCommonBusinessObject communicationCommonBusinessObject;
    
    @Inject
    @Named("communicationErrorLoggerService")
    private ICommunicationErrorLoggerService communicationErrorLoggerService;
    
    @Inject
    @Named("mailMessageIntegrationService")
    private MailMessageIntegrationService mailMessageIntegrationService;
    
    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;
    
    @Inject
    @Named("shortMessageIntegrationService")
    private ShortMessageIntegrationService shortMessageIntegrationService;
    
    @Inject
    @Named("moneyService")
    private MoneyService moneyService;
    
    @Inject
    @Named("communicationCacheService")
    private ICommunicationCacheService communicationCacheService;
    
    @Inject
    @Named("mailService")
    private MailService                   mailService;
    
    @Inject
    @Named("smsSendTransactionPostCommitWorker")
    private SmsSendTransactionPostCommitWorker smsSendTransactionPostCommitWorker;

    @Inject
    @Named("couchDataStoreDocumentService")
    private DatastorageService dataStorageService;
    
    @Inject
    @Named("mailSendTransactionPostCommitWorker")
    private MailSendTransactionPostCommitWorker mailSendTransactionPostCommitWorker;
    
    @Inject
    @Named("communicationDocumentGenerator")
    private IDocumentGenerator communicationDocumentGenerator;
    
    @Inject
    @Named("weakReferencedConfigurationHelper")
    private WeakReferencedConfigurationHelper<MoneyFormatConfigurationKey, Map<String, String>> weakCacheForConfigurations;
    
    @Inject
    @Named("barcodeGeneratorService")
    private BarcodeGeneratorService barcodeGeneratorService;
    
    
    
    @Inject
	@Named("communicationTemplateCachePopulator")
	private NeutrinoCachePopulator communicationTemplateCachePopulator;
    
    @Inject
	@Named("communicationNameService")
	private ICommunicationNameService communicationNameService;

	@Inject
	@Named("communicationEventLoggerService")
	private ICommunicationEventLoggerService communicationEventLoggerService;

    @Inject
    @Named("whatsappIntegrationService")
    private WhatsappIntegrationService whatsappIntegrationService;

    private final NeutrinoUUIDGenerator uuidGenerator = new NeutrinoUUIDGenerator(ProductInformationLoader.getProductCode() + "-");
    
    private String templateRootPath = "";
    
    private static final String FROM_ADDRESS = "config.communication.from.address";
    
    private static final String MESSAGE_EXCEPTION = "Message Exception Ocurred";
    private static final String IO_EXCEPTION = "IO Exception Occurred.";
    
    public static final String PRECISION = "PRECISION";
     
    public static final String MULTIPLESOFF = "MULTIPLESOFF";
    
    public static final String CONFIG_DECIMAL_GROUPING_CONSTANT_CONFIG_KEY="config.decimal.grouping.constant";
    
    public static final String CONFIG_DIGIT_GROUPING_CONSTANT_CONFIG_KEY="config.digit.grouping.constant";
    
    public static final String CONFIG_AMOUNT_FORMAT_CONSTANT_CONFIG_KEY="config.amount.format.constant";
    
    private static final String UUID_SEPARATOR ="-";
	
    private static final String DELIVERED = "delivered";

    private static final String READ = "read";
    
    private static final String FAILED = "failed";
    
    private static final String RETRY_EXCEEDED = "retryExceeded";
    
    private static final String[] EMPTY_STRING_ARRAY = new String[]{};
    
   private static final Map<Long, DateTime>  communicationTemplateLastWrittenToDiskTimeStampMap = new ConcurrentHashMap<>();
   private Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
   private static final Map<Long,String> communicationTemplateToFileMap = new ConcurrentHashMap<>();
    
	private Map<String, ICommunicationGenerationCompletionCallback> callbackServiceBeansMap;
       
    public void setTemplateRootPath(String templateRootPath) {
        this.templateRootPath = templateRootPath;
    }
    /**
     * updates the value of parameters in report data map and image map based on
     * the values maintained in Parameters master.
     * 
     * @param communicationParameters
     * @param reportDataMap
     * @param reportImageMap
     */
    @Override
    public void updateParameterValuesByParameterMaster(
            List<CommunicationParameter> communicationParameters,
            Map<String, Object> reportDataMap,
            Map<String, String> reportImageMap) {
        for (CommunicationParameter communicationParameter : communicationParameters) {
            updateDataOrImageMap(communicationParameter, reportDataMap,
                    reportImageMap, communicationParameter.getParameterValue());
        }
    }

    /**
     * checks which map is to get update and then updates it-- convenience
     * method
     * 
     * @param communicationParameter
     * @param reportDataMap
     * @param reportImageMap
     */
    protected void updateDataOrImageMap(
            CommunicationParameter communicationParameter,
            Map<String, Object> reportDataMap,
            Map<String, String> reportImageMap, String parameterValue) {
        if (communicationParameter.getEntityLifeCycleData()
                .getSystemModifiableOnly()
                && communicationParameter.getIsImage()
                && (reportImageMap
                        .get(communicationParameter.getParameterCode())==null
                || !"".equals(reportImageMap
                        .get(communicationParameter.getParameterCode())))) {
            reportImageMap.put(communicationParameter.getParameterCode(),
                    parameterValue);
        } else if (communicationParameter.getEntityLifeCycleData()
                .getSystemModifiableOnly()
                && reportDataMap.get(communicationParameter.getParameterCode()) == null) {
            reportDataMap.put(communicationParameter.getParameterCode(),
                    parameterValue);
        }
    }

    /**
     * accepts list of all communication groups and criteria attributes and
     * returns the applicable communication group.
     * 
     * @param communicationGroups
     * @param communicationGroupCriteriaVO
     * @return CommunicationGroup
     */
    

    @Override
    public List<DataPreparationServiceMethodVO> findAdditionalMethodsForCommunicationDataPreperation(
            String communicationCode) {
        DataPreparationServiceMethodVO commVO = null;
        List<DataPreparationServiceMethodVO> communicationVOList = new ArrayList<>();
        for (CommunicationParameter communicationParameter: communicationGeneratorDAO
                .findAdditionalMethodsForCommunicationDataPreperation(communicationCode)) {
            
			if (communicationParameter.getServiceInterfaceName() != null && communicationParameter.getBeanId() != null
					&& communicationParameter.getMethodName() != null) {
					commVO = new DataPreparationServiceMethodVO();
					commVO.setServiceInterfaceName(communicationParameter.getServiceInterfaceName());
					commVO.setTargetServiceName(communicationParameter.getBeanId());
					commVO.setTargetMethodName(communicationParameter.getMethodName());
					communicationVOList.add(commVO);
				}
            
        }
        return communicationVOList;
    }

    @Override
    public List<CommunicationTemplate> getTemplateByCommunicationMasterId(
            Long communicationMasterId) {
        return communicationGeneratorDAO
                .getTemplateByCommunicationMasterId(communicationMasterId);
    }


    @Override
    public List<Object[]> getAttributeValueForGenericParameter(
            Class<? extends GenericParameter> entityClass, String columnName) {
        return communicationGeneratorDAO.getAttributeValueForGenericParameter(
                entityClass, columnName);
    }

    @Override
    public List<Object[]> getAttributeValueForBaseMasterEntity(
            Class<? extends BaseMasterEntity> entityClass, String columnName,
            String dependentColumn, Long dependentColumnValue) {

        return communicationGeneratorDAO.getAttributeValueForBaseMasterEntity(
                entityClass, columnName, dependentColumn, dependentColumnValue);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public CommunicationEventRequestLog markCommEventRequestComplete(
            CommunicationEventRequestLog communicationEventRequestLog){
        communicationEventRequestLog.setStatus(STATUS_COMPLETED);
        return communicationGeneratorDAO.markCommEventRequestComplete(communicationEventRequestLog);
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void moveCommunicationEventRequestToHistory(
            CommunicationEventRequestLog communicationEventRequestLog){
        CommunicationEventRequestHistory communicationEventRequestHistory=communicationGenerationHelper.prepareCommEventRequestHistoryData(communicationEventRequestLog);
        communicationGeneratorDAO.saveCommunicationEventRequestHistory(communicationEventRequestHistory);
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void deleteCommunicationEventRequest(
            CommunicationEventRequestLog communicationEventRequestLog){
        communicationGeneratorDAO.deleteCommunicationEventRequest(communicationEventRequestLog);
    }

    @Override
    public List<CommunicationDataPreparationDetail> getActiveApprovedDetailBasedOnServiceSouceAndModule(
            SourceProduct module, Long serviceSelectionId) {
        return communicationGeneratorDAO.getActiveApprovedDetailBasedOnServiceSouceAndModule(module, serviceSelectionId);
    }

    @Override
    public List<CommunicationRequestDetail> getCommunicationGenerationDetail(
            CommunicationGenerationDetailVO communicationGenerationDetailVO) {
        return communicationGeneratorDAO.getCommunicationGenerationDetail(communicationGenerationDetailVO,0,0);
    }
    
    @Override
    public Boolean checkIfRetriedAttempstExhausted(
            CommunicationRequestDetail communicationRequestDetail,
            Map<String, Integer> retryAttemptConfigurations,CommunicationName communicationName) {
        Integer retrialAttemptsMade = communicationRequestDetail
                .getRetriedAttemptsDone();
        if (isNull(communicationName)
                || isNull(communicationName.getCommunicationType())
                || isNull(communicationName.getCommunicationType().getCode())) {
            return false;
        }
        String communicationTypeCode = communicationName.getCommunicationType().getCode();
        if ((CommunicationType.LETTER.equals(communicationTypeCode) && retrialAttemptsMade
                .compareTo(retryAttemptConfigurations
                        .get(CommunicationGeneratorConstants.LETTER_RETRY_ATTEMPT_CONFIG_KEY)) >= 0)
                || (CommunicationType.SMS.equals(communicationTypeCode) && retrialAttemptsMade
                        .compareTo(retryAttemptConfigurations
                                .get(CommunicationGeneratorConstants.SMS_RETRY_ATTEMPT_CONFIG_KEY)) >= 0)
                || (CommunicationType.EMAIL.equals(communicationTypeCode) && retrialAttemptsMade
                        .compareTo(retryAttemptConfigurations
                                .get(CommunicationGeneratorConstants.EMAIL_RETRY_ATTEMPT_CONFIG_KEY)) >= 0)
                ||  (CommunicationType.WHATSAPP.equals(communicationTypeCode) && retrialAttemptsMade
                .compareTo(retryAttemptConfigurations
                        .get(CommunicationGeneratorConstants.WHATSAPP_RETRY_ATTEMPT_CONFIG_KEY)) >= 0)) {
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
	public void deleteGeneratedCommunicationRequest(CommunicationRequestDetail communicationRequestDetail){

       communicationGeneratorDAO.deleteGeneratedCommunicationRequest(communicationRequestDetail);
    }
    
    

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public CommunicationGenerationDetailHistory moveCommunicationRequestToHistory(CommunicationGenerationDetailHistory communicationGenerationDetailHistory) {
        communicationGeneratorDAO.persist(communicationGenerationDetailHistory);
        return communicationGenerationDetailHistory;
    }

    public CommunicationGenerationDetailHistory prepareCommunicationRequestHistory(CommunicationRequestDetail communicationRequestDetail, Map<String, Object> additionalDataToBeUpdatedInHistory){
        CommunicationGenerationDetailHistory communicationGenerationDetailHistory=new CommunicationGenerationDetailHistory();
        communicationGenerationDetailHistory.setCommunicationCode(communicationRequestDetail.getCommunicationCode());
        communicationGenerationDetailHistory.setSubjectURI(communicationRequestDetail.getSubjectURI());
        communicationGenerationDetailHistory.setSubjectReferenceNumber(communicationRequestDetail.getSubjectReferenceNumber());
        communicationGenerationDetailHistory.setSubjectReferenceType(communicationRequestDetail.getSubjectReferenceType());
        communicationGenerationDetailHistory.setCommunicationTemplateId(communicationRequestDetail.getCommunicationTemplateId());
        communicationGenerationDetailHistory.setCommunicationEventCode(communicationRequestDetail.getCommunicationEventCode());
        communicationGenerationDetailHistory.setRetriedAttemptsDone(communicationRequestDetail.getRetriedAttemptsDone());
        communicationGenerationDetailHistory.setCommunicationTemplateCode(communicationRequestDetail.getCommunicationTemplateCode());
        communicationGenerationDetailHistory.setMakeBusinessDate(communicationRequestDetail.getMakeBusinessDate());
        communicationGenerationDetailHistory.setAuthorizationBusinessDate(communicationRequestDetail.getAuthorizationBusinessDate());
        communicationGenerationDetailHistory.setRegenerationReasonCode(communicationRequestDetail.getRegenerationReasonCode());
        communicationGenerationDetailHistory.setIssueReissueFlag(communicationRequestDetail.getIssueReissueFlag());
        communicationGenerationDetailHistory.setProcessDate(communicationRequestDetail.getProcessDate());
        communicationGenerationDetailHistory.setCommunicationText(communicationRequestDetail.getCommunicationText());
        communicationGenerationDetailHistory.setPhoneNumber(communicationRequestDetail.getPhoneNumber());
        communicationGenerationDetailHistory.setAlternatePhoneNumber(communicationRequestDetail.getAlternatePhoneNumber());
        communicationGenerationDetailHistory.setPrimaryEmailAddress(communicationRequestDetail.getPrimaryEmailAddress());
        communicationGenerationDetailHistory.setCcEmailAddress(communicationRequestDetail.getCcEmailAddress());
        communicationGenerationDetailHistory.setBccEmailAddress(communicationRequestDetail.getBccEmailAddress());
        communicationGenerationDetailHistory.getEntityLifeCycleData().setCreatedByUri(communicationRequestDetail.getEntityLifeCycleData().getCreatedByUri());
        communicationGenerationDetailHistory.setAdditionalFieldTxnId(notNull(communicationRequestDetail.getAdditionalData())?communicationRequestDetail.getAdditionalData().getId():null);        
        communicationGenerationDetailHistory.setApplicablePrimaryEntityURI(communicationRequestDetail.getApplicablePrimaryEntityURI());
        communicationGenerationDetailHistory.setEventLogTimeStamp(communicationRequestDetail.getEventLogTimeStamp());
        communicationGenerationDetailHistory.setSourceProduct(communicationRequestDetail.getSourceProduct());
        communicationGenerationDetailHistory.setStatus(communicationRequestDetail.getStatus());
        communicationGenerationDetailHistory.setSubjectId(communicationRequestDetail.getSubjectId());
        communicationGenerationDetailHistory.setApplicablePrimaryEntityId(communicationRequestDetail.getApplicablePrimaryEntityId());
        communicationGenerationDetailHistory.setReferenceDate(communicationRequestDetail.getReferenceDate());
        communicationGenerationDetailHistory.setRequestReferenceId(communicationRequestDetail.getRequestReferenceId());
        communicationGenerationDetailHistory.setGenerateMergedFile(communicationRequestDetail.getGenerateMergedFile());
        communicationGenerationDetailHistory.setUniqueRequestId(communicationRequestDetail.getUniqueRequestId());
        communicationGenerationDetailHistory.setBarcodeReferenceNumber(communicationRequestDetail.getBarcodeReferenceNumber());
        communicationGenerationDetailHistory.setEventRequestLogId(communicationRequestDetail.getEventRequestLogId());
        communicationGenerationDetailHistory.setLetterStorageId(communicationRequestDetail.getLetterStorageId());
        communicationGenerationDetailHistory.setPreviewFlag(communicationRequestDetail.getPreviewFlag() == null ? Boolean.FALSE : communicationRequestDetail.getPreviewFlag());
        communicationGenerationDetailHistory.setDeliveryPriority(communicationRequestDetail.getDeliveryPriority());
        communicationGenerationDetailHistory.setRequestType(communicationRequestDetail.getRequestType());
        communicationGenerationDetailHistory.setAttachmentName(communicationRequestDetail.getAttachmentName());
        communicationGenerationDetailHistory.setJsonAdditionalField1(communicationRequestDetail.getJsonAdditionalField1());
        communicationGenerationDetailHistory.setJsonAdditionalField2(communicationRequestDetail.getJsonAdditionalField2());
        communicationGenerationDetailHistory.setJsonAdditionalField3(communicationRequestDetail.getJsonAdditionalField3());
        updateAdditionalDataInHistory(communicationGenerationDetailHistory, additionalDataToBeUpdatedInHistory);
        return communicationGenerationDetailHistory;
    }
    
  public void updateAdditionalDataInHistory(CommunicationGenerationDetailHistory communicationGenerationDetailHistory, Map<String, Object> additionalDataToBeUpdatedInHistory) {
    if (ValidatorUtils.hasAnyEntry(additionalDataToBeUpdatedInHistory)) {
      Character status = ValidatorUtils.notNull(additionalDataToBeUpdatedInHistory.get(CommunicationGeneratorConstants.STATUS)) ? (Character) additionalDataToBeUpdatedInHistory
          .get(CommunicationGeneratorConstants.STATUS) : null;
      String communicationText = ValidatorUtils.notNull(additionalDataToBeUpdatedInHistory.get(CommunicationGeneratorConstants.COMMUNICATION_TEXT)) ? String.valueOf(additionalDataToBeUpdatedInHistory
          .get(CommunicationGeneratorConstants.COMMUNICATION_TEXT)) : null;
      if (ValidatorUtils.notNull(status)) {
    	  communicationGenerationDetailHistory.setStatus(status);
      }
      if (ValidatorUtils.notNull(communicationText)) {
    	  communicationGenerationDetailHistory.setCommunicationText(communicationText);
      }
    }
  }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void moveRequestToHistoryAndDeleteGeneratedRequestInNewTransaction(CommunicationRequestDetail communicationRequestDetail, Map<String, Object> additionalDataToBeUpdatedInHistory){
        CommunicationGenerationDetailHistory communicationGenerationDetailHistory = prepareCommunicationRequestHistory(communicationRequestDetail, additionalDataToBeUpdatedInHistory);
        moveCommunicationRequestToHistory(communicationGenerationDetailHistory);
        deleteGeneratedCommunicationRequest(communicationRequestDetail);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void moveRequestToHistoryAndDeleteGeneratedRequest(CommunicationRequestDetail communicationRequestDetail, Map<String, Object> additionalDataToBeUpdatedInHistory){
        CommunicationGenerationDetailHistory parentCommunicationGenerationDetailHistory = prepareCommunicationRequestHistory(communicationRequestDetail, additionalDataToBeUpdatedInHistory);

        moveCommunicationRequestToHistory(parentCommunicationGenerationDetailHistory);

        List<CommunicationRequestDetail> attachments = getAttachmentsForEmail(communicationRequestDetail.getId());

        if (CollectionUtils.isEmpty(attachments)) {
            for (CommunicationRequestDetail attachment : attachments) {
                CommunicationGenerationDetailHistory attachedHistoryRecord = prepareCommunicationRequestHistory(attachment,
                        null);
                attachedHistoryRecord
                        .setParentCommunicationGenerationDetailHistory(parentCommunicationGenerationDetailHistory);
                attachedHistoryRecord.setParentCommunicationGenerationDetailHistoryId(
                        parentCommunicationGenerationDetailHistory.getId());
                moveCommunicationRequestToHistory(attachedHistoryRecord);
                deleteGeneratedCommunicationRequest(attachment);

                updateCallbackHandlerOnCommunicationGenerationDetailHistoryCompletion(attachedHistoryRecord);

            }

        }

        deleteGeneratedCommunicationRequest(communicationRequestDetail);

        updateCallbackHandlerOnCommunicationGenerationDetailHistoryCompletion(parentCommunicationGenerationDetailHistory);
    }

    
    
    private void updateCallbackHandlerOnCommunicationGenerationDetailHistoryCompletion(CommunicationGenerationDetailHistory communicationGenerationDetailHistory) {
        CommunicationGenerationDetailVO communicationGenerationDetailVO = prepareCommunicationGenerationDetailVO(communicationGenerationDetailHistory);
        if (callbackServiceBeansMap == null) {
            callbackServiceBeansMap = NeutrinoSpringAppContextUtil.getBeansOfType(ICommunicationGenerationCompletionCallback.class);
        }
        for(ICommunicationGenerationCompletionCallback callbackServiceBean:callbackServiceBeansMap.values()){
            if (communicationGenerationDetailVO.getStatus() != null && communicationGenerationDetailVO.getStatus().equals(CommunicationRequestDetail.COMPLETED)) {
                callbackServiceBean.communicationGenerationHistoryDetailOnSucess(communicationGenerationDetailVO);
            }else if(communicationGenerationDetailVO.getStatus() != null && communicationGenerationDetailVO.getStatus().equals(CommunicationRequestDetail.FAILED)){
                callbackServiceBean.communicationGenerationHistoryDetailOnFailure(communicationGenerationDetailVO);
            }
        }    
        
    }
    
    private CommunicationGenerationDetailVO prepareCommunicationGenerationDetailVO(CommunicationGenerationDetailHistory communicationGenerationDetailHistory) {
        
        CommunicationGenerationDetailVO communicationGenerationDetailVO = new CommunicationGenerationDetailVO();
        communicationGenerationDetailVO.setApplicablePrimaryEntityId(communicationGenerationDetailHistory.getApplicablePrimaryEntityId());
        communicationGenerationDetailVO.setCommunicationCode(communicationGenerationDetailHistory.getCommunicationCode());
        communicationGenerationDetailVO.setCommunicationCode(communicationGenerationDetailHistory.getCommunicationCode());
        communicationGenerationDetailVO.setEventCode(communicationGenerationDetailHistory.getCommunicationEventCode());
        communicationGenerationDetailVO.setStatus(communicationGenerationDetailHistory.getStatus());
        communicationGenerationDetailVO.setSubjectEntityId(communicationGenerationDetailHistory.getSubjectId());
        communicationGenerationDetailVO.setSubjectURI(communicationGenerationDetailHistory.getSubjectURI());        
        communicationGenerationDetailVO.setRequestType(communicationGenerationDetailHistory.getRequestType());
        communicationGenerationDetailVO.setSubjectReferenceNumber(communicationGenerationDetailHistory.getSubjectReferenceNumber());
        communicationGenerationDetailVO.setSubjectReferenceType(communicationGenerationDetailHistory.getSubjectReferenceType());
        communicationGenerationDetailVO.setAttachmentName(communicationGenerationDetailHistory.getAttachmentName());
        communicationGenerationDetailVO.setEventRequestLogId(communicationGenerationDetailHistory.getEventRequestLogId());

        
        return communicationGenerationDetailVO;
    }
    
	@Override
	@Transactional(noRollbackFor = { SystemException.class, ServiceInputException.class, BusinessException.class,
			BaseException.class, NullPointerException.class })
	public boolean checkIfCommunicationGenerationAllowed(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> contextMap,Map<String, Object> localCacheMap) {
        boolean communicationGenerationAllowed=true;
		CommunicationDataPreparationDetail communicationDataPreparationDetail = communicationDataPreparationBusinessObject
				.getCommunicationPreparationDetail(ServiceSelectionCriteria.COMMUNICATION_BLOCK_STATUS_CHECK,
						communicationRequestDetail.getSourceProduct());
        if(notNull(communicationDataPreparationDetail)){
            String className=communicationDataPreparationDetail.getClassName();
            if(StringUtils.isEmpty(className)){
                Message message = new Message(
                        CommunicationGeneratorConstants.CLASS_NAME_NULL,
                        Message.MessageType.ERROR,
                        communicationDataPreparationDetail.getUri());
                  throw ExceptionBuilder
                .getInstance(SystemException.class)
                .setMessage(message)
                .setSubjectId(communicationRequestDetail.getSubjectId())
                .setSubjectReferenceNumber(communicationRequestDetail.getSubjectReferenceNumber())
                .setSeverity(
                        ExceptionSeverityEnum.SEVERITY_MEDIUM
                                .getEnumValue()).build();
            }
              
            try {
                String beanId = communicationDataPreparationDetail.getBeanId();
                if(StringUtils.isEmpty(beanId)){
                    Message message = new Message(
                            CommunicationGeneratorConstants.BEAN_ID_NULL,
                            Message.MessageType.ERROR,
                            communicationDataPreparationDetail.getUri());
                    
                      throw ExceptionBuilder
                    .getInstance(SystemException.class)
                    .setMessage(message)
                    .setSubjectId(communicationRequestDetail.getSubjectId())
                    .setSubjectReferenceNumber(communicationRequestDetail.getSubjectReferenceNumber())
                    .setSeverity(
                            ExceptionSeverityEnum.SEVERITY_MEDIUM
                                    .getEnumValue()).build();
                            
                        }
                Object beanObject = null;        
                Class<?> targetBeanClass = classCache.get(className);
    			if (targetBeanClass == null) {
    				targetBeanClass = Class.forName(className);
    				classCache.put(className, targetBeanClass);
    			}
                if(!CommunicationBlockStatusVerifier.class.isAssignableFrom(targetBeanClass)){
                    
                    Message message = new Message(
                            CommunicationGeneratorConstants.CLASS_NOT_OF_TYPE_DATA_PREP_DETAIL,
                            Message.MessageType.ERROR,
                            targetBeanClass.getName(),String.valueOf(CommunicationBlockStatusVerifier.class));
                    
                      throw ExceptionBuilder
                    .getInstance(SystemException.class)
                    .setMessage(message)
                    .setSubjectId(communicationRequestDetail.getSubjectId())
                    .setSubjectReferenceNumber(communicationRequestDetail.getSubjectReferenceNumber())
                    .setSeverity(
                            ExceptionSeverityEnum.SEVERITY_MEDIUM
                                    .getEnumValue()).build();
                    
                }
                String subjectEntitySimpleClassName = EntityId.fromUri(communicationRequestDetail.getSubjectURI()).getEntityClass().getSimpleName();
                
                String applicablePrimaryEntitySimpleClassName = null;
                if(notNull(communicationRequestDetail.getApplicablePrimaryEntityURI())){
                    applicablePrimaryEntitySimpleClassName=EntityId.fromUri(communicationRequestDetail.getApplicablePrimaryEntityURI()).getEntityClass().getSimpleName();
                }
                beanObject = beanAccessHelper.getBean(beanId, targetBeanClass);	
                communicationGenerationAllowed = ((CommunicationBlockStatusVerifier) beanObject)
                    .isCommunicationGenerationAllowed(
                            communicationRequestDetail
                                    .getCommunicationCode(),
                            (BaseEntity) contextMap
                                    .get(subjectEntitySimpleClassName),
                            (BaseEntity) contextMap
                                    .get(applicablePrimaryEntitySimpleClassName),localCacheMap);
                
                
            
            } catch (ClassNotFoundException e) {
                Message message = new Message(CommunicationGeneratorConstants.CLASS_NOT_FOUND_DATA_PREP_DETAIL,
                        Message.MessageType.ERROR,className);                        
                throw ExceptionBuilder
                        .getInstance(SystemException.class)
                        .setMessage(message)
                        .setSubjectId(communicationRequestDetail.getSubjectId())
                        .setSubjectReferenceNumber(communicationRequestDetail.getSubjectReferenceNumber())
                        .setOriginalException(e)
                        .setSeverity(
                                ExceptionSeverityEnum.SEVERITY_MEDIUM
                                        .getEnumValue())
                        .build();
            }
        }
        return communicationGenerationAllowed;
    }

        
    @Override
    public void prepareDataAndLogError(List<Message> errorMessages,CommunicationRequestDetail communicationRequestDetail) {
        List<CommunicationErrorLogDetail> communicationErrorLogDetail=communicationDataPreparationWrapper.prepareErrorLogData(errorMessages,communicationRequestDetail);
        communicationErrorLoggerBusinessObject.createCommunicationProcessErrorLoggerDetail(communicationErrorLogDetail);
    }
    
    @Override
    @Transactional(readOnly = true, noRollbackFor = { SMSException.class,
            NullPointerException.class, BusinessException.class,
            SystemException.class, ServiceInputException.class,
            DocumentGeneratorException.class, BaseException.class,
            VelocityException.class })
    public GeneratedContentVO generateCommunication(CommunicationGroupCriteriaVO communicationGroupCriteriaVO) {

        Map<String, Object> dataMap = new HashMap<>();
        Map<String, String> imageMap = new HashMap<>();
        List<TableDataVO> tableDataVOListToUpdate = new ArrayList<>();
        List<String> variableListToUpdate = new ArrayList<>();
        Map <String,Object> amountFormatMap =new HashMap<>();
        GeneratedContentVO contentVO = new GeneratedContentVO();
        if ((isNull(amountFormatMap.get(PRECISION))) || (isNull(amountFormatMap.get(MULTIPLESOFF)))) {
        	Map<String, String> cachedConfiguration = weakCacheForConfigurations.getFromConfigurations(
        			new MoneyFormatConfigurationKey(communicationGroupCriteriaVO.getSchedularInstanceId()));
            amountFormatMap = moneyService.fetchAmountDetails((Currency)communicationGroupCriteriaVO.getDataMap().get(APPLICABLE_CURRENCY), cachedConfiguration);
        }
        CommunicationTemplate communicationTemplate = communicationGroupCriteriaVO.getCommunicationTemplate();
        
		CommunicationName communicationName = communicationCacheService
				.getCommunicationName(communicationTemplate.getCommunicationMasterId());
		updateDataMap(dataMap, communicationGroupCriteriaVO.getDataMap(),
                communicationTemplate.getCommunication(),amountFormatMap);
		communicationEncryptionBusinessObject.encryptAttachments(communicationName, communicationTemplate, dataMap,
				communicationGroupCriteriaVO);
        if (!(CollectionUtils.isEmpty(communicationGroupCriteriaVO.getTableDataVOList()))
                && !(CollectionUtils.isEmpty(communicationGroupCriteriaVO.getVariableList()))) {
            updateTableMap(tableDataVOListToUpdate, variableListToUpdate,
                    communicationGroupCriteriaVO.getTableDataVOList(),
                    communicationGroupCriteriaVO.getVariableList(),
                    communicationName,amountFormatMap);
        }
        updateParameterValuesByParameterMaster(communicationTemplate
                        .getCommunication().getCommunicationParameters(),
                        dataMap, imageMap);
        updateImagePath(imageMap, templateRootPath);

        communicationTemplate = downloadTemplateIfRequired(communicationTemplate);
        
        if (CommunicationType.LETTER.equals(communicationTemplate
                .getCommunication().getCommunicationType().getCode())) {        	
        	updateImagePathForBarcodeImage(imageMap,communicationGroupCriteriaVO.getBarcodeReferenceNumber(),contentVO);
        	
            communicationDocumentGenerator.setTemplatePath(templateRootPath
                    + communicationTemplate.getCommunicationTemplateFile());
            try {
                XmlParserConfigurer.enablePooling();
                if ((!CollectionUtils.isEmpty(variableListToUpdate)) && (!CollectionUtils.isEmpty(tableDataVOListToUpdate))) {
                    contentVO.setGeneratedContent(communicationDocumentGenerator
                            .getPDFOutput(dataMap, imageMap,
                                    tableDataVOListToUpdate, variableListToUpdate));
                } else {
                    contentVO.setGeneratedContent(communicationDocumentGenerator
                            .getPDFOutput(dataMap, imageMap));
                }
            } finally {
                XmlParserConfigurer.releaseObjects();
            }
        } else if (CommunicationType.SMS.equals(communicationTemplate
                .getCommunication().getCommunicationType().getCode())
                || CommunicationType.EMAIL.equals(communicationTemplate
                        .getCommunication().getCommunicationType().getCode())
                || CommunicationType.WHATSAPP.equals(communicationTemplate
                .getCommunication().getCommunicationType().getCode())) {

            try {
            	contentVO.setGeneratedText(templateMergingUtility
                    .mergeTemplateIntoString(communicationTemplate
                            .getCommunicationTemplateFile(), dataMap));
                                   
            }
            catch (Exception e) {
                Message errorMessage=new Message(CommunicationGeneratorConstants.ERROR_IN_MERGING_TEMPLATE,
                        Message.MessageType.ERROR,communicationGroupCriteriaVO.getCommunicationCode());
            
                BaseLoggers.flowLogger.debug(errorMessage.getI18nCode(),e);
            }
        }
        communicationGroupCriteriaVO.setProcessedDataMap(dataMap);
        contentVO.setLocation(communicationTemplate.getCommunication()
                .getLocation());
        return contentVO;
    }
    
    /**
     * This method compares timestamp of template and timestamp of latest template from custom cache and then downloads template
     * at server location if required
     * @param communicationTemplate
     * @return returns template with updated path
     */
	private CommunicationTemplate downloadTemplateIfRequired(CommunicationTemplate communicationTemplate) {
		if(communicationTemplate.getUploadedDocumentId() != null) {
			DateTime lastWrittenToDiskTimeStamp  = communicationTemplateLastWrittenToDiskTimeStampMap.get(communicationTemplate.getId());
			CommunicationTemplate communicationTemplateFromCache = (CommunicationTemplate)communicationTemplateCachePopulator.get(communicationTemplate.getId());
			DateTime timeStamptoCompare = null;
			if(communicationTemplateFromCache.getEntityLifeCycleData().getLastUpdatedTimeStamp() != null) {
				timeStamptoCompare = communicationTemplateFromCache.getEntityLifeCycleData().getLastUpdatedTimeStamp();
			}else {
				timeStamptoCompare = communicationTemplateFromCache.getEntityLifeCycleData().getCreationTimeStamp();
			}
			if(lastWrittenToDiskTimeStamp!=null && timeStamptoCompare == null) {
				communicationTemplate.setCommunicationTemplateFile(communicationTemplateToFileMap.get(communicationTemplate.getId()));
				return communicationTemplate;
			}
			if (lastWrittenToDiskTimeStamp == null
					|| lastWrittenToDiskTimeStamp.isBefore(timeStamptoCompare)
					|| !Files.exists(
							Paths.get(templateRootPath, communicationTemplate.getCommunicationTemplateFile()))) {
				communicationTemplate = downloadTemplateFileAtServerLocation(communicationTemplate);
			}

		}
		return communicationTemplate;
	}
	private CommunicationTemplate downloadTemplateFileAtServerLocation(CommunicationTemplate communicationTemplate) {
		if (StringUtils.isEmpty(communicationTemplate.getUploadedDocumentId())) {
			return communicationTemplate;
		}
		DocumentMetaData documentMetaData = communicationNameService.getTemplateFromStorageService(communicationTemplate.getUploadedDocumentId());
		String templateFile = new StringBuilder(documentMetaData.getFileName()).append(".").append(documentMetaData.getFileExtension()).toString();
		String relativePath = new StringBuilder(File.separator).append(communicationTemplate.getCommunicationTemplateCode()).append(File.separator).append(templateFile).toString();
		String templateFilePath = new StringBuilder(templateRootPath).append(relativePath).toString();
		communicationTemplateToFileMap.put(communicationTemplate.getCommunicationMasterId(), relativePath);
		byte[] templateContent  =documentMetaData.getContent();
		try {
			Path pathToFile = Paths.get(templateFilePath);
			Files.createDirectories(pathToFile.getParent());
			Files.write(pathToFile, templateContent);
			communicationTemplate.setCommunicationTemplateFile(relativePath);
			communicationTemplateLastWrittenToDiskTimeStampMap.put(communicationTemplate.getId(), DateTime.now());
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.error(CommunicationGeneratorBusinessObject.ERROR_MESSAGE_COMMUNICATION, e);
			throw new SystemException(CommunicationGeneratorBusinessObject.ERROR_MESSAGE_COMMUNICATION, e);
		}
		return communicationTemplate;
	}
	
	
	private void updateImagePathForBarcodeImage(Map<String, String> imageMap, String barcodeReferenceNumber, GeneratedContentVO contentVO) {
		if(imageMap.containsKey(DocxDocumentGenerator.BARCODE_IMAGE_PARAMETER_CODE)){
			imageMap.put(DocxDocumentGenerator.BARCODE_IMAGE_PARAMETER_CODE,barcodeReferenceNumber);
			contentVO.setBarcodeImageAttached(true);
    	}
	}
	protected void updateTableMap(List<TableDataVO> tableDataVOListToUpdate, List<String> variableListToUpdate,
			List<TableDataVO> tableDataVOList, List<String> variableList, CommunicationName communication,
			Map<String, Object> amountFormatMap) {
		Map<String,CommunicationParameter> communicationParameterMap = new HashMap<>();
		for (CommunicationParameter communicationParameter : communication.getCommunicationParameters()) {
			if (variableList != null) {
				for (String variable : variableList) {
					if (variable.equals(communicationParameter.getSourceKey())) {
						variableListToUpdate.add(variable);
						communicationParameterMap.put(variable, communicationParameter);

					}
				}
			}
		}

		for (TableDataVO tableDataVO : tableDataVOList) {
			TableDataVO tempTableDataVO = new TableDataVO();
			tempTableDataVO.setTableKey(tableDataVO.getTableKey());
			formatTableDataVO(tableDataVO.getTableData(), amountFormatMap, tempTableDataVO,communicationParameterMap);
			tableDataVOListToUpdate.add(tempTableDataVO);
		}

	}

	private TableDataVO formatTableDataVO(List<Map<String, Object>> listOfTableDataMap,
			Map<String, Object> amountFormatMap, TableDataVO tempTableDataVO,Map<String,CommunicationParameter> communicationParameterMap) {

		tempTableDataVO.setTableData(new ArrayList<Map<String, Object>>());
		for (Map<String, Object> tableData : listOfTableDataMap) {
			Map<String, Object> tableDataMap = formatTableDataMap(tableData, amountFormatMap,
					tempTableDataVO.getTableKey(),communicationParameterMap);
			tempTableDataVO.getTableData().add(tableDataMap);

		}
		return tempTableDataVO;
	}

	private Map<String, Object> formatTableDataMap(Map<String, Object> tableDataMap,
			Map<String, Object> amountFormatMap, String tableKey, Map<String, CommunicationParameter> communicationParameterMap) {
		Map<String, Object> tempDataMap = new HashMap<>();
		String tableDataKey = tableKey +".";
		for (Map.Entry<String, Object> tableDataEntry : tableDataMap.entrySet()) {
			String key = tableDataEntry.getKey();
			CommunicationParameter communicationParameter = communicationParameterMap.get(tableDataKey + key);
			if(ValidatorUtils.notNull(communicationParameter)) {
			tempDataMap.put(key,
					checkFieldTypeAndDoFormatting(communicationParameter, tableDataEntry.getValue(), amountFormatMap));
			}
			else {
				tempDataMap.put(key, tableDataEntry.getValue());
			}
		}
		return tempDataMap;
	}

    /**
* 
*/

    protected void updateImagePath(Map<String, String> reportImageMap,
            String imageRootPath) {
        for (Map.Entry<String, String> entry : reportImageMap.entrySet()) {
        	entry.setValue(imageRootPath + entry.getValue());
        }
    }

    /**
     * updates the
     * 
     * @param reportDataMap
     * @param contentMap
     * @param communication
     */
    protected void updateDataMap(Map<String, Object> reportDataMap,
            Map<String, Object> contentMap, CommunicationName communication, Map<String,Object> amountFormatMap) {
        BeanWrapper currentObjectBeanWrapper = null;
        for (CommunicationParameter communicationParameter : communication
                .getCommunicationParameters()) {
            Object value = null;

            if (communicationParameter.getParameterSource() != null) {
                if (contentMap.get(communicationParameter.getSourceKey()) != null) {
                    currentObjectBeanWrapper = new BeanWrapperImpl(
                            contentMap.get(communicationParameter
                                    .getSourceKey()));
                    try {
                        value = currentObjectBeanWrapper
                                .getPropertyValue(communicationParameter
                                        .getParameterSource());
                    } catch (Exception e) {
                        
                        BaseLoggers.flowLogger.debug(communicationParameter
                                .getParameterSource()
                                + " not found in "
                                + communicationParameter.getSourceKey());
                    }
                }

            } else {
                value = contentMap.get(communicationParameter.getSourceKey());
            }

            if (value != null) {
                reportDataMap.put(
                        communicationParameter.getParameterCode(),
                        checkFieldTypeAndDoFormatting(communicationParameter,
                                value,amountFormatMap));
            }

        }
    }

    protected Object checkFieldTypeAndDoFormatting(
            CommunicationParameter communicationParameter, Object value,Map <String,Object> amountFormatMap) {
        if (CommunicationGeneratorConstants.FIELD_TYPE_DATE
                .equals(communicationParameter.getFieldType())
                && communicationParameter.getFormatMask() != null) {
            try {
            	if (isNull(value)) {
            		return null;
            	}
                if (value instanceof Date) {
                    return CoreDateUtility.formatDateAsString((Date) value,
                            communicationParameter.getFormatMask());
                } else {
              Date date=  	CoreDateUtility.getDateFromString( String.valueOf(value),communicationParameter.getFormatMask());
                	
                    return  isNull(date)?null:date.toString();
                }
            } catch (Exception e) {
                Message message = new Message(CommunicationGeneratorConstants.COMMN_PARAM_DATE,
                        Message.MessageType.ERROR,communicationParameter.getParameterDesc());
                throw ExceptionBuilder.getInstance(SystemException.class).setMessage(message).setOriginalException(e).setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();

            }
        } else if (CommunicationGeneratorConstants.FIELD_TYPE_AMOUNT
                .equals(communicationParameter.getFieldType())) {
            try {                
            	return moneyService.roundAndFormatNumber(new BigDecimal(value.toString()), amountFormatMap ,RoundingMode.HALF_UP);
            } catch (BaseException ex){
                BaseLoggers.exceptionLogger.error("Exception in Formatting Amount", ex);                
            }
            
        }
        if (notNull(communicationParameter.getFormatMask())
                && CommunicationGeneratorConstants.UPPER_CASE
                        .equalsIgnoreCase(communicationParameter
                                .getFormatMask())) {
            return formatCase(value, 'U');
        } else if (notNull(communicationParameter.getFormatMask())
                && CommunicationGeneratorConstants.LOWER_CASE
                        .equalsIgnoreCase(communicationParameter
                                .getFormatMask())) {
            return formatCase(value, 'L');
        } else if (notNull(communicationParameter.getFormatMask())
                && CommunicationGeneratorConstants.INIT_CASE
                .equalsIgnoreCase(communicationParameter
                        .getFormatMask())) {
        	return formatCase(value, 'S');
        }
        else if (notNull(communicationParameter.getFormatMask())
                && CommunicationGeneratorConstants.DATA_MASK
                        .equalsIgnoreCase(communicationParameter.getFieldType())) {
            try {
            	return communicationEncryptionBusinessObject.maskCommunicationParameter(value.toString(), communicationParameter
                    .getFormatMask());
            }catch (Exception e) {
                Message message = new Message(CommunicationGeneratorConstants.ERROR_IN_MASKING,
                        Message.MessageType.ERROR,communicationParameter.getParameterDesc());
                throw ExceptionBuilder.getInstance(SystemException.class).setMessage(message).setOriginalException(e).setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
            }
        }
        return value;
    }

    
    protected Object formatCase(Object value, Character formatType) {

    	switch (formatType) {
    	case 'U':
    		if (value instanceof String || value instanceof Character) {
    			return value.toString().toUpperCase();
    		}
    		break;
    	case 'L':
    		if (value instanceof String || value instanceof Character) {
    			return value.toString().toLowerCase();
    		}
    		break;
    	case 'S':
    		if (value instanceof String) {
    			return convertValueToInitCap(value.toString());
    		}
    		break;
    	default:
    		break;
    	}
    	return value;
    }
    
    
    
    @Override
    public GeneratedContentVO generateAndWriteOrSendCommunication(
            CommunicationGroupCriteriaVO communicationGroupCriteriaVO,
            CommunicationRequestDetail communicationRequestDetail, Map<String, Object> contextMap) {
    	CommunicationTemplate communicationTemplate = communicationGroupCriteriaVO.getCommunicationTemplate();
		String communicationCode = communicationTemplate.getCommunication().getCommunicationType().getCode();
		
		String barcodeReferenceNumber = null;
		
    	if(CommunicationType.LETTER.equals(communicationCode)) {
    		barcodeReferenceNumber = barcodeGeneratorService.getUniqueBarcodeReferenceNumber();
    		communicationGroupCriteriaVO.setBarcodeReferenceNumber(barcodeReferenceNumber);
    	}
    	
    	communicationRequestDetail.setUniqueRequestId(generateUniqueIdentifier());
        GeneratedContentVO generatedContentVO = generateCommunication(communicationGroupCriteriaVO);
        
        if(generatedContentVO.isBarcodeImageAttached()) {
    		communicationRequestDetail.setBarcodeReferenceNumber(barcodeReferenceNumber);
        }

        generatedContentVO.setEventRequestLogId(communicationRequestDetail.getEventRequestLogId());        
		Boolean returnContentOnly = checkIfReturnContentOnly(communicationRequestDetail, communicationCode);
		if (CommunicationType.LETTER.equals(communicationCode)
				&& (!communicationGroupCriteriaVO.getGenerateContentOnly()) && (!returnContentOnly)) {
			writePdfToFile(communicationRequestDetail, generatedContentVO);
		} else if (CommunicationType.SMS.equals(communicationCode) && (!returnContentOnly)) {
			sendSmsToIntegration(communicationRequestDetail, generatedContentVO);
			// timestamp should be updated when status callback is recieved from
			// integration layers
		} else if (CommunicationType.EMAIL.equals(communicationCode) && (!returnContentOnly)) {
			sendEmailToIntegration(communicationRequestDetail, generatedContentVO,
					communicationGroupCriteriaVO, (Boolean)contextMap.get(ON_DEMAND_FLAG));
		} else if (CommunicationType.WHATSAPP.equals(communicationCode) && (!returnContentOnly)) {
            sendWhatsappMessageToIntegration(communicationRequestDetail, generatedContentVO, communicationGroupCriteriaVO, (Boolean)contextMap.get(ON_DEMAND_FLAG));
        }


		return generatedContentVO;
	}

	private Boolean checkIfReturnContentOnly(CommunicationRequestDetail communicationRequestDetail, String communicationCode) {

		if (CommunicationType.SMS.equals(communicationCode) || CommunicationType.WHATSAPP.equals(communicationCode)) {
			return StringUtils.isEmpty(communicationRequestDetail.getPhoneNumber());
		}
		if (CommunicationType.EMAIL.equals(communicationCode)) {
			return StringUtils.isEmpty(communicationRequestDetail.getPrimaryEmailAddress());
		}
			return false;
	}
    
    private String processSubjectTemplate(CommunicationRequestDetail communicationRequestDetail,
            CommunicationTemplate communicationTemplate,
            Map<String, Object> processedDataMap) {
         String subject = communicationTemplate.getSubject();
         StringBuilder subjectCacheKey=new StringBuilder("commTemplate");
         DateTime timeStamp=communicationTemplate.getEntityLifeCycleData().getLastUpdatedTimeStamp();
         subjectCacheKey.append(communicationTemplate.getId().toString());
         if(timeStamp!=null)
         {
             subjectCacheKey.append(timeStamp.toString());
         }
         try {
                subject = templateService.getStringFromTemplateString(subjectCacheKey.toString(), subject,processedDataMap
                        );
            } catch (Exception e) {
                    BaseLoggers.flowLogger.error("Not able to parse email subject  "+communicationRequestDetail+"exception "+e);
                    
                    throw ExceptionBuilder
                    .getInstance(BusinessException.class)
                    .setMessage(new Message(CommunicationGeneratorConstants.ERROR_IN_PARSING_MAIL_SUBJECT,
                            Message.MessageType.ERROR))
                    .setSeverity(
                            ExceptionSeverityEnum.SEVERITY_MEDIUM
                            .getEnumValue()).build();
        }
        
        return subject;
    }
    protected void updatePhoneNumberInSmsVO(String text, SmsVO smsVO, CommunicationRequestDetail communicationRequestDetail){
        List<String> phoneNumberString = getListFromCommaSeparatedString(text);
        StringBuilder incorrectTelephoneNumbers = new StringBuilder();
        for (String phone: phoneNumberString) {
            List<String> phoneNumberComponents = Arrays.asList(phone.split("-"));
            try {
            	if(phoneNumberComponents.size()==1){
            		smsVO.addTelephoneNumber(Long.valueOf(phoneNumberComponents.get(0)));
            	}
            	if(phoneNumberComponents.size()==2){
            		smsVO.addTelephoneNumber(Integer.valueOf(phoneNumberComponents.get(0)), null, Long.valueOf(phoneNumberComponents.get(1)));    
            	}
            	if(phoneNumberComponents.size()==3){
            		smsVO.addTelephoneNumber(Integer.valueOf(phoneNumberComponents.get(0)), phoneNumberComponents.get(1), Long.valueOf(phoneNumberComponents.get(2)));
            	}
            }  catch (NumberFormatException ex) {
                incorrectTelephoneNumbers.append(phone).append(',');
                BaseLoggers.exceptionLogger.error(CommunicationGeneratorConstants.NUMBER_FORMAT_EXCEPTION_FOR_PHONE_NUMBERS, ex);
                Message message = new Message (CommunicationGeneratorConstants.NUMBER_FORMAT_EXCEPTION_FOR_PHONE_NUMBERS,Message.MessageType.ERROR,phone);
                throw ExceptionBuilder.getInstance(BaseException.class).setMessage(message).setOriginalException(ex).setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
            }    
        }
        if (StringUtils.isNotEmpty(incorrectTelephoneNumbers)) {
            incorrectTelephoneNumbers.deleteCharAt(incorrectTelephoneNumbers.length()-1);
        }
        if (incorrectTelephoneNumbers.length() > 0
                && isNull(smsVO.getTelephoneNumbers())) {
            Message errorMessage=new Message(CommunicationGeneratorConstants.TELEPHONE_NUMBER_FORMAT_MISMATCH,
                    Message.MessageType.ERROR,incorrectTelephoneNumbers.toString(),
                    communicationRequestDetail.getSubjectReferenceNumber());
            throw ExceptionBuilder
                    .getInstance(
                            BusinessException.class,
                            CommunicationGeneratorConstants.TELEPHONE_NUMBER_FORMAT_MISMATCH,
                            "Telephone Number(s) provided does not support the phone number format.")
                            .setMessage(errorMessage).build();
        }
        else if(hasElements(smsVO.getTelephoneNumbers()) && phoneNumberString.size()>smsVO.getTelephoneNumbers().size()){
            Message message = new Message(CommunicationGeneratorConstants.TELEPHONE_NUMBER_FORMAT_MISMATCH,
                    Message.MessageType.ERROR,incorrectTelephoneNumbers.toString(),
                    communicationRequestDetail.getSubjectReferenceNumber());
            List<Message> errorMessages = new ArrayList<>();
            errorMessages.add(message);
            List<CommunicationErrorLogDetail> communicationErrorLogDetail = communicationDataPreparationWrapper.prepareErrorLogData(errorMessages,communicationRequestDetail);
            communicationErrorLoggerService.createCommunicationProcessErrorLoggerDetail(communicationErrorLogDetail);
        }    
    }
    protected List<String> getListFromCommaSeparatedString(String text){
        if (text != null && text.length() > 0){
            String[] array = text.split(",");
            return Arrays.asList(array);
        }
        return Collections.emptyList();
    }
    
    protected List<InternetAddress> getInternetAddressList(String text){
        List<String> addresses = getListFromCommaSeparatedString(text);
        List<InternetAddress> internetAddressList = new ArrayList<>();
        if (!(CollectionUtils.isEmpty(addresses))) {
            try {
                for (String address: addresses) {
                    InternetAddress internetAddress = new InternetAddress(address);
                    internetAddressList.add(internetAddress);
                }
            }
            catch (AddressException e) {
                EmailException ee = new EmailException("To address(es) was/were not valid.");
                List<Message> validationMessages = new ArrayList<>();
                validationMessages.add(new Message(
                        EmailConstatnts.INVALID_TO_ADDRESSES,
                        Message.MessageType.ERROR));
                ee.setMessages(validationMessages);
                throw ee;
            }
        }
        return internetAddressList;
    }
    
	@Override
	@Transactional(noRollbackFor = { SystemException.class, ServiceInputException.class, BusinessException.class,
			BaseException.class, NullPointerException.class })
	public void updateCommunicationRequestWithApplicableEmailAndPhone(
			CommunicationRequestDetail communicationRequestDetail, Map<String, Object> contextMap) {
		String communicatuionTypeCode = communicationRequestDetail.getCommunicationTemplate().getCommunication()
				.getCommunicationType().getCode();
		if (CommunicationType.SMS.equals(communicatuionTypeCode)|| (CommunicationType.WHATSAPP.equals(communicatuionTypeCode))) {
			populateCommunicationRequestWithApplicablePhone(communicationRequestDetail, contextMap);
		}

		if (CommunicationType.EMAIL.equals(communicatuionTypeCode)) {
			populateCommunicationRequestWithApplicableEmail(communicationRequestDetail, contextMap);
		}
	}

	private void populateCommunicationRequestWithApplicablePhone(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> contextMap) {
		if (communicationRequestDetail.getPhoneNumber() == null) {
			communicationRequestDetail.setPhoneNumber((String) contextMap.get(SMS_PRIMARY_PHONE_NUMBERS));
		}
		if (communicationRequestDetail.getAlternatePhoneNumber() == null) {
			communicationRequestDetail.setAlternatePhoneNumber((String) contextMap.get(SMS_ALT_PHONE_NUMBERS));
		}

		if (StringUtils.isEmpty(communicationRequestDetail.getPhoneNumber()) && !((Boolean)contextMap.get(ON_DEMAND_FLAG))) {
			Message message = new Message(CommunicationGeneratorConstants.PHONE_NUMBER_UNAVAILABLE,
					Message.MessageType.ERROR,
					communicationRequestDetail.getCommunicationTemplate().getCommunication().getCommunicationName(),
					communicationRequestDetail.getSubjectReferenceNumber());
			BaseLoggers.exceptionLogger.error("Phone Number not available for the communication Request for "
					+ communicationRequestDetail.getSubjectReferenceNumber());
			throw ExceptionBuilder.getInstance(BusinessException.class).setMessage(message)
					.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();

		}
	}

	private void populateCommunicationRequestWithApplicableEmail(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> contextMap) {

		if (communicationRequestDetail.getPrimaryEmailAddress() == null) {
			communicationRequestDetail.setPrimaryEmailAddress((String) contextMap.get(TO_EMAIL_ADDRESSES));
		}
		if (communicationRequestDetail.getBccEmailAddress() == null) {
			communicationRequestDetail.setBccEmailAddress((String) contextMap.get(TO_BCC_EMAIL_ADDRESSES));
		}
		if (communicationRequestDetail.getCcEmailAddress() == null) {
			communicationRequestDetail.setCcEmailAddress((String) contextMap.get(TO_CC_EMAIL_ADDRESSES));
		}
		if (StringUtils.isEmpty(communicationRequestDetail.getPrimaryEmailAddress()) && !((Boolean)contextMap.get(ON_DEMAND_FLAG))) {
			Message message = new Message(CommunicationGeneratorConstants.EMAIL_ADDRESS_UNAVAILABLE,
					Message.MessageType.ERROR,
					communicationRequestDetail.getCommunicationTemplate().getCommunication().getCommunicationName(),
					communicationRequestDetail.getSubjectReferenceNumber());
			BaseLoggers.exceptionLogger.error("Email not available for the communication Request for subject "
					+ communicationRequestDetail.getSubjectReferenceNumber());
			throw ExceptionBuilder.getInstance(BusinessException.class).setMessage(message)
					.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();

		}
	}

	private String convertValueToInitCap(String value) {

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

    private void setMailAddressesInMimeMessageBuilder(
            List<String> toEmailAddresses,
            List<String> ccAddressList,
            List<String> bccAddressList,
            MimeMailMessageBuilder mimeMailMessageBuilder) {
        String[] emailArr = null;
        if (hasElements(toEmailAddresses)) {
            emailArr = toEmailAddresses.toArray(new String[0]);
            mimeMailMessageBuilder.setTo(emailArr);
        }
        if (hasElements(ccAddressList)) {
            emailArr = ccAddressList.toArray(new String[0]);
            mimeMailMessageBuilder.setCc(emailArr);
        }
        if (hasElements(bccAddressList)) {
            emailArr = bccAddressList.toArray(new String[0]);
            mimeMailMessageBuilder.setBcc(emailArr);
        }
    }
    
    protected List<String> getAddressListFromInternetAddressList(
            List<InternetAddress> internetAddressList) {
        List<String> emailAddressList = new ArrayList<>();
        if (hasElements(internetAddressList)) {
            for (InternetAddress internetAddress : internetAddressList) {
                emailAddressList.add(internetAddress.getAddress());
            }
        }
        return emailAddressList;
    }
    
   /*@Async*/
    private void sendMailMessage(MimeMailMessageBuilder mimeMailMessageBuilder) {
    	TransactionPostCommitWorker.handlePostCommitAsyncExecutor(mailSendTransactionPostCommitWorker, (Object)mimeMailMessageBuilder, true);
    }

    @Override
    public List<String> getDistinctRequestReferenceId(String communicationCode,SourceProduct sourceProduct,Boolean generateMergedFile) {
        return communicationGeneratorDAO.getDistinctRequestReferenceId(communicationCode,sourceProduct,generateMergedFile);
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public long getCommunicationGenerationDetailTotalRecordsSize(
            CommunicationGenerationDetailVO communicationGenerationDetailVO) {
        return communicationGeneratorDAO.getCommunicationGenerationDetailTotalRecordsSize(communicationGenerationDetailVO);
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public List<CommunicationRequestDetail> getCommunicationGenerationDetail(
            CommunicationGenerationDetailVO communicationGenerationDetailVO,
            int startIndex, int batchSize) {
    return communicationGeneratorDAO.getCommunicationGenerationDetail(communicationGenerationDetailVO, startIndex, batchSize);    
    }
    

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public List<Long> getCommunicationGenerationDetailIds(
            CommunicationGenerationDetailVO communicationGenerationDetailVO,
            int startIndex, int batchSize) {
     return communicationGeneratorDAO.getCommunicationGenerationDetailIds(communicationGenerationDetailVO, startIndex, batchSize);    
    }

    
    
    @Override
    public CommunicationName getCommunicationFromCommunicationCode(
            String communicationCode) {
        return communicationGeneratorDAO.getCommunicationFromCommunicationCode(communicationCode);    
    }
    

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void updateCommunicationRequestsAndMoveToHistory(List<CommunicationRequestDetail> communicationRequestDetails) {
        for (CommunicationRequestDetail communicationRequestDetail:communicationRequestDetails) {
            updateCommunicationRequestAndMoveToHistory(communicationRequestDetail,null,null);
        }
    }

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public CommunicationGenerationDetailHistory updateCommunicationRequestAndMoveToHistory(
			CommunicationRequestDetail communicationRequestDetail, String generatedText,
			CommunicationGroupCriteriaVO communicationGroupCriteriaVO) {

		Map<String, Object> additionalDataToBeUpdatedInHistory = new HashMap<>();
		additionalDataToBeUpdatedInHistory.put(CommunicationGeneratorConstants.COMMUNICATION_TEXT, null);
		communicationRequestDetail.setStatus(CommunicationRequestDetail.COMPLETED);
		CommunicationGenerationDetailHistory parentCommunicationGenerationDetailHistory = this
				.moveCommunicationRequestToHistory(prepareCommunicationRequestHistory(communicationRequestDetail,
						additionalDataToBeUpdatedInHistory));
		if (ValidatorUtils.notNull(communicationGroupCriteriaVO)
				&& ValidatorUtils.hasAnyEntry(communicationGroupCriteriaVO.getRequestDtlAndContentMap())) {
			for (Map.Entry<CommunicationRequestDetail, GeneratedContentVO> entry : communicationGroupCriteriaVO
					.getRequestDtlAndContentMap().entrySet()) {
				CommunicationGenerationDetailHistory attachedHistoryRecord = prepareCommunicationRequestHistory(
						entry.getKey(), null);
				attachedHistoryRecord.setParentCommunicationGenerationDetailHistory(parentCommunicationGenerationDetailHistory);
				attachedHistoryRecord.setParentCommunicationGenerationDetailHistoryId(
						parentCommunicationGenerationDetailHistory.getId());
				this.moveCommunicationRequestToHistory(attachedHistoryRecord);
				entry.getKey().setStatus(CommunicationRequestDetail.COMPLETED);
				communicationGeneratorDAO.saveOrUpdate(entry.getKey()); //cases where communicationRequestDetail is not saved. so saveOrUpdate
                //this.deleteGeneratedCommunicationRequest(entry.getKey());
				updateCallbackHandlerOnCommunicationGenerationDetailHistoryCompletion(attachedHistoryRecord);
			}
		}
		//avoid deleting CRD here. Possible deadlock situation(Most probably in case of parent/child CRDs). 
		//this.deleteGeneratedCommunicationRequest(communicationRequestDetail);
		communicationGeneratorDAO.saveOrUpdate(communicationRequestDetail); //cases where communicationRequestDetail is not saved. so saveOrUpdate		
		updateCallbackHandlerOnCommunicationGenerationDetailHistoryCompletion(parentCommunicationGenerationDetailHistory);
		return parentCommunicationGenerationDetailHistory;
	}

	@Override
	public void updateCommunicationRequestAndMoveToHistory(CommunicationRequestDetail communicationRequestDetail, String status) {
		updateCommunicationRequestAndMoveToHistory(communicationRequestDetail, status, true);
	}
	
	@Override
	public void updateCommunicationRequestAndMoveToHistory(CommunicationRequestDetail communicationRequestDetail,
			String status, boolean isEmailType) {
		//if anything other than sms throw exception
		Map<String, Object> additionalDataToBeUpdatedInHistory = new HashMap<>();
		if (DELIVERED.equalsIgnoreCase(status) || READ.equalsIgnoreCase(status)) {
			additionalDataToBeUpdatedInHistory.put(CommunicationGeneratorConstants.STATUS,
				CommunicationRequestDetail.COMPLETED);
		}
		else if (FAILED.equalsIgnoreCase(status)) {
			additionalDataToBeUpdatedInHistory.put(CommunicationGeneratorConstants.STATUS,
					CommunicationRequestDetail.FAILED);
			RequestVO requestVO = createRequestVO(communicationRequestDetail);
			TransactionPostCommitWorker.handlePostCommit(
					reqVO -> handleCallbackForFailedCommunications((RequestVO) reqVO),
					requestVO, true);
		}
		CommunicationGenerationDetailHistory parentCommunicationGenerationDetailHistory = this
				.moveCommunicationRequestToHistory(
						prepareCommunicationRequestHistory(communicationRequestDetail, additionalDataToBeUpdatedInHistory));

		// if communication is email fetch all the attachments
		List<CommunicationRequestDetail> attachments = Collections.emptyList();
		if (isEmailType) {
			attachments = getAttachmentsForEmail(communicationRequestDetail.getId());
		}
		for (CommunicationRequestDetail attachment : attachments) {
			CommunicationGenerationDetailHistory attachedHistoryRecord = prepareCommunicationRequestHistory(attachment,
					null);
			attachedHistoryRecord
					.setParentCommunicationGenerationDetailHistory(parentCommunicationGenerationDetailHistory);
			attachedHistoryRecord.setParentCommunicationGenerationDetailHistoryId(
					parentCommunicationGenerationDetailHistory.getId());
			this.moveCommunicationRequestToHistory(attachedHistoryRecord);
			this.deleteGeneratedCommunicationRequest(attachment);

			updateCallbackHandlerOnCommunicationGenerationDetailHistoryCompletion(attachedHistoryRecord);
		}

		this.deleteGeneratedCommunicationRequest(communicationRequestDetail);
		updateCallbackHandlerOnCommunicationGenerationDetailHistoryCompletion(
				parentCommunicationGenerationDetailHistory);

	}
	private RequestVO createRequestVO(CommunicationRequestDetail communicationRequestDetail) {
		String eventCode = communicationRequestDetail.getCommunicationEventCode();
		SourceProduct module = communicationRequestDetail.getSourceProduct();
		String applicablePrimaryEntityURI = communicationRequestDetail.getApplicablePrimaryEntityURI();
		String subjectURI = communicationRequestDetail.getSubjectURI();
		String subjectReferenceNumber = communicationRequestDetail.getSubjectReferenceNumber();
		String subjectReferenceType = communicationRequestDetail.getSubjectReferenceType();
		
		RequestVO requestVO = communicationEventLoggerService.createRequestVO(subjectURI, eventCode, module,
				subjectReferenceNumber, subjectReferenceType, applicablePrimaryEntityURI, communicationRequestDetail.getReferenceDate(),
				communicationRequestDetail.getAdditionalData());
		requestVO.setDeliveryPriority(communicationRequestDetail.getDeliveryPriority());
		requestVO.setRequestType(communicationRequestDetail.getRequestType());
		return requestVO;
	}

	private void handleCallbackForFailedCommunications(RequestVO requestVo) {
		// No callback if Request Type is ON-DEMAND
		if (notNull(requestVo.getRequestType())
				&& CommunicationConstants.ON_DEMAND.equals(requestVo.getRequestType())) {
			return;
		}
		CommunicationEventRequestLog communicationEventRequestLog = communicationEventLoggerService
				.logCommunicationEventInNewTransaction(requestVo);
		communicationEventLoggerService.generateCommunicationForCallback(requestVo, communicationEventRequestLog);
	}
	
	@Override
	public void updateCommunicationRequest(CommunicationRequestDetail communicationRequestDetail) {
		updateCommunicationRequest(communicationRequestDetail, CommunicationRequestDetail.COMPLETED);
	}
	
	@Override
	public void updateCommunicationRequest(CommunicationRequestDetail communicationRequestDetail, Character communicationRequestDetailStatus) {
		communicationRequestDetail.setStatus(communicationRequestDetailStatus);
		//There are cases where communicationRequestDetail is not saved in database. So saveOrUpdate will do the job.
		communicationGeneratorDAO.saveOrUpdate(communicationRequestDetail);
	}
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCommunicationRequestAndMoveToHistoryInNewTransaction(CommunicationRequestDetail communicationRequestDetail,String generatedText,CommunicationGroupCriteriaVO communicationGroupCriteriaVO) {        
        
        updateCommunicationRequestAndMoveToHistory(communicationRequestDetail,generatedText,communicationGroupCriteriaVO);
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void updateRetriedAttemptsandLogErrorForCommunicationRequests(List<CommunicationRequestDetail> communicationRequestDetails) {
        for (CommunicationRequestDetail communicationRequestDetail:communicationRequestDetails) {
            communicationRequestDetail.setRetriedAttemptsDone(communicationRequestDetail.getRetriedAttemptsDone()+1);
            communicationEventLoggerBusinessObject.updateCommunicationGenerationDetail(communicationRequestDetail);
        }
    }
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public List<CommunicationRequestDetail> getAttachmentsForEmail(long parentId) {
        return communicationGeneratorDAO.getAttachmentsForEmail(parentId);
    }
    
    @Transactional
    @Override
    public void detach(Entity entity) {
        communicationGeneratorDAO.detach(entity);
    }    
    
    private String getFromEmailAddress(CommunicationName communicationName){
    	String senderEmailAddress=communicationName.getSenderEmailAddress();
    	if(StringUtils.isBlank(senderEmailAddress)){
    		senderEmailAddress = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), FROM_ADDRESS).getPropertyValue();
    	}
    	return senderEmailAddress;
    }

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Long> getCommunicationGenerationDetailForCommunicationCode(
			CommunicationGenerationDetailVO communicationGenerationDetailVO, int startIndex, int batchSize) {

		return communicationGeneratorDAO.getCommunicationGenerationDetailForCommunication(
				communicationGenerationDetailVO, startIndex, batchSize);
	}


	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<CommunicationRequestDetail> getCommunicationGenerationDetailObjForCommunicationCode(
			CommunicationGenerationDetailVO communicationGenerationDetailVO, int startIndex, int batchSize) {
		return communicationGeneratorDAO.getCommunicationGenerationDetailObjForCommunication(
				communicationGenerationDetailVO, startIndex, batchSize);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<CommunicationRequestDetail> getCommunicationGenerationDetailObjForCommunicationCodeByRefId(
			CommunicationGenerationDetailVO communicationGenerationDetailVO, int startIndex, int batchSize) {

		return communicationGeneratorDAO.getCommunicationGenerationDetailObjForCommunicationByRefId(
				communicationGenerationDetailVO, startIndex, batchSize);
	}
	
	private String generateUniqueIdentifier(){
		 return uuidGenerator.generateUuid();
		 
	}
	
	/**
	 * ******************************************MESSAGE_EXCHANGE_RECORD***************************************************
	 * This method moves message exchange record to message exchange record history 
	 * for both the cases - SMS/Email.
	 * 
	 * A null status will not change it's message exchange record history.
	 */
	public void moveMessageToHistoryAndDeleteGeneratedMessageRecord(MessageExchangeRecord messageExchangeRecord,
			String status) {
		MessageExchangeRecordHistory messageExchangeRecordHistory = prepareMessageRecordHistory(messageExchangeRecord);
		if (DELIVERED.equalsIgnoreCase(status)) {
			messageExchangeRecordHistory.setDeliveryStatus(MessageDeliveryStatus.DELIVERED);
		} else if (FAILED.equalsIgnoreCase(status)) {
			messageExchangeRecordHistory.setDeliveryStatus(MessageDeliveryStatus.FAILED_DELIVERY);
		} else if (RETRY_EXCEEDED.equalsIgnoreCase(status)) {
			messageExchangeRecordHistory.setDeliveryStatus(MessageDeliveryStatus.FAILED_RETRY_EXCEEDED);
		}
		moveMessageExchangeRecordToHistory(messageExchangeRecordHistory);
		deleteGeneratedMessageExchangeRecord(messageExchangeRecord);
	}
	
	public MessageExchangeRecordHistory prepareMessageRecordHistory(MessageExchangeRecord messageExchangeRecord) {

		if (ShortMessageExchangeRecord.class.isInstance(messageExchangeRecord)) {
			return prepareMessageRecordHistoryForSMS(
					(ShortMessageExchangeRecord) messageExchangeRecord);
		} else if (MailMessageExchangeRecord.class.isInstance(messageExchangeRecord)) {
			return prepareMessageRecordHistoryForMail(
					(MailMessageExchangeRecord) messageExchangeRecord);
		}
		return null;
	}
	
	private ShortMessageRecordHistory prepareMessageRecordHistoryForSMS(ShortMessageExchangeRecord shortMessageExchangeRecord) {
		ShortMessageRecordHistory shortMessageRecordHistory = new ShortMessageRecordHistory();
		setCommonMessageExchangeRecordHistory(shortMessageRecordHistory, shortMessageExchangeRecord);
		shortMessageRecordHistory.setSmsBody(shortMessageExchangeRecord.getSmsBody());
		shortMessageRecordHistory.setSmsFrom(shortMessageExchangeRecord.getSmsFrom());
		shortMessageRecordHistory.setSmsTo(shortMessageExchangeRecord.getSmsTo());
		shortMessageRecordHistory.setStatusMessage(shortMessageExchangeRecord.getStatusMessage());
		shortMessageRecordHistory.setUniqueRequestId(shortMessageExchangeRecord.getUniqueRequestId());
		shortMessageRecordHistory.setSentTimestamp(shortMessageExchangeRecord.getSentTimestamp());
		return shortMessageRecordHistory;
	}
	
	private MessageExchangeRecordHistory prepareMessageRecordHistoryForMail(
			MailMessageExchangeRecord messageExchangeRecord) {
		MailMessageExchangeRecordHistory mailMessageExchangeRecordHistory = new MailMessageExchangeRecordHistory();
		setCommonMessageExchangeRecordHistory(mailMessageExchangeRecordHistory, messageExchangeRecord);
		mailMessageExchangeRecordHistory.setReadTimestamp(messageExchangeRecord.getReadTimestamp());
		mailMessageExchangeRecordHistory.setUniqueRequestId(messageExchangeRecord.getUniqueRequestId());
		mailMessageExchangeRecordHistory.setFromEmailAddress(messageExchangeRecord.getFromEmailAddress());
		mailMessageExchangeRecordHistory.setToAddressList(messageExchangeRecord.getToAddressList());
		mailMessageExchangeRecordHistory.setCcAddressList(messageExchangeRecord.getCcAddressList());
		mailMessageExchangeRecordHistory.setBccAddressList(messageExchangeRecord.getBccAddressList());
		mailMessageExchangeRecordHistory.setHtmlBody(messageExchangeRecord.getHtmlBody());
		mailMessageExchangeRecordHistory.setAttachmentStorageIds(messageExchangeRecord.getAttachmentStorageIds());
		mailMessageExchangeRecordHistory.setSubject(messageExchangeRecord.getSubject());
		return mailMessageExchangeRecordHistory;
	}
	
	private void setCommonMessageExchangeRecordHistory(MessageExchangeRecordHistory messageExchangeRecordHistory, MessageExchangeRecord messageExchangeRecord) {
		messageExchangeRecordHistory.setDeliveryComment(messageExchangeRecord.getDeliveryComment());
		messageExchangeRecordHistory.setDeliveryDescription(messageExchangeRecord.getDeliveryDescription());
		messageExchangeRecordHistory.setDeliveryStatus(messageExchangeRecord.getDeliveryStatus());
		messageExchangeRecordHistory.setDeliveryTimestamp(messageExchangeRecord.getDeliveryTimestamp());
		messageExchangeRecordHistory.setDestinationSystemId(messageExchangeRecord.getDestinationSystemId());
		messageExchangeRecordHistory.setMessageReceiptId(messageExchangeRecord.getMessageReceiptId());
		messageExchangeRecordHistory.setOwnerEntityUri(messageExchangeRecord.getOwnerEntityUri());
		messageExchangeRecordHistory.setRetriedAttemptsDone(messageExchangeRecord.getRetriedAttemptsDone());
		messageExchangeRecordHistory.setSentTimestamp(messageExchangeRecord.getSentTimestamp());
		messageExchangeRecordHistory.setEventRequestLogId(messageExchangeRecord.getEventRequestLogId());
	}
	
	public MessageExchangeRecordHistory moveMessageExchangeRecordToHistory(MessageExchangeRecordHistory messageExchangeRecordHistory) {
		communicationGeneratorDAO.persist(messageExchangeRecordHistory);
		return messageExchangeRecordHistory;
	}
	
	public void deleteGeneratedMessageExchangeRecord(MessageExchangeRecord messageExchangeRecord){
		communicationGeneratorDAO.deleteMessageExchangeRecord(messageExchangeRecord);
	}
	
	public Boolean checkAndDeleteIfRetryAttemptExceeded(MessageExchangeRecord messageExchangeRecord, Integer retrialAttemptsMade) {
		/*if (retrialAttemptsMade < 1) {
			return false;
		}*/
		Map<String, Integer> retryAttemptConfigurations = communicationGenerationHelper.getRetryAttemptConfigurationsFromCache();
		if (retrialAttemptsMade.compareTo(
				retryAttemptConfigurations.get(messageExchangeRecord.getRetryAttemptsConfigKey())) >= 0) {
			//handles mail/sms both cases for moving record in history.
			moveMessageToHistoryAndDeleteGeneratedMessageRecord(messageExchangeRecord, RETRY_EXCEEDED);
			updateCommunicationRequestAndMoveToHistory(communicationGeneratorDAO.getCommunicationGenerationDetailByUniqueId(messageExchangeRecord.getUniqueRequestId()), FAILED);
			return true;
		}
		return false;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendMessage(ShortMessageExchangeRecord shortMessageExchangeRecord) {

		SmsMessage smsMessage = new SmsMessage();
		smsMessage.setTo(shortMessageExchangeRecord.getSmsTo());
		smsMessage.setBody(shortMessageExchangeRecord.getSmsBody());
		smsMessage.setFrom(shortMessageExchangeRecord.getSmsFrom());
		smsMessage.setUniqueRequestId(shortMessageExchangeRecord.getUniqueRequestId());
		shortMessageExchangeRecord.setDeliveryStatus(MessageDeliveryStatus.SENT_TO_INTEGRATION);
		communicationGeneratorDAO.update(shortMessageExchangeRecord);
		TransactionPostCommitWorker.handlePostCommitAsyncExecutor(smsSendTransactionPostCommitWorker, (Object)smsMessage, true);
	}
	
	@Override
	public void sendSMSFromMessageRecordHistory(ShortMessageRecordHistory shortMessageHistory) {

		SmsMessage smsMessage = new SmsMessage();
		smsMessage.setTo(shortMessageHistory.getSmsTo());
		smsMessage.setBody(shortMessageHistory.getSmsBody());
		smsMessage.setFrom(shortMessageHistory.getSmsFrom());
		smsMessage.setUniqueRequestId(shortMessageHistory.getUniqueRequestId());
		TransactionPostCommitWorker.handlePostCommitAsyncExecutor(smsSendTransactionPostCommitWorker, (Object)smsMessage, true);
	}
	
	protected void sendEmailToIntegration(CommunicationRequestDetail communicationRequestDetail,
			GeneratedContentVO generatedContentVO, CommunicationGroupCriteriaVO communicationGroupCriteriaVO, Boolean onDemandFlag) {
		List<InternetAddress> toAddressList = new ArrayList<>();
		List<InternetAddress> ccAddressList = new ArrayList<>();
		List<InternetAddress> bccAddressList = new ArrayList<>();
		toAddressList.addAll(getInternetAddressList(communicationRequestDetail.getPrimaryEmailAddress()));
		ccAddressList.addAll(getInternetAddressList(communicationRequestDetail.getCcEmailAddress()));
		bccAddressList.addAll(getInternetAddressList(communicationRequestDetail.getBccEmailAddress()));
		CommunicationTemplate communicationTemplate = communicationGroupCriteriaVO.getCommunicationTemplate();
		MimeMailMessageBuilder mimeMailMessageBuilder = mailService.createMimeMailBuilder();
		String subject = processSubjectTemplate(communicationRequestDetail, communicationTemplate,
				communicationGroupCriteriaVO.getProcessedDataMap());
		mimeMailMessageBuilder.setFrom(getFromEmailAddress(communicationTemplate.getCommunication()))
				.setSubject(subject).setHtmlBody(generatedContentVO.getGeneratedText());
		StringBuilder fileName = new StringBuilder();
		StringBuilder attachmentStorageIds = new StringBuilder();
		if (ValidatorUtils.hasNoEntry(communicationRequestDetail.getOnDemandAttachments())
				&& StringUtils.isEmpty(communicationRequestDetail.getAttachmentFilePaths())
				&& ValidatorUtils.hasAnyEntry(communicationGroupCriteriaVO.getRequestDtlAndContentMap())) {
			for (Map.Entry<CommunicationRequestDetail, GeneratedContentVO> entry : communicationGroupCriteriaVO
					.getRequestDtlAndContentMap().entrySet()) {
				setFileName(communicationRequestDetail, entry, fileName);
				byte []generatedContentArr = entry.getValue().getGeneratedContent();
				attachmentStorageIds.append(dataStorageService.saveDocument(new ByteArrayInputStream(generatedContentArr), fileName.toString(), "PDF"));
				attachmentStorageIds.append(SEMI_COLON);
				mimeMailMessageBuilder.addAttachment(fileName.toString(),
						new ByteArrayResource(generatedContentArr));
				fileName.setLength(0);
			}
			attachmentStorageIds.setLength(attachmentStorageIds.length() - 1);
		}
		if (ValidatorUtils.hasAnyEntry(communicationRequestDetail.getOnDemandAttachments())) {
			for (Map.Entry<String, byte[]> attachment : communicationRequestDetail.getOnDemandAttachments().entrySet()) {
				mimeMailMessageBuilder.addAttachment(attachment.getKey(), new ByteArrayResource(attachment.getValue()));
			}
		} else if (StringUtils.isNotEmpty(communicationRequestDetail.getAttachmentFilePaths())) {
			//This is also case of on demand attachment where file path is given.
			addAttachmentFromFilePaths(mimeMailMessageBuilder, communicationRequestDetail);
		}
		setMailAddressesInMimeMessageBuilder(getAddressListFromInternetAddressList(toAddressList),
				getAddressListFromInternetAddressList(ccAddressList),
				getAddressListFromInternetAddressList(bccAddressList),
				mimeMailMessageBuilder);
		saveMailMessageExchangeRecord(mimeMailMessageBuilder, generatedContentVO.getGeneratedText(), communicationRequestDetail.getUniqueRequestId(),
				communicationRequestDetail.getEventRequestLogId(), attachmentStorageIds.toString());
		sendMailMessage(mimeMailMessageBuilder.setUniqueRequestId(communicationRequestDetail.getUniqueRequestId()));
	}
	
	private void saveMailMessageExchangeRecord(MimeMailMessageBuilder mimeMailMessageBuilder,
			String generatedText, String uniqueRequestId, String eventRequestLogId, String attachmentStorageIds) {
		try {
			MimeMessage mimeMessage = mimeMailMessageBuilder.getMimeMessage();
			MailMessageExchangeRecord mailExchangeRecord = new MailMessageExchangeRecord();
			mailExchangeRecord.setUniqueRequestId(uniqueRequestId);
			mailExchangeRecord.setEventRequestLogId(eventRequestLogId);
			mailExchangeRecord.setDeliveryStatus(MessageDeliveryStatus.SENT_TO_INTEGRATION);
			mailExchangeRecord.setSubject(mimeMessage.getSubject());
			mailExchangeRecord.setFromEmailAddress(mimeMessage.getFrom()[0].toString());
			mailExchangeRecord.setHtmlBody(generatedText);
			mailExchangeRecord.setAttachmentStorageIds(attachmentStorageIds);
			mailExchangeRecord.setToAddressList(
					Arrays.stream(mimeMessage.getRecipients(RecipientType.TO)).map(toAddress -> ((InternetAddress)toAddress).getAddress())
					.filter(toAddress -> !toAddress.trim().isEmpty()).collect(Collectors.joining(SEMI_COLON)));
			if (mimeMessage.getRecipients(RecipientType.CC) != null) {
				mailExchangeRecord.setCcAddressList(Arrays.stream(mimeMessage.getRecipients(RecipientType.CC))
						.map(toAddress -> ((InternetAddress) toAddress).getAddress()).filter(toAddress -> !toAddress.trim().isEmpty())
						.collect(Collectors.joining(SEMI_COLON)));
			}
			if (mimeMessage.getRecipients(RecipientType.BCC) != null) {
				mailExchangeRecord.setBccAddressList(Arrays.stream(mimeMessage.getRecipients(RecipientType.BCC))
						.map(toAddress -> ((InternetAddress) toAddress).getAddress()).filter(toAddress -> !toAddress.trim().isEmpty())
						.collect(Collectors.joining(SEMI_COLON)));
			}
			communicationGeneratorDAO.saveMessageExchangeRecord(mailExchangeRecord);
		} catch (Exception e) {
			throw new SystemException("An exception occured while saving message exchange record for uniqueRequestId : " + uniqueRequestId, e);
		}
	}
	
	/**
	 * Adding all files as attachment in email from folder or direct file path.
	 * NOTE----- THIS METHOD DOES NOT LOOK FOR FILES IN FOLDER RECURSIVELY.
	 * 
	 * @param mimeMailMessageBuilder
	 * @param communicationRequestDetail
	 */
	private void addAttachmentFromFilePaths(MimeMailMessageBuilder mimeMailMessageBuilder,
			CommunicationRequestDetail communicationRequestDetail) {
		String[] filePathArray = communicationRequestDetail.getAttachmentFilePaths().split(SEMI_COLON);
		for (String attachmentFilePath : filePathArray) {
			try {
				File attachmentFile = new File(attachmentFilePath);
				if (attachmentFile.isDirectory()) {
					File[] allFilesInDirectory = attachmentFile.listFiles();
					for (File fileInFolder : allFilesInDirectory) {
						if (!fileInFolder.isDirectory()) {	//avoid looking recursively for files.
							mimeMailMessageBuilder.addAttachment(fileInFolder.getName(),
									new ByteArrayResource(Files.readAllBytes(fileInFolder.toPath())));
						}
					}
				} else {
					mimeMailMessageBuilder.addAttachment(attachmentFile.getName(), new ByteArrayResource(Files.readAllBytes(attachmentFile.toPath())));
				}
			} catch (IOException ioe) {
				throw new SystemException(IO_EXCEPTION, ioe);
			}
		}
	}

    private void addAttachmentFromFilePaths(Map<String, byte[]> attachments,
                                            CommunicationRequestDetail communicationRequestDetail) {
        String[] filePathArray = communicationRequestDetail.getAttachmentFilePaths().split(SEMI_COLON);
        for (String attachmentFilePath : filePathArray) {
            try {
                File attachmentFile = new File(attachmentFilePath);
                if (attachmentFile.isDirectory()) {
                    File[] allFilesInDirectory = attachmentFile.listFiles();
                    for (File fileInFolder : allFilesInDirectory) {
                        if (!fileInFolder.isDirectory()) {	//avoid looking recursively for files.
                            attachments.put(fileInFolder.getName(),
                                    Files.readAllBytes(fileInFolder.toPath()));
                        }
                    }
                } else {
                    attachments.put(attachmentFile.getName(), Files.readAllBytes(attachmentFile.toPath()));
                }
            } catch (IOException ioe) {
                throw new SystemException(IO_EXCEPTION, ioe);
            }
        }
    }
	
	protected void sendSmsToIntegration(CommunicationRequestDetail communicationRequestDetail,GeneratedContentVO generatedContentVO) {
		String id = generateUniqueIdentifier();
		communicationRequestDetail.setUniqueRequestId(id);
		SmsMessage smsMessage = new SmsMessage();
		smsMessage.setTo(communicationRequestDetail.getPhoneNumber());
		smsMessage.setBody(generatedContentVO.getGeneratedText());
		smsMessage.setUniqueRequestId(id);
		ShortMessageExchangeRecord exchangeRecord = new ShortMessageExchangeRecord();
		exchangeRecord.setSmsBody(generatedContentVO.getGeneratedText());
		exchangeRecord.setSmsTo(communicationRequestDetail.getPhoneNumber());
		exchangeRecord.setUniqueRequestId(id);
		exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.SENT_TO_INTEGRATION);
		exchangeRecord.setEventRequestLogId(communicationRequestDetail.getEventRequestLogId());
		communicationGeneratorDAO.saveMessageExchangeRecord(exchangeRecord);
		TransactionPostCommitWorker.handlePostCommitAsyncExecutor(smsSendTransactionPostCommitWorker, (Object)smsMessage, true);
	}

	/*****
     *
     * Author - deepesh.sharma
     *
     * Following method - sendWhatsappMessageToIntegraion is right now delegated to another temporary method, for temporary adoption purpose.
     *
     * This is for the implementation of whatsapp as a communication mode
     *
     * This does not support attachment sending as of now.
     *
     * The attachment supporting method would be released in future sprints after the release of ECC message channels supporting attachments sending
     *
     *
     * */
	protected void sendWhatsappMessageToIntegration(CommunicationRequestDetail communicationRequestDetail, GeneratedContentVO generatedContentVO, CommunicationGroupCriteriaVO communicationGroupCriteriaVO, Boolean onDemandFlag){

	    sendWhatsappMessageToIntegrationTemporary(communicationRequestDetail, generatedContentVO,communicationGroupCriteriaVO,onDemandFlag);

	  /*  String id = generateUniqueIdentifier();
        communicationRequestDetail.setUniqueRequestId(id);
        WhatsAppMessage whatsAppMessage = new WhatsAppMessage();
        GenericMessage genericMessage = new GenericMessage<WhatsAppMessage>();

        genericMessage.setMessageChannel(MessageChannels.WHATSAPP);

        genericMessage.setMessageOriginatorId(ProductInformationLoader.getProductCode());

        genericMessage.setCallbackEnabled(true);

        whatsAppMessage.setTo(communicationRequestDetail.getPhoneNumber());
        whatsAppMessage.setBody(generatedContentVO.getGeneratedText());

        StringBuilder fileName = new StringBuilder();
        StringBuilder attachmentStorageIds = new StringBuilder();

        Map<String, byte[]> attachments = new HashMap<String, byte[]>();


        if (ValidatorUtils.hasNoEntry(communicationRequestDetail.getOnDemandAttachments())
                && StringUtils.isEmpty(communicationRequestDetail.getAttachmentFilePaths())
                && ValidatorUtils.hasAnyEntry(communicationGroupCriteriaVO.getRequestDtlAndContentMap())) {
            for (Map.Entry<CommunicationRequestDetail, GeneratedContentVO> entry : communicationGroupCriteriaVO
                    .getRequestDtlAndContentMap().entrySet()) {
                setFileName(communicationRequestDetail, entry, fileName);
                byte []generatedContentArr = entry.getValue().getGeneratedContent();
                attachmentStorageIds.append(dataStorageService.saveDocument(new ByteArrayInputStream(generatedContentArr), fileName.toString(), "PDF"));
                attachmentStorageIds.append(SEMI_COLON);
                attachments.put(fileName.toString(),
                        generatedContentArr);
                fileName.setLength(0);

            }
            attachmentStorageIds.setLength(attachmentStorageIds.length() - 1);


        }
        if (ValidatorUtils.hasAnyEntry(communicationRequestDetail.getOnDemandAttachments())) {
            for (Map.Entry<String, byte[]> attachment : communicationRequestDetail.getOnDemandAttachments().entrySet()) {
                attachments.put(attachment.getKey(), attachment.getValue());
            }
        } else if (StringUtils.isNotEmpty(communicationRequestDetail.getAttachmentFilePaths())) {
            //This is also case of on demand attachment where file path is given.
            addAttachmentFromFilePaths(attachments, communicationRequestDetail);
        }

        genericMessage.setBody(whatsAppMessage);




        boolean isIntegrationCallFailed = callWhatsappIntegrationService(genericMessage, attachments, id);

        persistMessageInExchangeRecord(generatedContentVO, communicationRequestDetail, id, isIntegrationCallFailed);
            */

    }

    /****
     *
     * Author - deepesh.sharma
     *
     * This method is developed for temporary adoption purpose.
     *
     * Does not supports sending of attachments along with whatsapp message
     *
     * This would be removed in the following sprints with the adoption of generic Message Channel support after final release of ECC
     *
     *
     *
     * */
    private void sendWhatsappMessageToIntegrationTemporary(CommunicationRequestDetail communicationRequestDetail, GeneratedContentVO generatedContentVO, CommunicationGroupCriteriaVO communicationGroupCriteriaVO, Boolean onDemandFlag) {

        String id = generateUniqueIdentifier();

        communicationRequestDetail.setUniqueRequestId(id);

        WhatsAppMessage whatsAppMessage = new WhatsAppMessage();

        whatsAppMessage.setUniqueRequestId(id);

        whatsAppMessage.setMessageOriginatorId(ProductInformationLoader.getProductCode());

        whatsAppMessage.setTo(communicationRequestDetail.getPhoneNumber());

        whatsAppMessage.setBody(generatedContentVO.getGeneratedText());

        whatsAppMessage.setMediaUris(new ArrayList<>());

        whatsAppMessage.setFrom("+14155238886");

        WhatsAppMessageSendResponse response = whatsappIntegrationService.sendWhatsAppMessage(whatsAppMessage);

        boolean isIntegrationCallFailed = true;

        if (!(response.getWhatsAppDeliveryStatus().equals(WhatsAppMessageSendResponse.WhatsAppDeliveryStatus.FAILED) || response.getWhatsAppDeliveryStatus().equals(WhatsAppMessageSendResponse.WhatsAppDeliveryStatus.UNDELIVERED))){
                isIntegrationCallFailed = false;
        }

        persistMessageInExchangeRecord(generatedContentVO, communicationRequestDetail, id, isIntegrationCallFailed);


    }


    /*

    *Author - deepesh.sharma
    *
    *This method is called by whatsapp integration service, in the attachment supporting phase.
    *
    *
    *Do not remove it without proper intimation
    *
    * Would be adopted in future sprints
    *
    * */
    /*

    private boolean callWhatsappIntegrationService(GenericMessage<WhatsAppMessage> genericMessage, Map<String, byte[]> attachments, String id){

	    boolean isFailed = true;


	    try {

            GenericMessageResponse response = null;

	        if ( attachments.size() == 0){
	            genericMessage.setUniqueRequestId(id);
	            response = whatsappIntegrationService.sendWhatsAppMessage(genericMessage);

	            if (!response.isSuccess()){
	                return isFailed;
                }
                else {
                    isFailed = false;

                    return isFailed;
                }
            }



            response = whatsappIntegrationService.sendWhatsAppMessage(genericMessage);
            if(!response.isSuccess()){
                return isFailed;
            }


            genericMessage.getBody().setBody(null);

            Iterator<Map.Entry<String, byte[]>> itr = attachments.entrySet().iterator();

            while(itr.hasNext()) {

                genericMessage.setAttachments(itr.next());

                if (!itr.hasNext()){
                    genericMessage.setUniqueRequestId(id);
                }

                response = whatsappIntegrationService.sendWhatsAppMessage(genericMessage);

                if(!response.isSuccess()){
                    return isFailed;
                }

            }

            isFailed = false;


        }
	    catch (Exception e){
            BaseLoggers.exceptionLogger.error("Error in calling whatsapp Integration Service" + e);
        }

        return isFailed;
    }
*/

    protected void persistMessageInExchangeRecord (GeneratedContentVO generatedContentVO, CommunicationRequestDetail communicationRequestDetail, String id, boolean isIntegrationCallFailed){

        WhatsAppExchangeRecord exchangeRecord = new WhatsAppExchangeRecord();
        exchangeRecord.setMessageBody(generatedContentVO.getGeneratedText());
        exchangeRecord.setMessageTo(communicationRequestDetail.getPhoneNumber());
        exchangeRecord.setUniqueRequestId(id);

        if (isIntegrationCallFailed) {
            exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.FAILED_AT_INTEGRATION);
        }
        else {
            exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.SENT_TO_INTEGRATION);
        }
        exchangeRecord.setEventRequestLogId(communicationRequestDetail.getEventRequestLogId());
        communicationGeneratorDAO.saveMessageExchangeRecord(exchangeRecord);

    }
	
	protected void writePdfToFile(CommunicationRequestDetail communicationRequestDetail, GeneratedContentVO generatedContentVO) {
		StringBuilder fileName = new StringBuilder();
		fileName.append(generatedContentVO.getLocation())
                .append(communicationRequestDetail.getCommunicationCode());
		String subjectReferenceType=communicationRequestDetail.getSubjectReferenceType();
		if(subjectReferenceType!=null &&!subjectReferenceType.isEmpty())
		{
			fileName.append(FILE_NAME_SEPARATOR_SYMBOL).append(subjectReferenceType);
		}
		         fileName  .append(FILE_NAME_SEPARATOR_SYMBOL)
                .append(communicationRequestDetail.getSubjectReferenceNumber())
                .append(FILE_NAME_SEPARATOR_SYMBOL)
                .append(communicationRequestDetail.getId())
                .append(CommunicationGeneratorConstants.PDF_EXTENSION);
        communicationGenerationHelper.writeDownloadedContent(
                generatedContentVO.getGeneratedContent(), fileName.toString());
	}
	
	public void createMimeMessageBuilderAndMailMessage(MailMessageExchangeRecord mailMessageRecord) {
		MimeMailMessageBuilder mimeMailMessageBuilder = createMimeMessageBuilder(mailMessageRecord.getUniqueRequestId(),
				mailMessageRecord.getFromEmailAddress(), mailMessageRecord.getSubject(), mailMessageRecord.getHtmlBody());
		addEmailAddresses(mailMessageRecord.getToAddressList(), mailMessageRecord.getCcAddressList(),
				mailMessageRecord.getBccAddressList(), mimeMailMessageBuilder);
		addAllAttachments(mailMessageRecord.getAttachmentStorageIds(), mimeMailMessageBuilder);
		mailMessageRecord.setDeliveryStatus(MessageDeliveryStatus.SENT_TO_INTEGRATION);
		communicationGeneratorDAO.updateMessageExchangeRecord(mailMessageRecord);
		sendMailMessage(mimeMailMessageBuilder);
	}
	
	@Override
	public void createMimeMessageBuilderAndSendMail(MailMessageExchangeRecordHistory mailRecordHistory) {
		MimeMailMessageBuilder mimeMailMessageBuilder = createMimeMessageBuilder(mailRecordHistory.getUniqueRequestId(),
				mailRecordHistory.getFromEmailAddress(), mailRecordHistory.getSubject(), mailRecordHistory.getHtmlBody());
		addEmailAddresses(mailRecordHistory.getToAddressList(), mailRecordHistory.getCcAddressList(),
				mailRecordHistory.getBccAddressList(), mimeMailMessageBuilder);
		addAllAttachments(mailRecordHistory.getAttachmentStorageIds(), mimeMailMessageBuilder);
		sendMailMessage(mimeMailMessageBuilder);
	}
	
	private MimeMailMessageBuilder createMimeMessageBuilder(String uniqueRequestId, String fromEmailAddress,
			String subject, String htmlBody) {
		MimeMailMessageBuilder mimeMailMessageBuilder = mailService.createMimeMailBuilder();
		mimeMailMessageBuilder.setUniqueRequestId(uniqueRequestId);
		mimeMailMessageBuilder.setFrom(fromEmailAddress);
		mimeMailMessageBuilder.setSubject(subject);
		mimeMailMessageBuilder.setHtmlBody(htmlBody);
		return mimeMailMessageBuilder;
	}
	
	@Override
	public String[] getAttachmentStorageIdArray(String attachmentStorageIds) {
		return convertStringIntoArray(attachmentStorageIds);
	}
	
	private void addAllAttachments(String attachmentStorageIds, MimeMailMessageBuilder mimeMailMessageBuilder) {
		String[] attachmentIdArray = convertStringIntoArray(attachmentStorageIds);
		for (String attachmentId : attachmentIdArray) {
			File retriveDocument = dataStorageService.retriveDocument(attachmentId);
			try {
				if (retriveDocument == null) {
					throw new SystemException("Document does not exist for given attachment id: "+ attachmentId);
				}
				mimeMailMessageBuilder.addAttachment(attachmentId, new ByteArrayResource(Files.readAllBytes(retriveDocument.toPath())));
			} catch (IOException ioe) {
				throw new SystemException(IO_EXCEPTION, ioe);
			} catch (Exception se) {
				BaseLoggers.exceptionLogger.error("Exception while parsing attachment id : "+ attachmentId + "\n", se);
			}
		}
	}
	
	private void addEmailAddresses(String toAddressList, String ccAddressList, String bccAddressList,
			MimeMailMessageBuilder mimeMailMessageBuilder) {
		mimeMailMessageBuilder.setTo(convertStringIntoArray(toAddressList));
		String[] arr = convertStringIntoArray(ccAddressList);
		if (arr.length != 0) {
			mimeMailMessageBuilder.setCc(arr);
		}
		if ((arr = convertStringIntoArray(bccAddressList)).length != 0) {
			mimeMailMessageBuilder.setBcc(arr);
		}
	}
	
	/**
	 * This method takes a single string which have address list of email 
	 * concatenated by <code>SEMI_COLON<code>(;).
	 * @param combinedString
	 * @return
	 */
	private String[] convertStringIntoArray(String combinedString) {
		if (combinedString != null && !combinedString.isEmpty()) {
			return combinedString.split(SEMI_COLON);
		}
		return EMPTY_STRING_ARRAY;
	}
	
	@Override
	public GeneratedContentVO generateCommunicationForPreview(CommunicationGroupCriteriaVO communicationGroupCriteriaVO,
			CommunicationRequestDetail communicationRequestDetail, Map<String, Object> contextMap) {
    	CommunicationTemplate communicationTemplate = communicationGroupCriteriaVO.getCommunicationTemplate();
		String communicationCode = communicationTemplate.getCommunication().getCommunicationType().getCode();
		String barcodeReferenceNumber = null;
    	if(CommunicationType.LETTER.equals(communicationCode)) {
    		barcodeReferenceNumber = barcodeGeneratorService.getUniqueBarcodeReferenceNumber();
    		communicationGroupCriteriaVO.setBarcodeReferenceNumber(barcodeReferenceNumber);
    	}
    	communicationRequestDetail.setUniqueRequestId(generateUniqueIdentifier());
    	
        GeneratedContentVO generatedContentVO = generateCommunication(communicationGroupCriteriaVO);
        generatedContentVO.setLocation(communicationGenerationHelper.getCommunicationPreviewDocPath());
        if(generatedContentVO.isBarcodeImageAttached()) {
    		communicationRequestDetail.setBarcodeReferenceNumber(barcodeReferenceNumber);
        }
        generatedContentVO.setCommunicationType(communicationCode);
        generatedContentVO.setEventRequestLogId(communicationRequestDetail.getEventRequestLogId());        
		if (CommunicationType.LETTER.equals(communicationCode)) {
			String fileName = getFileNameForCommPreview(communicationRequestDetail);
			generatedContentVO.setFileName(fileName);
			writePdfToFile(communicationRequestDetail, generatedContentVO, fileName);
		} else if (CommunicationType.SMS.equals(communicationCode)) {
			generatedContentVO.setSmsMessageContentVO(createSmsMessageForPreview(generatedContentVO));
		} else if (CommunicationType.EMAIL.equals(communicationCode)) {
			generatedContentVO.setMailMessageContentVO(createMailMessageForPreview(communicationRequestDetail,
					generatedContentVO, communicationGroupCriteriaVO));
		}
        else if (CommunicationType.WHATSAPP.equals(communicationCode)) {
            generatedContentVO.setMailMessageContentVO(createMailMessageForPreview(communicationRequestDetail,
                    generatedContentVO, communicationGroupCriteriaVO));

        }
		return generatedContentVO;
	
	}
	
	private SmsMessageContentVO createSmsMessageForPreview(GeneratedContentVO generatedContentVO) {
		SmsMessageContentVO smsMessage = new SmsMessageContentVO();
		smsMessage.setMessageText(generatedContentVO.getGeneratedText());
		return smsMessage;
	}
	
	private String getFileNameForCommPreview(CommunicationRequestDetail communicationRequestDetail) {
		StringBuilder fileName = new StringBuilder();
		return fileName.append(communicationRequestDetail.getCommunicationCode())
		        .append(FILE_NAME_SEPARATOR_SYMBOL)
		        .append(communicationRequestDetail.getSubjectReferenceNumber())
		        .append(FILE_NAME_SEPARATOR_SYMBOL)
		        .append(communicationRequestDetail.getId())
		        .append(CommunicationGeneratorConstants.PDF_EXTENSION).toString();
	}
	
	private MailMessageContentVO createMailMessageForPreview(CommunicationRequestDetail communicationRequestDetail,
			GeneratedContentVO generatedContentVO, CommunicationGroupCriteriaVO communicationGroupCriteriaVO) {
		MailMessageContentVO mailMessage = new MailMessageContentVO();
		CommunicationTemplate communicationTemplate = communicationGroupCriteriaVO.getCommunicationTemplate();
		mailMessage.setFromEmailAddress(getFromEmailAddress(communicationTemplate.getCommunication()));
		mailMessage.setToAddressList(communicationRequestDetail.getPrimaryEmailAddress());
		mailMessage.setCcAddressList(communicationRequestDetail.getCcEmailAddress());
		mailMessage.setBccAddressList(communicationRequestDetail.getBccEmailAddress());
		String subject = processSubjectTemplate(communicationRequestDetail, communicationTemplate,
				communicationGroupCriteriaVO.getProcessedDataMap());
		mailMessage.setSubject(subject);
		mailMessage.setHtmlBody(generatedContentVO.getGeneratedText());

		List<String> attachmentFileNameList = new ArrayList<>();
		String fileDownloadLocation = generatedContentVO.getLocation();
		StringBuilder fileName = new StringBuilder();
		if (ValidatorUtils.hasNoEntry(communicationRequestDetail.getOnDemandAttachments())
				&& StringUtils.isEmpty(communicationRequestDetail.getAttachmentFilePaths())
				&& ValidatorUtils.hasAnyEntry(communicationGroupCriteriaVO.getRequestDtlAndContentMap())) {
			for (Map.Entry<CommunicationRequestDetail, GeneratedContentVO> entry : communicationGroupCriteriaVO
					.getRequestDtlAndContentMap().entrySet()) {
				setFileName(communicationRequestDetail, entry, fileName);
				byte[] generatedContentArr = entry.getValue().getGeneratedContent();
				generateAttachmentForEmailPreview(attachmentFileNameList, generatedContentArr, fileDownloadLocation,
						fileName.toString());
				fileName.setLength(0);
			}
		}
		if (ValidatorUtils.hasAnyEntry(communicationRequestDetail.getOnDemandAttachments())) {
			for (Map.Entry<String, byte[]> attachment : communicationRequestDetail.getOnDemandAttachments()
					.entrySet()) {
				generateAttachmentForEmailPreview(attachmentFileNameList, attachment.getValue(), fileDownloadLocation,
						attachment.getKey());
			}
		} else if (StringUtils.isNotEmpty(communicationRequestDetail.getAttachmentFilePaths())) {
			// This is also case of on demand attachment where file path is given.
			generateAttachmentFromFilePaths(attachmentFileNameList, communicationRequestDetail, fileDownloadLocation);
		}
		mailMessage.setAttachmentFileNameList(attachmentFileNameList);

		return mailMessage;
	}
	
	private void setFileName(CommunicationRequestDetail communicationRequestDetail,
			Map.Entry<CommunicationRequestDetail, GeneratedContentVO> entry, StringBuilder fileName) {
		fileName.append(entry.getKey().getCommunicationCode()!=null?entry.getKey().getCommunicationCode():"")
				.append(FILE_NAME_SEPARATOR_SYMBOL)
				.append(communicationRequestDetail.getSubjectReferenceNumber()!=null?communicationRequestDetail.getSubjectReferenceNumber():"")
				.append(FILE_NAME_SEPARATOR_SYMBOL)
				.append(communicationRequestDetail.getId()!=null?communicationRequestDetail.getId():"")
				.append(CommunicationGeneratorConstants.PDF_EXTENSION);
	}
	
	private void generateAttachmentForEmailPreview(List<String> attachmentFileNameList, byte[] byteArray, String fileDownloadLocation,
			String fileName) {
		StringBuilder fileNameWithPath = new StringBuilder();
		fileNameWithPath.append(fileDownloadLocation).append(fileName);
		communicationGenerationHelper.writeDownloadedContent(byteArray, fileNameWithPath.toString());
		attachmentFileNameList.add(fileName);
	}
	
	protected void writePdfToFile(CommunicationRequestDetail communicationRequestDetail,
			GeneratedContentVO generatedContentVO, String fileName) {
		StringBuilder fileNameWithPath = new StringBuilder();
		fileNameWithPath.append(generatedContentVO.getLocation()).append(fileName);
		communicationGenerationHelper.writeDownloadedContent(generatedContentVO.getGeneratedContent(),
				fileNameWithPath.toString());
	}
	
	/**
	 * Adding all files as attachment in email from folder or direct file path.
	 * NOTE----- THIS METHOD DOES NOT LOOK FOR FILES IN FOLDER RECURSIVELY.
	 * 
	 * @param attachmentFileNameList
	 * @param communicationRequestDetail
	 * @param fileDownloadLocation
	 */
	private void generateAttachmentFromFilePaths(List<String> attachmentFileNameList,
			CommunicationRequestDetail communicationRequestDetail, String fileDownloadLocation) {
		String[] filePathArray = communicationRequestDetail.getAttachmentFilePaths().split(SEMI_COLON);
		for (String attachmentFilePath : filePathArray) {
			try {
				File attachmentFile = new File(attachmentFilePath);
				if (attachmentFile.isDirectory()) {
					File[] allFilesInDirectory = attachmentFile.listFiles();
					for (File fileInFolder : allFilesInDirectory) {
						if (!fileInFolder.isDirectory()) { // avoid looking recursively for files.
							generateAttachmentForEmailPreview(attachmentFileNameList,
									Files.readAllBytes(fileInFolder.toPath()), fileDownloadLocation,
									fileInFolder.getName());
						}
					}
				} else {
					generateAttachmentForEmailPreview(attachmentFileNameList,
							Files.readAllBytes(attachmentFile.toPath()), fileDownloadLocation,
							attachmentFile.getName());
				}
			} catch (IOException ioe) {
				throw new SystemException(IO_EXCEPTION, ioe);
			}
		}
	}

   


}
