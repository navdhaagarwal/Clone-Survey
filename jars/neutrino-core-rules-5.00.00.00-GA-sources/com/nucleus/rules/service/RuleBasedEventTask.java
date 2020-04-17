package com.nucleus.rules.service;

import com.nucleus.rules.model.RuleGroup;

public interface RuleBasedEventTask {

	public RuleGroup fetchRuleGroup();
	
	public void setRuleGroupInEventTask(RuleGroup ruleGroup);
	
	public Boolean isEventRuleBased();
}
