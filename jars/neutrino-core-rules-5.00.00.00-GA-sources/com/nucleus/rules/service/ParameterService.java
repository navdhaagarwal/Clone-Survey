package com.nucleus.rules.service;

import java.util.*;

import com.nucleus.rules.model.*;
import com.nucleus.service.BaseService;

import groovy.lang.GroovyClassLoader;

public interface ParameterService extends BaseService {
	public static final GroovyClassLoader GROOVY_CLASS_LOADER_PARAMETER = new GroovyClassLoader();
	
	public String getNameByObjectGraph(String name);
	public Parameter getApprovedParameterByName(String parameterName);
	public void updateParameterCache(Map<String,Object> dataMap);
	public ScriptParameterEvaluator getScriptParameterEvaluatorById(Long scriptParamId);
	public Parameter getParametersFromCacheById(Long id);
	public String getClassDeclarationWithRandomClassName(ScriptParameter scriptParamenter);
	public ScriptParameterEvaluator generateScriptParameterEvaluator(ScriptParameter scriptParameter);
	public boolean checkApprovalStatus(Parameter parameter);
	public <T extends Parameter> T getParameterByNameAndType(String parameterName, Class<T> entityClass);
	public <T extends Parameter> Long getParameterIdForParameterCacheByName(List<T> listOfAllParameters);
	public Map<Long, String> getDecryptedParamScriptIdMap(List<ScriptParameter> parameterList);
	public String decryptString(String scriptCode);
	/**
	 *
	 * Encrypt Script Param Query
	 * @param scriptRule
	 * @return
	 */

	public SQLParameter encryptSQLParam(SQLParameter scriptRule);

	/**
	 *
	 * Decrypt Script Param Query
	 * @param scriptRule
	 * @return
	 */
	public SQLParameter decryptSQLParam(SQLParameter scriptRule);

	public List<ParameterSimilarVO> findRecordForParameter(Parameter parameter,String parameterExp,String placeHolderName,String parameterValue,boolean isAdvanceRuleView);

	public Set<Rule> findRulesNeedToBeApprovedAgain(Long parameterId, String sourceProduct);

	public void sendNotificationTaskForRulesToBeApproved(String parmeterCode);
}
