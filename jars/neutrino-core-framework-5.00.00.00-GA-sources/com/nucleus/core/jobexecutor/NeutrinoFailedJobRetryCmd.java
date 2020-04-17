
package com.nucleus.core.jobexecutor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.calendar.DurationHelper;
import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.JobAddedNotification;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sainik Kumar Singhal
 * 
 * This is an customized version of org.activiti.engine.impl.cmd.JobRetryCmd.
 *  changes done in standard implementation-
 *  1. Job retry is set to 0 if greater than 0 and decremented otherwise
 *  2. For failed job due date is always the current date/timestamp
 * 
 */

public class NeutrinoFailedJobRetryCmd implements Command<Object> {

    private static final Logger log = LoggerFactory.getLogger(NeutrinoFailedJobRetryCmd.class.getName());

    protected String            jobId;
    protected Throwable         exception;

    public NeutrinoFailedJobRetryCmd(String jobId, Throwable exception) {
        this.jobId = jobId;
        this.exception = exception;
    }
    
    @Override
    public Object execute(CommandContext commandContext) {
        JobEntity job = commandContext.getJobEntityManager().findJobById(jobId);
        if (job == null) {
            return null;
        }

        ActivityImpl activity = getCurrentActivity(commandContext, job);
        ProcessEngineConfiguration processEngineConfig = commandContext.getProcessEngineConfiguration();

        if (activity == null || activity.getFailedJobRetryTimeCycleValue() == null) {
            log.debug(
                    "activitiy or FailedJobRetryTimerCycleValue is null in job " + jobId + "'. only decrementing retries.");
            job.setRetries(calculateJobRetries(job));
            job.setLockOwner(null);
            job.setLockExpirationTime(null);
            if (job.getDuedate() == null) {
                // add wait time for failed async job
                job.setDuedate(new Date());
            } else {
                // add default wait time for failed job
                job.setDuedate(new Date());
            }

        } else {
            String failedJobRetryTimeCycle = activity.getFailedJobRetryTimeCycleValue();
            try {
                DurationHelper durationHelper = new DurationHelper(failedJobRetryTimeCycle, processEngineConfig.getClock());
                job.setLockOwner(null);
                job.setLockExpirationTime(null);
                job.setDuedate(new Date());

                if (job.getExceptionMessage() == null) {  // is it the first exception
                    log.debug("Applying JobRetryStrategy '" + failedJobRetryTimeCycle + "' the first time for job "
                            + job.getId() + " with " + durationHelper.getTimes() + " retries");
                    // then change default retries to the ones configured
                    // job.setRetries(calculateJobRetries(job));

                } else {
                    log.debug("Decrementing retries of JobRetryStrategy '" + failedJobRetryTimeCycle + "' for job "
                            + job.getId());
                }
                job.setRetries(calculateJobRetries(job));

            } catch (Exception e) {
                throw new ActivitiException("failedJobRetryTimeCylcle has wrong format:" + failedJobRetryTimeCycle,
                        exception);
            }
        }

        if (exception != null) {
            job.setExceptionMessage(exception.getMessage());
            job.setExceptionStacktrace(getExceptionStacktrace());
        }

        // Dispatch both an update and a retry-decrement event
        ActivitiEventDispatcher eventDispatcher = commandContext.getEventDispatcher();
        if (eventDispatcher.isEnabled()) {
            eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, job));
            eventDispatcher
                    .dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_RETRIES_DECREMENTED, job));
        }

        if (processEngineConfig.isAsyncExecutorEnabled() == false) {
            JobExecutor jobExecutor = processEngineConfig.getJobExecutor();
            JobAddedNotification messageAddedNotification = new JobAddedNotification(jobExecutor);
            TransactionContext transactionContext = commandContext.getTransactionContext();
            transactionContext.addTransactionListener(TransactionState.COMMITTED, messageAddedNotification);
        }

        return null;
    }

    protected Date calculateDueDate(CommandContext commandContext, int waitTimeInSeconds, Date oldDate) {
        Calendar newDateCal = new GregorianCalendar();
        if (oldDate != null) {
            newDateCal.setTime(oldDate);

        } else {
            newDateCal.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
        }

        newDateCal.add(Calendar.SECOND, waitTimeInSeconds);
        return newDateCal.getTime();
    }

    private ActivityImpl getCurrentActivity(CommandContext commandContext, JobEntity job) {
        String type = job.getJobHandlerType();
        ActivityImpl activity = null;

        if (TimerExecuteNestedActivityJobHandler.TYPE.equals(type)
                || TimerCatchIntermediateEventJobHandler.TYPE.equals(type)) {
            ExecutionEntity execution = fetchExecutionEntity(commandContext, job.getExecutionId());
            if (execution != null) {
                activity = execution.getProcessDefinition().findActivity(job.getJobHandlerConfiguration());
            }
        } else if (TimerStartEventJobHandler.TYPE.equals(type)) {

            DeploymentManager deploymentManager = commandContext.getProcessEngineConfiguration().getDeploymentManager();
            if (TimerEventHandler.hasRealActivityId(job.getJobHandlerConfiguration())) {

                ProcessDefinitionEntity processDefinition = deploymentManager
                        .findDeployedProcessDefinitionById(job.getProcessDefinitionId());
                String activityId = TimerEventHandler.getActivityIdFromConfiguration(job.getJobHandlerConfiguration());
                activity = processDefinition.findActivity(activityId);

            } else {
                String processId = job.getJobHandlerConfiguration();
                if (job instanceof TimerEntity) {
                    processId = TimerEventHandler.getActivityIdFromConfiguration(job.getJobHandlerConfiguration());
                }

                ProcessDefinitionEntity processDefinition ;
                if (job.getTenantId() != null && job.getTenantId().length() > 0) {
                    processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKeyAndTenantId(processId,
                            job.getTenantId());
                } else {
                    processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKey(processId);
                }

                if (processDefinition != null) {
                    activity = processDefinition.getInitial();
                }
            }

        } else if (AsyncContinuationJobHandler.TYPE.equals(type)) {
            ExecutionEntity execution = fetchExecutionEntity(commandContext, job.getExecutionId());
            if (execution != null) {
                activity = execution.getActivity();
            }
        } else {
            // nop, because activity type is not supported
        }

        return activity;
    }

    private String getExceptionStacktrace() {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    private ExecutionEntity fetchExecutionEntity(CommandContext commandContext, String executionId) {
        return commandContext.getExecutionEntityManager().findExecutionById(executionId);
    }

    private int calculateJobRetries(JobEntity job) {

        // job must be executed only once by activiti engine
        // if retry count is greater than zero set it to zero
        // once it becomes zero keep decrementing -- so job can be handled by async workflow
        // job executor and application curing UI

        if (job.getRetries() > 0) {
            return 0;
        } else {
            return job.getRetries() - 1;
        }
    }

}
