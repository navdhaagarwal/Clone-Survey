package com.nucleus.adhoc;

import java.util.Date;
import java.util.List;

import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

import com.nucleus.user.UserInfo;

/**
 * The Interface AdhocService.
 *
 * @author Nucleus Software Exports Limited
 * Interface to expose methods for Adhoc Services.
 * create adhoc task
 * get task based on id
 * complete adhoc task
 * assign adhoc test
 */

public interface AdhocService {

    /**
     * Creates the adhoc task.
     *
     * @param adhocForm the adhoc form
     */

    public void createAdhocTask(AdhocForm adhocForm);

    /**
     * Gets the task for task id.
     *
     * @param taskId the task id
     * @return the task for task id
     */

    public Task getTaskForTaskId(String taskId);

    /**
     * Resolve adhoc task.
     *
     * @param taskId the task id
     */

    public void resolveAdhocTask(String taskId);

    /**
     * Complete adhoc task.
     *
     * @param taskId the task id
     */
    public void completeAdhocTask(String taskId);

    /**
     * Gets the adhoc tasks.
     *
     * @return the adhoc tasks
     */

    public List<Task> getAdhocTasks();

    /**
     * Adds the comment to adhoc task.
     *
     * @param taskId the task id
     * @param comment the comment
     */
    public void addCommentToAdhocTask(String taskId, String comment);

    /**
     * Gets the comments for adhoc task.
     *
     * @param taskId the task id
     * @return the comments for adhoc task
     */

    public List<Comment> getCommentsForAdhocTask(String taskId);

    /**
     * Gets the task types by authority.
     *
     * @param authCodes the auth codes
     * @return the task types by authority
     */

    public List<AdhocTaskType> getTaskTypesByAuthority(List<String> authCodes);

    /**
     * Gets the adhoc task for task id.
     *
     * @param taskId the task id
     * @return the adhoc task for task id
     */

    public AdhocTask getAdhocTaskForTaskId(String taskId);

    /**
     * Gets the adhoc task for adhoc task id.
     *
     * @param adhocTaskId the adhoc task id
     * @return the adhoc task for adhoc task id
     */
    public AdhocTask getAdhocTaskForAdhocTaskId(Long adhocTaskId);

    /**
     * Creates the adhoc task.
     *
     * @param name the name
     * @param description the description
     * @param dueDate the due date
     * @param ownerUri the owner uri
     * @param assigneeUri the assignee uri
     * @param teamUri the team uri
     * @param priority the priority
     * @param adhocTaskType the adhoc task type
     * @param adhocTaskSubType the adhoc task sub type
     */

    public void createAdhocTask(String name, String description, Date dueDate, String ownerUri, String assigneeUri,
            String teamUri, Integer priority, AdhocTaskType adhocTaskType, AdhocTaskSubType adhocTaskSubType);

    /**
     * Assign adhoc task.
     *
     * @param taskId the task id
     * @param assigneeUri the assignee uri
     */
    public void assignAdhocTask(String taskId, String assigneeUri);

    /**
     * Gets the adhoc tasks for pool.
     *
     * @param userInfo the user info
     * @return the adhoc tasks for pool
     */

    public List<Task> getAdhocTasksForPool(UserInfo userInfo);

    /**
     * Gets the adhoc tasks for team lead.
     *
     * @param userInfo the user info
     * @return the adhoc tasks for team lead
     */
    public List<Task> getAdhocTasksForTeamLead(UserInfo userInfo);

    /**
     * Gets the adhoc tasks for assignee.
     *
     * @param assigneeUri the assignee uri
     * @return the adhoc tasks for assignee
     */

    public List<Task> getAdhocTasksForAssignee(String assigneeUri);

    /**
     * Gets the all adhoc tasks for owner.
     *
     * @param userInfo the user info
     * @return the all adhoc tasks for owner
     */
    public List<Task> getAllAdhocTasksForOwner(UserInfo userInfo);

    /**
     * Gets the assigned adhoc tasks for owner.
     *
     * @param userInfo the user info
     * @return the assigned adhoc tasks for owner
     */

    public List<Task> getAssignedAdhocTasksForOwner(UserInfo userInfo);

    /**
     * Gets the assigned adhoc tasks for involved user.
     *
     * @param userInfo the user info
     * @return the assigned adhoc tasks for involved user
     */
    public List<Task> getAssignedAdhocTasksForInvolvedUser(UserInfo userInfo);

    /**
     * Gets the all adhoc tasks for involved user.
     *
     * @param userInfo the user info
     * @return the all adhoc tasks for involved user
     */

    public List<Task> getAllAdhocTasksForInvolvedUser(UserInfo userInfo);

}
