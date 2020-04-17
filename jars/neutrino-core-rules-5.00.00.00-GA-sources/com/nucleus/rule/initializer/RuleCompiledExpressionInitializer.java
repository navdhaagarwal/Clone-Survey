/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.rule.initializer;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.mvel2.MVEL;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.entity.SystemEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.SQLRule;
import com.nucleus.rules.model.ScriptParameter;
import com.nucleus.rules.model.ScriptRule;
import com.nucleus.rules.service.CompiledExpressionBuilder;
import com.nucleus.rules.service.RuleConstants;
import com.nucleus.rules.service.RuleService;

/**
 * Compile the approved rules at server startup
 * @author Nucleus Software Exports Limited
 *
 */
public class RuleCompiledExpressionInitializer implements RuleCompiledExpressionInitializerService {

    @Inject
    @Named(value = "ruleService")
    private RuleService               ruleService;

    @Inject
    @Named("compiledExpressionBuilder")
    private CompiledExpressionBuilder compiledExpressionBuilder;
    
    @Inject
    @Named("configurationService")
    protected ConfigurationService       configurationService;
    
    private static final String       RULE_SETUP_FLAG = "config.ruleSetup.flag";


    /**
     * Compile approved rules on server startup 
     * 
     */
    @Override
    public void invokeRuleCompiler(){
    	compileApprovedRule();
    }
    
    @PostConstruct
    protected void postContructCompileApprovedRule() {

    	boolean isRuleCompilationEnabled = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), RULE_SETUP_FLAG) != null ? 
    			Boolean.valueOf(configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), RULE_SETUP_FLAG).getPropertyValue()) : false;
    	if (isRuleCompilationEnabled){
    		compileApprovedRule();
    	}
    }
    
    private void compileApprovedRule(){
   	 String currentRuleName = "";

        try {
            /*
             * Fetch approved  and non compiled
             * rules from table
             */
            List<Rule> ruleList = ruleService.getAllApprovedRules();
            List<ScriptParameter> paramList = ruleService.getAllMvelBasedScriptParameters();

            if (ruleList != null && ruleList.size() > 0) {
                boolean isCompilationAllowed = true;
                /*
                 * Compile each rule
                 */
                for (Rule rule : ruleList) {
                    currentRuleName = rule.getName();

                    isCompilationAllowed = true;

                    if (rule.isCriteriaRuleFlag()) {
                        isCompilationAllowed = false;
                    }

                    if ((rule instanceof ScriptRule
                            && ((ScriptRule) rule).getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_SHELL_SCRIPT) || (rule instanceof SQLRule)) {
                        isCompilationAllowed = false;
                    }

                    if (isCompilationAllowed) {
                        compiledExpressionBuilder.buildAndCompileRuleExpression(rule.getId());
                    }
                }
            }

            if (paramList != null && paramList.size() > 0) {

                for (ScriptParameter parameter : paramList) {
                    parameter.setCompiledExpression(compileExpression(parameter.getScriptCodeValue()));
                }

            }

            // Compile Rule Group

            BaseLoggers.flowLogger.debug("Compiling Rule Group Expression -- Start"); 
            compiledExpressionBuilder.loadAndCompileRuleGroupExpression();
            BaseLoggers.flowLogger.debug("Compiling Rule Group Expression -- End");

            // Rule Action
            BaseLoggers.flowLogger.debug("Compiling Rule Action Expression -- Start");
            compiledExpressionBuilder.getAssigmentActionForCompilation();
            BaseLoggers.flowLogger.debug("Compiling Rule Group Expression -- End");

            // Assignment Matrix Actions
            BaseLoggers.flowLogger.debug("Compiling Assignment Matrix Actions Expression -- Start");
            compiledExpressionBuilder.getAssigmentMatrixActionForCompilation();
            BaseLoggers.flowLogger.debug("Compiling Assignment Matrix Actions Expression -- End");

        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Problem in Compiling Rule Expression: Rule is :: " + currentRuleName, e);
        }
   }

    private Serializable compileExpression(String expression) {
        Serializable compiledExpression = null;

        try {
            compiledExpression = MVEL.compileExpression(expression);
        } catch (Exception e) {
            BaseLoggers.flowLogger.debug("Error in compiling expression :: " + expression);
        }
        return compiledExpression;
    }

}
