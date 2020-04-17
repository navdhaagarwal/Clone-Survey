/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.taskAssignmentMaster;

import java.util.Map;

import com.nucleus.service.BaseService;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
public interface ColumnDataHandler extends BaseService {

    @SuppressWarnings("rawtypes")
    public boolean canHandle(Class entityName, Map contextMap);

    @SuppressWarnings("rawtypes")
    public Map<String, Object> fetchData(Class entityClass);

    public Object handleData(Object value, Map contextMap);

}
