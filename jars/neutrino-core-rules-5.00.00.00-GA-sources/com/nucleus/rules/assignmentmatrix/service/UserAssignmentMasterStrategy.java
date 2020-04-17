/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.assignmentmatrix.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.task.Task;
import org.apache.commons.collections4.CollectionUtils;

import com.nucleus.core.team.assignmentService.TeamAssignmentStrategy;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.process.BPMNProcessService;
import com.nucleus.rules.model.assignmentMatrix.AssignmentConstants;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Named(value = "userAssignmentStrategy")
public class UserAssignmentMasterStrategy implements AssignmentStrategy {

    @Inject
    @Named("teamService")
    private TeamService                    teamService;

    @Inject
    @Named("bpmnProcessService")
    private BPMNProcessService   bpmnProcessService;

    @Inject
    @Named("userService")
    private UserService                    userService;

    @Inject
    @Named("teamAssignmentStrategy")
    private TeamAssignmentStrategy teamAssignmentStrategy;

    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, Object> handleEntityList(List<? extends Entity> entitiesList) {

        Map<Object, Object> resultMap = new HashMap<Object, Object>();
        if (CollectionUtils.isEmpty(entitiesList)) {
            return resultMap;
        }
        User user = getLeastLoadedUser((List<User>) (entitiesList));
        List<Long> teamIds = teamService.getTeamIdAssociatedToUserByUserId(user.getId());
        /*
        List<Team> teamList = new ArrayList<Team>();
        for (Long teamId : teamIds) {
            teamList.add(teamService.getTeamByTeamId(teamId));
        }*/
        resultMap.put(AssignmentConstants.USER_URI, user.getUri());
        if (CollectionUtils.isNotEmpty(teamIds)) {
            resultMap.put(AssignmentConstants.TEAM_URI,
                    (AssignmentConstants.Team.getName()).concat(":").concat(String.valueOf(teamIds.get(0))));

        }

        return resultMap;

    }


    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, Object> findLeastLoadedEntity(List entitiesList) {

        return handleEntityList(entitiesList);

    }


 /**
 * 
 * This method is used to find least loaded user and returns the user 
 * @param userList
 * @return
 */
    private User getLeastLoadedUser(List<User> userList) {
        Map<String, Integer> userUriCountMap = new HashMap<String, Integer>();
        for (User user : userList) {
            List<Task> taskList = bpmnProcessService.getAssignedTasksForUser(user.getUri(), null);
            if (taskList != null) {
                userUriCountMap.put(user.getUri(), taskList.size());
            } else {
                userUriCountMap.put(user.getUri(), 0);
            }
        }

        Map<String, Integer> sortedMap = new TreeMap<String, Integer>(new MapComparator(userUriCountMap));
        sortedMap.putAll(userUriCountMap);
        String userUri = sortedMap.entrySet().iterator().next().getKey();
        UserInfo userInfo = userService.getUserById(EntityId.fromUri(userUri).getLocalId());
        if (userInfo != null) {
            return userInfo.getUserReference();
        }
        return userList.get(0);

    }

}
/**
 * 
 * @author Nucleus Software Exports Limited
 * This class is used to sort the map provided by getLeastLoadedUser method based on the task assigned to user .
 * Sorts the user in ascending order based on minimum tasks.
 */
class MapComparator implements Comparator<String> {
    Map<String, Integer> map;

    public MapComparator(Map<String, Integer> map) {
        this.map = map;
    }

    @Override
    public int compare(String keyA, String keyB) {

        if (map.get(keyA) <= map.get(keyB)) {
            return -1;
        } else {
            return 1;
        }
    }

}
