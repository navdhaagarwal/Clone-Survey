package com.nucleus.rules.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nucleus.rules.model.ParameterDataType;

public final class RuleConstants {

    public static final char                RULE_RESULT_PASS                         = 'P';
    public static final char                RULE_RESULT_FAIL                         = 'F';
    public static final char                RULE_RESULT_NORESULT                     = '0';
    public static final char                RULE_RESULT_IGNORE                       = '*';

    // used in credit policy jsp for matching
    public static final String              RULE_RESULT_PASS_STRING                  = "P";
    public static final String              RULE_RESULT_FAIL_STRING                  = "F";
    public static final String              RULE_RESULT_NORESULT_STRING              = "0";
    public static final String              RULE_RESULT_IGNORE_STRING                = "*";
    public static final String              RULE_RESULT_RULE_NORESULT                = "No Result";

    public static final int                 LEFT_ASSOC                               = 0;
    public static final int                 RIGHT_ASSOC                              = 1;

    public static final String              LEFT_PAREN                               = "(";
    public static final String              RIGHT_PAREN                              = ")";
    public static final String              LEFT_CURLY_BRACES                        = "{";
    public static final String              RIGHT_CURLY_BRACES                       = "}";
    public static final String              REL_OPS                                  = ">|<|>=|<=|==|!=";
    public static final String              DATE_PATTERN                             = "mm/dd/yyyy";

    // Supported operators
    public static final Map<String, int[]>  ARITH_OPERATORS                          = new HashMap<String, int[]>();
    public static final Map<String, int[]>  REL_OPERATORS                            = new HashMap<String, int[]>();
    public static final Map<String, int[]>  LOG_OPERATORS                            = new HashMap<String, int[]>();
    public static final Map<String, int[]>  OPERATORS                                = new HashMap<String, int[]>();

    // Operator to English Conversion
    public static final Map<String, String> operatorToEnglish                        = new HashMap<String, String>();

    public static final Map<String, String> approvalStatusMap                        = new HashMap<String, String>();

    public static final int                 MILLIS_IN_SECOND                         = 1000;

    public static final int                 SECONDS_IN_MINUTE                        = 60;

    public static final int                 MINUTES_IN_HOUR                          = 60;

    public static final int                 HOURS_IN_DAY                             = 24;

    public static final int                 DAYS_IN_YEAR                             = 365;

    public static final long                MILLISECONDS_IN_YEAR                     = (long) MILLIS_IN_SECOND
                                                                                             * SECONDS_IN_MINUTE
                                                                                             * MINUTES_IN_HOUR
                                                                                             * HOURS_IN_DAY * DAYS_IN_YEAR;

    public static final String              PARAMETER_NAME_ID                        = "$";

    public static final String              CURSOR_POSITION_PLACEHOLDER              = "$";

    public static final String              REPLACE_SPACE_IN_NAME                    = "_";

    public static final String              NUMERIC_CHARACTERS                       = " 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ";

    public static final String              RULE_KEY                                 = "Rule";

    public static final String              RULEGROUP_KEY                            = "RuleGroup";

    public static final String              RULESET_KEY                              = "RuleSet";

    public static final String              RULE_GROUP_RESULT_KEY                    = "RulegroupResult";

    public static final String              RULE_GROUP_PATTERN_KEY                   = "RulegroupPatternResult";

    public static final String              CRITERIA_RULES_RESULT_KEY                = "CriteriaRulesResult";

    public static final String              ALL_RULES_RESULT                         = "AllRulesResult";

    public static final String              CONTEXT_OBJECT                           = "contextObject";

    public static final String              AND_OPERATOR                             = "&&";

    public static final String              OR_OPERATOR                              = "||";

    public static final String              AND_OPERATOR_ENGLISH                     = "AND";

    public static final String              OR_OPERATOR_ENGLISH                      = "OR";
    
    public static final String              NOT_OPERATOR_ENGLISH                      = " !";

    public static final List<String>        RULE_GROUP_OPERATORS                     = new ArrayList<String>();

    public static final String              ADDITION_OPERATOR                        = "+";
    public static final String              SUBSTRACTION_OPERATOR                    = "-";
    public static final String              MULTIPLICATION_OPERATOR                  = "*";
    public static final String              DIVISION_OPERATOR                        = "/";
    public static final String              MODULUS_OPERATOR                         = "%";
    public static final String              POWER_OPERATOR                           = "**";

    public static final int                 PARAMETER_PAGE_SIZE                      = 10;
    public static final int                 CONDITION_PAGE_SIZE                      = 10;
    public static final int                 RULE_PAGE_SIZE                           = 10;

    public static final String              RULE_EXCEPTION_MESSAGE                   = "Some Exception occured while evaluating rule exception";

    public static final String              RULE_TIME_IN_MILLIS                      = ".millis";
    
    public static final String              RULE_TIME_FOR_JAVA_UTIL_DATE             = ".time";

    public static final String              RULE_IDS                                 = ".id";

    public static final String              NULL_SAFE_RULE_IDS                       = ".?id";

    public static final String              RULE_NULL_VALUE_EXCEPTION                = "Null Value";

    public static final List<String>        compoundOperators                        = new ArrayList<String>();

    public static final List<String>        conditionOperators                       = new ArrayList<String>();

    public static final List<String>        conditionOperatorsForMVELScript          = new ArrayList<String>();

    public static final List<String>        ruleOperators                            = new ArrayList<String>();

    public static final int                 RULE_SEED_SIZE                           = 8;

    public static final int                 CONDITION_SEED_SIZE                      = 3;

    public static final int                 COMPOUND_SEED_SIZE                       = 4;

    public static final String              SYSTEM_USER                              = "com.nucleus.user.User:9001";

    public static final String              SEED_MAKER                               = "com.nucleus.user.User:9015";

    public static final String              SEED_CHECKER                             = "com.nucleus.user.User:9016";

    public static final String              EXPRESSION_RESULT                        = "expressionResult";

    public static final String              IS_PLACE_HOLDER_PARAMETER_USED           = "isPlaceHolderParam";

    public static final String              CRITERIA_DOT_JOINED                      = "joined.";

    public static final String              CRITERIA_JOINED                          = "joined";

    public static final List<String>        DATATYPE_CODES_CONSTANT_PARAM            = new ArrayList<String>();

    public static final List<String>        DATATYPE_CODES_NONCONSTANT_PARAM         = new ArrayList<String>();

    public static final List<String>        DATATYPE_CODES_COMPPOUND_PARAM           = new ArrayList<String>();

    public static final String              SCRIPT_RULE_SENTENCE                     = "Script Rule";

    public static final int                 SAVE_ENTITY                              = 0;

    public static final int                 SAVE_AND_SEND_FOR_APRROVAL_ENTITY        = 1;

    public static final String              PLACEHOLDER_CONTEXT_NAME                 = "context";

    public static final String              PLACEHOLDER_OBJ                          = "obj";

    public static final String              IS_NULL_JPQL                             = "IS NULL";

    public static final int                 SCRIPTCODETYPE_SHELL_SCRIPT              = 1;

    public static final int                 SCRIPTCODETYPE_MVEL_SCRIPT               = 2;
    
    public static final String              MVEL_SHELL_SCRIPT_COLLECTION_TYPE        = "[]";

    public static final String              MVEL_SHELL_SCRIPT_AGGRERATE_SUM_CODE     = "1";

    public static final String              MVEL_SHELL_SCRIPT_AGGRERATE_AVERAGE_CODE = "2";
    
    public static final String              MVEL_SHELL_SCRIPT_AGGRERATE_MAX_CODE = "3";
    
    public static final String              MVEL_SHELL_SCRIPT_AGGRERATE_MIN_CODE = "4";

    public static final String              MVEL_SHELL_SCRIPT_FILTER_PLACEHOLDER     = "$";

    public static final Map<String, String> MVEL_SHELL_SCRIPT_AGGRGRATE_FUNCTION     = new HashMap<String, String>();

    public static final String              MVEL_FOREACH                             = " foreach ";
    public static final String              MVEL_COLON                               = " : ";
    public static final String              MVEL_SEMICOLON                           = " ;";
    public static final String              MVEL_LOOPVARIABLE                        = " loopVariable";
    public static final String              MVEL_IF                                  = "if ";
    public static final String              MVEL_RETURN_KEYWORD                      = "return ";
    public static final String              MVEL_RETURN_VARIABLE                     = "returnValue ";
    public static final String              MVEL_INDEX_VARIABLE                      = "totalIndex ";
    public static final String              MVEL_EQUAL                               = " = ";
    public static final String              MVEL_PLUS                                = " + ";
    public static final String              MVEL_GREATER_THAN                                = " > ";
    public static final String              MVEL_SMALLER_THAN                                = " < ";
    public static final String              MVEL_RETURN_STATEMENT_SUM                = "return returnValue ";
    public static final String              MVEL_RETURN_STATEMENT_AVERAGE            = "return (returnValue/totalIndex) ";

    public static final String              RULE_ELAPSED_TIME                        = "RuleElapsedTime";

    public static final String              EXPRESSION_SCRIPT_IF_START               = "if(";
    public static final String              EXPRESSION_SCRIPT_IF_END                 = ") {";
    public static final String              EXPRESSION_SCRIPT_NULL_CHECK             = " == null ";
    public static final String              EXPRESSION_SCRIPT_NULL_VALUE            = " null ";
    public static final String              EXPRESSION_SCRIPT_NOT_NULL_CHECK         = " != null ";
    public static final String              EXPRESSION_SCRIPT_RETURN_STATEMENT       = "return ";
    public static final String              EXPRESSION_SCRIPT_SEMICOLON              = " ;";
    public static final String              EXPRESSION_SCRIPT_RETURN_FALSE           = "return false ;";
    public static final String              EXPRESSION_SCRIPT_RETURN_NULL            = "return null ;";
    public static final String              EXPRESSION_SCRIPT_SIZE_CHECK            = ").isEmpty()) ";
    
    public static final String              MVEL_RESULT                              = "NaN,Infinity";

    public static final String              RULE_RESULT_NOT_APPLICABLE               = "N.A";

    public static final String              MVEL_BOOLEANFLAG_EQUAL_FALSE             = "booleanFlag = false ;";
    public static final String              MVEL_BOOLEANFLAG_EQUAL_TRUE              = "booleanFlag = true ;";
    public static final String              MVEL_RETURN_TRUE                         = "return true ; ";
    public static final String              MVEL_ELSE_OPEN                           = "else { ";
    public static final String              MVEL_ELSE_IF_OPEN                           = "else if( ";
    public static final String              MVEL_BOOLEANFLAG                         = " booleanFlag ";

    public static final String              SQL_RULE_SENTENCE                     = "SQL Rule";
    // Loading all the values on startup
    static {
        ARITH_OPERATORS.put("+", new int[] { 25, LEFT_ASSOC });
        ARITH_OPERATORS.put("-", new int[] { 25, LEFT_ASSOC });
        ARITH_OPERATORS.put("*", new int[] { 30, LEFT_ASSOC });
        ARITH_OPERATORS.put("/", new int[] { 30, LEFT_ASSOC });
        ARITH_OPERATORS.put("%", new int[] { 30, LEFT_ASSOC });
        ARITH_OPERATORS.put("^", new int[] { 35, RIGHT_ASSOC });
        ARITH_OPERATORS.put("**", new int[] { 30, LEFT_ASSOC });

        REL_OPERATORS.put("<", new int[] { 20, LEFT_ASSOC });
        REL_OPERATORS.put("<=", new int[] { 20, LEFT_ASSOC });
        REL_OPERATORS.put(">", new int[] { 20, LEFT_ASSOC });
        REL_OPERATORS.put(">=", new int[] { 20, LEFT_ASSOC });
        REL_OPERATORS.put("==", new int[] { 20, LEFT_ASSOC });
        REL_OPERATORS.put("!=", new int[] { 20, RIGHT_ASSOC });

        LOG_OPERATORS.put("!", new int[] { 15, RIGHT_ASSOC });

        LOG_OPERATORS.put("&&", new int[] { 10, LEFT_ASSOC });

        LOG_OPERATORS.put("||", new int[] { 5, LEFT_ASSOC });

        LOG_OPERATORS.put("EQV", new int[] { 0, LEFT_ASSOC });
        LOG_OPERATORS.put("NEQV", new int[] { 0, LEFT_ASSOC });

        OPERATORS.putAll(ARITH_OPERATORS);
        OPERATORS.putAll(REL_OPERATORS);
        OPERATORS.putAll(LOG_OPERATORS);

        operatorToEnglish.put("&&", "AND");
        operatorToEnglish.put("||", "OR");
        operatorToEnglish.put("~", "XOR");
        operatorToEnglish.put("!=", "IS NOT EQUAL TO");
        operatorToEnglish.put("!", "NOT");

        operatorToEnglish.put("+", "PLUS");
        operatorToEnglish.put("-", "MINUS");
        operatorToEnglish.put("*", "MULTIPLY BY");
        operatorToEnglish.put(">", "IS GREATER THAN");
        operatorToEnglish.put(">=", "IS GREATER THAN EQUAL TO");
        operatorToEnglish.put("<", "IS LESS THAN");
        operatorToEnglish.put("<=", "IS LESS THAN EQUAL TO");
        operatorToEnglish.put("/", "DIVIDED BY");
        operatorToEnglish.put("==", "IS EQUAL TO");
        operatorToEnglish.put("**", "RAISE TO THE POWER OF");

        RULE_GROUP_OPERATORS.add("&&");
        RULE_GROUP_OPERATORS.add("||");
        RULE_GROUP_OPERATORS.add("(");
        RULE_GROUP_OPERATORS.add(")");

        compoundOperators.add("(");
        compoundOperators.add(")");
        compoundOperators.add("+");
        compoundOperators.add("-");
        compoundOperators.add("*");
        compoundOperators.add("/");
        compoundOperators.add("**");

        conditionOperators.add("(");
        conditionOperators.add(")");

        conditionOperators.add(">");
        conditionOperators.add("<");
        conditionOperators.add(">=");
        conditionOperators.add("<=");
        conditionOperators.add("!=");
        conditionOperators.add("==");

        conditionOperators.add("+");
        conditionOperators.add("-");
        conditionOperators.add("/");
        conditionOperators.add("*");
        conditionOperators.add("**");

        conditionOperatorsForMVELScript.add(">");
        conditionOperatorsForMVELScript.add("<");
        conditionOperatorsForMVELScript.add(">=");
        conditionOperatorsForMVELScript.add("<=");
        conditionOperatorsForMVELScript.add("!=");
        conditionOperatorsForMVELScript.add("==");

        ruleOperators.add("&&");
        ruleOperators.add("||");
        ruleOperators.add(")");
        ruleOperators.add("(");

        DATATYPE_CODES_CONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN));
        DATATYPE_CODES_CONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_DATE));
        DATATYPE_CODES_CONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE));
        DATATYPE_CODES_CONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER));
        // DATATYPE_CODES_CONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_PLACEHOLDER));
        DATATYPE_CODES_CONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_STRING));

        DATATYPE_CODES_NONCONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN));
        DATATYPE_CODES_NONCONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_DATE));
        DATATYPE_CODES_NONCONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE));
        DATATYPE_CODES_NONCONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER));
        DATATYPE_CODES_NONCONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_PLACEHOLDER));
        DATATYPE_CODES_NONCONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_STRING));
        DATATYPE_CODES_NONCONSTANT_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE));

        DATATYPE_CODES_COMPPOUND_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER));
        DATATYPE_CODES_COMPPOUND_PARAM.add(Integer.toString(ParameterDataType.PARAMETER_DATA_TYPE_STRING));

        approvalStatusMap.put("0", "APPROVED");
        approvalStatusMap.put("1", "UNAPPROVED");
        approvalStatusMap.put("2", "WORFLOW_IN_PROGRESS");
        approvalStatusMap.put("3", "APPROVED_MODIFIED");
        approvalStatusMap.put("4", "APPROVED_DELETED");
        approvalStatusMap.put("6", "APPROVED_DELETED_IN_PROGRESS");
        approvalStatusMap.put("7", "UNAPPROVED_ADDED");
        approvalStatusMap.put("8", "UNAPPROVED_MODIFIED");
        approvalStatusMap.put("10", "UNAPPROVED_HISTORY");

        MVEL_SHELL_SCRIPT_AGGRGRATE_FUNCTION.put(MVEL_SHELL_SCRIPT_AGGRERATE_SUM_CODE, "SUM");
        MVEL_SHELL_SCRIPT_AGGRGRATE_FUNCTION.put(MVEL_SHELL_SCRIPT_AGGRERATE_AVERAGE_CODE, "AVERAGE");
        MVEL_SHELL_SCRIPT_AGGRGRATE_FUNCTION.put(MVEL_SHELL_SCRIPT_AGGRERATE_MAX_CODE, "MAX");
        MVEL_SHELL_SCRIPT_AGGRGRATE_FUNCTION.put(MVEL_SHELL_SCRIPT_AGGRERATE_MIN_CODE, "MIN");

    }

    public static final String SQL_PARAM_RESULT_FOUND ="S";
    public static final String SQL_PARAM_RESULT_NOT_FOUND ="F";
    public static final String NOT_APPLICABLE = "NA";
    public static final String ERROR = "ERROR";
    public static final String WARNING = "WARNING";
    public static final String ALIAS=" Alias required for : ";

    public static final String RULE_EXCEPTION = "RULE_EXCEPTION";
    public static final String RULE_ACTION_EXCEPTION = "RULE_ACTION_EXCEPTION";
    public static final String PARAMETER_EXCEPTION = "PARAMETER_EXCEPTION";


}
