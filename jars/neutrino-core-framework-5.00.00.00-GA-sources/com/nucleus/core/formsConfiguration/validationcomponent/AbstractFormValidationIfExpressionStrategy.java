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
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants.Operators;

public abstract class AbstractFormValidationIfExpressionStrategy {

	public abstract String execute(FormVO formVo,FormValidationExpression expression,Operators operator) throws Exception;

	
}

class FormValidationSimpleExpressionStrategy extends AbstractFormValidationIfExpressionStrategy{

	@Override
	public String execute(FormVO formVo, FormValidationExpression expression,Operators operator) throws Exception{
		String fieldKey = expression.getExpression();
		FormContainerVO componentVo = DynamicFormValidationUtility.getFormComponentbyKey(formVo,fieldKey);
		if(componentVo == null){
			throw new Exception("Unknwon Field Key : "+fieldKey);
		}
		// clone row status can be retrived from component display key, it is stored in form component[0][1]
		// we have to read 0 from that
		String cloneRowStatus = DynamicFormValidationUtility.getClonedRowStatus(componentVo.getComponentDisplayKey());
		String fieldIdOnUI;
		if(componentVo != null && (componentVo.getFieldType().equals(FormComponentType.CASCADED_SELECT)|| componentVo.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT))){
			fieldIdOnUI = fieldKey+"_"+formVo.getFormName();
		}
		else {
			fieldIdOnUI = fieldKey+"_"+formVo.getFormName()+"_"+cloneRowStatus;
		}
		AbstractJavaScriptSelectValueExpressionGenerator generator = FormValidationDataTypeOperatorMap.getGenerator(componentVo.getFieldType(), operator);
		if(generator == null){
			throw new Exception("No generator found for Field type :"+componentVo.getFieldType()+" and operator :"+operator);
		}


		return generator.generateExpression(fieldIdOnUI);
	}
	
}

class FormValidationConstantExpressionStrategy extends AbstractFormValidationIfExpressionStrategy{

	@Override
	public String execute(FormVO formVo, FormValidationExpression expression, Operators operator) throws Exception {
		String constantValue = expression.getExpression();
		constantValue = constantValue.replace("'", "''");
		return "'"+constantValue+"'";
	}
	
}


