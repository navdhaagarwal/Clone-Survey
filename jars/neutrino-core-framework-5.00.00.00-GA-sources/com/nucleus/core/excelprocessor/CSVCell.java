/**
 * 
 */
package com.nucleus.core.excelprocessor;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.logging.BaseLoggers;

public class CSVCell {

	private CellType columnType;

	private static final char TEXT_DELIMETER = '"';

	public StringBuilder createDataCell(ExcelColumn excelColumn, Object object, String cellType,
			StringBuilder csvDataRow, String csvSEPARATOR)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String cellDataType = cellType.toUpperCase();
		StringBuilder cellOutput;
		switch (columnType.valueOf(cellDataType)) {
		case BIGDECIMAL:
			cellOutput = createBigDecimalValuedCell(excelColumn, object, csvDataRow);
			break;
		case NULL:
			cellOutput = createNullValuedCell(excelColumn, object, csvDataRow);
			break;
		case INT:
			cellOutput = createIntegerValuedCell(excelColumn, object, csvDataRow);
			break;
		case LONG:
			cellOutput = createLongValuedCell(excelColumn, object, csvDataRow);
			break;
		case DOUBLE:
			cellOutput = createDoubleValuedCell(excelColumn, object, csvDataRow);
			break;
		case TIMESTAMP:
			cellOutput = createTimestampValuedCell(excelColumn, object, csvDataRow);
			break;
		case DATE:
			cellOutput = createDateValuedCell(excelColumn, object, csvDataRow);
			break;
		case STRING:
		default:
			cellOutput = createStringValuedCell(excelColumn, object, csvDataRow, csvSEPARATOR);
			break;

		case METHOD:
			cellOutput = createReturnValuedCell(excelColumn, object, csvDataRow);
			/*
			 * case METHOD: cellOutput=createReturnValuedCell(excelColumn,
			 * object,csvDataRow); break; case STRING: default :
			 * cellOutput=createStringValuedCell(excelColumn,
			 * object,csvDataRow,csvSEPARATOR); break;
			 */

		}
		cellOutput.append(csvSEPARATOR);
		return cellOutput;
	}

	public StringBuilder createBigDecimalValuedCell(ExcelColumn excelColumn, Object object, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			BigDecimal value;
			try {
				value = (BigDecimal) PropertyUtils.getProperty(object, dataAttribute);
				if (isNull(value)) {
					createNullValuedCell(excelColumn, object, csvDataRow);
				} else {
					csvDataRow.append(value);
				}
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(excelColumn, object, csvDataRow);
			}
		}
		return csvDataRow;
	}

	public StringBuilder createIntegerValuedCell(ExcelColumn excelColumn, Object object, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				Integer value = (Integer) PropertyUtils.getProperty(object, dataAttribute);
				if (isNull(value)) {
					createNullValuedCell(excelColumn, object, csvDataRow);
				} else {
					csvDataRow.append(value);
				}
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(excelColumn, object, csvDataRow);
			}
		}
		return csvDataRow;
	}

	public StringBuilder createDoubleValuedCell(ExcelColumn excelColumn, Object object, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				Double value = (Double) PropertyUtils.getProperty(object, dataAttribute);
				if (isNull(value)) {
					createNullValuedCell(excelColumn, object, csvDataRow);
				} else {
					csvDataRow.append(value);
				}
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(excelColumn, object, csvDataRow);
			}
		}
		return csvDataRow;
	}

	public StringBuilder createLongValuedCell(ExcelColumn excelColumn, Object object, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				Long value = (Long) PropertyUtils.getProperty(object, dataAttribute);
				if (isNull(value)) {
					createNullValuedCell(excelColumn, object, csvDataRow);
				} else {
					csvDataRow.append(value);
				}
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(excelColumn, object, csvDataRow);
			}
		}
		return csvDataRow;
	}

	public StringBuilder createStringValuedCell(ExcelColumn excelColumn, Object object, StringBuilder csvDataRow,
			String csvSEPARATOR) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				String value = PropertyUtils.getProperty(object, dataAttribute).toString();
				if (value != null && value.contains(csvSEPARATOR)) {
					csvDataRow.append(TEXT_DELIMETER).append(value).append(TEXT_DELIMETER);
				} else if (isNull(value)) {
					createNullValuedCell(excelColumn, object, csvDataRow);
				} else {
					value = CoreUtility.sanitize(value);
					csvDataRow.append(value);
				}

			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(excelColumn, object, csvDataRow);
			}
		}
		return csvDataRow;
	}

	public StringBuilder createReturnValuedCell(ExcelColumn excelColumn, Object object, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				String value = object.getClass().getDeclaredMethod(dataAttribute, new Class[] {})
						.invoke(object, new Object[] {}).toString();
				if (isNull(value)) {
					createNullValuedCell(excelColumn, object, csvDataRow);
				} else {
					value = CoreUtility.sanitize(value);
					csvDataRow.append(value);
				}
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(excelColumn, object, csvDataRow);
			}
		}
		return csvDataRow;
	}

	public StringBuilder createDateValuedCell(ExcelColumn excelColumn, Object object, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				Date value = (Date) PropertyUtils.getProperty(object, dataAttribute);

				if (isNull(value)) {
					createNullValuedCell(excelColumn, object, csvDataRow);
				} else {
					SimpleDateFormat dateFormat = excelColumn.getDateFormat() != null
							? new SimpleDateFormat(excelColumn.getDateFormat())
							: new SimpleDateFormat(FileConstants.DATE_FORMAT);

					csvDataRow.append(dateFormat.format(value));
				}
			} catch (RuntimeException e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(excelColumn, object, csvDataRow);
			}
		}
		return csvDataRow;
	}

	public StringBuilder createTimestampValuedCell(ExcelColumn excelColumn, Object object, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		SimpleDateFormat dateFormat = new SimpleDateFormat(FileConstants.TIMESTAMP_FORMAT);
		if (dataAttribute != null) {
			try {
				Date value = (Date) PropertyUtils.getProperty(object, dataAttribute);
				if (isNull(value)) {
					createNullValuedCell(excelColumn, object, csvDataRow);
				} else {
					csvDataRow.append(dateFormat.format(value));
				}
			} catch (RuntimeException e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(excelColumn, object, csvDataRow);
			}
		}
		return csvDataRow;
	}

	public StringBuilder createNullValuedCell(ExcelColumn excelColumn, Object object, StringBuilder csvDataRow)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			String value = StringUtils.EMPTY;
			csvDataRow.append(value);
		}
		return csvDataRow;
	}

}
