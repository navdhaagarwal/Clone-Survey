package com.nucleus.finnone.pro.communicationgenerator.listener;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEventListener;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;

@Named("communicationEventMappingListener")
public class CommunicationEventMappingListener extends GenericEventListener {
	@Inject
	@Named("communicationEventMappingPostCommitWorker")
	private TransactionPostCommitWork communicationEventMappingPostCommitWorker;

	@Inject
	@Named("fwCacheHelper")
	private FWCacheHelper fwCacheHelper;

	public static final String OWNER_ENTITY_ID = "OWNER_ENTITY_ID";

	public static final String EVENT_TYPE = "EVENT_TYPE";

	@Override
	public boolean canHandleEvent(Event event) {
		if (event instanceof MakerCheckerEvent && (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED
				|| event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED
				|| event.getEventType() == EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
			MakerCheckerEvent makerCheckerEvent = (MakerCheckerEvent) event;
			EntityId entityId = makerCheckerEvent.getOwnerEntityId();
			if (entityId.getEntityClass() != null
					&& CommunicationEventMappingHeader.class.isAssignableFrom(entityId.getEntityClass())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void handleEvent(Event event) {
		Map<String, Object> dataMap = new HashMap<>();

		dataMap.put(OWNER_ENTITY_ID, ((MakerCheckerEvent) event).getOwnerEntityId());
		dataMap.put(EVENT_TYPE, event.getEventType());
		dataMap.put(FWCacheConstants.COMMUNICATION_EVENT_MAPPING_CACHE, fwCacheHelper
				.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.COMMUNICATION_EVENT_MAPPING_CACHE));

		TransactionPostCommitWorker.handlePostCommit(communicationEventMappingPostCommitWorker, dataMap, true);

	}

}
