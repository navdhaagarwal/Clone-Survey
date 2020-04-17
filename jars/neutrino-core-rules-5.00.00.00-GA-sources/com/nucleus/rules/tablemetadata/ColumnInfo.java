package com.nucleus.rules.tablemetadata;

import java.io.Serializable;

public class ColumnInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String tableName;
	private String columnName;
	private int dataType;
	private String helpDesc;
	
	public ColumnInfo(String columnName, int dataType) {
		super();
		this.columnName = columnName;
		this.dataType = dataType;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	public String getHelpDesc() {
		return helpDesc;
	}
	public void setHelpDesc(String helpDesc) {
		this.helpDesc = helpDesc;
	}
	
	
}
