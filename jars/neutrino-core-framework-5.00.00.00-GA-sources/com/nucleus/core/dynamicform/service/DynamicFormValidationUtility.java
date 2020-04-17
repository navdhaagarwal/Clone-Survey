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

package com.nucleus.core.dynamicform.service;

import java.util.ArrayList;
import java.util.List;

import com.nucleus.core.formsConfiguration.FormContainerVO;
import com.nucleus.core.formsConfiguration.FormVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationExpression;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesIFMetadataVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesIFTreeExpressionMetadata;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesThenMetadataVO;
import com.nucleus.logging.BaseLoggers;

public class DynamicFormValidationUtility {
	
	public static FormValidationRulesIFTreeExpressionMetadata convertIntoTreeExpression(List<FormValidationRulesIFMetadataVO> ifs){
		if(ifs == null || ifs.isEmpty()){
			BaseLoggers.flowLogger.info("Input Ifs are null");
			return null;
		}
		FormValidationRulesIFTreeExpressionMetadata presentRootNode = null;
		for (FormValidationRulesIFMetadataVO formValidationRulesIFMetadataVO : ifs) {
			FormValidationExpression operatorNode = null;
			if(formValidationRulesIFMetadataVO.getJoinWithPreviousRule() !=null){
				operatorNode = createOperatorNode(formValidationRulesIFMetadataVO.getJoinWithPreviousRule());
				FormValidationRulesIFTreeExpressionMetadata newRootNode = new FormValidationRulesIFTreeExpressionMetadata();
				newRootNode.setLeftExp(presentRootNode);
				newRootNode.setRightExp(convertIntoTreeExpression(formValidationRulesIFMetadataVO));
				newRootNode.setNode(operatorNode);
				presentRootNode = newRootNode;
			}else{
				presentRootNode = convertIntoTreeExpression(formValidationRulesIFMetadataVO);
			}
		}
		return presentRootNode;
	}
	
	
	public static FormValidationRulesIFTreeExpressionMetadata convertIntoTreeExpression(FormValidationRulesIFMetadataVO singleIf){
		if(singleIf == null){
			BaseLoggers.flowLogger.info("Input Ifs are null");
			return null;
		}
		FormValidationRulesIFTreeExpressionMetadata leftNode = new FormValidationRulesIFTreeExpressionMetadata();
		leftNode.setNode(singleIf.getLeftOperandFieldKey());
		
		FormValidationRulesIFTreeExpressionMetadata rightNode = new FormValidationRulesIFTreeExpressionMetadata();
		rightNode.setNode(singleIf.getRightOperandFieldKey());
		
		FormValidationExpression operatorNode = createOperatorNode(singleIf.getOperator());
		
		return new FormValidationRulesIFTreeExpressionMetadata(operatorNode, leftNode, rightNode);
	}


	private static FormValidationExpression createOperatorNode(String operator) {
		FormValidationExpression operatorNode = new FormValidationExpression();
		operatorNode.setExpression(operator);
		operatorNode.setExpressionType(FormValidationConstants.IfOperandExpressionType.OPERATOR.toString());
		return operatorNode;
	}
	
	public static FormContainerVO getFormComponentbyKey(FormVO formVo ,String fieldKey){
		if(fieldKey == null || formVo == null){
			return null;
		}
		
		for (FormContainerVO container : formVo.getContainerVOList()) {
			if(container.getFormContainerVOList()!=null){
				for (FormContainerVO innercontainer : container.getFormContainerVOList()) {
						if(fieldKey.equals(innercontainer.getFieldKey())){
							return innercontainer;
						}
					}
			}
			if(fieldKey.equals(container.getFieldKey())){
				return container;
			}
		} 
		return null;
	}
	
	public static String getClonedRowStatus(String componentDisplayKey){
		if(componentDisplayKey.contains("[") && componentDisplayKey.contains("]")){
			return componentDisplayKey.substring(componentDisplayKey.indexOf("[")+1,
					componentDisplayKey.indexOf("]"));
		}
		return null;
	}
	
	public static List<FormValidationRulesIFMetadataVO> greateDummyIfs(){
		List<FormValidationRulesIFMetadataVO> ifs = new ArrayList<FormValidationRulesIFMetadataVO>();
		FormValidationRulesIFMetadataVO firstIf = new FormValidationRulesIFMetadataVO();
		
		FormValidationExpression firstifLeft = new FormValidationExpression();
		firstifLeft.setExpression("textbox");
		firstifLeft.setExpressionType(FormValidationConstants.IfOperandExpressionType.SIMPLE_EXPRESSION.toString());
		
		FormValidationExpression firstifRight = new FormValidationExpression();
		firstifRight.setExpression("nitin");
		firstifRight.setExpressionType(FormValidationConstants.IfOperandExpressionType.CONSTANT_VALUE.toString());
		
		firstIf.setLeftOperandFieldKey(firstifLeft);
		firstIf.setRightOperandFieldKey(firstifRight);
		firstIf.setOperator(FormValidationConstants.Operators.EQUAL.getOperator_displayName());
		
        FormValidationRulesIFMetadataVO secondIf = new FormValidationRulesIFMetadataVO();
		
		FormValidationExpression secondIfLeft = new FormValidationExpression();
		secondIfLeft.setExpression("dropdown");
		secondIfLeft.setExpressionType(FormValidationConstants.IfOperandExpressionType.SIMPLE_EXPRESSION.toString());
		
		FormValidationExpression secondIfRight = new FormValidationExpression();
		secondIfRight.setExpression("singh");
		secondIfRight.setExpressionType(FormValidationConstants.IfOperandExpressionType.CONSTANT_VALUE.toString());
		
		secondIf.setLeftOperandFieldKey(secondIfLeft);
		secondIf.setRightOperandFieldKey(secondIfRight);
		secondIf.setOperator(FormValidationConstants.Operators.EQUAL.getOperator_displayName());
		secondIf.setJoinWithPreviousRule(FormValidationConstants.Operators.AND.getOperator_displayName());
		
		ifs.add(firstIf);
		ifs.add(secondIf);
		return ifs;
	}
	
	public static List<FormValidationRulesThenMetadataVO> greateDummythens(){
		List<FormValidationRulesThenMetadataVO> thens = new ArrayList<FormValidationRulesThenMetadataVO>();
		FormValidationRulesThenMetadataVO firstThen = new FormValidationRulesThenMetadataVO();
		firstThen.setTargetFieldKey("textbox");
		firstThen.setTypeOfAction(FormValidationConstants.ThenActionTypes.SHOW_MESSAGE.toString());
		firstThen.setAction(new FormValidationExpression(FormValidationConstants.IfOperandExpressionType.CONSTANT_VALUE.toString(),"Hello"));
		
		FormValidationRulesThenMetadataVO secondThen = new FormValidationRulesThenMetadataVO();
		secondThen.setTargetFieldKey("textbox");
		secondThen.setTypeOfAction(FormValidationConstants.ThenActionTypes.ASSIGN_VALUE.toString());
		secondThen.setAction(new FormValidationExpression(FormValidationConstants.IfOperandExpressionType.SIMPLE_EXPRESSION.toString(),"textbox"));
		
		FormValidationRulesThenMetadataVO thirdThen = new FormValidationRulesThenMetadataVO();
		thirdThen.setTargetFieldKey("textbox");
		thirdThen.setTypeOfAction(FormValidationConstants.ThenActionTypes.CHANGE_STATE.toString());
		thirdThen.setAction(new FormValidationExpression(FormValidationConstants.IfOperandExpressionType.SIMPLE_EXPRESSION.toString(),"SHOW"));
		
		thens.add(firstThen);
		thens.add(secondThen);
		thens.add(thirdThen);
		return thens;
	}
}
