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
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants.StateChangeOperations;

public abstract class AbstractJavascriptStateChangeExpressionGenerator {

	public abstract String generate(FormContainerVO fieldInfo,FormVO formVo);
	
	public String getUiIdfromFieldId(FormContainerVO fieldInfo,FormVO formVo){
		String cloneRowStatus = DynamicFormValidationUtility.getClonedRowStatus(fieldInfo.getComponentDisplayKey());
		if(fieldInfo.getFieldType().equals(FormComponentType.CASCADED_SELECT)|| fieldInfo.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)) {
			return fieldInfo.getFieldKey() + "_" + formVo.getFormName();
		}
		if(fieldInfo.getFieldType().equals(FormComponentType.PANEL)){
			return fieldInfo.getFieldKey();
		}
		else {
			return fieldInfo.getFieldKey() + "_" + formVo.getFormName() + "_" + cloneRowStatus;
		}
	}
	
}


// class related to text area
class TextBoxShowStateChangeEpressionGenerator extends AbstractJavascriptStateChangeExpressionGenerator{

	@Override
	public String generate(FormContainerVO fieldInfo,FormVO formVo) {
		StateChangeOperations ope = FormValidationConstants.StateChangeOperations.SHOW;
		return "$('#"+getUiIdfromFieldId(fieldInfo, formVo)+"-control-group').removeAttr(\""+ope.getCssPropertyName()+"\");";
	}
	
}

class TextBoxHideStateChangeEpressionGenerator extends AbstractJavascriptStateChangeExpressionGenerator{

	@Override
	public String generate(FormContainerVO fieldInfo,FormVO formVo) {
		StateChangeOperations ope = FormValidationConstants.StateChangeOperations.HIDE;
		return "$('#"+getUiIdfromFieldId(fieldInfo, formVo)+"-control-group').attr(\""+ope.getCssPropertyName()+"\",\""+ope.getCssPropertyValue()+"\");";
	}
	
}

class PanelShowStateChangeEpressionGenerator extends AbstractJavascriptStateChangeExpressionGenerator{

	@Override
	public String generate(FormContainerVO fieldInfo,FormVO formVo) {
		StateChangeOperations ope = FormValidationConstants.StateChangeOperations.SHOW;
		return "$('#"+getUiIdfromFieldId(fieldInfo, formVo)+"').removeAttr(\""+ope.getCssPropertyName()+"\");";
	}

}
class PanelHideStateChangeEpressionGenerator extends AbstractJavascriptStateChangeExpressionGenerator{

	@Override
	public String generate(FormContainerVO fieldInfo,FormVO formVo) {
		StateChangeOperations ope = FormValidationConstants.StateChangeOperations.HIDE;
		return "$('#"+getUiIdfromFieldId(fieldInfo, formVo)+"').attr(\""+ope.getCssPropertyName()+"\",\""+ope.getCssPropertyValue()+"\");";
	}

}

/*class TextBoxRequiredStateChangeEpressionGenerator extends AbstractJavascriptStateChangeExpressionGenerator{

	@Override
	public String generate(String fieldIdonUi) {
		StateChangeOperations ope = FormValidationConstants.StateChangeOperations.REQUIRED;
		// TODO we need to get the exact value and way to apply the same
		return null;
	}
	
}

class TextBoxNotRequiredStateChangeEpressionGenerator extends AbstractJavascriptStateChangeExpressionGenerator{

	@Override
	public String generate(String fieldIdonUi) {
		StateChangeOperations ope = FormValidationConstants.StateChangeOperations.NOT_REQUIRED;
		// TODO we need to get the exact value and way to apply the same
		return null;
	}
}*/
	
//date picker 
	
	class DateShowStateChangeEpressionGenerator extends AbstractJavascriptStateChangeExpressionGenerator{

		@Override
		public String generate(FormContainerVO fieldInfo,FormVO formVo) {
			StateChangeOperations ope = FormValidationConstants.StateChangeOperations.SHOW;
			return "$('#"+getUiIdfromFieldId(fieldInfo, formVo)+"-control-group').removeAttr(\""+ope.getCssPropertyName()+"\");";
		}
		
	}

	class DateHideStateChangeEpressionGenerator extends AbstractJavascriptStateChangeExpressionGenerator{

		@Override
		public String generate(FormContainerVO fieldInfo,FormVO formVo) {
			StateChangeOperations ope = FormValidationConstants.StateChangeOperations.HIDE;
			return "$('#"+getUiIdfromFieldId(fieldInfo, formVo)+"-control-group').attr(\""+ope.getCssPropertyName()+"\",\""+ope.getCssPropertyValue()+"\");";
		}
		
	}
	
	
	class DropDownShowStateChangeEpressionGenerator extends AbstractJavascriptStateChangeExpressionGenerator{

		@Override
		public String generate(FormContainerVO fieldInfo,FormVO formVo) {
			StateChangeOperations ope = FormValidationConstants.StateChangeOperations.SHOW;
			if(fieldInfo.getBinderName()!=null && fieldInfo.getBinderName().equals("Custom Binder")){
				return "$('#"+getUiIdfromFieldId(fieldInfo, formVo)+"').parent().parent().removeAttr(\""+ope.getCssPropertyName()+"\");";
			}
			return "$('#"+getUiIdfromFieldId(fieldInfo, formVo)+"-control-group').removeAttr(\""+ope.getCssPropertyName()+"\");";
		}
		
	}

	class DropdownHideStateChangeEpressionGenerator extends AbstractJavascriptStateChangeExpressionGenerator{

		@Override
		public String generate(FormContainerVO fieldInfo,FormVO formVo) {
			StateChangeOperations ope = FormValidationConstants.StateChangeOperations.HIDE;
			if(fieldInfo.getBinderName()!=null && fieldInfo.getBinderName().equals("Custom Binder")){
				return "$('#"+getUiIdfromFieldId(fieldInfo, formVo)+"').parent().parent().attr(\""+ope.getCssPropertyName()+"\",\""+ope.getCssPropertyValue()+"\");";
			}
			return "$('#"+getUiIdfromFieldId(fieldInfo, formVo)+"-control-group').attr(\""+ope.getCssPropertyName()+"\",\""+ope.getCssPropertyValue()+"\");";
		}
		
	}
	
