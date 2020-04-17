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

import java.util.*;
import java.util.stream.Collectors;

import com.nucleus.core.formsConfiguration.FormComponentType;
import com.nucleus.core.formsConfiguration.FormContainerVO;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.service.BaseServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.nucleus.core.formsConfiguration.FormVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationExpression;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationIfExpressionStrategyExecutor;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationJavascriptWhenExpressionStrategyExecutor;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationMetadataVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesIFMetadataVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesIFTreeExpressionMetadata;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesThenMetadataVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationThenExpressionStrategyExecutor;
import com.nucleus.core.formsConfiguration.validationcomponent.IJavaScriptIfExpressionCombiner;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants.Operators;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants.WhenActionTypes;
import com.nucleus.logging.BaseLoggers;

@Component(value="IDynamicFormValidationService")
public class DynamicFormValidationServiceImpl extends BaseServiceImpl implements IDynamicFormValidationService {

	
	@Override
	public String createIfStatementFromExpressionTree(FormVO formVo, FormValidationRulesIFTreeExpressionMetadata expression) throws Exception{
		FormValidationConstants.initMappings();
		if(expression == null){
			return null;
		}
		String leftExpression = null;
		String rightExpression = null;
		// it means this is the head of last node and it has to be operator
		if(expression.getLeftExp().getLeftExp() == null){
			// check operator is valid
			FormValidationExpression operator = expression.getNode();
			Operators opr = FormValidationConstants.Operators.getOperatorByOperatorExpression(operator.getExpression());
			if(opr == null){
				throw new Exception("invalid operator found : "+operator.getExpression());
			}
			
			FormValidationExpression leftExp = expression.getLeftExp().getNode();
			if(leftExp !=null){
				leftExpression = FormValidationIfExpressionStrategyExecutor.execute(formVo, leftExp, opr);
			}else{
				throw new Exception("Node in left expression can not be null");
			}
			if(expression.getRightExp() !=null && expression.getRightExp().getNode()!=null){
				FormValidationExpression rightExp = expression.getRightExp().getNode();
				rightExpression = FormValidationIfExpressionStrategyExecutor.execute(formVo, rightExp, opr);
			}	
			
			return createStatement(expression, leftExpression, rightExpression);
			
		}
		
		leftExpression = createIfStatementFromExpressionTree(formVo, expression.getLeftExp());
		rightExpression = createIfStatementFromExpressionTree(formVo, expression.getRightExp());
		
		return createStatement(expression, leftExpression, rightExpression);
	}

	private String createStatement(FormValidationRulesIFTreeExpressionMetadata expression, String leftExpression,
			String rightExpression) throws Exception {
		FormValidationExpression operator =  expression.getNode();
		if(operator.getExpression() != null){
			IJavaScriptIfExpressionCombiner combiner = FormValidationConstants.Operators.getCobminerByExpression(operator.getExpression()); 
			if(combiner == null){
				throw new Exception("Unsupported operator :"+operator.getExpression());
			}
			return combiner.combine(leftExpression, rightExpression, expression.getRightExp().getNode().getExpressionFieldType());
			
		}else{
			BaseLoggers.flowLogger.error("Null Operator found :");
			throw new Exception("Null Operator found :");
		}
	}

	@Override
	public String createIfStatementFromUIVO(FormVO formVo, List<FormValidationRulesIFMetadataVO> ifs) throws Exception {
		return createIfStatementFromExpressionTree(formVo, DynamicFormValidationUtility.convertIntoTreeExpression(ifs));
	}

	@Override
	public String createThenStatementFromUIVO(FormVO formVo, List<FormValidationRulesThenMetadataVO> thens)
			throws Exception {
		if(thens == null || thens.isEmpty()){
			BaseLoggers.flowLogger.warn("No Thens action is received");
			return null;
		}
		StringBuilder result = new StringBuilder();
		for (FormValidationRulesThenMetadataVO formValidationRulesThenMetadataVO : thens) {
			String thenInJavaScript = createThenStatementFromUIVO(formVo, formValidationRulesThenMetadataVO);
			if(thenInJavaScript !=null){
				result.append(thenInJavaScript).append(";").append("\n");
			}
		}
		return result.toString();
	}

	private String createThenStatementFromUIVO(FormVO formVo,FormValidationRulesThenMetadataVO then) throws Exception{
		return FormValidationThenExpressionStrategyExecutor.execute(then, formVo);
	}
	
	public Map<String,String> createJavaScriptForValidation(FormVO formVo, FormValidationMetadataVO validationMetadata,String javaScriptFunctionName) throws Exception{
		// this need to be uncommented on final touch up
		StringBuilder javaScriptBuilder = new StringBuilder();
		if(validationMetadata.getThenActions() == null){
			throw new Exception("No Javascript rule can be created without then action");
		}
		Boolean ifAdded = Boolean.FALSE;
		// change JS function Name TODO
		//String javaScriptFunctionName = "clientSideRules()";
		javaScriptBuilder.append("function "+javaScriptFunctionName+"{").append("\n");
		if(validationMetadata.getIfConditions() !=null && !validationMetadata.getIfConditions().isEmpty()){
			javaScriptBuilder.append("if(").append(createIfStatementFromUIVO(formVo,validationMetadata.getIfConditions())).append(")").append("\n");
			javaScriptBuilder.append("{"); // opening of then under if
			ifAdded = Boolean.TRUE;
		}
		javaScriptBuilder.append(createThenStatementFromUIVO(formVo, validationMetadata.getThenActions()));
		if(ifAdded) {
			javaScriptBuilder.append("}"); // closing of then under if
		}
		javaScriptBuilder.append("}");
		BaseLoggers.flowLogger.info("Created Java Script is :["+javaScriptBuilder.toString()+"]");
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("FUNCTION", javaScriptBuilder.toString());
		resultMap.put("FUNCTION_NAME", javaScriptFunctionName);
		return resultMap;
	}
	
	public String createJavaScriptForValidation(FormVO formVo, List<FormValidationMetadataVO> validationMetadatas) throws Exception{
		// here we have to generate when event
		if(validationMetadatas == null || validationMetadatas.isEmpty()){
			BaseLoggers.flowLogger.info("No Validation Metadata received to create Javascript validations");
			return null;
		}
		StringBuilder result = new StringBuilder();
		int index = 0;
		formVo.setFormName(formVo.getFormName().replace(" ", "_"));
		for (FormValidationMetadataVO formValidationMetadataVO : validationMetadatas) {
			String whenEvent = formValidationMetadataVO.getWhenCondition();
			FormValidationConstants.WhenActionTypes whenAction = WhenActionTypes.valueOf(whenEvent);
			if(whenAction.equals(WhenActionTypes.PANEL_SAVE_CLICK)){
				whenAction.setPanelIdForPartialSave(formValidationMetadataVO.getWhenConditionPanelId());
			}
			if(whenAction == null){
				BaseLoggers.exceptionLogger.error("Unknown When Action Received"+whenEvent);
				throw new Exception("Unknown When Action Received"+whenEvent);
			}
			if(whenAction.equals(WhenActionTypes.PANEL_SAVE_CLICK)){
				whenAction.setPanelIdForPartialSave(formValidationMetadataVO.getWhenConditionPanelId());
			}
			Map<String,String> resultMap = createJavaScriptForValidation(formVo,formValidationMetadataVO,"clientSideRules_"+index+"()");
			String javaScriptFunction = resultMap.get("FUNCTION");
			result.append("\n");
			result.append(javaScriptFunction);
			String javaScriptFunctionName = resultMap.get("FUNCTION_NAME");
			result.append("\n").append(FormValidationJavascriptWhenExpressionStrategyExecutor.execute(whenAction, formValidationMetadataVO.getIfConditions(), javaScriptFunctionName,formVo));
			index++;
		}
		return result.toString();
	}

	public Map<String,String> createJavaScriptForTimeStamp(FormVO formVo,FormContainerVO formContainerVO,String javaScriptFunctionName) throws Exception{
		// this need to be uncommented on final touch up
		StringBuilder javaScriptBuilder = new StringBuilder();

		String generatedFieldId = getFieldIdofUI(formVo,formContainerVO.getFieldKey());
		String fieldType=getFieldType(formVo,formContainerVO.getFieldKey());
		if(fieldType.equalsIgnoreCase(FormComponentType.AUTOCOMPLETE)){
			generatedFieldId="Text_"+generatedFieldId;
		}else if(fieldType.equalsIgnoreCase(FormComponentType.MONEY)){
			generatedFieldId="amount_"+generatedFieldId;
		}
		javaScriptBuilder.append("function "+javaScriptFunctionName+"{");
		javaScriptBuilder.append("$.ajax({url :  getContextPath() + '/app/dynamicForm/getUserPreferenceTime',").append("\n");
		javaScriptBuilder.append("async : true,type : 'GET',");
		javaScriptBuilder.append("success : function(jqXHR) {");
		javaScriptBuilder.append("debugger;");
		javaScriptBuilder.append("$('#" + generatedFieldId+"').val(jqXHR);");
		javaScriptBuilder.append("}});");
		javaScriptBuilder.append("\n");
		javaScriptBuilder.append("}");
		BaseLoggers.flowLogger.info("Created Java Script is :["+javaScriptBuilder.toString()+"]");
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("FUNCTION", javaScriptBuilder.toString());
		resultMap.put("FUNCTION_NAME", javaScriptFunctionName);
		return resultMap;
	}

	public String createJavaScriptForTimeStamp(FormVO formVo) throws Exception{
		// here we have to generate when eventform
		List<FormContainerVO> formContainerVOS=formVo.getContainerVOList();
		List<FormContainerVO> fieldsKeyList=new ArrayList<>();
		if(CollectionUtils.isNotEmpty(formContainerVOS)){
			for(FormContainerVO formContainer:formContainerVOS){
				List<FormContainerVO> formFieldsKeyList=formContainer.getFormContainerVOList().stream().filter(value -> value.getAssociatedFieldKey() != null && FormComponentType.CURRENT_TIME_STAMP.equalsIgnoreCase(value.getFieldType())).collect(Collectors.toList());
				fieldsKeyList.addAll(formFieldsKeyList);
			}
		}
		StringBuilder result = new StringBuilder();
		int index = 0;
		formVo.setFormName(formVo.getFormName().replace(" ", "_"));
		for (FormContainerVO formContainerVO : fieldsKeyList) {

			Map<String,String> resultMap = createJavaScriptForTimeStamp(formVo,formContainerVO,"fieldKeyTimeStamp"+index+"()");
			String javaScriptFunction = resultMap.get("FUNCTION");
			result.append("\n");
			result.append(javaScriptFunction);
			String javaScriptFunctionName = resultMap.get("FUNCTION_NAME");
			result.append("\n").append(addOnChange(formVo,formContainerVO.getAssociatedFieldKey(),javaScriptFunctionName));
			index++;
		}
		return result.toString();
	}

	public String addOnChange(FormVO formVo,String fieldKey,
								   String javascriptFunctionName) throws Exception {

		StringBuilder result = new StringBuilder();
		result.append("$(document).ready(function() {").append("\n");
		Set<String> fieldsAdded = new HashSet<String>();

		String generatedFieldId = getFieldIdofUI(formVo,fieldKey);
		String fieldType=getFieldType(formVo,fieldKey);
		if(fieldType.equalsIgnoreCase(FormComponentType.AUTOCOMPLETE)){
			generatedFieldId="Text_"+generatedFieldId;
		}else if(fieldType.equalsIgnoreCase(FormComponentType.MONEY)){
			generatedFieldId="amount_"+generatedFieldId;
		}
			fieldsAdded.add(generatedFieldId);
			result.append("$('#" + generatedFieldId
					+"').change( function() {" + javascriptFunctionName + "});").append("\n");


		result.append("});");
		return result.toString();
	}
	private String getFieldIdofUI(FormVO formVo,String fieldKey) throws Exception{
		FormContainerVO componentVo = DynamicFormValidationUtility.getFormComponentbyKey(formVo,fieldKey);
		if(componentVo == null){
			throw new Exception("Unknwon Field Key : "+fieldKey);
		}
		// clone row status can be retrived from component display key, it is stored in form component[0][1]
		// we have to read 0 from that
		String cloneRowStatus = DynamicFormValidationUtility.getClonedRowStatus(componentVo.getComponentDisplayKey());
		if(componentVo.getFieldType().equals(FormComponentType.CASCADED_SELECT)){
			return fieldKey+"_"+formVo.getFormName();
		}
		else {
			return fieldKey+"_"+formVo.getFormName()+"_"+cloneRowStatus;
		}

	}
	private String getFieldType(FormVO formVo,String fieldKey) throws Exception{
		FormContainerVO componentVo = DynamicFormValidationUtility.getFormComponentbyKey(formVo,fieldKey);
		if(componentVo == null){
			throw new Exception("Unknwon Field Key : "+fieldKey);
		}else{
			return componentVo.getFieldType();
		}
	}
	public  String getUserPreferenceTime(){
		return DateUtils.getFormattedDate(new DateTime(),getUserPreferredDateTimeFormat());
	}
}
