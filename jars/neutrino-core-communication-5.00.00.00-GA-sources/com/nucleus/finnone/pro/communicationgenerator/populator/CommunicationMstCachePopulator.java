package com.nucleus.finnone.pro.communicationgenerator.populator;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationNameDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;

@Named("communicationMstCachePopulator")
public class CommunicationMstCachePopulator extends FWCachePopulator {

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
		BaseLoggers.flowLogger.debug("Init Called : CommunicationMstCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		Long id = (Long) key;
		CommunicationName communicationName = entityDao.find(CommunicationName.class, id);
		communicationCacheService.initializeCommunication(communicationName);
		return communicationName;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		buildCommMasterCache();
	}

	private void buildCommMasterCache() {

		List<CommunicationName> communications = communicationNameDAO.getApprovedCommunicationNames();
		if (ValidatorUtils.hasNoElements(communications)) {
			return;
		}
		for (CommunicationName communicationName : communications) {
			communicationCacheService.initializeCommunication(communicationName);
			put(communicationName.getId(), communicationName);
		}
	}

	@Override
	public void update(Action action, Object object) {
		BaseLoggers.flowLogger.debug("Update Called : CommunicationMstCachePopulator");
		CommunicationName communicationName = (CommunicationName)object;
		if(action.equals(Action.UPDATE)) {
			put(communicationName.getId(),communicationName);
		}else if(action.equals(Action.DELETE)) {
			remove(communicationName.getId());
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.COMMUNICATION_MST;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.COMMUNICATION_CACHE_GROUP;
	}
	
}
