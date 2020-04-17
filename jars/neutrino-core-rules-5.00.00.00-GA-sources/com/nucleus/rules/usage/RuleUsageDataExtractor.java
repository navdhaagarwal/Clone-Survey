package com.nucleus.rules.usage;

import java.util.List;

public interface RuleUsageDataExtractor<T> {

    /**
     * 
     * Method to get data based on rule id
     * @param id
     * @return
     */

    public List<T> getData(Long id);

    /**
     * 
     * Method to get the key
     * @return
     */

    public String getKey();
}
