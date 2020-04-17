package com.nucleus.core.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;

public interface CustomHistoryPluginService {

    public abstract void triggerCompletedService(Trigger trigger, JobExecutionContext context);

    public abstract void triggerFiredService(Trigger trigger, JobExecutionContext context);

}