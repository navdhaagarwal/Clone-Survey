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

import java.io.Serializable;

public class FormValidationExpression implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// it can be simple or complex .. in current scope it only simple .. map with enum IfOperandExpressionType
	private String expressionType;
	
	/**
	 * in case of CONSTANT it will hold constant value
	 * in case  expressionType is SIMPLE it will be fieldKey only
	 * in case of COMPLEX it will be combination of fieldkey and operators 
	 */
	private String expression;

	//for if condition,value mapped to field type
	private String expressionFieldType;

	public String getExpressionType() {
		return expressionType;
	}

	public void setExpressionType(String expressionType) {
		this.expressionType = expressionType;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getExpressionFieldType() {
		return expressionFieldType;
	}

	public void setExpressionFieldType(String expressionFieldType) {
		this.expressionFieldType = expressionFieldType;
	}

	public FormValidationExpression(String expressionType, String expression) {
		super();
		this.expressionType = expressionType;
		this.expression = expression;
	}
	
	public FormValidationExpression() {
		super();
	}
	
	
}
