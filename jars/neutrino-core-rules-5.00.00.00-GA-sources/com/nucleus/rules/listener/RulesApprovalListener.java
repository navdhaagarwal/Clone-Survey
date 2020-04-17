package com.nucleus.rules.listener;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEventListener;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.service.CompiledExpressionBuilder;

@Named
public class RulesApprovalListener extends GenericEventListener {

    @Inject
    @Named("compiledExpressionBuilder")
    private CompiledExpressionBuilder compiledExpressionBuilder;

    /**
     * To Check If The event is a MakerCheckerEvent
     * And the Entity Is RULE, then return true else false
     * 
     */

    @Override
    public boolean canHandleEvent(Event event) {
        if (event instanceof MakerCheckerEvent
                && (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED || event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED)) {
            MakerCheckerEvent mcEvent = (MakerCheckerEvent) event;
            EntityId ruleEntityId = mcEvent.getOwnerEntityId();
            if (ruleEntityId.getEntityClass() != null && ruleEntityId.getEntityClass().equals(Rule.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If the True Is returned from Above method,
     * then come here and get the Rule Id from MakerCheckerEvent
     * and send it to CompiledExpressionBuilder Service to compile
     * the rule
     */
    @Override
    public void handleEvent(Event event) {
        MakerCheckerEvent mcEvent = (MakerCheckerEvent) event;
        EntityId ruleEntityId = mcEvent.getOwnerEntityId();
        if (ruleEntityId != null) {
            compiledExpressionBuilder.buildAndCompileRuleExpression(ruleEntityId.getLocalId());
        }
    }

}
