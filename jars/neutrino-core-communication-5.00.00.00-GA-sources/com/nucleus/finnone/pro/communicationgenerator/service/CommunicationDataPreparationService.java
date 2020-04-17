package com.nucleus.finnone.pro.communicationgenerator.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationDataPreparationBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationCommonService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationDataPreparationService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationErrorLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationEventLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.finnone.pro.communicationgenerator.util.ServiceSelectionCriteria;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.money.MoneyService;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationDataPreparationService")
public class CommunicationDataPreparationService implements ICommunicationDataPreparationService {

	@Inject
	@Named("communicationDataPreparationBusinessObject")
	private ICommunicationDataPreparationBusinessObject communicationDataPreparationBusinessObject  ;
	
	@Inject
	@Named("moneyService")
	private MoneyService moneyService;
	
	@Inject
	@Named("communicationDataPreparationWrapper")
	private CommunicationDataPreparationWrapper communicationDataPreparationWrapper;
	
	
	@Inject
	@Named("communicationCommonService")
	private ICommunicationCommonService communicationCommonService;
		
	@Inject
	@Named("communicationEventLoggerService")
	private ICommunicationEventLoggerService communicationEventLoggerService;
	
	
	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;
	
	@Inject
	@Named("communicationGeneratorService")
	private CommunicationGeneratorService communicationGeneratorService;
	
	@Inject
	@Named("communicationGenerationHelper")
	private CommunicationGenerationHelper communicationGenerationHelper;
	
	@Inject
	@Named("communicationErrorLoggerService")
	private ICommunicationErrorLoggerService communicationErrorLoggerService;
	
	@Inject
	private BeanAccessHelper beanAccessHelper;
	
	
	
	
	
	/*@Transactional
	public GeneratedContentVO prepareDataForCommunicationGeneration(List<CommunicationRequestDetail> communicationRequestDetails,Boolean isOnDemandGeneration,Map<String, Object> localCacheMap){
		
		ConcurrentHashMap<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap= new ConcurrentHashMap<String, List<DataPreparationServiceMethodVO>>();
		List<DataPreparationServiceMethodVO> dataPreparationServiceMethodVOs=null;
		GeneratedContentVO generatedContentVO=null;
		CommunicationGroupCriteriaVO communicationGroupCriteriaVO = null;
		String communicationText = null;
		
		Integer letterRetriedAttempts=THREE;
		Integer smsRetriedAttempts=THREE;
		Integer emailRetriedAttempts=THREE;
		String letterRetriedAttemptsString = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                "config.sms.retriedAttempts").getText();
		
		String smsRetriedAttemptsString = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                "config.letter.retriedAttempts").getText();

		String emailRetriedAttemptsString = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                "config.email.retriedAttempts").getText();
		
		if(notNull(letterRetriedAttemptsString)){
			letterRetriedAttempts=Integer.parseInt(letterRetriedAttemptsString);
		}
		if(notNull(smsRetriedAttemptsString)){
			smsRetriedAttempts=Integer.parseInt(smsRetriedAttemptsString);
		}
		if(notNull(emailRetriedAttemptsString)){
			emailRetriedAttempts=Integer.parseInt(emailRetriedAttemptsString);
		}
		if(communicationRequestDetails !=null && communicationRequestDetails.size()>0){
			Map<String,Object> contextMap=null;
			for(CommunicationRequestDetail communicationRequestDetail:communicationRequestDetails){	
				contextMap=new HashMap<String, Object>();
				contextMap.put("subjectURI", communicationRequestDetail.getSubjectURI());
				contextMap.put("applicablePrimaryEntityURI", communicationRequestDetail.getApplicablePrimaryEntityURI());
				contextMap.putAll(communicationDataPreparationBusinessObject.getInitializedReferencedObjects(communicationRequestDetail));
				String module=communicationRequestDetail.getModule();
				Boolean retriedAttemptsExhausted=communicationDataPreparationBusinessObject.checkIfRetriedAttempstExhausted(communicationRequestDetail,letterRetriedAttempts,smsRetriedAttempts,emailRetriedAttempts);
					if(retriedAttemptsExhausted){
						communicationDataPreparationBusinessObject.moveRequestToHistoryAndDeleteGeneratedRequestInNewTransaction(communicationRequestDetail);
						continue;
					}
				
				//TODO - check letter block or unblock status
					CommunicationDataPreparationDetail communicationDataPreparationDetail=communicationDataPreparationBusinessObject.getCommunicationPreparationDetail(ServiceSelectionCriteria.COMMUNICATION_BLOCK_STATUS_CHECK, module);
					if(notNull(communicationDataPreparationDetail)){
						String className=communicationDataPreparationDetail.getClassName();
						if(StringUtils.isEmpty(className)){
							//TODO Proper I18N message to be added
							 
			  				List<Message> errorMessages=new ArrayList<Message>();
			  				errorMessages.add(CoreUtility.prepareMessage(CommunicationGeneratorConstants.CLASS_NAME_NULL,
			  						Message.MessageType.ERROR,communicationDataPreparationDetail.getUri()));
			  				prepareDataAndLogError(errorMessages,communicationRequestDetail);
							throw ExceptionBuilder.getInstance(SystemException.class, "class.name.is.empty",
															"Class name in CommunicationDataPreparationDetail was null for "+communicationDataPreparationDetail.getUri(), 
															communicationDataPreparationDetail.getId(), null, null).build();
						}
					  	
						try {
							Object beanObject = null;
							
							
							Class targetBeanClass = Class.forName(className);
							String beanId = communicationDataPreparationDetail.getBeanId();
							if(StringUtils.isEmpty(beanId)){
								//TODO Proper I18N message to be added
								List<Message> errorMessages=new ArrayList<Message>();
				  				errorMessages.add(CoreUtility.prepareMessage(CommunicationGeneratorConstants.BEAN_ID_NULL,
				  						Message.MessageType.ERROR,communicationDataPreparationDetail.getUri()));
				  				prepareDataAndLogError(errorMessages,communicationRequestDetail);
								throw ExceptionBuilder.getInstance(SystemException.class, "bean.name.is.empty",
																"bean id in CommunicationDataPreparationDetail was null for "+communicationDataPreparationDetail.getUri(), 
																communicationDataPreparationDetail.getId(), null, null).build();
							}
							if(!CommunicationBlockStatusVerifier.class.isAssignableFrom(targetBeanClass)){
							//TODO Proper I18N message to be added
								
								throw ExceptionBuilder.getInstance(SystemException.class, "class.not.of.CommunicationBlockStatusVerifier.type",
										"Class configured "+targetBeanClass.getName()+" in CommunicationDataPreparationDetail was not type of "+CommunicationBlockStatusVerifier.class , 
										communicationDataPreparationDetail.getId(), null, null)
										.setOriginalException(new BeanNotOfRequiredTypeException(beanId,CommunicationBlockStatusVerifier.class ,targetBeanClass))
										.build();
							}
							beanObject=beanAccessHelper.getBean(beanId,targetBeanClass);
							String subjectEntitySimpleClassName = EntityId.fromUri(communicationRequestDetail.getSubjectURI()).getEntityClass().getSimpleName();
							
							String applicablePrimaryEntitySimpleClassName = EntityId.fromUri(communicationRequestDetail.getApplicablePrimaryEntityURI()).getEntityClass().getSimpleName();
							boolean communicationGenerationAllowed = ((CommunicationBlockStatusVerifier) beanObject)
								.isCommunicationGenerationAllowed(
										communicationRequestDetail
												.getCommunicationCode(),
										(BaseEntity) contextMap
												.get(subjectEntitySimpleClassName),
										(BaseEntity) contextMap
												.get(applicablePrimaryEntitySimpleClassName));
							
							if(!communicationGenerationAllowed){
								//TODO - if on demand - throw exception
								//TODO - if bulk - log exception and return
							}
						
						} catch (ClassNotFoundException e) {
							//TODO Proper I18N message to be added
							throw ExceptionBuilder.getInstance(SystemException.class, "class.not.found",
									"Class configured "+className+" in CommunicationDataPreparationDetail was not found" , 
									communicationDataPreparationDetail.getId(), null, null)
									.setOriginalException(e)
									.build();	
						}
					}
				
				try{
					if(additionalMethodsMap.containsKey(communicationRequestDetail.getCommunicationCode())){
						communicationDataPreparationBusinessObject.prepareContextMapForTemplateSelectionOrCommunicationGeneration(module,contextMap,localCacheMap,isOnDemandGeneration, ServiceSelectionCriteria.COMMUNICATION_GENERATION);
			              dataPreparationServiceMethodVOs=additionalMethodsMap.get(communicationRequestDetail.getCommunicationCode());
			       }else{
			              dataPreparationServiceMethodVOs=communicationGeneratorService.findAdditionalMethodsForCommunicationDataPreperation(communicationRequestDetail.getCommunicationCode());
			              if(dataPreparationServiceMethodVOs !=null){
			                     additionalMethodsMap.put(communicationRequestDetail.getCommunicationCode(), dataPreparationServiceMethodVOs);
			              }
			       }
				   
				   //TODO - optimize callAdditionalMethods for better performance by providing context map
			       if(dataPreparationServiceMethodVOs !=null && dataPreparationServiceMethodVOs.size()>0){
			    	   contextMap.putAll(communicationDataPreparationBusinessObject.callAdditionalMethods(dataPreparationServiceMethodVOs,communicationRequestDetail));
			       }
			       
			       if(CommunicationType.SMS.equals(communicationRequestDetail.getCommunicationTemplate().getCommunication().getCommunicationType().getCode())){				
						String phoneNumbers= (String)contextMap.get(SMS_PRIMARY_PHONE_NUMBERS);
						String alternatePhoneNumbers=(String)contextMap.get(SMS_ALT_PHONE_NUMBERS);
						communicationRequestDetail.setPhoneNumber(phoneNumbers);
						communicationRequestDetail.setAlternatePhoneNumber(alternatePhoneNumbers);					
						if(StringUtils.isEmpty(phoneNumbers))
						{
							Message message = new Message(CommunicationGeneratorConstants.PHONE_NUMBER_UNAVAILABLE,
									Message.MessageType.ERROR,
									communicationRequestDetail
											.getCommunicationTemplate()
											.getCommunication().getCommunicationName()
											,communicationRequestDetail.getSubjectReferenceNumber());
							BaseLoggers.exceptionLogger.error("Phone Number not available for the communication Request for "+communicationRequestDetail.getSubjectReferenceNumber());
							//TODO Exception needs to be thrown or logged
							communicationRequestDetail.setRetriedAttemptsDone(communicationRequestDetail.getRetriedAttemptsDone()+1);
							continue;
						}
					}
					
					if(CommunicationType.EMAIL.equals(communicationRequestDetail.getCommunicationTemplate().getCommunication().getCommunicationType().getCode())){
						String emailAddresses=(String)contextMap.get(TO_EMAIL_ADDRESSES);
						String bccEmailAddresses=(String)contextMap.get(TO_BCC_EMAIL_ADDRESSES);
						String ccEmailAddresses=(String)contextMap.get(TO_CC_EMAIL_ADDRESSES);
						communicationRequestDetail.setPrimaryEmailAddress(emailAddresses);
						communicationRequestDetail.setBccEmailAddress(bccEmailAddresses);
						communicationRequestDetail.setCcEmailAddress(ccEmailAddresses);
						if(StringUtils.isEmpty(emailAddresses))
						{
							Message message = new Message(
									CommunicationGeneratorConstants.EMAIL_ADDRESS_UNAVAILABLE,
									Message.MessageType.ERROR,
									communicationRequestDetail
											.getCommunicationTemplate()
											.getCommunication().getCommunicationName(), communicationRequestDetail.getSubjectReferenceNumber());
							BaseLoggers.exceptionLogger.error("Email not available for the communication Request for subject "+communicationRequestDetail.getSubjectReferenceNumber());
							//TODO Exception needs to be thrown or logged
							communicationRequestDetail.setRetriedAttemptsDone(communicationRequestDetail.getRetriedAttemptsDone()+1);
							continue;
						}
					}

			       communicationGroupCriteriaVO=communicationGenerationHelper.prepareDataForCommunicationGroupCriteriaVO(contextMap,communicationRequestDetail);			       
			       if(isOnDemandGeneration){
			              generatedContentVO= communicationGeneratorService.generateCommunication(communicationGroupCriteriaVO); 
			              communicationRequestDetail.setCommunicationText(communicationText);
			              communicationRequestDetail.setStatus(CommunicationRequestDetail.COMPLETED);
			              communicationDataPreparationBusinessObject.moveCommunicationRequestToHistory(communicationRequestDetail);
			              communicationDataPreparationBusinessObject.deleteGeneratedCommunicationRequest(communicationRequestDetail);

			       }else{
			              communicationText=communicationDataPreparationBusinessObject.generateAndWriteOrSendCommunication(communicationGroupCriteriaVO,communicationRequestDetail);                           
			              communicationRequestDetail.setCommunicationText(communicationText);
			              communicationRequestDetail.setStatus(CommunicationRequestDetail.COMPLETED);
			              communicationDataPreparationBusinessObject.moveCommunicationRequestToHistory(communicationRequestDetail);
			              communicationDataPreparationBusinessObject.deleteGeneratedCommunicationRequest(communicationRequestDetail);
			       }

				
				}catch(BaseException baseException){
					if(!(baseException.isLogged())){
						
						
						BaseLoggers.exceptionLogger.error("BusinessException in prepareDataForCommunicationGeneration", baseException);
						baseException.setLogged(true);
						throw baseException;
					}					
					if(isOnDemandGeneration){
						communicationDataPreparationBusinessObject.moveRequestToHistoryAndDeleteGeneratedRequestInNewTransaction(communicationRequestDetail);
						throw baseException;
					}
					else{
						communicationRequestDetail.setRetriedAttemptsDone(communicationRequestDetail.getRetriedAttemptsDone()+1);
						communicationEventLoggerService.updateCommunicationGenerationDetail(communicationRequestDetail);
					}
				}catch(Exception exception){
					
					BaseLoggers.exceptionLogger.error("Exception in prepareDataForCommunicationGeneration", exception);
					if(isOnDemandGeneration)
					{
						if(exception instanceof TransactionSystemException){
							TransactionSystemException systemException = (TransactionSystemException) exception;
							if(systemException.getApplicationException() instanceof BusinessException)
							{
								communicationDataPreparationBusinessObject.moveRequestToHistoryAndDeleteGeneratedRequestInNewTransaction(communicationRequestDetail);
								BaseException be=(BaseException) systemException.getApplicationException();
								be.setLogged(true);
								throw be;
							}
						}
						else if(exception instanceof InvocationTargetException){
							
							InvocationTargetException targetException = (InvocationTargetException)exception;
							if(targetException.getTargetException() instanceof BusinessException)
							{
								communicationDataPreparationBusinessObject.moveRequestToHistoryAndDeleteGeneratedRequestInNewTransaction(communicationRequestDetail);
								BaseException be=(BaseException) targetException.getTargetException();
								be.setLogged(true);
								throw be;
							}
							
						}
						else{
							communicationDataPreparationBusinessObject.moveRequestToHistoryAndDeleteGeneratedRequestInNewTransaction(communicationRequestDetail);
							throw ExceptionBuilder
									.getInstance(
											BusinessException.class,
											CommunicationGeneratorConstants.LETTER_GENERATION_ERROR,
											"Error in Generating Letter")
									.setMessage(
											CommunicationGeneratorConstants.LETTER_GENERATION_ERROR)
									.setOriginalException(exception).build();
						}
					}
					else{
						communicationRequestDetail.setRetriedAttemptsDone(communicationRequestDetail.getRetriedAttemptsDone()+1);
						communicationEventLoggerService.updateCommunicationGenerationDetail(communicationRequestDetail);
					}
				}
			}
		}
		return generatedContentVO;
	}*/

	@Override
	public Map<String, Object> prepareContextMapForTemplateSelectionOrCommunicationGeneration(SourceProduct module,
			Map<String, Object> contextMap, Map<String, Object> localCacheMap,
			Boolean isOnDemandGeneration, String serviceSelectionCode) {
		return communicationDataPreparationBusinessObject.prepareContextMapForTemplateSelectionOrCommunicationGenerationOrAdhocAndBulk(module,contextMap,localCacheMap, serviceSelectionCode);
	}
	@Override
	@Transactional(noRollbackFor={SystemException.class,ServiceInputException.class, BusinessException.class, BaseException.class, NullPointerException.class})
	public Map<String, Object> getInitializedReferencedObjects(CommunicationRequestDetail communicationRequestDetail, Map<String, Object> localCacheMap){
		return communicationDataPreparationBusinessObject.getInitializedReferencedObjects(communicationRequestDetail, localCacheMap);
	}

	@Override
	@Transactional(noRollbackFor={SystemException.class,ServiceInputException.class, BusinessException.class, BaseException.class, NullPointerException.class})
	public Map<String, Object> prepareDataForCommunicationGeneration(
			CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> contextMap,
			Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap) {

		
		SourceProduct module = communicationRequestDetail.getSourceProduct();
		communicationDataPreparationBusinessObject
		.prepareContextMapForTemplateSelectionOrCommunicationGenerationOrAdhocAndBulk(
				module, contextMap, localCacheMap,
				ServiceSelectionCriteria.COMMUNICATION_GENERATION);
		
		List<DataPreparationServiceMethodVO> dataPreparationServiceMethodVOs = null;
		if (additionalMethodsMap.containsKey(communicationRequestDetail
				.getCommunicationCode())) {
			
			dataPreparationServiceMethodVOs = additionalMethodsMap
					.get(communicationRequestDetail.getCommunicationCode());
		} else {
			dataPreparationServiceMethodVOs = communicationGeneratorService
					.findAdditionalMethodsForCommunicationDataPreperation(communicationRequestDetail
							.getCommunicationCode());
			if (dataPreparationServiceMethodVOs != null) {
				additionalMethodsMap.put(
						communicationRequestDetail.getCommunicationCode(),
						dataPreparationServiceMethodVOs);
			}
		}
		
		if (hasElements(dataPreparationServiceMethodVOs)) {
			Set<DataPreparationServiceMethodVO> voSet = new HashSet<>(dataPreparationServiceMethodVOs);
			contextMap.putAll(communicationDataPreparationBusinessObject
					.callAdditionalMethods(voSet,
							communicationRequestDetail));
		}

		return contextMap;
	}
	
}
