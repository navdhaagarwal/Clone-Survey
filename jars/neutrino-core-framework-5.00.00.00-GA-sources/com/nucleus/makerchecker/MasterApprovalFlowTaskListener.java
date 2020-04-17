/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.makerchecker;

import static com.nucleus.core.common.NeutrinoComparators.CREATION_TIME_STAMP_COMPARATOR;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateTask;
import org.joda.time.DateTime;

import com.nucleus.approval.ApprovalFlow;
import com.nucleus.approval.ApprovalTask;
import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.GenericEventListener;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.process.WorkflowTaskEvent;
import com.nucleus.user.UserService;

/**
 * @author Nucleus Software India Pvt Ltd
 * This class handles the maker checker approval task event.
 */
@Named
public class MasterApprovalFlowTaskListener extends GenericEventListener implements MasterApprovalFlowConstants {

    @Inject
    @Named(value = "makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named(value = "userService")
    private UserService         userService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService   baseMasterService;

    /* (non-Javadoc) @see com.nucleus.event.EventListener#canHandleEvent(com.nucleus.event.Event) */
    @Override
    public boolean canHandleEvent(Event event) {
        if (!(event instanceof WorkflowTaskEvent)) {
            return false;
        } else {
            WorkflowTaskEvent wftEvent = (WorkflowTaskEvent) event;
            boolean isApprovalTask = (wftEvent.getWorkflowProcessDefinitionId().equalsIgnoreCase(WORKFLOW_DEFINITION_ID))
                    && (wftEvent.getWorkflowTaskId().equalsIgnoreCase(CHECKER_APPROVAL_TASK_WF_ID));
            boolean isMakerModificationTask = (wftEvent.getWorkflowProcessDefinitionId()
                    .equalsIgnoreCase(WORKFLOW_DEFINITION_ID))
                    && (wftEvent.getWorkflowTaskId().equalsIgnoreCase(MAKER_CHANGES_SEND_BACK_WF_ID));
            return isApprovalTask || isMakerModificationTask;
        }
    }

    public void handleEvent(Event event) {
        WorkflowTaskEvent wftEvent = (WorkflowTaskEvent) event;
        DelegateTask delegateTask = (DelegateTask) wftEvent.getWorkflowTaskInstance();
        ApprovalTask approvalTask = convertDelegateTaskToApprovalTask(delegateTask);
        String actions = (String) delegateTask.getVariable(WF_APPROVAL_TASK_ACTIONS_VARIABLE_KEY);
        String processInstanceId = delegateTask.getProcessInstanceId();
        if (wftEvent.getWorkflowTaskId().equalsIgnoreCase(CHECKER_APPROVAL_TASK_WF_ID)) {
            approvalTask.setActions(actions);
            makerCheckerService.createCheckerApprovalTask(approvalTask, processInstanceId);
        } else if (wftEvent.getWorkflowTaskId().equalsIgnoreCase(MAKER_CHANGES_SEND_BACK_WF_ID)) {
            approvalTask.setActions(actions);
            makerCheckerService.createEntityModificationTaskForNewMaker(approvalTask, processInstanceId);
        }
    }

    private ApprovalTask convertDelegateTaskToApprovalTask(DelegateTask delegateTask) {
        ApprovalTask approvalTask = new ApprovalTask();
        approvalTask.setWorkflowUserTaskId(delegateTask.getId());
        String assigneeUri = delegateTask.getAssignee();
        approvalTask.setMakerCheckerAssigneeUri(assigneeUri);
        if (delegateTask.getDueDate() != null) {
            approvalTask.setDueDate(new DateTime(delegateTask.getDueDate()));
        }
        EntityId approvalFlowEntityId = EntityId.fromUri((String) delegateTask.getVariable(WF_PROCESS_ENTITY_VARIABLE_KEY));
        approvalTask.setApprovalFlowReference(approvalFlowEntityId.toInstance(ApprovalFlow.class));
        approvalTask.setName(delegateTask.getName());
        approvalTask.setDescription(delegateTask.getDescription());
        updateRefUUIdToTask(approvalTask);
        return approvalTask;
    }

    private void updateRefUUIdToTask(ApprovalTask approvalTask) {
        MakerCheckerApprovalFlow approvalFlow = (MakerCheckerApprovalFlow) baseMasterService
                .getEntityByEntityId(approvalTask.getApprovalFlowReference().getEntityId());
        if (approvalFlow.getChangedEntityUri() != null) {
            String changedEntityUri = approvalFlow.getChangedEntityUri();
            EntityId entityId = EntityId.fromUri(changedEntityUri);
            BaseMasterEntity initialEntity = baseMasterService.getEntityByEntityId(entityId);
            if (initialEntity.getEntityLifeCycleData() != null) {
                approvalTask.setRefUUId(initialEntity.getEntityLifeCycleData().getUuid());
            }
        } else {
            Set<UnapprovedEntityData> unapprovedEntityDatas = approvalFlow.getChangeTrail();
            UnapprovedEntityData lastUnApprovedEntityData = Collections.max(unapprovedEntityDatas, CREATION_TIME_STAMP_COMPARATOR);
            BaseMasterEntity lastUnApprovedEntity = baseMasterService.getEntityByEntityId(lastUnApprovedEntityData
                    .getChangedEntityId());
            if (lastUnApprovedEntity.getEntityLifeCycleData() != null) {
                approvalTask.setRefUUId(lastUnApprovedEntity.getEntityLifeCycleData().getUuid());
            }
        }

    }
}
