/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.taskAssignmentMaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;

import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.entity.EntityId;
import com.nucleus.rules.model.EntityType;
import com.nucleus.rules.model.assignmentMatrix.AssignmentConstants;
import com.nucleus.rules.service.RuleService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
public class UserColumnDataHandler extends BaseServiceImpl implements ColumnDataHandler {

	public static final String loggedInUser       = "-1";

	public static final String previousUser       = "-2";

	public static final String leadOfLoggedInUser = "-3";
    
	public static final String lastDDEUser        = "-4";
    
	public static final String loggedInUserWithPreviousTeam        = "-5";
    
	public static final String teamLead        = "-6";

    public static final String leastLoadedUser = "-7";

    @Inject
    @Named("ruleService")
    private RuleService         ruleService;

    @Inject
    @Named("teamService")
    private TeamService         teamService;

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canHandle(Class entityName, Map contextMap) {

        if (User.class.isAssignableFrom(entityName)) {
            return true;

        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map<String, Object> fetchData(Class entityName) {

        String packageName = entityName.getName();
        EntityType entityType = ruleService.getEntityTypeData(packageName);

        String fields = entityType.getFields();
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        Map<String, Object> mapOfUriandDisplayName = new HashMap<String, Object>();

        mapOfUriandDisplayName.put(loggedInUser, "loggedInUser");
        mapOfUriandDisplayName.put(previousUser, "previousUser");
        mapOfUriandDisplayName.put(leadOfLoggedInUser, "leadOfLoggedInUser");
        mapOfUriandDisplayName.put(lastDDEUser, "lastDDEUser");
        mapOfUriandDisplayName.put(loggedInUserWithPreviousTeam, "loggedInUserWithPreviousTeam");
        mapOfUriandDisplayName.put(teamLead, "TeamLead");
        mapOfUriandDisplayName.put(leastLoadedUser, "Least Loaded User");


        result = ruleService.searchEntityData(entityName, fields.split(","));

        if (result != null) {

            for (Map<String, String> mapp : result) {
                String id = null;
                String displayName = null;
                Iterator it = mapp.entrySet().iterator();
                while (it.hasNext()) {
                    Entry entry = (Entry) it.next();
                    String key = (String) entry.getKey();
                    String val = String.valueOf(entry.getValue());
                    if (key.equalsIgnoreCase("id")) {
                        id = val;
                    } else {
                        displayName = String.valueOf(entry.getValue());
                    }

                }
                mapOfUriandDisplayName.put(id, displayName);

            }

        }

        return mapOfUriandDisplayName;
    }

    @Override
    public Object handleData(Object value, Map contextMap) {
        String val = String.valueOf(value);

        if (!(val.equals(loggedInUser) || val.equals(previousUser) || val.equals(leadOfLoggedInUser)||val.equals(lastDDEUser)||val.equals(loggedInUserWithPreviousTeam) || val.equalsIgnoreCase(teamLead))) {
            return val;
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        User user = null;
        UserInfo ui = null;

        if (val.equals(loggedInUser)) {
            ui = getCurrentUser();
            user = ui.getUserReference();

        } else if (val.equals(previousUser)) {
            if (null != contextMap && null != contextMap.get("contextObjectPreviousAssignee")) {
                user = entityDao.get(EntityId.fromUri((String) contextMap.get("contextObjectPreviousAssignee")));
            }

        } else if (val.equals(leadOfLoggedInUser)) {
            ui = getCurrentUser();
            List<Team> teams = teamService.getTeamNotLeadByThisUser(ui.getId());
            
            if (CollectionUtils.isNotEmpty(teams)) {
                user = teams.get(0).getTeamLead();
            }
        }
        else if(val.equals(lastDDEUser))
        {
            if (null != contextMap && null != contextMap.get("contextObjectLastDDEAssignee")) {
                user = entityDao.get(EntityId.fromUri((String) contextMap.get("contextObjectLastDDEAssignee")));
            }
        }
        else if(val.equals(loggedInUserWithPreviousTeam))
        {
            ui = getCurrentUser();
            user = ui.getUserReference();
        } else if(val.equalsIgnoreCase(teamLead)){
            user =(User) contextMap.get("teamLead");
        }

        if (user == null) {
            ui = getCurrentUser();
            user = ui.getUserReference();
        }

        List<Team> teamList = teamService.getTeamsAssociatedToUserByUserId(user.getId());
        Team teamForTask = null;
        if (CollectionUtils.isNotEmpty(teamList)) {
            if (teamList.size() == 1) {
                teamForTask = teamList.get(0);
            } else {
                for (Team team : teamList) {
                    if(user.getUri()!=null && team != null && team.getTeamLead() !=null) {
                        if (user.getUri().equalsIgnoreCase(team.getTeamLead().getUri())) {
                            teamForTask = team;
                            break;
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(teamList) && teamForTask == null) {
            teamForTask = teamList.get(0);
        }

        if (teamForTask != null) {
            resultMap.put(AssignmentConstants.Team.getName(), teamForTask.getId());
            resultMap.put(AssignmentConstants.User.getName(), user.getId());
            return resultMap;
        }

        return val;
    }
}
