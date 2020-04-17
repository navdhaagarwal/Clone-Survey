/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.adhoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.process.BPMNProcessService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserInfo;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Named("adhocService")
@MonitoredWithSpring(name = "Adhoc_Service_IMPL_")
public class AdhocServiceImpl extends BaseServiceImpl implements AdhocService {

    private static final String     ADHOC     = "ADHOC";

    private static final String     TASK_TYPE = "taskType";

    @Inject
    @Named("bpmnProcessService")
    protected BPMNProcessService    bpmnProcessService;

    @Inject
    @Named("teamService")
    protected TeamService           teamService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Override
    public void createAdhocTask(String name, String description, Date dueDate, String ownerUri, String assigneeUri,
            String teamUri, Integer priority, AdhocTaskType adhocTaskType, AdhocTaskSubType adhocTaskSubType) {

        NeutrinoValidator.notNull(name, "name cannot be null");
        NeutrinoValidator.notNull(description, "description cannot be null");
        NeutrinoValidator.notNull(dueDate, "dueDate cannot be null");
        NeutrinoValidator.notNull(ownerUri, "ownerUri cannot be null");
        NeutrinoValidator.notNull(teamUri, "teamUri cannot be null");
        // NeutrinoValidator.notNull(assigneeEntityId,
        // "assigneeEntityId cannot be null");
        NeutrinoValidator.notNull(priority, "priority cannot be null");
        NeutrinoValidator.notNull(adhocTaskType, "adhocTaskType cannot be null");
        NeutrinoValidator.notNull(adhocTaskSubType, "adhocTaskSubType cannot be null");

        AdhocTask adhocTask = new AdhocTask();

        Map<String, Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("name", name);
        parametersMap.put("description", description);
        parametersMap.put("dueDate", dueDate);
        parametersMap.put("ownerUri", ownerUri);
        parametersMap.put("teamUri", teamUri);
        parametersMap.put(TASK_TYPE, ADHOC);
        if (assigneeUri != null && !assigneeUri.isEmpty()) {
            parametersMap.put("assigneeUri", assigneeUri);
            adhocTask.setTaskStatus(AdhocTaskStatus.ASSIGNED);
        } else {
            parametersMap.put("assigneeUri", null);
            adhocTask.setTaskStatus(AdhocTaskStatus.OPEN);
        }
        parametersMap.put("priority", priority);
        String processInstanceId = bpmnProcessService.startProcess("adhocProcess", parametersMap);

        Task task = bpmnProcessService.getTaskByProcessId(processInstanceId);

        adhocTask.setTaskType(adhocTaskType);
        adhocTask.setTaskSubType(adhocTaskSubType);
        if (task != null) {
            adhocTask.setTaskId(task.getId());
        }
        adhocTask.setTitle(name);
        entityDao.saveOrUpdate(adhocTask);

    }

    @Override
    public void createAdhocTask(AdhocForm adhocForm) {
        NeutrinoValidator.notNull(adhocForm.getName(), "name cannot be null");
        NeutrinoValidator.notNull(adhocForm.getDescription(), "description cannot be null");
        NeutrinoValidator.notNull(adhocForm.getDueDate().toDate(), "dueDate cannot be null");
        NeutrinoValidator.notNull(adhocForm.getOwner(), "ownerEntityId cannot be null");
        NeutrinoValidator.notNull(adhocForm.getTeamUri(), "teamEntityId cannot be null");
        // NeutrinoValidator.notNull(adhocForm.getAssigneeUri(),
        // "assigneeEntityId cannot be null");
        NeutrinoValidator.notNull(adhocForm.getPriority(), "priority cannot be null");
        NeutrinoValidator.notNull(adhocForm.getTaskType(), "adhocTaskType cannot be null");
        NeutrinoValidator.notNull(adhocForm.getTaskSubType(), "adhocTaskSubType cannot be null");

        Map<String, Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("name", adhocForm.getName());
        parametersMap.put("description", adhocForm.getDescription());
        parametersMap.put("dueDate", adhocForm.getDueDate().toDate());
        parametersMap.put("ownerUri", adhocForm.getOwner());
        parametersMap.put("teamUri", adhocForm.getTeamUri());
        parametersMap.put("assigneeUri", adhocForm.getAssignee());
        parametersMap.put("priority", adhocForm.getPriority());
        parametersMap.put(TASK_TYPE, ADHOC);
        if (adhocForm.getVariablesMap() != null) {
            parametersMap.putAll(adhocForm.getVariablesMap());
        }

        AdhocTask adhocTask = new AdhocTask();

        if (!StringUtils.isEmpty(adhocForm.getAssignee())) {
            parametersMap.put("assigneeUri", adhocForm.getAssignee());
            adhocTask.setTaskStatus(AdhocTaskStatus.ASSIGNED);
        } else {
            parametersMap.put("assigneeUri", null);
            adhocTask.setTaskStatus(AdhocTaskStatus.OPEN);
        }
        String processInstanceId = bpmnProcessService.startProcess("adhocProcess", parametersMap);

        Task task = bpmnProcessService.getTaskByProcessId(processInstanceId);

        adhocTask.setTaskType(adhocForm.getTaskType());
        adhocTask.setTaskSubType(adhocForm.getTaskSubType());
        adhocTask.setTaskId(task.getId());
        adhocTask.setTitle(task.getName());
        entityDao.saveOrUpdate(adhocTask);

    }

    @Override
    public void resolveAdhocTask(String taskId) {
        NeutrinoValidator.notNull(taskId, "taskId can not be null");
        AdhocTask adhocTask = getAdhocTaskForTaskId(taskId);

        if (adhocTask == null) {
            throw new InvalidDataException("No Adhoc task Exists");
        } else if (AdhocTaskStatus.RESOLVED.equals(adhocTask.getTaskStatus())) {
            throw new InvalidDataException("Adhoc task has already been resolved");
        } else if (AdhocTaskStatus.OPEN.equals(adhocTask.getTaskStatus())) {
            throw new InvalidDataException("Adhoc task is not yet assigned to anyone");
        }
        bpmnProcessService.resolveUserTask(taskId);
        adhocTask.setTaskStatus(AdhocTaskStatus.RESOLVED);
        entityDao.saveOrUpdate(adhocTask);
    }

    @Override
    public void completeAdhocTask(String taskId) {
        NeutrinoValidator.notNull(taskId, "taskId can not be null");
        AdhocTask adhocTask = getAdhocTaskForTaskId(taskId);

        if (adhocTask == null) {
            throw new InvalidDataException("No Adhoc task Exists");
        } else if (AdhocTaskStatus.ASSIGNED.equals(adhocTask.getTaskStatus())) {
            throw new InvalidDataException("Adhoc task is pending with assignee");
        } else if (AdhocTaskStatus.OPEN.equals(adhocTask.getTaskStatus())) {
            throw new InvalidDataException("Adhoc task is still open");
        }
        bpmnProcessService.completeUserTask(taskId);
    }

    @Override
    public void assignAdhocTask(String taskId, String assigneeUri) {
        NeutrinoValidator.notNull(taskId, "taskId can not be null");
        AdhocTask adhocTask = getAdhocTaskForTaskId(taskId);

        if (StringUtils.isEmpty(assigneeUri)) {
            bpmnProcessService.setAssignee(taskId, null);
            bpmnProcessService.setTaskVariable(taskId, "assignedToTeam", null);
            adhocTask.setTaskStatus(AdhocTaskStatus.OPEN);
        } else {
            bpmnProcessService.setAssignee(taskId, assigneeUri);
            bpmnProcessService
                    .setTaskVariable(taskId, "assignedToTeam", bpmnProcessService.getCandidateGroupForTask(taskId));
            adhocTask.setTaskStatus(AdhocTaskStatus.ASSIGNED);
        }
    }

    @Override
    public Task getTaskForTaskId(String taskId) {
        NeutrinoValidator.notNull(taskId, "taskId cannot be null");
        return bpmnProcessService.getTaskByTaskId(taskId, null);
    }

    @Override
    public AdhocTask getAdhocTaskForTaskId(String taskId) {

        NamedQueryExecutor<AdhocTask> executor = new NamedQueryExecutor<AdhocTask>("Adhoc.byTaskId").addParameter("taskId",
                taskId);
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public AdhocTask getAdhocTaskForAdhocTaskId(Long adhocTaskId) {
        NeutrinoValidator.notNull(adhocTaskId, "adhocTaskId cannot be null");
        return entityDao.find(AdhocTask.class, adhocTaskId);
    }

    @Override
    public List<Task> getAdhocTasks() {
        return bpmnProcessService.getAdhocTasks();
    }

    @Override
    public List<Task> getAdhocTasksForPool(UserInfo userInfo) {
        List<String> teamUris = teamService.getTeamByUrisForUserId(userInfo);
        if (teamUris == null || teamUris.isEmpty()) {
            return Collections.emptyList();
        }
        return bpmnProcessService.getUnassignedAdhocTasksForTeam(teamUris);
    }

    @Override
    public List<Task> getAdhocTasksForTeamLead(UserInfo userInfo) {
        List<Task> tasks = new ArrayList<Task>();
        // For Performance T
        /*        List<Team> teamList = teamService.getTeamsLedByThisUserInLoggedInBranch(userInfo);
        for (Team team : teamList) {
            tasks.addAll(bpmnProcessService.getAssignedAdhocTasksForTeam(team.getUri()));
        }*/

        List<Long> teamIdList = teamService.getTeamIdsLedByThisUserInLoggedInBranch(userInfo);
        for (Long teamId : teamIdList) {
            tasks.addAll(bpmnProcessService.getAssignedAdhocTasksForTeam(teamService.getTeamUriForTeamId(teamId)));
        }
        // End For Performance T
        if (CollectionUtils.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        return tasks;
    }

    @Override
    public List<Task> getAllAdhocTasksForOwner(UserInfo userInfo) {
        List<String> teamUris = teamService.getTeamByUrisForUserId(userInfo);
        if (teamUris == null || teamUris.isEmpty()) {
            return Collections.emptyList();
        }
        return bpmnProcessService.getAllAdhocTasksForOwner(userInfo.getUserEntityId().getUri());
    }

    @Override
    public List<Task> getAssignedAdhocTasksForOwner(UserInfo userInfo) {
        List<Task> tasks = new ArrayList<Task>();
        List<String> teamUris = teamService.getTeamByUrisForUserId(userInfo);
        for (String teamUri : teamUris) {
            tasks.addAll(bpmnProcessService.getAssignedAdhocTasksForOwner(userInfo.getUserEntityId().getUri(), teamUri));
        }
        if (CollectionUtils.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        return tasks;
    }

    @Override
    public List<Task> getAdhocTasksForAssignee(String assigneeUri) {
        return bpmnProcessService.getAdhocTasksForAssignee(assigneeUri);
    }

    @Override
    public void addCommentToAdhocTask(String taskId, String comment) {
        bpmnProcessService.addComment(taskId, null, comment);
    }

    @Override
    public List<Comment> getCommentsForAdhocTask(String taskId) {
        return bpmnProcessService.getCommentsForTask(taskId);
    }

    @Override
    public List<AdhocTaskType> getTaskTypesByAuthority(List<String> authCodes) {
        return genericParameterService.findByAuthorities(authCodes, AdhocTaskType.class);
    }

    @Override
    public List<Task> getAssignedAdhocTasksForInvolvedUser(UserInfo userInfo) {
        List<Task> adhocTasks = new ArrayList<Task>();
        Map<String, Task> taskMap = new HashMap<String, Task>();

        List<Task> list1 = getAssignedAdhocTasksForOwner(userInfo);
        List<Task> list2 = getAdhocTasksForAssignee(userInfo.getUserEntityId().getUri());

        Iterator<Task> listIterator1 = list1.iterator();
        Iterator<Task> listIterator2 = list2.iterator();

        while (listIterator1.hasNext()) {
            Task task = listIterator1.next();
            taskMap.put(task.getId(), task);
        }
        while (listIterator2.hasNext()) {
            Task task = listIterator2.next();
            taskMap.put(task.getId(), task);
        }

        adhocTasks.addAll(taskMap.values());

        return adhocTasks;
    }

    @Override
    public List<Task> getAllAdhocTasksForInvolvedUser(UserInfo userInfo) {
        List<Task> adhocTasks = new ArrayList<Task>();
        Map<String, Task> taskMap = new HashMap<String, Task>();

        List<Task> list1 = getAllAdhocTasksForOwner(userInfo);
        List<Task> list2 = getAdhocTasksForAssignee(userInfo.getUserEntityId().getUri());

        Iterator<Task> listIterator1 = list1.iterator();
        Iterator<Task> listIterator2 = list2.iterator();
        Task task = null;
        while (listIterator1.hasNext()) {
            task = listIterator1.next();
            taskMap.put(task.getId(), task);
        }
        while (listIterator2.hasNext()) {
            task = listIterator2.next();
            taskMap.put(task.getId(), task);
        }
        if (!taskMap.isEmpty()) {
            adhocTasks.addAll(taskMap.values());
        }
        return adhocTasks;
    }

}
