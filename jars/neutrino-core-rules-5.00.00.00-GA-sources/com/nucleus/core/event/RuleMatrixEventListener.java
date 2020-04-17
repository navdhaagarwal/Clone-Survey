/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */

package com.nucleus.core.event;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.rules.service.RuleGroupEvaluationService;
import org.springframework.context.ApplicationListener;

import com.nucleus.rules.assignmentmatrix.service.AssignmentMatrixExecutionService;

/**
 * @author Nucleus Software India Pvt Ltd
 */
@Named(value = "ruleMatrixEventListener")
public class RuleMatrixEventListener implements ApplicationListener<RuleMatrixEvent> {

	@Inject
	@Named("assignmentMatrixExecutionService")
	private AssignmentMatrixExecutionService assignmentMatrixExecutionService;

	@Inject
	@Named("ruleGroupEvaluationService")
	private RuleGroupEvaluationService ruleGroupEvaluationService;

	public void onApplicationEvent(RuleMatrixEvent e) {
		RuleMatrixEventWorker worker = (RuleMatrixEventWorker) e.getEventWorker();
        Boolean result = true;
		if(worker.getIsRuleBased()) {
            result = this.ruleGroupEvaluationService.executeRuleGroup(worker.getRuleGroup(), worker.getContextmap(),

                    worker.getUuid(), worker.getName(), worker.getAuditingEnabled(), worker.getPurgingRequired());
        }
		if ((null != result) && (result.booleanValue()))
			assignmentMatrixExecutionService.executeRuleMatrix(worker.getRuleMatrixMaster(), worker.getContextmap());

	}
}