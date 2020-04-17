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

import com.nucleus.core.formsConfiguration.FormVO;

public class FormValidationThenAssignmentValueSelectExpStrExecutor {

	public static String execute(FormVO formVo,FormValidationExpression expression) throws Exception{
		if(expression == null || expression.getExpressionType() == null || expression.getExpression() == null){
			throw new Exception("Null Then assignment Action Received");
		}
		AbstractFormValidationThenAssignmentExpressionStrategy strategy = null;
		if(FormValidationConstants.IfOperandExpressionType.CONSTANT_VALUE.toString().equals(expression.getExpressionType())){
			strategy = new FormValidationThenAssignmentConstantExpressionStrategy();
		}else if(FormValidationConstants.IfOperandExpressionType.SIMPLE_EXPRESSION.toString().equals(expression.getExpressionType())){
			strategy = new FormValidationThenAssignmentSimpleExpressionStrategy();
		}else if(FormValidationConstants.IfOperandExpressionType.COMPLEX_EXPRESSION.toString().equals(expression.getExpressionType())){
			strategy = new FormValidationThenAssignmentComplexExpressionStrategy();
		}else{
			throw new Exception("Unknown Then Assignment Strategy Name"+expression.getExpression());
		}
		return strategy.execute(formVo, expression);
	}
}
