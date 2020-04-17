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
import com.nucleus.core.formsConfiguration.FormContainerVO;
import com.nucleus.core.formsConfiguration.FormVO;

public class JavaScriptAssignValueExpressionGeneratorExecutor {

	public static String execute(String targetFieldId,FormVO formVo,String newValueExpression) throws Exception{
		if(targetFieldId ==null || formVo == null){
			throw new Exception("Either targetFieldId or formVo received is null");
		}
		FormContainerVO fieldType = DynamicFormValidationUtility.getFormComponentbyKey(formVo, targetFieldId);
		if(fieldType == null){
			throw new Exception("FormContainerVo can not be extract for field id :"+targetFieldId);
		}
		String fieldIdonUI = targetFieldId+"_"+formVo.getFormName()+"_"+DynamicFormValidationUtility.getClonedRowStatus(fieldType.getComponentDisplayKey());
		AbstractJavaScriptAssignValueExpressionGenerator assignGenerator = FormValidationDataTypeOperatorMap.getThenAssignmentAssignGenerator(fieldType.getFieldType());
		if(assignGenerator == null){
			throw new Exception("Assign expression generator is not registerd for field type :"+fieldType.getFieldType());
		}
		return assignGenerator.assignNewValueExpression(fieldIdonUI, newValueExpression);
	}
}
