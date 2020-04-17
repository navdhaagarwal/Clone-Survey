package com.nucleus.makerchecker;

import java.util.Map;

public class GridVO {
    
    private Integer                       iDisplayStart;
    
    private Integer                       iDisplayLength;
    
    private String                        sortColName;
    
    private String                        sortDir;
    
    private Map<String, Object>           searchMap;
    
    private boolean						  isEntityCountRequired;
    
    private boolean						  containsSearchEnabled;

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

    public String getSortColName() {
        return sortColName;
    }

    public void setSortColName(String sortColName) {
        this.sortColName = sortColName;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public Map<String, Object> getSearchMap() {
        return searchMap;
    }

    public void setSearchMap(Map<String, Object> searchMap) {
        this.searchMap = searchMap;
    }
    
    public boolean isEntityCountRequired() {
		return isEntityCountRequired;
	}

	public void setEntityCountRequired(boolean isEntityCountRequired) {
		this.isEntityCountRequired = isEntityCountRequired;
	}

	public boolean isContainsSearchEnabled() {
		return containsSearchEnabled;
	}

	public void setContainsSearchEnabled(boolean containsSearchEnabled) {
		this.containsSearchEnabled = containsSearchEnabled;
	}
}
