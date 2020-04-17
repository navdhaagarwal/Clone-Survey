package com.nucleus.process.listeners;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Transient;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nucleus.entity.EntityId;
import com.nucleus.event.EventBus;
import com.nucleus.event.EventTypes;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.event.MakerCheckerHelper;
import com.nucleus.makerchecker.MasterApprovalFlowConstants;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.process.WorkflowTaskEvent;
import com.nucleus.user.User;

/**
 * Listeners which listens to workflow user task and creates a new event out of it. Once the event is created, it will fire
 * the event using event bus. 
 */
@Named(value = "bpmnTaskListener")
public class BPMNTaskListener implements TaskListener {

    private static final long serialVersionUID = -3951055373218325937L;

    @Autowired
    private EventBus          eventBus;
    
    @Inject
    @Transient
    @Named("entityDao")
    private EntityDao                   entityDao;
    
    @Inject
    @Transient
    @Named(value = "makerCheckerHelper")
    protected MakerCheckerHelper   makerCheckerHelper;
        
    @Override
    public void notify(DelegateTask delegateTask) {

        // do nothing

        /*  String workflowDefinitionId = StringUtils.substringBefore(delegateTask.getProcessDefinitionId(), ":");
        WorkflowTaskEvent workFlowTaskEvent = new WorkflowTaskEvent().setWorkflowTaskInstance(delegateTask)
                .setWorkflowProcessDefinitionId(workflowDefinitionId).setWorkflowTaskId(delegateTask.getTaskDefinitionKey());

        eventBus.fireEvent(workFlowTaskEvent);*/

    }

    public void processApproval(DelegateTask delegateTask, String actionsSuggested, String assigneeAuthorityOrUserUri) {
        delegateTask.setVariable("actions", actionsSuggested);
        delegateTask.setAssignee(assigneeAuthorityOrUserUri);
        String workflowDefinitionId = StringUtils.substringBefore(delegateTask.getProcessDefinitionId(), ":");
        WorkflowTaskEvent workFlowTaskEvent = new WorkflowTaskEvent().setWorkflowTaskInstance(delegateTask)
                .setWorkflowProcessDefinitionId(workflowDefinitionId).setWorkflowTaskId(delegateTask.getTaskDefinitionKey());
        eventBus.fireEvent(workFlowTaskEvent);
        // Changes made by om.giri for auditing in case of reassign task to maker
        
        if(!StringUtils.isBlank(delegateTask.getTaskDefinitionKey()) && delegateTask.getTaskDefinitionKey().equals(MasterApprovalFlowConstants.MAKER_CHANGES_SEND_BACK_WF_ID))
        {
        	sendBackEventNotificationHandler(delegateTask);
        }
    }
    
    /**
	 * Added by om.giri, Added method for auditing in case of reassign task to maker
	 * @param delegateTask
	 */
    private void sendBackEventNotificationHandler(DelegateTask delegateTask)
    {
    	long reviewerId = (Long) delegateTask.getVariable("reviewerId");
        String masterEntityUri = (String) delegateTask.getVariable("masterEntityUri");
       
        delegateTask.getExecution().getVariableNames();
        EntityId userEntityId = new EntityId(User.class, reviewerId);
        BaseMasterEntity initialEntity = null;
        if (masterEntityUri != null) {
            initialEntity = entityDao.get(EntityId.fromUri(masterEntityUri));
        }
        MakerCheckerEvent event = new MakerCheckerEvent(EventTypes.MAKER_CHECKER_SEND_BACK, true, userEntityId,
            initialEntity,makerCheckerHelper.getEntityDescription(initialEntity.getClass().getSimpleName()));
        eventBus.fireEvent(event);
    }
    
    
}