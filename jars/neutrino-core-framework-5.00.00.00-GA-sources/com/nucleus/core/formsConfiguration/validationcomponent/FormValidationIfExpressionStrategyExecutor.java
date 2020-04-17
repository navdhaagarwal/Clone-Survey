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
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants.Operators;

public class FormValidationIfExpressionStrategyExecutor {

	public static String execute(FormVO formVo,FormValidationExpression expression,Operators operator) throws Exception{
		
		String expressionType = expression.getExpressionType();
		AbstractFormValidationIfExpressionStrategy strategy = null;
		if(expressionType == null){
			throw new Exception("Null expression received");
		}
		else if(expressionType.equals(FormValidationConstants.IfOperandExpressionType.CONSTANT_VALUE.getCode())){
			strategy = new FormValidationConstantExpressionStrategy();
		}else if(expressionType.equals(FormValidationConstants.IfOperandExpressionType.SIMPLE_EXPRESSION.getCode())){
			strategy = new FormValidationSimpleExpressionStrategy();
		}else if(expressionType.equals(FormValidationConstants.IfOperandExpressionType.COMPLEX_EXPRESSION.getCode())){
			throw new Exception("Complex expression is not supported yet");
		}
		if(strategy != null) {
			return strategy.execute(formVo, expression, operator);
		}
		else{
			throw new Exception("No strategy found for expression type: "+ expressionType);
		}
	}
}
