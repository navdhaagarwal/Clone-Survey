package com.nucleus.core.formsConfiguration;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ParentChildForm implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String[] parentIds;
	private String[] childIds;
	private List<Map<String,String>> selectdparentIds;
	private List<Map<String,String>> selectdchildIds;
	
	
	public String[] getParentIds() {
		return parentIds;
	}
	public void setParentIds(String[] parentIds) {
		this.parentIds = parentIds;
	}
	public String[] getChildIds() {
		return childIds;
	}
	public void setChildIds(String[] childIds) {
		this.childIds = childIds;
	}
	public List<Map<String, String>> getSelectdparentIds() {
		return selectdparentIds;
	}
	public void setSelectdparentIds(List<Map<String, String>> selectdparentIds) {
		this.selectdparentIds = selectdparentIds;
	}
	public List<Map<String, String>> getSelectdchildIds() {
		return selectdchildIds;
	}
	public void setSelectdchildIds(List<Map<String, String>> selectdchildIds) {
		this.selectdchildIds = selectdchildIds;
	}
	
	
}
