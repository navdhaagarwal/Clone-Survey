package com.nucleus.core.formsConfiguration;

import java.util.Map;

/**
 * 
 * @author Nucleus Software Exports Limited
 * interface to support Multiple dynamic forms
 */

public interface MultipleFormData extends IDynamicForm{

    /**
     * 
     * To return map of persistent form data
     * @return
     */
    public Map<String, PersistentFormData> getFormDataMap();

    /**
     * 
     * To add persistent form data
     * @param modelName
     * @param persistentFormData
     */
    public void addToFormDataMap(String modelName, PersistentFormData persistentFormData);
}
