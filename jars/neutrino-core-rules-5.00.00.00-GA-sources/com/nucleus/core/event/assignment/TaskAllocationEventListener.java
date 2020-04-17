/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.assignment;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationListener;

import com.nucleus.rules.assignmentmatrix.service.TaskAssignmentMatrixEvaluationService;
import com.nucleus.rules.service.RuleGroupEvaluationService;

/**
 * 
 * @author Nucleus Software India Pvt Ltd
 */
@Named(value = "taskAssignmentEventListener")
public class TaskAllocationEventListener implements ApplicationListener<TaskAllocationEvent> {

	@Inject
	@Named("assignmentMatrixEvaluateService")
	private TaskAssignmentMatrixEvaluationService taskAssignmentMatrixEvaluationService;

	@Inject
	@Named("ruleGroupEvaluationService")
	private RuleGroupEvaluationService ruleGroupEvaluationService;

	@Override
	public void onApplicationEvent(TaskAllocationEvent e) {
		TaskAllocationEventWorker worker = (TaskAllocationEventWorker) e.getEventWorker();
		Boolean result = true;
		if (null != worker.getIsRuleBased() && worker.getIsRuleBased()) {
			result = this.ruleGroupEvaluationService.executeRuleGroup(worker.getRuleGroup(), worker.getContextMap(),
					worker.getUuid(), worker.getName(), worker.getAuditingEnabled(), worker.getPurgingRequired());
		}
		if ((null != result) && (result.booleanValue()))
			worker.setResultMap(taskAssignmentMatrixEvaluationService
					.executeTaskAssignMatrix(worker.getTaskAssignmentMaster(), worker.getContextMap()));

	}

}
