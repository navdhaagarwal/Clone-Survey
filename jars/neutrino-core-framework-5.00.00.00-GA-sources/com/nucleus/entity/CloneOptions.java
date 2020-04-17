package com.nucleus.entity;

import java.util.HashMap;
import java.util.Map;

import com.nucleus.core.exceptions.SystemException;

/**
 * Pojo to hold clone options.
 */
public class CloneOptions {

    private Map<String, CloneOption> cloneOptionMap = new HashMap<String, CloneOption>(5);

    public CloneOptions(CloneOption... cloneOptions) {
        if (cloneOptions == null || cloneOptions.length == 0) {
            throw new SystemException("CloneOptions can be only constructed with at least one clone Option");
        }
        for (CloneOption cloneOption : cloneOptions) {
            cloneOptionMap.put(cloneOption.getKey(), cloneOption);
        }
    }

    public CloneOption getCloneOption(String key) {
        return cloneOptionMap.get(key);
    }

    public String getCloneOptionAsString(String key) {
        return String.valueOf(cloneOptionMap.get(key).getValue());
    }

    public long getCloneOptionAsLong(String key) {
        return Long.valueOf(cloneOptionMap.get(key).getValue());
    }

    public double getCloneOptionAsDouble(String key) {
        return Double.valueOf(cloneOptionMap.get(key).getValue());
    }

    public boolean getCloneOptionAsBoolean(String key) {
        boolean cloneOptionFlag = false;
        CloneOption cloneOption = cloneOptionMap.get(key);
        if (cloneOption != null) {
            cloneOptionFlag = Boolean.valueOf(cloneOption.getValue());
        }
        return cloneOptionFlag;
    }

}
