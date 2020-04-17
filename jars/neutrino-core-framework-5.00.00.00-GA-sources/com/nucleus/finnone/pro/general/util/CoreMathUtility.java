/**
 * 
 */
package com.nucleus.finnone.pro.general.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.nucleus.finnone.pro.base.constants.CoreConstant;
import com.nucleus.logging.BaseLoggers;

/**
 * @author Dhananjay.Jha
 *
 */

public class CoreMathUtility {
	public static int ROUNDING_METHOD_ROUND_OFF = 0;
	/**
	 * 
	 * 
	 * @param number
	 * @param roundTill
	 * @return
	 */
	public static int getPrecision(BigDecimal number, int roundTill){
		
		int precision = (number.precision()-number.scale())+roundTill;
		return precision;
	}
	/**
	 * 
	 * @param number
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static MathContext getMathContext(BigDecimal number, int roundTill, String roundingMode){

		return new MathContext(getPrecision(number,roundTill), getRoundingMode(roundingMode));
		
	}
	/**
	 * 
	 * @param number
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal round(BigDecimal number, int roundTill, String roundingMode){
			return round(number,roundTill,getRoundingMode(roundingMode));
	}
	/**
	 * 
	 * @param number
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal round(BigDecimal number,  int roundTill, RoundingMode roundingMode){
		return number.setScale(roundTill, roundingMode);
	} 
	
	
	/**
	 * 
	 * @param number1
	 * @param number2
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal add(BigDecimal number1,BigDecimal number2,  BigDecimal multipleOf, String roundingMode){
		return add(number1,number2,multipleOf,getRoundingMode(roundingMode));
	} 
	
	
	public static BigDecimal minimum(BigDecimal number1,BigDecimal number2){
		return number1.compareTo(number2)>0?number2:number1;
	} 
	/**
	 * 
	 * @param number1
	 * @param number2
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal add(BigDecimal number1,BigDecimal number2,  BigDecimal multipleOf, RoundingMode roundingMode){
		BigDecimal sum = number1.add(number2);
		return getRoundedValue(sum, multipleOf, roundingMode);
//		return sum.setScale(roundTill, roundingMode);
		
	} 
	
	/**
	 * 
	 * @param number1
	 * @param number2
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal subtract(BigDecimal number1,BigDecimal number2, BigDecimal multipleOf, String roundingMode){
		return subtract(number1,number2,multipleOf,getRoundingMode(roundingMode));
	} 
	/**
	 * 
	 * @param number1
	 * @param number2
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal subtract(BigDecimal number1,BigDecimal number2,  BigDecimal multipleOf, RoundingMode roundingMode){
		BigDecimal sum = number1.subtract(number2);
		return getRoundedValue(sum, multipleOf, roundingMode);
//		return sum.setScale(roundTill, roundingMode);
		
	} 
	
	/**
	 * 
	 * @param number1
	 * @param number2
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal multiply(BigDecimal number1,BigDecimal number2,  int roundTill, String roundingMode){
		return multiply(number1,number2,roundTill,getRoundingMode(roundingMode));
	} 
	/**
	 * 
	 * @param number1
	 * @param number2
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal multiply(BigDecimal number1,BigDecimal number2,  int roundTill, RoundingMode roundingMode){
		BigDecimal result = number1.multiply(number2);
		return result.setScale(roundTill, roundingMode);
		
	} 
	
	/**
	 * 
	 * @param number1
	 * @param number2
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal divide(BigDecimal numerator,BigDecimal denominator,  int roundTill, int roundingMode){
		return divide(numerator,denominator,roundTill,getRoundingModeValue(roundingMode));
	} 
	
	/**
	 * 
	 * @param number1
	 * @param number2
	 * @param roundTill
	 * @param roundingMode
	 * @return
	 */
	public static BigDecimal divide(BigDecimal numerator,BigDecimal denominator, int roundTill, RoundingMode roundingMode){
		return numerator.divide(denominator, roundTill, roundingMode);
	} 
	/**
	 * 
	 * @param number
	 * @return
	 */
	public static BigDecimal replaceNullWithZero(BigDecimal number){
		return number!=null?number:BigDecimal.ZERO;
	}
	
	/**
	 * 
	 * @param amount
	 * @param minAmount
	 * @param maxAmount
	 * * @return
	 */
	public static boolean between(BigDecimal amount,BigDecimal minAmount, BigDecimal maxAmount){
		boolean inRange=false;
		
		//0="Both values are equal", 1="First Value is greater", -1="Second value is greater"
		int minComparisonResult= amount.compareTo(minAmount);
		int maxComparisonResult= amount.compareTo(maxAmount);
		
		if(minComparisonResult == -1  || maxComparisonResult == 1) {
			inRange = false;
		} else if ((minComparisonResult == 0 || minComparisonResult == 1) && (maxComparisonResult == 0 || maxComparisonResult == -1)) {
			inRange = true;
		}
		
		return inRange;
		
	} 
	
	/**
	 * 
	 * @param roundingMethod
	 * @return
	 */
	public static RoundingMode getRoundingMode(String roundingMethod)
	{
		if("RU".equals(roundingMethod))
		{
			return RoundingMode.CEILING;	
		}
		else if ("UP".equals(roundingMethod))
		{
			return RoundingMode.UP;
		}
		else if ("RD".equals(roundingMethod))
		{
			return RoundingMode.DOWN;
		}
		else if ("RO".equals(roundingMethod))
		{
			return RoundingMode.HALF_UP;
		}
		throw new IllegalArgumentException("Rounding method "+roundingMethod+" is not supported.");
	}
	
	
	public static RoundingMode getRoundingModeValue(int roundingMethod)
	{
		if(roundingMethod==0)
		{
			return RoundingMode.HALF_UP;	
		}
		else if (roundingMethod==1)
		{
			return RoundingMode.CEILING;
		}
		else if (roundingMethod==-1)
		{
			return RoundingMode.DOWN;
		}
		throw new IllegalArgumentException("Rounding method "+roundingMethod+" is not supported.");
	}
	
	
	/**
     * Method is used to round the value or number based 
     * on the Round Method using multiple Of.
     * @param value
     * @param multipleOf
     * @param roundMethod
     * @return
     */

    public static BigDecimal getRoundedValue(BigDecimal value, BigDecimal multipleOf, int roundMethod){
          return getRoundedValue(value,multipleOf,getRoundingModeValue(roundMethod));

    }


	/**
	 * Method is used to round the value or number based 
	 * on the Round Method using multiple Of.
	 * @param value
	 * @param multipleOf
	 * @param roundMethod
	 * @return
	 */
	public static BigDecimal getRoundedValue(BigDecimal value, BigDecimal multipleOf, RoundingMode roundingMode)
	{
		BigDecimal roundedValue = value.divide(multipleOf, 5, RoundingMode.HALF_UP);
		roundedValue = roundedValue.setScale(0, roundingMode).multiply(multipleOf);
		
		return roundedValue;
	}
	
	
	/*
	 * Method is used to get multiple of based on precision
	 */
	public static BigDecimal getMultipleOf(int precision){
		return BigDecimal.valueOf(1/Math.pow(10,precision));
	}
	
	public static BigDecimal add(BigDecimal amountOne,BigDecimal amounttwo){
		return (amountOne.add(amounttwo));
	}
	
	public static BigDecimal subtract(BigDecimal amountOne,BigDecimal amounttwo){
		return (amountOne.subtract(amounttwo));
	}
	
	public static Integer subtract(Integer number1,Integer number2){
	    return number1-number2;
	}
	
	public static Integer add(Integer number1,Integer number2){
	    return number1+number2;
	}
	
	public static Integer multiply(Integer number1,Integer number2){
	    return number1*number2;
	}
	
	
	public static BigDecimal preparedFormulaAndCalculate(BigDecimal rate,int noOfdays,String daysPerYearCalculationMethod,BigDecimal attributeValue,String byDays){
		int daysInYear = 0;
		if (String.valueOf(CoreConstant.DAYS_IN_YEAR_COMPUTATION_METHOD_ACTUAL).equals(daysPerYearCalculationMethod)) {
			daysInYear = 365;
		} else if (String.valueOf(CoreConstant.DAYS_IN_YEAR_COMPUTATION_METHOD_360).equals(daysPerYearCalculationMethod)) {
			daysInYear = 360;
		} else if (String.valueOf(CoreConstant.DAYS_IN_YEAR_COMPUTATION_METHOD_365).equals(daysPerYearCalculationMethod)) {
			daysInYear = 365;
		}
		
		return evaluateAndCalculate(rate,attributeValue,noOfdays,daysInYear,byDays);
	}
	
	public static BigDecimal evaluateAndCalculate(BigDecimal rate,BigDecimal attributeValue,int noOfdays,int daysInYear,String byDays){
		if(CoreConstant.BY_DAYS_CAL.equals(byDays)){
			BigDecimal dividend = attributeValue.multiply(rate).multiply(BigDecimal.valueOf(noOfdays));
			BigDecimal divisor = BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(daysInYear));
			return dividend.divide(divisor,20,RoundingMode.HALF_UP);
		}else{
			BigDecimal dividend = attributeValue.multiply(rate);
			BigDecimal divisor = BigDecimal.valueOf(100);
			return dividend.divide(divisor,20,RoundingMode.HALF_UP);
		}
	}
	
	public static Boolean isLessThan(Double is,Double that){
		return is.doubleValue()<that.doubleValue();
	}
	
	public static Boolean isGreaterThan(Double is,Double that){
		return is.doubleValue()>that.doubleValue();
	}
	
	public static Boolean isEqual(Double is,Double that){
		return is.doubleValue()==that.doubleValue();
	}
	
	public static Boolean isGreaterThanOrEqual(Double is,Double that){
		return is.doubleValue()>=that.doubleValue();
	}
	
	public static Boolean isLessThanOrEqual(Double is,Double that){
		return is.doubleValue()<=that.doubleValue();
	}
	
	public static Boolean isLessThan(Integer is,Integer that){
		return is.intValue()<that.intValue();
	}
	
	public static Boolean isGreaterThan(Integer is,Integer that){
		return is.intValue()>that.intValue();
	}
	
	public static Boolean isEqual(Integer is,Integer that){
		return is.intValue()==that.intValue();
	}
	
	public static Boolean isGreaterThanOrEqual(Integer is,Integer that){
		return is.intValue()>=that.intValue();
	}
	
	public static Boolean isLessThanOrEqual(Integer is,Integer that){
		return is.intValue()<=that.intValue();
	}
	
	public static Boolean isLessThan(BigDecimal is,BigDecimal that){
		return is.compareTo(that)<0;
	}
	
	public static Boolean isGreaterThan(BigDecimal is,BigDecimal that){
		return is.compareTo(that)>0;
	}
	
	public static Boolean isEqual(BigDecimal is,BigDecimal that){
		return is.compareTo(that)==0;
	}
	
	public static Boolean isGreaterThanOrEqual(BigDecimal is,BigDecimal that){
		return is.compareTo(that)>=0;
	}
	
	public static Boolean isLessThanOrEqual(BigDecimal is,BigDecimal that){
		return is.compareTo(that)<=0;
	}
	public static Boolean isEqual(Long is, Long that) {
	    return is.compareTo(that) == 0;
	  }
	 public static Boolean isGreaterThan(Long is, Long that) {
		    return is.compareTo(that) > 0;
		  }
	
	 
		  
}