package com.nucleus.process;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

// TODO: Auto-generated Javadoc
/**
 * BPMN Process Service class to do the process specific operations.
 *
 */
public interface BPMNProcessService {

    /**
     * Will start the workflow process with the given process ID.
     *
     * @param processDefinitionKey Process id of workflow instance
     * @param parametersMap Parameters to be passed to process engine while starting the
     * process
     * @return the string
     */
    public String startProcess(String processDefinitionKey, Map<String, Object> parametersMap);

    /**
     * Completes the workflow user task.
     *
     * @param taskId
     *            task id which is to be marked as completed
     * @param parametersMap
     *            Parameters to be passed to process engine while marking the
     *            task as completed
     */
    public void completeUserTask(String taskId, Map<String, Object> parametersMap);

    public void completeUserTask(String taskId);

    /**
     * Generates the workflow image corresponding to processInstanceId in PNG
     * format and returns it as byte array.
     *
     * @param processInstanceId The process instance id from which the workflow information
     * will be derived
     * @return the byte[]
     */
    public byte[] generateWorkflowImage(String processInstanceId);

    /**
     * Generates the workflow image corresponding to taskId in PNG format and
     * returns it as byte array.
     *
     * @param taskId The task id from which the workflow information will be
     * derived
     * @return the byte[]
     */
    byte[] generateWorkflowImageByTask(String taskId);

    /**
     * Gets the candidate group for task.
     *
     * @param taskId the task id
     * @return the candidate group for task
     */
    public String getCandidateGroupForTask(String taskId);

    /**
     * Gets the task variable.
     *
     * @param taskId the task id
     * @param variableName the variable name
     * @return the task variable
     */
    public Object getTaskVariable(String taskId, String variableName);

    /**
     * Gets the group id from identity links for task.
     *
     * @param taskId the task id
     * @return the group id from identity links for task
     */
    public String getGroupIdFromIdentityLinksForTask(String taskId);

    /**
     * Gets the tasks for variables by process definition key filtered by variable map containing process variable key and value.
     *
     * @param processDefinitionKey the process definition key
     * @param variableMap the variable map
     * @return the tasks for variables
     */
    public List<Task> getTasksForVariables(String processDefinitionKey, Map<String, Object> variableMap);

    /**
     * Gets the tasks for candidate groups by process definition key filtered by variable map containing process variable key and value.
     *
     * @param processDefinitionKey the process definition key
     * @param teamUris the team uris
     * @param variableMap the variable map
     * @return the tasks for candidate groups
     */
    public List<Task> getTasksForCandidateGroups(String processDefinitionKey, List<String> teamUris,
            Map<String, Object> variableMap);

    /**
     * Gets the tasks for  process definition key filtered by variable map containing process variable key and value.
     *
     * @param userUri the user uri
     * @param processDefinitionKey the process definition key
     * @param variableMap the variable map
     * @return the tasks for candidate groups
     */
    public List<Task> getAllTasksForProcessDef(String userUri, String processDefinitionKey, Map<String, Object> variableMap);

    /**
     * Gets the unassigned tasks by process definition key filtered by variable map containing process variable key and value.
     *
     * @param processDefinitionKey the process definition key
     * @param variableMap the variable map
     * @return the unassigned tasks
     */
    public List<Task> getUnassignedTasks(String processDefinitionKey, Map<String, Object> variableMap);

    /**
     * Gets the assigned tasks for process definition key filtered by variable map containing process variable key and value.
     *
     * @param processDefinitionKey the process definition key
     * @param assignee the assignee
     * @param variableMap the variable map
     * @return the assigned tasks for process definition key
     */
    public List<Task> getAssignedTasksForProcessDefinitionKey(String processDefinitionKey, String assignee,
            Map<String, Object> variableMap);

    /**
     * Gets the task by process id and task definition key.
     *
     * @param processInstanceId the process instance id
     * @param taskDefinitionKey the task definition key
     * @return the task by process id and task definition key
     */
    public Task getTaskByProcessIdAndTaskDefinitionKey(String processInstanceId, String taskDefinitionKey);

    /**
     * Sets the assignee.
     *
     * @param taskId the task id
     * @param userId the user id
     */
    public void setAssignee(String taskId, String userId);

    /**
     * Sets the task variable.
     *
     * @param taskId the task id
     * @param variableName the variable name
     * @param value the value
     */
    public void setTaskVariable(String taskId, String variableName, Object value);

    /**
     * Gets the process variable by task id and variable name.
     *
     * @param taskId the task id
     * @param variableName the variable name
     * @return the process variable
     */
    public Object getProcessVariable(String taskId, String variableName);

    /**
     * Sets the process variable.
     *
     * @param executionId the execution id
     * @param variableName the variable name
     * @param value the value
     */
    public void setProcessVariable(String executionId, String variableName, Object value);

    /**
     * Gets the task by task id filtered by variable map containing process variable key and value.
     *
     * @param taskId the task id
     * @param variableMap the variable map
     * @return the task by task id
     */
    public Task getTaskByTaskId(String taskId, Map<String, Object> variableMap);

    /**
     * Gets the assigned tasks by process definition key filtered by variable map containing process variable key and value.
     *
     * @param processDefinitionKey the process definition key
     * @param assignee the assignee
     * @param variableMap the variable map
     * @return the assigned tasks
     */
    public List<Task> getAssignedTasks(String processDefinitionKey, String assignee, Map<String, Object> variableMap,
            Integer displayStart, Integer displayLength);

    /**
     * Gets the assigned tasks for user filtered by variable map containing process variable key and value.
     *
     * @param assignee the assignee
     * @param variableMap the variable map
     * @return the assigned tasks for user
     */
    public List<Task> getAssignedTasksForUser(String assignee, Map<String, Object> variableMap);

    /**
     * Gets the task by process id.
     *
     * @param processInstanceId the process instance id
     * @return the task by process id
     */
    public Task getTaskByProcessId(String processInstanceId);

    /**
     * Gets variables for this task id.
     *
     * @param taskId the task id
     * @return the variables
     */
    public Map<String, Object> getVariables(String taskId);

    /**
     * Fire a signal event received by signal name, process instance id and optionally set process variables.
     *
     * @param signalName the signal name
     * @param processInstanceId the process instance id
     * @param processVars the process vars
     */
    public void signalEventReceived(String signalName, String processInstanceId, Map<String, Object> processVars);

    /**
     * Gets the executions for assignee uri.
     *
     * @param userUri the user uri
     * @return the executions for assignee uri
     */
    public List<Execution> getExecutionsForAssigneeUri(String userUri);

    /**
     * Resolve user task.
     *
     * @param taskId the task id
     */
    public void resolveUserTask(String taskId);

    /**
     * Gets the adhoc tasks.
     *
     * @return the adhoc tasks
     */
    public List<Task> getAdhocTasks();

    /**
     * Adds the comment by task id, process instance id and the message to be added.
     *
     * @param taskId the task id
     * @param processInstanceId the process instance id
     * @param message the message
     */
    public void addComment(String taskId, String processInstanceId, String message);

    /**
     * Gets the comments for task.
     *
     * @param taskId the task id
     * @return the comments for task
     */
    public List<Comment> getCommentsForTask(String taskId);

    /**
     * Gets the assigned adhoc tasks for team.
     *
     * @param teamUri the team uri
     * @return the assigned adhoc tasks for team
     */
    public List<Task> getAssignedAdhocTasksForTeam(String teamUri);

    /**
     * Gets the adhoc tasks for assignee.
     *
     * @param assigneeUri the assignee uri
     * @return the adhoc tasks for assignee
     */
    public List<Task> getAdhocTasksForAssignee(String assigneeUri);

    /**
     * Gets the unassigned adhoc tasks for team.
     *
     * @param teamUris the team uris
     * @return the unassigned adhoc tasks for team
     */
    public List<Task> getUnassignedAdhocTasksForTeam(List<String> teamUris);

    /**
     * Gets the assigned adhoc tasks for owner.
     *
     * @param ownerUri the owner uri
     * @param teamUri the team uri
     * @return the assigned adhoc tasks for owner
     */
    public List<Task> getAssignedAdhocTasksForOwner(String ownerUri, String teamUri);

    /**
     * Gets the all adhoc tasks for owner.
     *
     * @param ownerUri the owner uri
     * @return the all adhoc tasks for owner
     */
    public List<Task> getAllAdhocTasksForOwner(String ownerUri);

    /**
     * Start process by message.
     *
     * @param processId the process id
     * @param parametersMap the parameters map
     * @return the string
     */
    public String startProcessByMessage(String processId, Map<String, Object> parametersMap);

    /**
     * Gets the task list by process id.
     *
     * @param processInstanceId the process instance id
     * @return the task list by process id
     */
    public List<Task> getTaskListByProcessId(String processInstanceId);

    /**
     * Gets the tasks for loan application.
     *
     * @param appId the app id
     * @return the tasks for loan application
     */
    public List<Task> getTasksForLoanApplication(Long appId);

    /**
     * Gets the all tasks.
     *
     * @param variableMap the variable map
     * @return the all tasks
     */
    List<Task> getAllTasks(Map<String, Object> variableMap);

    /**
     * Gets the list of all approval tasks based on the name of assignee.
     *
     * @param assignee the assignee
     * @return the all approval tasks
     */
    public List<Task> getSendForApprovalTasks(String assignee);

    /**
     * Gets the applications based on name of stage.
     *
     * @param stageName the stage name
     * @return the application id's based on stage
     */
    public List<Long> getApplicationsBasedOnStage(String stageName);

    /**
     * Gets the super process instance by process id.
     *
     * @param processInstanceId the process instance id
     * @return the history process instance
     */
    //public HistoricProcessInstance getSuperProcessInstanceByProcessId(String processInstanceId);

    /**
     * Delete user task using process instance id with the reason to delete.
     *
     * @param processId the process id
     * @param deletionReason the deletion reason
     */
    public void deleteUserTask(String processId, String deletionReason);

    /**
     * Gets the tasks for candidate group list.
     *
     * @param teamUris the team uris
     * @param variableMap the variable map
     * @return the tasks for candidate group list
     */
    public List<Task> getTasksForCandidateGroupList(List<String> teamUris, Map<String, Object> variableMap);

    /**
     * Change team assignment from oldTeam to new Team for the task with given taskId .
     *
     * @param taskId the id of the task
     * @param oldTeamUri  the old team uri
     * @param newTeamUri  the new team uri
     */
    public void changeAssignedTeam(String taskId, String oldTeamUri, String newTeamUri);

    /**
     * Change current processing variable's value.
     *
     * @param executionId  the executionId
     * @param variableName the variable name
     * @param value the new value
     */
    public void changeCurrentProcessingVariable(String executionId, String variableName, Object value);

    /**
     * Gets the sub process instance by process id.
     *
     * @param processInstanceId the process instance id
     * @return the sub process instance by process id
     */
    public ProcessInstance getSubProcessInstanceByProcessId(String processInstanceId);

    /**
     * Gets the list of the candidate groups linked to the task with the given taskId.
     *
     * @param taskId the task id
     * @return the list of team uri
     */
    public List<String> getTeamUriByTaskId(String taskId);

    /**
     * Gets the list of the historic task instance with the given variables.
     *
     * @param variableMap the variable map
     * @param finishedTask the finished task. null value will bring all finished and unfinished tasks, true will get finished tasks, false will get unfinished tasks
     * @return the list of historic task instance
     */
    public List<HistoricTaskInstance> getAllHistoricTasks(Map<String, Object> variableMap, Boolean finishedTask);

    /**
     * Gets the assigned tasks for application.
     *
     * @param processDefinitionKey the process definition key
     * @param assignee the assignee
     * @param variableMap the variable map
     * @param displayStart the display start
     * @param displayLength the display length
     * @return the assigned tasks for application
     */
    public List<Task> getAssignedTasksForApplication(String processDefinitionKey, String assignee,
            Map<String, Object> variableMap, Integer displayStart, Integer displayLength);

    public List<Task> getAppTasksForCandidateGroups(String processDefinitionKey, List<String> teamUris,
            Map<String, Object> variableMap, Integer displayStart, Integer displayRecords);

    public List<Task> getAppTasksForVariables(String processDefinitionKey, Map<String, Object> variableMap,
            Integer displayStart, Integer displayRecords);

    /**
     * @param processInstanceId
     * @param variableName
     * @return
     */
    public HistoricVariableInstance getAllHistoricTasksVariables(String processInstanceId, String variableName);

    List<Task> getPaginatedTasksForVariables(String processDefinitionKey, Map<String, Object> variableMap,
            Integer displayStart, Integer displayRecords);

    List<Task> getPaginatedTasksForCandidateGroups(String processDefinitionKey, List<String> teamUris,
            Map<String, Object> variableMap, Integer displayStart, Integer displayRecords);

    public Long getLeadAssigneeTaskCount(String processDefinitionKey, String uri, Map<String, Object> variableMap);

    public Long getAppAssigneeTaskCount(String processDefinitionKey, String assignee, Map<String, Object> variableMap);

    public Long getLeadPoolTaskCount(String processDefinitionKey, List<String> teamUris, Map<String, Object> variableMap);

    public Long getAppPoolTaskCount(String processDefinitionKey, List<String> teamUris, Map<String, Object> variableMap);

    public Long getLeadTaskCount(String processDefinitionKey, Map<String, Object> variableMap);

    public Long getAppTaskCount(String processDefinitionKey, Map<String, Object> variableMap);

    public Long getAssignedTaskCount(String processDefinitionKey, String assignee, Map<String, Object> variableMap);

    public List<Task> getAssignedTasksForUserAndDueDate(String assignee, Map<String, Object> variableMap, Date expiryDate,
            Integer totalDisplayRecords);

    /**
     * Gets the list of the historic task instance with the given variables.
     *
     * @param variableMap the variable map
     * @param finishedTask the finished task. null value will bring all finished and unfinished tasks, true will get finished tasks, false will get unfinished tasks
     * @param userURI the URI of the user
     * @return the list of historic task instance
     */

    List<HistoricTaskInstance> getAllHistoricTasksforUser(Map<String, Object> variableMap, Boolean finishedTask,
            String userURI);

    List<Execution> getApplicationExecutionsForAssigneeUri(String userUri);

    /**
     * Gets the all lead tasks within time span.
     *
     * @param variableMap the variable map
     * @param startDate the start date
     * @param endDate the end date
     * @param displayStart the display start
     * @param displayRecords the display records
     * @return the all lead tasks within time span
     */
    public List<Task> getAllLeadTasksWithinTimeSpan(Map<String, Object> variableMap, Date startDate, Date endDate,
            Integer displayStart, Integer displayRecords);

    /**
     * Gets the all lead tasks within time span count.
     *
     * @param variableMap the variable map
     * @param startDate the start date
     * @param endDate the end date
     * @return the count of all lead tasks within time span count
     */
    Long getAllLeadTasksWithinTimeSpanCount(Map<String, Object> variableMap, Date startDate, Date endDate);

    List<String> getAllHistoricTasksVariables(Map<String, Object> variableMap);

    List<Task> getAssignedTasks(String assignee, Map<String, Object> variableMap, Integer displayStart, Integer displayLength);

    List<Task> getPaginatedTasksForCandidateGroups(List<String> teamUris, Map<String, Object> variableMap,
            Integer displayStart, Integer displayRecords);

    public abstract Task getTaskIncludingProcessVariables(String taskId);

    public abstract String getSuperProcessInstanceId(String processInstanceId);

    public ProcessInstance getProcessInstanceId(String processInstanceId);

    public List<ProcessInstance> getPaginatedProcessInstance(Map<String, Object> variableMap);

    public List<Task> getAllTasksIncludeVariables(Map<String, Object> variableMap);

    public List<Task> getAssignedTasks(String assignee, Map<String, Object> variableMap);

    public Long getTasksForCandidateGroupsCount(List<String> teamUris, Map<String, Object> variableMap);

    public List<Task> getAllUnassignedTasks(Map<String, Object> variableMap);

    /**
     * Gets the status of the task for the list of users.This method determines whether the task has been 
     * completed or not(in all cases on due date, before due date or after due date)
     *
     * @param userUriList the user uri list
     * @param startDate the start date
     * @param endDate the end date
     * @return list of workflowTaskVO
     */
    public List<WorkflowTaskVO> getTaskStatusReport(List<String> userUriList, Date startDate, Date endDate);

    public List<Task> getAssignedTasksForUser(String assignee, Map<String, Object> variableMap, int displayStart,
            int displayLength);

    public List<HistoricTaskInstance> getAllNativeHistoricTasksforUser(Map<String, Object> variableMap,
            Boolean finishedTask, Boolean searchTask, Boolean isPaginated, int displayStart, int displayLength);

    public List<HistoricTaskInstance> getAllFinishedHistoricTasks(Map<String, Object> variableMap);

    public List<Task> getAllTasksByNativeQuery(Map<String, Object> variableMap);

    public ProcessInstance getSuperProcessInstance(String processInstanceId);

    public Long fetchLoginComparison(String selectClause, Map<String, Object> parameters);

    public HistoricTaskInstance getHistoricTaskByTaskId(String taskId, Map<String, Object> variableMap);

    List<BigDecimal> getHistoricTasksVariables(List<String> processInstanceId, String variableName);

    public List<Object[]> getApplicationTasksVariables(List<BigDecimal> variableValue);

    List<String> getAllProcInstanceforUser(Map<String, Object> variableMap, Boolean finishedTask, Boolean searchTask,
            Boolean isPaginated, int displayStart, int displayLength);

    // performance changes

    public Map<String, String> getTextVariablesByProcInstIds(List<String> procInstIds, String variableName);

    public Map<String, Long> getLongVariablesByProcInstIds(List<String> procInstIds, String variableName);

    public Map<String, Long> getAppIdsByProcessInstIds(Collection<Task> taskList);

    public Map<String, String> getStageNamesByProcessInstIds(Collection<Task> taskList);

    public Map<String, List<String>> getTeamUriListsByTaskId(Collection<Task> tasks);

    /**
     * 
     * Returns paginated tasks for a particular stage.
     * @param variableMap
     * @param displayStart
     * @param displayRecords
     * @return List<Task>
     */
    public List<Task> getPaginatedTasksForStage(Map<String, Object> variableMap, Integer displayStart, Integer displayRecords);

	public Long getPaginatedTasksCountForStage(Map<String, Object> variableMap);

	List<String> getProcInstanceforRCUUser(Map<String, Object> variableMap,
			Boolean finishedTask, Boolean searchTask, Boolean isPaginated,
			int displayStart, int displayLength);
	public Map<String, Object> getVariablesForProcessInstanceId(String pid);
	 public Task getTaskByTaskIdWithProcessVariable(String taskId);
	    public Long getTasksCountForLoanApplication(Long appId);
	
	List<Task> getTaskByTaskId(Collection<String> taskIds);

	List<HistoricTaskInstance> getAllNativeHistoricTasksForIds(List<String> taskIds);

	Map<String, List<String>> getTeamUriByTaskId(Collection<String> taskIds);

	Map<String, Map<String, Object>> getTaskByVariable(
			Collection<String> taskIds, Collection<String> variableNames);

	/**
     * Resolve if an execution is at the receive task.
     *
     * @param processInstanceId the process instance id
     * @param receiveTaskId the receive task id
     * 
     * @return the execution id 
     */
	public String resolveReceiveTask(String processInstanceId, String receiveTaskId);

	/**
     * Signal the execution if it is waiting at a receive task.
     * 
     * @param executionId the execution id 
     */
	public void signalReceiveTask(String executionId);

	List<Task> getTaskListByTaskName(String taskName);

	/**
     * Gets the process variable of a task.
     *
     * @param taskId the task id
     * @param variableName the variable name
     * @return the process variable
     */
	public Object getProcessVariableByTask(String taskId, String variableName);

	//List<String> getAllHistoricProcessVariables(Map<String, Object> variableMap);

}
