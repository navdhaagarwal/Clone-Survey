package com.nucleus.core.event.service;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.event.EventDefinition;
import com.nucleus.core.event.EventExecutionResult;
import com.nucleus.user.UserService;

public class EventExecutionServiceDefaultImpl implements EventExecutionService {

    @Inject
    @Named(value = "userService")
    private UserService userService;

       @Override
    public EventExecutionResult fireEventExecution(String eventExecutionPoint, Map contextObjectMap,
            EventExecutionVO eventExecutionVO) {
        return executeEventDefinition(null, null, null,true,false);

    }

	@Override
	public EventExecutionResult executeEventDefinition(EventDefinition eventDefinition, Map contextObjectMap,
			EventExecutionVO eventExecutionVO, boolean auditingEnabled, boolean purgingRequired) {
		return null;
	}

	@Override
	public EventExecutionResult executeEventDefinition(EventDefinition eventDefinition, Map contextObjectMap,
			EventExecutionVO eventExecutionVO) {
		return null;
	}

}
