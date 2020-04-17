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


public class FormValidationRulesIFMetadataVO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// it will be key of field only
	private FormValidationExpression leftOperandFieldKey;
	
	// operator like equal/ not equal/ has to map with enum
	private String operator;
	
	// it will be key of field or any constant value
	private FormValidationExpression rightOperandFieldKey;
	
	// it can be AND or OR only
	private String joinWithPreviousRule;

	

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getJoinWithPreviousRule() {
		return joinWithPreviousRule;
	}

	public void setJoinWithPreviousRule(String joinWithPreviousRule) {
		this.joinWithPreviousRule = joinWithPreviousRule;
	}

	public FormValidationExpression getLeftOperandFieldKey() {
		return leftOperandFieldKey;
	}

	public void setLeftOperandFieldKey(FormValidationExpression leftOperandFieldKey) {
		this.leftOperandFieldKey = leftOperandFieldKey;
	}

	public FormValidationExpression getRightOperandFieldKey() {
		return rightOperandFieldKey;
	}

	public void setRightOperandFieldKey(FormValidationExpression rightOperandFieldKey) {
		this.rightOperandFieldKey = rightOperandFieldKey;
	}

	
}
