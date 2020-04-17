package com.nucleus.rules.assignmentmatrix.service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.assignmentMatrix.*;
import com.nucleus.rules.service.*;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.MapQueryExecutor;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.datatable.DataTableJsonHelper;
import com.nucleus.rules.model.*;
import flexjson.JSONSerializer;
import net.bull.javamelody.MonitoredWithSpring;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONObject;

import com.nucleus.core.dynamicform.service.FormDefinitionService;
import com.nucleus.core.formsConfiguration.FormConfigEntityData;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.exception.RuleException;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMasterType;
import org.springframework.ui.ModelMap;
import org.springframework.util.ReflectionUtils;


import static com.nucleus.rules.model.assignmentMatrix.AssignmentConstants.operatorsSupportedByDataTypeMap_ASSIGN;
import static java.util.Comparator.comparing;

@Named(value = "assignmentMatrixService")
@MonitoredWithSpring(name = "AssignmentMatrix_Service_IMPL_")
public class AssignmentMatrixServiceImpl extends BaseRuleServiceImpl implements AssignmentMatrixService {

    @Inject
    @Named("formDefinitionService")
    private FormDefinitionService     formDefinitionService;

    @Inject
    @Named("compiledExpressionBuilder")
    private CompiledExpressionBuilder compiledExpressionBuilder;

    @Inject
    @Named("ruleService")
    private RuleService               ruleService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService   genericParameterService;
    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    @Inject
    @Named(value = "makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("ruleMatrixMasterService")
    private RuleMatrixMasterService ruleMatrixMasterService;

    public static final String CONTEXT_OBJECT_FOR_AGGREGATE_FUNCTION = "contextObjectParamListForAggregateFunction";

    private static final int DEFAULT_SIZE = 3;

    @Override
    public void populateAssignmentMasterFields(BaseAssignmentMaster baseAssignmentMaster) {
        if (null != baseAssignmentMaster && null != baseAssignmentMaster.getAssignmentSet()
                && baseAssignmentMaster.getAssignmentSet().size() > 0) {

            for (AssignmentSet assignmentSet : baseAssignmentMaster.getAssignmentSet()) {

                if (assignmentSet instanceof AssignmentGrid) {
                    populateAssignmentGridProperties((AssignmentGrid) assignmentSet);

                } else if (assignmentSet instanceof AssignmentExpression) {
                    populateAssignmentExpProperties((AssignmentExpression) assignmentSet);
                }
            }
        }

    }

    @Override
    public void populateAssignmentExpProperties(AssignmentExpression assignmentExpression) {

        // End of loop for one row - now create rule here
        ScriptRule scriptRule = new ScriptRule();
        scriptRule.setScriptCodeType(RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT);
        scriptRule.setApprovalStatus(ApprovalStatus.APPROVED);
        scriptRule.getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.EMPTY_PARENT);
        Set<Parameter> parameters = new HashSet<Parameter>();

        String expressionWithParameterId = "";
        for (AssignmentMatrixRowData assignmentMatrixRowData : assignmentExpression.getAssignmentMatrixRowData()) {
            expressionWithParameterId = getParameterExpressionWthId(assignmentMatrixRowData.getExpression());
            scriptRule.setScriptCode(processExpToFormRuleExp(expressionWithParameterId, parameters));

            compiledExpressionBuilder.compileMvelScriptRule(scriptRule, parameters);
            assignmentMatrixRowData.setRule(scriptRule);

            // Compiling assignment action script and saving it
            if (!assignmentExpression.isTaskAssignment()
                    && (assignmentExpression.isRuleMatrix() == null || !assignmentExpression.isRuleMatrix())) {
                compileAndSaveScript(assignmentMatrixRowData.getAssignmentMatrixAction(),
                        assignmentExpression.getAssignmentActionFieldMetaDataList(),null);
            }
            if (assignmentExpression.isRuleMatrix() != null && assignmentExpression.isRuleMatrix()) {
                compileAndSaveScriptForRuleMatrix(assignmentMatrixRowData.getAssignmentMatrixAction(),
                        assignmentExpression.getAssignmentActionFieldMetaDataList());
            }
        }

    }

    @Override
    public void populateAssignmentGridProperties(AssignmentGrid assignmentGrid) {

        /* stores field name as key and value as value
             Say example (Age > 21)
                 key is Age
                 Value is 21
        */
        Map<String, Object> fieldKeyValueMap = null;

        // Map to store condition name as key and value as its expression

        /* stores field name as key and value as value
         * example expression is (Age) && (fName)
            key is Age, Value is Age > 21
            key is fName, Value is fName!=Empty
        */
        Map<String, String> conditionNameExpression = null;
        Set<Parameter> parametersSet = null;

        if (null != assignmentGrid && null != assignmentGrid.getAssignmentFieldMetaDataList()
                && assignmentGrid.getAssignmentFieldMetaDataList().size() > 0
                && null != assignmentGrid.getAssignmentMatrixRowData()
                && assignmentGrid.getAssignmentMatrixRowData().size() > 0) {

            int i = 0;
            String exp = "";

            for (AssignmentMatrixRowData assignmentMatrixRowData : assignmentGrid.getAssignmentMatrixRowData()) {

                // counter to create rules names with incremented values
                i++;
                if(assignmentMatrixRowData.getEditedOrNewFlag()!=null && assignmentMatrixRowData.getEditedOrNewFlag()){
                exp = null;

                // Intialize the Parameter Set for each AssignmentMatrixRowData
                parametersSet = new HashSet<Parameter>();

                // Intialize for each row of grid
                conditionNameExpression = new HashMap<String, String>();
                fieldKeyValueMap = new HashMap<String, Object>();

                // converted json string to map for one row
                fieldKeyValueMap = AssignmentMatrixMasterUtility.convertJsonToMap(assignmentMatrixRowData.getRowMapValues());
                StringBuilder ruleExp = new StringBuilder();
                for (AssignmentFieldMetaData assignmentFieldMetaData : assignmentGrid.getAssignmentFieldMetaDataList()) {
                    if(null != assignmentFieldMetaData.getRuleBased() && assignmentFieldMetaData.getRuleBased()){
                        if(0==ruleExp.length()){
                            ruleExp.append(assignmentFieldMetaData.getIndexId()+":"+fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));
                        }else{
                            ruleExp.append("," +assignmentFieldMetaData.getIndexId()+":"+fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));
                        }
                        continue;
                    }
                    if (assignmentGrid.isTaskAssignment()) {
                        exp = getConditionExpression(fieldKeyValueMap, assignmentFieldMetaData);
                        if (StringUtils.isNotBlank(exp)) {
                            conditionNameExpression.put(assignmentFieldMetaData.getIndexId(), exp);
                        }
                    } else if (assignmentGrid.isRuleMatrix() != null && assignmentGrid.isRuleMatrix()) {
                        if (assignmentFieldMetaData.getParameterBased() != null
                                && assignmentFieldMetaData.getParameterBased()) {
                            exp = getConditionExpression(fieldKeyValueMap, assignmentFieldMetaData, parametersSet);
                        } else {
                            exp = getConditionExpression(fieldKeyValueMap,
                                    assignmentFieldMetaData);
                        }

                        if (StringUtils.isNotBlank(exp)) {
                            conditionNameExpression.put(assignmentFieldMetaData.getIndexId(), exp);
                        }
                    } else {
                        if (assignmentFieldMetaData.getParameterBased() != null
                                && assignmentFieldMetaData.getParameterBased()) {
                            exp = getConditionExpression(fieldKeyValueMap, assignmentFieldMetaData, parametersSet);
                        } else {
                            exp = getConditionExpression(fieldKeyValueMap,
                                    assignmentFieldMetaData);
                        }
                        if (StringUtils.isNotBlank(exp)) {
                            conditionNameExpression.put(assignmentFieldMetaData.getIndexId(), exp);
                        }
                    }

                }
                // End of loop for one row - now create rule here
                ScriptRule scriptRule = new ScriptRule();
                if (assignmentGrid.isTaskAssignment()) {
                    scriptRule = createScriptRule(conditionNameExpression, assignmentGrid.getGridLevelExpressionId(),
                            assignmentGrid.getAssignmentSetName(), i, null);
                } else if (assignmentGrid.isRuleMatrix() != null && assignmentGrid.isRuleMatrix()) {
                    scriptRule = createScriptRule(conditionNameExpression, assignmentGrid.getGridLevelExpressionId(),
                            assignmentGrid.getAssignmentSetName(), i, parametersSet);
                } else {
                    scriptRule = createScriptRule(conditionNameExpression, assignmentGrid.getGridLevelExpressionId(),
                            assignmentGrid.getAssignmentSetName(), i, parametersSet);
                }
                assignmentMatrixRowData.setRule(scriptRule);
                assignmentMatrixRowData.setExpression(scriptRule.getScriptCode());
                if(ruleExp.length()>0) {
                    assignmentMatrixRowData.setRuleExp(ruleExp.toString());
                }
                /*Compiling assignment action script and saving it only if it is not a Task Assignment Master nor Rule
                 Matrix Master*/
                if (!assignmentGrid.isTaskAssignment()
                        && (assignmentGrid.isRuleMatrix() == null || !assignmentGrid.isRuleMatrix())) {

                    compileAndSaveScript(assignmentMatrixRowData.getAssignmentMatrixAction(),
                            assignmentGrid.getAssignmentActionFieldMetaDataList(), assignmentGrid.getAggregateFunction());
                } else if (assignmentGrid.isRuleMatrix() != null && assignmentGrid.isRuleMatrix()) {
                    compileAndSaveScriptForRuleMatrix(assignmentMatrixRowData.getAssignmentMatrixAction(),
                            assignmentGrid.getAssignmentActionFieldMetaDataList());
                }
                assignmentMatrixRowData.setEditedOrNewFlag(false);
            }
            }
        }
    }

    /**
     *
     * Create script rules using expression formed above
     *          similar to rule level expression
     * @param conditionNameExpressionMap
     * @param gridExpression
     * @param name
     * @param rowCounter
     * @return
     */

    private ScriptRule createScriptRule(Map<String, String> conditionNameExpressionMap, String gridExpression, String name,
                                        int rowCounter, Set<Parameter> parametersSet) {
        ScriptRule scriptRule = new ScriptRule();

        String finalExpressionForRule = "";

        if (null != gridExpression) {

            String gridExpressionArray[] = gridExpression.split(" ");

            if (null != gridExpressionArray) {

                for (int i = 0 ; i < gridExpressionArray.length ; i++) {

                    // use constants list here for supported data types
                    List<String> listOfOperators = AssignmentConstants.ALL_SUPOORTED_OPERATORS;

                    if (!(listOfOperators.contains(gridExpressionArray[i]))) {
                        if (conditionNameExpressionMap.containsKey(gridExpressionArray[i])) {
                            gridExpressionArray[i] = conditionNameExpressionMap.get(gridExpressionArray[i]);
                        } else {
                            gridExpressionArray[i] = "true";
                        }
                    }
                }

                for (int i = 0 ; i < gridExpressionArray.length ; i++) {
                    finalExpressionForRule = finalExpressionForRule + gridExpressionArray[i] + " ";
                }
            }

        }

        scriptRule.setScriptCodeType(RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT);
        scriptRule.setScriptCode(finalExpressionForRule);

        BaseLoggers.flowLogger.debug("finalExpressionForRule :: " + finalExpressionForRule);

        scriptRule.setName(name + "_" + rowCounter);
        scriptRule.setCode(name + "_" + rowCounter);
        scriptRule.setDescription(name + "_" + rowCounter);
        scriptRule.setApprovalStatus(ApprovalStatus.APPROVED);
        scriptRule.getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.EMPTY_PARENT);

        compiledExpressionBuilder.compileMvelScriptRule(scriptRule, parametersSet);

        return scriptRule;

    }

    /**
     * For Assignment Master
     * Takes object map of key value of row data
     * and AssignmentFieldMetaData object
     *      similar to condition level expression
     *  use to create expression
     * @param fieldKeyValue
     * @param assignmentFieldMetaData
     * @return
     */

    private String getConditionExpression(Map<String, Object> fieldKeyValueMap,
                                          AssignmentFieldMetaData assignmentFieldMetaData, Set<Parameter> parametersSet) {

        String expression = "";

        String actualOgnl = "";

        // Create actual OGNL based on data type

        actualOgnl = createActualOgnlOnDataType(assignmentFieldMetaData);

        // form proper conditions

        if (AssignmentConstants.EQUALS.equals(assignmentFieldMetaData.getOperator())
                || AssignmentConstants.NOT_EQUALS.equals(assignmentFieldMetaData.getOperator())
                || AssignmentConstants.LESS_THEN.equals(assignmentFieldMetaData.getOperator())
                || AssignmentConstants.LESS_THEN_EQUALS.equals(assignmentFieldMetaData.getOperator())
                || AssignmentConstants.GREATER_THEN.equals(assignmentFieldMetaData.getOperator())
                || AssignmentConstants.GREATER_THEN_EQUALS.equals(assignmentFieldMetaData.getOperator())) {

            String exp = getParameterExpressionValue(parametersSet,
                    fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));

            if (StringUtils.isNotBlank(exp)) {
                expression = actualOgnl + " " + assignmentFieldMetaData.getOperator() + " " + exp;
            }

        } else if (AssignmentConstants.IN_OPERATOR.equals(assignmentFieldMetaData.getOperator())) {

            if (!JSONObject.NULL.equals(fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()))
                    && !fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()).toString().isEmpty()) {
                String multiValues[] = ((String) (fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId())))
                        .split(AssignmentConstants.MULTI_VALUE_SEPARATOR);
                if (null != multiValues && multiValues.length > 0) {

                    expression = expression + "( ";
                    for (int i = 0 ; i < multiValues.length ; i++) {
                        if (i < multiValues.length - 1) {
                            expression = expression + "( " + actualOgnl + " == "
                                    + getParameterExpressionValue(parametersSet, multiValues[i]) + " ) || ";
                        } else {
                            expression = expression + "( " + actualOgnl + " == "
                                    + getParameterExpressionValue(parametersSet, multiValues[i]) + " ) ";
                        }
                    }
                    expression = expression + " )";
                }
            }

        } else if (AssignmentConstants.NOT_IN_OPERATOR.equals(assignmentFieldMetaData.getOperator())) {

            if (!JSONObject.NULL.equals(fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()))
                    && !fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()).toString().isEmpty()) {
                String multiValues[] = ((String) (fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId())))
                        .split(AssignmentConstants.MULTI_VALUE_SEPARATOR);
                if (null != multiValues && multiValues.length > 0) {

                    expression = expression + "( ";
                    for (int i = 0 ; i < multiValues.length ; i++) {
                        if (i < multiValues.length - 1) {
                            expression = expression + "( " + actualOgnl + " != "
                                    + getParameterExpressionValue(parametersSet, multiValues[i]) + " ) || ";
                        } else {
                            expression = expression + "( " + actualOgnl + " != "
                                    + getParameterExpressionValue(parametersSet, multiValues[i]) + " ) ";
                        }
                    }
                    expression = expression + " )";
                }
            }

        } else if (AssignmentConstants.BETWEEN_OPERATOR.equals(assignmentFieldMetaData.getOperator())) {

            if (!JSONObject.NULL.equals(fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()))
                    && !fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()).toString().isEmpty()) {
                String multiValues[] = ((String) (fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId())))
                        .split(AssignmentConstants.MULTI_VALUE_SEPARATOR);

                if (null != multiValues && multiValues.length == 2) {
                    String firstValue = getParameterExpressionValue(parametersSet, multiValues[0]);
                    String secondValue = getParameterExpressionValue(parametersSet, multiValues[1]);

                    if (StringUtils.isNotBlank(secondValue)) {
                        /* if (Double.parseDouble(firstValue) > Double.parseDouble(secondValue)) {

                             expression = "( ( " + secondValue + " <= " + actualOgnl + " ) && ( " + actualOgnl + " <= "
                                     + firstValue + " ) ) ";
                         } else {*/
                        expression = "( ( " + firstValue + " <= " + actualOgnl + " ) && ( " + actualOgnl + " <= "
                                + secondValue + " ) ) ";
                        /* }*/

                    } else {
                        expression = "( ( " + actualOgnl + " >= " + firstValue + " ) ) ";

                    }

                } else if (null != multiValues && multiValues.length == 1) {
                    String firstValue = getParameterExpressionValue(parametersSet, multiValues[0]);
                    expression = "( " + actualOgnl + " >= " + firstValue + " )";
                }
            }

        }

        BaseLoggers.flowLogger.debug("getConditionExpression :: expression :: " + expression);
        return expression;
    }

    /**
     *
     * Method to return the actual value
     * @param dataType
     * @param value
     * @return
     */

    private String getParameterExpressionValue(Set<Parameter> parametersSet, Object value) {
        Parameter parameter = null;

        BaseLoggers.flowLogger.debug("Parameter id :: " + value);

        if (!JSONObject.NULL.equals(value) && !value.toString().isEmpty()) {
            parameter = entityDao.find(Parameter.class, Long.parseLong(value.toString()));
        }

        String expression = compiledExpressionBuilder.buildParameterExpressionToCompile(parameter, parametersSet, null,
                true, null, 0);
        BaseLoggers.flowLogger.debug("Expression formed is :: " + expression);
        return expression;

    }

    /**
     * For Task Assignment Master
     * Takes object map of key value of row data
     * and AssignmentFieldMetaData object
     *      similar to condition level expression
     *  use to create expression
     * @param fieldKeyValue
     * @param assignmentFieldMetaData
     * @return
     */

    private String getConditionExpression(Map<String, Object> fieldKeyValueMap,
                                          AssignmentFieldMetaData assignmentFieldMetaData) {

        String expression = "";

        String actualOgnl = "";
        int dataType = assignmentFieldMetaData.getDataType();

        // Create actual OGNL based on data type
        actualOgnl = createActualOgnlOnDataType(assignmentFieldMetaData);

        // form proper conditions

        if (AssignmentConstants.EQUALS.equals(assignmentFieldMetaData.getOperator())) {
            expression = actualOgnl + " " + assignmentFieldMetaData.getOperator() + " "
                    + getLiteralValue(dataType, fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));

        } else if (AssignmentConstants.NOT_EQUALS.equals(assignmentFieldMetaData.getOperator())) {
            expression = actualOgnl + " " + assignmentFieldMetaData.getOperator() + " "
                    + getLiteralValue(dataType, fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));

        } else if (AssignmentConstants.LESS_THEN.equals(assignmentFieldMetaData.getOperator())) {
            expression = actualOgnl + " " + assignmentFieldMetaData.getOperator() + " "
                    + getLiteralValue(dataType, fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));

        } else if (AssignmentConstants.LESS_THEN_EQUALS.equals(assignmentFieldMetaData.getOperator())) {

            expression = actualOgnl + " " + assignmentFieldMetaData.getOperator() + " "
                    + getLiteralValue(dataType, fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));

        } else if (AssignmentConstants.GREATER_THEN.equals(assignmentFieldMetaData.getOperator())) {

            expression = actualOgnl + " " + assignmentFieldMetaData.getOperator() + " "
                    + getLiteralValue(dataType, fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));

        } else if (AssignmentConstants.GREATER_THEN_EQUALS.equals(assignmentFieldMetaData.getOperator())) {

            expression = actualOgnl + " " + assignmentFieldMetaData.getOperator() + " "
                    + getLiteralValue(dataType, fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));

        } else if (AssignmentConstants.IN_OPERATOR.equals(assignmentFieldMetaData.getOperator())) {

            if (!JSONObject.NULL.equals(fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()))
                    && !fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()).toString().isEmpty()) {
                String multiValues[] = ((String) (fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId())))
                        .split(AssignmentConstants.MULTI_VALUE_SEPARATOR);

                if (null != multiValues && multiValues.length > 0) {

                    expression = expression + "( ";

                    for (int i = 0 ; i < multiValues.length ; i++) {
                        if (i < multiValues.length - 1) {
                            expression = expression + "( " + actualOgnl + " == " + getLiteralValue(dataType, multiValues[i])
                                    + " ) || ";
                        } else {
                            expression = expression + "( " + actualOgnl + " == " + getLiteralValue(dataType, multiValues[i])
                                    + " ) ";
                        }
                    }
                    expression = expression + " )";
                }
            }

        } else if (AssignmentConstants.NOT_IN_OPERATOR.equals(assignmentFieldMetaData.getOperator())) {

            if (!JSONObject.NULL.equals(fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()))
                    && !fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()).toString().isEmpty()) {
                String multiValues[] = ((String) (fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId())))
                        .split(AssignmentConstants.MULTI_VALUE_SEPARATOR);

                if (null != multiValues && multiValues.length > 0) {

                    expression = expression + "( ";

                    for (int i = 0 ; i < multiValues.length ; i++) {
                        if (i < multiValues.length - 1) {
                            expression = expression + "( " + actualOgnl + " != " + getLiteralValue(dataType, multiValues[i])
                                    + " ) || ";
                        } else {
                            expression = expression + "( " + actualOgnl + " != " + getLiteralValue(dataType, multiValues[i])
                                    + " ) ";
                        }
                    }
                    expression = expression + " )";
                }
            }

        } else if (AssignmentConstants.BETWEEN_OPERATOR.equals(assignmentFieldMetaData.getOperator())) {

            if (!JSONObject.NULL.equals(fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()))
                    && !fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()).toString().isEmpty()) {
                String multiValues[] = ((String) (fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId())))
                        .split(AssignmentConstants.MULTI_VALUE_SEPARATOR);

                if (StringUtils.isNotBlank(multiValues[1])) {

                    if (Double.parseDouble(multiValues[0]) > Double.parseDouble(multiValues[1])) {

                        expression = " ( ( " + multiValues[1] + " <= " + actualOgnl + " ) && ( " + actualOgnl + " <= "
                                + multiValues[0] + " ) ) ";
                    } else {
                        expression = " ( ( " + multiValues[0] + " <= " + actualOgnl + " ) && ( " + actualOgnl + " <= "
                                + multiValues[1] + " ) ) ";
                    }

                } else {
                    expression = "( ( " + actualOgnl + " >= " + multiValues[0] + " ) ) ";

                }

            }

        }

        return expression;
    }

    private String createActualOgnlOnDataType(AssignmentFieldMetaData assignmentFieldMetaData) {
        String actualOgnl;
        String ognl = "( ?" + assignmentFieldMetaData.getOgnl();
        if (assignmentFieldMetaData.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
            actualOgnl = RulesConverterUtility.getNullSafeObjectGraph(ognl) + RuleConstants.RULE_TIME_IN_MILLIS + " )";

        } else if (assignmentFieldMetaData.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
            actualOgnl = RulesConverterUtility.getNullSafeObjectGraph(ognl) + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE + " )";

        } else if (assignmentFieldMetaData.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
            actualOgnl = RulesConverterUtility.getNullSafeObjectGraph(ognl) + RuleConstants.RULE_IDS + " )";

        } else {
            actualOgnl = RulesConverterUtility.getNullSafeObjectGraph(ognl) + " )";
        }
        return actualOgnl;
    }

    /**
     *
     * Method to return the actual value
     * @param dataType
     * @param value
     * @return
     */

    private String getLiteralValue(int dataType, Object value) {
        String actualValue = "";

        if (dataType == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
            actualValue = value + "L";

        } else if (dataType == ParameterDataType.PARAMETER_DATA_TYPE_STRING) {
            actualValue = "'" + value + "'";

        } else {
            //Added for PDDEV-7174 to be removed with jettison-1.2.jar todo-upgrade
            try{
                actualValue = (String) value;
            }
            catch(ClassCastException e)
            {
                actualValue=value.toString();
            }
        }

        if (value instanceof java.lang.String) {

            if (value == null || ((String) value).equalsIgnoreCase("null")) {

                actualValue = null;

            }

        }

        return actualValue;

    }

    /**
     *
     * Method to form/compile the assignment expression
     * @param assignmentMatrixAction
     * @return
     */
    @Override
    public void compileAndSaveScript(AssignmentMatrixAction assignmentMatrixAction,
                                     List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList, String aggregateFunction) {

        String finalAssignmentExp = "";

        if (null != assignmentMatrixAction) {

            Set<Parameter> assignmentMatrixActionParameters = new HashSet<Parameter>();

            // converted json string to map for one row for assignment actions
            Map<String, Object> fieldKeyValueMap = AssignmentMatrixMasterUtility.convertJsonToMap(assignmentMatrixAction
                    .getAssignActionValues());

            if (null != assignmentActionFieldMetaDataList && null != fieldKeyValueMap
                    && assignmentActionFieldMetaDataList.size() > 0 && fieldKeyValueMap.size() > 0) {

                String assignmentExpression = null;

                for (AssignmentFieldMetaData assignmentFieldMetaData : assignmentActionFieldMetaDataList) {
                    if (fieldKeyValueMap.containsKey(assignmentFieldMetaData.getIndexId())) {
                        assignmentExpression = assignmentFieldMetaData.getOgnl();

                        if (ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE == assignmentFieldMetaData.getDataType()) {
                            assignmentExpression += ".id";
                        }
                        if (!JSONObject.NULL.equals(fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()))
                                && !fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()).toString().isEmpty() && assignmentFieldMetaData.getParameterBased() != null
                                && assignmentFieldMetaData.getParameterBased()) {
                            if(!fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()).toString().contains(AssignmentConstants.MULTI_VALUE_SEPARATOR))
                            {
                                assignmentExpression += " = "
                                        + getParameterExpressionValue(assignmentMatrixActionParameters,
                                        fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));

                                BaseLoggers.flowLogger.debug("assignmentExpression :: " + assignmentExpression);
                                finalAssignmentExp = finalAssignmentExp + assignmentExpression + ";";
                            }
                            else
                                finalAssignmentExp = createExpressionForAggregateFunctions(aggregateFunction,assignmentFieldMetaData.getOgnl());
                        }

                        else if (!JSONObject.NULL.equals(fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()))
                                && !fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()).toString().isEmpty())

                        {
                            assignmentExpression += " = "
                                    + getLiteralValue(assignmentFieldMetaData.getDataType(),
                                    fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));

                            BaseLoggers.flowLogger.debug("assignmentExpression :: " + assignmentExpression);
                            finalAssignmentExp = finalAssignmentExp + assignmentExpression + ";";
                        }

                        assignmentMatrixAction.setParameters(assignmentMatrixActionParameters);
                    }
                }
            }
            if (finalAssignmentExp != null && !finalAssignmentExp.isEmpty()) {
                assignmentMatrixAction.setCompiledExpression(compileExpression(finalAssignmentExp));
            }
        }

    }

    /**
     *
     * Method to form/compile the Rule Matrix action expression
     * @param assignmentMatrixAction
     * @return
     */
    @Override
    public void compileAndSaveScriptForRuleMatrix(AssignmentMatrixAction assignmentMatrixAction,
                                                  List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList) {

        String finalAssignmentExp = "";

        if (null != assignmentMatrixAction) {

            Set<Parameter> assignmentMatrixActionParameters = new HashSet<Parameter>();

            // converted json string to map for one row for assignment actions
            Map<String, Object> fieldKeyValueMap = AssignmentMatrixMasterUtility.convertJsonToMap(assignmentMatrixAction
                    .getAssignActionValues());

            if (null != assignmentActionFieldMetaDataList && null != fieldKeyValueMap
                    && assignmentActionFieldMetaDataList.size() > 0 && fieldKeyValueMap.size() > 0) {

                String assignmentExpression = null;

                for (AssignmentFieldMetaData assignmentFieldMetaData : assignmentActionFieldMetaDataList) {
                    if (fieldKeyValueMap.containsKey(assignmentFieldMetaData.getIndexId())) {
                        assignmentExpression = assignmentFieldMetaData.getOgnl();

                        if (ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE == assignmentFieldMetaData.getDataType()) {
                            assignmentExpression += ".id";
                        }
                        if (!JSONObject.NULL.equals(fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()))
                                && !fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()).toString().isEmpty()) {
                            assignmentExpression += " = "
                                    + getLiteralValue(assignmentFieldMetaData.getDataType(),
                                    fieldKeyValueMap.get(assignmentFieldMetaData.getIndexId()));

                            BaseLoggers.flowLogger.debug("assignmentExpression :: " + assignmentExpression);
                            finalAssignmentExp = finalAssignmentExp + assignmentExpression + ";";
                        }

                        assignmentMatrixAction.setParameters(assignmentMatrixActionParameters);
                    }
                }
            }
            if (finalAssignmentExp != null && !finalAssignmentExp.isEmpty()) {
                assignmentMatrixAction.setCompiledExpression(compileExpression(finalAssignmentExp));
            }
        }

    }

    @Override
    public FormConfigEntityDataVO getBinderNameForReferenceOgnl(String ognl) {

        Class clazz = null;
        String binderName = "";

        try {
            clazz = Class.forName(ContextObjectClass.getClassName(ognl.split("\\.")[0]));

        } catch (Exception e) {
            BaseLoggers.exceptionLogger.debug("Error" + e);
            throw new RuleException(e.getMessage());
        }
        String packageName = "";
        try {
            packageName = AssignmentMatrixMasterUtility.getPackageName(clazz, ognl);
        } catch (RuleException e) {
            return null;
        }
        FormConfigEntityDataVO formConfigEntityDataVO = new FormConfigEntityDataVO();
        if (null != packageName) {
            FormConfigEntityData configEntityData = formDefinitionService.getFormConfigDataByPackageName(packageName);
            if (configEntityData != null) {
                formConfigEntityDataVO.setItemLabel(configEntityData.getItemLabel());
                formConfigEntityDataVO.setItemValue(configEntityData.getItemValue());

                if (null != configEntityData) {
                    String tempBinderName = configEntityData.getWebDataBinderName();

                    if (null != tempBinderName) {
                        if (tempBinderName.indexOf(",") != -1) {
                            binderName = tempBinderName.split(",")[0];
                        } else {
                            binderName = tempBinderName;
                        }
                    }
                }
            }
            formConfigEntityDataVO.setWebDataBinderName(binderName);
            return formConfigEntityDataVO;
        } else {
            return null;
        }

    }

    @Override
    public AssignmentMaster getAssignmentMatrixByName(String name) {

        NamedQueryExecutor<AssignmentMaster> assignmentMasterExecutor = new NamedQueryExecutor<AssignmentMaster>(
                "AssignmentMaster.GetByName").addParameter("name", name);
        List<AssignmentMaster> assignmentMasterList = entityDao.executeQuery(assignmentMasterExecutor);
        if (assignmentMasterList.size() > 0)
            return assignmentMasterList.get(0);
        else
            return null;

    }



    @Override
    public List<Map<String, ?>> getAssignmentMatrixByPurpose(String[] searchColumnList, String value, int page) {

        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

        NamedQueryExecutor<Map<String, ?>> assignmentMasterExecutor = new NamedQueryExecutor<Map<String, ?>>("AssignmentMaster.filterByPurpose")
                .addParameter("purposeCode", PurposeType.LEASE)
                .addParameter("value","%"+value+"%").addParameter("statusList",statusList);
        System.out.println(PurposeType.LEASE);
        List<Map<String, ?>> assignmentMasterList = entityDao.executeQuery(assignmentMasterExecutor,page*3,3);
        long size=entityDao.executeTotalRowsQuery(assignmentMasterExecutor);

        if(CollectionUtils.isNotEmpty(assignmentMasterList)){
            Map<String, Long> sizeMap = new HashMap<>();
            sizeMap.put("size", Long.valueOf(size));

            assignmentMasterList.add(sizeMap);
        }
        return assignmentMasterList;

    }
    /**
     *
     * Method to create the mvel script rule for
     * Assignment Set of type Assignment Expression
     * @param expression
     * @return
     */

    private String getParameterExpressionWthId(String expression) {

        if (null != expression) {
            expression = " " + expression + " ";
            expression = expression.replaceAll("\\s+", " ");
            String[] tokens = expression.split(" ");

            Long paramId = null;

            for (String token : tokens) {
                if (!AssignmentConstants.assignmentExpressionOperators.contains(token)) {
                    paramId = ruleService.getParameterIdByName(token);
                    if (paramId != null) {
                        expression = expression.replace(" " + token + " ", " " + paramId.toString() + " ");
                    }

                }

            }
            expression = expression.trim();
        }

        return expression;
    }

    private String processExpToFormRuleExp(String expressionWithParameterId, Set<Parameter> parameters) {
        String script = "";

        if (null != expressionWithParameterId) {
            String[] tokens = expressionWithParameterId.split(" ");

            Parameter parameter = null;

            for (int i = 0 ; i < tokens.length ; i++) {

                if (!AssignmentConstants.assignmentExpressionOperators.contains(tokens[i])) {
                    parameter = ruleService.getParameter(Long.parseLong(tokens[i]));
                    tokens[i] = buildExpressionToCompile(parameter, parameters);
                }
            }

            for (String s : tokens) {
                script = script + s + " ";
            }

        }
        return script;
    }

    private String buildExpressionToCompile(Parameter parameter, Set<Parameter> parameters) {

        String expression = "";

        expression = compiledExpressionBuilder.buildParameterExpressionToCompile(parameter, parameters, null, true, null, 0);
        BaseLoggers.flowLogger.debug("Expression formed is :: " + expression);

        return expression;
    }

    @Override
    public Object[] convertInfixToRPN(String[] inputTokens) {
        return infixToRPN(inputTokens).toArray();
    }

    @Override
    public List<EntityType> getEntityTypesForTaskAssignment(List<String> displayEntityNameList) {
        NamedQueryExecutor<EntityType> entityTypeCriteria = new NamedQueryExecutor<EntityType>(
                "AssignmentMaster.GetEntityTypes").addParameter("displayEntityNameList", displayEntityNameList);

        List<EntityType> entityTypes = entityDao.executeQuery(entityTypeCriteria);

        if (entityTypes == null) {
            entityTypes = Collections.emptyList();
        }
        return entityTypes;
    }

    @Override
    public List<AssignmentMatrixAction> getAssignmentActionsToCompile() {
        NamedQueryExecutor<AssignmentMatrixAction> assignmentMatrixActionExecutor = new NamedQueryExecutor<AssignmentMatrixAction>(
                "AssignmentSet.getAllAssignmentMatrixActionToCompile");
        return entityDao.executeQuery(assignmentMatrixActionExecutor);

    }

    @Override
    public AssignmentSet getAssignmentSetByAction(Long id) {
        NamedQueryExecutor<AssignmentSet> assignmentSetExecutor = new NamedQueryExecutor<AssignmentSet>(
                "AssignmentSet.getByAssignmentMatrixAction").addParameter("id", id);
        return entityDao.executeQueryForSingleValue(assignmentSetExecutor);
    }

    @Override
    public AssignmentMaster getAssignmentMatrixById(Long id) {
        return entityDao.find(AssignmentMaster.class, id);
    }

    @Override
    public List<RuleMatrixMaster> getRateLimitMasters() {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

        RuleMatrixMasterType ruleMatrixMasterType = genericParameterService.findByCode(
                RuleMatrixMasterType.RULE_MATRIX_MASTER_TYPE_RATE_LIMIT, RuleMatrixMasterType.class);
        if (ruleMatrixMasterType != null) {
            NamedQueryExecutor<RuleMatrixMaster> executor = new NamedQueryExecutor<RuleMatrixMaster>(
                    "RuleMatrixMaster.getRateLimitMasters").addParameter("statusList", statusList).addParameter(
                    "ruleMatrixMasterTypeId", ruleMatrixMasterType.getId());
            return entityDao.executeQuery(executor);
        }

        return null;
    }


    @Override
    public List<RuleMatrixMaster> getChargeLimitMasters() {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

        RuleMatrixMasterType ruleMatrixMasterType = genericParameterService.findByCode(
                RuleMatrixMasterType.RULE_MATRIX_MASTER_TYPE_CHARGE_LIMIT, RuleMatrixMasterType.class);
        if (ruleMatrixMasterType != null) {
            NamedQueryExecutor<RuleMatrixMaster> executor = new NamedQueryExecutor<RuleMatrixMaster>(
                    "RuleMatrixMaster.getChargeLimitMasters").addParameter("statusList", statusList).addParameter(
                    "ruleMatrixMasterTypeId", ruleMatrixMasterType.getId());
            return entityDao.executeQuery(executor);
        }

        return null;
    }

    private String createExpressionForAggregateFunctions(String aggregateFunction,String ognl)

    {
        StringBuilder finalExpression = new StringBuilder();
        String currentLoopVariable = "loopVariable";
        finalExpression.append(RuleConstants.MVEL_RETURN_VARIABLE)
                .append(RuleConstants.MVEL_EQUAL)
                .append(RuleConstants.EXPRESSION_SCRIPT_NULL_VALUE)
                .append(RuleConstants.MVEL_SEMICOLON);
        finalExpression.append(RuleConstants.EXPRESSION_SCRIPT_IF_START)
                .append(CONTEXT_OBJECT_FOR_AGGREGATE_FUNCTION)
                .append(RuleConstants.EXPRESSION_SCRIPT_NOT_NULL_CHECK)
                .append(RuleConstants.AND_OPERATOR)
                .append(RuleConstants.NOT_OPERATOR_ENGLISH)
                .append(RuleConstants.LEFT_PAREN)
                .append(RuleConstants.LEFT_PAREN)
                .append(CONTEXT_OBJECT_FOR_AGGREGATE_FUNCTION)
                .append(RuleConstants.EXPRESSION_SCRIPT_SIZE_CHECK)
                .append(RuleConstants.RIGHT_PAREN)
                .append(RuleConstants.LEFT_CURLY_BRACES);
        finalExpression.append(RuleConstants.MVEL_FOREACH).append(
                RuleConstants.LEFT_PAREN);
        finalExpression.append(currentLoopVariable).append(
                RuleConstants.MVEL_COLON
                        + CONTEXT_OBJECT_FOR_AGGREGATE_FUNCTION);
        finalExpression.append(RuleConstants.RIGHT_PAREN);
        finalExpression.append(RuleConstants.LEFT_CURLY_BRACES);
        String relationalOperator = "";

        if (aggregateFunction
                .equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_MAX_CODE)) {
            relationalOperator = RuleConstants.MVEL_SMALLER_THAN;
        }

        else if (aggregateFunction
                .equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_MIN_CODE)) {
            relationalOperator = RuleConstants.MVEL_GREATER_THAN;
        }

        finalExpression.append(RuleConstants.EXPRESSION_SCRIPT_IF_START)
                .append(currentLoopVariable)
                .append(RuleConstants.EXPRESSION_SCRIPT_NOT_NULL_CHECK)
                .append(RuleConstants.RIGHT_PAREN)
                .append(RuleConstants.LEFT_CURLY_BRACES);

        finalExpression.append(RuleConstants.EXPRESSION_SCRIPT_IF_START)
                .append(RuleConstants.MVEL_RETURN_VARIABLE)
                .append(RuleConstants.EXPRESSION_SCRIPT_NULL_CHECK)
                .append(RuleConstants.RIGHT_PAREN)
                .append(RuleConstants.LEFT_CURLY_BRACES)
                .append(RuleConstants.MVEL_RETURN_VARIABLE)
                .append(RuleConstants.MVEL_EQUAL).append(currentLoopVariable)
                .append(RuleConstants.MVEL_SEMICOLON)
                .append(RuleConstants.RIGHT_CURLY_BRACES);

        finalExpression.append(RuleConstants.MVEL_ELSE_IF_OPEN)
                .append(RuleConstants.MVEL_RETURN_VARIABLE)
                .append(relationalOperator).append(currentLoopVariable)
                .append(RuleConstants.RIGHT_PAREN)
                .append(RuleConstants.LEFT_CURLY_BRACES)
                .append(RuleConstants.MVEL_RETURN_VARIABLE)
                .append(RuleConstants.MVEL_EQUAL).append(currentLoopVariable)
                .append(RuleConstants.MVEL_SEMICOLON)
                .append(RuleConstants.RIGHT_CURLY_BRACES)
                .append(RuleConstants.RIGHT_CURLY_BRACES)
                .append(RuleConstants.RIGHT_CURLY_BRACES)
                .append(ognl).append(RuleConstants.MVEL_EQUAL)
                .append(RuleConstants.MVEL_RETURN_VARIABLE)
                .append(RuleConstants.RIGHT_CURLY_BRACES);

        return finalExpression.toString();
    }

    @Override
    public List<Map<String, ?>> searchAutoCompleteValues(String className, String itemVal, String[] searchColumnList,
                                                         String value, Boolean loadApprovedEntityFlag, String itemsList,
                                                         Boolean strictSearchOnitemsList, int page, String whereCondition,
                                                         Map<String, Object>paramMap) {
        NeutrinoValidator.notNull(className, "Class name cannot be null");
        NeutrinoValidator.notNull(searchColumnList, "Columns List cannot be null");
        NeutrinoValidator.notNull(itemVal, "Item value cannot be null");
        Class entityClass = null;
        int counter = 0;
        long totalRecords = 0;
        try {
            entityClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            BaseLoggers.flowLogger.error(e.toString());
        }
        List<Map<String, ?>> finalResult = new ArrayList<>();
        List<Long> itemsId = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean isFirstClause = true;
        MapQueryExecutor executor = new MapQueryExecutor(entityClass).addQueryColumns(searchColumnList).addQueryColumns(itemVal);
        if (BaseMasterEntity.class.isAssignableFrom(entityClass) && loadApprovedEntityFlag) {
            executor.addAndClause("masterLifeCycleData.approvalStatus IN (:approvalStatus)");
            executor.addAndClause("entityLifeCycleData.persistenceStatus !="+ PersistenceStatus.EMPTY_PARENT);
            executor.addBoundParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        }

        StringBuilder whereClause = new StringBuilder();
        if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
            whereClause
                    .append("(entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false) and activeFlag = true ");
        } else {
            whereClause
                    .append("(entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false) ");
        }
        whereClause.append(whereCondition);
        for(Map.Entry<String, Object> entry : paramMap.entrySet()){
            executor.addBoundParameter(entry.getKey(), entry.getValue());
        }
        if (itemsList != null) {
            itemsId = getItemListIds(itemsList);

        }
        if (itemsId != null) {
            if (!itemsId.isEmpty()) {
                whereClause.append(" and id IN (:itemsIds)");
                executor.addBoundParameter("itemsIds", itemsId);
            } else if (strictSearchOnitemsList) {
                /* In case strict search on listOfItems is enabled and list of items is empty, return empty list. */
                return new ArrayList<>();
            }
        }

        executor.addAndClause(whereClause.toString());

        for (String search_col : searchColumnList) {
            if (isFirstClause) {
                sb.append("(lower(" + search_col + ") like " );
                isFirstClause = false;
            } else {
                sb.append(" or " + "lower(" + search_col + ") like " );
            }
            sb.append( "lower(:value) ");
        }
        executor.addBoundParameter("value", "%"+value+"%");
        sb.append(")");
        executor = executor.addAndClause(sb.toString());
        String orderByClause=prepareOrderByClause(searchColumnList,entityClass);
        executor.addOrderByClause(orderByClause);
        List<Map<String, ?>> result = entityDao.executeQuery(executor,page * 3, 3);
        for (Map<String, ?> temp : result) {
            finalResult.add(counter, temp);
            counter++;
        }
        totalRecords = totalRecords + entityDao.executeTotalRowsQuery(executor);
        Map<String, Long> sizeMap = new HashMap<>();
        sizeMap.put("size", totalRecords);
        finalResult.add(counter, sizeMap);
        if (finalResult != null) {
            BaseLoggers.flowLogger.debug("size of finalResult :{}", finalResult.size());
        }
        return finalResult;
    }



    @Override
    public String prepareAutoComplete(ModelMap map, String i_label, String idCurr,
                                      String content_id, int page, List<Map<String, ?>> list) {
        if(CollectionUtils.isNotEmpty(list)) {
            Map listMap = list.get(list.size() - 1);
            int sizeList1 = Integer.parseInt(listMap.get("size").toString());
            list.remove(list.size() - 1);
            map.put("size", sizeList1);
            map.put("page", page);
        }
        if(i_label != null && i_label.contains(".")) {
            i_label = i_label.replace(".", "");
        }
        map.put("data", list);
        if(idCurr != null && idCurr.trim().length() > 0) {
            idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
        }
        map.put("idCurr", idCurr);
        map.put("i_label", i_label);
        map.put("content_id", content_id);
        return "autocomplete";
    }

    private List<Long> getItemListIds(String listItems) {
        listItems = listItems.substring(1, listItems.length() - 1);
        List<Long> listOfIds = new ArrayList<>();
        if (org.apache.commons.lang3.StringUtils.isNoneEmpty(listItems)) {
            String[] list = listItems.split(",");
            for (int i = 0 ; i < list.length ; i++) {
                String[] subList = list[i].split(":");
                listOfIds.add(Long.parseLong(subList[1]));
            }
        }
        return listOfIds;
    }

    private String prepareOrderByClause(String[] searchColumnList,
                                        Class entityClass) {
        StringBuilder orderByClause = new StringBuilder();
        orderByClause.append("order by ");
        for (int i = 0; i < searchColumnList.length; i++) {
            Class endColumnType = entityClass;
            String[] sortColumns = searchColumnList[i].split("\\.");
            for (String sortColumn : sortColumns) {
                Field sortableField = ReflectionUtils.findField(endColumnType,
                        sortColumn);
                if (ValidatorUtils.isNull(sortableField)) {
                    break;
                } else {
                    endColumnType = sortableField.getType();
                }

            }

            if (checkIfFieldIsStringOrCharType(endColumnType)) {
                prepareOrderByClauseIfFieldIsStringOrCharType(orderByClause,
                        searchColumnList[i], i, searchColumnList.length);
            } else {
                if (searchColumnList.length > 1
                        && i != searchColumnList.length - 1) {
                    orderByClause.append(searchColumnList[i] + " , ");
                } else {
                    orderByClause.append(searchColumnList[i]);
                }
            }
        }
        return orderByClause.toString();
    }

    private void prepareOrderByClauseIfFieldIsStringOrCharType(
            StringBuilder orderByClause, String searchColumn, int arrayIndex, int searchColArrayLength) {
        if(searchColArrayLength>1 && arrayIndex!=searchColArrayLength-1){
            orderByClause.append("lower(" +searchColumn+") , ");
        }else{
            orderByClause.append("lower(" +searchColumn+")");
        }

    }

    private Boolean checkIfFieldIsStringOrCharType(Class type) {
        if(ValidatorUtils.notNull(type) && (type.equals(String.class) || type.equals(Character.class))){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public String getDataTypeForObjectGraphType(Long id) {
        NamedQueryExecutor<String> queryExecutor = new NamedQueryExecutor<String>("RuleMatrix.getDataTypeForObjectGraphType").addParameter("id", id);
        return entityDao.executeQueryForSingleValue(queryExecutor);
    }

    @Override
    public String loadAssignmentGridHeaderJson(AssignmentGrid assignmentGrid) {
        List<List<Object>> headData = new ArrayList<>();
        DataTableJsonHelper jsonHelper = new DataTableJsonHelper();
        List<Object> rowData = new ArrayList<>();
        prepareHeadJson(rowData, assignmentGrid);
        int rowIndex = 0 ;
        if(Objects.nonNull(assignmentGrid) &&CollectionUtils.isNotEmpty(assignmentGrid.getAssignmentMatrixRowData())){

            rowIndex = assignmentGrid.getAssignmentMatrixRowData().size();

        }
        headData.add(rowData);
        jsonHelper.setAaData(headData);
        jsonHelper.setiTotalRecords(rowIndex);
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.exclude("*.class").deepSerialize(jsonHelper);
        BaseLoggers.flowLogger.info("String is"+jsonString);
        return jsonString;
    }

    private void prepareHeadJson(List<Object> rowData, AssignmentGrid assignmentGrid) {
        rowData.add("Priority");

        List<AssignmentFieldMetaData> assignmentFieldMetaDataList = assignmentGrid.getAssignmentFieldMetaDataList();
        if(CollectionUtils.isEmpty(assignmentFieldMetaDataList)){
            assignmentFieldMetaDataList = new ArrayList<>();
        }
        List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList = assignmentGrid.getAssignmentActionFieldMetaDataList();
        if(CollectionUtils.isEmpty(assignmentActionFieldMetaDataList)){
            assignmentActionFieldMetaDataList = new ArrayList<>();
        }
        for (AssignmentFieldMetaData ifGridItem : assignmentFieldMetaDataList) {
            if(Objects.isNull(ifGridItem.getFieldName())){
                rowData.add("");
            } else {
                if(null != ifGridItem.getRuleBased()&& ifGridItem.getRuleBased()){
                    rowData.add(ifGridItem.getFieldName());
                }else{
                    rowData.add(ifGridItem.getFieldName() + "  " + ifGridItem.getOperator());
                }


            }
        }
        for (AssignmentFieldMetaData thenGridItem : assignmentActionFieldMetaDataList) {
            if(Objects.isNull(thenGridItem.getFieldName())){
                rowData.add("");
            } else {
                rowData.add(thenGridItem.getFieldName()+"####");
            }
        }
        rowData.add("Actions");
    }

    @Override
    public String populateRowData(String mode, Integer index, ModelMap map,AssignmentSet assignmentSetVO,String sourceProduct,Long moduleID) {
        LinkedHashMap<Object,Object> hashMapTemp = new LinkedHashMap<>();
        if(mode.equalsIgnoreCase("edit")){
            for(AssignmentFieldMetaData assignmentFieldMetaData:((AssignmentGrid )assignmentSetVO).getAssignmentFieldMetaDataList()){
                if(Objects.nonNull(assignmentFieldMetaData.getRuleBased())&& assignmentFieldMetaData.getRuleBased()){
                    //do nothing
                }else
                if((Objects.nonNull(assignmentFieldMetaData.getParameterBased())&& assignmentFieldMetaData.getParameterBased())){
                    if(assignmentFieldMetaData.getOperator().equalsIgnoreCase(AssignmentConstants.BETWEEN_OPERATOR)){
                        List<String> paramList = new ArrayList<>();
                        String[] arrayParam = ((String)assignmentSetVO.getAssignmentMatrixRowData().get(index).getLinkedMap().get(assignmentFieldMetaData.getIndexId())).split(",");
                        for(String elementId : arrayParam){
                            paramList.add(elementId);
                        }
                        hashMapTemp.put(assignmentFieldMetaData.getIndexId(), paramList);

                    }
                }
                else{
                    if(assignmentFieldMetaData.getOperator().equalsIgnoreCase(AssignmentConstants.BETWEEN_OPERATOR)){
                        List<String> paramList = new ArrayList<>();
                        String[] arrayParam = ((String)assignmentSetVO.getAssignmentMatrixRowData().get(index).getLinkedMap().get(assignmentFieldMetaData.getIndexId())).split(",");
                        for(String elementId : arrayParam){
                            paramList.add(elementId);
                        }
                        hashMapTemp.put(assignmentFieldMetaData.getIndexId(), paramList);

                    }
                }
            }
            if(Objects.nonNull(assignmentSetVO.getAssignmentMatrixRowData().get(index).getId())){
                map.put("tempEditRowCount",index);
            }
        }
        else{
            List<AssignmentMatrixRowData> rowDataList = assignmentSetVO.getAssignmentMatrixRowData();
            if(CollectionUtils.isNotEmpty(rowDataList)){
                index = rowDataList.size();
            }
        }
        if(Objects.nonNull(assignmentSetVO.getAggregateFunction()) && StringUtils.isNotEmpty(assignmentSetVO.getAggregateFunction())){
            map.put("minMax",true);
        }
        else{
            map.put("minMax",false);
        }
        map.put("paramList",hashMapTemp);
        map.put("moduleId", moduleID);
        map.put("sourceProduct", sourceProduct);
        map.put("assignmentSetVO",assignmentSetVO);
        map.put("thenView",true);
        map.put("index",index);
        List<String> ruleResultList = new ArrayList<>();
        ruleResultList.add("P");
        ruleResultList.add("F");
        ruleResultList.add("*");
        map.put("ruleResultList",ruleResultList);
        return "gridRowDataAssignmentMatrix";
    }

    @Override
    public String loadAssignmentGridBodyJson(AssignmentGrid assignmentGrid) {
        List<List<Object>> bodyData = new ArrayList<>();
        DataTableJsonHelper jsonHelper = new DataTableJsonHelper();
        int rowIndex = 0 ;
        if(Objects.nonNull(assignmentGrid) &&CollectionUtils.isNotEmpty(assignmentGrid.getAssignmentMatrixRowData())){
            for(int i = 0 ;i<assignmentGrid.getAssignmentMatrixRowData().size();i++){
                List<Object> rowData = new ArrayList<>();
                prepareRowJson(rowData, assignmentGrid,i);
                bodyData.add(rowData);
            }
            rowIndex = assignmentGrid.getAssignmentMatrixRowData().size();

        }

        jsonHelper.setAaData(bodyData);
        jsonHelper.setiTotalRecords(rowIndex);
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.exclude("*.class").deepSerialize(jsonHelper);
        BaseLoggers.flowLogger.info("String is"+jsonString);
        return jsonString;
    }

    private void prepareRowJson(List<Object> rowData, AssignmentGrid assignmentGrid,int rowIndex) {
        List<Object> priorityList = new ArrayList<>();
        List<Object> actionsList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(assignmentGrid.getAssignmentMatrixRowData())){
            AssignmentMatrixRowData assignmentMatrixRowData= assignmentGrid.getAssignmentMatrixRowData().get(rowIndex);
            if(Objects.nonNull(assignmentMatrixRowData) && Objects.nonNull(assignmentMatrixRowData.getAssignmentMatrixAction())){
                priorityList.add(assignmentMatrixRowData.getPriority());
                priorityList.add(rowIndex);
                rowData.add(priorityList);
                for(AssignmentFieldMetaData ifColumnData : assignmentGrid.getAssignmentFieldMetaDataList()){
                    if(ifColumnData != null && Objects.nonNull(assignmentMatrixRowData.getLinkedMap())) {
                        if(Objects.nonNull(ifColumnData.getParameterBased()) && ifColumnData.getParameterBased()){
                            if(Objects.nonNull(assignmentMatrixRowData.getLinkedMap().get(ifColumnData.getIndexId()))){
                            if((ifColumnData.getOperator().equalsIgnoreCase(AssignmentConstants.IN_OPERATOR)
                                    || ifColumnData.getOperator().equalsIgnoreCase(AssignmentConstants.NOT_IN_OPERATOR)
                                    || ifColumnData.getOperator().equalsIgnoreCase(AssignmentConstants.BETWEEN_OPERATOR))){
                                String []parameterIds = null;
                                String displayName="";
                                if(!((String)(assignmentGrid.getAssignmentMatrixRowData().get(rowIndex).getLinkedMap().get(ifColumnData.getIndexId()))).equalsIgnoreCase("")){
                                    parameterIds =((String)(assignmentGrid.getAssignmentMatrixRowData().get(rowIndex).getLinkedMap().get(ifColumnData.getIndexId()))).split(",");
                                    if(parameterIds!=null){
                                        for(String parameterId : parameterIds){
                                            Parameter obj = entityDao.find(Parameter.class,Long.parseLong(parameterId));
                                            if(Objects.nonNull(obj)){
                                                displayName = displayName + obj.getDisplayName()+",";
                                            }
                                            else{
                                                displayName = displayName + ",";
                                            }
                                        }
                                        displayName = displayName.substring(0,(displayName.length()-1));
                                    }
                                }
                                rowData.add(displayName);
                            }
                            else{
                                String parameterId = (String) assignmentMatrixRowData.getLinkedMap().get(ifColumnData.getIndexId());
                                if((parameterId!=null) && (!parameterId.isEmpty())){
                                    Parameter obj = entityDao.find(Parameter.class,Long.parseLong(parameterId));
                                    if(Objects.nonNull(obj)){
                                        rowData.add(obj.getDisplayName());
                                    }
                                    else{
                                        rowData.add("");
                                    }
                                }
                                else{
                                    rowData.add("");
                                }
                            }
                         }
                         else{
                                rowData.add("");
                            }
                        }
                        else{
                            rowData.add(assignmentMatrixRowData.getLinkedMap().get(ifColumnData.getIndexId()));
                        }

                    } else{
                        rowData.add("");
                    }
                }
                for(AssignmentFieldMetaData thenColumnData : assignmentGrid.getAssignmentActionFieldMetaDataList()){
                    if(thenColumnData != null && Objects.nonNull(assignmentMatrixRowData.getAssignmentMatrixAction().getJsonAssignActionMap())) {
                        if(Objects.nonNull(thenColumnData.getParameterBased()) && thenColumnData.getParameterBased() ){
                            String []parameterIds = null;
                            String displayName="";
                            parameterIds =((String)(assignmentMatrixRowData.getAssignmentMatrixAction().getJsonAssignActionMap().get(thenColumnData.getIndexId()))).split(",");
                            if(parameterIds!=null){
                                for(String parameterId : parameterIds){
                                    Parameter obj = entityDao.find(Parameter.class,Long.parseLong(parameterId));
                                    if(Objects.nonNull(obj)){
                                        displayName = displayName + obj.getDisplayName()+",";
                                    }
                                    else{
                                        displayName = displayName + ",";
                                    }
                                }
                                displayName = displayName.substring(0,(displayName.length()-1));
                            }
                            rowData.add(displayName);
                        }
                        else{
                            rowData.add(assignmentMatrixRowData.getAssignmentMatrixAction().getJsonAssignActionMap().get(thenColumnData.getIndexId()));
                        }
                    }
                    else{
                        rowData.add("");
                    }
                }
            }
        }
        actionsList.add("edit");
        actionsList.add("delete");
        rowData.add(actionsList);
    }


    @Override
    public String loadAssignmentGridRowJson(AssignmentGrid assignmentGrid,int rowIndex) {
        List<List<Object>> bodyData = new ArrayList<>();
        List<Object> rowData = new ArrayList<>();
        DataTableJsonHelper jsonHelper = new DataTableJsonHelper();
        prepareRowJson(rowData, assignmentGrid,rowIndex);
        bodyData.add(rowData);
        jsonHelper.setAaData(bodyData);
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.exclude("*.class").deepSerialize(jsonHelper);
        BaseLoggers.flowLogger.info("String is"+jsonString);
        return jsonString;
    }

    @Override
    public void setAssignmentSetPropertiesForGrid(ModelMap map,int assignmentGridIndex,
                                                   List<ObjectGraphTypes> objectGraphTypesActionFieldList,List<ObjectGraphTypes> objectGraphTypesParameterActionFieldList,
                                                   AssignmentSet assignmentSet ){
        List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList = new ArrayList<AssignmentFieldMetaData>();
        if(!objectGraphTypesActionFieldList.isEmpty())
        {
            for (ObjectGraphTypes objectGraphType : objectGraphTypesActionFieldList) {
                AssignmentFieldMetaData assignmentFieldMetaData = new AssignmentFieldMetaData();
                assignmentFieldMetaData.setOgnl(objectGraphType.getObjectGraph());
                assignmentFieldMetaData.setDataType(Integer.parseInt(objectGraphType.getDataType().getCode()));
                assignmentFieldMetaData.setFieldName(objectGraphType.getDisplayName());
                assignmentFieldMetaData.setIndexId(objectGraphType.getId() + "");
                assignmentFieldMetaData.setParameterBased(false);
                assignmentActionFieldMetaDataList.add(assignmentFieldMetaData);

            }
        }

        for (ObjectGraphTypes objectGraphType : objectGraphTypesParameterActionFieldList) {

            AssignmentFieldMetaData assignmentFieldMetaData = new AssignmentFieldMetaData();
            assignmentFieldMetaData.setParameterBased(true);
            assignmentFieldMetaData.setOgnl(objectGraphType.getObjectGraph());
            assignmentFieldMetaData.setDataType(Integer.parseInt(objectGraphType.getDataType().getCode()));
            assignmentFieldMetaData.setFieldName(objectGraphType.getDisplayName());
            assignmentFieldMetaData.setIndexId(objectGraphType.getId() + "");
            assignmentActionFieldMetaDataList.add(assignmentFieldMetaData);

        }

        assignmentSet.setAssignmentActionFieldMetaDataList(assignmentActionFieldMetaDataList);

        //map.put(FormTag.MODEL_ATTRIBUTE_VARIABLE_NAME, assignmentMaster);
        //map.put(NestedPathTag.NESTED_PATH_VARIABLE_NAME, "assignmentMaster" + PropertyAccessor.NESTED_PROPERTY_SEPARATOR);
        map.put("currentSetStatus", assignmentGridIndex);
        map.put("operatorsSupportedByDataTypeMap_ASSIGN", AssignmentConstants.operatorsSupportedByDataTypeMap_ASSIGN);


    }

    @Override
    public void deleteRowDataFromSet(AssignmentSet assignmentSetVO,long[] deleteArrayList){
        if(Objects.nonNull(deleteArrayList) && deleteArrayList.length!=0){
            List<AssignmentMatrixRowData> assignmentMatrixRowDataList = assignmentSetVO.getAssignmentMatrixRowData();
            for(long index : deleteArrayList){
                assignmentMatrixRowDataList.remove(Integer.parseInt(String.valueOf(index)));
            }
        }

    }

    @Override
    public void generateOperatorsForOgnlMetaField(AssignmentSet assignmentSet){
        if(assignmentSet instanceof AssignmentGrid){
            for(AssignmentFieldMetaData assignmentFieldMetaData : ((AssignmentGrid) assignmentSet).getAssignmentFieldMetaDataList()){
                String[] opts = operatorsSupportedByDataTypeMap_ASSIGN.get(assignmentFieldMetaData.getDataType());
                if(opts != null){
                    assignmentFieldMetaData.setOperators(Arrays.asList(opts));
                }
            }
        }
    }

    @Override
    public void prepareParametersForMultiSelect(AssignmentGrid assignmentGrid,Long moduleId, String sourceProduct){

        List<AssignmentFieldMetaData> assignmentFieldMetaDataList = assignmentGrid.getAssignmentFieldMetaDataList();
        for(AssignmentFieldMetaData assignmentFieldMetaData : assignmentFieldMetaDataList){
                if((null != assignmentFieldMetaData.getParameterBased() && assignmentFieldMetaData.getParameterBased()) && ("IN".equalsIgnoreCase(assignmentFieldMetaData.getOperator()) ||
                        "NOT_IN".equalsIgnoreCase(assignmentFieldMetaData.getOperator())) ){
                assignmentFieldMetaData.setParameterList(ruleMatrixMasterService.getParametersBasedOnDataTypeModule(moduleId,sourceProduct,assignmentFieldMetaData.getDataType()));
            }
        }
        if(Objects.nonNull(assignmentGrid.getAggregateFunction()) && StringUtils.isNotEmpty(assignmentGrid.getAggregateFunction())){
            for(AssignmentFieldMetaData assignmentFieldMetaData:assignmentGrid.getAssignmentActionFieldMetaDataList()){
                assignmentFieldMetaData.setParameterList(ruleMatrixMasterService.getParametersBasedOnDataTypeModule(moduleId,sourceProduct,assignmentFieldMetaData.getDataType()));
            }

        }


    }

    @Override
    public AssignmentSet CopyAssignmentSetValues(AssignmentSet assignmentSetVO ){
        AssignmentSet assignmentSet1 = null;
        if(assignmentSetVO instanceof AssignmentGrid){
            assignmentSet1 = new AssignmentGrid();
            List<AssignmentMatrixRowData> assignmentMatrixRowDataList1 = new ArrayList<>();
            //Deep Copy of AssignmentMatrixRowData
            for(AssignmentMatrixRowData assignmentMatrixRowData : assignmentSetVO.getAssignmentMatrixRowData()){
                AssignmentMatrixRowData assignmentMatrixRowData1 = new AssignmentMatrixRowData();
                LinkedHashMap<Object,Object> linkedMap1 = new LinkedHashMap<>();
                if(Objects.nonNull(assignmentMatrixRowData.getLinkedMap())){
                    linkedMap1.putAll(assignmentMatrixRowData.getLinkedMap());
                }
                assignmentMatrixRowData1.setLinkedMap(linkedMap1);
                AssignmentMatrixAction assignmentMatrixAction1 = new AssignmentMatrixAction();
                LinkedHashMap<Object,Object> actionLinkedMap1 = new LinkedHashMap<>();
                actionLinkedMap1.putAll(assignmentMatrixRowData.getAssignmentMatrixAction().getJsonAssignActionMap());
                assignmentMatrixAction1.setJsonAssignActionMap(actionLinkedMap1);
                assignmentMatrixRowData1.setAssignmentMatrixAction(assignmentMatrixAction1);
                assignmentMatrixRowData1.setPriority(assignmentMatrixRowData.getPriority());
                assignmentMatrixRowData1.setRuleExpression(assignmentMatrixRowData.getRuleExpression());
                assignmentMatrixRowData1.setSourceProduct(assignmentMatrixRowData.getSourceProduct());
                assignmentMatrixRowDataList1.add(assignmentMatrixRowData1);
                assignmentMatrixRowData1.setId(assignmentMatrixRowData.getId());
                assignmentMatrixRowData1.setEditedOrNewFlag(assignmentMatrixRowData.getEditedOrNewFlag());
                assignmentMatrixRowData1.setRuleExp(assignmentMatrixRowData.getRuleExp());
            }
            assignmentSet1.setAssignmentMatrixRowData(assignmentMatrixRowDataList1);
            ((AssignmentGrid) assignmentSet1).setGridLevelExpressionId(((AssignmentGrid) assignmentSetVO).getGridLevelExpressionId());
            ((AssignmentGrid) assignmentSet1).setGridLevelExpression(((AssignmentGrid) assignmentSetVO).getGridLevelExpression());
            ((AssignmentGrid) assignmentSet1).setAssignmentFieldMetaDataList(((AssignmentGrid) assignmentSetVO).getAssignmentFieldMetaDataList());

        }
        assignmentSet1.setAssignmentSetName(assignmentSetVO.getAssignmentSetName());
        assignmentSet1.setAssignmentSetRule(assignmentSetVO.getAssignmentSetRule());
        assignmentSet1.setAssignmentSetType(assignmentSetVO.getAssignmentSetType());
        assignmentSet1.setDefaultSet(assignmentSetVO.getDefaultSet());
        assignmentSet1.setExecuteAll(assignmentSetVO.getExecuteAll());
        assignmentSet1.setPriority(assignmentSetVO.getPriority());
        assignmentSet1.setAggregateFunction(assignmentSetVO.getAggregateFunction());
        assignmentSet1.setAssignmentActionFieldMetaDataList(assignmentSetVO.getAssignmentActionFieldMetaDataList());
        assignmentSet1.setEffectiveFrom(assignmentSetVO.getEffectiveFrom());
        assignmentSet1.setEffectiveTill(assignmentSetVO.getEffectiveTill());
        assignmentSet1.setBufferDays(assignmentSetVO.getBufferDays());

        return assignmentSet1;
    }

    @Override
    public void populateAssignmentSetValues(AssignmentSet assignmentSet1,AssignmentSet assignmentSetVO){
        if(assignmentSet1 instanceof AssignmentGrid){
            for(int i= 0 ;i<assignmentSet1.getAssignmentMatrixRowData().size();i++){
                if(assignmentSetVO.getAssignmentMatrixRowData().size() > i ){
                    assignmentSetVO.getAssignmentMatrixRowData().get(i).setLinkedMap(assignmentSet1.getAssignmentMatrixRowData().get(i).getLinkedMap());
                    assignmentSetVO.getAssignmentMatrixRowData().get(i).getAssignmentMatrixAction().setJsonAssignActionMap(
                            assignmentSet1.getAssignmentMatrixRowData().get(i).getAssignmentMatrixAction().getJsonAssignActionMap());
                    assignmentSetVO.getAssignmentMatrixRowData().get(i).setPriority(assignmentSet1.getAssignmentMatrixRowData().get(i).getPriority());
                    assignmentSetVO.getAssignmentMatrixRowData().get(i).setRuleExpression(assignmentSet1.getAssignmentMatrixRowData().get(i).getRuleExpression());
                    assignmentSetVO.getAssignmentMatrixRowData().get(i).setSourceProduct(assignmentSet1.getAssignmentMatrixRowData().get(i).getSourceProduct());
                    assignmentSetVO.getAssignmentMatrixRowData().get(i).setEditedOrNewFlag(assignmentSet1.getAssignmentMatrixRowData().get(i).getEditedOrNewFlag());
                    assignmentSetVO.getAssignmentMatrixRowData().get(i).setRuleExp(assignmentSet1.getAssignmentMatrixRowData().get(i).getRuleExp());
                }
                else{
                    AssignmentMatrixRowData assignmentMatrixRowData = assignmentSet1.getAssignmentMatrixRowData().get(i);
                    assignmentSetVO.getAssignmentMatrixRowData().add(assignmentMatrixRowData);
                }
            }
            ((AssignmentGrid) assignmentSetVO).setGridLevelExpressionId(((AssignmentGrid) assignmentSet1).getGridLevelExpressionId());
            ((AssignmentGrid) assignmentSetVO).setGridLevelExpression(((AssignmentGrid) assignmentSet1).getGridLevelExpression());
        }
        assignmentSetVO.setAssignmentSetName(assignmentSet1.getAssignmentSetName());
        assignmentSetVO.setAssignmentSetRule(assignmentSet1.getAssignmentSetRule());
        assignmentSetVO.setAssignmentSetType(assignmentSet1.getAssignmentSetType());
        assignmentSetVO.setDefaultSet(assignmentSet1.getDefaultSet());
        assignmentSetVO.setExecuteAll(assignmentSet1.getExecuteAll());
        assignmentSetVO.setPriority(assignmentSet1.getPriority());
        assignmentSetVO.setAggregateFunction(assignmentSet1.getAggregateFunction());
        assignmentSetVO.setEffectiveFrom(assignmentSet1.getEffectiveFrom());
        assignmentSetVO.setEffectiveTill(assignmentSet1.getEffectiveTill());
        assignmentSetVO.setBufferDays(assignmentSet1.getBufferDays());
    }

    @Override
    public String loadAssignmentGridHeaderJsonTask(AssignmentGrid assignmentGrid) {
        List<List<Object>> headData = new ArrayList<>();
        DataTableJsonHelper jsonHelper = new DataTableJsonHelper();
        List<Object> rowData = new ArrayList<>();
        prepareHeadJsonTask(rowData, assignmentGrid);
        int rowIndex = 0 ;
        if(Objects.nonNull(assignmentGrid) &&CollectionUtils.isNotEmpty(assignmentGrid.getAssignmentMatrixRowData())){

            rowIndex = assignmentGrid.getAssignmentMatrixRowData().size();

        }
        headData.add(rowData);
        jsonHelper.setAaData(headData);
        jsonHelper.setiTotalRecords(rowIndex);
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.exclude("*.class").deepSerialize(jsonHelper);
        BaseLoggers.flowLogger.info("String is"+jsonString);
        return jsonString;
    }

    private void prepareHeadJsonTask(List<Object> rowData, AssignmentGrid assignmentGrid) {
        rowData.add("Priority");

        List<AssignmentFieldMetaData> assignmentFieldMetaDataList = assignmentGrid.getAssignmentFieldMetaDataList();
        if(CollectionUtils.isEmpty(assignmentFieldMetaDataList)){
            assignmentFieldMetaDataList = new ArrayList<>();
        }
        List<EntityTypeMetaData> entityTypeMetaDataList = assignmentGrid.getEntityTypeMetaDataList();
        if(CollectionUtils.isEmpty(entityTypeMetaDataList)){
            entityTypeMetaDataList = new ArrayList<>();
        }
        for (AssignmentFieldMetaData ifGridItem : assignmentFieldMetaDataList) {
            if(Objects.isNull(ifGridItem.getFieldName())){
                rowData.add("");
            } else {
            	if(null != ifGridItem .getRuleBased() && ifGridItem .getRuleBased()){
            		rowData.add(ifGridItem.getFieldName());
            	}else{
            		rowData.add(ifGridItem.getFieldName() + "  " + ifGridItem.getOperator());
            	}

            }
        }
        for (EntityTypeMetaData thenGridItem : entityTypeMetaDataList) {
            if(Objects.isNull(thenGridItem.getDisplayName())){
                rowData.add("");
            } else {
                rowData.add(thenGridItem.getDisplayName()+"####");
            }
        }
        rowData.add("Hold####");
        rowData.add("Actions");
    }

    @Override
    public String populateRowDataTask(String mode, Integer index, ModelMap map, AssignmentSet assignmentSetVO, Long moduleId) {

        LinkedHashMap<Object,Object> hashMapTemp = new LinkedHashMap<>();
        if(mode.equalsIgnoreCase("edit")){
            for(AssignmentFieldMetaData assignmentFieldMetaData:((AssignmentGrid )assignmentSetVO).getAssignmentFieldMetaDataList()){
                if((Objects.nonNull(assignmentFieldMetaData.getWebBinderName())&& assignmentFieldMetaData.getWebBinderName() != "")){
                    if(AssignmentConstants.BETWEEN_OPERATOR.equalsIgnoreCase(assignmentFieldMetaData.getOperator())){
                        List<String> paramList = new ArrayList<>();
                        String[] arrayParam = ((String)assignmentSetVO.getAssignmentMatrixRowData().get(index).getLinkedMap().get(assignmentFieldMetaData.getIndexId())).split(",");
                        for(String elementId : arrayParam){
                            paramList.add(elementId);
                        }
                        hashMapTemp.put(assignmentFieldMetaData.getIndexId(), paramList);

                    }
                }
                else{
                    if(AssignmentConstants.BETWEEN_OPERATOR.equalsIgnoreCase(assignmentFieldMetaData.getOperator())){
                        List<String> paramList = new ArrayList<>();
                        String[] arrayParam = ((String)assignmentSetVO.getAssignmentMatrixRowData().get(index).getLinkedMap().get(assignmentFieldMetaData.getIndexId())).split(",");
                        for(String elementId : arrayParam){
                            paramList.add(elementId);
                        }
                        hashMapTemp.put(assignmentFieldMetaData.getIndexId(), paramList);

                    }
                }
            }
        }
        else{
            List<AssignmentMatrixRowData> rowDataList = assignmentSetVO.getAssignmentMatrixRowData();
            if(CollectionUtils.isNotEmpty(rowDataList)){
                index = rowDataList.size();
            }
        }
        map.put("paramList",hashMapTemp);
        map.put("moduleId", moduleId);
        map.put("assignmentSetVO",assignmentSetVO);
        map.put("thenView",true);
        map.put("index",index);
        List<String> ruleResultList = new ArrayList<>();
        ruleResultList.add("P");
        ruleResultList.add("F");
        ruleResultList.add("*");
        map.put("ruleResultList",ruleResultList);
        return "gridRowDataTaskAssignment";
    }



}
