package com.nucleus.web.useradministration;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import org.apache.commons.beanutils.PropertyUtils;

import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.user.User;

public class UserGridDataComparator implements Comparator<Object> {

	String sortColName;
	String sortDir;

	private static final String NESTED_PROPERTY_EXCEPTION = "Exception occured while accessing nested property  value for column configuration.";
	private static final String SORT_DIR_ASC = "ASC";
	private static final  String SORT_DIR_DESC = "DESC";

	public UserGridDataComparator(String sortColName, String sortDir) {
		this.sortColName = sortColName;
		this.sortDir = sortDir;
	}

	@Override
	public int compare(Object o1, Object o2) {
		int sortResult =0 ;
		
		if (o1 instanceof User && o2 instanceof User) {
			sortResult = getSortingResult(o1, o2);
		}
		
		return sortResult;
	}

	private int getSortingResult(Object o1, Object o2) {
		
		int sortResult = 0;
		
		try {

			User userObject1 = (User) o1;
			User userObject2 = (User) o2;

			Object userObjectColValue1 = PropertyUtils.getNestedProperty(userObject1,sortColName);
			Object userObjectColValue2 = PropertyUtils.getNestedProperty(userObject2,sortColName);
			
			sortResult = getSortingResultBySortDirection(userObjectColValue1,userObjectColValue2,sortDir);
			
		} catch (IllegalAccessException illegalAccessException) {
			throw ExceptionBuilder.getInstance(SystemException.class,NESTED_PROPERTY_EXCEPTION,NESTED_PROPERTY_EXCEPTION).
				setOriginalException(illegalAccessException)
					.setMessage(NESTED_PROPERTY_EXCEPTION).build();
		} catch (InvocationTargetException invocationTargetException) {
			throw ExceptionBuilder.getInstance(SystemException.class,NESTED_PROPERTY_EXCEPTION,NESTED_PROPERTY_EXCEPTION).
				setOriginalException(invocationTargetException)
					.setMessage(NESTED_PROPERTY_EXCEPTION).build();
		} catch (NoSuchMethodException noSuchMethodException) {
			throw ExceptionBuilder.getInstance(SystemException.class,NESTED_PROPERTY_EXCEPTION,NESTED_PROPERTY_EXCEPTION).
				setOriginalException(noSuchMethodException)
					.setMessage(NESTED_PROPERTY_EXCEPTION).build();
		}
		
		return sortResult;
	}
	
	private int getSortingResultBySortDirection(Object value1,Object value2,String sortDir){
		
		int sortResult = 0;		
		
		if (String.valueOf(value1) != null && String.valueOf(value1).compareTo(String.valueOf(value2)) < 0) {
			if(SORT_DIR_ASC.equalsIgnoreCase(sortDir)){
				sortResult = -1;
			}else if(SORT_DIR_DESC.equalsIgnoreCase(sortDir)){
				sortResult = 1;
			}
		} else if (String.valueOf(value1) != null  && String.valueOf(value1).compareTo(String.valueOf(value2)) > 0) {
			if(SORT_DIR_ASC.equalsIgnoreCase(sortDir)){
				sortResult = 1;
			}else if(SORT_DIR_DESC.equalsIgnoreCase(sortDir)){
				sortResult = -1;
			}
		} 
		
		return sortResult;
	}
	

}
