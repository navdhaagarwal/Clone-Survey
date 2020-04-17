package com.nucleus.rules.tablemetadata;

import java.io.Serializable;
import java.util.List;

public class TableJoinNodes implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String tableName;
	
	private String aliasName;
	
	private List<ColumnInfo> columns;
	
	private TableJoinNodes forward;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public List<ColumnInfo> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnInfo> columns) {
		this.columns = columns;
	}

	public TableJoinNodes getForward() {
		return forward;
	}

	public void setForward(TableJoinNodes forward) {
		this.forward = forward;
	}
	
	
}
