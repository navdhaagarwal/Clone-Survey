/**
 * 
 */
package com.nucleus.core.excelprocessor;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import com.nucleus.logging.BaseLoggers;

public class Cell {

	public HSSFCell createDataCell(HSSFRow row, HSSFCell cell, ExcelColumn excelColumn, Object object,
			HSSFSheet worksheet, String cellType)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String cellDataType = cellType.toUpperCase();
		HSSFCell cellOutput;
		switch (CellType.valueOf(cellDataType)) {
		case BIGDECIMAL:
			cellOutput = createBigDecimalValuedCell(row, cell, excelColumn, object, worksheet);
			break;
		case DATE:
			cellOutput = createDateValuedCell(row, cell, excelColumn, object, worksheet);
			break;
		case NULL:
			cellOutput = createNullValuedCell(row, cell, excelColumn, object, worksheet);
			break;
		case INT:
			cellOutput = createIntegerValuedCell(row, cell, excelColumn, object, worksheet);
			break;
		case LONG:
			cellOutput = createLongValuedCell(row, cell, excelColumn, object, worksheet);
			break;
		case TIMESTAMP:
			cellOutput = createTimestampValuedCell(row, cell, excelColumn, object, worksheet);
			break;
		case DOUBLE:
			cellOutput = createDoubleValuedCell(row, cell, excelColumn, object, worksheet);
			break;
		case STRING:
		default:
			cellOutput = createStringValuedCell(row, cell, excelColumn, object, worksheet);
		}
		return cellOutput;
	}

	public HSSFCell createBigDecimalValuedCell(HSSFRow row, HSSFCell cell, ExcelColumn excelColumn, Object object,
			HSSFSheet worksheet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			BigDecimal value;
			try {
				value = (BigDecimal) PropertyUtils.getProperty(object, dataAttribute);
				cell.setCellValue(value.doubleValue());
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(row, cell, excelColumn, object, worksheet);
			}
			worksheet.autoSizeColumn((short) excelColumn.getColumnOrder());
		}
		return cell;
	}

	public HSSFCell createIntegerValuedCell(HSSFRow row, HSSFCell cell, ExcelColumn excelColumn, Object object,
			HSSFSheet worksheet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				Integer value = (Integer) PropertyUtils.getProperty(object, dataAttribute);
				cell.setCellValue(value.doubleValue());
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(row, cell, excelColumn, object, worksheet);
			}
			worksheet.autoSizeColumn((short) excelColumn.getColumnOrder());
		}
		return cell;
	}

	public HSSFCell createDoubleValuedCell(HSSFRow row, HSSFCell cell, ExcelColumn excelColumn, Object object,
			HSSFSheet worksheet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				Double value = (Double) PropertyUtils.getProperty(object, dataAttribute);
				cell.setCellValue(value);
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(row, cell, excelColumn, object, worksheet);
			}
			worksheet.autoSizeColumn((short) excelColumn.getColumnOrder());
		}
		return cell;
	}

	public HSSFCell createLongValuedCell(HSSFRow row, HSSFCell cell, ExcelColumn excelColumn, Object object,
			HSSFSheet worksheet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				Long value = (Long) PropertyUtils.getProperty(object, dataAttribute);
				if(value != null) {
					cell.setCellValue(value.doubleValue());
				} else {
					createNullValuedCell(row, cell, excelColumn, object, worksheet);
				}
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(row, cell, excelColumn, object, worksheet);
			}
			worksheet.autoSizeColumn((short) excelColumn.getColumnOrder());
		}
		return cell;
	}

	public HSSFCell createStringValuedCell(HSSFRow row, HSSFCell cell, ExcelColumn excelColumn, Object object,
			HSSFSheet worksheet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				Object property = PropertyUtils.getProperty(object, dataAttribute);
				if(property!=null) {
					String value = property.toString();
					HSSFRichTextString rts = new HSSFRichTextString(value);
					cell.setCellValue(rts);
				} else {
					createNullValuedCell(row, cell, excelColumn, object, worksheet);
				}
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(row, cell, excelColumn, object, worksheet);
			}
			worksheet.autoSizeColumn((short) excelColumn.getColumnOrder());
		}
		return cell;
	}

	public HSSFCell createDateValuedCell(HSSFRow row, HSSFCell cell, ExcelColumn excelColumn, Object object,
			HSSFSheet worksheet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			try {
				Date value = (Date) PropertyUtils.getProperty(object, dataAttribute);
				SimpleDateFormat dateFormat = excelColumn.getDateFormat() != null
						? new SimpleDateFormat(excelColumn.getDateFormat())
						: new SimpleDateFormat(FileConstants.DATE_FORMAT);

				cell.setCellValue(new HSSFRichTextString(dateFormat.format(value)));
			} catch (RuntimeException e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(row, cell, excelColumn, object, worksheet);
			}
			worksheet.autoSizeColumn((short) excelColumn.getColumnOrder());
		}
		return cell;
	}

	public HSSFCell createTimestampValuedCell(HSSFRow row, HSSFCell cell, ExcelColumn excelColumn, Object object,
			HSSFSheet worksheet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		SimpleDateFormat dateFormat = new SimpleDateFormat(FileConstants.TIMESTAMP_FORMAT);
		if (dataAttribute != null) {
			try {
				Date value = (Date) PropertyUtils.getProperty(object, dataAttribute);
				cell.setCellValue(new HSSFRichTextString(dateFormat.format(value)));
			} catch (RuntimeException e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				createNullValuedCell(row, cell, excelColumn, object, worksheet);
			}
			worksheet.autoSizeColumn((short) excelColumn.getColumnOrder());
		}
		return cell;
	}

	public HSSFCell createNullValuedCell(HSSFRow row, HSSFCell cell, ExcelColumn excelColumn, Object object,
			HSSFSheet worksheet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String dataAttribute = excelColumn.getDataAttribute();
		if (dataAttribute != null) {
			String value = " ";
			HSSFRichTextString rts = new HSSFRichTextString(value);
			cell.setCellValue(rts);
			worksheet.autoSizeColumn((short) excelColumn.getColumnOrder());
		}
		return cell;
	}

}
