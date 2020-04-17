package com.nucleus.core.team.managementService;



import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.event.EventTypes;
import com.nucleus.event.TeamEvent;
import com.nucleus.master.BaseMasterService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

@Named("teamManagementService")
public class TeamManagementServiceImpl extends BaseServiceImpl implements TeamManagementService{

	@Inject
	@Named("baseMasterService")
	BaseMasterService baseMasterService;
	
	@Inject
	@Named("teamService")
	private TeamService teamService;
	
	 @Override
	    public void addUsersToThisTeam(Set<UserInfo> userInfo, Team team) {
	        Set<User> users = new HashSet<User>();
	        Iterator<UserInfo> iter = userInfo.iterator();
	        String allUsersAddedToThisTeam = "";
	        while (iter.hasNext()) {
	        	UserInfo uInfo = iter.next();
	            User user = uInfo.getUserReference();
	            if (isThisTeamPresentInAnyBranchOfThisUser(team, uInfo)) {
	                users.add(user);
	                allUsersAddedToThisTeam += uInfo.getDisplayName() + " , ";
	            }
	        }
	        users.addAll(team.getUsers());
	        team.setUsers(users);
	        
	        entityDao.saveOrUpdate(team);
	        
	        TeamEvent event = new TeamEvent(EventTypes.USER_ADDED_TO_TEAM_EVENT, true,
            		getCurrentUser().getUserEntityId(), team);
	        event.setTeamName(team.getName());
	        event.setAddedUsers(allUsersAddedToThisTeam);
	        event.setAssociatedUser(getCurrentUser().getDisplayName());
            eventBus.fireEvent(event);
	    }

	    @Override
	    public void addUserToThisTeam(UserInfo userInfo, Team team) {
	        Set<UserInfo> userInfoSet = new HashSet<UserInfo>();
	        userInfoSet.add(userInfo);
	        addUsersToThisTeam(userInfoSet,team);
	    }

	    @Override
	    public void removeUsersFromThisTeam(Set<User> users, Team team) {
	        if (team != null) {
	            Set<User> teamUsers = team.getUsers();
	            teamUsers.removeAll(users);
	            if(users.contains(team.getTeamLead())){
	            	changeTeamLeader(team, null);
	            }
	            team.setUsers(teamUsers);
	            entityDao.saveOrUpdate(team);
	            
	            String allUsersRemovedFromThisTeam = "";
	            Iterator<User> iter = users.iterator();
		        while (iter.hasNext()) {
		            User user = iter.next();
		            allUsersRemovedFromThisTeam += user.getDisplayName() + " , ";
		        }
	            
		        TeamEvent event = new TeamEvent(EventTypes.USER_REMOVED_FROM_TEAM_EVENT, true,
	            		getCurrentUser().getUserEntityId(), team);
	            event.setTeamName(team.getName());
	            event.setRemovedUsers(allUsersRemovedFromThisTeam);
	            event.setAssociatedUser(getCurrentUser().getDisplayName());
	            eventBus.fireEvent(event);
	        }
	    }

	    @Override
	    public void removeUserFromThisTeam(User user, Team team) {
	       Set<User> users = new HashSet<User>();
	       users.add(user);
	       removeUsersFromThisTeam(users,team);
	    }

	    @Override
	    public void allocateTeamsToThisUser(Set<Team> teams, UserInfo userInfo) {
	        if (teams.size() > 0) {
	            Iterator<Team> iter = teams.iterator();
	            while (iter.hasNext()) {
	                Team team = iter.next();
	                addUserToThisTeam(userInfo, team);
	                entityDao.saveOrUpdate(team);
	            }
	        }
	    }

	    @Override
	    public void deallocateTeamsToThisUser(Set<Team> teams, User user) {
	        if (teams.size() > 0) {
	            Iterator<Team> iter = teams.iterator();
	            while (iter.hasNext()) {
	                Team team = iter.next();
	                Set<User> teamUsers = team.getUsers();
	                if (teamUsers.remove(user)) {
	                    team.setUsers(teamUsers);
	                    entityDao.saveOrUpdate(team);
	                }
	            }
	        }
	    }
	    
	    private boolean isThisTeamPresentInAnyBranchOfThisUser(Team team, UserInfo userInfo) {
	    	if(userInfo.getUserBranchList()!=null && userInfo.getUserBranchList().size()>0){
	    		if(userInfo.getUserBranchList().contains(team.getTeamBranch())){
	    			return true;
	    		}
	    		else{
	    			return false;
	    		}
	    	}
	    	NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
	    	"UserManagement.getUserOrgBranchesList").addParameter("userID", userInfo.getId());
	    	List<OrganizationBranch> orgBranchList = entityDao.executeQuery(executor);
	    	if (orgBranchList != null && orgBranchList.contains(team.getTeamBranch())) {
	    		return true;
	    	}
	    	return false;
	    }

		@Override
		public void changeTeamLeader(Team team, UserInfo user) {
			
			TeamEvent event = new TeamEvent(EventTypes.TEAM_LEADER_UPDATED, true,
	            		getCurrentUser().getUserEntityId(), team);
			 event.setTeamName(team.getName());
			if(user != null){
				team.setTeamLead(user.getUserReference());
				event.setTeamLeader(user.getDisplayName());
			}
			else{
				team.setTeamLead(null);
				event.setTeamLeader("Team Leader is removed and No One ");
			}
			teamService.saveTeam(team);
		    eventBus.fireEvent(event);
		}

	   
}
