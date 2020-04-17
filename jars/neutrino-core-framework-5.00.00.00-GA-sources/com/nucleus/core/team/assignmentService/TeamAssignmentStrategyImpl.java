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

package com.nucleus.core.team.assignmentService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import net.bull.javamelody.MonitoredWithSpring;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.task.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.nucleus.address.City;
import com.nucleus.address.Country;
import com.nucleus.address.State;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.team.assignment.SingleMappingOfProductTypeAndTeam;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.core.xml.util.XmlUtils;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.EntityId;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.master.BaseMasterService;
import com.nucleus.process.BPMNProcessService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.task.TaskService;
import com.nucleus.user.UserInfo;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Named("teamAssignmentStrategy")
@MonitoredWithSpring(name = "TeamAssignmentStrategy_Service_IMPL_")
public class TeamAssignmentStrategyImpl extends BaseServiceImpl implements TeamAssignmentStrategy {

    /*start for TeamProductTypeMappingList*/
    private final Map<String, List<SingleMappingOfProductTypeAndTeam>> cacheMapForTeamProductTypeMappingList = new LinkedHashMap<String, List<SingleMappingOfProductTypeAndTeam>>();
    /*end for TeamProductTypeMappingList*/

    @Inject
    @Named("frameworkConfigResourceLoader")
    private NeutrinoResourceLoader                                     resourceLoader;

    @Autowired
    protected ProcessEngine                                            processEngine;

    @Inject
    @Named("bpmnProcessService")
    protected BPMNProcessService                                       bpmnProcessService;

    @Inject
    @Named("platformTaskService")
    TaskService                                                        taskService;

    @Inject
    @Named("masterConfigurationRegistry")
    MasterConfigurationRegistry                                        masterConfigurationRegistry;

    @Inject
    @Named("baseMasterService")
    BaseMasterService                                                  baseMasterService;

    @Inject
    @Named("teamService")
    protected TeamService                                              teamService;

    @Inject
    @Named("organizationService")
    protected OrganizationService                                      organizationService;

    @Override
    public Team getAssignedTeam() {
        Team team = null;
        UserInfo userInfo = getCurrentUser();
        // The user may not have a logged in branch (example in test execution) in which case we would fetch all teams and
        // assign one team randomly
        Long teamId = null;
        if (userInfo != null && userInfo.getLoggedInBranch() != null && userInfo.getLoggedInBranch().getId() != null) {
            //teamId = getLeastLoadedTeamIdOfThisBranch(teamService.getTeamIdsOfUserInLoggedInBranch(userInfo));
            Random rand = new Random();
            List<Long> teamIds =teamService.getTeamIdAssociatedToUserByUserId(userInfo.getId());
            teamId = CollectionUtils.isNotEmpty(teamIds)?teamIds.get(rand.nextInt(teamIds.size())):null;
        }
        if (teamId == null) {
            teamId = teamService.getAllTeamIds().iterator().next();
        }
        
        if(teamId != null){
        	team = entityDao.find(Team.class, teamId);
        }
        
        return team;
    }
    
    @Override
    public Long getAssignedTeamId() {
        Long teamId = null;
        UserInfo userInfo = getCurrentUser();
        // The user may not have a logged in branch (example in test execution) in which case we would fetch all teams and
        // assign one team randomly
        if (userInfo != null && userInfo.getLoggedInBranch() != null && userInfo.getLoggedInBranch().getId() != null) {
            Random rand = new Random();
            List<Long> teamIds = teamService.getTeamIdsOfUserInLoggedInBranch(userInfo);
            teamId =CollectionUtils.isNotEmpty(teamIds)? teamIds.get(rand.nextInt(teamIds.size())):null;
        }
        if (teamId == null) {
            teamId = teamService.getAllTeamIds().iterator().next();
        }
        return teamId;
    }


    @Override
    public Team getTeamToAssignForQuickLead(String productTypeShortName) {
        List<Team> identifiedTeams = new ArrayList<Team>();
        // we will fetch the team from an xml - at present just giving the first team on return

        List<SingleMappingOfProductTypeAndTeam> mappingsOfProductTypeAndTeam = getTeamProductTypeMapping("TeamAssignmentStratergyForQuickLead");

        for (SingleMappingOfProductTypeAndTeam prTypeAndTeam : mappingsOfProductTypeAndTeam) {
            if (prTypeAndTeam.getProductTypeShortName().equals(productTypeShortName)) {
                identifiedTeams.add(teamService.getTeamByTeamName(prTypeAndTeam.getTeamName()));
            }
        }

        /*  
         * currently we are not able to use below fxns. as resource loader comes out to be null
         * 
         
         List<SingleMappingOfProductTypeAndTeam> mappingOfProductTypeAndTeam = getTeamProductTypeMappingList("TeamAssignmentStratergyForQuickLead");
        */
        Team team = getLeastLoadedTeamOfThisBranch(identifiedTeams);
        if (team == null) {
            team = teamService.getAllTeams().get(0);
        }

        return team;
    }

    @Override
    public Team getTeamToAssignForIC(City city) {

        List<OrganizationBranch> organizations = new ArrayList<OrganizationBranch>();

        try {
            organizations = getOrganizationOfThisCity(city);

            OrganizationBranch organization = null;
            if (organizations.size() > 0) {
                // the strategy to select the best possible branch of the city is to be selected. Till now we just treat all
                // branches equal
                // and take all teams of all these branches and then choose the least loaded teams out of these teams
                organization = organizations.get(0);
            }

            if (organization == null || getTeamsOfTheseBranches(organizations) == null) {
                organizations = getOrganizationOfThisState(city.getState());

                organization = null;
                if (organizations.size() > 0) {
                    // the strategy to select the best possible branch of the state is to be selected
                    organization = organizations.get(0);
                }

                if (organization == null || getTeamsOfTheseBranches(organizations) == null) {
                    organizations = getOrganizationOfThisCountry(city.getCountry());

                }
            }
        } catch (NullPointerException ne) {
            BaseLoggers.exceptionLogger.error("NullPointorException . So , first Team from database will be returned",
                    ne.getMessage());
        }

        List<Team> teamsToChooseFrom = getTeamsOfTheseBranches(organizations);

        Team team = getLeastLoadedTeamOfThisBranch(teamsToChooseFrom);

        if (team == null) {
            team = teamService.getAllTeams().get(0);
        }

        return team;
    }

    @SuppressWarnings("unchecked")
	private Team getLeastLoadedTeamOfThisBranch(List<Team> teams) {
        List<String> teamUris = new ArrayList<String>();
        for (Team team : teams) {
            teamUris.add(team.getUri());
        }
        if (teamUris == null || (teamUris != null && teamUris.size() == 0)) {
            return null;
        }
        
        String nativeQueryString="SELECT I.GROUP_ID_ FROM ACT_RU_IDENTITYLINK I WHERE I.GROUP_ID_ IN :teamUris GROUP BY I.GROUP_ID_";
        Query nativeQuery = entityDao.getEntityManager().createNativeQuery(nativeQueryString).setParameter("teamUris", teamUris);
        nativeQuery.unwrap(org.hibernate.SQLQuery.class).addSynchronizedQuerySpace("");
        List<String> taskBasedTeamList = nativeQuery.getResultList();
        
        if(CollectionUtils.isNotEmpty(taskBasedTeamList)){
        	String teamUri = taskBasedTeamList.get(0);
        	return teamService.getTeamByTeamId(EntityId.fromUri(teamUri).getLocalId());
        }else{
        	return teamService.getTeamByTeamId(EntityId.fromUri(teamUris.get(0)).getLocalId());
        }
    }
    private Long getLeastLoadedTeamIdOfThisBranch(List<Long> teamIds) {
        List<String> teamUris = new ArrayList<String>();
        for (Long teamId : teamIds) {
            teamUris.add(teamService.getTeamUriForTeamId(teamId));
        }
        if (teamUris == null || (teamUris != null && teamUris.size() == 0)) {
            return null;
        }
        
        String nativeQueryString ="SELECT I.GROUP_ID_ FROM ACT_RU_IDENTITYLINK I WHERE I.GROUP_ID_ IN :teamUris GROUP BY I.GROUP_ID_";
//      List<String> taskBasedTeamList = entityDao.getEntityManager().createNativeQuery(nativeQueryString.toString()).getResultList();
        Query nativeQuery = entityDao.getEntityManager().createNativeQuery(nativeQueryString).setParameter("teamUris", teamUris);
        nativeQuery.unwrap(org.hibernate.SQLQuery.class).addSynchronizedQuerySpace("");
        List<String> taskBasedTeamList = nativeQuery.getResultList();
        		
        if(CollectionUtils.isNotEmpty(taskBasedTeamList)){
        	String teamUri = taskBasedTeamList.get(0);
        	return EntityId.fromUri(teamUri).getLocalId();
        }else{
        	return EntityId.fromUri(teamUris.get(0)).getLocalId();
        }
    }

    private List<OrganizationBranch> getOrganizationOfThisCity(City city) {
        List<OrganizationBranch> branches = null;

        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                "TeamAssignmentStratergy.getOrganizationOfThisCity").addParameter("cityId", city.getId());
        branches = entityDao.executeQuery(executor);

        return branches;
    }

    private List<OrganizationBranch> getOrganizationOfThisState(State state) {
        List<OrganizationBranch> branches = null;

        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                "TeamAssignmentStratergy.getOrganizationOfThisState").addParameter("stateId", state.getId());
        branches = entityDao.executeQuery(executor);

        return branches;
    }

    private List<OrganizationBranch> getOrganizationOfThisCountry(Country country) {
        List<OrganizationBranch> branches = null;

        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                "TeamAssignmentStratergy.getOrganizationOfThisCountry").addParameter("countryId", country.getId());
        branches = entityDao.executeQuery(executor);

        return branches;
    }

    /*start for mapping */

    private List<SingleMappingOfProductTypeAndTeam> getTeamProductTypeMapping(String entityName) {

        List<SingleMappingOfProductTypeAndTeam> leadProductTypeAndTeamMapping = getTeamProductTypeMappingLoader(entityName);
        if (leadProductTypeAndTeamMapping != null) {
            return leadProductTypeAndTeamMapping;
        }

        return null;
    }

    /*end for mapping*/

    /*start for TeamProductTypeMappingList*/
    public List<SingleMappingOfProductTypeAndTeam> getTeamProductTypeMappingLoader(String entityName) {

        /*if (!cacheMapForTeamProductTypeMappingList.containsKey(entityName)) {*/
        Resource resource = resourceLoader.getResource("team-assignment-config" + SystemUtils.FILE_SEPARATOR + entityName
                + ".xml");
        if (!resource.exists()) {
            cacheMapForTeamProductTypeMappingList.put(entityName, null);
        } else {
            try {
                List config = XmlUtils.readFromXml(IOUtils.toString(resource.getInputStream()), List.class);
                cacheMapForTeamProductTypeMappingList.put(entityName, config);
            } catch (Exception e) {
                throw new SystemException("Application is unable to read " + entityName + ".xml", e);
            }
        }
        /*}*/
        return cacheMapForTeamProductTypeMappingList.get(entityName);
    }

    /*end for TeamProductTypeMappingList*/

    /*private List<ColumnConfiguration> getColumnConfigurationList(String entityName) {
    	MasterConfigurationLoader masterConfigurationLoader = new MasterConfigurationLoader();
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getColumnConfigurationList();
        }
        return null;
    }*/

    /*private GridConfiguration getConfiguration(String entityName) {
               GridConfiguration gridConfiguration = getConfiguration2(entityName);
               if (gridConfiguration != null) {
                   return gridConfiguration;
           }
           return null;
       }*/

    /* public GridConfiguration getConfiguration2(String entityName) {
    	 
    	    //somehow we need to set the resourceLoader from cas-framework-context.xml
    	 
            if (!cacheMap.containsKey(entityName)) {
                Resource resource = resourceLoader.getResource("masters-config" + SystemUtils.FILE_SEPARATOR + entityName
                        + ".xml");
                if (!resource.exists()) {
                    cacheMap.put(entityName, null);
                } else {
                    try {
                        GridConfiguration config = XmlUtils.readFromXml(IOUtils.toString(resource.getInputStream()),
                                GridConfiguration.class);
                        cacheMap.put(entityName, config);
                    } catch (Exception e) {
                        throw new SystemException("Application is unable to read " + entityName + ".xml", e);
                    }
                }
            }
            return cacheMap.get(entityName);
        }*/

    private List<Team> getTeamsOfTheseBranches(List<OrganizationBranch> organizations) {
        List<Team> teamsToChooseFrom = new ArrayList<Team>();
        for (OrganizationBranch org : organizations) {
            List<Team> teamsOfThisBranch = teamService.getAllTeamsOfThisBranch(org);
            teamsToChooseFrom.addAll(teamsOfThisBranch);
        }

        return teamsToChooseFrom;
    }

    public Team getTeamForTask(String taskId) {
        String candidateGroup = null;
        try {
            candidateGroup = bpmnProcessService.getCandidateGroupForTask(taskId);
        } catch (NullPointerException ne) {
            BaseLoggers.exceptionLogger.error("NullPointerException while retrieving candidate group found for taskId:"
                    + taskId, ne.getMessage());
        }
        if (candidateGroup != null) {
            return teamService.getTeamByTeamId(EntityId.fromUri(candidateGroup).getLocalId());
        }
        return null;
    }

    @Override
    public Team getLeastLoadedTeam(List<Team> teamList) {
        return getLeastLoadedTeamOfThisBranch(teamList);
    }

}

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
