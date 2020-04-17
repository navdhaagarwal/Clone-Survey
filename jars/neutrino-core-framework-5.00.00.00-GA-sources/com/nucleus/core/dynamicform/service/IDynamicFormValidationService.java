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

import java.util.List;
import java.util.Map;

import com.nucleus.core.formsConfiguration.FormVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationMetadataVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesIFMetadataVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesIFTreeExpressionMetadata;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesThenMetadataVO;
import com.nucleus.service.BaseService;

public interface IDynamicFormValidationService extends BaseService{
	/**
	 * will create If segment javascript function using expression tree
	 * @param formVo
	 * @param expression
	 * @return
	 * @throws Exception
	 */
	public String createIfStatementFromExpressionTree(FormVO formVo, FormValidationRulesIFTreeExpressionMetadata expression) throws Exception;
	
	/**
	 * will create If segment javascript function using VO
	 * @param formVo
	 * @param ifs
	 * @return
	 * @throws Exception
	 */
	public String createIfStatementFromUIVO(FormVO formVo,List<FormValidationRulesIFMetadataVO> ifs) throws Exception;
	
	/**
	 * will create Then segment javascript function List of VO
	 * @param formVo
	 * @param thens
	 * @return
	 * @throws Exception
	 */
	public String createThenStatementFromUIVO(FormVO formVo,List<FormValidationRulesThenMetadataVO> thens) throws Exception;
	
	/**
	 * will complete javascript function using expression tree for single VO
	 * @param formVo
	 * @param validationMetadata
	 * @param javaScriptFunctionName
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> createJavaScriptForValidation(FormVO formVo, FormValidationMetadataVO validationMetadata,String javaScriptFunctionName) throws Exception;
	
	/**
	 * will complete javascript function using expression tree for list of VO
	 * @param formVo
	 * @param validationMetadatas
	 * @return
	 * @throws Exception
	 */
	public String createJavaScriptForValidation(FormVO formVo, List<FormValidationMetadataVO> validationMetadatas) throws Exception;

	public String createJavaScriptForTimeStamp(FormVO formVo) throws Exception;

	String getUserPreferenceTime();
	
}
