package com.nucleus.rules.tablemetadata;

import java.io.Serializable;

public class ForeignKeyInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String keyName;

	private TableInfo sourceTable;
	
	private ColumnInfo sourceTableColumn;
	
	private TableInfo referenceTable;
	
	private ColumnInfo referenceTableColumn;

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	
	public TableInfo getSourceTable() {
		return sourceTable;
	}

	public void setSourceTable(TableInfo sourceTable) {
		this.sourceTable = sourceTable;
	}

	public ColumnInfo getSourceTableColumn() {
		return sourceTableColumn;
	}

	public void setSourceTableColumn(ColumnInfo sourceTableColumn) {
		this.sourceTableColumn = sourceTableColumn;
	}

	public TableInfo getReferenceTable() {
		return referenceTable;
	}

	public void setReferenceTable(TableInfo referenceTable) {
		this.referenceTable = referenceTable;
	}

	public ColumnInfo getReferenceTableColumn() {
		return referenceTableColumn;
	}

	public void setReferenceTableColumn(ColumnInfo referenceTableColumn) {
		this.referenceTableColumn = referenceTableColumn;
	}
	
	
	
}
