/**
 * 
 */
package com.nucleus.core.excelprocessor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public interface IExcelGenerator {

	HSSFWorkbook generateExcel(ExcelMetaData metaObject, List<? extends Object> dataObject, Object headerObject)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;
}
