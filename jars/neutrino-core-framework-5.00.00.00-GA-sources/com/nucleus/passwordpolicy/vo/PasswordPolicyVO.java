package com.nucleus.passwordpolicy.vo;

import com.nucleus.config.persisted.vo.ValueType;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class PasswordPolicyVO {
    public static final String CONFIGURATION = "Configuration";
    public static final String PASSWORD_POLICY = "Password_Policy";
    private String type;   //Configuration/Password Policy
    private Long entityId;
    private String name;
    private String configValue;
    private String description;
    private Boolean enabled;
    private String errorCode;
    private String valueType;
    private Boolean userModifiable;
    private HashMap<String, Object> viewProperties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Boolean getUserModifiable() {
        return userModifiable;
    }

    public void setUserModifiable(Boolean userModifiable) {
        this.userModifiable = userModifiable;
    }

    public void addProperty(String key, Object value) {
        if (viewProperties == null) {
            this.viewProperties = new LinkedHashMap<String, Object>();
        }
        this.viewProperties.put(key, value);
    }

    public HashMap<String, Object> getViewProperties() {
        if (viewProperties == null) {
            viewProperties = new LinkedHashMap<String, Object>();
        }
        return viewProperties;
    }

    public void setViewProperties(HashMap<String, Object> viewProperties) {
        this.viewProperties = viewProperties;
    }
}
