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
package com.nucleus.core.formsConfiguration.validationcomponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants.Operators;

public class FormValidationDataTypeOperatorMap {

	// below maps will be used for If only
	private static Map<FormValidationConstants.Operators,Set<String>> componentTypeByOperators = new HashMap<FormValidationConstants.Operators, Set<String>>();
	
	private static Map<String,Set<FormValidationConstants.Operators>> operatorsByComponentType = new HashMap<String, Set<FormValidationConstants.Operators>>();
	
	private static Map<String,Set<FormValidationConstants.Operators>> operatorsByComponentAndFieldType = new HashMap<String, Set<Operators>>();
	
	private static Map<String,AbstractJavaScriptSelectValueExpressionGenerator> ifExpressionGeneratorByOperatorAndFieldType = new HashMap<String, AbstractJavaScriptSelectValueExpressionGenerator>();
	
	// below maps will be used for then only
	
	private static Map<String,AbstractJavaScriptSelectValueExpressionGenerator> thenAssignExpressionSelectGeneratorByOperator = new HashMap<String, AbstractJavaScriptSelectValueExpressionGenerator>();
	
	private static Map<String,AbstractJavaScriptAssignValueExpressionGenerator> thenAssignExpressionAssignGeneratorByOperator = new HashMap<String, AbstractJavaScriptAssignValueExpressionGenerator>(); 
	
	private static Map<String,Set<FormValidationConstants.ThenActionTypes>> thenActionTypesByFieldType = new HashMap<String, Set<FormValidationConstants.ThenActionTypes>>();
	
	private static Map<Tuple_2,AbstractJavascriptStateChangeExpressionGenerator> thenActionStateChangeGeneratorByComponentTypeAndAction = new HashMap<Tuple_2, AbstractJavascriptStateChangeExpressionGenerator>();
	
	//private static Map<>
	
	// to be use for If expression
	public static void registerOperatorWithDataType(String componentType,FormValidationConstants.Operators operator,AbstractJavaScriptSelectValueExpressionGenerator generator){
		Set<String> componentTypes = componentTypeByOperators.get(operator);
		if(componentTypes == null){
			componentTypes = new HashSet<String>();
			componentTypeByOperators.put(operator, componentTypes);
		}
		componentTypes.add(componentType);
		
		Set<Operators> operators = operatorsByComponentType.get(componentType);
		if(operators == null){
			operators = new HashSet<FormValidationConstants.Operators>();
			operatorsByComponentType.put(componentType, operators);
		}
		operators.add(operator);
		ifExpressionGeneratorByOperatorAndFieldType.put(componentType+"_"+operator.toString(), generator);
	}
	
	public static void registerOperatorWithDataType(String componentType,Integer fieldType, FormValidationConstants.Operators operator,AbstractJavaScriptSelectValueExpressionGenerator generator){
		Set<Operators> operators = operatorsByComponentAndFieldType.get(componentType.toString()+"_"+fieldType.toString());
		if(operators == null){
			operators = new HashSet<FormValidationConstants.Operators>();
			operatorsByComponentAndFieldType.put(componentType.toString()+"_"+fieldType.toString(), operators);
		}
		operators.add(operator);
		ifExpressionGeneratorByOperatorAndFieldType.put(componentType+"_"+operator.toString(), generator);
	}
	
	public static Set<Operators> getOperatorsByDataType(String type){
		return operatorsByComponentType.get(type);
	}
	
	public static Set<String> getComponentTypeByOperatorsForIf(Operators ope){
		return componentTypeByOperators.get(ope);
	}
	
	public static Set<Operators> getOperatorsByComponenrTypeAndFieldType(String type,Integer fieldType){
		return operatorsByComponentAndFieldType.get(type.toString()+"_"+fieldType.toString());
	}
	
	public static AbstractJavaScriptSelectValueExpressionGenerator getGenerator(String componentType,FormValidationConstants.Operators operator){
		return ifExpressionGeneratorByOperatorAndFieldType.get(componentType+"_"+operator.toString());
	}
	
	
	// to be use for then action
	public static void registerThenAssignmentSelectGeneratorByFieldType(String fieldType,AbstractJavaScriptSelectValueExpressionGenerator generator){
		thenAssignExpressionSelectGeneratorByOperator.put(fieldType, generator);
	}
	
	public static AbstractJavaScriptSelectValueExpressionGenerator getThenAssignmentSelectGenerator(String fieldType){
		return thenAssignExpressionSelectGeneratorByOperator.get(fieldType);
	}
	
	
	public static void registerThenAssignmentAssignGeneratorByFieldType(String fieldType,FormValidationConstants.ThenActionTypes actionType, AbstractJavaScriptAssignValueExpressionGenerator generator){
		thenAssignExpressionAssignGeneratorByOperator.put(fieldType, generator);
		Set<FormValidationConstants.ThenActionTypes> types = thenActionTypesByFieldType.get(fieldType);
		if(types == null){
			types = new HashSet<>();
			thenActionTypesByFieldType.put(fieldType, types);
		}
		types.add(actionType);
	}
	
	public static AbstractJavaScriptAssignValueExpressionGenerator getThenAssignmentAssignGenerator(String fieldType){
		return thenAssignExpressionAssignGeneratorByOperator.get(fieldType);
	}

	public static AbstractJavascriptStateChangeExpressionGenerator getThenActionStateChangeGeneratorByComponentTypeAndAction(Tuple_2 key) {
		return thenActionStateChangeGeneratorByComponentTypeAndAction.get(key);
	}

	public static void registerThenActionStateChangeGeneratorByComponentTypeAndAction(
			Tuple_2 key, AbstractJavascriptStateChangeExpressionGenerator generator,FormValidationConstants.ThenActionTypes actionType) {
		thenActionStateChangeGeneratorByComponentTypeAndAction.put(key, generator);
		Set<FormValidationConstants.ThenActionTypes> types = thenActionTypesByFieldType.get(key.get_1());
		if(types == null){
			types = new HashSet<>();
			thenActionTypesByFieldType.put(key.get_1(), types);
		}
		types.add(actionType);
	}
	
	// to get list of operator support value read operation
	public List<String> getFieldTypeForSelect(){
		return new ArrayList<String>(thenAssignExpressionSelectGeneratorByOperator.keySet());
	}
	
	public List<String> getFieldTypeForAssign(){
		return new ArrayList<String>(thenAssignExpressionAssignGeneratorByOperator.keySet());
	}
	
	public static Set<FormValidationConstants.ThenActionTypes> getThenActionByFieldType(String fieldType){
		return thenActionTypesByFieldType.get(fieldType);
	}
}
