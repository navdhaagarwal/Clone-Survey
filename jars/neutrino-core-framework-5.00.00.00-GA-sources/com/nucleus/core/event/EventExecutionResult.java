/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
public class EventExecutionResult implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long     serialVersionUID = 1L;

    /** The result map. */
    protected Map<Object, Object> resultMap;

    public EventExecutionResult(Map<Object, Object> resultMap) {
        this.resultMap = resultMap;
    }

    public Map<Object, Object> getResultMap() {
        return resultMap;
    }

    /**
     * Gets the result for code.
     *
     * @param taskCode the task code
     * @return the result for code
     */
    public Object getResultForCode(String taskCode) {

        return resultMap.get(taskCode);

    }

    @SuppressWarnings("rawtypes")
    public Boolean getValidationTask() {
        if (resultMap != null) {
            Iterator iterator = resultMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (entry.getValue() instanceof Boolean) {
                    return (Boolean) entry.getValue();
                }
            }

        }
        return null;

    }

    public String getTransactionId() {
        return (String) resultMap.get(EventCode.EVENT_EXECUTION_RESULT_TRANSACTION_ID);
    }

}
