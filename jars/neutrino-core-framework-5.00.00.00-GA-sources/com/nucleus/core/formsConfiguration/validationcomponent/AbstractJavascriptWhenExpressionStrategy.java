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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nucleus.core.dynamicform.service.DynamicFormValidationUtility;
import com.nucleus.core.formsConfiguration.FormComponentType;
import com.nucleus.core.formsConfiguration.FormContainerVO;
import com.nucleus.core.formsConfiguration.FormVO;

/**
 * 
 * @author nitin.singh
 *
 */
public abstract class AbstractJavascriptWhenExpressionStrategy {

	public abstract String createExpression(FormVO formVo, List<FormValidationRulesIFMetadataVO> ifConditions,String javascriptFunctionName) throws Exception;
	
	public String getFieldIdofUI(FormVO formVo,String fieldKey) throws Exception{
		FormContainerVO componentVo = DynamicFormValidationUtility.getFormComponentbyKey(formVo,fieldKey);
		if(componentVo == null){
			throw new Exception("Unknwon Field Key : "+fieldKey);
		}
		// clone row status can be retrived from component display key, it is stored in form component[0][1]
		// we have to read 0 from that
		String cloneRowStatus = DynamicFormValidationUtility.getClonedRowStatus(componentVo.getComponentDisplayKey());
		if(componentVo.getFieldType().equals(FormComponentType.CASCADED_SELECT)|| componentVo.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
			return fieldKey+"_"+formVo.getFormName();
		}
		else {
			return fieldKey+"_"+formVo.getFormName()+"_"+cloneRowStatus;
		}

	}

}

class WhenExpressionGeneratorForOnLoadStrategy extends AbstractJavascriptWhenExpressionStrategy {

	@Override
	public String createExpression(FormVO formVo,List<FormValidationRulesIFMetadataVO> ifConditions,
			String javascriptFunctionName) throws Exception {
		return "$(document)" + ".ready(function() {" + javascriptFunctionName + "});";
	}

}

class WhenExpressionGeneratorForOnFieldValueChangeStrategy extends AbstractJavascriptWhenExpressionStrategy {

	@Override
	public String createExpression(FormVO formVo,List<FormValidationRulesIFMetadataVO> ifConditions, 
			String javascriptFunctionName) throws Exception {
		if (ifConditions == null || ifConditions.isEmpty()) {
			throw new Exception("No if conditions for When expression generator");
		}
		StringBuilder result = new StringBuilder();
		result.append("$(document).ready(function() {").append("\n");
		Set<String> fieldsAdded = new HashSet<String>();
		for (FormValidationRulesIFMetadataVO formValidationRulesIFMetadataVO : ifConditions) {
			String expressiontype = formValidationRulesIFMetadataVO.getLeftOperandFieldKey().getExpressionType();
			if (expressiontype.equals(FormValidationConstants.IfOperandExpressionType.SIMPLE_EXPRESSION.toString())) {
				String generatedFieldId = getFieldIdofUI(formVo,formValidationRulesIFMetadataVO.getLeftOperandFieldKey().getExpression());
				if(fieldsAdded.add(generatedFieldId)) {
					result.append("$('[id^=" + generatedFieldId
							+"]').change( function() {" + javascriptFunctionName + "});").append("\n");
				}
			} else if (expressiontype.equals(FormValidationConstants.IfOperandExpressionType.COMPLEX_EXPRESSION)) {
				throw new Exception("Complex Expression in left hand of if is not supported");
			}

			String expressionTYpeForRightHand = formValidationRulesIFMetadataVO.getRightOperandFieldKey().getExpressionType();

			if (expressionTYpeForRightHand.equals(FormValidationConstants.IfOperandExpressionType.SIMPLE_EXPRESSION.toString())) {
				String generatedFieldId = getFieldIdofUI(formVo,formValidationRulesIFMetadataVO.getRightOperandFieldKey().getExpression());
				if(fieldsAdded.add(generatedFieldId)) {
					result.append("$('[id^=" + generatedFieldId
							+"]').change( function(){" + javascriptFunctionName + "});").append("\n");
				}
			} else if (expressiontype.equals(FormValidationConstants.IfOperandExpressionType.COMPLEX_EXPRESSION)) {
				throw new Exception("Complex Expression in left hand of if is not supported");
			}
		}
		result.append("});");
		return result.toString();
	}

}



class WhenExpressionGeneratorForOnSaveButtonClickStrategy extends AbstractJavascriptWhenExpressionStrategy {

	@Override
	public String createExpression(FormVO formVo,List<FormValidationRulesIFMetadataVO> ifConditions, 
			String javascriptFunctionName) throws Exception {
		
		return "$(document).ready(function() {"
				+	"$('#dynSave').click( function() {"+javascriptFunctionName+"})"
				+"});";
	}
	
}

class WhenExpressionGeneratorForOnPanelSaveButtonClickStrategy extends AbstractJavascriptWhenExpressionStrategy{

	private String panelId;
	public WhenExpressionGeneratorForOnPanelSaveButtonClickStrategy(String panelId){
		this.panelId = panelId;
	}

	@Override
	public String createExpression(FormVO formVo,List<FormValidationRulesIFMetadataVO> ifConditions,
								   String javascriptFunctionName) throws Exception {

		return "$(document).ready(function() {"
				+	"$('#" + this.panelId +  "saveButton').click( function() {"+javascriptFunctionName+"})"
				+"});";
	}


}

