package com.nucleus.rules.model.assignmentMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nucleus.core.team.entity.Team;
import com.nucleus.rules.model.ParameterDataType;
import com.nucleus.user.User;

public class AssignmentConstants {

    public static final Map<String, Map<Integer, List<Integer>>> operatorsDataTypeMap                     = new HashMap<String, Map<Integer, List<Integer>>>();

    public static final List<Integer>                            ASSIGNMENT_SUPPORTED_DATATYPES           = new ArrayList<Integer>();

    public static final Map<Integer, String[]>                   operatorsSupportedByDataTypeMap_ASSIGN   = new HashMap<Integer, String[]>();

    public static final String[]                                 SUPPORTED_OPERATORS_STRING_ASSIGN        = { "==", "!=",
            "IN", "NOT_IN"                                                                               };

    public static final String[]                                 SUPPORTED_OPERATORS_INTEGER_ASSIGN       = { "==", "!=",
            ">", "<", ">=", "<=", "IN", "BETWEEN", "NOT_IN"                                              };

    public static final String[]                                 SUPPORTED_OPERATORS_NUMBER_ASSIGN        = { "==", "!=",
            ">", "<", ">=", "<=", "IN", "BETWEEN", "NOT_IN"                                              };

    public static final String[]                                 SUPPORTED_OPERATORS_BOOLEAN_ASSIGN       = { "==", "!=" };

    public static final String[]                                 SUPPORTED_OPERATORS_DATE_ASSIGN          = { "==", "!=",
            ">", "<", ">=", "<=", "IN", "BETWEEN", "NOT_IN"                                              };
    
    public static final String[]                                 SUPPORTED_OPERATORS_JAVA_UTIL_DATE_ASSIGN = { "==", "!=",
            ">", "<", ">=", "<=", "IN", "BETWEEN", "NOT_IN"                                              };

    public static final String[]                                 SUPPORTED_OPERATORS_REFERENCE_ASSIGN     = { "==", "!=",
            "IN", "NOT_IN"                                                                               };

    // DataMap for "==" and "!=" -- Start
    public static final Map<Integer, List<Integer>>              DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ = new HashMap<Integer, List<Integer>>();

    public static final Integer[]                                STRING_SUPPORTED_DATATYPES_ARRAY         = { ParameterDataType.PARAMETER_DATA_TYPE_STRING };

    public static final Integer[]                                INTEGER_SUPPORTED_DATATYPES_ARRAY        = {
            ParameterDataType.PARAMETER_DATA_TYPE_INTEGER, ParameterDataType.PARAMETER_DATA_TYPE_NUMBER  };

    public static final Integer[]                                NUMBER_SUPPORTED_DATATYPES_ARRAY         = {
            ParameterDataType.PARAMETER_DATA_TYPE_INTEGER, ParameterDataType.PARAMETER_DATA_TYPE_NUMBER  };

    public static final Integer[]                                DATE_SUPPORTED_DATATYPES_ARRAY           = { ParameterDataType.PARAMETER_DATA_TYPE_DATE };
    
    public static final Integer[]                                JAVA_UTIL_DATE_SUPPORTED_DATATYPES_ARRAY = { ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE };

    public static final Integer[]                                BOOLEAN_SUPPORTED_DATATYPES_ARRAY        = { ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN };

    public static final Integer[]                                REFERNECE_SUPPORTED_DATATYPES_ARRAY      = { ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE };

    // DataMap for "==" and "!=" -- End

    // DataMap for ">", "<", ">=", "<=" -- Start
    public static final Map<Integer, List<Integer>>              DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT = new HashMap<Integer, List<Integer>>();

    /**
     * Added for service checks -- Start
     */

    public static List<String>                                   ALL_SUPOORTED_OPERATORS                  = new ArrayList<String>();

    public static final String                                   EQUALS                                   = "==";
    public static final String                                   NOT_EQUALS                               = "!=";

    public static final String                                   GREATER_THEN                             = ">";
    public static final String                                   GREATER_THEN_EQUALS                      = ">=";

    public static final String                                   LESS_THEN                                = "<";
    public static final String                                   LESS_THEN_EQUALS                         = "<=";

    public static final String                                   IN_OPERATOR                              = "IN";

    public static final String                                   NOT_IN_OPERATOR                          = "NOT_IN";

    public static final String                                   BETWEEN_OPERATOR                         = "BETWEEN";

    public static final String                                   MULTI_VALUE_SEPARATOR                    = "#";

    public static final String                                   TEAM_URI                                 = "teamUri";

    public static final String                                   USER_URI                                 = "userUri";
    public static final String                                   HOLD_FLAG                                 = "assignmentHoldFlag";

    public static final String                                   ANY_URI                                  = "anyUri";

    public static final Class<User>                              User                                     = User.class;

    public static final Class<Team>                              Team                                     = Team.class;

    public static final List<String>                             assignmentExpressionOperators            = new ArrayList<String>();

    public static final String                                   USER                                     = "User";
    public static final String                                   TEAM                                     = "Team";

    /**
     * To check the AssignmentSet is whether Grid or Expression
     */
    public static final int                                      ASSIGNMENT_SET_TYPE_GRID                 = 0;
    public static final int                                      ASSIGNMENT_SET_TYPE_EXPRESSION           = 1;
    public static final int                                      ASSIGNMENT_SET_TYPE_CRITERIA             = 2;
    public static final String                                   ASSIGNMENT_SET_GRID                      = "GRID";
    public static final String                                   ASSIGNMENT_SET_EXPRESSION                = "EXPRESSION";
    public static final String                                   APPLICATION_STAMPING_DATE = "applicationStampingDate";
    public static final String                                   ASSIGNMENT_SET_EXECUTION_VO_LIST = "assignmentSetExecutionVOList";
    public static final String                                   ASSIGNMENT_SET_EXECUTION_RESULT_MAP = "assignmentSetExecutionResultMap";
    public static final String                                   ASSIGNMENT_OBJECT_EXPRESSION_RESULT = "assignmentObjectExpressionResult";
    public static final String                                   IS_ASSIGNMENT_MATRIX = "isAssignmentMatrix";
    public static final String                                   THEN_ACTION_PARAMETER_SEARCH ="thenActionParameterSearch";
    /**
     * Added for service checks -- End
     */

    static {

        ASSIGNMENT_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);
        ASSIGNMENT_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_DATE);
        ASSIGNMENT_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE);
        ASSIGNMENT_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER);
        ASSIGNMENT_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);
        ASSIGNMENT_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_STRING);
        ASSIGNMENT_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE);

        operatorsSupportedByDataTypeMap_ASSIGN.put(ParameterDataType.PARAMETER_DATA_TYPE_STRING,
                SUPPORTED_OPERATORS_STRING_ASSIGN);
        operatorsSupportedByDataTypeMap_ASSIGN.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                SUPPORTED_OPERATORS_INTEGER_ASSIGN);
        operatorsSupportedByDataTypeMap_ASSIGN.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                SUPPORTED_OPERATORS_NUMBER_ASSIGN);
        operatorsSupportedByDataTypeMap_ASSIGN.put(ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN,
                SUPPORTED_OPERATORS_BOOLEAN_ASSIGN);
        operatorsSupportedByDataTypeMap_ASSIGN.put(ParameterDataType.PARAMETER_DATA_TYPE_DATE,
                SUPPORTED_OPERATORS_DATE_ASSIGN);
        operatorsSupportedByDataTypeMap_ASSIGN.put(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE,
                SUPPORTED_OPERATORS_JAVA_UTIL_DATE_ASSIGN);
        operatorsSupportedByDataTypeMap_ASSIGN.put(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE,
                SUPPORTED_OPERATORS_REFERENCE_ASSIGN);

        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ.put(ParameterDataType.PARAMETER_DATA_TYPE_STRING,
                Arrays.asList(STRING_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                Arrays.asList(INTEGER_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                Arrays.asList(NUMBER_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ.put(ParameterDataType.PARAMETER_DATA_TYPE_DATE,
                Arrays.asList(DATE_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ.put(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE,
                Arrays.asList(JAVA_UTIL_DATE_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ.put(ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN,
                Arrays.asList(BOOLEAN_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ.put(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE,
                Arrays.asList(REFERNECE_SUPPORTED_DATATYPES_ARRAY));

        operatorsDataTypeMap.put("==", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ);
        operatorsDataTypeMap.put("!=", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ);

        operatorsDataTypeMap.put("<", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT);
        operatorsDataTypeMap.put(">", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT);
        operatorsDataTypeMap.put(">=", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT);
        operatorsDataTypeMap.put("<=", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT);

        ALL_SUPOORTED_OPERATORS.add(">");
        ALL_SUPOORTED_OPERATORS.add(">=");
        ALL_SUPOORTED_OPERATORS.add("<");
        ALL_SUPOORTED_OPERATORS.add("<=");
        ALL_SUPOORTED_OPERATORS.add("==");
        ALL_SUPOORTED_OPERATORS.add("!=");
        ALL_SUPOORTED_OPERATORS.add("(");
        ALL_SUPOORTED_OPERATORS.add(")");
        ALL_SUPOORTED_OPERATORS.add(" ");
        ALL_SUPOORTED_OPERATORS.add("||");
        ALL_SUPOORTED_OPERATORS.add("&&");
        ALL_SUPOORTED_OPERATORS.add("IN");
        ALL_SUPOORTED_OPERATORS.add("BETWEEN");

        assignmentExpressionOperators.add("(");
        assignmentExpressionOperators.add(")");

        assignmentExpressionOperators.add(">");
        assignmentExpressionOperators.add("<");
        assignmentExpressionOperators.add(">=");
        assignmentExpressionOperators.add("<=");
        assignmentExpressionOperators.add("!=");
        assignmentExpressionOperators.add("==");

        assignmentExpressionOperators.add("+");
        assignmentExpressionOperators.add("-");
        assignmentExpressionOperators.add("/");
        assignmentExpressionOperators.add("*");

        assignmentExpressionOperators.add("&&");
        assignmentExpressionOperators.add("||");
    }

    public static final String ASSIGNMENT_RESULT_CONTEXT = "contextObjectAssignmentMatrixResult";
    public static final String ASSIGNMENT_RESULT_MAP_CONTEXT = "contextObjectAssignmentMatrixResultMap";
    public static final String MULTIPLE_RESULTS = "multipleResults";
    public static final String INDEX_REPLACEMENT ="indexReplaced";
    public static final String MULTIPLE_RESULT_CONTEXT = "contextObjectMultipleResults";
    public static final String JUNK_VALUE = "junk";
}
