package com.nucleus.rules.service;

import java.io.Serializable;
import java.util.Map;

public interface ScriptRuleEvaluator extends Serializable{
	public static final String CLASS_DECLARATION = "public class GrooovyScriptRule";
	public static final String IMPLEMENTS_STRING = " implements com.nucleus.rules.service.ScriptRuleEvaluator ";
    public static final String METHOD_DEFINITION_STRING = "public Boolean evaluateRule(Map contextMap){";
    public static final String METHOD_ARGUMENT_NAME = "contextMap";
    public static final Object OPENING_CURLY_BRACE = "{";
    public static final String METHOD_CLOSING_STRING = "}";
    public static final String DOUBLE_CLOSING_CURLY_BRACES = "}}";
    
    public Boolean evaluateRule(Map contextMap);
}
