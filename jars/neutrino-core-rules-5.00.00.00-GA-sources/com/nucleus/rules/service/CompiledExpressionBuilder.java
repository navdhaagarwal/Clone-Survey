/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.rules.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.nucleus.rules.model.AssignmentAction;
import com.nucleus.rules.model.DerivedParameter;
import com.nucleus.rules.model.ObjectGraphTypes;
import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.RuleAction;
import com.nucleus.rules.model.ScriptRule;
import com.nucleus.service.BaseService;

/**
 * @author Nucleus Software Exports Limited
 *
 */

public interface CompiledExpressionBuilder extends BaseService {

    /**
     * Build Rule Expression By Value
     * @param rule
     * @return
     */
    public void buildAndCompileRuleExpression(long ruleId);

    /**
     * 
     * Method to evaluate the Null safe compile expression
     * @param ruleId
     * @param map
     * @return
     * @throws IOException 
     */
    public char evaluateRule(long ruleId, Map map);

    /**
     * 
     * Method to evaluate the Normal compile expression
     * @param ruleId
     * @param map
     * @return
     */
    public char evaluateRule(long ruleId, Map map, boolean isStrictEvaluation);

    /**
     * Method to generate compiled expression for derived parameter 
     * @param scriptParameterId
     */
    public void buildAndCompileMvelScriptparameter(long scriptParameterId);

    /**
     * Method to generate script code of derived parameter
     * @param derivedParameter
     * @return
     */
    public String buildExpressionOfDerivedParameter(DerivedParameter derivedParameter);

    /**
     * 
     * Method to build Mvel Expression
     *      for AssignMent Action Master
     *      Also to Compile the same
     * @param assignmentAction
     * @return
     */
    public AssignmentAction buildExpressionForAssignmentActions(AssignmentAction assignmentAction);

    /**
     * 
     * Method to execute the ruleAction
     * @param ruleAction
     * @param map
     */
    public void executeAssignmentAction(RuleAction ruleAction, Map<Object, Object> map);

    /**
     * 
     * Compile the Mvel Script Rule
     * @param rule
     */
    public void compileMvelScriptRule(ScriptRule rule, Set<Parameter> parameters);

    /**
     * 
     * Method to build parameter expression
     * Also compile the same
     * @param parameter
     * @param parameters
     * @param objectGraph
     * @param isNullSafeExp
     * @return
     */
    public String buildParameterExpressionToCompile(Parameter parameter, Set<Parameter> parameters, Set<String> objectGraph,
            boolean isNullSafeExp, String[] expression, int currentIndex);

    /**
     * 
     * Method to build the rule level expression
     * Also compile the same.
     * @param expression
     * @return
     */
    public Serializable compileRuleLevelExp(String expression);

    /**
     * 
     * Method to load the Rule Group and
     * compile the expression
     */

    public void loadAndCompileRuleGroupExpression();

    /**
     * 
     * get rule actions for compilation
     * @return
     */
    public void getAssigmentActionForCompilation();

    /**
     * 
     * get AssigmentMatrixAction For Compilation
     * @return
     */
    public void getAssigmentMatrixActionForCompilation();

	public String buildNullSafeExpressionToCompile(ObjectGraphTypes objectGraph);
}
