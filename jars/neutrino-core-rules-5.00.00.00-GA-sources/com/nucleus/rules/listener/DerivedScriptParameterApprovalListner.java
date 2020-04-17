package com.nucleus.rules.listener;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEventListener;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.rules.model.DerivedParameter;
import com.nucleus.rules.service.CompiledExpressionBuilder;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Listener for derived parameters
 */

@Named
public class DerivedScriptParameterApprovalListner extends GenericEventListener {

    @Inject
    @Named("compiledExpressionBuilder")
    private CompiledExpressionBuilder compiledExpressionBuilder;

    /**
     * To Check If The event is a MakerCheckerEvent
     * And the Entity Is DerivedParameter, then return true else false
     * 
     */

    @Override
    public boolean canHandleEvent(Event event) {
        if (event instanceof MakerCheckerEvent
                && (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED || event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED)) {
            MakerCheckerEvent mcEvent = (MakerCheckerEvent) event;
            EntityId derivedParameterEntityId = mcEvent.getOwnerEntityId();
            if (derivedParameterEntityId.getEntityClass() != null
                    && derivedParameterEntityId.getEntityClass().equals(DerivedParameter.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If the True Is returned from Above method,
     * then come here and get the parameter Id from MakerCheckerEvent
     * and send it to CompiledExpressionBuilder Service to compile
     * the parameter
     */

    @Override
    public void handleEvent(Event event) {
        MakerCheckerEvent mcEvent = (MakerCheckerEvent) event;
        EntityId derivedParameterEntityId = mcEvent.getOwnerEntityId();
        if (derivedParameterEntityId != null) {
            compiledExpressionBuilder.buildAndCompileMvelScriptparameter(derivedParameterEntityId.getLocalId());
        }
    }

}
