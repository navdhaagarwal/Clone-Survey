/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.ruleValidation;

/**
 * @author Nucleus Software India Pvt Ltd
 */
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationListener;

import com.nucleus.rules.service.RuleGroupEvaluationService;

@Named(value = "ruleValidationEventListener")
public class RuleValidationEventListener implements ApplicationListener<RuleValidationEvent> {

    @Inject
    @Named("ruleGroupEvaluationService")
    private RuleGroupEvaluationService ruleGroupEvaluationService;

    @Override
    public void onApplicationEvent(RuleValidationEvent e) {
        RuleValidationEventWorker worker = (RuleValidationEventWorker) e.getEventWorker();

        Boolean result = ruleGroupEvaluationService.executeRuleGroup(worker.getRuleGroup(), worker.getMap(),
                worker.getUuid(), worker.getName(), worker.isAuditingEnabled(),worker.isPurgingRequired());

        worker.setRuleGroupResult(result);

    }

}
