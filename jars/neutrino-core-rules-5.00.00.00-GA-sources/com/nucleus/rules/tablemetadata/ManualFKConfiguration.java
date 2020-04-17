package com.nucleus.rules.tablemetadata;

import java.io.Serializable;

public class ManualFKConfiguration implements Serializable{

	private String sourceTableName;
	private String sourceColumn;
	private String referenceTableName;
	private String referenceTableColumnName;
	
	public String getSourceTableName() {
		return sourceTableName;
	}
	public void setSourceTableName(String sourceTableName) {
		this.sourceTableName = sourceTableName;
	}
	public String getSourceColumn() {
		return sourceColumn;
	}
	public void setSourceColumn(String sourceColumn) {
		this.sourceColumn = sourceColumn;
	}
	public String getReferenceTableName() {
		return referenceTableName;
	}
	public void setReferenceTableName(String referenceTableName) {
		this.referenceTableName = referenceTableName;
	}
	public String getReferenceTableColumnName() {
		return referenceTableColumnName;
	}
	public void setReferenceTableColumnName(String referenceTableColumnName) {
		this.referenceTableColumnName = referenceTableColumnName;
	}
	
	
}
