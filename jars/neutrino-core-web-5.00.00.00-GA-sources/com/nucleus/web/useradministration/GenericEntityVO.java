/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.web.useradministration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.nucleus.entity.BaseEntity;

/**
 * VO to represent entity values in a map based structure.
 */
public class GenericEntityVO implements Serializable{

    private static final long serialVersionUID = 7075109431732423279L;

    private BaseEntity              entity;

    private HashMap<String, Object> properties;

    public GenericEntityVO() {
    }

    public void addProperty(String key, Object value) {
        if (properties == null) {
            this.properties = new LinkedHashMap<String, Object>();
        }
        this.properties.put(key, value);
    }

    public BaseEntity getEntity() {
        return entity;
    }

    public void setEntity(BaseEntity entity) {
        this.entity = entity;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

}
