package com.nucleus.task;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.approval.ApprovalTask;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.makerchecker.MakerCheckerApprovalFlow;
import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;

@Named(value = "platformTaskService")
public class TaskServiceImpl extends BaseServiceImpl implements TaskService {

    @Inject
    @Named(value = "entityDao")
    private EntityDao entityDao;

    @Override
    public void changeTaskAssignee(Long taskId, Long newAssigneeUserId) {
        Task persistedTask = entityDao.find(Task.class, taskId);
        changeTaskAssignee(persistedTask, newAssigneeUserId);
    }

    @Override
    public void changeTaskAssignee(Task task, Long newAssigneeUserId) {
        User user = new User(newAssigneeUserId);
        task.setAssignee(user);
        task.setLastUserAssignmentTimestamp(DateUtils.getCurrentUTCTime());
    }

    @Override
    public void createTask(Task task) {
        validateTask(task);
        entityDao.persist(task);
    }

    private void validateTask(Task task) {
        NeutrinoValidator.notNull(task, "Task cannot be null");
        // Disabling validations for the time being.
        /*if(task.getAssignee()==null){
        	throw new InvalidDataException("Assignee cannot be null for a task");
        }*/
    }

    /**
     * Will Complete the Approval Task, also complete the User task and start the
     * Work Flow again
     *   
     * @param task
     * @param actionTaken
     */
    public void completeApprovalTask(Task task, String actionTaken) {

        if (!(task instanceof ApprovalTask)) {
            return;
        }
        ApprovalTask approvalTask = (ApprovalTask) task;
        approvalTask.setActionTaken(actionTaken);
        approvalTask.setTaskStatus(TaskStatus.COMPLETED);
        task.setCompletionDate(DateUtils.getCurrentUTCTime());

        entityDao.persist(task);

        // TODO: Complete the respective UserTask and invoke the WorkFlow.
    }

    @Override
    public List<Task> getTasksForUser(int taskStatus, Long userId) {
    	NamedQueryExecutor<Task> queryExecutor=new NamedQueryExecutor<Task>("Task.getAllTasksForUserAndTaskStatus");
    	queryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        return entityDao.executeQuery(queryExecutor.addParameter(
                "userId", userId).addParameter("taskStatus", taskStatus));
    }

    @Override
    public List<Task> getAllTasksForUser(Long userId) {
    	NamedQueryExecutor<Task> queryExecutor=new NamedQueryExecutor<Task>("Task.getAllTasksForUser");
        queryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        return entityDao
                .executeQuery(queryExecutor.addParameter("userId", userId));
    }

    @Override
    public List<Task> getAllTasks() {
        return entityDao.findAll(Task.class);

    }

    @Override
	public List<Task> getPendingTasksForWorklow(MakerCheckerApprovalFlow approvalFlow) {
        return entityDao.executeQuery(new NamedQueryExecutor<Task>("Task.getPendingTasksForWorklow").addParameter(
                "approvalFlowReference", approvalFlow).addParameter("taskStatus", TaskStatus.PENDING));
    }

    @Override
    public List<BaseMasterEntity> getEntitiesForUserTaskList(Long userId, int taskStatus) {
        List<BaseMasterEntity> bmeList = new ArrayList<BaseMasterEntity>();
        List<Object> taskAndUnapprovedEntityDataList = getLastChangeTrailFromWorkflowForUserTask(userId, taskStatus);
        if (taskAndUnapprovedEntityDataList != null && taskAndUnapprovedEntityDataList.size() > 0) {
            for (Object taskAndUnapprovedEntityDataObj : taskAndUnapprovedEntityDataList) {
                Object[] taskAndUnapprovedEntityDataArr = (Object[]) taskAndUnapprovedEntityDataObj;
                UnapprovedEntityData unapprovedEntityData = (UnapprovedEntityData) taskAndUnapprovedEntityDataArr[0];
                ApprovalTask approvalTask = (ApprovalTask) taskAndUnapprovedEntityDataArr[1];
                BaseMasterEntity bma;
                if (null != unapprovedEntityData.getChangedEntityId()) {
                    // Newly created or edited
                    bma = entityDao.get(unapprovedEntityData.getChangedEntityId());
                } else {
                    // edited or marked for deletion.
                    bma = entityDao.get(unapprovedEntityData.getOriginalEntityId());
                }
                bma.addProperty("actions", approvalTask.getActions());
                bmeList.add(bma);
            }
        }
        return bmeList;
    }

    private List<Object> getLastChangeTrailFromWorkflowForUserTask(Long userId, int taskStatus) {
    	NamedQueryExecutor<Object> queryExecutor=new NamedQueryExecutor<Object>("Task.getLastChangeTrailFromWorkflowForUserTask");
        queryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
  		return (List<Object>) entityDao.executeQuery(queryExecutor.addParameter("userId", userId).addParameter("taskStatus",
                taskStatus));
    }

}