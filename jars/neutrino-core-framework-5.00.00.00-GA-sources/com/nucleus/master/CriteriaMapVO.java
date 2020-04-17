package com.nucleus.master;

import java.util.Map;

public class CriteriaMapVO {

	private Map<String,Object> lessThanEqualClauseCriteriaMap;
	private Map<String,Object> greaterThanEqualClauseCriteriaMap;
	private Map<String,Object> equalClauseCriteriaMap;
	private Map<String,Object> lessThanClauseCriteriaMap;
	private Map<String,Object> greaterThanClauseCriteriaMap;
	private Map<String,Object> notEqualClauseCriteriaMap;

	public Map<String, Object> getLessThanEqualClauseCriteriaMap() {
		return lessThanEqualClauseCriteriaMap;
	}
	public void setLessThanEqualClauseCriteriaMap(
			Map<String, Object> lessThanEqualClauseCriteriaMap) {
		this.lessThanEqualClauseCriteriaMap = lessThanEqualClauseCriteriaMap;
	}
	public Map<String, Object> getGreaterThanEqualClauseCriteriaMap() {
		return greaterThanEqualClauseCriteriaMap;
	}
	public void setGreaterThanEqualClauseCriteriaMap(
			Map<String, Object> greaterThanEqualClauseCriteriaMap) {
		this.greaterThanEqualClauseCriteriaMap = greaterThanEqualClauseCriteriaMap;
	}
	public Map<String, Object> getEqualClauseCriteriaMap() {
		return equalClauseCriteriaMap;
	}
	public void setEqualClauseCriteriaMap(Map<String, Object> equalClauseCriteriaMap) {
		this.equalClauseCriteriaMap = equalClauseCriteriaMap;
	}
	public Map<String, Object> getLessThanClauseCriteriaMap() {
		return lessThanClauseCriteriaMap;
	}
	public void setLessThanClauseCriteriaMap(
			Map<String, Object> lessThanClauseCriteriaMap) {
		this.lessThanClauseCriteriaMap = lessThanClauseCriteriaMap;
	}
	public Map<String, Object> getGreaterThanClauseCriteriaMap() {
		return greaterThanClauseCriteriaMap;
	}
	public void setGreaterThanClauseCriteriaMap(
			Map<String, Object> moreThanClauseCriteriaMap) {
		this.greaterThanClauseCriteriaMap = moreThanClauseCriteriaMap;
	}
	public Map<String, Object> getNotEqualClauseCriteriaMap() {
		return notEqualClauseCriteriaMap;
	}
	public void setNotEqualClauseCriteriaMap(
			Map<String, Object> notEqualClauseCriteriaMap) {
		this.notEqualClauseCriteriaMap = notEqualClauseCriteriaMap;
	}
	
	
}
