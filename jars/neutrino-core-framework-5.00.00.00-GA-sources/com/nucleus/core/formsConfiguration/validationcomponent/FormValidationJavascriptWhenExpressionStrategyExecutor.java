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

import java.util.List;

import com.nucleus.core.formsConfiguration.FormVO;

/**
 * 
 * @author nitin.singh
 *
 */
public class FormValidationJavascriptWhenExpressionStrategyExecutor {

	public static String execute(FormValidationConstants.WhenActionTypes whenAction,List<FormValidationRulesIFMetadataVO> ifConditions, 
			String javascriptFunctionName,FormVO formVo) throws Exception{
		AbstractJavascriptWhenExpressionStrategy strategy = null;
		if(FormValidationConstants.WhenActionTypes.IF_VALUE_CHANGE.equals(whenAction)){
			strategy = new WhenExpressionGeneratorForOnFieldValueChangeStrategy(); 
		}else if(FormValidationConstants.WhenActionTypes.ON_PAGE_LOAD.equals(whenAction)){
			strategy = new WhenExpressionGeneratorForOnLoadStrategy();
		}else if(FormValidationConstants.WhenActionTypes.SAVE_BUTTON_CLICK.equals(whenAction)){
			strategy = new WhenExpressionGeneratorForOnSaveButtonClickStrategy();
		}else if(FormValidationConstants.WhenActionTypes.PANEL_SAVE_CLICK.equals(whenAction)){
			strategy = new WhenExpressionGeneratorForOnPanelSaveButtonClickStrategy(whenAction.getPanelIdForPartialSave());
		}else {
			throw new Exception("Unknows when Action Type"+whenAction);
		}
		return strategy.createExpression(formVo,ifConditions, javascriptFunctionName);
	}
}
