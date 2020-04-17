/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.service;

import java.util.Map;

import com.nucleus.core.event.EventDefinition;
import com.nucleus.core.event.EventExecutionResult;
import com.nucleus.service.BaseService;

/**
 * The Interface EventExecutionService.
 *
 * @author Nucleus Software India Pvt Ltd
 */
public interface EventExecutionService extends BaseService {

    /**
     * Execute event definition by evaluating the event task list one by one  and directing them to their respective listeners
     * and returns the eventExecutionResult which contains a result map that contains the result of each 
     * execution with their respective code.
     *
     * @param eventDefinition the event definition
     * @param  eventExecutionVO to contain the extra fields as per the requirement
     * @param contextObjectMap the context object map
     * @param auditingEnabled
     * @param purgingRequired
     * @return the event execution result 
     */
   public EventExecutionResult executeEventDefinition(EventDefinition eventDefinition, Map contextObjectMap,
            EventExecutionVO eventExecutionVO,boolean auditingEnabled,boolean purgingRequired);
   
   
   /**
    * Execute event definition by evaluating the event task list one by one  and directing them to their respective listeners
    * and returns the eventExecutionResult which contains a result map that contains the result of each 
    * execution with their respective code.
    *
    * @param eventDefinition the event definition
    * @param  eventExecutionVO to contain the extra fields as per the requirement
    * @param contextObjectMap the context object map
    * @return the event execution result 
    * @deprecated (Since GA2.5, To Support configurable auditing)
    */
   @Deprecated
   EventExecutionResult executeEventDefinition(EventDefinition eventDefinition, Map contextObjectMap,
           EventExecutionVO eventExecutionVO);
    
    

    /**
     * Executes event definition by finding out event definition object using eventExecutionPoint and then calling the above service
     * Created for firing the events from controller where only eventExecutionPoint is present rather than event definition object.
     * @param eventExecutionPoint
     * @param  eventExecutionVO to contain the extra fields as per the requirement
     * @param contextObjectMap
     * @return
     */
    public EventExecutionResult fireEventExecution(String eventExecutionPoint, Map contextObjectMap,
            EventExecutionVO eventExecutionVO);

}
