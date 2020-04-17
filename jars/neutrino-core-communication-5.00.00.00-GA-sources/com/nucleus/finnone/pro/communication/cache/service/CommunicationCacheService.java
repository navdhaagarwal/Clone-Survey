package com.nucleus.finnone.pro.communication.cache.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.service.ConfigurationServiceImpl;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.EntityId;
import com.nucleus.event.EventTypes;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationNameDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.listener.CommunicationNameListener;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationGeneratorService;
import com.nucleus.finnone.pro.communicationgenerator.util.ServiceSelectionCriteria;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.finnone.pro.general.util.documentgenerator.IXDoxReportManager;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationCacheService")
public class CommunicationCacheService implements ICommunicationCacheService{
	
    public static final String DELAY_TIME_CONSTANT 				= "600000";
    public static final String SMS_INITIAL_DELAY 				= DELAY_TIME_CONSTANT;
	public static final String SMS_FIXED_DELAY					= DELAY_TIME_CONSTANT;
	public static final String MAIL_INITIAL_DELAY				= "900000";
	public static final String MAIL_FIXED_DELAY					= DELAY_TIME_CONSTANT;
    
	@Inject
	@Named("configurationService")
	private ConfigurationServiceImpl configurationService;
	
	@Inject
	@Named("commnDataPreparationDtlCachePopulator")
	private NeutrinoCachePopulator commnDataPreparationDtlCachePopulator;
	
	@Inject
	@Named("communicationMstCachePopulator")
	private NeutrinoCachePopulator communicationMstCachePopulator;
	
	@Inject
	@Named("communicationTemplateCachePopulator")
	private NeutrinoCachePopulator communicationTemplateCachePopulator;
	
	@Inject
	@Named("commnRetryAttemptConfigCachePopulator")
	private NeutrinoCachePopulator commnRetryAttemptConfigCachePopulator;
    
    @Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;
    
    @Inject
	@Named("communicationAdditionalMethodsCachePopulator")
	private NeutrinoCachePopulator communicationAdditionalMethodsCachePopulator;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	@Inject
	@Named("communicationGeneratorService")
	private ICommunicationGeneratorService communicationGeneratorService;

	@Inject
	@Named("communicationNameDAO")
	ICommunicationNameDAO communicationNameDAO;
	
	@Inject
	@Named("xDoxReportManager")
	IXDoxReportManager xDoxReportManager;
	
	@Value(value = "#{'${comm.sms.resend.init.delay}'}")
	private String smsInitialSchedulerDelay;
	
	@Value(value = "#{'${comm.sms.resend.fixed.delay}'}")
	private String smsFixedSchedulerDelay;
	
	@Value(value = "#{'${comm.email.resend.init.delay}'}")
	private String mailInitialSchedulerDelay;
	
	@Value(value = "#{'${comm.email.resend.fixed.delay}'}")
	private String mailFixedSchedulerDelay;
	
	private String templatePath;
	
	public String getSmsInitialSchedulerDelay() {
		return delayConfiguration("${comm.sms.resend.init.delay}", smsInitialSchedulerDelay);
	}
	
	public String getSmsFixedSchedulerDelay() {
		return delayConfiguration("${comm.sms.resend.fixed.delay}", smsFixedSchedulerDelay);
	}
	
	public String getMailInitialSchedulerDelay() {
		return delayConfiguration("${comm.email.resend.init.delay}", mailInitialSchedulerDelay);
	}
	
	public String getMailFixedSchedulerDelay() {
		return delayConfiguration("${comm.email.resend.fixed.delay}", mailFixedSchedulerDelay);
	}
	
	private String delayConfiguration(String propertyKeyRef, String propertyValue) {
		if (propertyValue instanceof String 
				&& propertyKeyRef.equalsIgnoreCase(propertyValue)){
			propertyValue = SMS_INITIAL_DELAY;
		}
		return propertyValue;
	}
	
	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	
	@Transactional
	@Override
	public void initializeCommunication(CommunicationName communicationName) {
		Hibernate.initialize(communicationName);
		Hibernate.initialize(communicationName.getAttachments());
		Hibernate.initialize(communicationName.getCommunicationParameters());
		Hibernate.initialize(communicationName.getCommunicationTemplates());
	}

	@Override
	public ServiceSelectionCriteria getServiceSelectionCriteria(String serviceSelectionCriteriaCode)
	{
		return (ServiceSelectionCriteria) genericParameterService.findByCode(serviceSelectionCriteriaCode, ServiceSelectionCriteria.class, true);
	}

	@Override
	public CommunicationDataPreparationDetail getActiveApprovedDetailBasedOnServiceSouceAndModule(
			SourceProduct sourceProduct, String serviceSelectionCriteriaCode) {
		StringBuilder key = new StringBuilder(serviceSelectionCriteriaCode);
		key.append(FWCacheConstants.KEY_DELIMITER);
		key.append(sourceProduct.getCode());
		
		List<Long> idList = (List<Long>) this.commnDataPreparationDtlCachePopulator.get(key.toString());
	    if (CollectionUtils.isEmpty(idList)){
	      return null;
	    }
		return entityDao.find(CommunicationDataPreparationDetail.class,idList.get(0));
	}
	
	@Override
	public List<CommunicationDataPreparationDetail> getActiveApprovedDetailsBasedOnServiceSouceAndModule(
			SourceProduct sourceProduct, String serviceSelectionCriteriaCode) {
		StringBuilder key = new StringBuilder(serviceSelectionCriteriaCode);
		key.append(FWCacheConstants.KEY_DELIMITER);
		key.append(sourceProduct.getCode());
		
		List<Long> idList = (List<Long>) this.commnDataPreparationDtlCachePopulator.get(key.toString());
	    if (CollectionUtils.isEmpty(idList)){
	      return null;
	    }
	    List<CommunicationDataPreparationDetail> communicationDataPreparationDetailList = new ArrayList<>();
	    for(Long id : idList) {
	    	communicationDataPreparationDetailList.add(entityDao.find(CommunicationDataPreparationDetail.class,id));
	    }
		return communicationDataPreparationDetailList;
	}

	
	@Override
	public CommunicationName getCommunicationName(Long id) {
		return (CommunicationName) communicationMstCachePopulator.get(id);
	}
	
	
	@Transactional
	@Override
	public CommunicationTemplate getCommunicationTemplate(Long id) {
		return (CommunicationTemplate) communicationTemplateCachePopulator.get(id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Integer> getRetryAttemptsConfiguration() {
		return (Map<String, Integer>) commnRetryAttemptConfigCachePopulator.get(RETRY_ATTEMPTS);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<DataPreparationServiceMethodVO> getAdditionalMethodsForDataPreparation(String communicationCode) {
		//Falback will ideally automatically put data in cache even while getting.
		return (List<DataPreparationServiceMethodVO>) communicationAdditionalMethodsCachePopulator.get(communicationCode);
	}
	@Override
	@Transactional
	public void refreshCommunicationCache(Map<String, Object> dataMap) {

		EntityId communicationNameEntityId = (EntityId) dataMap.get(CommunicationNameListener.OWNER_ENTITY_ID);
		int eventType = (int) dataMap.get(CommunicationNameListener.EVENT_TYPE);
		CommunicationName communicationName =  entityDao.get(communicationNameEntityId);
		List<CommunicationTemplate> communicationTemplates = communicationName.getCommunicationTemplates();
		this.initializeCommunication(communicationName);
		
		if (eventType == EventTypes.MAKER_CHECKER_APPROVED || eventType ==  EventTypes.MAKER_CHECKER_UPDATED_APPROVED) {
			communicationMstCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.COMMUNICATION_MST), Action.UPDATE, communicationName);
			if(communicationTemplates != null) {
				for(CommunicationTemplate communicationTemplate : communicationTemplates) {
					Hibernate.initialize(communicationTemplate);
					communicationTemplateCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.COMMUNICATION_TEMPLATE), Action.UPDATE, communicationTemplate);
				}
			}
		
		} else if (eventType == EventTypes.MAKER_CHECKER_DELETION_APPROVED) {
			communicationMstCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.COMMUNICATION_MST), Action.DELETE, communicationName);
			if(communicationTemplates != null) {
				for(CommunicationTemplate communicationTemplate : communicationTemplates) {
					Hibernate.initialize(communicationTemplate);
					communicationTemplateCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.COMMUNICATION_TEMPLATE), Action.DELETE, communicationTemplate);
				}
			}
		}
	
	}

}
