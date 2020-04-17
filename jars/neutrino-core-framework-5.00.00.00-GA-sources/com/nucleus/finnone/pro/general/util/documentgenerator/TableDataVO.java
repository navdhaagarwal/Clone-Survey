package com.nucleus.finnone.pro.general.util.documentgenerator;

import java.util.List;
import java.util.Map;

public class TableDataVO {
	
	private String tableKey;
	private List<Map<String,Object>> tableData;
	public List<Map<String, Object>> getTableData() {
		return tableData;
	}
	public void setTableData(List<Map<String, Object>> tableData) {
		this.tableData = tableData;
	}
	public String getTableKey() {
		return tableKey;
	}
	public void setTableKey(String tableKey) {
		this.tableKey = tableKey;
	}	
}
