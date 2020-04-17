package com.nucleus.finnone.pro.communication.cache;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.entity.EntityId;
import com.nucleus.event.EventTypes;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;
import com.nucleus.finnone.pro.communicationgenerator.listener.CommunicationEventMappingListener;
import com.nucleus.finnone.pro.communicationgenerator.listener.CommunicationNameListener;
import com.nucleus.finnone.pro.communicationgenerator.populator.CommunicationEventCodeRequestTypeCachePopulator;
import com.nucleus.persistence.EntityDao;

@Named("communicationEventMappingPostCommitWorker")
public class CommunicationEventMappingPostCommitWorker implements TransactionPostCommitWork {
	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	@Inject
	@Named("communicationEventCodeRequestTypeCachePopulator")
	public  CommunicationEventCodeRequestTypeCachePopulator communicationEventCodeRequestTypeCachePopulator;
	@SuppressWarnings("unchecked")
	@Override
	public void work(Object argument) {
		Map<String, Object> dataMap=(Map<String, Object>)argument;
		EntityId entityId = (EntityId) dataMap.get(CommunicationEventMappingListener.OWNER_ENTITY_ID);
		int eventType = (int) dataMap.get(CommunicationNameListener.EVENT_TYPE);
		CommunicationEventMappingHeader communicationEventMappingHeader =  entityDao.get(entityId);
		
		
		if (eventType == EventTypes.MAKER_CHECKER_APPROVED || eventType ==  EventTypes.MAKER_CHECKER_UPDATED_APPROVED) {
			if(communicationEventMappingHeader.isActiveFlag()) {
			communicationEventCodeRequestTypeCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.COMMUNICATION_EVENT_MAPPING_CACHE), Action.UPDATE, communicationEventMappingHeader);
			}
			else
			{
				communicationEventCodeRequestTypeCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.COMMUNICATION_EVENT_MAPPING_CACHE), Action.DELETE, communicationEventMappingHeader);
					
			}
		
		} else if (eventType == EventTypes.MAKER_CHECKER_DELETION_APPROVED) {
			communicationEventCodeRequestTypeCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.COMMUNICATION_EVENT_MAPPING_CACHE), Action.DELETE, communicationEventMappingHeader);
			
		}
	

	}

}