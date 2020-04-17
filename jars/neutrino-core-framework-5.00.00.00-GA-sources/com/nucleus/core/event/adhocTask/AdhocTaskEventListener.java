/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.adhocTask;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationListener;

import com.nucleus.adhoc.AdhocServiceImpl;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Named(value = "adhocTaskEventListener")
public class AdhocTaskEventListener implements ApplicationListener<AdhocTaskEvent> {

    @Inject
    @Named("adhocService")
    private AdhocServiceImpl adhocService;

    @Override
    public void onApplicationEvent(AdhocTaskEvent event) {
        AdhocTaskEventWorker adhocTaskEventWorker = (AdhocTaskEventWorker) event.getEventWorker();
        adhocService.createAdhocTask(adhocTaskEventWorker.getTitle(), adhocTaskEventWorker.getDescription(),
                adhocTaskEventWorker.getDueDate(), adhocTaskEventWorker.getOwner(), adhocTaskEventWorker.getAssignee(),
                adhocTaskEventWorker.getTeamUri(), adhocTaskEventWorker.getPriority(), adhocTaskEventWorker.getTaskType(),
                adhocTaskEventWorker.getTaskSubType());

    }

}
