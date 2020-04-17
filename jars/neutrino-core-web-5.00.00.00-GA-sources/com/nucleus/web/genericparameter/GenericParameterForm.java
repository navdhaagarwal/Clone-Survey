package com.nucleus.web.genericparameter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class GenericParameterForm implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String code;

	private String name;

	private String description;

	private String dTYpe;

	private String            parentCode;

	private String groupId = "DEFAULT";
	
	private Boolean    notModifiable = Boolean.FALSE;
	
	private HashMap<String, Object> viewProperties;
	
	private Boolean availableOffline;
	
	private Boolean offlineFlag;

	private boolean           activeFlag;

	private Integer dType_Action_Flag;

	private Boolean  defaultFlag;

	private String defaultSelected;

	private String dtypeSimpleName;

	public Boolean getNotModifiable() {
		return notModifiable;
	}

	public void setNotModifiable(Boolean notModifiable) {
		this.notModifiable = notModifiable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getdTYpe() {
		return dTYpe;
	}

	public void setdTYpe(String dTYpe) {
		this.dTYpe = dTYpe;
	}

	public HashMap<String, Object> getViewProperties() {
		return viewProperties;
	}

	public void setViewProperties(HashMap<String, Object> viewProperties) {
		this.viewProperties = viewProperties;
	}
	
    public void addProperty(String key, Object value) {
        if (viewProperties == null) {
            this.viewProperties = new LinkedHashMap<String, Object>();
        }
        this.viewProperties.put(key, value);
    }
    public String getLogInfo(){
        String log=null;
        
        log="\nName: "+name;
        log+="\nCode: "+code;
        log+="\nDescription: "+description;
        
        return log;
    }

    public Boolean getAvailableOffline() {
      return availableOffline;
    }

    public void setAvailableOffline(Boolean availableOffline) {
      this.availableOffline = availableOffline;
    }

    public Boolean getOfflineFlag() {
      return offlineFlag;
    }

    public void setOfflineFlag(Boolean offlineFlag) {
      this.offlineFlag = offlineFlag;
    }

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}


	public boolean isActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	public Integer getdType_Action_Flag() {
		return dType_Action_Flag;
	}

	public void setdType_Action_Flag(Integer dType_Action_Flag) {
		this.dType_Action_Flag = dType_Action_Flag;
	}

	public Boolean getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(Boolean defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public String getDtypeSimpleName() {
		return dtypeSimpleName;
	}

	public void setDtypeSimpleName(String dtypeSimpleName) {
		this.dtypeSimpleName = dtypeSimpleName;
	}

	public String getDefaultSelected() {
		return defaultSelected;
	}

	public void setDefaultSelected(String defaultSelected) {
		this.defaultSelected = defaultSelected;
	}
}
