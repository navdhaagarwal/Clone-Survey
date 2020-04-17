package com.nucleus.core.excelprocessor;

public class CellHeader {
	private String columnLabel;
	private String dataAttribute;
	private int columnOrder; 
	private int columnLength;

	public int getColumnLength() {
		return columnLength;
	}

	public void setColumnLength(int columnLength) {
		this.columnLength = columnLength;
	}

	public String getColumnLabel() {
		return columnLabel;
	}

	public void setColumnLabel(String columnLabel) {
		this.columnLabel = columnLabel;
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

}
