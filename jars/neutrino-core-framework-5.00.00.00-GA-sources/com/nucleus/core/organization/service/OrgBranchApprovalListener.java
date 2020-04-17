package com.nucleus.core.organization.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEventListener;
import com.nucleus.event.MakerCheckerEvent;

@Named
public class OrgBranchApprovalListener extends GenericEventListener {

    @Inject
    @Named("organizationService")
    private OrganizationService organizationService;

    @Override
    public boolean canHandleEvent(Event event) {
        if (event instanceof MakerCheckerEvent
                && (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED || event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED)) {
            MakerCheckerEvent mcEvent = (MakerCheckerEvent) event;
            EntityId orgBranchEntityId = mcEvent.getOwnerEntityId();
            if (orgBranchEntityId.getEntityClass() != null
                    && orgBranchEntityId.getEntityClass().equals(OrganizationBranch.class)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        MakerCheckerEvent mcEvent = (MakerCheckerEvent) event;
        EntityId orgBranchEntityId = mcEvent.getOwnerEntityId();
        if (orgBranchEntityId != null) {
            organizationService.postOrgBranchApprovalAction(orgBranchEntityId);
        }
    }

}
