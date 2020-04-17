package com.nucleus.process.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.el.Expression;

import com.nucleus.logging.BaseLoggers;

/**
 * ProcessStartListner class will listen at start Workflow process event
 * 
 *  @author Nucleus Software India Pvt Ltd
 */
public class ProcessStartListener implements ExecutionListener {

    private Expression entity;

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        BaseLoggers.workflowLogger.debug("Process Instance Id started: {} with entity value {}",
                execution.getProcessInstanceId(), entity.getValue(execution));

        // TODO: hook the code to save the workflow process Id with MasterApproval Entity
    }

}
