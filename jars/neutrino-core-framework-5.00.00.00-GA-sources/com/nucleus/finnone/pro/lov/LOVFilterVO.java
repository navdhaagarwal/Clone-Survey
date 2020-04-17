package com.nucleus.finnone.pro.lov;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

public class LOVFilterVO {

	private String filterColumnName;
	private int queryFilterClause=3;
	private List<Long> longTypeFilterColumnValues;
	private List<String> stringTypeFilterColumnValues;
	private List<Character> characterTypeFilterColumnValues;
	private List<Boolean> booleanTypeFilterColumnValues;
	private List<Integer> integerTypeFilterColumnValues;
	private List<BigDecimal> bigDecimalTypeFilterColumnValues;
	private List<Date> dateTypeFilterColumnValues;
	
	public String getFilterColumnName() {
		return filterColumnName;
	}
	public void setFilterColumnName(String filterColumnName) {
		this.filterColumnName = filterColumnName;
	}
	public List<Object> getFilterColumnValues() {
		ArrayList<Object> filterColumnValues = new ArrayList<Object>();
		if(hasElements(longTypeFilterColumnValues)){
			for(Long longValue:longTypeFilterColumnValues){
				filterColumnValues.add(longValue);
			}
		}
		if(hasElements(stringTypeFilterColumnValues)){
			for(String stringValue:stringTypeFilterColumnValues){
				filterColumnValues.add(stringValue);
			}
		}
		if(hasElements(characterTypeFilterColumnValues)){
			for(Character charValue:characterTypeFilterColumnValues){
				filterColumnValues.add(charValue);
			}
		}
		if(hasElements(booleanTypeFilterColumnValues)){
			for(Boolean booleanValue:booleanTypeFilterColumnValues){
				filterColumnValues.add(booleanValue);
			}
		}
		if(hasElements(integerTypeFilterColumnValues)){
			for(Integer integerValue:integerTypeFilterColumnValues){
				filterColumnValues.add(integerValue);
			}
		}
		if(hasElements(bigDecimalTypeFilterColumnValues)){
			for(BigDecimal bigDecimalValue:bigDecimalTypeFilterColumnValues){
				filterColumnValues.add(bigDecimalValue);
			}
		}
		if(hasElements(dateTypeFilterColumnValues)){
			for(Date dateValue:dateTypeFilterColumnValues){
				filterColumnValues.add(dateValue);
			}
		}
		return filterColumnValues;
	}
	public List<Long> getLongTypeFilterColumnValues() {
		return longTypeFilterColumnValues;
	}
	public void setLongTypeFilterColumnValues(List<Long> longTypeFilterColumnValues) {
		this.longTypeFilterColumnValues = longTypeFilterColumnValues;
	}
	public List<String> getStringTypeFilterColumnValues() {
		return stringTypeFilterColumnValues;
	}
	public void setStringTypeFilterColumnValues(
			List<String> stringTypeFilterColumnValues) {
		this.stringTypeFilterColumnValues = stringTypeFilterColumnValues;
	}
	public List<Character> getCharacterTypeFilterColumnValues() {
		return characterTypeFilterColumnValues;
	}
	public void setCharacterTypeFilterColumnValues(
			List<Character> characterTypeFilterColumnValues) {
		this.characterTypeFilterColumnValues = characterTypeFilterColumnValues;
	}
	public List<Boolean> getBooleanTypeFilterColumnValues() {
		return booleanTypeFilterColumnValues;
	}
	public void setBooleanTypeFilterColumnValues(
			List<Boolean> booleanTypeFilterColumnValues) {
		this.booleanTypeFilterColumnValues = booleanTypeFilterColumnValues;
	}
	public List<Integer> getIntegerTypeFilterColumnValues() {
		return integerTypeFilterColumnValues;
	}
	public void setIntegerTypeFilterColumnValues(
			List<Integer> integerTypeFilterColumnValues) {
		this.integerTypeFilterColumnValues = integerTypeFilterColumnValues;
	}
	public List<BigDecimal> getBigDecimalTypeFilterColumnValues() {
		return bigDecimalTypeFilterColumnValues;
	}
	public void setBigDecimalTypeFilterColumnValues(
			List<BigDecimal> bigDecimalTypeFilterColumnValues) {
		this.bigDecimalTypeFilterColumnValues = bigDecimalTypeFilterColumnValues;
	}
	public List<Date> getDateTypeFilterColumnValues() {
		return dateTypeFilterColumnValues;
	}
	public void setDateTypeFilterColumnValues(List<Date> dateTypeFilterColumnValues) {
		this.dateTypeFilterColumnValues = dateTypeFilterColumnValues;
	}
	public int getQueryFilterClause() {
		return queryFilterClause;
	}
	public void setQueryFilterClause(int queryFilterClause) {
		this.queryFilterClause = queryFilterClause;
	}
	
		
}
