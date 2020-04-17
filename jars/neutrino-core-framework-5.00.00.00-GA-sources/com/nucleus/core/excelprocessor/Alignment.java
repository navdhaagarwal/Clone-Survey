package com.nucleus.core.excelprocessor;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;

public enum Alignment {

	RIGHT(HSSFCellStyle.ALIGN_RIGHT), LEFT(HSSFCellStyle.ALIGN_LEFT), CENTER(HSSFCellStyle.ALIGN_CENTER),
	JUSTIFY(HSSFCellStyle.ALIGN_JUSTIFY);

	private short enumValue = 0;

	Alignment(short flag) {
		this.enumValue = flag;
	}
}
