/**
 * 
 */
package com.nucleus.core.excelprocessor;

public class ExcelColumn {

	private String columnCode;
	private String columnLabel;
	private String columnType;
	private String columnAmountFormat;
	private String dataAttribute;
	private int columnOrder;
	private CellStyle columnStyle;
	private String dateFormat;
	private String sameRowFlag;

	public String getColumnCode() {
		return columnCode;
	}

	public void setColumnCode(String columnCode) {
		this.columnCode = columnCode;
	}

	public String getColumnLabel() {
		return columnLabel;
	}

	public void setColumnLabel(String columnLabel) {
		this.columnLabel = columnLabel;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String getColumnAmountFormat() {
		return columnAmountFormat;
	}

	public void setColumnAmountFormat(String columnAmountFormat) {
		this.columnAmountFormat = columnAmountFormat;
	}

	public String getDataAttribute() {
		return dataAttribute;
	}

	public void setDataAttribute(String dataAttribute) {
		this.dataAttribute = dataAttribute;
	}

	public int getColumnOrder() {
		return columnOrder;
	}

	public void setColumnOrder(int columnOrder) {
		this.columnOrder = columnOrder;
	}

	public int getColumnLength() {
		return columnLength;
	}

	public void setColumnLength(int columnLength) {
		this.columnLength = columnLength;
	}

	private int columnLength;

	public CellStyle getColumnStyle() {
		return columnStyle;
	}

	public void setColumnStyle(CellStyle columnStyle) {
		this.columnStyle = columnStyle;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getSameRowFlag() {
		return sameRowFlag;
	}

	public void setSameRowFlag(String sameRowFlag) {
		this.sameRowFlag = sameRowFlag;
	}
}
