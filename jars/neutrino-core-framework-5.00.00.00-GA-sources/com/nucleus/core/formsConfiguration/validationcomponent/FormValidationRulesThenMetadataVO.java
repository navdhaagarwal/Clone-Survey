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

import java.io.Serializable;

public class FormValidationRulesThenMetadataVO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String targetFieldKey;
	
	// have to map with enum - ThenActionTypes
	private String typeOfAction;
	
	/**
	 *  in case of SHOW_MESSAGE -> type will be constant and expression will be value
	 *  in case of ASSIGN_VALUE -> type will be simple/complex expression
	 *  in case of change_state -> type will be contant and expression will hold state class
	 */
	private FormValidationExpression action;

	public String getTypeOfAction() {
		return typeOfAction;
	}

	public void setTypeOfAction(String typeOfAction) {
		this.typeOfAction = typeOfAction;
	}

	public FormValidationExpression getAction() {
		return action;
	}

	public void setAction(FormValidationExpression action) {
		this.action = action;
	}
	

	public String getTargetFieldKey() {
		return targetFieldKey;
	}

	public void setTargetFieldKey(String targetFieldKey) {
		this.targetFieldKey = targetFieldKey;
	}
	
	
}
