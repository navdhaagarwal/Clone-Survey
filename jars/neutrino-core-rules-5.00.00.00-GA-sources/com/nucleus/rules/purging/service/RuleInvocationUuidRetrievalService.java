package com.nucleus.rules.purging.service;

import java.util.List;

import com.nucleus.core.purging.api.PurgeContext;

@FunctionalInterface
public interface RuleInvocationUuidRetrievalService {
	
	List<String> findRulesAuditLogUuidsForArchivalAndPurging(PurgeContext purgeContext);

}
