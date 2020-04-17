/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.organization.listener;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.businessmapping.service.UserOrgBranchMappingService;
import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEventListener;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;

/**
 * @author Nucleus Software Exports Limited
 * 
 * This listener is used to listen to all {@link OrganizationBranch} creation events to create a mapping between the newly created branch and all super admins
 */
@Named
public class OrganizationBranchApprovalListener extends GenericEventListener {

    @Inject
    @Named(value = "userOrgBranchMappingService")
    UserOrgBranchMappingService userOrgBranchMappingService;
  
    @Inject
    @Named("organizationBranchCachePostCommitWorker")
    private TransactionPostCommitWork organizationBranchCachePostCommitWorker;
    
    public static final String OWNER_ENTITY_ID = "OWNER_ENTITY_ID";
    
    public static final String EVENT_TYPE = "EVENT_TYPE";
    
    
    @Inject
    @Named("fwCacheHelper")
    private FWCacheHelper fwCacheHelper;
    
    /* (non-Javadoc) @see com.nucleus.event.EventListener#canHandleEvent(com.nucleus.event.Event) */
    /**
     * To Check If The event is a MakerCheckerEvent
     * And the Entity Is OrganizationBranch, then return true else false
     */
    @Override
    public boolean canHandleEvent(Event event) { 
        // should not check for for MAKER_CHECKER_UPDATED_APPROVED events because we only want to map super admins to a newly created organization branch.
        if (event instanceof MakerCheckerEvent
                && (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED||event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED||event.getEventType() == EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
            MakerCheckerEvent makerCheckerEvent = (MakerCheckerEvent) event;
            EntityId organizationBranchEntityId = makerCheckerEvent.getOwnerEntityId();
            if (organizationBranchEntityId.getEntityClass() != null
                    && organizationBranchEntityId.getEntityClass().equals(OrganizationBranch.class)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc) @see com.nucleus.event.EventListener#handleEvent(com.nucleus.event.Event) */
    /**
     * If canHandleEvent() method returns true,
     * then come here and make the superAdmin(s) branch admin of
     * the OrganizationBranch in the MakerCheckerEvent.
     */
    @Override
    public void handleEvent(Event event) {
    	if(event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED)
    	{
        userOrgBranchMappingService.mapSuperAdminToNewlyCreatedBranch(((MakerCheckerEvent) event).getOwnerEntityId());
    	}
    	Map<String, Object> dataMap = new HashMap<>();
    	dataMap.put(OWNER_ENTITY_ID, ((MakerCheckerEvent) event).getOwnerEntityId());
    	dataMap.put(EVENT_TYPE, event.getEventType());
    	dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP, fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.ORGANIZATION_BRANCH_INFO_CACHE));
    	TransactionPostCommitWorker.handlePostCommit(organizationBranchCachePostCommitWorker, dataMap, true);
    	
    	
    }

}
