package com.nucleus.core.team.managementService;

import java.util.Set;

import com.nucleus.core.team.entity.Team;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

public interface TeamManagementService {
	/**
     * @Description
     * adds a group of passed users to the associated users of the passed team
     */
    public void addUsersToThisTeam(Set<UserInfo> userInfos, Team team);
    
    /**
     * @Description
     * adds the passed user to the associated users of the passed team
     */
    public void addUserToThisTeam(UserInfo userInfo, Team team);
    
    /**
     * @Description
     * removes a group of passed users from the associated users of the passed team
     */
    public void removeUsersFromThisTeam(Set<User> users, Team team);

    /**
     * @Description
     * removes the passed user from the associated users of the passed team
     */
    public void removeUserFromThisTeam(User user, Team team);
    
    /**
     * @Description
     * adds the passed user to the associated users of all the passed teams
     */
    public void allocateTeamsToThisUser(Set<Team> teams, UserInfo userInfo);

    /**
     * @Description
     * removes the passed user from the associated users of all the passed teams if it exists in that team
     */
    public void deallocateTeamsToThisUser(Set<Team> teams, User user);

    /**
     * @Description
     * changes the team leader of a team
     */
    public void changeTeamLeader(Team team,UserInfo user);

}
