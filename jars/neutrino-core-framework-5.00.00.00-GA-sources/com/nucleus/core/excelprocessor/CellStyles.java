package com.nucleus.core.excelprocessor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class CellStyles {

	public enum Alignment{
		
		RIGHT(HSSFCellStyle.ALIGN_RIGHT),LEFT(HSSFCellStyle.ALIGN_LEFT),CENTER(HSSFCellStyle.ALIGN_CENTER),
		JUSTIFY(HSSFCellStyle.ALIGN_JUSTIFY);
		
		short enumValue = 0;
		
		Alignment(short flag) {
	        this.enumValue = flag;
	    }
	}
	
	private Map<Alignment,HSSFCellStyle> cellAlignmentMap;

	public Map<Alignment, HSSFCellStyle> getCellAlignmentMap() {
		return cellAlignmentMap;
	}
	
	public static Map<Alignment, HSSFCellStyle> getPossibleCellStylesAsPerTheAlignmentMap(HSSFWorkbook workBook) {
	    Map<Alignment,HSSFCellStyle> theCellStyleAsPerTheAlignmentMap = new EnumMap<Alignment,HSSFCellStyle>(Alignment.class);      
	  
	    HSSFCellStyle cellStyleRight=workBook.createCellStyle();
        cellStyleRight.setAlignment(Alignment.RIGHT.enumValue);
        cellStyleRight.setWrapText(true);
        theCellStyleAsPerTheAlignmentMap.put(Alignment.RIGHT, cellStyleRight);

        HSSFCellStyle cellStyleCenter=workBook.createCellStyle();
        cellStyleCenter.setAlignment(Alignment.CENTER.enumValue);
        cellStyleCenter.setWrapText(true);
        theCellStyleAsPerTheAlignmentMap.put(Alignment.CENTER, cellStyleCenter);
        

        HSSFCellStyle cellStyleJustify=workBook.createCellStyle();
        cellStyleJustify.setAlignment(Alignment.JUSTIFY.enumValue);
        cellStyleJustify.setWrapText(true);
        theCellStyleAsPerTheAlignmentMap.put(Alignment.JUSTIFY,cellStyleJustify);
        

        HSSFCellStyle cellStyleLeft=workBook.createCellStyle();
        cellStyleLeft.setAlignment(Alignment.LEFT.enumValue);
        cellStyleLeft.setWrapText(true);
        theCellStyleAsPerTheAlignmentMap.put(Alignment.LEFT,cellStyleLeft);
	  
	  return theCellStyleAsPerTheAlignmentMap;
	}
	
	
	public HSSFCellStyle createCellStyle(HSSFWorkbook workbook,ExcelColumn excelColumn)
	{
		HSSFCellStyle cellStyle;
		cellAlignmentMap= new HashMap<CellStyles.Alignment, HSSFCellStyle>();

		String cellAlignment=excelColumn.getColumnStyle().getAlignment().toUpperCase();
		switch(Alignment.valueOf(cellAlignment))
		{
		case RIGHT:
					cellStyle=workbook.createCellStyle();
					cellStyle.setAlignment(Alignment.RIGHT.enumValue);
					cellStyle.setWrapText(true);
					cellAlignmentMap.put(Alignment.RIGHT, cellStyle);
					break;
		case CENTER:
					cellStyle=workbook.createCellStyle();
					cellStyle.setAlignment(Alignment.CENTER.enumValue);
					cellStyle.setWrapText(true);
					cellAlignmentMap.put(Alignment.CENTER, cellStyle);
					break;
		case JUSTIFY:
					cellStyle=workbook.createCellStyle();
					cellStyle.setAlignment(Alignment.JUSTIFY.enumValue);
					cellStyle.setWrapText(true);
					cellAlignmentMap.put(Alignment.JUSTIFY,cellStyle);
					break;
		case LEFT: 
		default:
					cellStyle=workbook.createCellStyle();
					cellStyle.setAlignment(Alignment.LEFT.enumValue);
					cellStyle.setWrapText(true);
					cellAlignmentMap.put(Alignment.LEFT,cellStyle);
		}
		return cellStyle;
		
	}
	
}
