package com.nucleus.finnone.pro.communicationgenerator.populator;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationNameDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;

@Named("communicationTemplateCachePopulator")
public class CommunicationTemplateCachePopulator extends FWCachePopulator {

	@Inject
	@Named("communicationNameDAO")
	ICommunicationNameDAO communicationNameDAO;

	@Inject
	@Named("communicationCacheService")
	ICommunicationCacheService communicationCacheService;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : CommunicationTemplateCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		Long id = (Long) key;
		CommunicationTemplate communicationTemplate = entityDao.find(CommunicationTemplate.class, id);
		Hibernate.initialize(communicationTemplate);
		communicationCacheService.initializeCommunication(communicationTemplate.getCommunication());
		return communicationTemplate;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		buildCommTemplateCache();
	}

	private void buildCommTemplateCache() {

		List<CommunicationTemplate> communicationTemplates = entityDao.findAll(CommunicationTemplate.class);
		if (ValidatorUtils.hasNoElements(communicationTemplates)) {
			return;
		}
			for(CommunicationTemplate communicationTemplate : communicationTemplates ) {
			Hibernate.initialize(communicationTemplate);
			communicationCacheService.initializeCommunication(communicationTemplate.getCommunication());
				put(communicationTemplate.getId(), communicationTemplate);

			}
		}

	@Override
	public void update(Action action, Object object) {
		BaseLoggers.flowLogger.debug("Update Called : CommunicationTemplateCachePopulator");
		CommunicationTemplate communicationTemplate = (CommunicationTemplate) object;
		if(action.equals(Action.UPDATE)) {
			put(communicationTemplate.getId(),communicationTemplate);
		}else if(action.equals(Action.DELETE)) {
			remove(communicationTemplate.getId());
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.COMMUNICATION_TEMPLATE;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.COMMUNICATION_CACHE_GROUP;
	}

}
