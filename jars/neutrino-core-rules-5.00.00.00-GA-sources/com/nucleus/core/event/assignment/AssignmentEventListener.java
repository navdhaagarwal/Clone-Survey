/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.assignment;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationListener;

import com.nucleus.rules.assignmentmatrix.service.AssignmentMatrixExecutionService;
import com.nucleus.rules.service.RuleGroupEvaluationService;

/**
 * 
 * @author Nucleus Software India Pvt Ltd
 */
@Named(value = "assignmentEventListener")
public class AssignmentEventListener implements ApplicationListener<AssignmentEvent> {

	@Inject
	@Named("assignmentMatrixExecutionService")
	private AssignmentMatrixExecutionService assignmentMatrixExecutionService;

	@Inject
	@Named("ruleGroupEvaluationService")
	private RuleGroupEvaluationService ruleGroupEvaluationService;

	public void onApplicationEvent(AssignmentEvent e) {
 		AssignmentEventWorker worker = (AssignmentEventWorker) e.getEventWorker();
		Boolean result = true;
		if (null != worker.getIsRuleBased() && worker.getIsRuleBased()) {
			result = this.ruleGroupEvaluationService.executeRuleGroup(worker.getRuleGroup(), worker.getContextmap(),

					worker.getUuid(), worker.getName(), worker.getAuditingEnabled(), worker.getPurgingRequired());
		}
		if ((null != result) && (result.booleanValue()))
			assignmentMatrixExecutionService.executeAssignMatrix(worker.getAssignmentMaster(), worker.getContextmap());

	}
}