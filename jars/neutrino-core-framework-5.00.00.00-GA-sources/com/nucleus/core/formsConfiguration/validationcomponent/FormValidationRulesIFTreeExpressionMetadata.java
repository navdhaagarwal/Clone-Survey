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

public class FormValidationRulesIFTreeExpressionMetadata implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// it can be operator or operand any
	private FormValidationExpression node;
	
	private FormValidationRulesIFTreeExpressionMetadata leftExp;
	
	private FormValidationRulesIFTreeExpressionMetadata rightExp;

	

	public FormValidationExpression getNode() {
		return node;
	}

	public void setNode(FormValidationExpression node) {
		this.node = node;
	}

	public FormValidationRulesIFTreeExpressionMetadata getLeftExp() {
		return leftExp;
	}

	public void setLeftExp(FormValidationRulesIFTreeExpressionMetadata leftExp) {
		this.leftExp = leftExp;
	}

	public FormValidationRulesIFTreeExpressionMetadata getRightExp() {
		return rightExp;
	}

	public void setRightExp(FormValidationRulesIFTreeExpressionMetadata rightExp) {
		this.rightExp = rightExp;
	}

	public FormValidationRulesIFTreeExpressionMetadata(FormValidationExpression node,
			FormValidationRulesIFTreeExpressionMetadata leftExp, FormValidationRulesIFTreeExpressionMetadata rightExp) {
		super();
		this.node = node;
		this.leftExp = leftExp;
		this.rightExp = rightExp;
	}

	public FormValidationRulesIFTreeExpressionMetadata() {
		super();
	}
	
}
