package com.nucleus.core.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.plugins.history.LoggingTriggerHistoryPlugin;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.quartz.Trigger;

@Transactional(propagation = Propagation.REQUIRED)
public class CustomHistoryPlugin extends LoggingTriggerHistoryPlugin {

    public static CustomHistoryPluginService customHistoryPluginService;

    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        super.triggerFired(trigger, context);
        if (getCustomHistoryPluginService() != null) {
            getCustomHistoryPluginService().triggerFiredService(trigger, context);
        }
    }

    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        super.triggerComplete(trigger, context, triggerInstructionCode);
        if (getCustomHistoryPluginService() != null) {
            getCustomHistoryPluginService().triggerCompletedService(trigger, context);
        }

    }

        public static CustomHistoryPluginService getCustomHistoryPluginService() {
            return customHistoryPluginService;
        }

        public static void setCustomHistoryPluginService(CustomHistoryPluginService customHistoryPluginService) {
            CustomHistoryPlugin.customHistoryPluginService = customHistoryPluginService;
        }

}
