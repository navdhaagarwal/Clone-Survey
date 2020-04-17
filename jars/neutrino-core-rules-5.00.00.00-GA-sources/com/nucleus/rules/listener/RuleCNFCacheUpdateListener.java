package com.nucleus.rules.listener;

import com.nucleus.entity.*;
import com.nucleus.event.*;
import com.nucleus.persistence.*;
import com.nucleus.rules.model.*;
import com.nucleus.rules.service.*;

import javax.inject.*;

@Named
public class RuleCNFCacheUpdateListener extends GenericEventListener{

    @Inject
    @Named("ruleExpressionCNFComparisonService")
    private RuleExpressionCNFComparisonService ruleExpressionCNFComparisonService;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    @Override
    public boolean canHandleEvent(Event event) {
        if(event instanceof MakerCheckerEvent){

            MakerCheckerEvent mcEvent = (MakerCheckerEvent) event;

            if(mcEvent.getOwnerEntityId()!=null) {
                EntityId ruleEntityId = mcEvent.getOwnerEntityId();
                if (ruleEntityId.getEntityClass() != null && ruleEntityId.getEntityClass().equals(Rule.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void handleEvent(Event event) {

        MakerCheckerEvent mcEvent = (MakerCheckerEvent) event;
        EntityId ruleEntityId = mcEvent.getOwnerEntityId();
        if (ruleEntityId != null) {
            Rule rule = entityDao.find(Rule.class,ruleEntityId.getLocalId());
            String ruleExp = rule.getRuleExpression();
            String ruleCode = rule.getCode();
            Integer approvalStatus = rule.getApprovalStatus();
            long ruleId = rule.getId();
            if(event.getEventType() == EventTypes.MAKER_CHECKER_DELETION_APPROVED){
                ruleExpressionCNFComparisonService.deleteFromCnfMetaDataCache(ruleExp,ruleCode,ruleId);
            }
            if( event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED || event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED) {
                ruleExpressionCNFComparisonService.updateFromCnfMetaDataCache(ruleExp, ruleCode, ruleId, approvalStatus);
            }

        }
    }


}
