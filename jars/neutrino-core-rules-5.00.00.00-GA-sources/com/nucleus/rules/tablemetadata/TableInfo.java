package com.nucleus.rules.tablemetadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

public class TableInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String tableName;

	private List<ColumnInfo> columns;

	private List<ForeignKeyInfo> fkInfo;
	
	private String helpText;

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<ColumnInfo> getColumns() {
		return columns;
	}

	public ColumnInfo getColumnInfoByName(String columnName) {
		List<ColumnInfo> column =  columns.stream().filter((c) -> {
			return (c.getColumnName().equals(columnName) ? true : false);
		}).collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(column)){
			return column.get(0);
		}
		return null;
	}

	public void setColumns(List<ColumnInfo> columns) {
		this.columns = columns;
	}

	public List<ForeignKeyInfo> getFkInfo() {
		return fkInfo;
	}

	public void setFkInfo(List<ForeignKeyInfo> fkInfo) {
		this.fkInfo = Optional.ofNullable(this.fkInfo).orElse(new ArrayList<>());
		this.fkInfo.addAll(fkInfo);
	}

	public void addFkInfo(ForeignKeyInfo fkInfo) {
		this.fkInfo = Optional.ofNullable(this.fkInfo).orElse(new ArrayList<>());
		if (fkInfo.getSourceTable() == null) {
			fkInfo.setSourceTable(this);
		}
		this.fkInfo.add(fkInfo);
	}

	public void addColumn(ColumnInfo column) {
		this.columns = Optional.ofNullable(this.columns).orElse(new ArrayList<>());
		if (StringUtils.isEmpty(column.getTableName())) {
			column.setTableName(this.getTableName());
		}
		this.columns.add(column);
	}

	public TableInfo(String tableName) {
		super();
		this.tableName = tableName;
	}

}
