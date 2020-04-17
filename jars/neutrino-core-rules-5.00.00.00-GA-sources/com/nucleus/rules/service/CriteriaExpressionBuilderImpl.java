package com.nucleus.rules.service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import com.nucleus.rules.utils.DataContext;
import net.bull.javamelody.MonitoredWithSpring;

import org.apache.commons.collections4.CollectionUtils;
import org.mvel2.MVEL;

import com.nucleus.entity.EntityId;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.exception.RuleException;
import com.nucleus.rules.model.CompoundParameter;
import com.nucleus.rules.model.ConditionExpression;
import com.nucleus.rules.model.ConstantParameter;
import com.nucleus.rules.model.NullParameter;
import com.nucleus.rules.model.ObjectGraphParameter;
import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.ParameterDataType;
import com.nucleus.rules.model.ParameterExpression;
import com.nucleus.rules.model.PlaceHolderParameter;
import com.nucleus.rules.model.ReferenceParameter;
import com.nucleus.rules.model.RuleExpression;
import com.nucleus.rules.model.RuleGroupExpression;
import com.nucleus.rules.model.SystemParameter;
import com.nucleus.rules.model.SystemParameterType;

/**
 * 
 * Class to evaluate rule, condition and parameter and form query
 * to build criteria query
 */
@Named(value = "criteriaExpression")
public class CriteriaExpressionBuilderImpl extends BaseRuleServiceImpl implements CriteriaExpressionBuilder {

    @Override
    @MonitoredWithSpring(name = "CEBI_BUILD_CRITERIA_RULE_QUERY")
    public String buildCriteriaRuleQuery(RuleGroupExpression ruleGroupExpression, Map<Object, Object> map,
                                         Map<String, String> joinsMap) {
        String leftExpression = null;
        String rightExpression = null;

        if (isLeafNode(ruleGroupExpression)) {
            return buildRuleExpression(ruleGroupExpression.getRules().getExpression(), map, joinsMap);
        }

        if (ruleGroupExpression.getLeftExpression() != null) {
            leftExpression = buildCriteriaRuleQuery((RuleGroupExpression) ruleGroupExpression.getLeftExpression(), map,
                    joinsMap);
        }

        if (ruleGroupExpression.getRightExpression() != null) {
            rightExpression = buildCriteriaRuleQuery((RuleGroupExpression) ruleGroupExpression.getRightExpression(), map,
                    joinsMap);
        }

        String criteriaRuleOperator = RulesConverterUtility.getQueryRuleOperator(ruleGroupExpression.getOperator());
        return "(" + leftExpression + " " + criteriaRuleOperator + " " + rightExpression + ")";

    }


    public String buildRuleExpression(RuleExpression ruleExpression, Map<Object, Object> map, Map<String, String> joinsMap) {
        String leftExpression = null;
        String rightExpression = null;

        if (isLeafNode(ruleExpression)) {
            return (String) buildConditionExpression(ruleExpression.getConditions().getExpression(), map, joinsMap);
        }

        if (ruleExpression.getLeftExpression() != null) {
            leftExpression = buildRuleExpression((RuleExpression) ruleExpression.getLeftExpression(), map, joinsMap);
        }

        if (ruleExpression.getRightExpression() != null) {
            rightExpression = buildRuleExpression((RuleExpression) ruleExpression.getRightExpression(), map, joinsMap);
        }

        String ruleOperator = RulesConverterUtility.getQueryRuleOperator(ruleExpression.getOperator());
        return "(" + leftExpression + " " + ruleOperator + " " + rightExpression + ")";

    }

    private Object buildConditionExpression(ConditionExpression condition, Map<Object, Object> map,
            Map<String, String> joinsMap) {
        Object leftObject = null;
        Object rightObject = null;

        if (isLeafNode(condition)) {
            return evaluateParameter(condition.getParameter(), map, joinsMap);
        }

        if (condition.getLeftExpression() != null) {
            leftObject = buildConditionExpression((ConditionExpression) condition.getLeftExpression(), map, joinsMap);
        }

        if (condition.getRightExpression() != null) {
            rightObject = buildConditionExpression((ConditionExpression) condition.getRightExpression(), map, joinsMap);
        }

        String conditionOperator = getQueryConditionOperator(condition.getOperator(), leftObject, rightObject);
        return "( " + leftObject + " " + conditionOperator + " " + rightObject + " )";

    }

    protected Object evaluateParameter(Parameter parameter, Map<Object, Object> map, Map<String, String> joinsMap) {
        if (parameter == null) {
            throw new RuleException("Parameter Cannot be null/empty");
        }
        if(map instanceof DataContext){
            DataContext dataContext = (DataContext)map;
            dataContext.setExecutionStarted(true);
        }
        Object parameterValue = null;

        if (parameter instanceof PlaceHolderParameter) {
            if (null != ((PlaceHolderParameter) parameter).getObjectGraph()
                    && !((PlaceHolderParameter) parameter).getObjectGraph().equals("")) {

                if (((PlaceHolderParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
                    parameterValue = MVEL.eval(
                            RulesConverterUtility.getNullSafeObjectGraph(((PlaceHolderParameter) parameter).getObjectGraph()
                                    + RuleConstants.RULE_IDS), map);
                } else {
                    parameterValue = MVEL.eval(RulesConverterUtility
                            .getNullSafeObjectGraph(((PlaceHolderParameter) parameter).getObjectGraph()), map);
                }

            } else {
                parameterValue = MVEL.eval(((PlaceHolderParameter) parameter).getContextName(), map);
            }

        } else if (parameter instanceof ObjectGraphParameter) {
            String objectGraph = "";
            Class clazz = null;
            objectGraph = ((ObjectGraphParameter) parameter).getObjectGraph();
            Set<String> baseContextObjectNameList = null;
            try {
                if(map.containsKey(GridCriteriaRuleConstants.BASE_CONTEXT_OBJECT_NAME_LIST) ) {
                    baseContextObjectNameList = (Set<String>) map.get(GridCriteriaRuleConstants.BASE_CONTEXT_OBJECT_NAME_LIST);
                }
                if(CollectionUtils.isEmpty(baseContextObjectNameList) || !(baseContextObjectNameList.contains(objectGraph.split("\\.")[0]) || objectGraph.split("\\.")[0].contains(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION))) {
                    clazz = Class.forName(ContextObjectClass.getClassName(objectGraph.split("\\.")[0]));
                }
                if(map.containsKey(GridCriteriaRuleConstants.LEFT_EXPRESSION_MAP) ) {
                    ((HashMap)map.get(GridCriteriaRuleConstants.LEFT_EXPRESSION_MAP)).put(objectGraph.split("\\.")[0], objectGraph);
                }
            } catch (Exception e) {
                BaseLoggers.exceptionLogger.debug("Error" + e);
                throw new RuleException(e.getMessage());
            }

            if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
                parameterValue = getPath(clazz, objectGraph, joinsMap) + RuleConstants.RULE_TIME_IN_MILLIS;
            } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                parameterValue = getPath(clazz, objectGraph, joinsMap) + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE;
            } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
                if(CollectionUtils.isNotEmpty(baseContextObjectNameList) && (baseContextObjectNameList.contains(objectGraph.split("\\.")[0]) || objectGraph.split("\\.")[0].contains(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION))) {
                    if(objectGraph.endsWith(RuleConstants.RULE_IDS)) {
                        parameterValue = objectGraph;
                    }else{
                        parameterValue = objectGraph + RuleConstants.RULE_IDS;
                    }
                }else {
                    parameterValue = getPath(clazz, objectGraph, joinsMap) + RuleConstants.RULE_IDS;
                }
            } else {
                if(CollectionUtils.isNotEmpty(baseContextObjectNameList) && (baseContextObjectNameList.contains(objectGraph.split("\\.")[0]) || objectGraph.split("\\.")[0].contains(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION))) {
                    parameterValue = objectGraph;
                }else {
                    parameterValue = getPath(clazz, objectGraph, joinsMap);
                }
            }

        } else if (parameter instanceof ConstantParameter) {
            if (parameter.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING) {
                parameterValue = "'" + ((ConstantParameter) parameter).getLiteralValue() + "'";
            } else {
                parameterValue = ((ConstantParameter) parameter).getLiteralValue();
            }

        } else if (parameter instanceof ReferenceParameter) {
            parameterValue = entityDao.get(((ReferenceParameter) parameter).getReferenceEntityId()).getId();

        } else if (parameter instanceof SystemParameter) {

            if (((SystemParameter) parameter).getSystemParameterType() == SystemParameterType.SYSTEM_PARAMETER_TYPE_CURRENT_USER) {

                parameterValue = entityDao.get(EntityId.fromUri(map.get("user.referenceURI").toString()));

            } else {
                parameterValue = ((SystemParameter) parameter).getSystemParameterValue();
            }

        }

        else if (parameter instanceof CompoundParameter) {
            parameterValue = buildParameterExpression(((CompoundParameter) parameter).getExpression(), map, joinsMap);
            return parameterValue.toString();

        } else if (parameter instanceof NullParameter) {
            parameterValue = RuleConstants.IS_NULL_JPQL;

        }

        return parameterValue;
    }

    protected Object buildParameterExpression(ParameterExpression parameterExpression, Map<Object, Object> map,
            Map<String, String> joinsMap) {
        Object leftObject = null;
        Object rightObject = null;

        if (isLeafNode(parameterExpression)) {

            return evaluateParameter(parameterExpression.getParameter(), map, joinsMap);
        }

        if (parameterExpression.getLeftExpression() != null) {
            leftObject = buildParameterExpression((ParameterExpression) parameterExpression.getLeftExpression(), map,
                    joinsMap);
        }

        if (parameterExpression.getRightExpression() != null) {
            rightObject = buildParameterExpression((ParameterExpression) parameterExpression.getRightExpression(), map,
                    joinsMap);
        }

        return setParamValue(leftObject, parameterExpression.getOperator(), rightObject);

    }

    private Object setParamValue(Object leftObject, String operator, Object rightObject) {
        Object paramValue = null;
        if (operator.equals("+")) {
            paramValue = (Long) leftObject + (Long) rightObject;
        } else if (operator.equals("-")) {
            paramValue = (Long) leftObject - (Long) rightObject;
        } else if (operator.equals("*")) {
            paramValue = (Long) leftObject * (Long) rightObject;
        } else if (operator.equals("/")) {
            paramValue = (Double) leftObject / (Double) rightObject;
        }
        return paramValue;

    }

    private String getQueryConditionOperator(String operator, Object leftObject, Object rightObject) {
        if (operator.equals("==")) {
            if ((leftObject instanceof String && ((String) leftObject).indexOf("'") != -1)
                    || (rightObject instanceof String && ((String) rightObject).indexOf("'") != -1)) {
                return "like";

            } else if (rightObject instanceof String && ((String) rightObject).indexOf(RuleConstants.IS_NULL_JPQL) != -1) {
                return "";

            } else {
                return "=";
            }
        } else {
            return operator;
        }
    }

    /**
     * 
     * Method to get the path
     * @param clazz
     * @param ognl
     * @param joinsMap
     * @return
     */

    public String getPath(Class clazz, String ognl, Map<String, String> joinsMap) {
        Field field;
        Type type;
        try {
            String[] ognlArray = ognl.split("\\.");
            String path = ognlArray[0];
            String alias = "join" + "_" + ognlArray[0];
            for (int index = 1 ; index < ognlArray.length ; index++) {
                field = retrieveField(ognlArray[index], clazz);
                type = field.getGenericType();
                path = path + "." + ognlArray[index];
                alias = alias + "_" + ognlArray[index];
                if (type instanceof ParameterizedType) {
                    if (!joinsMap.containsKey(alias)) {
                        joinsMap.put(alias, path);
                    }
                    path = alias;
                    Type t = ((ParameterizedType) type).getActualTypeArguments()[0];
                    clazz = (Class) t;
                } else {
                    clazz = (Class) type;
                }
            }
            return path;
        } catch (SecurityException e) {
            BaseLoggers.exceptionLogger.debug("Error" + e);
            throw new RuleException(e.getMessage());
        }
    }

    /**
     * 
     * Method to get the field recursively.
     * @param ognl
     * @param clazz
     * @return
     */
    private Field retrieveField(String ognl, Class clazz) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(ognl);
            return field;
        } catch (SecurityException e) {
            BaseLoggers.exceptionLogger.debug("Error" + e);
            throw new RuleException(e.getMessage());
        } catch (NoSuchFieldException e) {
            if (clazz.getSimpleName().equalsIgnoreCase("BaseEntity")) {
                BaseLoggers.exceptionLogger.debug("Error" + e);
                throw new RuleException(e.getMessage());
            } else {
                clazz = clazz.getSuperclass();
                return retrieveField(ognl, clazz);
            }
        }
    }
}
