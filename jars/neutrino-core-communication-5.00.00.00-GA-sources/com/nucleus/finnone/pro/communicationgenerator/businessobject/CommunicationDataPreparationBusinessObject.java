package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationDataPreparationDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.AddOnDataProviderForAdhocAndBulkCommunication;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.AddOnDataProviderForCommunicationGeneration;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.AddOnDataProviderForTemplateSelection;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationGeneratorService;
import com.nucleus.finnone.pro.communicationgenerator.util.ServiceSelectionCriteria;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationDataPreparationBusinessObject")
public class CommunicationDataPreparationBusinessObject implements ICommunicationDataPreparationBusinessObject {

	@Inject
	@Named("communicationDataPreparationDAO")
	private ICommunicationDataPreparationDAO communicationDataPreparationDAO;
	
	@Inject
	@Named("communicationGeneratorService")
	private ICommunicationGeneratorService communicationGeneratorService;
	
	@Inject
	private BeanAccessHelper beanAccessHelper;
	
	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("communicationCacheService")
	private ICommunicationCacheService communicationCacheService;
	
	public static final String INDIVIDUAL = "INDIVIDUAL";
	public static final String NON_INDIVIDUAL = "NON_INDIVIDUAL";
	public static final String ERROR_TARGET_SERVICE = "Unhandeled Error in callTargetService during MethodUtils call";
	
	private Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

	@Override
	public Map<String, Object> getInitializedReferencedObjects(CommunicationRequestDetail communicationRequestDetail, Map<String, Object> localCacheMap) {
		
		Map<String, Object> communicationDetailMap = new HashMap<>();
		Object beanObject = localCacheMap.get("subjectURI");
		if (beanObject == null) {
			EntityId entityId = EntityId.fromUri(communicationRequestDetail.getSubjectURI());
			beanObject = communicationDataPreparationDAO.findById(entityId.getLocalId(), entityId.getEntityClass());
		}
		if (beanObject != null) {
			communicationDetailMap.put(beanObject.getClass().getSimpleName(), beanObject);
		}
		beanObject = localCacheMap.get("applicablePrimaryEntityURI");
		if (communicationRequestDetail.getApplicablePrimaryEntityURI() != null && beanObject == null) {
			EntityId entityId = EntityId.fromUri(communicationRequestDetail.getApplicablePrimaryEntityURI());		
			beanObject = communicationDataPreparationDAO.findById(entityId.getLocalId(), entityId.getEntityClass());
		}
		if (beanObject != null) {
			communicationDetailMap.put(beanObject.getClass().getSimpleName(), beanObject);
		}
		return communicationDetailMap;
	}
	
	
	@Override
	public Map<String, Object> callAdditionalMethods(Set<DataPreparationServiceMethodVO> dataPreparationServiceMethodVOs, CommunicationRequestDetail communicationRequestDetail) {
		Map<String, Object> communicationDetailMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(dataPreparationServiceMethodVOs)) {
			for (DataPreparationServiceMethodVO dataPreparationServiceMethodVO : dataPreparationServiceMethodVOs) {
				callAdditionalMethodForDataPreparation(dataPreparationServiceMethodVO, communicationRequestDetail, communicationDetailMap);
			}
		}
		return communicationDetailMap;
	}

	@SuppressWarnings("unchecked")
	private void callAdditionalMethodForDataPreparation(DataPreparationServiceMethodVO dataPreparationServiceMethodVO,
			CommunicationRequestDetail communicationRequestDetail, Map<String, Object> communicationDetailMap) {
		try {
			String serviceInterfaceName = dataPreparationServiceMethodVO.getServiceInterfaceName();
			Class<?> targetBeanClass = classCache.get(serviceInterfaceName);
			if (targetBeanClass == null) {
				targetBeanClass = Class.forName(serviceInterfaceName);
				classCache.put(serviceInterfaceName, targetBeanClass);
			}
			Object beanObject = beanAccessHelper.getBean(dataPreparationServiceMethodVO.getTargetServiceName(), targetBeanClass);
			Map<String,Object> detailMap = (Map<String,Object>) callTargetServiceInExistingTransaction(beanObject, dataPreparationServiceMethodVO.getTargetMethodName(), communicationRequestDetail);
			if (ValidatorUtils.notNull(detailMap)) {
				communicationDetailMap.putAll(detailMap);
			}
		} catch(BaseException baseException){
			if(!(baseException.isLogged())){
				BaseLoggers.exceptionLogger.error("Tracing BusinessException:CommunicationDataPreparationBusinessObject.callAdditionalMethods", baseException);
				baseException.setLogged(true);
			}					
			throw baseException;
		} catch (TransactionSystemException tse) {
			if (tse.getApplicationException() instanceof BusinessException) {
				BaseException be=(BaseException) tse.getApplicationException();
				be.setLogged(true);
				throw be;
			}
			CommunicationExceptionLoggerHelper.throwSystemException(tse, "Tracing:CommunicationDataPreparationBusinessObject.callAdditionalMethods::");
		} catch(Exception exception) {
			BaseLoggers.exceptionLogger.error("Tracing:CommunicationDataPreparationBusinessObject.callAdditionalMethods::", exception);
				throw ExceptionBuilder.getInstance(BusinessException.class, "fmsg.00002018", "Error in calling Additional Method [" + dataPreparationServiceMethodVO.getTargetMethodName() + "] for report Generation")
				.setOriginalException(exception)
				.setMessage(CoreUtility.prepareMessage("fmsg.00002018",dataPreparationServiceMethodVO.getTargetMethodName(),communicationRequestDetail.getCommunicationCode()))
				.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();	
		}
	}


	@Override
	public Map<String, Object> prepareContextMapForTemplateSelectionOrCommunicationGenerationOrAdhocAndBulk(
			SourceProduct module, Map<String, Object> contextMap, Map<String, Object> localCacheMap,
			String serviceSelectionCode) {
		
		String subjectReferenceNumber = (String) contextMap.get("subjectReferenceNumber");
		List<CommunicationDataPreparationDetail> communicationDataPreparationDetails = getCommunicationPreparationDetails(
				serviceSelectionCode, module);
		if(CollectionUtils.isNotEmpty(communicationDataPreparationDetails)) {
			for(CommunicationDataPreparationDetail communicationDataPreparationDetail : communicationDataPreparationDetails) {
				if (isNull(communicationDataPreparationDetail)) {
			  		break;
			  	}

				String className=communicationDataPreparationDetail.getClassName();
				if (StringUtils.isEmpty(className)) {
					Message message = new Message(
							CommunicationGeneratorConstants.CLASS_NAME_NULL,
							Message.MessageType.ERROR,
							communicationDataPreparationDetail.getUri());
					throw ExceptionBuilder
							.getInstance(SystemException.class)
							.setMessage(message)
							.setSubjectId(communicationDataPreparationDetail.getId())
							.setSubjectReferenceNumber(subjectReferenceNumber)
							.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
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
								.setSubjectId(communicationDataPreparationDetail.getId())
								.setSubjectReferenceNumber(subjectReferenceNumber)
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
					if(ServiceSelectionCriteria.TEMPLATE_SELECTION.equals(serviceSelectionCode) 
							&& !AddOnDataProviderForTemplateSelection.class.isAssignableFrom(targetBeanClass)){
						Message message = new Message(CommunicationGeneratorConstants.CLASS_NOT_OF_TYPE_DATA_PREP_DETAIL,
								Message.MessageType.ERROR,targetBeanClass.getName(),String.valueOf(AddOnDataProviderForTemplateSelection.class));						
						throw ExceptionBuilder
								.getInstance(SystemException.class)
								.setMessage(message)
								.setSubjectId(communicationDataPreparationDetail.getId())
								.setSubjectReferenceNumber(subjectReferenceNumber)
								.setOriginalException(
										new BeanNotOfRequiredTypeException(
												beanId,
												AddOnDataProviderForTemplateSelection.class,
												targetBeanClass))
								.setSeverity(
										ExceptionSeverityEnum.SEVERITY_MEDIUM
												.getEnumValue()).build();
					}
					if(ServiceSelectionCriteria.COMMUNICATION_GENERATION.equals(serviceSelectionCode) 
							&& !AddOnDataProviderForCommunicationGeneration.class.isAssignableFrom(targetBeanClass)){
						Message message = new Message(CommunicationGeneratorConstants.CLASS_NOT_OF_TYPE_DATA_PREP_DETAIL,
								Message.MessageType.ERROR,targetBeanClass.getName(),String.valueOf(AddOnDataProviderForCommunicationGeneration.class));						
						throw ExceptionBuilder
								.getInstance(SystemException.class)
								.setMessage(message)
								.setSubjectId(communicationDataPreparationDetail.getId())
								.setSubjectReferenceNumber(subjectReferenceNumber)
								.setOriginalException(
										new BeanNotOfRequiredTypeException(
												beanId,
												AddOnDataProviderForCommunicationGeneration.class,
												targetBeanClass))
								.setSeverity(
										ExceptionSeverityEnum.SEVERITY_MEDIUM
												.getEnumValue()).build();
					}
					if(ServiceSelectionCriteria.ADHOC_BULK_COMMUNICATION.equals(serviceSelectionCode) 
							&& !AddOnDataProviderForAdhocAndBulkCommunication.class.isAssignableFrom(targetBeanClass)){
						Message message = new Message(CommunicationGeneratorConstants.CLASS_NOT_OF_TYPE_DATA_PREP_DETAIL,
								Message.MessageType.ERROR,targetBeanClass.getName(),String.valueOf(AddOnDataProviderForAdhocAndBulkCommunication.class));						
						throw ExceptionBuilder
								.getInstance(SystemException.class)
								.setMessage(message)
								.setSubjectId(communicationDataPreparationDetail.getId())
								.setSubjectReferenceNumber(subjectReferenceNumber)
								.setOriginalException(
										new BeanNotOfRequiredTypeException(
												beanId,
												AddOnDataProviderForAdhocAndBulkCommunication.class,
												targetBeanClass))
								.setSeverity(
										ExceptionSeverityEnum.SEVERITY_MEDIUM
												.getEnumValue()).build();
					}
					beanObject = beanAccessHelper.getBean(beanId, targetBeanClass);
					if(ServiceSelectionCriteria.COMMUNICATION_GENERATION.equals(serviceSelectionCode)){
						((AddOnDataProviderForCommunicationGeneration)beanObject).provideDataForCommunicationGeneration(contextMap, localCacheMap);
					}
					if(ServiceSelectionCriteria.TEMPLATE_SELECTION.equals(serviceSelectionCode)){
						((AddOnDataProviderForTemplateSelection)beanObject).provideDataForTemplateSelection(contextMap, localCacheMap);
					}
					if(ServiceSelectionCriteria.ADHOC_BULK_COMMUNICATION.equals(serviceSelectionCode)){
						((AddOnDataProviderForAdhocAndBulkCommunication)beanObject).provideDataForAdhocAndBulkCommunication(contextMap, localCacheMap);
					}
				} catch (ClassNotFoundException e) {
					Message message = new Message(CommunicationGeneratorConstants.CLASS_NOT_FOUND_DATA_PREP_DETAIL,
							Message.MessageType.ERROR,className);						
					throw ExceptionBuilder
							.getInstance(SystemException.class)
							.setMessage(message)
							.setSubjectId(communicationDataPreparationDetail.getId())
							.setSubjectReferenceNumber(subjectReferenceNumber)
							.setOriginalException(e)
							.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue())
							.build();
				}
			  	
			}
		}
		return contextMap;
	}
	
	@Override
	public CommunicationDataPreparationDetail getCommunicationPreparationDetail(String serviceSelectionCode, SourceProduct module){

		return communicationCacheService.getActiveApprovedDetailBasedOnServiceSouceAndModule(module, serviceSelectionCode);
	}
	
	@Override
	public List<CommunicationDataPreparationDetail> getCommunicationPreparationDetails(String serviceSelectionCode, SourceProduct module){

		return communicationCacheService.getActiveApprovedDetailsBasedOnServiceSouceAndModule(module, serviceSelectionCode);
	}

	@Transactional
	public Object callTargetServiceInExistingTransaction(Object beanObject, String operationName, Object createdObject) {

		return callTargetService(beanObject, operationName, createdObject);

	}

	@Transactional
	public Object callTargetService(Object beanObject,String operationName,Object createdObject) {
		Object object = null;
		try {
			object = MethodUtils.invokeMethod(beanObject, operationName, createdObject);
		} catch (BaseException e) {
			BaseLoggers.exceptionLogger.error(ERROR_TARGET_SERVICE, e);
			throw e;                   
		} catch (Exception exception) {
			CommunicationExceptionLoggerHelper.logAndThrowException(exception, ERROR_TARGET_SERVICE);
		}
		return object;
	}

}
