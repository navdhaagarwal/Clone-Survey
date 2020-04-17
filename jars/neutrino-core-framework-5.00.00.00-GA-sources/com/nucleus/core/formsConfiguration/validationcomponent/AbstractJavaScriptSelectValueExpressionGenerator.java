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

public abstract class AbstractJavaScriptSelectValueExpressionGenerator {
	
	public  String generateExpression(String... fieldIdOnUi){
		if(fieldIdOnUi.length == 1){
			return generateExpression(fieldIdOnUi[0]);
		}
		return null;
	};
	
	public abstract String generateExpression(String fieldIdOnUi);
		
}

class SelectExpressionGeneratorById extends AbstractJavaScriptSelectValueExpressionGenerator{

	@Override
	public String generateExpression(String fieldIdOnUi) {
		return "$('#"+fieldIdOnUi+"').val()";
	}
	
}


class SelectExpressionGeneratorForDropDownById extends AbstractJavaScriptSelectValueExpressionGenerator{

	@Override
	public String generateExpression(String fieldIdOnUi) {
		return "$('#"+fieldIdOnUi+" :selected').val()";
	}
	
}

class SelectExpressionGeneratorForCascadeSelectById extends AbstractJavaScriptSelectValueExpressionGenerator{

	@Override
	public String generateExpression(String fieldIdOnUi) {
		return "$('#"+fieldIdOnUi+" :selected').text()";
	}

}

class SelectExpressionGeneratorForRadioById extends AbstractJavaScriptSelectValueExpressionGenerator{

	@Override
	public String generateExpression(String fieldIdOnUi) {
		return "$('input[id^=" + fieldIdOnUi + "]:checked').val()";
	}
	
}

class IsSelectedExpressionById extends AbstractJavaScriptSelectValueExpressionGenerator{

	@Override
	public String generateExpression(String fieldIdOnUi) {
		return "$('#"+fieldIdOnUi+"').prop('checked')==true";
	}
	
}

class IsNotselectedExpressionById extends AbstractJavaScriptSelectValueExpressionGenerator{

	@Override
	public String generateExpression(String fieldIdOnUi) {
		return "$('#"+fieldIdOnUi+"').prop('checked') == false";
	}	
}



class SelectExpressionGeneratorForMoneyById extends AbstractJavaScriptSelectValueExpressionGenerator{

	@Override
	public String generateExpression(String fieldIdOnUi) {
		return "$('#hid_" + fieldIdOnUi + "').val()";
	}
}


class SelectExpressionGeneratorForAutoCompleteById extends AbstractJavaScriptSelectValueExpressionGenerator{
	@Override
	public String generateExpression(String fieldIdOnUi) {
		return "$('#Text_"+fieldIdOnUi+"').val()";
	}


}



