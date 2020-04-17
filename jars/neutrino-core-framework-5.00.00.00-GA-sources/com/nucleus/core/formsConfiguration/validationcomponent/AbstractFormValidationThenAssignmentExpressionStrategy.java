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

import com.nucleus.core.dynamicform.service.DynamicFormValidationUtility;
import com.nucleus.core.formsConfiguration.FormComponentType;
import com.nucleus.core.formsConfiguration.FormContainerVO;
import com.nucleus.core.formsConfiguration.FormVO;

public abstract class AbstractFormValidationThenAssignmentExpressionStrategy {

	public abstract String execute(FormVO formVo,FormValidationExpression expression) throws Exception;
	
}

class FormValidationThenAssignmentConstantExpressionStrategy extends AbstractFormValidationThenAssignmentExpressionStrategy{

	@Override
	public String execute(FormVO formVo, FormValidationExpression expression) throws Exception {
		return "'"+expression.getExpression()+"'";
	}
	
}

class FormValidationThenAssignmentSimpleExpressionStrategy extends AbstractFormValidationThenAssignmentExpressionStrategy{

	@Override
	public String execute(FormVO formVo, FormValidationExpression expression) throws Exception{
		String fieldId = expression.getExpression();
		FormContainerVO fieldType = DynamicFormValidationUtility.getFormComponentbyKey(formVo, fieldId);
		if(fieldType == null || fieldType.getFieldType() == null){
			throw new Exception("either no FormContainer found for :"+expression.getExpression()+" or Field type is not set for FormcontainerVO");
		}
		AbstractJavaScriptSelectValueExpressionGenerator selectGenerator = FormValidationDataTypeOperatorMap.getThenAssignmentSelectGenerator(fieldType.getFieldType());
		if(selectGenerator == null){
			throw new Exception("No select expression generator found for field type :"+fieldType.getFieldType());
		}
		String fieldIdOnUi;
		if(fieldType.getFieldType().equals(FormComponentType.CASCADED_SELECT)|| fieldType.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
			fieldIdOnUi = fieldId + "_" + formVo.getFormName();
		}
		else {
			fieldIdOnUi = fieldId + "_" + formVo.getFormName() + "_" + DynamicFormValidationUtility.getClonedRowStatus(fieldType.getComponentDisplayKey());
		}
		return selectGenerator.generateExpression(fieldIdOnUi);
	}
	
}

class FormValidationThenAssignmentComplexExpressionStrategy extends AbstractFormValidationThenAssignmentExpressionStrategy{

	@Override
	public String execute(FormVO formVo, FormValidationExpression expression) throws Exception {
		throw new Exception("Complex Type expression is still not supported");
	}
	
}