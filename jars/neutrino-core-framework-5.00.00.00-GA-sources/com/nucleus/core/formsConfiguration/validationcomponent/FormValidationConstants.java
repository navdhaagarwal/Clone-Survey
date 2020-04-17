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

import com.nucleus.core.formsConfiguration.FieldDataType;
import com.nucleus.core.formsConfiguration.FormComponentType;

public class FormValidationConstants {

	protected static final String STYLE = "style";
	protected static final String DISPLAY_NONE = "display : none";
	protected static final String REQUIRED_CLASS = "required";
	protected static final String CLASS = "class";
	
	protected static final String ADD = "ADD";
	protected static final String REMOVE = "REMOVE";
	

	public static enum Operators{
		EQUAL("==","EQUAL TO",new EqualOperatorCombiner()),
		NOT_EQUAL("!=","NOT EQUAL TO",new NotEqualOperatorCombiner()),
		START_WITH("^=","START WITH",new StartsWithOperatorCombiner()),
		CONTAINS("Like","CONTAINS",new ContainsOperatorCombiner()),
		GREATER_THAN(">","GREATER THAN",new GreaterThenOperatorCombiner()),
		LESS_THAN("<","LESS THAN",new LessThenOperatorCombiner()),
		IS_SELECTED("Selected","IS SELECTED",new IsSelectedOperatorCombiner()),
		IS_CHECKED("check","IS CHECKED",new IsSelectedOperatorCombiner()),
		IS_UNCHECKED("uncheck","IS UNCHECKED",new IsNotSelectedOperatorCombiner()),
		AND("&&","AND",new AndOperatorCombiner()),
		OR("||","OR",new OrOperatorCombiner());
		
		private String operator_exp;
		private String operator_displayName;
		private IJavaScriptIfExpressionCombiner combiner;
		
		private Operators(String operator_exp, String operator_displayName, IJavaScriptIfExpressionCombiner combiner) {
			this.operator_exp = operator_exp;
			this.operator_displayName = operator_displayName;
			this.combiner = combiner;
		}

		public String getOperator_exp() {
			return operator_exp;
		}

		public String getOperator_displayName() {
			return operator_displayName;
		}

		public IJavaScriptIfExpressionCombiner getCombiner() {
			return combiner;
		}

		public static Operators getOperatorByOperatorExpression(String expression){
			for (Operators operator : Operators.values()) {
				if(operator.getOperator_exp().equals(expression)){
					return operator;
				}
			}
			return null;
		}
		
		public static IJavaScriptIfExpressionCombiner getCobminerByExpression(String expression){
			for (Operators operator : Operators.values()) {
				if(operator.getOperator_exp().equals(expression)){
					return operator.getCombiner();
				}
			}
			return null;
		}
	}
	
	public static enum StateChangeOperations{
		SHOW("SHOW",STYLE,DISPLAY_NONE),
		HIDE("HIDE",STYLE,DISPLAY_NONE),
		REQUIRED("REQUIRED",CLASS,REQUIRED_CLASS),
		NOT_REQUIRED("NOT REQUIRED",CLASS,REQUIRED_CLASS);
		
		private String operation;
		private String cssPropertyName;
		private String cssPropertyValue;
		
		private StateChangeOperations(String operation, String cssClassName,String cssPropertyValue) {
			this.operation = operation;
			this.cssPropertyName = cssClassName;
			this.cssPropertyValue = cssPropertyValue;
		}
		public String getOperation() {
			return operation;
		}
		public String getCssPropertyName() {
			return cssPropertyName;
		}
		public String getCssPropertyValue() {
			return cssPropertyValue;
		}
		
	}
	
	public static enum IfOperandExpressionType{
		OPERATOR("OPERATOR"),
		CONSTANT_VALUE("CONSTANT_VALUE"),
		SIMPLE_EXPRESSION("SIMPLE_EXPRESSION"),
		COMPLEX_EXPRESSION("COMPLEX_EXPRESSION");
		
		private String code;

		private IfOperandExpressionType(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
	
	public enum ThenActionTypes{
		SHOW_MESSAGE("SHOW_MESSAGE"),
		ASSIGN_VALUE("ASSIGN_VALUE"),
		CHANGE_STATE("CHANGE_STATE");
		
		private String code;

		private ThenActionTypes(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
	
	public enum WhenActionTypes{

		IF_VALUE_CHANGE("VALUE CHANGE OF IF FIELDS"),
		SAVE_BUTTON_CLICK("SAVE BUTTON CLICK"),
		ON_PAGE_LOAD("ON PAGE LOAD"),
		PANEL_SAVE_CLICK("PANEL SAVE CLICK");
		
		private String displayValue;

		private String panelIdForPartialSave;

		private WhenActionTypes(String displayValue) {
			this.displayValue = displayValue;
		}

		public String getDisplayValue() {
			return displayValue;
		}
		
		public static WhenActionTypes getEnumByValue(String value){
			for (WhenActionTypes when : WhenActionTypes.values()) {
				if(value.equals(when.getDisplayValue())){
					return when;
				}
			}
			return null;
		}

		public String getPanelIdForPartialSave() {
			return panelIdForPartialSave;
		}

		public void setPanelIdForPartialSave(String panelIdForPartialSave) {
			this.panelIdForPartialSave = panelIdForPartialSave;
		}
	}
	
	
	public static void initMappings(){
		if(initMappingDone){
			return;
		}
		// auto complete
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.AUTOCOMPLETE, Operators.EQUAL,new SelectExpressionGeneratorForAutoCompleteById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.AUTOCOMPLETE, Operators.NOT_EQUAL,new SelectExpressionGeneratorForAutoCompleteById());
		// check box
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.CHECKBOX, Operators.IS_CHECKED,new IsSelectedExpressionById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.CHECKBOX, Operators.IS_UNCHECKED, new IsNotselectedExpressionById());
		// date
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.DATE, Operators.EQUAL,new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.DATE, Operators.NOT_EQUAL,new SelectExpressionGeneratorById());
	//	FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.DATE, Operators.GREATER_THEN,new selectExpressionGeneratorById());
	//	FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.DATE, Operators.LESS_THEN,new selectExpressionGeneratorById());
		//drop down
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.DROP_DOWN, Operators.EQUAL, new SelectExpressionGeneratorForDropDownById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.DROP_DOWN, Operators.NOT_EQUAL, new SelectExpressionGeneratorForDropDownById());
		// email
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.EMAIL, Operators.EQUAL, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.EMAIL, Operators.NOT_EQUAL, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.EMAIL, Operators.CONTAINS, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.EMAIL, Operators.START_WITH, new SelectExpressionGeneratorById());
		// money
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.MONEY, Operators.EQUAL, new SelectExpressionGeneratorForMoneyById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.MONEY, Operators.NOT_EQUAL, new SelectExpressionGeneratorForMoneyById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.MONEY, Operators.GREATER_THAN, new SelectExpressionGeneratorForMoneyById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.MONEY, Operators.LESS_THAN, new SelectExpressionGeneratorForMoneyById());
		// multi select
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.MULTISELECTBOX, Operators.EQUAL, new SelectExpressionGeneratorById());
		//phone number
	/*	FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.PHONE, Operators.EQUAL, new selectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.PHONE, Operators.NOT_EQUAL, new selectExpressionGeneratorById());*/
		//radio -> not possible as of now
		//FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.RADIO, Operators.IS_SELECTED, new IsSelectedExpressionById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.RADIO, Operators.EQUAL, new SelectExpressionGeneratorForRadioById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.RADIO, Operators.EQUAL, new SelectExpressionGeneratorForRadioById());
		// text area
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_AREA, Operators.EQUAL, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_AREA, Operators.NOT_EQUAL, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_AREA, Operators.CONTAINS, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_AREA, Operators.START_WITH, new SelectExpressionGeneratorById());
		// text box with integer type
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_INTEGER, Operators.EQUAL, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_INTEGER, Operators.NOT_EQUAL, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_INTEGER, Operators.LESS_THAN, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_INTEGER, Operators.GREATER_THAN, new SelectExpressionGeneratorById());
		// text box with number type
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_NUMBER, Operators.EQUAL, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_NUMBER, Operators.NOT_EQUAL, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_NUMBER, Operators.LESS_THAN, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_NUMBER, Operators.GREATER_THAN, new SelectExpressionGeneratorById());
		// text box with text type
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_TEXT, Operators.EQUAL, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_TEXT, Operators.NOT_EQUAL, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_TEXT, Operators.CONTAINS, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.TEXT_BOX,FieldDataType.DATA_TYPE_TEXT, Operators.START_WITH, new SelectExpressionGeneratorById());
		//for cascade select
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.CASCADED_SELECT, Operators.EQUAL, new SelectExpressionGeneratorForCascadeSelectById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.CASCADED_SELECT, Operators.NOT_EQUAL, new SelectExpressionGeneratorForCascadeSelectById());
		
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.CUSTOM_CASCADED_SELECT, Operators.EQUAL, new SelectExpressionGeneratorForCascadeSelectById());
		FormValidationDataTypeOperatorMap.registerOperatorWithDataType(FormComponentType.CUSTOM_CASCADED_SELECT, Operators.NOT_EQUAL, new SelectExpressionGeneratorForCascadeSelectById());
		
		// then value selector register
		FormValidationDataTypeOperatorMap.registerThenAssignmentAssignGeneratorByFieldType(FormComponentType.TEXT_AREA,FormValidationConstants.ThenActionTypes.ASSIGN_VALUE, new JavaScriptAssignValueForTextBoxExpressionGenerator());
		FormValidationDataTypeOperatorMap.registerThenAssignmentAssignGeneratorByFieldType(FormComponentType.TEXT_BOX,FormValidationConstants.ThenActionTypes.ASSIGN_VALUE, new JavaScriptAssignValueForTextBoxExpressionGenerator());
		//FormValidationDataTypeOperatorMap.registerThenAssignmentAssignGeneratorByFieldType(FormComponentType.PHONE, new JavaScriptAssignValueForTextBoxExpressionGenerator());
		FormValidationDataTypeOperatorMap.registerThenAssignmentAssignGeneratorByFieldType(FormComponentType.EMAIL,FormValidationConstants.ThenActionTypes.ASSIGN_VALUE, new JavaScriptAssignValueForTextBoxExpressionGenerator());
		
		
		// 
		FormValidationDataTypeOperatorMap.registerThenAssignmentSelectGeneratorByFieldType(FormComponentType.TEXT_AREA, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerThenAssignmentSelectGeneratorByFieldType(FormComponentType.TEXT_BOX, new SelectExpressionGeneratorById());
		FormValidationDataTypeOperatorMap.registerThenAssignmentSelectGeneratorByFieldType(FormComponentType.AUTOCOMPLETE, new SelectExpressionGeneratorById());
		
		
		// register state change by field type
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.TEXT_BOX, FormValidationConstants.StateChangeOperations.SHOW.toString()),
				new TextBoxShowStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.TEXT_BOX, FormValidationConstants.StateChangeOperations.HIDE.toString()),
				new TextBoxHideStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.TEXT_AREA, FormValidationConstants.StateChangeOperations.SHOW.toString()),
				new TextBoxShowStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.TEXT_AREA, FormValidationConstants.StateChangeOperations.HIDE.toString()),
				new TextBoxHideStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.DATE, FormValidationConstants.StateChangeOperations.SHOW.toString()),
				new DateShowStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.DATE, FormValidationConstants.StateChangeOperations.HIDE.toString()),
				new DateHideStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.EMAIL, FormValidationConstants.StateChangeOperations.SHOW.toString()),
				new TextBoxShowStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.EMAIL, FormValidationConstants.StateChangeOperations.HIDE.toString()),
				new TextBoxHideStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.DROP_DOWN, FormValidationConstants.StateChangeOperations.SHOW.toString()),
				new DropDownShowStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.DROP_DOWN, FormValidationConstants.StateChangeOperations.HIDE.toString()),
				new DropdownHideStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.CASCADED_SELECT, FormValidationConstants.StateChangeOperations.SHOW.toString()),
				new DropDownShowStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.CASCADED_SELECT, FormValidationConstants.StateChangeOperations.HIDE.toString()),
				new DropdownHideStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.CUSTOM_CASCADED_SELECT, FormValidationConstants.StateChangeOperations.SHOW.toString()),
				new DropDownShowStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.CUSTOM_CASCADED_SELECT, FormValidationConstants.StateChangeOperations.HIDE.toString()),
				new DropdownHideStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);

		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.PANEL, FormValidationConstants.StateChangeOperations.SHOW.toString()),
				new PanelShowStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);
		FormValidationDataTypeOperatorMap.registerThenActionStateChangeGeneratorByComponentTypeAndAction(
				new Tuple_2(FormComponentType.PANEL, FormValidationConstants.StateChangeOperations.HIDE.toString()),
				new PanelHideStateChangeEpressionGenerator(),FormValidationConstants.ThenActionTypes.CHANGE_STATE);

		initMappingDone = Boolean.TRUE;
	}
	
	private static Boolean initMappingDone = Boolean.FALSE;
	
}


