/**
 * 
 */
package com.nucleus.core.excelprocessor;

import java.util.List;

public class ExcelMetaData {

	private String reportName; // to be removed
	private String workSheetName;
	private List<ExcelColumn> excelColumnList;
	private CellStyle headerStyle;
	private List<ExcelColumn> reportHeaders;

	public List<ExcelColumn> getReportHeaders() {
		return reportHeaders;
	}

	public void setReportHeaders(List<ExcelColumn> reportHeaders) {
		this.reportHeaders = reportHeaders;
	}

	public CellStyle getHeaderStyle() {
		return headerStyle;
	}

	public void setHeaderStyle(CellStyle headerStyle) {
		this.headerStyle = headerStyle;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getWorkSheetName() {
		return workSheetName;
	}

	public void setWorkSheetName(String workSheetName) {
		this.workSheetName = workSheetName;
	}

	public List<ExcelColumn> getExcelColumnList() {
		return excelColumnList;
	}

	public void setExcelColumnList(List<ExcelColumn> excelColumnList) {
		this.excelColumnList = excelColumnList;
	}

}
