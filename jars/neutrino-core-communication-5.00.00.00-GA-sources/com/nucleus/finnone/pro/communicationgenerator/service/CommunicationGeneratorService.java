package com.nucleus.finnone.pro.communicationgenerator.service;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.FILE_NAME_SEPARATOR_SYMBOL;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationCommonBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationErrorLoggerBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationGeneratorBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationDataPreparationService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationErrorLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationEventLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationGeneratorService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.finnone.pro.communicationgenerator.util.MoneyFormatConfigurationKey;
import com.nucleus.finnone.pro.communicationgenerator.util.WeakReferencedConfigurationHelper;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGenerationDetailVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.GeneratedContentVO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.money.MoneyService;
import com.nucleus.pdfmerger.PdfMergerUtility;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationGeneratorService")
public class CommunicationGeneratorService implements
        ICommunicationGeneratorService {
	
	private static final String CONFIGURATION_QUERY = "Configuration.getPropertyValueFromPropertyKey";
    private static final String COMMUNICATION_SCHEDULER_BATCH = "config.communication.batch";
    private static final String COMMUNICATION_MERGE_CREATE = "config.communication.merge.create";

    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int START_POSITION_ZERO = 0;
    private static final String DEFAULD_CREATER = "System";
    private static final int DEFAULT_BATCH_SIZE_PER_THREAD = 100;
    
    @Inject
    @Named("communicationGeneratorBusinessObject")
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
	@Named("entityDao")
	private EntityDao entityDao;

    @Inject
    @Named("communicationCommonBusinessObject")
    private ICommunicationCommonBusinessObject communicationCommonBusinessObject;

    @Inject
    @Named("communicationGenerationDelegate")
    private ICommunicationGenerationDelegate communicationGenerationDelegate;
    
    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;
    
    @Inject
    @Named("communicationCacheService")
    private ICommunicationCacheService communicationCacheService;
    
    @Inject
    @Named("moneyService")
    private MoneyService moneyService;
    
    @Inject
    @Named("weakReferencedConfigurationHelper")
    private WeakReferencedConfigurationHelper<MoneyFormatConfigurationKey, Map<String, String>> weakCacheForConfigurations;
    
    @Override
    public List<DataPreparationServiceMethodVO> findAdditionalMethodsForCommunicationDataPreperation(
            String communicationCode) {
        return communicationCacheService.getAdditionalMethodsForDataPreparation(communicationCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationTemplate> getTemplateByCommunicationMasterId(
            Long communicationMasterId) {
        return communicationGeneratorBusinessObject
                .getTemplateByCommunicationMasterId(communicationMasterId);
    }


    @Transactional(readOnly = true)
    public List<Object[]> getAttributeValueForGenericParameter(
            Class<? extends GenericParameter> entityClass, String columnName) {

        return communicationGeneratorBusinessObject
                .getAttributeValueForGenericParameter(entityClass, columnName);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAttributeValueForBaseMasterEntity(
            Class<? extends BaseMasterEntity> entityClass, String columnName,
            String dependentColumn, Long dependentColumnValue) {

        return communicationGeneratorBusinessObject
                .getAttributeValueForBaseMasterEntity(entityClass, columnName,
                        dependentColumn, dependentColumnValue);
    }

    protected Map<SourceProduct, List<CommunicationEventRequestLog>> fetchEventRequestBasedOnModule(
            List<CommunicationEventRequestLog> communicationEventRequestLogs) {
        Map<SourceProduct, List<CommunicationEventRequestLog>> eventRequestBasedOnModule = new HashMap<SourceProduct, List<CommunicationEventRequestLog>>();
        List<CommunicationEventRequestLog> commEventRequestLogsBasedOnModule = null;
        for (CommunicationEventRequestLog communicationEventRequestLog : communicationEventRequestLogs) {
            if (eventRequestBasedOnModule.get(communicationEventRequestLog
                    .getSourceProduct()) == null) {
                commEventRequestLogsBasedOnModule = new ArrayList<CommunicationEventRequestLog>();
                commEventRequestLogsBasedOnModule
                        .add(communicationEventRequestLog);
                eventRequestBasedOnModule.put(
                        communicationEventRequestLog.getSourceProduct(),
                        commEventRequestLogsBasedOnModule);
            } else {
                eventRequestBasedOnModule.get(
                        communicationEventRequestLog.getSourceProduct()).add(
                        communicationEventRequestLog);
                eventRequestBasedOnModule.put(
                        communicationEventRequestLog.getSourceProduct(),
                        commEventRequestLogsBasedOnModule);
            }
        }
        return eventRequestBasedOnModule;
    }

    @Override
    @Transactional
    public List<CommunicationDataPreparationDetail> getActiveApprovedDetailBasedOnServiceSouceAndModule(
            SourceProduct module, Long serviceSelectionId) {
        return communicationGeneratorBusinessObject
                .getActiveApprovedDetailBasedOnServiceSouceAndModule(module,
                        serviceSelectionId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateCommunications() {
        CommunicationGenerationDetailVO communicationGenerationDetailVO = new CommunicationGenerationDetailVO();
        communicationGenerationDetailVO
                .setStatus(CommunicationRequestDetail.INITIATED);
		Map<String, Object> localCacheMap = new HashMap<String, Object>();
		Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap 
		                    = new HashMap<String, List<DataPreparationServiceMethodVO>>();
		generateCommunication(communicationGenerationDetailVO, localCacheMap, additionalMethodsMap);
    }

    
    public byte[] generateCommunication(
            CommunicationGenerationDetailVO communicationGenerationDetailVO,
            Map<String, Object> localCacheMap,Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap) {

    	int batchSize = getBatchSize(localCacheMap);
        int startIndex = START_POSITION_ZERO;
        List<CommunicationRequestDetail> communicationRequestDetailsInBatch = 
        		communicationGeneratorBusinessObject.getCommunicationGenerationDetailObjForCommunicationCode(
                communicationGenerationDetailVO, startIndex, batchSize);
        while (hasElements(communicationRequestDetailsInBatch)) {
        		MoneyFormatConfigurationKey moneyConfigurationKey = new MoneyFormatConfigurationKey(communicationGenerationDetailVO.getSchedularInstanceId());
        		weakCacheForConfigurations.putConfigurationInCache(moneyConfigurationKey, moneyService.fetchConfigurationProperties());
	        	generateCommunicationAndMoveToHistory(communicationRequestDetailsInBatch, localCacheMap,additionalMethodsMap,false);
	            communicationRequestDetailsInBatch.clear();
	            communicationRequestDetailsInBatch = 
	            	communicationGeneratorBusinessObject.getCommunicationGenerationDetailObjForCommunicationCode(
	            			communicationGenerationDetailVO,startIndex,batchSize);
	            weakCacheForConfigurations.removeConfiguration(moneyConfigurationKey);
        }
        return null;
    }

    private int getBatchSize(Map<String, Object> localCacheMap) {
    	 if(!localCacheMap.containsKey(COMMUNICATION_SCHEDULER_BATCH))
         {
         	int batchSize;
             String batchSizeCount = configurationService
                     .getPropertyValueByPropertyKey(COMMUNICATION_SCHEDULER_BATCH,
                             CONFIGURATION_QUERY);
             if (isNull(batchSizeCount)) {
                 batchSize = DEFAULT_BATCH_SIZE;
             } else {
                 batchSize = Integer.parseInt(batchSizeCount);
             }
             localCacheMap.put(COMMUNICATION_SCHEDULER_BATCH, batchSize);
         }
		return (int) localCacheMap.get(COMMUNICATION_SCHEDULER_BATCH);
	}

	public List<GeneratedContentVO> generateCommunicationAndMoveToHistory(
			List<CommunicationRequestDetail> communicationRequestDetails, Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap, boolean mergeFile) {
		List<GeneratedContentVO> contentVOs=new ArrayList<GeneratedContentVO>();
        if (hasElements(communicationRequestDetails)) {

        	Map<String, List<CommunicationRequestDetail>> groupByPrimaryAppId =
			communicationRequestDetails.stream()
					.collect(Collectors.groupingBy(CommunicationRequestDetail::getPrimaryAppOrSubjectUri));
        	contentVOs=groupByPrimaryAppId.entrySet().parallelStream()
        	.map( mapEntry ->  
			{	
				return gererateCommunication(mapEntry.getValue(), localCacheMap, additionalMethodsMap, false, mergeFile);
				 
			}).flatMap(list-> list.stream()).collect(Collectors.toList());
   
		  } else {
            BaseLoggers.flowLogger
                    .info(CommunicationGeneratorConstants.NO_RECORDS);
        }
        return contentVOs;
    }
	
	
	private List<GeneratedContentVO> gererateCommunication(
			List<CommunicationRequestDetail> communicationRequestDetails, Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap, boolean onDemand,
			boolean mergeFile)
	{
		return communicationRequestDetails.stream().map(communicationRequestDetail -> {
			return generateContentVoForCommunication(communicationRequestDetail, localCacheMap, additionalMethodsMap,
					false, mergeFile);
		}).filter(contentVo -> contentVo != null).collect(Collectors.toList());

	}

	private GeneratedContentVO generateContentVoForCommunication(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> localCacheMap, Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap,
			boolean onDemand, boolean mergeFile) 
	{

		GeneratedContentVO contentVO=null;
			try{
				communicationGeneratorBusinessObject.detach(communicationRequestDetail);
				communicationGenerationHelper.initializeCommunicationRequestDetailFromCache(communicationRequestDetail);
				contentVO= communicationGenerationDelegate.generateSchedularBasedCommunicationInNewTransaction(
						communicationRequestDetail, localCacheMap, additionalMethodsMap, false, mergeFile);
				
		}catch (Exception exception) {
			StringBuilder exceptionMessage=new StringBuilder("Error occured while generating communication for communicationRequestDetail with id ");
			exceptionMessage.append(communicationRequestDetail.getId());
			BaseLoggers.exceptionLogger
					.error(exceptionMessage.toString(),exception);
			Message message=new Message();
			message.setI18nCode(exceptionMessage.toString());
			List<Message> exceptionMessages=new ArrayList<Message>();
			exceptionMessages.add(message);
			communicationErrorLoggerService.updateCommunicationAndCommunicationProcessErrorLoggerDetail(
					communicationRequestDetail, exceptionMessages, localCacheMap);

		}
			return contentVO;
	
	}
	
    @Transactional(propagation = Propagation.REQUIRED)
    public void generateCommunicationForMergedFile(CommunicationName communicationName,
            CommunicationGenerationDetailVO communicationGenerationDetailVO,
            Map<String, Object> localCacheMap,Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap) {

        List<String> distinctRequestReferenceIds = getDistinctRequestReferenceIds(
                communicationGenerationDetailVO, true);
         Configuration configuration = configurationService
				.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), COMMUNICATION_MERGE_CREATE);
				
         String creator = null;
         if(configuration!=null){
        	 creator= configuration.getPropertyValue();
         }
        if (creator == null) {
            creator = DEFAULD_CREATER;
        }
        int batchSize = communicationGenerationHelper
                .getBatchSizeFromConfiguration(COMMUNICATION_SCHEDULER_BATCH,
                        CONFIGURATION_QUERY, DEFAULT_BATCH_SIZE);
        
        for (String requestReferenceId : distinctRequestReferenceIds) {
        	generateAndMergeCommunicationByRefId(communicationName,communicationGenerationDetailVO, requestReferenceId, localCacheMap,
    				additionalMethodsMap, batchSize,creator);
        }

    }

	private void generateAndMergeCommunicationByRefId(CommunicationName communicationName,
			CommunicationGenerationDetailVO communicationGenerationDetailVO, String requestReferenceId,
			Map<String, Object> localCacheMap, Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap,
			int batchSize, String creator) {
		
        communicationGenerationDetailVO.setRequestReferenceId(requestReferenceId);
        List<CommunicationRequestDetail> communicationRequestDetailsInBatch = null;
        List<GeneratedContentVO> generatedContentList = null;

        communicationRequestDetailsInBatch = communicationGeneratorBusinessObject
                .getCommunicationGenerationDetailObjForCommunicationCodeByRefId(
                        communicationGenerationDetailVO, 0,
                        batchSize);

        while (hasElements(communicationRequestDetailsInBatch)) {
        	
        	generatedContentList = generateCommunicationAndMoveToHistory(
                    communicationRequestDetailsInBatch, localCacheMap,additionalMethodsMap, true);
            try {
            	
            	mergeAndWriteGeneratedContent(communicationName,generatedContentList,communicationRequestDetailsInBatch,creator);
                communicationGeneratorBusinessObject
                .updateCommunicationRequestsAndMoveToHistory(communicationRequestDetailsInBatch);

            } 
            catch (BusinessException  |  IOException e) {
            	BaseLoggers.exceptionLogger.error("Error occured while merging letters ",e);
            	
                communicationGeneratorBusinessObject
                .updateRetriedAttemptsandLogErrorForCommunicationRequests(communicationRequestDetailsInBatch);
            	updateErrorLog(e, communicationGenerationDetailVO);
            } 
            communicationRequestDetailsInBatch.clear();
            communicationRequestDetailsInBatch = communicationGeneratorBusinessObject
                    .getCommunicationGenerationDetailObjForCommunicationCodeByRefId(
                            communicationGenerationDetailVO, 0,
                            batchSize);
            }
       }
	
	private void mergeAndWriteGeneratedContent(CommunicationName communicationName,
			List<GeneratedContentVO> generatedContentList,
			List<CommunicationRequestDetail> communicationRequestDetailsInBatch,String creator) throws IOException {
		
		if(ValidatorUtils.hasNoElements(generatedContentList))
		{
			return;
		}
		List<InputStream> generatedContent=generatedContentList.stream().map(contentVo
				->{
					return (InputStream) new ByteArrayInputStream(
							contentVo.getGeneratedContent());
		}).collect(Collectors.toList());
		
        PdfMergerUtility pdfMergerUtility = beanAccessHelper.getBean(
                "pdfMergerService", PdfMergerUtility.class);
		  String location = communicationName.getLocation();
          String fileName = communicationName.getCommunicationCode()
                  + FILE_NAME_SEPARATOR_SYMBOL
                  + communicationRequestDetailsInBatch
                          .get(communicationRequestDetailsInBatch
                                  .size() - 1).getId().toString()
                  + FILE_NAME_SEPARATOR_SYMBOL
                  + communicationRequestDetailsInBatch.get(0).getId()
                          .toString()
                  + CommunicationGeneratorConstants.PDF_EXTENSION;
          FileOutputStream fs = new FileOutputStream(location
                  + File.separator + fileName);

          pdfMergerUtility.merge(
        		  generatedContent, fs,
                  fileName, creator, fileName);
	}

    @Transactional(readOnly = true)
    public List<String> getDistinctRequestReferenceIds(
            CommunicationGenerationDetailVO communicationGenerationDetailVO,
            Boolean generatedMergeFile) {

        return communicationGeneratorBusinessObject
                .getDistinctRequestReferenceId(
                        communicationGenerationDetailVO.getCommunicationCode(),
                        communicationGenerationDetailVO.getSourceProduct(),
                        generatedMergeFile);

    }

    
    @Transactional
    public GeneratedContentVO generateCommunicationOnDemand(
            CommunicationRequestDetail communicationRequestDetails) {
        return generateCommunicationOnDemand(communicationRequestDetails,
               null, false);
    }
    
    @Transactional
    public GeneratedContentVO generateCommunicationOnDemand(
            CommunicationRequestDetail communicationRequestDetails, Map<String, Object> localCacheMap, boolean returnGeneratedLetterContentOnly) {
		return communicationGenerationDelegate.generateCommunication(communicationRequestDetails,
				localCacheMap == null ? new HashMap<>() : localCacheMap,
				new HashMap<String, List<DataPreparationServiceMethodVO>>(), true, returnGeneratedLetterContentOnly);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationRequestDetail> getCommunicationGenerationDetail(
            CommunicationGenerationDetailVO communicationGenerationDetailVO) {
        return communicationGeneratorBusinessObject
                .getCommunicationGenerationDetail(communicationGenerationDetailVO);
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
	public void logAndGenerateCommunicationsForCommunicationRequests(List<CommunicationName> communicationList,
			SourceProduct module, Map<Object, Object> parameters) {
    	Map<String, Object> localCacheMap = new ConcurrentHashMap<String, Object>();
    	
		localCacheMap.put(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID,
				parameters.get(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID));
    	
    	Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap
    	   =new ConcurrentHashMap<String, List<DataPreparationServiceMethodVO>>();
        if (CollectionUtils.isNotEmpty(communicationList)) {
            for (CommunicationName communication : communicationList) {
               
                generateCommunications(communication,
                        module,parameters,localCacheMap,additionalMethodsMap);
            }
        }
    }

	@Override
	@Transactional(propagation = Propagation.NEVER)
	public void logAndGenerateCommunicationsForCommunicationRequests(SourceProduct module,
			Map<Object, Object> parameters) {
		Map<String, Object> localCacheMap = new ConcurrentHashMap<String, Object>();
		if (parameters != null && parameters.containsKey(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID)) {
			localCacheMap.put(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID,
					parameters.get(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID));
		}
		Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap = new ConcurrentHashMap<String, List<DataPreparationServiceMethodVO>>();
		generateCommunications(null, module, parameters, localCacheMap, additionalMethodsMap);
	}
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void generateCommunications(String communicationCode,
            SourceProduct module) {
    	    Map<String, Object> localCacheMap = new ConcurrentHashMap<String, Object>();
        	Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap
        					=new HashMap<String, List<DataPreparationServiceMethodVO>>();

    		generateCommunications(communicationGeneratorBusinessObject
                    .getCommunicationFromCommunicationCode(communicationCode),
                     module,null,localCacheMap,additionalMethodsMap);
    }
    
    
	@Override
	public void generateCommunications(CommunicationName communicationName, SourceProduct module,
			Map<Object, Object> parameters, Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap) {
    	
        CommunicationGenerationDetailVO communicationGenerationDetailVO = new CommunicationGenerationDetailVO();
        if(communicationName != null) {
        	communicationGenerationDetailVO.setCommunicationCode(communicationName.getCommunicationCode());
        }
        communicationGenerationDetailVO.setSourceProduct(module);
        communicationGenerationDetailVO
                .setStatus(CommunicationRequestDetail.INITIATED);
        
        if (ValidatorUtils.notNull(parameters)
				&& parameters.containsKey(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID)) {
			communicationGenerationDetailVO.setSchedularInstanceId((String) parameters.get(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID));
		}
        //Below if block will only generate applicable letters for merging, remaining will be generated later
        if (communicationName != null && CommunicationType.LETTER.equals(communicationName.getCommunicationType().getCode())) {
	            communicationGenerationDetailVO.setGenerateMergedFile(true);
	            generateCommunicationForMergedFile(communicationName,communicationGenerationDetailVO,
	                    localCacheMap,additionalMethodsMap);
        }
        communicationGenerationDetailVO.setGenerateMergedFile(false);
        communicationGenerationDetailVO.setRequestReferenceId(null);
        generateCommunication(communicationGenerationDetailVO, localCacheMap,additionalMethodsMap);
    }
    
	 protected void updateErrorLog(Exception e,
	            CommunicationGenerationDetailVO communicationGenerationDetailVO) {

		  if(BusinessException.class.isAssignableFrom(e.getClass()))
		  {
			  updateErrorLog((BusinessException)e,communicationGenerationDetailVO);
			  return;
		  }
		  if(FileNotFoundException.class.isAssignableFrom(e.getClass()))
		  {
			  updateErrorLog((FileNotFoundException)e,communicationGenerationDetailVO);
			  return;
		  }
		  if(IOException.class.isAssignableFrom(e.getClass()))
		  {
			  updateErrorLog((IOException)e,communicationGenerationDetailVO);
			  return;
		  }
		  
		  SystemException systemException = (SystemException) ExceptionBuilder
		.getInstance(SystemException.class,
				CommunicationGeneratorConstants.COMMUNICATION_PDF_MERGER_ERROR,
				"Error occured while merging letters")
		.setMessage(CommunicationGeneratorConstants.COMMUNICATION_PDF_MERGER_ERROR)
		.setOriginalException(e).build();
		updateErrorLog(systemException,communicationGenerationDetailVO);
		  
    } 

    protected void updateErrorLog(BusinessException e,
            CommunicationGenerationDetailVO communicationGenerationDetailVO) {
        List<CommunicationErrorLogDetail> communicationErrorLogDetails = new ArrayList<CommunicationErrorLogDetail>();
        CommunicationErrorLogDetail communicationErrorLogDetail = new CommunicationErrorLogDetail();
        communicationErrorLogDetail.setErrorDescription(e.getLogMessage());
        communicationErrorLogDetail.setErrorMessageId(e.getExceptionCode());
        communicationErrorLogDetail
                .setCommunicationCode(communicationGenerationDetailVO
                        .getCommunicationCode());
        communicationErrorLogDetail
                .setCommunicationEventCode(communicationGenerationDetailVO
                        .getEventCode());
        communicationErrorLogDetails.add(communicationErrorLogDetail);
        communicationErrorLoggerBusinessObject
                .createCommunicationProcessErrorLoggerDetail(communicationErrorLogDetails);

    }

    protected void updateErrorLog(SystemException e,
            CommunicationGenerationDetailVO communicationGenerationDetailVO) {
        List<CommunicationErrorLogDetail> communicationErrorLogDetails = new ArrayList<CommunicationErrorLogDetail>();
        CommunicationErrorLogDetail communicationErrorLogDetail = new CommunicationErrorLogDetail();
        communicationErrorLogDetail.setErrorDescription(e.getLogMessage());
        communicationErrorLogDetail.setErrorMessageId(e.getExceptionCode());
        communicationErrorLogDetail
                .setCommunicationCode(communicationGenerationDetailVO
                        .getCommunicationCode());
        communicationErrorLogDetail
                .setCommunicationEventCode(communicationGenerationDetailVO
                        .getEventCode());
        communicationErrorLogDetails.add(communicationErrorLogDetail);
        communicationErrorLoggerBusinessObject
                .createCommunicationProcessErrorLoggerDetail(communicationErrorLogDetails);

    }
    
    
    protected void updateErrorLog(FileNotFoundException e,
            CommunicationGenerationDetailVO communicationGenerationDetailVO) {
        List<CommunicationErrorLogDetail> communicationErrorLogDetails = new ArrayList<CommunicationErrorLogDetail>();
        CommunicationErrorLogDetail communicationErrorLogDetail = new CommunicationErrorLogDetail();
        communicationErrorLogDetail
                .setErrorDescription(e.getLocalizedMessage());
        communicationErrorLogDetail
                .setCommunicationCode(communicationGenerationDetailVO
                        .getCommunicationCode());
        communicationErrorLogDetail
                .setCommunicationEventCode(communicationGenerationDetailVO
                        .getEventCode());
        communicationErrorLogDetails.add(communicationErrorLogDetail);
        communicationErrorLoggerBusinessObject
                .createCommunicationProcessErrorLoggerDetail(communicationErrorLogDetails);

    }

    protected void updateErrorLog(IOException e,
            CommunicationGenerationDetailVO communicationGenerationDetailVO) {
        List<CommunicationErrorLogDetail> communicationErrorLogDetails = new ArrayList<CommunicationErrorLogDetail>();
        CommunicationErrorLogDetail communicationErrorLogDetail = new CommunicationErrorLogDetail();
        communicationErrorLogDetail
                .setErrorDescription(e.getLocalizedMessage());
        communicationErrorLogDetail
                .setCommunicationCode(communicationGenerationDetailVO
                        .getCommunicationCode());
        communicationErrorLogDetail
                .setCommunicationEventCode(communicationGenerationDetailVO
                        .getEventCode());
        communicationErrorLogDetails.add(communicationErrorLogDetail);
        communicationErrorLoggerBusinessObject
                .createCommunicationProcessErrorLoggerDetail(communicationErrorLogDetails);

    }

}
