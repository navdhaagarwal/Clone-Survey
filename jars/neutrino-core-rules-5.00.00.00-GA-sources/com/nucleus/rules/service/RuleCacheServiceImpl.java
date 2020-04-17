package com.nucleus.rules.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.ScriptRule;

/**
 * This class handles is used to service Cache for all the Rules
 * 
 * @author rajesh.seth
 * 
 */

@Named(value = "ruleCacheService")
public class RuleCacheServiceImpl extends BaseRuleServiceImpl implements
		RuleCacheService {

	@Inject
	@Named("scriptRuleEvaluatorCachePopulator")
	private NeutrinoCachePopulator scriptRuleEvaluatorCachePopulator;
	
	public static final String SCRIPT_RULE_OBJECT = "SCRIPT_RULE_OBJECT";

	/**
	 * This method returns all ScriptRules from database.
	 * 
	 * @return
	 */
	@Override
	public List<ScriptRule> getAllScriptRulesFromDB() {
		BaseLoggers.flowLogger.debug(" Start getAllScriptRulesFromDB :: ");
		NamedQueryExecutor<ScriptRule> executor = new NamedQueryExecutor<ScriptRule>(
				"Rules.fetchAllByDType").addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
				.addQueryHint(QueryHint.QUERY_HINT_FETCHSIZE, 500);
		List<ScriptRule> scriptRules = entityDao.executeQuery(executor);
		BaseLoggers.flowLogger.debug(" End getAllScriptRulesFromDB :: ");
		return CollectionUtils.isNotEmpty(scriptRules) ? scriptRules : Collections.emptyList();
	}
	
	/**
	 * This method updates Script Rule Cache in case any Script Rule is updated
	 * from Rule Masters
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updateScriptRuleCache(Map<String,Object> dataMap) {
		ScriptRule scriptRule = (ScriptRule) dataMap.get(SCRIPT_RULE_OBJECT);
		scriptRuleEvaluatorCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.INSERT,scriptRule);
	}
	
	@Override
	public ScriptRuleEvaluator generateScriptRuleEvaluator(ScriptRule scriptRule) {
		try {
			StringBuilder dynamicClassSyntax = new StringBuilder();
			dynamicClassSyntax.append(getClassDeclarationWithRandomClassName(scriptRule))
				.append(ScriptRuleEvaluator.METHOD_DEFINITION_STRING)
				.append(scriptRule.getScriptCodeValue())
				.append(ScriptRuleEvaluator.DOUBLE_CLOSING_CURLY_BRACES);
			Class<?> clazz = GROOVY_CLASS_LOADER_RULE.parseClass(dynamicClassSyntax.toString());
			return (ScriptRuleEvaluator) clazz.newInstance();
		} catch (Exception e) {
			BaseLoggers.exceptionLogger
					.error("Error occured in generating ScriptRule for scriptCode id:: "
					+ scriptRule.getId()
					+ " and ScriptRule Name  "
					+ scriptRule.getName() + "::" + e, e);
		}
		return null;
	}
	
	@Override
	public String getClassDeclarationWithRandomClassName(ScriptRule scriptRule) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ScriptRuleEvaluator.CLASS_DECLARATION);
		if(ValidatorUtils.notNull(scriptRule.getCode())){
			stringBuilder.append(scriptRule.getCode().replaceAll("[^a-zA-Z0-9]", ""));
		}else{
			stringBuilder.append(getRandomStringOfNumbers());
		}
		if (ValidatorUtils.notNull(scriptRule.getId())) {
			stringBuilder.append(getRandomStringOfNumbers());
		} else {
			stringBuilder.append(scriptRule.getId());
		}
		stringBuilder.append(ScriptRuleEvaluator.IMPLEMENTS_STRING);
		stringBuilder.append(ScriptRuleEvaluator.OPENING_CURLY_BRACE);	
		return stringBuilder.toString();
	}

	/**
	 * Reducing the possibility of name conflict in generated class. 
	 * @return
	 */
	private String getRandomStringOfNumbers() {
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		sb.append(random.nextInt(100000));
		sb.append(random.nextInt(100000));
		sb.append(random.nextInt(100000));
		return sb.toString();
	}
	
	/**
	 * This method returns updated/approved rule from database based on the Id.
	 * 
	 * @param ruleId
	 * @return Rule
	 */
	@Override
	public Rule getRuleById(Long ruleId) {
		NamedQueryExecutor<Rule> executor = new NamedQueryExecutor<Rule>(
				"Rules.findRuleById")
				.addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST).addParameter(
						"ruleId", ruleId);
		return entityDao.executeQueryForSingleValue(executor);

	}

	/**
	 * This method returns ScriptRuleEvaluator from Cache based on scriptRuleId.
	 * 
	 * @param scriptRule
	 * @return ScriptRuleEvaluator
	 */
	@Override
	public ScriptRuleEvaluator getScriptRuleEvaluatorByIdFromCache(
			ScriptRule scriptRule) {
		ScriptRuleEvaluator scriptRuleEvaluator = null;
		if (scriptRule != null) {
			if (scriptRule.getId() != null ) {
				scriptRuleEvaluator = (ScriptRuleEvaluator) scriptRuleEvaluatorCachePopulator.get(scriptRule.getId());
			} else {
				scriptRuleEvaluator = generateScriptRuleEvaluator(scriptRule);
			}
		}
		return scriptRuleEvaluator;
	}

}
