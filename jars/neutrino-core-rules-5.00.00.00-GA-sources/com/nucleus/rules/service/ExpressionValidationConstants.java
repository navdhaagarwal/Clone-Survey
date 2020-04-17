package com.nucleus.rules.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nucleus.rules.model.ParameterDataType;

public class ExpressionValidationConstants {

    /** 
     * ****** operatorsDataTypeMap **********
     * Map holding Key as operator and value as map.
           Internal Map contains key as datatype and value 
               as list of supported datatype
     */

    public static final Map<String, Map<Integer, List<Integer>>> operatorsDataTypeMap                           = new HashMap<String, Map<Integer, List<Integer>>>();

    /** 
     * ****** operatorsDataTypeResultMap **********
     * Map holding Key as operator and value as map.
           Internal Map contains key as datatype and value 
               as resultant datatype
     */
    public static final Map<String, Map<Integer, Integer>>       operatorsDataTypeResultMap                     = new HashMap<String, Map<Integer, Integer>>();

    public static final String[]                                 SUPPORTED_OPERATORS_STRING_COMP                = { "+" };
    public static final String[]                                 SUPPORTED_OPERATORS_INTEGER_COMP               = { "+",
            "-", "*", "/", "**"                                                                                };
    public static final String[]                                 SUPPORTED_OPERATORS_NUMBER_COMP                = { "+",
            "-", "*", "/", "**"                                                                                };
    public static final String[]                                 SUPPORTED_OPERATORS_BOOLEAN_COMP               = {};
    public static final String[]                                 SUPPORTED_OPERATORS_DATE_COMP                  = { "-" };
    public static final String[]                                 SUPPORTED_OPERATORS_JAVA_UTIL_DATE_COMP        = { "-" };
    public static final String[]                                 SUPPORTED_OPERATORS_REFERENCE_COMP             = {};

    public static final String[]                                 SUPPORTED_OPERATORS_STRING_SCRIPT_MVEL         = { "==" };
    public static final String[]                                 SUPPORTED_OPERATORS_INTEGER_SCRIPT_MVEL        = { "<",
            ">", "<=", ">=", "=="                                                                              };
    public static final String[]                                 SUPPORTED_OPERATORS_NUMBER_SCRIPT_MVEL         = { "<",
            ">", "<=", ">=", "=="                                                                              };
    public static final String[]                                 SUPPORTED_OPERATORS_BOOLEAN_SCRIPT_MVEL        = { "==" };
    public static final String[]                                 SUPPORTED_OPERATORS_DATE_SCRIPT_MVEL           = { "<",
            ">", "<=", ">=", "=="                                                                              };
    public static final String[]                                 SUPPORTED_OPERATORS_JAVA_UTIL_DATE_SCRIPT_MVEL = { "<",
            ">", "<=", ">=", "=="                                                                              };
    public static final String[]                                 SUPPORTED_OPERATORS_REFERENCE_SCRIPT_MVEL      = { "==" };

    public static final Map<Integer, String[]>                   operatorsSupportedByDataTypeMap_COMP           = new HashMap<Integer, String[]>();

    public static final Map<Integer, String[]>                   operatorsSupportedByDataTypeMap_SCRIPT_MVEL    = new HashMap<Integer, String[]>();

    public static final String[]                                 SUPPORTED_CONDITION_JOIN_OPERATORS_MVEL_SCRIPT = { "&&",
            "||"                                                                                               };

    public static final String[]                                 SUPPORTED_OPERATORS_STRING_CON                 = { "+",
            "==", "!="                                                                                         };
    public static final String[]                                 SUPPORTED_OPERATORS_INTEGER_CON                = { "+",
            "-",

            "*", "/", "**", "==", "!=", ">", "<", ">=", "<="                                                   };
    public static final String[]                                 SUPPORTED_OPERATORS_NUMBER_CON                 = { "+",
            "-", "*", "/", "**", "==", "!=", ">", "<", ">=", "<="                                              };
    public static final String[]                                 SUPPORTED_OPERATORS_BOOLEAN_CON                = { "==",
            "!="                                                                                               };
    public static final String[]                                 SUPPORTED_OPERATORS_DATE_CON                   = { "-",
            "==", "!=", ">", "<", ">=", "<="                                                                   };
    public static final String[]                                 SUPPORTED_OPERATORS_JAVA_UTIL_DATE_CON        = { "-",
            "==", "!=", ">", "<", ">=", "<="                                                                   };
    public static final String[]                                 SUPPORTED_OPERATORS_REFERENCE_CON              = { "==",
            "!="                                                                                               };

    public static final Map<Integer, String[]>                   operatorsSupportedByDataTypeMap_CON            = new HashMap<Integer, String[]>();

    public static final Integer[]                                STRING_SUPPORTED_DATATYPES_ARRAY               = { ParameterDataType.PARAMETER_DATA_TYPE_STRING };

    public static final Integer[]                                INTEGER_SUPPORTED_DATATYPES_ARRAY              = {
            ParameterDataType.PARAMETER_DATA_TYPE_INTEGER, ParameterDataType.PARAMETER_DATA_TYPE_NUMBER        };

    public static final Integer[]                                NUMBER_SUPPORTED_DATATYPES_ARRAY               = {
            ParameterDataType.PARAMETER_DATA_TYPE_INTEGER, ParameterDataType.PARAMETER_DATA_TYPE_NUMBER        };

    public static final Integer[]                                DATE_SUPPORTED_DATATYPES_ARRAY                 = { ParameterDataType.PARAMETER_DATA_TYPE_DATE };
    
    public static final Integer[]                                JAVA_UTIL_DATE_SUPPORTED_DATATYPES_ARRAY       = { ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE };

    public static final Integer[]                                BOOLEAN_SUPPORTED_DATATYPES_ARRAY              = { ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN };

    public static final Integer[]                                REFERNECE_SUPPORTED_DATATYPES_ARRAY            = { ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE };

    // Put Supported Data Types -- Start

    // DataMap for "==" and "!=" -- Start
    public static final Map<Integer, List<Integer>>              DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ       = new HashMap<Integer, List<Integer>>();

    // DataMap for "==" and "!=" -- End

    // DataMap for ">", "<", ">=", "<=" -- Start
    public static final Map<Integer, List<Integer>>              DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT       = new HashMap<Integer, List<Integer>>();

    // DataMap for ">", "<", ">=", "<=" -- End

    // DataMap for "+" -- Start
    public static final Map<Integer, List<Integer>>              DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_PLUS     = new HashMap<Integer, List<Integer>>();
    // DataMap for "+" -- End

    // DataMap for "+" -- Start
    public static final Map<Integer, List<Integer>>              DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_Minus    = new HashMap<Integer, List<Integer>>();
    // DataMap for "+" -- End

    // DataMap for "*", "/" -- Start
    public static final Map<Integer, List<Integer>>              DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_AT       = new HashMap<Integer, List<Integer>>();
    // DataMap for "*","/" -- End

    // DataMap for "**", "/" -- Start
    public static final Map<Integer, List<Integer>>              DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_POWER    = new HashMap<Integer, List<Integer>>();
    // DataMap for "**","/" -- End

    // Put Supported Data Types -- End

    // For DataTypes and there results -- Start

    public static final Map<Integer, Integer>                    OPERATOR_PLUS_DATATYPE_RESULT                  = new HashMap<Integer, Integer>();

    public static final Map<Integer, Integer>                    OPERATOR_MINUS_DATATYPE_RESULT                 = new HashMap<Integer, Integer>();

    public static final Map<Integer, Integer>                    OPERATOR_DIVIDE_DATATYPE_RESULT                = new HashMap<Integer, Integer>();

    public static final Map<Integer, Integer>                    OPERATOR_MULTIPLY_DATATYPE_RESULT              = new HashMap<Integer, Integer>();

    public static final Map<Integer, Integer>                    OPERATOR_POWER_DATATYPE_RESULT                 = new HashMap<Integer, Integer>();

    public static final Map<String, Integer>                     REL_OPERATORS                                  = new HashMap<String, Integer>();

    public static final String[]                                 ARITHMETIC_OPS                                 = { "+",
            "-", "*", "/", "**", "!=", "=="                                                                    };

    public static final String[]                                 REL_OPS                                        = { ">",
            "<", ">=", "<=", "==", "!="                                                                        };

    // For DataTypes and there results -- End

    public static final Map<Integer, List<Integer>>              COMPOUND_PARAMETER_SUPPORTED_DATATYPES         = new HashMap<Integer, List<Integer>>();

    public static final Integer[]                                ALPHANUM_SUPPORTED_DATATYPES                   = { ParameterDataType.PARAMETER_DATA_TYPE_STRING };

    public static final Integer[]                                NUMBER_SUPPORTED_DATATYPES                     = {
            ParameterDataType.PARAMETER_DATA_TYPE_INTEGER, ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
            ParameterDataType.PARAMETER_DATA_TYPE_DATE, ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE   };

    public static final List<Integer>                            CONDITION_SUPPORTED_DATATYPES                  = new ArrayList<Integer>();

    // Rule Operators
    public static final String[]                                 RULE_OPERATORS                                 = { "!",
            "&&", "||", "!="                                                                                   };

    public static final String[]                                 NULL_CONDITION_OPERATORS                       = { "==",
            "!="                                                                                               };

    public static final String[]                                 ARITHMETIC_OPS_FOR_NULL_SAFE                   = { "+",
            "-", "*", "/", "**"                                                                                };

    public static final Map<String, Integer>                     LOGICAL_OPERATORS                              = new HashMap<String, Integer>();

    static {

        /**
         * Put value against == and != -- Start
         */

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

        /**
         * Put value against == and != -- End
         */

        /**
         * Put value against ">", "<", ">=", "<=" -- Start
         */

        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                Arrays.asList(INTEGER_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                Arrays.asList(NUMBER_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT.put(ParameterDataType.PARAMETER_DATA_TYPE_DATE,
                Arrays.asList(DATE_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT.put(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE,
                Arrays.asList(JAVA_UTIL_DATE_SUPPORTED_DATATYPES_ARRAY));

        /**
         * Put value against ">", "<", ">=", "<=" -- End
         */

        /**
         * Put value against "+"  -- Start
         */

        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_PLUS.put(ParameterDataType.PARAMETER_DATA_TYPE_STRING,
                Arrays.asList(STRING_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_PLUS.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                Arrays.asList(INTEGER_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_PLUS.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                Arrays.asList(NUMBER_SUPPORTED_DATATYPES_ARRAY));

        /**
         * Put value against "+"  -- End
         */

        /**
         * Put value against "-"  -- Start
         */

        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_Minus.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                Arrays.asList(INTEGER_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_Minus.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                Arrays.asList(NUMBER_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_Minus.put(ParameterDataType.PARAMETER_DATA_TYPE_DATE,
                Arrays.asList(DATE_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_Minus.put(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE,
                Arrays.asList(JAVA_UTIL_DATE_SUPPORTED_DATATYPES_ARRAY));

        /**
         * Put value against "-"  -- End
         */

        /**
         * Put value against "*", "/"  -- Start
         */

        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_AT.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                Arrays.asList(INTEGER_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_AT.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                Arrays.asList(NUMBER_SUPPORTED_DATATYPES_ARRAY));

        /**
         * Put value against "*", "/"  -- End
         */

        /**
         * Put value against "**", "/"  -- Start
         */

        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_POWER.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                Arrays.asList(INTEGER_SUPPORTED_DATATYPES_ARRAY));
        DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_POWER.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                Arrays.asList(NUMBER_SUPPORTED_DATATYPES_ARRAY));

        /**
         * Put value against "**", "/"  -- End
         */

        operatorsDataTypeMap.put("==", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ);
        operatorsDataTypeMap.put("!=", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_EQ);

        operatorsDataTypeMap.put("<", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT);
        operatorsDataTypeMap.put(">", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT);
        operatorsDataTypeMap.put(">=", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT);
        operatorsDataTypeMap.put("<=", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_GT);

        operatorsDataTypeMap.put("+", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_PLUS);
        operatorsDataTypeMap.put("-", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_Minus);
        operatorsDataTypeMap.put("*", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_AT);
        operatorsDataTypeMap.put("/", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_AT);
        operatorsDataTypeMap.put("**", DATATYPE_WITH_SUPPORTED_DATATYPES_FOR_POWER);
        /**
         * Data Type Result Maps
         */

        // For Plus Operator
        OPERATOR_PLUS_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_STRING,
                ParameterDataType.PARAMETER_DATA_TYPE_STRING);
        OPERATOR_PLUS_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);
        OPERATOR_PLUS_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);

        // For Minus Operator
        OPERATOR_MINUS_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);
        OPERATOR_MINUS_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);
        OPERATOR_MINUS_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_DATE,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);
        OPERATOR_MINUS_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);

        // For Divide Operator
        OPERATOR_DIVIDE_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);
        OPERATOR_DIVIDE_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);

        // For Multiply Operator
        OPERATOR_MULTIPLY_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);
        OPERATOR_MULTIPLY_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);

        // For Power Operator
        OPERATOR_POWER_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);
        OPERATOR_POWER_DATATYPE_RESULT.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);

        operatorsDataTypeResultMap.put("+", OPERATOR_PLUS_DATATYPE_RESULT);
        operatorsDataTypeResultMap.put("-", OPERATOR_MINUS_DATATYPE_RESULT);
        operatorsDataTypeResultMap.put("/", OPERATOR_DIVIDE_DATATYPE_RESULT);
        operatorsDataTypeResultMap.put("*", OPERATOR_MULTIPLY_DATATYPE_RESULT);
        operatorsDataTypeResultMap.put("**", OPERATOR_POWER_DATATYPE_RESULT);

        REL_OPERATORS.put("<", ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);
        REL_OPERATORS.put("<=", ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);
        REL_OPERATORS.put(">", ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);
        REL_OPERATORS.put(">=", ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);
        REL_OPERATORS.put("==", ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);
        REL_OPERATORS.put("!=", ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);

        LOGICAL_OPERATORS.put("||", ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);
        LOGICAL_OPERATORS.put("&&", ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);
        LOGICAL_OPERATORS.put("!=", ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);

        // Put data for compound parameter

        COMPOUND_PARAMETER_SUPPORTED_DATATYPES.put(ParameterDataType.PARAMETER_DATA_TYPE_STRING,
                Arrays.asList(ALPHANUM_SUPPORTED_DATATYPES));

        COMPOUND_PARAMETER_SUPPORTED_DATATYPES.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                Arrays.asList(NUMBER_SUPPORTED_DATATYPES));

        CONDITION_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN);
        CONDITION_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_DATE);
        CONDITION_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE);
        CONDITION_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER);
        CONDITION_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER);
        CONDITION_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_STRING);
        CONDITION_SUPPORTED_DATATYPES.add(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE);

        operatorsSupportedByDataTypeMap_COMP.put(ParameterDataType.PARAMETER_DATA_TYPE_STRING,
                SUPPORTED_OPERATORS_STRING_COMP);
        operatorsSupportedByDataTypeMap_COMP.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                SUPPORTED_OPERATORS_INTEGER_COMP);
        operatorsSupportedByDataTypeMap_COMP.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                SUPPORTED_OPERATORS_NUMBER_COMP);
        operatorsSupportedByDataTypeMap_COMP.put(ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN,
                SUPPORTED_OPERATORS_BOOLEAN_COMP);
        operatorsSupportedByDataTypeMap_COMP.put(ParameterDataType.PARAMETER_DATA_TYPE_DATE, SUPPORTED_OPERATORS_DATE_COMP);
        operatorsSupportedByDataTypeMap_COMP.put(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE, SUPPORTED_OPERATORS_JAVA_UTIL_DATE_COMP);
        operatorsSupportedByDataTypeMap_COMP.put(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE,
                SUPPORTED_OPERATORS_REFERENCE_COMP);

        operatorsSupportedByDataTypeMap_CON
                .put(ParameterDataType.PARAMETER_DATA_TYPE_STRING, SUPPORTED_OPERATORS_STRING_CON);
        operatorsSupportedByDataTypeMap_CON.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                SUPPORTED_OPERATORS_INTEGER_CON);
        operatorsSupportedByDataTypeMap_CON
                .put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER, SUPPORTED_OPERATORS_NUMBER_CON);
        operatorsSupportedByDataTypeMap_CON.put(ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN,
                SUPPORTED_OPERATORS_BOOLEAN_CON);
        operatorsSupportedByDataTypeMap_CON.put(ParameterDataType.PARAMETER_DATA_TYPE_DATE, SUPPORTED_OPERATORS_DATE_CON);
        
        operatorsSupportedByDataTypeMap_CON.put(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE, SUPPORTED_OPERATORS_JAVA_UTIL_DATE_CON);
        
        operatorsSupportedByDataTypeMap_CON.put(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE,
                SUPPORTED_OPERATORS_REFERENCE_CON);

        operatorsSupportedByDataTypeMap_SCRIPT_MVEL.put(ParameterDataType.PARAMETER_DATA_TYPE_STRING,
                SUPPORTED_OPERATORS_STRING_SCRIPT_MVEL);
        operatorsSupportedByDataTypeMap_SCRIPT_MVEL.put(ParameterDataType.PARAMETER_DATA_TYPE_INTEGER,
                SUPPORTED_OPERATORS_INTEGER_SCRIPT_MVEL);
        operatorsSupportedByDataTypeMap_SCRIPT_MVEL.put(ParameterDataType.PARAMETER_DATA_TYPE_NUMBER,
                SUPPORTED_OPERATORS_NUMBER_SCRIPT_MVEL);
        operatorsSupportedByDataTypeMap_SCRIPT_MVEL.put(ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN,
                SUPPORTED_OPERATORS_BOOLEAN_SCRIPT_MVEL);
        operatorsSupportedByDataTypeMap_SCRIPT_MVEL.put(ParameterDataType.PARAMETER_DATA_TYPE_DATE,
                SUPPORTED_OPERATORS_DATE_SCRIPT_MVEL);
        operatorsSupportedByDataTypeMap_SCRIPT_MVEL.put(ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE,
        		SUPPORTED_OPERATORS_JAVA_UTIL_DATE_SCRIPT_MVEL);
        operatorsSupportedByDataTypeMap_SCRIPT_MVEL.put(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE,
                SUPPORTED_OPERATORS_REFERENCE_SCRIPT_MVEL);

    }

}
