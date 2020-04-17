/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.assignmentmatrix.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import com.nucleus.core.team.assignmentService.TeamAssignmentStrategy;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.entity.Entity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.process.BPMNProcessService;
import com.nucleus.rules.model.assignmentMatrix.AssignmentConstants;
import com.nucleus.rules.service.GridCriteriaRuleConstants;
import com.nucleus.user.User;
import org.activiti.engine.task.Task;
import org.apache.commons.collections4.CollectionUtils;

import static java.util.Comparator.comparing;

/**
 * @author Nucleus Software India Pvt Ltd
 */
@Named(value = "teamAssignmntStrategy")
public class TeamAssignmentMasterStrategy implements AssignmentStrategy {

    @Inject
    @Named("teamAssignmentStrategy")
    private TeamAssignmentStrategy teamAssignmentStrategy;

    @Inject
    @Named("teamService")
    protected TeamService teamService;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    @Inject
    @Named("bpmnProcessService")
    private BPMNProcessService bpmnProcessService;

    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, Object> handleEntityList(List<? extends Entity> entitiesList) {
        Map<Object, Object> criteriaResult = new HashMap<Object, Object>();
        if (entitiesList != null && entitiesList.size() > 0) {

            Team team = teamAssignmentStrategy.getLeastLoadedTeam((List<Team>) entitiesList);
            criteriaResult.put(AssignmentConstants.TEAM_URI, team.getUri());

        }
        return criteriaResult;
    }

    public Map<Object, Object> findLeastLoadedEntity(List entitiesList) {
        Map<Object, Object> criteriaResult = new HashMap<>();
        if (entitiesList.isEmpty()) {
            return criteriaResult;
        }
        List<Map> mapList = entitiesList;
        Map<User, Integer> userTaskMap = new ConcurrentHashMap<>();
        Map<String, Integer> teamUriTaskMap = new HashMap<>();

        Map<String, Set<User>> teamUserMap = new HashMap<>();
        for (Map elem : mapList) {
            Set<User> teamUserList = new HashSet<>();
            String teamUri = ((Team) elem.get(GridCriteriaRuleConstants.TEAMS)).getUri();
            User user = (User) elem.get(GridCriteriaRuleConstants.USERS);
            if (teamUserMap.containsKey(teamUri)) {
                teamUserList = teamUserMap.get(teamUri);
            }
            teamUserList.add(user);
            teamUriTaskMap.put(teamUri, 0);
            userTaskMap.put(user, 0);
            teamUserMap.put(teamUri, teamUserList);
        }

        updateUsersTaskMap(userTaskMap);
        updateTeamsTaskMap(teamUriTaskMap);
        addUserTaskToTeamTask(userTaskMap, teamUriTaskMap, teamUserMap);

        HashMap<String, Double> teamToTaskRatioMap = getSortedTeamToTaskRatioMap(teamUriTaskMap, teamUserMap);

        if (teamToTaskRatioMap != null && teamToTaskRatioMap.keySet() != null) {
            criteriaResult.put(AssignmentConstants.TEAM_URI, teamToTaskRatioMap.entrySet().iterator().next().getKey());
        }
        return criteriaResult;
    }

    private void addUserTaskToTeamTask(Map<User, Integer> userTaskMap, Map<String, Integer> teamUriTaskMap, Map<String, Set<User>> teamUserMap) {
        for (Map.Entry<String, Set<User>> entry : teamUserMap.entrySet()) {
            int totalTasks = 0;
            if (teamUriTaskMap.containsKey(entry.getKey())) {
                totalTasks = teamUriTaskMap.get(entry.getKey());
            }
            for (User user : entry.getValue()) {
                totalTasks = totalTasks + userTaskMap.get(user);
            }
            teamUriTaskMap.put(entry.getKey(), totalTasks);
        }
    }

    private LinkedHashMap<String, Double> getSortedTeamToTaskRatioMap(Map<String, Integer> teamUriTaskMap, Map<String, Set<User>> teamUserMap) {
        HashMap<String, Double> teamToTaskRatioMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : teamUriTaskMap.entrySet()) {
            int applicableUsersCountInTeam = (teamUserMap.get(entry.getKey())).size();
            if (applicableUsersCountInTeam == 0) {
                applicableUsersCountInTeam = 1;
            }
            Double ratioTaskPerUser = Double.valueOf(Double.valueOf(entry.getValue()) / Double.valueOf(applicableUsersCountInTeam));
            teamToTaskRatioMap.put(entry.getKey(), ratioTaskPerUser);
        }


        return teamToTaskRatioMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, LinkedHashMap::new));
    }

    private void updateUsersTaskMap(Map<User, Integer> userTaskMap) {
        for (User user : userTaskMap.keySet()) {
            List<Task> taskList = bpmnProcessService.getAssignedTasksForUser(user.getUri(), null);
            if (taskList != null) {
                userTaskMap.put(user, taskList.size());
            } else {
                userTaskMap.put(user, 0);
            }
        }
    }

    private Map updateTeamsTaskMap(Map<String, Integer> teamUriTaskMap) {
        Set<String> teamUris = teamUriTaskMap.keySet();

        if (CollectionUtils.isEmpty(teamUris)) {
            return null;
        }





        if (CollectionUtils.isNotEmpty(teamUris)) {
            AtomicInteger counter = new AtomicInteger(0);
            int size = 1000;

            Collection<List<String>> partitioned = teamUris.stream()
                    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / size))
                    .values();
            for(List<String> list : partitioned){
                StringBuilder nativeQueryString = new StringBuilder();
                StringBuilder teamUriString = new StringBuilder();
                teamUriString = new StringBuilder();

                int i = 0;
                for (String uri : list) {
                    teamUriString.append("'").append(uri).append("'");
                    if (i != list.size() - 1) {
                        teamUriString.append(",");
                    }
                    i++;
                }
                nativeQueryString.append(GridCriteriaRuleConstants.TEAM_TASK_QUERY).append(teamUriString).append(")").append(GridCriteriaRuleConstants.GROUP_BY_I_GROUP_ID);
                Query nativeQuery = entityDao.getEntityManager().createNativeQuery(nativeQueryString.toString());
                nativeQuery.unwrap(org.hibernate.SQLQuery.class).addSynchronizedQuerySpace("");
                List<Object[]> arrayList = nativeQuery.getResultList();
                for (Object[] objects : arrayList) {
                    teamUriTaskMap.put((String) objects[0], ((BigDecimal) objects[1]).intValueExact());
                }
            }
        }
        return teamUriTaskMap;

    }

}



