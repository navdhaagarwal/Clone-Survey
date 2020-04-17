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
import java.util.ArrayList;
import java.util.List;

public class FormValidationMetadataVO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// objects to hold ifs
	private List<FormValidationRulesIFMetadataVO> ifConditions;
	
	
	private List<FormValidationRulesThenMetadataVO> thenActions;
	
	// this will be derived no need to populate from UI
	private String whenCondition;

	private String whenConditionPanelId;

	public List<FormValidationRulesIFMetadataVO> getIfConditions() {
		return ifConditions;
	}

	public void setIfConditions(List<FormValidationRulesIFMetadataVO> ifConditions) {
		this.ifConditions = ifConditions;
	}

	public List<FormValidationRulesThenMetadataVO> getThenActions() {
		return thenActions;
	}

	public void setThenActions(List<FormValidationRulesThenMetadataVO> thenActions) {
		this.thenActions = thenActions;
	}

	public String getWhenCondition() {
		return whenCondition;
	}

	public void setWhenCondition(String whenCondition) {
		this.whenCondition = whenCondition;
	}


	public String getWhenConditionPanelId() {
		return whenConditionPanelId;
	}

	public void setWhenConditionPanelId(String whenConditionPanelId) {
		this.whenConditionPanelId = whenConditionPanelId;
	}

	public FormValidationMetadataVO() {
		super();
		ifConditions = new ArrayList();
		FormValidationRulesIFMetadataVO ifs = new FormValidationRulesIFMetadataVO();
		ifConditions.add(ifs);
		thenActions = new ArrayList();
		FormValidationRulesThenMetadataVO then = new FormValidationRulesThenMetadataVO();
		thenActions.add(then);
	} 
	

}
