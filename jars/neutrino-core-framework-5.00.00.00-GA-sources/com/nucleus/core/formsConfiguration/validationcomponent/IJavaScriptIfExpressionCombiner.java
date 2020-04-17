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

import com.nucleus.core.formsConfiguration.*;
import com.nucleus.core.money.utils.*;

public interface IJavaScriptIfExpressionCombiner {
	public String combine(String leftExpression,String rightExpression,String fieldType);
}

class EqualOperatorCombiner implements IJavaScriptIfExpressionCombiner{



	@Override
	public String combine(String leftExpression, String rightExpression,String fieldType){
		if(fieldType!=null) {
			if (fieldType.equals(FormComponentType.MONEY)) {
				if (rightExpression.contains(MoneyUtils.MONEY_DELIMITER)) {
					String[] rightExpArray = rightExpression.replaceAll("'", "").split(MoneyUtils.MONEY_DELIMITER);
					rightExpression = "parseFloat('" + rightExpArray[1] + "'.replace(/,/g, ''))";

					//if money code is same then only check for value equality
					return "( " + leftExpression + ".split('~')[0] == '" + rightExpArray[0] + "'  &&  parseFloat(" + leftExpression + ".split('~')[1].replace(/,/g, '')) == " + rightExpression + " ) ";

				}
			} else if (fieldType.equals(FormComponentType.DATE)) {

				leftExpression = "Date.parseString(" + leftExpression + ",dateFormatSessionScope).getTime()";

			}
		}
		return "( "+leftExpression+" )"+" == "+"( "+rightExpression+" )";
	}
	
}


class NotEqualOperatorCombiner implements IJavaScriptIfExpressionCombiner{


	@Override
	public String combine(String leftExpression, String rightExpression,String fieldType){
		if(fieldType!=null) {
			if (fieldType.equals(FormComponentType.MONEY)) {
				if (rightExpression.contains(MoneyUtils.MONEY_DELIMITER)) {
					String[] rightExpArray = rightExpression.replace("'", "").split(MoneyUtils.MONEY_DELIMITER);
					rightExpression = "parseFloat('" + rightExpArray[1] + "'.replace(/,/g, ''))";
					return "( " + leftExpression + ".split('~')[0] == '" + rightExpArray[0] + "'  &&  parseFloat(" + leftExpression + ".split('~')[1].replace(/,/g, '')) != " + rightExpression + " ) ";
				}
			}
		}
		return "( "+leftExpression+" )"+" != "+"( "+rightExpression+" )";
	}
	
}


class StartsWithOperatorCombiner implements IJavaScriptIfExpressionCombiner{

	@Override
	public String combine(String leftExpression, String rightExpression,String fieldType){
		return leftExpression+".startsWith("+rightExpression+")";
	}
	
}

class ContainsOperatorCombiner implements IJavaScriptIfExpressionCombiner{

	@Override
	public String combine(String leftExpression, String rightExpression,String fieldType){
		return leftExpression+".indexOf("+rightExpression+") != -1";
	}
	
}

class GreaterThenOperatorCombiner implements IJavaScriptIfExpressionCombiner{


	@Override
	public String combine(String leftExpression, String rightExpression,String fieldType){
		if(fieldType!=null) {
			if (fieldType.equals(FormComponentType.MONEY)) {
				if (rightExpression.contains(MoneyUtils.MONEY_DELIMITER)) {
					String[] rightExpArray = rightExpression.replace("'", "").split(MoneyUtils.MONEY_DELIMITER);
					rightExpression = "parseFloat('" + rightExpArray[1] + "'.replace(/,/g, ''))";
					return "( " + leftExpression + ".split('~')[0] == '" + rightExpArray[0] + "'  &&  parseFloat(" + leftExpression + ".split('~')[1].replace(/,/g, '')) > " + rightExpression + " ) ";
				}
			}
		}
		return "( "+leftExpression+" )" +" > " +"( "+rightExpression+" )";
	}
	
}

class LessThenOperatorCombiner implements IJavaScriptIfExpressionCombiner{

	@Override
	public String combine(String leftExpression, String rightExpression,String fieldType){
		if(fieldType!=null) {
			if (fieldType.equals(FormComponentType.MONEY)) {
				if (rightExpression.contains(MoneyUtils.MONEY_DELIMITER)) {
					String[] rightExpArray = rightExpression.replace("'", "").split(MoneyUtils.MONEY_DELIMITER);
					rightExpression = "parseFloat('" + rightExpArray[1] + "'.replace(/,/g, ''))";
					return "( " + leftExpression + ".split('~')[0] == '" + rightExpArray[0] + "'  &&  parseFloat(" + leftExpression + ".split('~')[1].replace(/,/g, '')) < " + rightExpression + " ) ";
				}
			}
		}
		return "( "+leftExpression+" )" +" < " +"( "+rightExpression+" )";
	}
	
}

class IsSelectedOperatorCombiner implements IJavaScriptIfExpressionCombiner{

	@Override
	public String combine(String leftExpression, String rightExpression,String fieldType){
		return leftExpression;
	}
	
}

class IsNotSelectedOperatorCombiner implements IJavaScriptIfExpressionCombiner{

	@Override
	public String combine(String leftExpression, String rightExpression,String fieldType){
		return leftExpression;
	}
	
}

class AndOperatorCombiner implements IJavaScriptIfExpressionCombiner{

	@Override
	public String combine(String leftExpression, String rightExpression,String fieldType){
		return "( "+leftExpression+ " )" +" && " + "( "+rightExpression + " )" ;
	}
	
}

class OrOperatorCombiner implements IJavaScriptIfExpressionCombiner{

	@Override
	public String combine(String leftExpression, String rightExpression,String fieldType){
		return "( "+leftExpression+ " )" +" || " + "( "+rightExpression + " )" ;
	}
	
}
