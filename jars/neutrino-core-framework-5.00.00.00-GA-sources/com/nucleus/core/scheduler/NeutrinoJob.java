package com.nucleus.core.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public abstract class NeutrinoJob extends QuartzJobBean {

    public static int           JOB_STAUS_PAUSED        = 1;
    public static int           JOB_STATUS_RUNNING      = 0;
    private static final String APPLICATION_CONTEXT_KEY = "applicationContext";

    private Logger              log                     = LoggerFactory.getLogger(this.getClass());

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public ApplicationContext getApplicationContext(JobExecutionContext context) throws Exception {
        ApplicationContext appCtx = null;
        appCtx = (ApplicationContext) context.getScheduler().getContext().get(APPLICATION_CONTEXT_KEY);
        if (appCtx == null) {
            throw new JobExecutionException("No application context available in scheduler context for key \""
                    + APPLICATION_CONTEXT_KEY + "\"");
        }
        return appCtx;
    }

}
