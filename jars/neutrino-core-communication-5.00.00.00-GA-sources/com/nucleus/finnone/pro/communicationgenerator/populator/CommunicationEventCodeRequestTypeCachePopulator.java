package com.nucleus.finnone.pro.communicationgenerator.populator;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;

import com.nucleus.core.event.EventCode;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationCategory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
@Named("communicationEventCodeRequestTypeCachePopulator")
public class CommunicationEventCodeRequestTypeCachePopulator  extends FWCachePopulator{

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.COMMUNICATION_EVENT_MAPPING_CACHE;
	}

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : CommunicationEventCodeRequestTypeCachePopulator");
		
	}

	@Override
	public Object fallback(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void build(Long tenantId) {

		Query communicationEventMappingHeaderQuery = entityDao.getEntityManager()
				.createNamedQuery("getAllCommunicationEventMappings")
				.setParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);

		List<CommunicationEventMappingHeader> communicationEventMappingHeader = communicationEventMappingHeaderQuery
				.getResultList();

		if (CollectionUtils.isNotEmpty(communicationEventMappingHeader)) {
			communicationEventMappingHeader.forEach(commEventMappingHeader -> {
				EventCode eventCode = entityDao.find(EventCode.class, commEventMappingHeader.getEventCodeId());
				CommunicationCategory communicationCategory = entityDao.find(CommunicationCategory.class,
						commEventMappingHeader.getCommunicationCategoryId());
				
				put(eventCode.getCode(), communicationCategory.getCode());
			});
		}
	}

	@Override
	public void update(Action action, Object object) {
		BaseLoggers.flowLogger.debug("Update Called : CommunicationEventCodeRequestTypeCachePopulator");
	
		CommunicationEventMappingHeader communicationEventMappingHeader = (CommunicationEventMappingHeader) object;
		EventCode eventCode = entityDao.find(EventCode.class, communicationEventMappingHeader.getEventCodeId());
		CommunicationCategory communicationCategory = entityDao.find(CommunicationCategory.class,
				communicationEventMappingHeader.getCommunicationCategoryId());
		
		if(action.equals(Action.UPDATE)) {
			put(eventCode.getCode(), communicationCategory.getCode());
		}else if(action.equals(Action.DELETE)) {
			remove(eventCode.getCode());
		}
		
	}

}
