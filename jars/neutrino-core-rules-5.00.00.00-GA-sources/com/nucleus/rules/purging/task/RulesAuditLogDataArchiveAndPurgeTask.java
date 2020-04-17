package com.nucleus.rules.purging.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.purging.api.ArchiveAndPurgeTask;
import com.nucleus.core.purging.api.PurgeContext;
import com.nucleus.core.purging.api.PurgeTableInfo;
import com.nucleus.core.purging.api.PurgeUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.purging.service.RuleInvocationUuidRetrievalService;

public class RulesAuditLogDataArchiveAndPurgeTask implements ArchiveAndPurgeTask {

	private RuleInvocationUuidRetrievalService ruleInvocationUuidRetrievalService;

	private static final LinkedHashMap<String[], PredicateColumn> RULES_AUDIT_TABLES_MIGRATION_SCRIPTS = new LinkedHashMap<>();
	
	private static final String ID_COL                             = "ID";
	
	private static String RULES_AUDIT_LOG_PARAM_VAL = "RULES_AUDIT_LOG_PARAM_VAL";
	
	private static String RULES_AUDIT_LOG = "RULES_AUDIT_LOG";
	
	static {

		// Select queries
		RULES_AUDIT_TABLES_MIGRATION_SCRIPTS.put(
				new String[] { "RULES_AUDIT_LOG",
						"SELECT ID FROM RULES_AUDIT_LOG  SRC WHERE SRC.RULE_INVOCATIONUUID in (:paramIds)" },
				PredicateColumn.RULE_INVOCATIONUUID);

		RULES_AUDIT_TABLES_MIGRATION_SCRIPTS.put(
				new String[] { "RULES_AUDIT_LOG_PARAM_VAL",
						"SELECT ID FROM RULES_AUDIT_LOG_PARAM_VAL  SRC WHERE SRC.RULES_AUDIT_LOG_FK in (:paramIds)" },
				PredicateColumn.RULES_AUDIT_LOG_FK);
	}

	@Override
	public Map<String, List<Object>> findRecordsForArchiveAndPurge(PurgeContext purgeContext) {
		NamedParameterJdbcTemplate jdbcTemplate = purgeContext.getJdbcTemplate();
		List<String> rulesAuditUuids = null;
		boolean ruleArchivalAndPurgingServiceEnabled = false;
		if (ruleInvocationUuidRetrievalService != null) {
			ruleArchivalAndPurgingServiceEnabled = true;
			rulesAuditUuids = ruleInvocationUuidRetrievalService
					.findRulesAuditLogUuidsForArchivalAndPurging(purgeContext);
		} else {
			try{
				RuleInvocationUuidRetrievalService defaultRuleInvocationUuidRetrievalService = NeutrinoSpringAppContextUtil
						.getBeanByName("defaultRuleInvocationUuidRetrievalService",
								RuleInvocationUuidRetrievalService.class);
				if (defaultRuleInvocationUuidRetrievalService != null) {
					ruleArchivalAndPurgingServiceEnabled = true;
					rulesAuditUuids = defaultRuleInvocationUuidRetrievalService
							.findRulesAuditLogUuidsForArchivalAndPurging(purgeContext);
				}
			}catch (NoSuchBeanDefinitionException e) {
				BaseLoggers.exceptionLogger.error("No bean named 'defaultRuleInvocationUuidRetrievalService' is defined."+e.getMessage());
			}
		}

		if (!ruleArchivalAndPurgingServiceEnabled) {
			throw new IllegalStateException(
					"Atleast one implementation for service 'RuleInvocationUuidRetrievalService' is required.");
		}
		// select primary keys for each table to archive and purge
		Map<String, List<Object>> tableToIdMap = new HashMap<>();
		if (rulesAuditUuids != null && !rulesAuditUuids.isEmpty()) {
			prepareTableToIdMap(tableToIdMap, rulesAuditUuids, jdbcTemplate);
		}
		return tableToIdMap;
	}

	private void prepareTableToIdMap(Map<String, List<Object>> tableToIdMap, List<String> rulesAuditUuids,
			NamedParameterJdbcTemplate jdbcTemplate) {
		List<String> rulesAuditLogIds = null;
		for (Entry<String[], PredicateColumn> entry : RULES_AUDIT_TABLES_MIGRATION_SCRIPTS.entrySet()) {

			// create an entry for table if not exists already
			String tableName = entry.getKey()[0];
			if (tableToIdMap.get(tableName) == null) {
				tableToIdMap.put(tableName, new ArrayList<Object>());
			}
			if (entry.getValue().equals(PredicateColumn.RULE_INVOCATIONUUID)) {
				rulesAuditLogIds = PurgeUtils.queryForListWithSingleInClause(entry.getKey()[1], "paramIds",
						rulesAuditUuids, String.class, jdbcTemplate);
				tableToIdMap.get(tableName).addAll(rulesAuditLogIds);
			} else if (entry.getValue().equals(PredicateColumn.RULES_AUDIT_LOG_FK)) {
				List<String> rulesAuditLogParamValIds = PurgeUtils.queryForListWithSingleInClause(entry.getKey()[1],
						"paramIds", rulesAuditLogIds, String.class, jdbcTemplate);
				tableToIdMap.get(tableName).addAll(rulesAuditLogParamValIds);
			}
		}
	}

	private enum PredicateColumn {
		RULE_INVOCATIONUUID, RULES_AUDIT_LOG_FK
	}

	

	public void setRuleInvocationUuidRetrievalService(
			RuleInvocationUuidRetrievalService ruleInvocationUuidRetrievalService) {
		this.ruleInvocationUuidRetrievalService = ruleInvocationUuidRetrievalService;
	}

	@Override
	public void addTablesInDeletionOrder(Set<PurgeTableInfo> tableNames) {
		tableNames.add(PurgeTableInfo.from(RULES_AUDIT_LOG_PARAM_VAL, "RULES_AUDIT_LOG_PARAM_VAL_ARCH", ID_COL));
		tableNames.add(PurgeTableInfo.from(RULES_AUDIT_LOG, "RULES_AUDIT_LOG_ARCH", ID_COL));		
	}
	
	

}
