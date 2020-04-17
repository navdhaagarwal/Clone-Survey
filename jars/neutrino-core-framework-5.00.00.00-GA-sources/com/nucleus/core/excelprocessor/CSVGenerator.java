/**
 * 
 */
package com.nucleus.core.excelprocessor;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.inject.Named;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;

@Named("csvFileGenerator")
public class CSVGenerator implements ICSVGenerator {

	private String csvSEPARATOR = FileConstants.CSV_SEPERATOR;
	private ThreadLocal<String> userSpecificCSVSeparator = new ThreadLocal<String>();

	@Override
	public StringBuilder generateCSV(ExcelMetaData metaObject, List<? extends Object> dataObject, Object headerObject)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		StringBuilder csvDataRow = new StringBuilder();
		csvDataRow = createReportHeader(metaObject, headerObject, csvDataRow);
		if (csvDataRow != null) {
			csvDataRow = createHeaderRow(metaObject, csvDataRow);
			if (csvDataRow != null) {
				csvDataRow = createdataRows(metaObject, dataObject, csvDataRow);
			}
			if (csvDataRow != null) {
				csvDataRow.append(FileConstants.END_OF_LINE);
				csvDataRow.append(FileConstants.END_OF_REPORT);
			}
		}
		return csvDataRow;
	}

	@Override
	public StringBuilder generateCSV(ExcelMetaData metaObject, List<? extends Object> dataObject, Object headerObject,
			String csvSeparator) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (notNull(csvSeparator)) {
			userSpecificCSVSeparator.set(csvSeparator);
		}
		return generateCSV(metaObject, dataObject, headerObject);
	}

	private StringBuilder createReportHeader(ExcelMetaData metaObject, Object headerObject, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		CSVCell csvCell = new CSVCell();
		StringBuilder resultingCSVDataRow = csvDataRow;
		if (metaObject != null) {
			String headerLabel = metaObject.getReportName();
			if (headerLabel != null) {
				csvDataRow.append(headerLabel);
				csvDataRow.append(FileConstants.END_OF_LINE);
			}

			if (hasElements(metaObject.getReportHeaders())) {
				for (ExcelColumn column : metaObject.getReportHeaders()) {
					headerLabel = column.getColumnLabel();
					csvDataRow.append(headerLabel);
					csvDataRow.append(
							notNull(userSpecificCSVSeparator.get()) ? userSpecificCSVSeparator.get() : csvSEPARATOR);
					resultingCSVDataRow = csvCell.createDataCell(column, headerObject, column.getColumnType(),
							csvDataRow,
							notNull(userSpecificCSVSeparator.get()) ? userSpecificCSVSeparator.get() : csvSEPARATOR);
					resultingCSVDataRow.append(FileConstants.END_OF_LINE);
				}
			}
		}
		return resultingCSVDataRow;
	}

	private StringBuilder createHeaderRow(ExcelMetaData metaObject, StringBuilder csvDataRow) {
		for (ExcelColumn excelColumn : metaObject.getExcelColumnList()) {
			csvDataRow.append(excelColumn.getColumnLabel());
			csvDataRow.append(notNull(userSpecificCSVSeparator.get()) ? userSpecificCSVSeparator.get() : csvSEPARATOR);
		}
		if (csvDataRow != null) {
			csvDataRow.append(FileConstants.END_OF_LINE);
		}
		return csvDataRow;
	}

	private StringBuilder createdataRows(ExcelMetaData metaObject, List<? extends Object> dataObject,
			StringBuilder csvDataRow) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		StringBuilder resultingCSVDataRow = csvDataRow;

		if (ValidatorUtils.hasNoElements(dataObject)) {
			resultingCSVDataRow = createDataRow(metaObject, FileConstants.NO_RECORD_FOUND, csvDataRow);
		}
		for (Object object : dataObject) {
			resultingCSVDataRow = createDataRow(metaObject, object, csvDataRow);
		}
		if (resultingCSVDataRow != null) {
			resultingCSVDataRow.append(FileConstants.END_OF_LINE);
		}
		return resultingCSVDataRow;
	}

	private StringBuilder createDataRow(ExcelMetaData metaObject, Object object, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		StringBuilder resultingCSVDataRow = csvDataRow;

		if (object.equals(FileConstants.NO_RECORD_FOUND)) {
			resultingCSVDataRow.append(FileConstants.NO_RECORD_FOUND);
		} else {
			for (ExcelColumn excelColumn : metaObject.getExcelColumnList()) {
				resultingCSVDataRow = createCell(excelColumn, object, csvDataRow);
			}
		}
		if (resultingCSVDataRow != null) {
			resultingCSVDataRow.append(FileConstants.END_OF_LINE);
		}
		return resultingCSVDataRow;
	}

	private StringBuilder createCell(ExcelColumn excelColumn, Object object, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		CSVCell csvCell = new CSVCell();
		String cellType = excelColumn.getColumnType();
		return csvCell.createDataCell(excelColumn, object, cellType, csvDataRow,
				notNull(userSpecificCSVSeparator.get()) ? userSpecificCSVSeparator.get() : csvSEPARATOR);
	}
}
