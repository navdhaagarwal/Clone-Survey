package com.nucleus.activiti.spring.config;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.DefaultHistoryManager;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.persistence.entity.CommentEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.persistence.entity.HistoricFormPropertyEntity;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CasDefaultHistoryManager extends AbstractManager implements HistoryManager {

    private static Logger log = LoggerFactory.getLogger(DefaultHistoryManager.class.getName());
    private HistoryLevel historyLevel;

    public CasDefaultHistoryManager() {
        this.historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    }

    public boolean isHistoryLevelAtLeast(HistoryLevel level) {
        if (log.isDebugEnabled()) {
            log.debug("Current history level: {}, level required: {}", this.historyLevel, level);
        }

        return this.historyLevel.isAtLeast(level);
    }

    public boolean isHistoryEnabled() {
        if (log.isDebugEnabled()) {
            log.debug("Current history level: {}", this.historyLevel);
        }
        return (!(this.historyLevel.equals(HistoryLevel.NONE)));
    }

    public void recordProcessInstanceEnd(String processInstanceId, String deleteReason, String activityId) {
        return;
    }

    public void recordProcessInstanceNameChange(String processInstanceId, String newName) {
        return;
    }

    public void recordProcessInstanceStart(ExecutionEntity processInstance) {
        return;
    }

    public void recordSubProcessInstanceStart(ExecutionEntity parentExecution, ExecutionEntity subProcessInstance) {
        return;
    }

    public void recordActivityStart(ExecutionEntity executionEntity) {
        return;
    }

    public void recordActivityEnd(ExecutionEntity executionEntity) {
        return;
    }

    protected void endHistoricActivityInstance(HistoricActivityInstanceEntity historicActivityInstance) {
        return;
    }

    public void recordStartEventEnded(ExecutionEntity execution, String activityId) {
        return;
    }

    public HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution) {
        return findActivityInstance(execution, execution.getActivityId(), true);
    }

    protected HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution, String activityId,
                                                                  boolean checkPersistentStore) {
        String executionId = execution.getId();

        List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = getDbSqlSession().findInCache(HistoricActivityInstanceEntity.class);

        for (HistoricActivityInstanceEntity cachedHistoricActivityInstance : cachedHistoricActivityInstances) {
            if ((executionId.equals(cachedHistoricActivityInstance.getExecutionId())) && (activityId != null)
                    && (activityId.equals(cachedHistoricActivityInstance.getActivityId()))
                    && (cachedHistoricActivityInstance.getEndTime() == null)) {
                return cachedHistoricActivityInstance;
            }
        }

        List historicActivityInstances = null;
        if (checkPersistentStore) {
            historicActivityInstances = new HistoricActivityInstanceQueryImpl(Context.getCommandContext())
                    .executionId(executionId).activityId(activityId).unfinished().listPage(0, 1);
        }

        if ((historicActivityInstances != null) && (!(historicActivityInstances.isEmpty()))) {
            return ((HistoricActivityInstanceEntity) historicActivityInstances.get(0));
        }

        if (execution.getParentId() != null) {
            return findActivityInstance(execution.getParent(), activityId, checkPersistentStore);
        }

        return null;
    }

    public void recordExecutionReplacedBy(ExecutionEntity execution, InterpretableExecution replacedBy) {
        return;
    }

    public void recordProcessDefinitionChange(String processInstanceId, String processDefinitionId) {
        return;
    }

    public void recordTaskCreated(TaskEntity task, ExecutionEntity execution) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = new HistoricTaskInstanceEntity(task, execution);
            getDbSqlSession().insert(historicTaskInstance);
        }
    }

    public void recordTaskAssignment(TaskEntity task) {
        ExecutionEntity executionEntity = task.getExecution();
        if ((!(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY))) || (executionEntity == null))
            return;
        HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(executionEntity);
        if (historicActivityInstance != null)
            historicActivityInstance.setAssignee(task.getAssignee());
    }

    public void recordTaskClaim(String taskId) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setClaimTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
        }
    }

    public void recordTaskId(TaskEntity task) {
        if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
            ExecutionEntity execution = task.getExecution();
            if (execution != null) {
                HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(execution);
                if (historicActivityInstance != null)
                    historicActivityInstance.setTaskId(task.getId());
            }
        }
    }

    public void recordTaskEnd(String taskId, String deleteReason) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance =  getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.markEnded(deleteReason);
        }
    }

    public void recordTaskAssigneeChange(String taskId, String assignee) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setAssignee(assignee);
        }
    }

    public void recordTaskOwnerChange(String taskId, String owner) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setOwner(owner);
        }
    }

    public void recordTaskNameChange(String taskId, String taskName) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setName(taskName);
        }
    }

    public void recordTaskDescriptionChange(String taskId, String description) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession()
                    .selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setDescription(description);
        }
    }

    public void recordTaskDueDateChange(String taskId, Date dueDate) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession()
                    .selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setDueDate(dueDate);
        }
    }

    public void recordTaskPriorityChange(String taskId, int priority) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession()
                    .selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setPriority(priority);
        }
    }

    public void recordTaskCategoryChange(String taskId, String category) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession()
                    .selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setCategory(category);
        }
    }

    public void recordTaskFormKeyChange(String taskId, String formKey) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession()
                    .selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setFormKey(formKey);
        }
    }

    public void recordTaskParentTaskIdChange(String taskId, String parentTaskId) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession()
                    .selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setParentTaskId(parentTaskId);
        }
    }

    public void recordTaskExecutionIdChange(String taskId, String executionId) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession()
                    .selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setExecutionId(executionId);
        }
    }

    public void recordTaskDefinitionKeyChange(TaskEntity task, String taskDefinitionKey) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession()
                    .selectById(HistoricTaskInstanceEntity.class, task.getId());
            if (historicTaskInstance != null) {
                historicTaskInstance.setTaskDefinitionKey(taskDefinitionKey);

                if (taskDefinitionKey != null) {
                    Expression taskFormExpression = task.getTaskDefinition().getFormKeyExpression();
                    if (taskFormExpression != null) {
                        Object formValue = taskFormExpression.getValue(task.getExecution());
                        if (formValue != null)
                            historicTaskInstance.setFormKey(formValue.toString());
                    }
                }
            }
        }
    }

    public void recordTaskProcessDefinitionChange(String taskId, String processDefinitionId) {
        if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession()
                    .selectById(HistoricTaskInstanceEntity.class, taskId);
            if (historicTaskInstance != null)
                historicTaskInstance.setProcessDefinitionId(processDefinitionId);
        }
    }

    public void recordVariableCreate(VariableInstanceEntity variable) {
        if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY))
            HistoricVariableInstanceEntity.copyAndInsert(variable);
    }

    public void recordHistoricDetailVariableCreate(VariableInstanceEntity variable,
                                                   ExecutionEntity sourceActivityExecution, boolean useActivityId) {
        if (!(isHistoryLevelAtLeast(HistoryLevel.FULL))) {
            return;
        }
        HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = HistoricDetailVariableInstanceUpdateEntity
                .copyAndInsert(variable);

        if ((useActivityId) && (sourceActivityExecution != null)) {
            HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(sourceActivityExecution);
            if (historicActivityInstance != null)
                historicVariableUpdate.setActivityInstanceId(historicActivityInstance.getId());
        }
    }

    public void recordVariableUpdate(VariableInstanceEntity variable) {
        if (!(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)))
            return;
        HistoricVariableInstanceEntity historicProcessVariable = getDbSqlSession()
                .findInCache(HistoricVariableInstanceEntity.class, variable.getId());
        if (historicProcessVariable == null) {
            historicProcessVariable = Context.getCommandContext().getHistoricVariableInstanceEntityManager()
                    .findHistoricVariableInstanceByVariableInstanceId(variable.getId());
        }

        if (historicProcessVariable != null)
            historicProcessVariable.copyValue(variable);
        else
            HistoricVariableInstanceEntity.copyAndInsert(variable);
    }

    public void recordVariableRemoved(VariableInstanceEntity variable) {
        if (!(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)))
            return;
        HistoricVariableInstanceEntity historicProcessVariable = getDbSqlSession()
                .findInCache(HistoricVariableInstanceEntity.class, variable.getId());
        if (historicProcessVariable == null) {
            historicProcessVariable = Context.getCommandContext().getHistoricVariableInstanceEntityManager()
                    .findHistoricVariableInstanceByVariableInstanceId(variable.getId());
        }

        if (historicProcessVariable == null)
            return;
        Context.getCommandContext().getHistoricVariableInstanceEntityManager().delete(historicProcessVariable);
    }

    public void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create) {
        createIdentityLinkComment(taskId, userId, groupId, type, create, false);
    }

    public void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create,
                                          boolean forceNullUserId) {
        if (isHistoryEnabled()) {
            String authenticatedUserId = Authentication.getAuthenticatedUserId();
            CommentEntity comment = new CommentEntity();
            comment.setUserId(authenticatedUserId);
            comment.setType("event");
            comment.setTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
            comment.setTaskId(taskId);
            if ((userId != null) || (forceNullUserId)) {
                if (create)
                    comment.setAction("AddUserLink");
                else {
                    comment.setAction("DeleteUserLink");
                }
                comment.setMessage(new String[] { userId, type });
            } else {
                if (create)
                    comment.setAction("AddGroupLink");
                else {
                    comment.setAction("DeleteGroupLink");
                }
                comment.setMessage(new String[] { groupId, type });
            }
            (getSession(CommentEntityManager.class)).insert(comment);
        }
    }

    public void createProcessInstanceIdentityLinkComment(String processInstanceId, String userId, String groupId,
                                                         String type, boolean create) {
        createProcessInstanceIdentityLinkComment(processInstanceId, userId, groupId, type, create, false);
    }

    public void createProcessInstanceIdentityLinkComment(String processInstanceId, String userId, String groupId,
                                                         String type, boolean create, boolean forceNullUserId) {
        if (isHistoryEnabled()) {
            String authenticatedUserId = Authentication.getAuthenticatedUserId();
            CommentEntity comment = new CommentEntity();
            comment.setUserId(authenticatedUserId);
            comment.setType("event");
            comment.setTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
            comment.setProcessInstanceId(processInstanceId);
            if ((userId != null) || (forceNullUserId)) {
                if (create)
                    comment.setAction("AddUserLink");
                else {
                    comment.setAction("DeleteUserLink");
                }
                comment.setMessage(new String[] { userId, type });
            } else {
                if (create)
                    comment.setAction("AddGroupLink");
                else {
                    comment.setAction("DeleteGroupLink");
                }
                comment.setMessage(new String[] { groupId, type });
            }
            (getSession(CommentEntityManager.class)).insert(comment);
        }
    }

    public void createAttachmentComment(String taskId, String processInstanceId, String attachmentName,
                                        boolean create) {
        if (isHistoryEnabled()) {
            String userId = Authentication.getAuthenticatedUserId();
            CommentEntity comment = new CommentEntity();
            comment.setUserId(userId);
            comment.setType("event");
            comment.setTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
            comment.setTaskId(taskId);
            comment.setProcessInstanceId(processInstanceId);
            if (create)
                comment.setAction("AddAttachment");
            else {
                comment.setAction("DeleteAttachment");
            }
            comment.setMessage(attachmentName);
            (getSession(CommentEntityManager.class)).insert(comment);
        }
    }

    public void reportFormPropertiesSubmitted(ExecutionEntity processInstance, Map<String, String> properties,
                                              String taskId) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)){
            for (String propertyId : properties.keySet()) {
                String propertyValue = (String) properties.get(propertyId);
                HistoricFormPropertyEntity historicFormProperty = new HistoricFormPropertyEntity(processInstance,
                        propertyId, propertyValue, taskId);
                getDbSqlSession().insert(historicFormProperty);
            }
        }
    }

    public void recordIdentityLinkCreated(IdentityLinkEntity identityLink) {
        if ((isHistoryLevelAtLeast(HistoryLevel.AUDIT))
                && ((identityLink.getProcessInstanceId() != null) || (identityLink.getTaskId() != null))) {
            HistoricIdentityLinkEntity historicIdentityLinkEntity = new HistoricIdentityLinkEntity(identityLink);
            getDbSqlSession().insert(historicIdentityLinkEntity);
        }
    }

    public void deleteHistoricIdentityLink(String id) {
        if (isHistoryLevelAtLeast(HistoryLevel.AUDIT))
            getHistoricIdentityLinkEntityManager().deleteHistoricIdentityLink(id);
    }

    public void updateProcessBusinessKeyInHistory(ExecutionEntity processInstance) {
        return;
    }

}
