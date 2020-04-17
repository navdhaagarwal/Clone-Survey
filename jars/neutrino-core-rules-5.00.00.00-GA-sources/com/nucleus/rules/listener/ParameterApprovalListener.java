package com.nucleus.rules.listener;

import com.nucleus.core.notification.*;
import com.nucleus.document.core.entity.*;
import com.nucleus.entity.*;
import com.nucleus.event.*;
import com.nucleus.logging.*;
import com.nucleus.persistence.*;
import com.nucleus.rules.model.*;
import com.nucleus.rules.service.*;
import com.nucleus.user.*;
import org.apache.commons.collections.*;

import javax.inject.*;
import java.util.*;

@Named
public class ParameterApprovalListener extends GenericEventListener {


    @Inject
    @Named("parameterService")
    private ParameterService parameterService;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;


    @Override
    public boolean canHandleEvent(Event event) {
        if(event instanceof MakerCheckerEvent){

            MakerCheckerEvent mcEvent = (MakerCheckerEvent) event;

            if(mcEvent.getOwnerEntityId()!=null) {
                EntityId ruleEntityId = mcEvent.getOwnerEntityId();
                if (ruleEntityId.getEntityClass() != null && ruleEntityId.getEntityClass().getSuperclass()!=null
                        && (ruleEntityId.getEntityClass().getSuperclass().equals(Parameter.class) || (ruleEntityId.getEntityClass().getSuperclass().getSuperclass()!=null && ruleEntityId.getEntityClass().getSuperclass().getSuperclass().equals(Parameter.class)))) {
                    return true;
                }
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
        if( event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED || event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED) {

            EntityId parameterId = mcEvent.getOwnerEntityId();
            Parameter parameter = entityDao.find(Parameter.class, parameterId.getLocalId());
            if (parameter != null) {
                Set<Rule> ruleList = parameterService.findRulesNeedToBeApprovedAgain(parameterId.getLocalId(), parameter.getSourceProduct());

                if (CollectionUtils.isNotEmpty(ruleList)) {
                    try {
                        parameterService.sendNotificationTaskForRulesToBeApproved(parameter.getName());
                    } catch (Exception e) {
                        BaseLoggers.exceptionLogger.error("Error while calling warning message for rules needed to be approved",e);
                    }
                }
            }
        }
    }


}
