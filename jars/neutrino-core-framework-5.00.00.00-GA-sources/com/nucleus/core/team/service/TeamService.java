/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - Â© 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.team.service;

import java.util.List;
import java.util.Set;
import java.util.Map;

import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.team.entity.Team;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

/**
 * @author Nucleus Software Exports Limited
 */
public interface TeamService {

    /**
     * @Description
     * returns all the teams present in database
     */
    public List<Team> getAllTeams();

    /**
     * @Description
     * returns the team object using the team name
     */
    public Team getTeamByTeamName(String teamName);

    /**
     * @Description
     * returns the team object using the team id
     */
    public Team getTeamByTeamId(Long teamId);
    
    
    public Team getTeamByUri(String teamUri);

    /**
     * @Description
     * returns the set of userinfo of the associated users of a team by its team id
     */
    public Set<UserInfo> getAssociatedUsersOfTeamByTeamId(Long teamId);

    /**
     * @Description
     * returns the set of userinfo of the associated users of a team by its team name
     */
    public Set<UserInfo> getAssociatedUsersOfTeamByTeamName(String teamName);

    /**
     * @Description
     * returns the teams which has the user with passed userId as the associated user
     */
    public List<Team> getTeamsAssociatedToUserByUserId(Long userId);

    /**
     * @Description
     * saves the Team
     */
    public void saveTeam(Team team);

    /**
     * @Description
     * deletes the team
     */
    public void deleteTeam(Team team);

    /**
     * @Description
     * get all the Teams which is in any of the branch alloted to user but the team don't have the 
     * passed user as the associated user i,e; team is eligible to be made as user's team
     */
    public List<Team> getTheEligibleTeamsNotAssociatedToThisUser(User user);

    /**
     * @Description
     * get the number of teams led by this user
     */
    public Long getNumberOfTeamsLedByThisUser(User thisUser);

    /**
     * @Description
     * get the number of teams represented by this user
     */
    public Long getNoOfTeamsRepresentedByThisUser(User thisUser);

    /**
     * @Description
     * get all the teams led by this user
     */
    public List<Team> getTeamsLedByThisUser(UserInfo user);
    
    
    public List<Team> getTeamsLedByThisUser(UserInfo user, List<Integer> approvalStatusList, boolean activeFlag);
    

    /**
     * @Description
     * get all the teams led by this user
     */
    public List<Team> getTeamsLedByThisUserInLoggedInBranch(UserInfo user);

    /**
     * @Description
     * get all the team-uri's represented by this user
     */
    public List<String> getTeamByUrisForUserId(UserInfo userInfo);
    
    public List<String> getTeamByUrisForUserIdWithoutLoggedInBranch(UserInfo userInfo);

    /**
     * @Description
     * get all the users present in branch of this team
     */
    public List<UserInfo> getAllUsersPresentInBranchOfThisTeam(Team team);

    /**
     * @Description
     * This function is to get All Teams Of Those Branches Which Contains Teams Of This User
     */
    public List<Team> getAllTeamsOfBranchesContainingTeamsOfThisUser(User user);

    /**
     * @Description
     * get all the teams represented by user in its logged In branch
     */
    public List<Team> getTeamsOfUserInLoggedInBranch(UserInfo userInfo);

    /**
     * @Description
     * get all the teams present in logged In branch of the passed user
     */
    public List<Team> getAllTeamsOfLoggedInBranchOfThisUser(UserInfo userInfo);

    /**
     * @Description
     * get all the teams present in the passed branch
     */
    public List<Team> getAllTeamsOfThisBranch(OrganizationBranch branch);

    /**
     * Checks if is user team lead.
     *
     * @param userInfo the user info
     * @return true, if is user team lead
     */
    public boolean isUserTeamLead(UserInfo userInfo);

    /**
     * Gets the team uri from team id.
     *
     * @param teamId the team id
     * @return the team uri for team id
     */
    public String getTeamUriForTeamId(Long teamId);

    /**
     * Gets the team name from team id.
     *
     * @param teamId the team id
     * @return the team name for team id
     */
    public String getTeamNameForTeamId(Long teamId);

    /**
     * Gets all team ids (Approved - 0,3,4,6) associated to user by user id.
     *
     * @param userId the user id
     * @return the team id associated to user by user id
     */
    public List<Long> getTeamIdAssociatedToUserByUserId(Long userId);

    /**
     * Gets all team ids (Approved/Unapproved - All) associated to user by user id.
     *
     * @param userId the user id
     * @return the team id associated to user by user id
     */

    public List<Long> getAllTeamIdAssociatedToUserByUserId(Long userId);

    /**
     * Gets the team branch name associated with team from team id.
     *
     * @param teamId the team id
     * @return the team branch name for team id
     */
    public String getTeamBranchNameForTeamId(Long teamId);

    /**
     * Gets the team lead from team id.
     *
     * @param teamId the team id
     * @return the team lead for team id
     */
    public User getTeamLeadForTeamId(Long teamId);

    /**
     * Gets the team users by team id.
     * All users will be fetched irrespective of user status
     * @param teamId the team id
     * @return the team users by team id
     */
    public Set<User> getTeamUsersByTeamId(Long teamId);
    
    /**
     * Retrieves all users by team id & userStatus.
     * @param teamId the team id
     * @param userStatus 
     */
    public Set<User> getTeamUsersByTeamIdAndUserStaus(Long teamId,List<Integer> userStatus);

    /**
     * Gets the team uri by team name.
     *
     * @param teamName the team name
     * @return the team uri by team name
     */
    public String getTeamUriByTeamName(String teamName);

    /**
     * Gets the teams id of associated user in logged in branch.
     *
     * @param userInfo the user info
     * @return the teams id of user in logged in branch
     */
    public List<Long> getTeamsIdOfUserInLoggedInBranch(UserInfo userInfo);

    /**
     * Gets the team branch calendar by team id.
     *
     * @param teamId the team id
     * @return the team branch calendar by team id
     */
    public BranchCalendar getTeamBranchCalendarByTeamId(Long teamId);

    /**
     * Gets the teams id of associated user in logged in branch.
     *
     * @param userInfo the user info
     * @return the teams id of user in logged in branch
     */
    public List<Team> getAllTeamsOfLoggedInBranch();

    public List<User> getAllTeamLeads();

    public List<Team> getAllTeamsOfLoggedInAllBranch(OrgBranchInfo branchInfo);

    public List<String> getTeamByUrisForAllBranches(UserInfo userInfo);

    public List<Long> getAllTeamIdsOfBranchesContainingTeamsOfThisUser(User user);

    // For Performance T

    public List<Long> getTeamIdsLedByThisUserInLoggedInBranch(UserInfo user);

    public List<Object[]> getAssociatedUserIdsAndUserNamesOfTeamByTeamId(Long teamId);

    public List<Long> getTeamIdsOfUserInLoggedInBranch(UserInfo userInfo);

    public List<Long> getTeamIdsLedByThisUser(UserInfo user);

    public Long getTeamLeadIdForTeamId(Long teamId);

    public List<Long> getAllTeamIdsOfLoggedInBranchOfThisUser(UserInfo userInfo);

    public List<Object[]> getAllTeamIdsAndNamesOfThisBranch(OrganizationBranch branch);

    public List<Object[]> getAllTeamIdsAndNamesOfLoggedInBranchOfThisUser(UserInfo userInfo);

    public List<Object[]> getAllTeamIdsAndNames();

    public List<Object[]> getTeamIdsAndNamesLedByThisUserInLoggedInBranch(UserInfo user);

    public List<Object[]> getTeamIdsAndNamesOfUserInLoggedInBranch(UserInfo userInfo);

    public Set<Object[]> getTeamUserIdsAndNamesByTeamId(Long teamId);

    public List<Long> getAllTeamIds();

    public List<String> getTeamUrisForUser(UserInfo userInfo);

    public OrganizationBranch getTeamBranchForTeamId(Long teamId);

    public List<Long> getTeamIdsLedOfThisUserId(Long userId);

    public Long getTeamBranchIdByTeamId(Long teamId);

    public Team createTeamIfNotExist(User user);

    List<Team> getTeamsLedByThisUserInBranch(UserInfo user, Long branchId);

    public List<String> getTeamUsernamesByTeamId(Long teamId);

    public Long getTeamIdsLedByThisUserName(String username);

    public List<Long> getAssociatedUserIds(Long teamId, User currentUser);

    public List<User> getTeamUsersByTeamId(UserInfo userInfo);

    public List<User> getUserByBranchFromUsers(Long branchId, List<Long> userIds);
    
    public List<User> getUserByBranchListFromUsers(List<Long> branchId, List<Long> userIds);

    public Set<User> getUserSetByBranch(Long userId, Long branchId);
    
    public Set<User> getUserSetByBranchList(Long userId, List<Long> branchId);

    public List<String> getTeamSetByCurrentUser(Long userId);

    public List<User> getAssociatedUsers(Long teamId, Long userId);

    public List<Team> getTeamNotLeadByThisUser(Long userId);

    public List<String> getTeamSetByCurrentLead(Long userId, List<String> teamUris);

    public List<String> getTeamHierarchyOfcurrentUser(Long userId);

    /**
     * Gets the all teams id associated with current logged in user.
     *
     * @param userId the user id
     * @return the all teams id
     */
    public List<Long> getTeamIdSetByCurrentUser(Long userId);

    /**
     * Gets the all user id's associated with team and current user.
     *
     * @param teamId the team id
     * @param userId the user id
     * @return the all user id.
     */
    public List<Long> getAssociatedUsersId(Long teamId, Long userId);

    public List<Long> getTeamUserIdByTeamId(UserInfo userInfo);

    public List<Long> getUserIdByTeamId(List<Long> teamIds);

    public Set<Long> getTeamUserIdsByTeamId(Long teamId);

    /**
     * Gets the all teamleads uris of teams associated with current logged in user.
     *
     * @param userId the user id
     * @return the all teamleads uris
     */
    public List<String> getDistinctTeamLeadUrisOfThisUser(Long userId);
    
    public void checkIfTeamExistOrNot(User user);
    
    public Boolean checkTeamOfUser(User user) ;
    
    
    /**
     * Gets the user list from team id list.
     *
     * @param teamIds list of team id
     * @return list of users
     */
    public List<User> getTeamUsersByTeamIds(List<Long> teamIds);
    /**
     * Gets the team list from team id list.
     *
     * @param teamIds list of team id
     * @return list of team
     */
    public List<Team> getTeamsByTeamIds(List<Long> teamIds);

	/**
	 * 
	 * @param team
	 */
    void saveTeamsForUser(Team team);
    
    public Set<Long> getAssociatedUsersIdByTeamName(String teamName);

    List<String> getUsersUriByTeamIds(List<Long> teamIds);

    List<String> getUsersUriForAllTeamsLeadByUser(UserInfo userInfo);

	boolean isThisTeamNamePresent(String teamName);
    
	public List<Map<String, String>> getUserIdNameMapByTeamId(Long teamId);

    public List<Team> getAllTeamByTeamName(String teamName);

    public List<UserInfo> getAllUsersPresentInTeamBranch(OrganizationBranch branch);
}
