package com.nucleus.rules.service;

import java.util.List;
import java.util.Map;

import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.ScriptRule;
import com.nucleus.service.BaseService;

import groovy.lang.GroovyClassLoader;

/**
 * Rule Service Cache interface.
 * 
 * @author rajesh.seth
 * 
 */
public interface RuleCacheService extends BaseService{

	public static final GroovyClassLoader GROOVY_CLASS_LOADER_RULE = new GroovyClassLoader();

	public ScriptRuleEvaluator getScriptRuleEvaluatorByIdFromCache(ScriptRule scriptRule);

	public Rule getRuleById(Long ruleId);

	public ScriptRuleEvaluator generateScriptRuleEvaluator(ScriptRule scriptRule);

	public String getClassDeclarationWithRandomClassName(ScriptRule scriptRule);

	public List<ScriptRule> getAllScriptRulesFromDB();

	void updateScriptRuleCache(Map<String,Object> dataMap);

	public String decryptString(String scriptCode);

}
