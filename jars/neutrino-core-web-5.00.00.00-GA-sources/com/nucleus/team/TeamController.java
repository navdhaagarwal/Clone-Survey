package com.nucleus.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.autocomplete.AutocompleteVO;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.persistence.HibernateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import com.nucleus.html.util.HtmlUtils;

import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.managementService.TeamManagementService;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.autocomplete.AutocompleteService;
import com.nucleus.core.organization.service.OrganizationService;
@Controller
@RequestMapping(value = "/Team")
@SessionAttributes("team")
public class TeamController extends BaseController {

    @Inject
    @Named("teamService")
    private TeamService           teamService;


    @Inject
    @Named("organizationService")
    private OrganizationService  organizationService;

    @Inject
    @Named("autocompleteService")
    private AutocompleteService  autocompleteService;


    @Inject
    @Named("teamManagementService")
    private TeamManagementService teamManagementService;

    @Inject
    @Named("userService")
    private UserService           userService;

    @Inject
    @Named("makerCheckerService")
    MakerCheckerServiceImpl       makerCheckerService;

    static final String  masterID      = "Team";


    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() {
        return Team.class.getName();
    }

    /*for Add/Remove Team's Users
     */

    @PreAuthorize("hasAuthority('MAKER_TEAM') or hasAuthority('CHECKER_TEAM') or hasAuthority('VIEW_TEAM')")
    @ResponseBody
    @RequestMapping(value = "/CheckName/{teamName}", method = RequestMethod.GET)
    public String checkName(@PathVariable String teamName, ModelMap map) {
        if (teamService.isThisTeamNamePresent(teamName))
            return "UnAvailable";

        return "Available";
    }

    @ResponseBody
    @RequestMapping(value = "/changeTeamLeader/{teamName}/{teamLeader}", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    public String changeTeamLeader(@PathVariable String teamName, @PathVariable String teamLeader, ModelMap map) {
        Team team = teamService.getTeamByTeamName(teamName);
        teamManagementService.changeTeamLeader(team, userService.getUserFromUsername(teamLeader));

        return teamLeader;
    }

    @ResponseBody
    @RequestMapping(value = "/removeTeamLeader/{teamName}", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    public String removeTeamLeader(@PathVariable String teamName, ModelMap map) {
        Team team = teamService.getTeamByTeamName(teamName);
        team.setTeamLead(null);
        teamService.saveTeam(team);
        return "";
    }

    private List<UserInfo> getTheUsersInTeam(Team team) {
        List<UserInfo> listOfTeamUserInfos = new ArrayList<UserInfo>();
        Set<User> users = team.getApprovedUsers();
        if (CollectionUtils.isNotEmpty(users)) {
            for (User user : users) {
                UserInfo userInfo = new UserInfo(user);
                listOfTeamUserInfos.add(userInfo);
            }
        }
        return listOfTeamUserInfos;
    }

    private List<UserInfo> getTheUsersNotInTeam(Team team) {
        List<UserInfo> notInTeam = teamService.getAllUsersPresentInBranchOfThisTeam(team);
        List<UserInfo> listOfTeamUsers = getTheUsersInTeam(team);
        if(notInTeam != null){
            notInTeam.removeAll(listOfTeamUsers);
        }
        return notInTeam;
    }

    @RequestMapping(value = "/toTransferToTeamTemp", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('MAKER_TEAM') or hasAuthority('CHECKER_TEAM') or hasAuthority('VIEW_TEAM')")
    public String toTransferToTeamTemp(@ModelAttribute("team") Team team, @RequestParam String selectedIdsAvailable,
                                       ModelMap map) {

        Set<UserInfo> userInfos = new HashSet<UserInfo>();
        String[] ids = selectedIdsAvailable.split(" ");
        for (int i = 0 ; i < ids.length ; i++) {
            User user = baseMasterService.getMasterEntityById(User.class, new Long(ids[i]));
            userInfos.add(new UserInfo(user));
        }

        team = insertUsersInTeamTemp(userInfos, team);
        map.put("inTeam", getTheUsersInTeam(team));
        map.put("notInTeam", getTheUsersNotInTeam(team));
        map.put("leader", team.getTeamLead());
        map.put("team", team);
        return "addRemoveTeamMemberTable";
    }

    

    ////autocomplete
	@RequestMapping(value = "/populateTeamBranchAutoComplete")
    @PreAuthorize("hasAuthority('MAKER_TEAM') or hasAuthority('CHECKER_TEAM') or hasAuthority('VIEW_TEAM')")
    @ResponseBody
    public AutocompleteVO filterTeamBranchAutoComplete(ModelMap map, @RequestParam String value,
			@RequestParam String itemVal, @RequestParam String searchCol, @RequestParam String className,
			@RequestParam Boolean loadApprovedEntityFlag, @RequestParam String i_label, @RequestParam String idCurr,
			@RequestParam String content_id, @RequestParam int page, @RequestParam(required = false) String itemsList,
			@RequestParam(required = false) Boolean strictSearchOnitemsList) {

        AutocompleteVO autocompleteVO = new AutocompleteVO();
		String[] searchColumnList = searchCol.split(" ");
		if (strictSearchOnitemsList == null) {
			strictSearchOnitemsList = false;
		}
		if (loadApprovedEntityFlag == null) {
			loadApprovedEntityFlag = false;
		}

		//List<Map<String, ?>> list = organizationService.getOrgBranchesOfBranchTypeByPage(page);
		List<Map<String, ?>> list = organizationService.getOrgBranchesOfBranchTypeByPage(searchCol,value,page);

		int sizeList = 0;

		if (list.size() > 0) {
			Map<String, ?> listMap = list.get(list.size() - 1);
			sizeList = ((Long) listMap.get("size")).intValue();
			list.remove(list.size() - 1);


			//map.put("size", sizeList);
			//map.put("page", page);
			autocompleteVO.setS(sizeList);
			autocompleteVO.setP(page);
		}

		if (i_label != null && i_label.contains(".")) {
			i_label = i_label.replace(".", "");
		}

		//map.put("data", list);
		autocompleteVO.setD(list);
        int i;
        String[] sclHeading=new String[searchColumnList.length];
        for(i=0;i<searchColumnList.length;i++)
        {
            searchColumnList[i]=searchColumnList[i].replace(".", "");
            sclHeading[i]=messageSource.getMessage("label.autoComplete."+searchColumnList[i],null, Locale.getDefault());
        }


        if (idCurr != null && idCurr.trim().length() > 0) {
			idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
		}
		//map.put("idCurr", HtmlUtils.htmlEscape(idCurr));
		//map.put("i_label", i_label);
		//map.put("content_id", content_id);
		//map.put("itemVal", itemVal);
		autocompleteVO.setIc(HtmlUtils.htmlEscape(idCurr));
        autocompleteVO.setIl(i_label);
        autocompleteVO.setCi(content_id);
        autocompleteVO.setIv(itemVal);
        autocompleteVO.setScl(searchColumnList);
        autocompleteVO.setColh(sclHeading);

        return autocompleteVO;

	}

	@RequestMapping(value="/populateScreenDescription")
    @ResponseBody
    public String getScreenDescription(@RequestParam("screenId") Long screenId)
    {
       if(screenId!=null)
       {
            return organizationService.getScreenDescription(screenId);
       }
       return null;
    }

 


    
    @RequestMapping(value = "/toRemoveFromTeamTemp", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    public String toRemoveFromTeamTemp(@ModelAttribute("team") Team team, @RequestParam String selectedIdsExisting,
            ModelMap map) {

        Set<User> users = new HashSet<User>();
        String[] ids = selectedIdsExisting.split(" ");
        for (int i = 0 ; i < ids.length ; i++) {
            users.add(baseMasterService.getMasterEntityById(User.class, Long.parseLong(ids[i])));
        }

        team = removeUsersFromThisTeamTemp(users, team);


        map.put("inTeam", getTheUsersInTeam(team));
        map.put("notInTeam", getTheUsersNotInTeam(team));
        map.put("leader", team.getTeamLead());
        map.put("team", team);

        return "addRemoveTeamMemberTable";

    }

    private Team removeUsersFromThisTeamTemp(Set<User> users, Team team) {
        if (team != null) {
            Set<User> teamUsers = team.getUsers();
            teamUsers.removeAll(users);
            if (users.contains(team.getTeamLead())) {
                team.setTeamLead(null);
            }
            team.setUsers(teamUsers);
        }
        return team;
    }

    private Team insertUsersInTeamTemp(Set<UserInfo> userInfo, Team team) {
        Set<User> users = new HashSet<User>();
        Iterator<UserInfo> iter = userInfo.iterator();
        while (iter.hasNext()) {
            UserInfo uInfo = iter.next();
            User user = baseMasterService.getMasterEntityById(User.class, uInfo.getId());
            users.add(user);
        }
        Set<User> teamUsers = team.getUsers();
        if (CollectionUtils.isNotEmpty(teamUsers)) {
            users.addAll(teamUsers);
        }
        team.setUsers(users);
        return team;
    }

    @RequestMapping(value = "/save")
    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    public String saveTeam(@ModelAttribute("team") Team team, BindingResult result, ModelMap map,
                           @RequestParam("createAnotherMaster") boolean createAnotherMaster, SessionStatus sessionStatus) {
        //Code to check as if any existing(or new) record is being modified(or created) into another existing record
        if (checkTeamName(team,map,result)) {
            return "addRemoveTeamMember";
        }
        boolean eventResult = executeMasterEvent(team,"contextObjectTeam",map);
        if(!eventResult){

            String masterName = team.getClass().getSimpleName();
            String uniqueValue = team.getName();
            String uniqueParameter = "name";
            getActInactReasMapForEditApproved(map,team,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            map.put("viewable" , false);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,team.getReasonActInactMap());
            team.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("inTeam", getTheUsersInTeam(team));
            map.put("notInTeam", getTheUsersNotInTeam(team));
            map.put("leader", team.getTeamLead());
            map.put("viewable" , false);
            map.put("masterID", masterID);
            return "team";
        }

        if (team != null) {
            if (null != team.getAssociatedWithBP() && Team.ASSOCAITED_WITH_BP_Y.equalsIgnoreCase(team.getAssociatedWithBP())){
                persistTeam(team);
            }else {
                if(null != team && null != team.getTeamBranch()&& team.getTeamBranch().getId()!=null)
                {
                    team.setTeamBranch(entityDao.find(OrganizationBranch.class,team.getTeamBranch().getId()));
                }
                ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = team.getReasonActInactMap();
                if(reasonsActiveInactiveMapping != null){
                    saveActInactReasonForMaster(reasonsActiveInactiveMapping,team);
                }
                team.setReasonActInactMap(reasonsActiveInactiveMapping);
                User user = getUserDetails().getUserReference();
                if (null != user) {
                    makerCheckerService.masterEntityChangedByUser(team, user);
                }
            }
        }
        //persistTeam(team);
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            Team teamForCreateAnother= new Team();
            teamForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("team", teamForCreateAnother);
            map.put("masterID", masterID);
            return "team";
        }
        return "redirect:/app/grid/Team/Team/loadColumnConfig";
    }

    private boolean checkTeamName(Team team, ModelMap map, BindingResult result) {
        boolean errorOccuredInThisTeam = Boolean.FALSE;
        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("name", team.getName());
        List<String> colNameList = checkValidationForDuplicates(team, Team.class, validateMap);
        if (result.hasErrors() || (colNameList != null && !colNameList.isEmpty())) {
            String masterName = team.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != team.getId()) {
                Team teamForCode = baseMasterService.findById(Team.class, team.getId());
                uniqueValue = teamForCode.getName();
                uniqueParameter = "name";
                getActInactReasMapForEditApproved(map, team, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                team.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,team.getReasonActInactMap());
            team.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("edit" , true);
            map.put("viewable" , false);
            prepareDataForShowDuplicates(team, map, result, colNameList);
            errorOccuredInThisTeam = Boolean.TRUE;
        }
        return errorOccuredInThisTeam;
    }

    private void prepareDataForShowDuplicates(Team team, ModelMap map,
                                              BindingResult result, List<String> colNameList) {
        map.put("team", team);
        map.put("masterID", masterID);
        if (team.getId() != null) {
            map.put("inTeam", getTheUsersInTeam(team));
            map.put("notInTeam", getTheUsersNotInTeam(team));
            map.put("leader", team.getTeamLead());
            map.put("edit", true);
            map.put("viewable", false);
        }
        /*
         * if List "colNameList" Contains Any Duplicate Values Column Names,
         * Then set them in result
         */
        if (colNameList != null && !colNameList.isEmpty()) {
            for (String c : colNameList) {
                result.rejectValue(c, "label." + c + ".validation.exists");
            }
        }
    }

    @RequestMapping(value = "/saveAndSendForApproval")
    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    public String saveAndApproveTeam(@ModelAttribute("team") Team team, BindingResult result, ModelMap map,
                                     @RequestParam("createAnotherMaster") boolean createAnotherMaster, SessionStatus sessionStatus) {
        //Code to check as if any existing(or new) record is being modified(or created) into another existing record
        if (checkTeamName(team,map,result)) {
            return "addRemoveTeamMember";
        }
        boolean eventResult = executeMasterEvent(team,"contextObjectTeam",map);
        if(!eventResult){

            String masterName = team.getClass().getSimpleName();
            String uniqueValue = team.getName();
            String uniqueParameter = "name";
            getActInactReasMapForEditApproved(map,team,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            map.put("viewable" , false);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,team.getReasonActInactMap());
            team.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("inTeam", getTheUsersInTeam(team));
            map.put("notInTeam", getTheUsersNotInTeam(team));
            map.put("leader", team.getTeamLead());
            map.put("viewable" , false);
            map.put("masterID", masterID);
            return "team";
        }
        // we need to get below logged in user from session
        if (null != team){
            if (null != team.getAssociatedWithBP() && Team.ASSOCAITED_WITH_BP_Y.equalsIgnoreCase(team.getAssociatedWithBP())){
                persistTeam(team);
            }else {
                if (team.getTeamLead() != null && team.getTeamLead().getId() == null) {
                    team.setTeamLead(null);
                } else {
                    User teamLead = new User(team.getTeamLead().getId());
                    team.setTeamLead(teamLead);
                }
                User user = getUserDetails().getUserReference();
                if(null != team && null != team.getTeamBranch()&& team.getTeamBranch().getId()!=null)
                {
                    team.setTeamBranch(entityDao.find(OrganizationBranch.class,team.getTeamBranch().getId()));
                }
                if (user != null) {
                    ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = team.getReasonActInactMap();
                    if(reasonsActiveInactiveMapping != null){
                        saveActInactReasonForMaster(reasonsActiveInactiveMapping,team);
                    }
                    team.setReasonActInactMap(reasonsActiveInactiveMapping);
                    makerCheckerService.saveAndSendForApproval(team, user);
                }
            }
        }

        //persistTeam(team);
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            Team teamForCreateAnother= new Team();
            teamForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("team", teamForCreateAnother);
            map.put("masterID", masterID);
            return "team";
        }
        return "redirect:/app/grid/Team/Team/loadColumnConfig";
    }

    private void persistTeam(Team team) {
        if (team.getTeamLead() != null && team.getTeamLead().getId() == null) {
            team.setTeamLead(null);
        } else {
            User teamLead = new User(team.getTeamLead().getId());
            team.setTeamLead(teamLead);
            String userName=userService.getUserNameByUserId(team.getTeamLead().getId());
            User user=userService.findUserByUsername(userName);
            user.setTeamLead(true);
            userService.saveUser(user);

        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = team.getReasonActInactMap();
        if( team.getReasonActInactMap() != null && team.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
            List<MasterActiveInactiveReasons> reasonInActList = team.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                    .filter(m -> ((m.getReasonInactive() != null))).collect(Collectors.toList());
            List<MasterActiveInactiveReasons> reasonActList = team.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                    .filter(m -> ((m.getReasonActive() != null))).collect(Collectors.toList());

            if (reasonInActList.size() != 0 || reasonActList.size() != 0) {
                if(team.getReasonActInactMap().getReasonDeletedId() != null && team.getReasonActInactMap().getMasterActiveInactiveReasons()!= null && team.getReasonActInactMap().getMasterActiveInactiveReasons().size() > 0)
                    removeDeletedReasons(team.getReasonActInactMap().getReasonDeletedId(), team.getReasonActInactMap().getMasterActiveInactiveReasons());
                saveActInactReasonForMaster(reasonsActiveInactiveMapping, team);
                team.setReasonActInactMap(reasonsActiveInactiveMapping);
            }
            else{
                team.setReasonActInactMap(null);
            }
        }
        teamService.saveTeam(team);
    }

    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    @RequestMapping(value = "/create")
    public String createTeam(ModelMap map) {
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        Team team= new Team();
        team.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",team.getReasonActInactMap());
        map.put("team",team);
        //map.put("masterID", masterID);
        return "team";
    }

    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    @RequestMapping(value = "/saveTeam", method = RequestMethod.POST)
    public String saveTeam(@ModelAttribute("team") Team team, BindingResult result, ModelMap map) {
        BaseLoggers.flowLogger.debug("Saving Team Details-->" + team.getLogInfo());
        //Code to check as if any existing(or new) record is being modified(or created) into another existing record
        team.setReasonActInactMap(null);
        if (checkTeamName(team,map,result)) {
            return "team";
        }
        if(null != team && null != team.getTeamBranch()&& team.getTeamBranch().getId()!=null)
        {
            team.setTeamBranch(entityDao.find(OrganizationBranch.class,team.getTeamBranch().getId()));
        }

        if (null != team){
                if (null != team.getAssociatedWithBP() && Team.ASSOCAITED_WITH_BP_Y.equalsIgnoreCase(team.getAssociatedWithBP())){
                    persistTeam(team);
                }else {
                    User user = getUserDetails().getUserReference();
                    if (user != null) {
                    ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = team.getReasonActInactMap();
                    if(reasonsActiveInactiveMapping != null){
                        saveActInactReasonForMaster(reasonsActiveInactiveMapping,team);
                    }
                    team.setReasonActInactMap(reasonsActiveInactiveMapping);
                    makerCheckerService.masterEntityChangedByUser(team, user);
                }
            }
        }
        //teamService.saveTeam(team);
        return "redirect:/app/grid/Team/Team/loadColumnConfig";
    }

    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    @RequestMapping(value = "/saveAndAddTeam", method = RequestMethod.POST)
    public String saveAndAddTeam(@ModelAttribute("team") Team team, BindingResult result, ModelMap map) {
        //Code to check as if any existing(or new) record is being modified(or created) into another existing record
        team.setReasonActInactMap(null);
        if (checkTeamName(team,map,result)) {
            return "team";
        }


        if (null != team) {
            if (null != team.getAssociatedWithBP() && Team.ASSOCAITED_WITH_BP_Y.equalsIgnoreCase(team.getAssociatedWithBP())) {
                teamService.saveTeam(team);
            } else {
                User user = getUserDetails().getUserReference();
                if (user != null) {
                    if (null != team && null != team.getTeamBranch() && team.getTeamBranch().getId() != null) {
                        team.setTeamBranch(entityDao.find(OrganizationBranch.class, team.getTeamBranch().getId()));
                    }
                    makerCheckerService.masterEntityChangedByUser(team, user);
                }
            }
        }
        //teamService.saveTeam(team);
        if (null != team && !(ApprovalStatus.UNAPPROVED_ADDED == team.getApprovalStatus() || ApprovalStatus.CLONED == team.getApprovalStatus())){
            map.put("teamNameViewMode", true);
        }
        map.put("activeFlag", true);
        map.put("activeFlagApproved", true);
        map.put("editActive", false);
        map.put("viewable", false);
        map.put("create", true);
        map.put("inactiveReasonFlag", false);
        map.put("flagForFirstTimeEdit",false);
        map.put("inTeam", null);
        map.put("notInTeam", getTheUsersNotInTeam(team));
        map.put("leader", team.getTeamLead());
        map.put("team", team);
        map.put("masterID", masterID);
        return "addRemoveTeamMember";
    }

    private Long getNoOfTeamsRepresentedBy(User thisUser) {
        Long count = teamService.getNoOfTeamsRepresentedByThisUser(thisUser);
        return count;
    }

    @RequestMapping(value = "/toTransferToUser", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('MAKER_TEAM') or hasAuthority('CHECKER_TEAM') or hasAuthority('VIEW_TEAM')")
    public String toTransferToUser(ModelMap map, Long idOfUser, String allIdsToTranfer) {
        Set<Team> newUserTeams = new HashSet<Team>();
        Set<Team> finalUserTeams;
        String[] ids = allIdsToTranfer.split(" ");
        for (int i = 0 ; i < ids.length ; i++) {
            Team team = baseMasterService.getMasterEntityById(Team.class, new Long(ids[i]));
            newUserTeams.add(team);
        }
        User user;
        if (idOfUser != null) {
            user = baseMasterService.getMasterEntityById(User.class, idOfUser);
        } else {
            user = new User();
        }
        finalUserTeams = new HashSet<Team>(teamService.getTeamsAssociatedToUserByUserId(user.getId()));
        finalUserTeams.addAll(newUserTeams);
        teamManagementService.allocateTeamsToThisUser(finalUserTeams, userService.getUserById(idOfUser));
        map.put("user", user);
        if (user.getId() != null) {
            map.put("inUser", teamService.getTeamsAssociatedToUserByUserId(user.getId()));
            map.put("notInUser", teamService.getTheEligibleTeamsNotAssociatedToThisUser(user));
            map.put("leaderOfNumberOfTeams", getLeaderOfNumberOfTeams(user));
            map.put("noOfTeamsRepresentedBy", getNoOfTeamsRepresentedBy(user));
        }
        return "addRemoveTeams";
    }

    @RequestMapping(value = "/toRemoveFromUser", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    public String toRemoveFromUser(ModelMap map, Long idOfUser, String allIdsToRemove) {
        List<Team> oldUserTeams = new ArrayList<Team>();
        List<Team> newUserTeams;
        String[] ids = allIdsToRemove.split(" ");
        for (int i = 0 ; i < ids.length ; i++) {
            oldUserTeams.add(baseMasterService.getMasterEntityById(Team.class, new Long(ids[i])));
        }
        User user;
        if (idOfUser != null) {
            user = baseMasterService.getMasterEntityById(User.class, idOfUser);
        } else {
            user = new User();
        }
        newUserTeams = new ArrayList<Team>(teamService.getTeamsAssociatedToUserByUserId(user.getId()));
        newUserTeams.removeAll(oldUserTeams);
        for (int i = 0 ; i < oldUserTeams.size() ; i++) {
            Team team = oldUserTeams.get(i);
            teamManagementService.removeUserFromThisTeam(user, team);
            if (user == team.getTeamLead())
                team.setTeamLead(null);
        }
        map.put("user", user);
        if (user.getId() != null) {
            map.put("inUser", teamService.getTeamsAssociatedToUserByUserId(user.getId()));
            map.put("notInUser", teamService.getTheEligibleTeamsNotAssociatedToThisUser(user));
            map.put("leaderOfNumberOfTeams", getLeaderOfNumberOfTeams(user));
            map.put("noOfTeamsRepresentedBy", getNoOfTeamsRepresentedBy(user));
        }
        return "addRemoveTeams";
    }

    private Long getLeaderOfNumberOfTeams(User thisUser) {
        Long count = teamService.getNumberOfTeamsLedByThisUser(thisUser);
        return count;
    }

    @PreAuthorize("hasAuthority('MAKER_TEAM') or hasAuthority('CHECKER_TEAM')")
    @RequestMapping(value = "/view/{id}")
    public String view(@PathVariable Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        Team thisTeam = baseMasterService.getMasterEntityWithActionsById(Team.class, id, currentUser.getUserEntityId().getUri());
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,thisTeam.getReasonActInactMap());
        thisTeam.setReasonActInactMap(reasonsActiveInactiveMapping);
        String masterName = thisTeam.getClass().getSimpleName();
        String uniqueValue = thisTeam.getName();
        String uniqueParameter = "name";
        getActInactReasMapForEditApproved(map,thisTeam,masterName,uniqueParameter,uniqueValue);
        map.put("team", thisTeam);
        map.put("edit", false);
        map.put("viewable", true);
        map.put("masterID", masterID);
        if (thisTeam != null) {
            map.put("inTeam", getTheUsersInTeam(thisTeam));
            map.put("notInTeam", getTheUsersNotInTeam(thisTeam));
            map.put("leader", thisTeam.getTeamLead());
            getMasterActions(thisTeam,map);
        }
        return "addRemoveTeamMember";
    }

    @SuppressWarnings("unchecked")
    private void getMasterActions(Team team, ModelMap map) {
        if (team.getViewProperties() != null) {
            List<String> actions = (List<String>) team.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }
    }

    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    @RequestMapping(value = "/edit/{id}")
    public String edit(@PathVariable Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        Team thisTeam = baseMasterService.getMasterEntityWithActionsById(Team.class, id, currentUser.getUserEntityId().getUri());
        if (thisTeam != null) {
            map.put("inTeam", getTheUsersInTeam(thisTeam));
            map.put("notInTeam", getTheUsersNotInTeam(thisTeam));
            map.put("leader", thisTeam.getTeamLead());
            getMasterActions(thisTeam,map);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,thisTeam.getReasonActInactMap());
        thisTeam.setReasonActInactMap(reasonsActiveInactiveMapping);
        //getActInactReasMapForEdit(map,thisTeam);
        String masterName = thisTeam.getClass().getSimpleName();
        String uniqueValue = thisTeam.getName();
        String uniqueParameter = "name";
        getActInactReasMapForEditApproved(map,thisTeam,masterName,uniqueParameter,uniqueValue);
        if (null != thisTeam && !(ApprovalStatus.UNAPPROVED_ADDED == thisTeam.getApprovalStatus() || ApprovalStatus.CLONED == thisTeam.getApprovalStatus())){
            map.put("teamNameViewMode", true);
        }
        map.put("team", thisTeam);
        map.put("edit", true);
        map.put("viewable", false);
        map.put("masterID", masterID);
        return "addRemoveTeamMember";
    }

    @RequestMapping(value = "userView/{id}")
    @PreAuthorize("hasAuthority('MAKER_TEAM') or hasAuthority('CHECKER_TEAM') or hasAuthority('VIEW_TEAM')")
    public String userView(@PathVariable Long id, ModelMap map) {
        User thisUser = baseMasterService.getMasterEntityById(User.class, id);
        map.put("user", thisUser);
        if (thisUser != null) {
            map.put("inUser", teamService.getTeamsAssociatedToUserByUserId(thisUser.getId()));
            map.put("notInUser", teamService.getTheEligibleTeamsNotAssociatedToThisUser(thisUser));
            map.put("leaderOfNumberOfTeams", getLeaderOfNumberOfTeams(thisUser));
            map.put("noOfTeamsRepresentedBy", getNoOfTeamsRepresentedBy(thisUser));
            /*to remove this as a comment after making edit function for the same with same code except the commented line
            i,e; this function is currently behaving as a edit function rather than a view function
                    to make it a view function we need to uncomment the below line*/

            /*map.put("viewType","view");*/
        }

        return "addRemoveTeams";
    }

    @RequestMapping(value = "/delete/{id}")
    @PreAuthorize("hasAuthority('MAKER_TEAM')")
    public String deleteTeam(@PathVariable Long[] id, ModelMap map) {
        for (long teamId : id) {
            Team thisTeam = teamService.getTeamByTeamId(teamId);
            if (null != thisTeam){
                if (null != thisTeam.getAssociatedWithBP() && Team.ASSOCAITED_WITH_BP_Y.equalsIgnoreCase(thisTeam.getAssociatedWithBP())){
                    teamService.deleteTeam(thisTeam);
                }else {
                    entityDao.detach(thisTeam);
                    User user = getUserDetails().getUserReference();
                    EntityId updatedById = user.getEntityId();
                    makerCheckerService.masterEntityMarkedForDeletion(thisTeam, updatedById);
                }
            }
            //teamService.deleteTeam(thisTeam);
        }
        return "redirect:/app/grid/Team/Team/loadColumnConfig";
    }


}
