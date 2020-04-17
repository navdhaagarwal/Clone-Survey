package com.nucleus.makerchecker;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Named;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;

import com.nucleus.logging.BaseLoggers;

@Named("gridDataUtility")
public class GridDataUtility {
	
	private static String EXCEPTION_OCCURED_STRING="Exception occured while accessing nested property for column configuration ";

	public Object getColumnValueFromColumnConfiguration(Object entity,ColumnConfiguration columnConfiguration) {
		Object columnValue;
		try {
			if(columnConfiguration.getIsRegional()){
					columnValue = PropertyUtils.getNestedProperty(entity,columnConfiguration.getRegionalDataField());
	        }else{
	        	columnValue = PropertyUtils.getNestedProperty(entity,columnConfiguration.getDataField());
	        }
		} catch (IllegalAccessException e) {
			columnValue = null;
			BaseLoggers.masterDataLogger
            .error(EXCEPTION_OCCURED_STRING
                    + columnConfiguration.getTitleKey() + "' :" + e.getMessage(),e);
		} catch (InvocationTargetException e) {
			columnValue = null;
			BaseLoggers.masterDataLogger
            .error(EXCEPTION_OCCURED_STRING
                    + columnConfiguration.getTitleKey() + "' :" + e.getMessage(),e);
		} catch (NoSuchMethodException e) {
			columnValue = null;
			BaseLoggers.masterDataLogger
            .error(EXCEPTION_OCCURED_STRING
                    + columnConfiguration.getTitleKey() + "' :" + e.getMessage(),e);
		}
		catch (NestedNullException e) {
            columnValue = null;
            BaseLoggers.masterDataLogger
                    .error(EXCEPTION_OCCURED_STRING
                            + columnConfiguration.getTitleKey() + "' :" + e.getMessage(),e);
        }
		return columnValue;
	}

}
