/**
 * 
 */
package com.nucleus.core.excelprocessor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import com.nucleus.core.excelprocessor.CellStyles.Alignment;
import com.nucleus.finnone.pro.general.constants.YesNoCharacterEnum;

@Named("excelFileGenerator")
public class ExcelGenerator implements IExcelGenerator {

	/**
	 * Method generateExcel is used to generate the Excel Format for given Object.
	 * 
	 * @param ExcelMetaData:metaObject
	 * @param List:dataObject
	 * @return HSSFWorkbook
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@Override
	public HSSFWorkbook generateExcel(ExcelMetaData metaObject, List<? extends Object> dataObject, Object headerObject)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return createWorkBook(metaObject, dataObject, headerObject);
	}

	/**
	 * Method createWorkBook is used to create workbook for a given Excel report.
	 * 
	 * @param ExcelMetaData:metaObject
	 * @param List:dataObject
	 * @return HSSFWorkbook
	 */
	protected static HSSFWorkbook createWorkBook(ExcelMetaData metaObject, List<? extends Object> dataObject,
			Object headerObject) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		HSSFWorkbook workBook = new HSSFWorkbook();
		createWorkSheet(metaObject, workBook, dataObject, headerObject);
		return workBook;
	}

	/**
	 * Method createWorkSheet is used to generate the Excel Format for given Object.
	 * 
	 * @param ExcelMetaData:metaObject
	 * @param HSSFWorkbook:workbook
	 * @param List:dataObject
	 * @return HSSFSheet
	 */
	protected static HSSFSheet createWorkSheet(ExcelMetaData metaObject, HSSFWorkbook workbook,
			List<? extends Object> dataObject, Object headerObject)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		HSSFSheet worksheet = workbook.createSheet(metaObject.getWorkSheetName());
		short rowNumber = 0;
		if (CollectionUtils.isNotEmpty(metaObject.getReportHeaders())) {
			rowNumber = createReportHeader(metaObject, workbook, worksheet, rowNumber++, headerObject);
		}
		rowNumber = createHeaderRow(metaObject, workbook, worksheet, dataObject, rowNumber);
		if (dataObject != null) {
			createDataRows(metaObject, workbook, worksheet, dataObject, rowNumber);
		}
		return worksheet;
	}

	/**
	 * Method createReportHeader is used to generate a report header row for the
	 * Excel Sheet.
	 * 
	 * @param        ExcelMetaData:metaObject
	 * @param        HSSFWorkbook:workbook
	 * @param        HSSFSheet:worksheet
	 * @param        short:rowNumber
	 * @param Object
	 * @param        short:headerObject
	 * @return short
	 */
	private static short createReportHeader(ExcelMetaData metaObject, HSSFWorkbook workbook, HSSFSheet worksheet,
			short rowNumber, Object headerObject)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		short rownum = rowNumber;
		HSSFCellStyle cellStyle = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) FileConstants.HEADER_FONT_SIZE);
		cellStyle.setFont(font);
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cellStyle.setWrapText(true);
		HSSFRow row = worksheet.createRow(rownum);
		HSSFRichTextString headerLabel = new HSSFRichTextString(metaObject.getReportName());
		HSSFCell cell = row.createCell(metaObject.getReportHeaders().get(0).getColumnOrder());
		cell.setCellValue(headerLabel);
		cell.setCellStyle(cellStyle);
		rownum++;
		row = worksheet.createRow(rownum);
		for (ExcelColumn column : metaObject.getReportHeaders()) {
			cell = row.createCell(column.getColumnOrder());
			headerLabel = new HSSFRichTextString(column.getColumnLabel());
			cell.setCellValue(headerLabel);
			// font.setFontName(FileConstants.HEADER_FONT_NAME);
			cellStyle.setFont(font);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			cellStyle.setWrapText(true);
			cell.setCellStyle(cellStyle);
			cell = row.createCell((column.getColumnOrder() + 1));
			Cell cellObject = new Cell();
			cell = cellObject.createDataCell(row, cell, column, headerObject, worksheet, column.getColumnType());
			cell.setCellStyle(setReportHeaderCellStyle(workbook, column));
			if (!(column.getSameRowFlag() != null
					&& column.getSameRowFlag().equalsIgnoreCase(YesNoCharacterEnum.YES.getEnumValue().toString()))) {
				rownum++;
				row = worksheet.createRow(rownum);
			}
		}
		return rownum;
	}

	/**
	 * Method setReportHeaderCellStyle is used to generates a add cell style to
	 * header row in the Excel Sheet.
	 * 
	 * @param HSSFWorkbook:workbook
	 * @param ExcelColumn:excelColumn
	 * @return HSSFCellStyle
	 */
	private static HSSFCellStyle setReportHeaderCellStyle(HSSFWorkbook workbook, ExcelColumn column) {
		HSSFFont font = workbook.createFont();
		HSSFCellStyle cellStyle = workbook.createCellStyle();

		if (column.getColumnStyle() != null) {
			if (column.getColumnStyle().getFontName() != null) {
				// font.setFontName(column.getColumnStyle().getFontName());
				font.setFontHeightInPoints(column.getColumnStyle().getFontSize());
				cellStyle.setFont(font);
				cellStyle.setWrapText(true);
			}
			cellStyle.setAlignment(
					CellStyles.Alignment.valueOf((column.getColumnStyle().getAlignment()).toUpperCase()).enumValue);
		}
		return cellStyle;
	}

	/**
	 * Method createHeaderRow is used to generates a grid header row for the Excel
	 * Sheet.
	 * 
	 * @param ExcelMetaData:metaObject
	 * @param HSSFWorkbook:workbook
	 * @param HSSFSheet:worksheet
	 * @param List:dataObject
	 * @param short:rowNumber
	 * @return short
	 */
	protected static short createHeaderRow(ExcelMetaData metaObject, HSSFWorkbook workbook, HSSFSheet worksheet,
			List<? extends Object> dataObject, short rowNumber)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		short rowNum = rowNumber;
		HSSFRow row = worksheet.createRow(rowNum);
		for (ExcelColumn excelColumn : metaObject.getExcelColumnList()) {
			createHeader(metaObject, excelColumn, workbook, worksheet, row);
		}
		rowNum++;
		return rowNum;
	}

	/**
	 * Method createdataRows is used to generate rows for the Excel Sheet.
	 * 
	 * @param ExcelMetaData:metaObject
	 * @param HSSFWorkbook:workbook
	 * @param HSSFSheet:worksheet
	 * @param List:dataObject
	 * @param short:rowNumber
	 * @return short
	 */
	protected static void createDataRows(ExcelMetaData metaObject, HSSFWorkbook workBook, HSSFSheet worksheet,
			List<? extends Object> dataObject, short rowNumber)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		HSSFRow row;
		short rowNum = rowNumber;
		Map<Alignment, HSSFCellStyle> theCellStyleAsPerTheAlignmentMap = CellStyles
				.getPossibleCellStylesAsPerTheAlignmentMap(workBook);
		for (Object object : dataObject) {
			row = worksheet.createRow(rowNum);
			createDataRow(metaObject, workBook, worksheet, row, dataObject, object, theCellStyleAsPerTheAlignmentMap);
			rowNum++;
		}
		int endCell = FileConstants.COLUMN_ORDER;
		if (dataObject.size() == 0) {
			row = worksheet.createRow(rowNum);
			HSSFCell cell = row.createCell(++endCell);
			worksheet.autoSizeColumn((short) (++endCell));
			cell.setCellValue(new HSSFRichTextString(FileConstants.NO_RECORD_FOUND));
			rowNum++;
		}
		if (metaObject.getExcelColumnList() != null && metaObject.getExcelColumnList().size() > 0) {
			if (metaObject.getExcelColumnList().size() % 2 == 0) {
				endCell = (metaObject.getExcelColumnList().size() / 2) - 1;
			} else {
				endCell = (metaObject.getExcelColumnList().size() / 2) - 2;
			}
		}

		HSSFCellStyle cellStyle = workBook.createCellStyle();
		row = worksheet.createRow(rowNum);
		HSSFCell cell = row.createCell(++endCell);
		worksheet.autoSizeColumn((short) (++endCell));
		HSSFRichTextString value = new HSSFRichTextString(FileConstants.END_OF_REPORT);
		cellStyle.setAlignment(CellStyles.Alignment.CENTER.enumValue);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(value);
	}

	/**
	 * Method createdataRow is used to generate a rows for the Excel Sheet.
	 * 
	 * @param ExcelMetaData:metaObject
	 * @param HSSFWorkbook:workbook
	 * @param HSSFSheet:worksheet
	 * @param List:dataObject
	 * @param short:rowNumber
	 * @return short
	 */
	protected static void createDataRow(ExcelMetaData metaObject, HSSFWorkbook workbook, HSSFSheet worksheet,
			HSSFRow row, List<? extends Object> dataObject, Object object,
			Map<Alignment, HSSFCellStyle> theCellStyleAsPerTheAlignmentMap)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		for (ExcelColumn excelColumn : metaObject.getExcelColumnList()) {
			createCell(metaObject, workbook, worksheet, row, excelColumn, object, theCellStyleAsPerTheAlignmentMap);
		}
	}

	/**
	 * Method createCell is used to generates a cell for a row in the Excel Sheet.
	 * 
	 * @param ExcelMetaData:metaObject
	 * @param HSSFWorkbook:workbook
	 * @param HSSFSheet:worksheet
	 * @param ExcelColumn:excelColumn
	 * @param Object:object
	 * @return HSSFCell
	 */
	protected static HSSFCell createCell(ExcelMetaData metaObject, HSSFWorkbook workBook, HSSFSheet worksheet,
			HSSFRow row, ExcelColumn excelColumn, Object object,
			Map<Alignment, HSSFCellStyle> theCellStyleAsPerTheAlignmentMap)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		HSSFCell cell = row.createCell(excelColumn.getColumnOrder());
		Cell cellObject = new Cell();
		String cellType = excelColumn.getColumnType();
		HSSFCellStyle cellStyle = createCellStyle(workBook, excelColumn, theCellStyleAsPerTheAlignmentMap);
		HSSFDataFormat dataFormat = workBook.createDataFormat();
		cell = cellObject.createDataCell(row, cell, excelColumn, object, worksheet, cellType);
		if ((cellType.equalsIgnoreCase(FileConstants.BIG_DECIMAL) || cellType.equalsIgnoreCase(FileConstants.INTEGER))
				&& excelColumn.getColumnAmountFormat() != null) {
			cellStyle.setDataFormat(dataFormat.getFormat(excelColumn.getColumnAmountFormat()));
		}
		cell.setCellStyle(cellStyle);
		return cell;
	}

	/**
	 * Method createHeader is used to generates a cell for a header row in the Excel
	 * Sheet.
	 * 
	 * @param ExcelMetaData:metaObject
	 * @param ExcelColumn:header
	 * @param HSSFWorkbook:workbook
	 * @param HSSFSheet:worksheet
	 * @param ExcelColumn:excelColumn
	 * @return void
	 */
	protected static void createHeader(ExcelMetaData metaObject, ExcelColumn header, HSSFWorkbook workBook,
			HSSFSheet worksheet, HSSFRow row) {
		HSSFCellStyle cellStyle = workBook.createCellStyle();
		HSSFFont font = workBook.createFont();
		if (metaObject.getHeaderStyle().getFontSize() != 0) {
			font.setFontHeightInPoints(metaObject.getHeaderStyle().getFontSize());
		}

		font.setFontName(metaObject.getHeaderStyle().getFontName());
		font.setBold(Boolean.TRUE);

		cellStyle.setFont(font);
		cellStyle.setWrapText(true);
		if (metaObject.getHeaderStyle().getBackgroundColor() != 0) {
			cellStyle.setFillForegroundColor(metaObject.getHeaderStyle().getBackgroundColor());
		} else {
			cellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		}
		cellStyle.setAlignment(
				Alignment.valueOf(StringUtils.upperCase(metaObject.getHeaderStyle().getAlignment())).enumValue);
		cellStyle.setVerticalAlignment(ExtendedFormatRecord.VERTICAL_TOP);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setBorderBottom(ExtendedFormatRecord.THIN);
		cellStyle.setBorderTop(ExtendedFormatRecord.THIN);
		cellStyle.setBorderLeft(ExtendedFormatRecord.THIN);
		cellStyle.setBorderRight(ExtendedFormatRecord.THIN);
		HSSFCell cell = row.createCell(header.getColumnOrder());
		row.setHeightInPoints(FileConstants.CELL_HEIGHT);
		worksheet.autoSizeColumn((short) header.getColumnOrder());
		HSSFRichTextString rts = new HSSFRichTextString(header.getColumnLabel());
		cell.setCellValue(rts);
		cell.setCellStyle(cellStyle);

	}

	/**
	 * Method createCellStyle is used to create cell style for each cell of excel
	 * sheet.
	 * 
	 * @param HSSFWorkbook:workBook
	 * @param CellStyle:style
	 * @return HSSFCellStyle
	 */
	protected static HSSFCellStyle createCellStyle(HSSFWorkbook workBook, ExcelColumn excelColumn,
			Map<Alignment, HSSFCellStyle> theCellStyleAsPerTheAlignmentMap) {
		HSSFCellStyle cellStyle;
		CellStyles cellStyles = new CellStyles();
		if (theCellStyleAsPerTheAlignmentMap.containsKey(
				CellStyles.Alignment.valueOf((excelColumn.getColumnStyle().getAlignment()).toUpperCase()))) {
			cellStyle = theCellStyleAsPerTheAlignmentMap
					.get(CellStyles.Alignment.valueOf((excelColumn.getColumnStyle().getAlignment()).toUpperCase()));
		} else {
			cellStyle = cellStyles.createCellStyle(workBook, excelColumn);
		}
		cellStyle.setBorderBottom(ExtendedFormatRecord.THIN);
		cellStyle.setBorderTop(ExtendedFormatRecord.THIN);
		cellStyle.setBorderLeft(ExtendedFormatRecord.THIN);
		cellStyle.setBorderRight(ExtendedFormatRecord.THIN);
		return cellStyle;
	}

}
