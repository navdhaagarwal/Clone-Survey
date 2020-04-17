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

public class FormValidationThenExpressionStrategyExecutor {

	public static String execute(FormValidationRulesThenMetadataVO thenActions,FormVO formVo) throws Exception{
		if(thenActions == null || thenActions.getTargetFieldKey() == null){
			throw new Exception("Received target field or actions are null");
		}
		AbstractFormValidationThenExpressionStrategy strategy = null;
		if(FormValidationConstants.ThenActionTypes.SHOW_MESSAGE.toString().equals(thenActions.getTypeOfAction())){
			strategy = new FormValidationErrorMessageThenExpressionStrategy();
		}else if(FormValidationConstants.ThenActionTypes.ASSIGN_VALUE.toString().equals(thenActions.getTypeOfAction())){
			strategy = new FormValidationAssignValueThenExpressionStrategy();
		}else if(FormValidationConstants.ThenActionTypes.CHANGE_STATE.toString().equals(thenActions.getTypeOfAction())){
			strategy = new FormValidationStateChangeThenExpressionStrategy();
		}else{
			throw new Exception("Unknown Then action received : "+thenActions.getTypeOfAction());
		}
		return strategy.execute(thenActions.getTargetFieldKey(), formVo, thenActions.getAction());
	}
}
