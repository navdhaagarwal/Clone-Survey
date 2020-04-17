/**
 * 
 */
package com.nucleus.core.excelprocessor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface ICSVGenerator {

	StringBuilder generateCSV(ExcelMetaData metaObject, List<? extends Object> dataObject,Object headerObject) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;
	StringBuilder generateCSV(ExcelMetaData metaObject, List<? extends Object> dataObject,Object headerObject, String csvSeparator) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;
}
