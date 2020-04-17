/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.rules.service.RuleGroupEvaluationService;
import org.springframework.context.ApplicationListener;

import com.nucleus.rules.service.RuleInvocationService;

/**
 * @author Nucleus Software India Pvt Ltd
 */
@Named(value = "ruleInvocationEventListener")
public class RuleInvocationEventListener implements ApplicationListener<RuleInvocationEvent> {

	@Inject
	private RuleInvocationService ruleInvocationService;

	@Inject
	@Named("ruleGroupEvaluationService")
	private RuleGroupEvaluationService ruleGroupEvaluationService;

	public void onApplicationEvent(RuleInvocationEvent e) {
		RuleInvocationEventWorker worker = (RuleInvocationEventWorker) e.getEventWorker();
		Boolean result = true;
		if (null != worker.getIsRuleBased() && worker.getIsRuleBased()) {
			result = this.ruleGroupEvaluationService.executeRuleGroup(worker.getRuleGroup(), worker.getMap(),
					worker.getUuid(), worker.getName(), worker.isAuditingEnabled(), worker.isPurgingRequired());
		}
		if ((null != result) && (result.booleanValue()))
			worker.setRuleInvocationResult(ruleInvocationService.invokeRule(worker.getInvocationPoint(),
					worker.getMap(), worker.isAuditingEnabled(), worker.isPurgingRequired()));
	}
}