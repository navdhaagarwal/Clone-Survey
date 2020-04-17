package com.nucleus.rules.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.rules.model.RulesAuditLog;

@Named(value = "postCommitWorkerForRulesAuditLogData")
public class PostCommitWorkerForRulesAuditLogData implements TransactionPostCommitWork {

	@Inject
	@Named("rulesAuditLogService")
	private RulesAuditLogService rulesAuditLogService;
	
	

	@Override
	public void work(Object argument) {
		List<RulesAuditLog> ruleAuditLogList = (List<RulesAuditLog>) argument;
		rulesAuditLogService.saveRuleAuditLogData(ruleAuditLogList);
	}
}
