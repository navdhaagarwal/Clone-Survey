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
package com.nucleus.rules.service;

import java.io.Serializable;
import java.util.Map;

import com.nucleus.rules.utils.DataContext;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 */

public class RuleExpressionMvelEvaluator {

    public static String createNullSafeParameter(String parameter) {
        StringBuilder finalExp = new StringBuilder();

        if (StringUtils.isNotBlank(parameter)) {
            if (!((RuleConstants.OPERATORS.containsKey(parameter) || RuleConstants.LEFT_PAREN.equals(parameter) || RuleConstants.RIGHT_PAREN
                    .equals(parameter)))) {
                finalExp.append("checkNullValue(").append(parameter).append(")");
            }
        }
        return finalExp.toString();
    }

    public static Object evaluateExpression(String expression, Map contextMap) {
        try {
            if(contextMap instanceof DataContext){
                DataContext dataContext = (DataContext)contextMap;
                dataContext.setExecutionStarted(true);
            }
            return MVEL.eval(expression, contextMap, getMvelFactory(contextMap));
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception Occured : " + e.getMessage());
            return null;
        }

    }

    public static Object evaluateCompiledExpression(Serializable compiledExpression, Map contextMap) {
        try {
            if(contextMap instanceof DataContext){
                DataContext dataContext = (DataContext)contextMap;
                dataContext.setExecutionStarted(true);
            }
            return MVEL.executeExpression(compiledExpression, contextMap, getMvelFactory(contextMap));
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception Occured : " + e.getMessage());
            return null;
        }

    }

    private static VariableResolverFactory getMvelFactory(Map contextMap) {
        VariableResolverFactory functionFactory = new MapVariableResolverFactory(contextMap);
        MVEL.eval("checkNullValue = def (x) { x == null ? 0 : x };", functionFactory);

        return functionFactory;
    }
}
