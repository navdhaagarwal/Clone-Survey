package com.nucleus.task;

import java.util.List;

import com.nucleus.makerchecker.MakerCheckerApprovalFlow;
import com.nucleus.master.BaseMasterEntity;

public interface TaskService {

    public void changeTaskAssignee(Long taskId, Long newAssigneeUserId);

    public void changeTaskAssignee(Task task, Long newAssigneeUserId);

    public void createTask(Task task);

    /**
     * Returns the list of tasks for user
     * @param taskStatus The status for which tasks are to be searched see {@code TaskStatus} for list 
     * of possible task statuses
     * @param userId The userId for which the tasks are to be retrieved
     * @return List of tasks
     */
    List<Task> getTasksForUser(int taskStatus, Long userId);

    List<Task> getAllTasksForUser(Long userId);

    List<Task> getAllTasks();

    public void completeApprovalTask(Task task, String actionTaken);
    
    public List<Task> getPendingTasksForWorklow(MakerCheckerApprovalFlow approvalFlow);
    
    public List<BaseMasterEntity> getEntitiesForUserTaskList(Long userId,int taskStatus);
    
}
