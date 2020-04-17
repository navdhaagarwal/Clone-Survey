package com.nucleus.rules.eventdefinition.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nucleus.core.event.EventDefinition;
import com.nucleus.core.event.EventTask;
import com.nucleus.rules.model.Rule;
import com.nucleus.service.BaseService;

/**
 * The Interface EventDefinitionService.
 */
public interface EventDefinitionService extends BaseService {

    /**
     * Gets the event definition by code.
     *
     * @param code the code
     * @return the event definition by code
     */
    public EventDefinition getEventDefinitionByCode(String code);

    public void sortEventTaskList(List<EventTask> eventTaskList);
    
    
    /**
     * @param eventCode
     * @return
     */
    Map<String, List> getRootContextObjectFromEventCode(String eventCode);
    
    /**
     * @param rules
     * @return
     */
    Map<String,List> getRootContextObjectFromRule(List<Rule> rules);
}
