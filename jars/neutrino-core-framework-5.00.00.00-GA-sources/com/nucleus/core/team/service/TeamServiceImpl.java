/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.team.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.misc.util.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;

import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.EntityLifeCycleData;
import com.nucleus.entity.EntityLifeCycleDataBuilder;
import com.nucleus.event.EventTypes;
import com.nucleus.event.TeamEvent;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

import net.bull.javamelody.MonitoredWithSpring;

@Named("teamService")
@MonitoredWithSpring(name = "Team_Service_IMPL_")
public class TeamServiceImpl extends BaseServiceImpl implements TeamService {

    private static final String       QUERY_FOR_TEAM_NAME_FOR_TEAM_ID          = "Team.fetchTeamNameForTeamId";

    private static final String       QUERY_TEAM_USERS_FOR_TEAM_ID             = "Team.fetchTeamUsersForTeamId";

    private static final String       QUERY_GET_ALL_USERS_BY_TEAM_AND_USER_STATUS= "Team.getAllUsersByTeamAndUserStatus";
    
    private static final String       QUERY_TEAM_USER_IDS_FOR_TEAM_ID          = "Team.fetchTeamUserIdsForTeamId";

    private static final String       QUERY_TEAM_USERIDS_USERNAMES_FOR_TEAM_ID = "Team.getassosiatedUserIdsandusernamesWithTeamBasedOnTeamID";

    private static final String       QUERY_FOR_TEAM_BRANCH_NAME_FOR_TEAM_ID   = "Team.fetchTeamBranchNameForTeamId";

    private static final String       QUERY_FOR_TEAM_LEAD_FOR_TEAM_ID          = "Team.fetchTeamLeadForTeamId";

    private static final String       QUERY_FOR_TEAM_LEAD_ID_FOR_TEAM_ID       = "Team.fetchTeamLeadIdForTeamId";

    private static final String       QUERY_FOR_GETTING_ALL_TEAM_LEAD          = "Team.fetchAllTeamLead";

    private static final String       QUERY_FOR_TEAM_BRANCH_FOR_TEAM_ID        = "Team.fetchTeamBranchForTeamId";
    private static final String       QUERY_TEAM_USERNAMES_FOR_TEAM_ID         = "Team.fetchTeamUsernamesForTeamId";

	private static final String TEAM_GET_ALL_ELIGIBLE_BPUSERS_FOR_THIS_BRANCH = "SELECT distinct(ubpm.associatedUser) FROM UserBPMapping ubpm where ubpm.businessPartnerId in "
			+ " (:associatedBusinessPartnerId) and (ubpm.associatedUser.masterLifeCycleData.approvalStatus in (0,3))";
    @Inject
    @Named("userService")
    private UserService               userService;

    @Inject
    @Named("baseMasterService")
    BaseMasterService                 baseMasterService;

    @Inject
    @Named("organizationService")
    OrganizationService               organizationService;

    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore userManagementService;

    /**
     * 
     * @Description
     * returns all the teams present in database
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_ALL_TEAMS")
    public List<Team> getAllTeams() {
        NamedQueryExecutor<Team> namedQuery = new NamedQueryExecutor<Team>("Team.getAllTeams").addQueryHint(
                QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(namedQuery);
    }

    /**
     * 
     * @Description
     * returns all the teamIds present in database
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_ALL_TEAMS")
    public List<Long> getAllTeamIds() {
        NamedQueryExecutor<Long> namedQuery = new NamedQueryExecutor<Long>("Team.getAllTeamIds").addQueryHint(
                QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(namedQuery);
    }

    /**
     * 
     * @Description
     * returns all the teamid,names present in database
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_ALL_TEAMIDS_NAMES")
    public List<Object[]> getAllTeamIdsAndNames() {
        NamedQueryExecutor<Object[]> namedQuery = new NamedQueryExecutor<Object[]>("Team.getAllTeamIdsAndNmaes")
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(namedQuery);
    }

    /**
     * @param teamId(Long)
     * @Description
     * if there is no team corresponding to teamId or team has not user
     * @return empty userSet
     * else
     * @return the set of userinfo of the associated users of a team by its team id
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_TEAM_USR_BY_TEAMID")
    public Set<UserInfo> getAssociatedUsersOfTeamByTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        BaseLoggers.flowLogger.info("getAssociatedUsersOfTeamByTeamId " + teamId);
        Set<UserInfo> userInfoSet = new HashSet<UserInfo>();
        Set<User> userSet = getTeamUsersByTeamId(teamId);
        if (userSet != null) {
            Hibernate.initialize(userSet);

            List<User> userList = new ArrayList<User>(userSet);

            for (int i = 0 ; i < userList.size() ; i++) {
                userInfoSet.add(new UserInfo(userList.get(i)));
            }
        }
        return userInfoSet;
    }

    @Override
    public Set<User> getTeamUsersByTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>(QUERY_TEAM_USERS_FOR_TEAM_ID)
        		.addParameter("teamId", teamId)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return new HashSet<User>(entityDao.executeQuery(executor));
    }

    
    @Override
    public Set<User> getTeamUsersByTeamIdAndUserStaus(Long teamId, List<Integer> userStatus){
    	NeutrinoValidator.notNull(teamId, "Team id can not be null");
    	NeutrinoValidator.notEmpty(userStatus, "User status can not be null or empty");
    	NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>(QUERY_GET_ALL_USERS_BY_TEAM_AND_USER_STATUS)
    			.addParameter("teamId", teamId)
    			.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
    			.addParameter("userStatusList",userStatus)
    			.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
    	
    	return new HashSet<>(entityDao.executeQuery(executor));
    }
    
    
    @Override
    public Set<Long> getTeamUserIdsByTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>(QUERY_TEAM_USER_IDS_FOR_TEAM_ID)
        		.addParameter("teamId", teamId)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return new HashSet<Long>(entityDao.executeQuery(executor));
    }

    @Override
    public List<User> getTeamUsersByTeamId(UserInfo userInfo) {
        NeutrinoValidator.notNull(userInfo, "User can not be null");
        List<Long> teamIds = getTeamIdsOfUserInLoggedInBranch(userInfo);
        if (CollectionUtils.isEmpty(teamIds)) {
            return Collections.emptyList();
        }
        // List<String> userUriList = null;
        NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>("Team.fetchTeamUsersForTeamIdList")
        		.addParameter("teamIdList", teamIds)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        /*if (CollectionUtils.isNotEmpty(userList)) {
            userUriList = new ArrayList<String>();
            for (User user : userList) {
                userUriList.add(user.getUri());
            }
        } else {
            return Collections.emptyList();
        }*/

        return entityDao.executeQuery(executor);
    }

    @Override
    public List<Long> getTeamUserIdByTeamId(UserInfo userInfo) {
        NeutrinoValidator.notNull(userInfo, "User can not be null");
        List<Long> teamIds = getTeamIdsOfUserInLoggedInBranch(userInfo);
        if (CollectionUtils.isEmpty(teamIds)) {
            return Collections.emptyList();
        }
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.fetchTeamUsersIdForTeamIdList")
        		.addParameter("teamIdList", teamIds)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public Set<Object[]> getTeamUserIdsAndNamesByTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        NamedQueryExecutor<Object[]> executor = new NamedQueryExecutor<Object[]>(QUERY_TEAM_USERIDS_USERNAMES_FOR_TEAM_ID)
                .addParameter("teamId", teamId)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        return new HashSet<Object[]>(entityDao.executeQuery(executor));
    }

    /**
     * @param userId
     * @Description
     * userId can not be null .
     * @return the teams which has the user with passed userId as the associated user
     */
    @Override
    @MonitoredWithSpring(name = "TSI_ASSO_USR_TEAMS")
    public List<Team> getTeamsAssociatedToUserByUserId(Long userId) {
        NeutrinoValidator.notNull(userId, "user id can not be null");
        NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getTeamByUserId").addParameter("userId",
                userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public List<Long> getTeamIdAssociatedToUserByUserId(Long userId) {
        NeutrinoValidator.notNull(userId, "user id can not be null");
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamIdByUserId").addParameter("userId",
                userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public List<Long> getAllTeamIdAssociatedToUserByUserId(Long userId) {
        NeutrinoValidator.notNull(userId, "user id can not be null");
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getAllTeamIdByUserId").addParameter("userId",
                userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    /**
     * @param teamName
     * @Description
     * teamName cn not be null.
     * returns the team object using the team name
     */
    @Override
    public Team getTeamByTeamName(String teamName) {
        NeutrinoValidator.notNull(teamName, "team name can not be null");
        NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getTeam").addParameter("teamName", teamName)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQueryForSingleValue(executor);
    }

    /**
     * @param (id of team) which is to be get
     * @Description
     * if teamId null
     * returns null object
     * else
     * returns the team object using the team id
     */
    @Override
    public Team getTeamByTeamId(Long localId) {
        if (localId != null) {
            EntityId entityId = new EntityId(Team.class, localId);
            return entityDao.get(entityId);
        } else {
            return null;
        }
    }

    /**
     * @param teamName
     * @Description
     * if there is no team associated to this teamName or team has no user
     * returns null set
     * else
     * returns the set of userinfo of the associated users of a team by its team name
     */
    @Override
    public Set<UserInfo> getAssociatedUsersOfTeamByTeamName(String teamName) {
        Team team = getTeamByTeamName(teamName);
        if (team != null) {
            Set<User> userSet = team.getUsers();
            if (userSet != null) {
                Hibernate.initialize(userSet);
                List<User> userList = new ArrayList<User>(userSet);
                Set<UserInfo> userInfoSet = new HashSet<UserInfo>();
                for (int i = 0 ; i < userList.size() ; i++) {
                    userInfoSet.add(userService.getUserById(userList.get(i).getId()));
                }
                return userInfoSet;
            }
        }
        return Collections.emptySet();
    }

    /**
     * @param team object
     * @Description
     * Team object which is to saved can not be null.
     * this method saves the passed object of team
     */
    @Override
    public void saveTeam(Team team) {
        NeutrinoValidator.notNull(team, "team object can not be null");
    
        User user = getCurrentUser().getUserReference();
        EntityLifeCycleData teamEntityLifeCycleData = team.getEntityLifeCycleData();

        if(ValidatorUtils.isNull(teamEntityLifeCycleData) )
        {
               EntityLifeCycleData entityLifeCycleData = new EntityLifeCycleDataBuilder().setCreatedByEntityId(
                      user.getEntityId()).getEntityLifeCycleData();
               team.setEntityLifeCycleData(entityLifeCycleData);
        }
        else if (ValidatorUtils.isNull(teamEntityLifeCycleData.getCreatedByUri()))
        {
               teamEntityLifeCycleData.setCreatedByUri(user.getUri());
        }
        else
        {
               teamEntityLifeCycleData.setLastUpdatedByUri(user.getUri());
        }
        if (null != team.getAssociatedWithBP() && Team.ASSOCAITED_WITH_BP_Y.equalsIgnoreCase(team.getAssociatedWithBP())){
            team.setApprovalStatus(0);
        }
         team.getMasterLifeCycleData().setReviewedByUri(user.getUri());
         team.getMasterLifeCycleData().setReviewedTimeStamp(DateUtils.getCurrentUTCTime());
         if(team.getTeamBranch().getId()!=null)
         {
         team.setTeamBranch(entityDao.find(OrganizationBranch.class,team.getTeamBranch().getId()));
         }
         entityDao.saveOrUpdate(team);
     }

    
    
    /**
     * 
     * @param team
     */
    @Override
    public void saveTeamsForUser(Team team) {
        NeutrinoValidator.notNull(team, "team object can not be null");
        entityDao.saveOrUpdate(team);
    }

    /**
     * @param team Object
     * @Description
     * deletes the team
     */
    
    /*Changes done for cas-21919 for DCB Temporary */
    @Override
    public void deleteTeam(Team team) {
        if (team != null) {
        	team.setUsers(null);
        	team.setTeamLead(null);
           // entityDao.delete(team);
            team.setApprovalStatus(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
            entityDao.saveOrUpdate(team);
            TeamEvent event = new TeamEvent(EventTypes.TEAM_DELETED_EVENT, true, getCurrentUser().getUserEntityId(), team);
            event.setTeamName(team.getName());
            eventBus.fireEvent(event);
        }
    }

    /**
     * @param user object
     * @Description
     * userId can not be null.
     * if user is null
     * returns null list.
     * else
     * get all the Teams which is in any of the branch alloted to user but the team don't have the 
     * passed user as the associated user i,e; team is eligible to be made as user's team
     */
    @Override
    public List<Team> getTheEligibleTeamsNotAssociatedToThisUser(User user) {
        if (user != null) {
            NeutrinoValidator.notNull(user.getId(), "user id can not be null");
            NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getTeamByUserId").addParameter("userId",
                    user.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            List<Team> userTeams = entityDao.executeQuery(executor);
            List<Team> allTeams = getAllTeamsOfBranchesContainingTeamsOfThisUser(user);
            allTeams.removeAll(userTeams);
            // after removing userTeams from allTeams , allTeams contains teams not of user and that is what is returned
            return allTeams;
        }
        return Collections.emptyList();
    }

    /**
     * @param current user object
     * @Description
     * user id can not be null.
     * if passed user is null
     * returns null count.
     * else
     * get the number of teams led by this user in all the branches.
     */
    @Override
    public Long getNumberOfTeamsLedByThisUser(User thisUser) {
        if (thisUser != null) {
            NeutrinoValidator.notNull(thisUser.getId(), "user id can not be null");
            NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getLeaderOfNumberOfTeams").addParameter(
                    "userId", thisUser.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQueryForSingleValue(executor);
        }
        return null;
    }

    /**
     * @param user object
     * @Description
     * userId can not be null.
     * if user is null
     * returns null teamList
     * else
     * return  all the teams led by this user in all the branches
     */
    @Override
    public List<Team> getTeamsLedByThisUser(UserInfo user) {
        if (user != null) {
            NeutrinoValidator.notNull(user.getId(), "user id can not be null");
            NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getTeamsLeadedByThisUser").addParameter(
                    "userId", user.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(executor);
        }
        return Collections.emptyList();
    }

    /**
     * @param user 
     * @param activeFlag true/false
     * 
     * @return
     * if user is null returns null teamList
     * else
     * return  all the teams led by this user and activeFlag value and approval status in 
     */
    @Override
    public List<Team> getTeamsLedByThisUser(UserInfo user, List<Integer> approvalStatusList, boolean activeFlag) {
    	
        if (user != null && approvalStatusList!=null && !approvalStatusList.isEmpty()) {
        	
            NeutrinoValidator.notNull(user.getId(), "user id can not be null");
            NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getTeamsLeadedByUserIdAndActiveFlag")
            		.addParameter("userId", user.getId())
            		.addParameter("activeFlag", activeFlag)
            		.addParameter("approvalStatusList", approvalStatusList)
            		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(executor);
        }
        return Collections.emptyList();
    }
    
    
    
    
    /**
     * @param user object
     * @Description
     * userId can not be null.
     * if user is null
     * returns null teamList
     * else
     * return  all the teams led by this user in all the branches
     */
    @Override
    public List<Long> getTeamIdsLedByThisUser(UserInfo user) {
        if (user != null) {
            NeutrinoValidator.notNull(user.getId(), "user id can not be null");
            NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamIdsLeadedByThisUser")
                    .addParameter("userId", user.getId())
                    .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
                    .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(executor);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Long> getTeamIdsLedOfThisUserId(Long userId) {
        if (userId != null) {
            NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamIdsLeadOfThisUser").addParameter(
                    "userId", userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(executor);
        }
        return Collections.emptyList();
    }

    @Override
    public Long getTeamBranchIdByTeamId(Long teamId) {
        if (teamId != null) {
            NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamBranchByTeamId").addParameter(
                    "teamId", teamId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQueryForSingleValue(executor);
        }
        return null;
    }

    /**@param userInfo object
     * @Description
     * userId can not be null.
     * if user is null
     * returns null teamList
     * else
     * returns all the teams led by this user in logged in branch
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_TEAM_LED_BY_USR_IN_BRANCH")
    public List<Team> getTeamsLedByThisUserInLoggedInBranch(UserInfo user) {
        if (user != null) {
            NeutrinoValidator.notNull(user.getId(), "user id can not be null");
            if (user.getLoggedInBranch() != null) {
                NeutrinoValidator.notNull(user.getLoggedInBranch().getId(), "branch id can not be null");
                NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getTeamsLeadedByThisUserByUserInfo")
                        .addParameter("userId", user.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
                executor.addParameter("branchId", user.getLoggedInBranch().getId());
                return entityDao.executeQuery(executor);
            }
        }
        return Collections.emptyList();
    }

    /**@param userInfo object
     * @Description
     * userId can not be null.
     * if user is null
     * returns null teamList
     * else
     * returns all the teams led by this user in given branch
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_TEAM_LED_BY_USR_IN_GIVEN_BRANCH")
    public List<Team> getTeamsLedByThisUserInBranch(UserInfo user, Long branchId) {
        if (user != null) {
            NeutrinoValidator.notNull(user.getId(), "user id can not be null");
            if (user.getLoggedInBranch() != null) {
                NeutrinoValidator.notNull(user.getLoggedInBranch().getId(), "branch id can not be null");
                NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getTeamsLeadedByThisUserByUserInfo")
                        .addParameter("userId", user.getId()).addParameter("branchId", branchId)
                        .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
                return entityDao.executeQuery(executor);
            }
        }
        return Collections.emptyList();
    }

    /**@param userInfo object
     * @Description
     * userId can not be null.
     * if user is null
     * returns null teamList
     * else
     * returns all the teamids,names led by this user in logged in branch
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_TEAMIdS_NAMES_LED_BY_USR_IN_BRANCH")
    public List<Object[]> getTeamIdsAndNamesLedByThisUserInLoggedInBranch(UserInfo user) {
        if (user != null) {
            NeutrinoValidator.notNull(user.getId(), "user id can not be null");
            if (user.getLoggedInBranch() != null) {
                NeutrinoValidator.notNull(user.getLoggedInBranch().getId(), "branch id can not be null");
                NamedQueryExecutor<Object[]> executor = new NamedQueryExecutor<Object[]>(
                        "Team.getTeamIdsAndNamesLeadedByThisUserByUserInfo").addParameter("userId", user.getId())
                        .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
                executor.addParameter("branchId", user.getLoggedInBranch().getId());
                return entityDao.executeQuery(executor);
            }
        }
        return Collections.emptyList();
    }

    /**
     * @param user object
     * @Description
     * current user id can not be null.
     * if passed user is null
     * returns
     * null count
     * else
     * returns the number of teams represented by this user
     */
    @Override
    public Long getNoOfTeamsRepresentedByThisUser(User thisUser) {
        if (thisUser != null) {
            NeutrinoValidator.notNull(thisUser.getId(), "user id can not be null");
            NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getNoOfTeamsRepresentedBy").addParameter(
                    "userId", thisUser.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQueryForSingleValue(executor);
        }
        return null;
    }

    /**
     * @param userInfo object
     * @Description
     * firstly,this method finds all the teams in loggedIn branch of passed user
     * then get all the team-uri's represented by this user info object
     */
    @Override
    public List<String> getTeamByUrisForUserId(UserInfo userInfo) {
        List<Long> teamIdList = getTeamsIdOfUserInLoggedInBranch(userInfo);
        List<String> teamUris = new LinkedList<String>();
        if (teamIdList != null) {
            for (Long teamId : teamIdList) {
                teamUris.add(getTeamUriForTeamId(teamId));
            }
            return teamUris;
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getTeamByUrisForUserIdWithoutLoggedInBranch(UserInfo userInfo) {
        List<Long> teamIdList = getTeamsIdOfUserWithoutLoggedInBranch(userInfo);
        List<String> teamUris = new LinkedList<String>();
        if (teamIdList != null) {
            for (Long teamId : teamIdList) {
                teamUris.add(getTeamUriForTeamId(teamId));
            }
            return teamUris;
        }
        return Collections.emptyList();
    }

    private List<Long> getTeamsIdOfUserWithoutLoggedInBranch(UserInfo userInfo) {
        if (userInfo != null) {
            NeutrinoValidator.notNull(userInfo.getId(), "user id can not be null");
            NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamsIdOfUserWithoutLoggedInBranch")
                    .addParameter("userId", userInfo.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(executor);

        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getTeamByUrisForAllBranches(UserInfo userInfo) {
        List<Team> teamList = getAllTeamsOfBranchesContainingTeamsOfThisUser(userInfo.getUserReference());
        List<String> teamUris = new LinkedList<String>();
        if (teamList != null) {
            for (Team team : teamList) {
                teamUris.add(team.getUri());
            }
            return teamUris;
        }
        return Collections.emptyList();
    }

    /**
     * @param team object
     * @Description
     * Id of teamBranch can not be null.
     * if team or teamBranch is null 
     * returns null list
     * else
     * returns all the users present in branch of this team
     */
    @Override
    public List<UserInfo> getAllUsersPresentInBranchOfThisTeam(Team team) {
        if (team != null) {
            OrganizationBranch branch = team.getTeamBranch();
            if (branch != null) {
                return getAllUsersPresentInTeamBranch(branch);
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    @Override
    public List<UserInfo> getAllUsersPresentInTeamBranch(OrganizationBranch branch) {
        if (branch != null) {
            NeutrinoValidator.notNull(branch.getId(), "team branch id can not be null");
            NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>("Team.getAllEligibleUsersForThisTeam")
                    .addParameter("branchId", branch.getId())
                    .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
            List<User> userList = entityDao.executeQuery(executor);
            List<User> bpUsersList = getBusinessPartnerUsersByBranchId(branch.getId());
            if(ValidatorUtils.hasElements(userList)){
                userList.addAll(bpUsersList);
            }
            List<UserInfo> userInfoList = new ArrayList<UserInfo>();
            for (int i = 0 ; i < userList.size() ; i++) {
                User user = userList.get(i);
                UserInfo userInfo=new UserInfo(user);
                if(!userInfoList.contains(userInfo) && (user.isLoginEnabled() || user.isBusinessPartner())){
                    userInfoList.add(userInfo);
                }
            }
            return userInfoList;
        }else {
            return Collections.emptyList();
        }
    }

    private List<User> getBusinessPartnerUsersByBranchId(Long branchId) {
    	
    	List<Long> bpIdsForOrgBranch=entityDao.executeQuery(new NamedQueryExecutor<Long>("BPOrgBranchMapping.getBPIdByOrgBranch")
        		.addParameter("branchId", branchId));
    	
		return entityDao.executeSingleInClauseHQLQuery(TEAM_GET_ALL_ELIGIBLE_BPUSERS_FOR_THIS_BRANCH, "associatedBusinessPartnerId", bpIdsForOrgBranch, User.class);
	}

	/**
     * @param user object
     * @Description
     * user id can not be null.
     * This function is to get All Teams Of Those Branches Which Contains Teams Of This User
     * if passed user is null
     * returns null list
     * else
     * returns all Teams Of Those Branches Which Contains Teams Of This User
     * 
     */
    @Override
    public List<Team> getAllTeamsOfBranchesContainingTeamsOfThisUser(User user) {
        if (user != null) {
            NeutrinoValidator.notNull(user.getId(), "user id can not be null");
            NamedQueryExecutor<Team> allTeamsExecutor = new NamedQueryExecutor<Team>(
                    "Team.getAllTeamsOfThoseBranchesWhichContainsTeamsOfThisUser").addParameter("userId", user.getId())
                    .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(allTeamsExecutor);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Long> getAllTeamIdsOfBranchesContainingTeamsOfThisUser(User user) {
        if (user != null) {
            NeutrinoValidator.notNull(user.getId(), "user id can not be null");
            NamedQueryExecutor<Long> allTeamsExecutor = new NamedQueryExecutor<Long>(
                    "Team.getAllTeamIdsOfThoseBranchesWhichContainsTeamsOfThisUser").addParameter("userId", user.getId())
                    .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(allTeamsExecutor);
        }
        return Collections.emptyList();
    }

    /**
     * @param userInfo object
     * @Description
     * userId can not be null.
     * if userInfo is null or loggedInbranch is null
     * @return null
     * else
     * @return all the teams represented by user in its logged In branch
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_TEAM_USR_IN_BRANCH")
    public List<Team> getTeamsOfUserInLoggedInBranch(UserInfo userInfo) {
        if (userInfo != null) {
            NeutrinoValidator.notNull(userInfo.getId(), "user id can not be null");
            if (userInfo.getLoggedInBranch() != null) {
                NeutrinoValidator.notNull(userInfo.getLoggedInBranch().getId(), "branch id can not be null");
                NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getTeamsOfUserInLoggedInBranch")
                        .addParameter("userId", userInfo.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
                executor.addParameter("branchId", userInfo.getLoggedInBranch().getId());
                return entityDao.executeQuery(executor);
            }
        }
        return Collections.emptyList();
    }

    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_TEAM_ID_USR_IN_BRANCH")
    public List<Long> getTeamsIdOfUserInLoggedInBranch(UserInfo userInfo) {
        if (userInfo != null) {
            NeutrinoValidator.notNull(userInfo.getId(), "user id can not be null");
            if (userInfo.getLoggedInBranch() != null) {
                NeutrinoValidator.notNull(userInfo.getLoggedInBranch().getId(), "branch id can not be null");
                NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamsIdOfUserInLoggedInBranch")
                        .addParameter("userId", userInfo.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
                executor.addParameter("branchId", userInfo.getLoggedInBranch().getId());
                return entityDao.executeQuery(executor);
            }
        }
        return Collections.emptyList();
    }

    /**
     * @param userInfo object
     * @Description
     * userId can not be null.
     * if userInfo is null or loggedInbranch is null
     * @return null
     * else
     * @return all the teamids and names represented by user in its logged In branch
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_TEAM_USR_IN_BRANCH")
    public List<Object[]> getTeamIdsAndNamesOfUserInLoggedInBranch(UserInfo userInfo) {
        if (userInfo != null) {
            NeutrinoValidator.notNull(userInfo.getId(), "user id can not be null");
            if (userInfo.getLoggedInBranch() != null) {
                NeutrinoValidator.notNull(userInfo.getLoggedInBranch().getId(), "branch id can not be null");
                NamedQueryExecutor<Object[]> executor = new NamedQueryExecutor<Object[]>(
                        "Team.getTeamIdsAndNamesOfUserInLoggedInBranch").addParameter("userId", userInfo.getId())
                        .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
                executor.addParameter("branchId", userInfo.getLoggedInBranch().getId());
                return entityDao.executeQuery(executor);
            }
        }
        return Collections.emptyList();
    }

    /**
     * @param userInfo object
     * @Description
     * id of loggedInbranch can not be null
     * if userInfo== null or loggedInbranch == null
     * @return null
     * else
     * @return all the teams present in logged In branch of the passed user
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_ALL_TEAM_USR_IN_BRANCH")
    public List<Team> getAllTeamsOfLoggedInBranchOfThisUser(UserInfo userInfo) {
        if (userInfo != null && userInfo.getLoggedInBranch() != null) {
            NeutrinoValidator.notNull(userInfo.getLoggedInBranch().getId(), "branch id can not be null");
            NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getAllTeamsOfLoggedInBranchOfThisUser")
                    .addParameter("branchId", userInfo.getLoggedInBranch().getId()).addQueryHint(
                            QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(executor);

        }
        return Collections.emptyList();
    }

    /**
     * @param userInfo object
     * @Description
     * id of loggedInbranch can not be null
     * if userInfo== null or loggedInbranch == null
     * @return null
     * else
     * @return all the teamIdss present in logged In branch of the passed user
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_ALL_TEAM_USR_ID_IN_BRANCH")
    public List<Long> getAllTeamIdsOfLoggedInBranchOfThisUser(UserInfo userInfo) {
        if (userInfo != null && userInfo.getLoggedInBranch() != null) {
            NeutrinoValidator.notNull(userInfo.getLoggedInBranch().getId(), "branch id can not be null");
            NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getAllTeamIdsOfLoggedInBranchOfThisUser")
                    .addParameter("branchId", userInfo.getLoggedInBranch().getId()).addQueryHint(
                            QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(executor);

        }
        return Collections.emptyList();
    }

    /**
     * @param userInfo object
     * @Description
     * id of loggedInbranch can not be null
     * if userInfo== null or loggedInbranch == null
     * @return null
     * else
     * @return all the teamIds and names present in logged In branch of the passed user
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_ALL_TEAM_USR_ID_names_IN_BRANCH")
    public List<Object[]> getAllTeamIdsAndNamesOfLoggedInBranchOfThisUser(UserInfo userInfo) {
        if (userInfo != null && userInfo.getLoggedInBranch() != null) {
            NeutrinoValidator.notNull(userInfo.getLoggedInBranch().getId(), "branch id can not be null");
            NamedQueryExecutor<Object[]> executor = new NamedQueryExecutor<Object[]>(
                    "Team.getAllTeamIdsAndNamesOfLoggedInBranchOfThisUser").addParameter("branchId",
                    userInfo.getLoggedInBranch().getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(executor);

        }
        return Collections.emptyList();
    }

    /**
     * @param userInfo object
     * @Description
     * id of loggedInbranch can not be null
     * if userInfo== null or loggedInbranch == null
     * @return null
     * else
     * @return all the teams present in logged In branch of the passed user
     */
    @Override
    public List<Team> getAllTeamsOfLoggedInBranch() {
        NeutrinoValidator.notNull(getCurrentUser().getLoggedInBranch().getId(), "branch id can not be null");
        NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getAllTeamsOfLoggedInBranchOfThisUser")
                .addParameter("branchId", getCurrentUser().getLoggedInBranch().getId()).addQueryHint(
                        QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);

    }

    @Override
    @MonitoredWithSpring(name = "TSI_ALL_TEAMS_OF_LOGGEDIN_ALL_BRANCHES")
    public List<Team> getAllTeamsOfLoggedInAllBranch(OrgBranchInfo branchInfo) {
        NeutrinoValidator.notNull(branchInfo.getId(), "branch id can not be null");
        NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getAllTeamsOfLoggedInBranchOfThisUser")
                .addParameter("branchId", branchInfo.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);

    }

    /**
     * @param organizationBranch object
     * @Description
     * branch id can not be null
     * if branch null 
     * @return null
     * else  
     * @return  all the teams present in the passed branch
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_ALL_TEAM_FOR_BRANCH")
    public List<Team> getAllTeamsOfThisBranch(OrganizationBranch branch) {
        if (branch != null) {
            NeutrinoValidator.notNull(branch.getId());
            List<Integer> statusList = new ArrayList<Integer>();
            statusList.add(ApprovalStatus.APPROVED);
            statusList.add(ApprovalStatus.APPROVED_MODIFIED);
            statusList.add(ApprovalStatus.APPROVED_DELETED);
            statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
            NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getAllTeamsOfThisBranch").addParameter("approvalStatusList", statusList)
                    .addParameter("branchId", branch.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(executor);
        }
        return Collections.emptyList();
    }

    /**
     * @param organizationBranch object
     * @Description
     * branch id can not be null
     * if branch null 
     * @return null
     * else  
     * @return  all the teamiDs,teamnames present in the passed branch
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_ALL_TEAMIDs_AND_names_FOR_BRANCH")
    public List<Object[]> getAllTeamIdsAndNamesOfThisBranch(OrganizationBranch branch) {
        if (branch != null) {
            NeutrinoValidator.notNull(branch.getId());
            NamedQueryExecutor<Object[]> executor = new NamedQueryExecutor<Object[]>(
                    "Team.getAllTeamIdsAndNamesOfLoggedInBranchOfThisUser").addParameter("branchId", branch.getId())
                    .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(executor);
        }
        return Collections.emptyList();
    }

    /**
     * Checks if is user team lead.
     *
     * @param userInfo the user info
     * @return true, if is user team lead
     */
    @Override
    public boolean isUserTeamLead(UserInfo userInfo) {
        if (userInfo != null) {
            List<Team> teams = getTeamsLedByThisUser(userInfo);
            return CollectionUtils.isNotEmpty(teams);
        }
        return false;
    }

    @Override
    public String getTeamUriForTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        return new EntityId(Team.class, teamId).getUri();
    }

    @Override
    public String getTeamNameForTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(QUERY_FOR_TEAM_NAME_FOR_TEAM_ID).addParameter(
                "teamId", teamId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public String getTeamBranchNameForTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(QUERY_FOR_TEAM_BRANCH_NAME_FOR_TEAM_ID)
                .addParameter("teamId", teamId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public OrganizationBranch getTeamBranchForTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_TEAM_BRANCH_FOR_TEAM_ID).addParameter("teamId", teamId);
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public User getTeamLeadForTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>(QUERY_FOR_TEAM_LEAD_FOR_TEAM_ID).addParameter(
                "teamId", teamId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public Long getTeamLeadIdForTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>(QUERY_FOR_TEAM_LEAD_ID_FOR_TEAM_ID).addParameter(
                "teamId", teamId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public String getTeamUriByTeamName(String teamName) {
        NeutrinoValidator.notNull(teamName, "team name can not be null");

        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamId")
                .addParameter("teamName", teamName).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        Long teamId = entityDao.executeQueryForSingleValue(executor);
        return getTeamUriForTeamId(teamId);
    }

    @Override
    @MonitoredWithSpring(name = "TSI_TEAM_BRANCH_CAL")
    public BranchCalendar getTeamBranchCalendarByTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        NamedQueryExecutor<OrganizationBranch> organizationBranchExecutor = new NamedQueryExecutor<OrganizationBranch>(QUERY_FOR_TEAM_BRANCH_FOR_TEAM_ID)
                .addParameter("teamId", teamId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
    	OrganizationBranch organizationBranch = (OrganizationBranch) entityDao.executeQueryForSingleValue(organizationBranchExecutor);
    	
    	BranchCalendar branchCalendar;
    	
    	if(organizationBranch.getHasParentBranchCalender()){
    		branchCalendar = organizationService.getDerivedBranchCalendar(organizationBranch);
            if (notNull(branchCalendar)) {
        		Hibernate.initialize(branchCalendar);
                Hibernate.initialize(branchCalendar.getHolidayList());
            }            
    	}else{
    		branchCalendar = organizationBranch.getBranchCalendar();
    		if (notNull(branchCalendar)) {
        		Hibernate.initialize(branchCalendar);
                Hibernate.initialize(branchCalendar.getHolidayList());
            }    		
    	}    
    	
		return branchCalendar;

    }

    @Override
    public List<User> getAllTeamLeads() {
        NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>(QUERY_FOR_GETTING_ALL_TEAM_LEAD)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint( QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public List<Object[]> getAssociatedUserIdsAndUserNamesOfTeamByTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        BaseLoggers.flowLogger.info("getAssociatedUserIdsAndUserNamesOfTeamByTeamId " + teamId);
        NamedQueryExecutor<Object[]> executor = new NamedQueryExecutor<Object[]>(QUERY_TEAM_USERIDS_USERNAMES_FOR_TEAM_ID)
        		.addParameter("teamId", teamId)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        return entityDao.executeQuery(executor);
    }

    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_TEAM_IDS_LED_BY_USR_IN_BRANCH")
    public List<Long> getTeamIdsLedByThisUserInLoggedInBranch(UserInfo user) {
        if (user != null) {
            NeutrinoValidator.notNull(user.getId(), "user id can not be null");
            if (user.getLoggedInBranch() != null) {
                NeutrinoValidator.notNull(user.getLoggedInBranch().getId(), "branch id can not be null");
                NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamIdsLeadedByThisUserByUserInfo")
                        .addParameter("userId", user.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
                executor.addParameter("branchId", user.getLoggedInBranch().getId());
                return entityDao.executeQuery(executor);
            }
        }
        return Collections.emptyList();
    }

    /**
     * @param userInfo object
     * @Description
     * userId can not be null.
     * if userInfo is null or loggedInbranch is null
     * @return null
     * else
     * @return all the teamIds represented by user in its logged In branch
     */
    @Override
    @MonitoredWithSpring(name = "TSI_FETCH_TEAM_IDS_USR_IN_BRANCH")
    public List<Long> getTeamIdsOfUserInLoggedInBranch(UserInfo userInfo) {
        if (userInfo != null) {
            NeutrinoValidator.notNull(userInfo.getId(), "user id can not be null");
            if (userInfo.getLoggedInBranch() != null) {
                NeutrinoValidator.notNull(userInfo.getLoggedInBranch().getId(), "branch id can not be null");
                NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamIdsOfUserInLoggedInBranch")
                        .addParameter("userId", userInfo.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
                executor.addParameter("branchId", userInfo.getLoggedInBranch().getId());
                return entityDao.executeQuery(executor);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getTeamUrisForUser(UserInfo userInfo) {
        List<Long> teamIdList = getTeamIdAssociatedToUserByUserId(userInfo.getId());
        List<Long> teamsIdsOfUserInLoggedinBranch = teamIdList;
        Team team;
        OrganizationBranch roBranch;
        List<Long> childBranchesForRo = null;
        Long userLoggedInBranchId = null;
        if (userInfo.getLoggedInBranch() != null) {
            userLoggedInBranchId = userInfo.getLoggedInBranch().getId();
        }
        List<String> teamUris = new LinkedList<String>();
        if (CollectionUtils.isEmpty(teamIdList)) {
            return Collections.emptyList();
        }
        for (Long teamId : teamIdList) {
            if (teamsIdsOfUserInLoggedinBranch.contains(teamId)) {
                teamUris.add(getTeamUriForTeamId(teamId));
                continue;
            }
            team = getTeamByTeamId(teamId);

            if (team == null || !team.getRegionOfficeTeam()) {
                continue;
            }
            roBranch = team.getTeamBranch();
            if (roBranch == null) {
                continue;
            }
            childBranchesForRo = organizationService.getAllChildBranchesIds(roBranch.getId(), "CAS");
            if (userLoggedInBranchId == null || CollectionUtils.isEmpty(childBranchesForRo)) {
                continue;
            }
            if (childBranchesForRo.contains(userLoggedInBranchId)) {
                teamUris.add(getTeamUriForTeamId(teamId));
            }

        }
        return teamUris;

    }

    @Override
    public Team createTeamIfNotExist(User user) {
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamId").addParameter("teamName",
                user.getUsername() + "_Team").addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<Long> teamId = entityDao.executeQuery(executor);

        if (CollectionUtils.isEmpty(teamId)) {
            Team team = new Team();
            team.setActiveFlag(true);
            team.setPersistenceStatus(0);
            team.setApprovalStatus(0);
            team.setDescription("Team created when user is marked as Team Lead");
            team.setName(user.getUsername() + "_Team");
            team.setEmail(user.getUsername() + "@testmail.com");
            team.setTeamLead(user);
            List<OrganizationBranch> userBranches = userManagementService.getUserPrimaryBranch(user.getId());
            OrganizationBranch branch = null;
            if (CollectionUtils.isNotEmpty(userBranches)) {
                branch = userBranches.get(0);
            }
            team.setTeamBranch(branch);
            Set<User> userSet = new HashSet<User>();
            userSet.add(user);

            team.setUsers(userSet);
            entityDao.persist(team);
            return team;
        }
        return null;

    }

    @Override
    public List<String> getTeamUsernamesByTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(QUERY_TEAM_USERNAMES_FOR_TEAM_ID)
        		.addParameter("teamId", teamId)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public Long getTeamIdsLedByThisUserName(String username) {

        NeutrinoValidator.notNull(username, "User name can not be null");

        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamIdsLeadedByThisUsername")
                .addParameter("userName", username)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<Long> ids = entityDao.executeQuery(executor);
        if (!ids.isEmpty()) {
            return ids.get(0);
        } else
            return null;

    }

    @Override
    @MonitoredWithSpring(name = "TSL_USR_SET_BY_BRANCH")
    public Set<User> getUserSetByBranch(Long userId, Long branchId) {

        BaseLoggers.flowLogger.info("Fetching User Set for user Id : = " + userId + " and Branch Id : =" + branchId);

        List<Long> teamId = getTeamIdForUser(userId);
        Set<User> userSet = new HashSet<User>();

        User user = entityDao.find(User.class, userId);

        if (CollectionUtils.isNotEmpty(teamId)) {
            userSet = prepareUserSet(teamId, branchId, userSet, user);
            userSet.remove(user);

        } else {
            userSet.add(user);
        }

        return userSet;
    }
    
    @Override
    @MonitoredWithSpring(name = "TSL_USR_SET_BY_BRANCH_LIST")
    public Set<User> getUserSetByBranchList(Long userId, List<Long> branchId) {

        BaseLoggers.flowLogger.info("Fetching User Set for user Id : = " + userId);

        List<Long> teamId = getTeamIdForUser(userId);
        Set<User> userSet = new HashSet<User>();

        User user = entityDao.find(User.class, userId);

        if (CollectionUtils.isNotEmpty(teamId)) {
            userSet = prepareUserSetBranchList(teamId, branchId, userSet, user);
            userSet.remove(user);

        } else {
            userSet.add(user);
        }

        return userSet;
    }    

    @Override
    @MonitoredWithSpring(name = "TSL_TEAM_SET_BY_USR")
    public List<String> getTeamSetByCurrentUser(Long userId) {

        BaseLoggers.flowLogger.info("Fetching Team Set for user Id : = " + userId);

        List<Long> teamId = getTeamIdForUser(userId);
        Set<Team> teamSet = new HashSet<Team>();
        List<String> teamUris = null;

        if (CollectionUtils.isNotEmpty(teamId)) {
            teamSet = prepareTeamSet(teamId, teamSet, userId);

        } else {
            // In case if current user is not team lead
            List<Team> teamList = getTeamsAssociatedToUserByUserId(userId);
            if (CollectionUtils.isNotEmpty(teamList)) {
                teamSet.addAll(teamList);
            }
        }

        if (CollectionUtils.isNotEmpty(teamSet)) {
            teamUris = new ArrayList<String>();
            for (Team team : teamSet) {
                teamUris.add(team.getUri());
            }
        }

        return teamUris;
    }

    @Override
    public List<Long> getAssociatedUserIds(Long teamId, User currentUser) {
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getassosiatedUserId")
                .addParameter("teamId", teamId)
                .addParameter("userId", currentUser.getId())
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public List<User> getAssociatedUsers(Long teamId, Long userId) {
        NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>("Team.fetchUsersForTeamLead")
                .addParameter("teamId", teamId).addParameter("userId", userId)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public List<User> getUserByBranchFromUsers(Long branchId, List<Long> userIds) {
        NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>("Users.InCurrentBranchAndTeam")
                .addParameter("userIds", userIds).addParameter("branchId", branchId)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }
    
    @Override
    public List<User> getUserByBranchListFromUsers(List<Long> branchId, List<Long> userIds) {
        NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>("Users.InCurrentBranchListAndTeam")
                .addParameter("userIds", userIds).addParameter("branchId", branchId)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    private List<Long> getTeamIdForUser(Long userId) {
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getTeamIdsLeadedByThisUser")
        		.addParameter("userId", userId)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        return entityDao.executeQuery(executor);

    }

    /**
     * 
     * Method to prepare Unique Team Set
     * @param teamIds
     * @param teamSet
     * @param userId
     * @return
     */

    private Set<Team> prepareTeamSet(List<Long> teamIds, Set<Team> teamSet, Long userId) {

        if (CollectionUtils.isNotEmpty(teamIds)) {

            List<Long> userList = null;

            List<Long> teamIdForSubsequentTeamLeads = null;
            Map<Long,Team> teamIdMap=new HashMap<Long,Team>();

            if(CollectionUtils.isNotEmpty(teamSet)){
            	for(Team team:teamSet){
            		teamIdMap.put(team.getId(),team);
            		/*teamIdsList.add(team.getId());*/
            	}
            }
            // teamIds of super team
            for (Long teamId : teamIds) {
				if(teamIdMap.containsKey(teamId)){
            		continue;
            	}
                teamSet.add(entityDao.find(Team.class, teamId));
                // get user id's for specific team
                BaseLoggers.flowLogger.info("Iteration for Team" + teamId);

                userList = getAssociatedUsersId(teamId, userId);

                if (CollectionUtils.isNotEmpty(userList)) {

                    for (Long user : userList) {

                        BaseLoggers.flowLogger.info("Iteration for User" +user);

                        teamIdForSubsequentTeamLeads = getTeamIdForUser(user);
                        prepareTeamSet(teamIdForSubsequentTeamLeads, teamSet, user);
                    }

                }
            }

        }
        return teamSet;
    }

    /**
     * 
     * Method to prepare unique user set
     * @param teamIds
     * @param branchId
     * @param userSet
     * @param currentUser
     * @return
     */

    private Set<User> prepareUserSet(List<Long> teamIds, Long branchId, Set<User> userSet, User currentUser) {



        if (CollectionUtils.isNotEmpty(teamIds)) {
        	List<Long> userIds = null;
            List<User> userList = null;

            List<Long> teamIdForSubsequentTeamLeads = null;
            Map<Long,User> userIdMap=new HashMap<Long,User>();

            if(CollectionUtils.isNotEmpty(userSet)){
            	for(User user:userSet){
            		userIdMap.put(user.getId(),user);
            	}
            }
            // teamIds of super team
            for (Long teamId : teamIds) {
                // get user id's for specific team
                BaseLoggers.flowLogger.info("Iteration for Team" + teamId);

                userIds = getAssociatedUserIds(teamId, currentUser);

                if (CollectionUtils.isNotEmpty(userIds)) {
                    // filter above user list based on branch

                    userList = getUserByBranchFromUsers(branchId, userIds);

                    if (CollectionUtils.isNotEmpty(userList)) {                  
                        for (User user : userList) {
                            BaseLoggers.flowLogger.info("Iteration for User" + user.getId());
                            if(userIdMap.containsKey(user.getId())){
                            	continue;
                            }
                             userSet.add(user);
                            if (null != user.getTeamLead() && user.getTeamLead()) {
                                teamIdForSubsequentTeamLeads = getTeamIdForUser(user.getId());                            
                                prepareUserSet(teamIdForSubsequentTeamLeads, branchId, userSet, user);

                            } 
                        }

                    } 
                }
            }
    
            }
        return userSet;
    }
    
    
    /**
     * 
     * Method to prepare unique user set
     * @param teamIds
     * @param branchId
     * @param userSet
     * @param currentUser
     * @return
     */

    private Set<User> prepareUserSetBranchList(List<Long> teamIds, List<Long> branchId, Set<User> userSet, User currentUser) {



        if (CollectionUtils.isNotEmpty(teamIds)) {
        	List<Long> userIds = null;
            List<User> userList = null;

            List<Long> teamIdForSubsequentTeamLeads = null;
            Map<Long,User> userIdMap=new HashMap<Long,User>();

            if(CollectionUtils.isNotEmpty(userSet)){
            	for(User user:userSet){
            		userIdMap.put(user.getId(),user);
            	}
            }
            // teamIds of super team
            for (Long teamId : teamIds) {
                // get user id's for specific team
                BaseLoggers.flowLogger.info("Iteration for Team" + teamId);

                userIds = getAssociatedUserIds(teamId, currentUser);

                if (CollectionUtils.isNotEmpty(userIds)) {
                    // filter above user list based on branch

                    userList = getUserByBranchListFromUsers(branchId, userIds);

                    if (CollectionUtils.isNotEmpty(userList)) {                  
                        for (User user : userList) {
                            BaseLoggers.flowLogger.info("Iteration for User" + user.getId());
                            if(userIdMap.containsKey(user.getId())){
                            	continue;
                            }
                             userSet.add(user);
                            if (null != user.getTeamLead() && user.getTeamLead()) {
                                teamIdForSubsequentTeamLeads = getTeamIdForUser(user.getId());                            
                                prepareUserSetBranchList(teamIdForSubsequentTeamLeads, branchId, userSet, user);

                            } 
                        }

                    } 
                }
            }
    
            }
        return userSet;
    }
    

    @Override
    public List<Team> getTeamNotLeadByThisUser(Long userId) {
        NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getTeamsNotLeadByThisUser")
        		.addParameter("userId", userId)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

       return entityDao.executeQuery(executor);
    }

    @Override
    @MonitoredWithSpring(name = "TSL_TEAM_HIERARCHY_BY_USR")
    public List<String> getTeamHierarchyOfcurrentUser(Long userId) {
        List<String> teamUris = new ArrayList<String>();

        List<String> downwardsTeamUris = getTeamSetByCurrentUser(userId);
        getTeamSetByCurrentLead(userId, teamUris);

        if (CollectionUtils.isNotEmpty(downwardsTeamUris)) {
            teamUris.addAll(downwardsTeamUris);
        }
        return teamUris;
    }

	@Override
	@MonitoredWithSpring(name = "TSL_TEAM_SET_BY_USR_LEAD")
	public List<String> getTeamSetByCurrentLead(Long userId, List<String> teamUris) {

		BaseLoggers.flowLogger.info("Fetching Team Set for user Id : = " + userId);

		List<Team> teamList = getTeamNotLeadByThisUser(userId);
		if (CollectionUtils.isEmpty(teamList)) {
			return teamUris;
		}
		for (Team team : teamList) {
			if (!teamUris.contains(team.getUri())) {
				teamUris.add(team.getUri());
				if (team.getTeamLead() != null) {
					getTeamSetByCurrentLead(team.getTeamLead().getId(), teamUris);
				}
			}
		}
		return teamUris;
	}

    @Override
    @MonitoredWithSpring(name = "TSL_TEAM_ID_SET_BY_USR")
    public List<Long> getTeamIdSetByCurrentUser(Long userId) {

        BaseLoggers.flowLogger.info("Fetching Team Set for user Id : = " + userId);

        
        Set<Long> teamIdSet = new HashSet<Long>();
        
        List<Long> teamList = getTeamIdAssociatedToUserByUserId(userId);
        if (CollectionUtils.isNotEmpty(teamList)) {
            teamIdSet.addAll(teamList);
        }
        
        List<Long> teamId = getTeamIdForUser(userId);

        if (CollectionUtils.isNotEmpty(teamId)) {
            teamIdSet = prepareTeamIdSet(teamId, teamIdSet, userId);

        } /*else {
            // In case if current user is not team lead
            List<Long> teamList = getTeamIdAssociatedToUserByUserId(userId);
            if (CollectionUtils.isNotEmpty(teamList)) {
                teamIdSet.addAll(teamList);
            }
        }*/

        return new ArrayList<Long>(teamIdSet);
    }

    @Override
    public List<Long> getAssociatedUsersId(Long teamId, Long userId) {
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.fetchUsersIdForTeamLead")
                .addParameter("teamId", teamId).addParameter("userId", userId)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    private Set<Long> prepareTeamIdSet(List<Long> teamIds, Set<Long> teamSet, Long userId) {

        if (CollectionUtils.isNotEmpty(teamIds)) {

            List<Long> userIdList = null;

            List<Long> teamIdForSubsequentTeamLeads = null;

            // teamIds of super team
            for (Long teamId : teamIds) {
            	if(teamSet.contains(teamId)){
            		continue;
            	}
                teamSet.add(teamId);
                // get user id's for specific team
                BaseLoggers.flowLogger.info("Iteration for Team" + teamId);

                userIdList = getAssociatedUsersId(teamId, userId);

                if (CollectionUtils.isNotEmpty(userIdList)) {

                    for (Long usersId : userIdList) {

                        BaseLoggers.flowLogger.info("Iteration for User" + usersId);

                        teamIdForSubsequentTeamLeads = getTeamIdForUser(usersId);
                        prepareTeamIdSet(teamIdForSubsequentTeamLeads, teamSet, usersId);
                    }

                }
            }

        }
        return teamSet;
    }

    @Override
    public List<Long> getUserIdByTeamId(List<Long> teamIds) {
        NeutrinoValidator.notNull(teamIds, "Team Ids can not be null");
        if (CollectionUtils.isEmpty(teamIds)) {
            return Collections.emptyList();
        }
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.fetchTeamUsersIdForTeamIdList")
        		.addParameter("teamIdList", teamIds)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public List<String> getDistinctTeamLeadUrisOfThisUser(Long userId) {
        NeutrinoValidator.notNull(userId, "user Id can not be null");
        if (userId == null) {
            return Collections.emptyList();
        }
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getDistinctTeamLeadIdsOfThisUser")
                .addParameter("userId", userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<Long> idList = entityDao.executeQuery(executor);
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }
        List<String> uriList = new ArrayList<String>();
        for (Long id : idList) {
            if (id != null) {
                String uri = User.class.getName() + ":" + id;
                uriList.add(uri);
            }
        }

        return uriList;

    }

    @Override
    public void checkIfTeamExistOrNot(User user) {
        NeutrinoValidator.notNull(user, "user cannot be null");
        /*check if current user has any team associated with it or not */
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getNoOfTeamsRepresentedBy").addParameter(
                "userId", user.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        Long teamsRepresentedByUser = entityDao.executeQueryForSingleValue(executor);

        /*check if current user is a leader of any team or not */
        NamedQueryExecutor<Long> teamExecutor = new NamedQueryExecutor<Long>("Team.getLeaderOfNumberOfTeams").addParameter(
                "userId", user.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        Long teamsLeadByUser = entityDao.executeQueryForSingleValue(teamExecutor);

        /*if current user is neither team lead not has any team*/
        if (teamsRepresentedByUser == 0 && teamsLeadByUser == 0) {
            Team team = new Team();
            team.setActiveFlag(true);
            team.setPersistenceStatus(0);
            team.setApprovalStatus(0);
            team.setDescription("Team created when user is marked as Team Lead");
            team.setName(user.getUsername() + "_Team");
            team.setEmail(user.getUsername() + "@testmail.com");
            team.setTeamLead(user);
            List<OrganizationBranch> userBranches = userManagementService.getUserPrimaryBranch(user.getId());
            OrganizationBranch branch = null;
            if (CollectionUtils.isNotEmpty(userBranches)) {
                branch = userBranches.get(0);
            }
            team.setTeamBranch(branch);
            Set<User> userSet = new HashSet<User>();
            userSet.add(user);

            team.setUsers(userSet);
            entityDao.persist(team);
        }

    }

    @Override
    public Boolean checkTeamOfUser(User user) {
        Boolean teamLeadByUser = false;
        NeutrinoValidator.notNull(user, "user cannot be null");
        /*check if current user has any team associated with it or not */
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Team.getNoOfTeamsRepresentedBy").addParameter(
                "userId", user.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        Long teamsRepresentedByUser = entityDao.executeQueryForSingleValue(executor);

        /*check if current user is a leader of any team or not */
        NamedQueryExecutor<Long> teamExecutor = new NamedQueryExecutor<Long>("Team.getLeaderOfNumberOfTeams").addParameter(
                "userId", user.getId()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        Long teamsLeadByUser = entityDao.executeQueryForSingleValue(teamExecutor);
        if (teamsRepresentedByUser == 0 && teamsLeadByUser == 0) {
            teamLeadByUser = false;
        } else {
            teamLeadByUser = true;
        }

        return teamLeadByUser;
    }

    @Override
    public Team getTeamByUri(String teamUri) {
        NeutrinoValidator.notNull(teamUri, "Team Uri cannot be null");
        Team team=entityDao.find(Team.class, EntityId.fromUri(teamUri).getLocalId());
        if(team!=null)
        return team;
        
        return null;
    }
    
    @Override
    public List<User> getTeamUsersByTeamIds(List<Long> teamIds) {
        NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>("Team.getUsersFromTeamIds")
        		.addParameter("teamIds", teamIds)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        return (List<User>) entityDao.executeQuery(executor);
    }

    @Override
    public List<Team> getTeamsByTeamIds(List<Long> teamIds) {
        NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getTeamsFromTeamIds").addParameter(
                "teamIds", teamIds);
        return (List<Team>) entityDao.executeQuery(executor);
    }

    @Override
    public List<String> getUsersUriForAllTeamsLeadByUser(UserInfo userInfo){

        List<Long> teamId = getTeamIdForUser(userInfo.getId());
        List<User> teamUsers = getTeamUsersByTeamIds(teamId);
        List<String> userUris = new ArrayList<String>();
        for(User user: teamUsers){

            if(!user.getUri().equals(userInfo.getUserEntityId().getUri())){
                userUris.add(user.getUri());
            }
        }

        userUris.add(userInfo.getUserEntityId().getUri());
        return userUris;
    }

    @Override
    public List<String> getUsersUriByTeamIds(List<Long> teamIds){
    NamedQueryExecutor<User> executor = new NamedQueryExecutor<User>("Team.getUsersFromTeamIds")
    		.addParameter("teamIds", teamIds)
    		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        List<User> users = (List<User>) entityDao.executeQuery(executor);
        List<String> userUris = new ArrayList<String>();
        for(User user: users){
            if(!userUris.contains(user.getUri())){
                userUris.add(user.getUri());
                }
            }

        return userUris;
    }

    @Override
    public Set<Long> getAssociatedUsersIdByTeamName(String teamName) {
        Team team = getTeamByTeamName(teamName);
        if (team != null) {
            Set<User> userSet = team.getUsers();
            if (userSet != null) {
                Hibernate.initialize(userSet);
                List<User> userList = new ArrayList<User>(userSet);
                Set<Long> userIdSet = new HashSet<Long>();
              for(User user:userList)
              {
                  userIdSet.add(user.getId());
              }
                return userIdSet;
            }
        }
        return Collections.emptySet();
    }
    
    @Override
    public boolean isThisTeamNamePresent(String teamName) {
    	boolean teamNamePresent = Boolean.FALSE;
    	List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<Long> namedQuery = new NamedQueryExecutor<Long>("Team.getNoOfTeamsByTeamName")
								        		.addParameter("approvalStatusList", statusList).addParameter("teamName", teamName);
        Long count = entityDao.executeQueryForSingleValue(namedQuery);
        if (count > 0) {
			teamNamePresent = Boolean.TRUE;
		}
		return teamNamePresent;
    }
    
    @Override
	public List<Map<String, String>> getUserIdNameMapByTeamId(Long teamId) {
        NeutrinoValidator.notNull(teamId, "team id can not be null");
        BaseLoggers.flowLogger.info("getUserIdNameMapByTeamId " + teamId);
        NamedQueryExecutor<Map<String,String>> executor = new NamedQueryExecutor<Map<String,String>>("Team.getUserIdNameMapByTeamId")
        		.addParameter("teamId", teamId)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        return entityDao.executeQuery(executor);
	}

    public List<Team> getAllTeamByTeamName(String teamName) {
        NeutrinoValidator.notNull(teamName, "team name can not be null");
        NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.getAllTeamByTeamName").addParameter("teamName", teamName)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

}
