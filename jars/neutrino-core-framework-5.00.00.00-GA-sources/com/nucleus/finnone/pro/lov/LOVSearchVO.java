package com.nucleus.finnone.pro.lov;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import com.nucleus.finnone.pro.base.utility.SpecialCharacterDecoder;

public class LOVSearchVO {

	private Map<String, Object> searchMap;
	private List<LOVFilterVO> filterVoList;
	private Integer iDisplayStart; 
	private Integer iDisplayLength; 
	private Integer sortCol; 
	private String sortDir;
	private Class entityClass;
	private List<String> sortedElements;
	
	public Class getEntityClass() {
		return entityClass;
	}
	public void setEntityClass(Class entityClass) {
		this.entityClass = entityClass;
	}
	public Map<String, Object> getSearchMap() {
	    if(notNull(searchMap)){
    	    	for(Iterator<Map.Entry<String, Object>> entrySet = searchMap.entrySet().iterator();entrySet.hasNext();){
    	    	    Entry<String,Object> searchMapKeyValue = entrySet.next();
    	    	    Object searchValue = notNull(searchMapKeyValue.getValue())?SpecialCharacterDecoder.decodeParameterValue(searchMapKeyValue.getValue().toString())
    									      :searchMapKeyValue.getValue();
    	    	    searchMapKeyValue.setValue(searchValue);
    	    	}
	    }
	    return searchMap;
	}
	public void setSearchMap(Map<String, Object> searchMap) {
		this.searchMap = searchMap;
	}
	public List<LOVFilterVO> getFilterVoList() {
		return filterVoList;
	}
	public void setFilterVoList(List<LOVFilterVO> filterVoList) {
		this.filterVoList = filterVoList;
	}
	public Integer getiDisplayStart() {
		return iDisplayStart;
	}
	public void setiDisplayStart(Integer iDisplayStart) {
		this.iDisplayStart = iDisplayStart;
	}
	public Integer getiDisplayLength() {
		return iDisplayLength;
	}
	public void setiDisplayLength(Integer iDisplayLength) {
		this.iDisplayLength = iDisplayLength;
	}
	public Integer getSortCol() {
		return sortCol;
	}
	public void setSortCol(Integer sortCol) {
		this.sortCol = sortCol;
	}
	public String getSortDir() {
		return sortDir;
	}
	public void setSortDir(String sortDir) {
		this.sortDir = sortDir;
	}
	public List<String> getSortedElements() {
		return sortedElements;
	}
	public void setSortedElements(List<String> sortedElements) {
		this.sortedElements = sortedElements;
	}
	
}
