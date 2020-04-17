/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.user.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.role.service.RoleService;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.web.common.controller.BaseController;

/**
 * @author Nucleus Software Exports Limited
 * A controller to handle request from userSelect tag.
 */
@Controller
@RequestMapping(value = "/userSelect")
public class UserSelectTagController extends BaseController {

    @Inject
    @Named("teamService")
    private TeamService                 teamService;
    
    @Inject
    @Named("roleService")
    private RoleService                 roleService;
    
    @Inject
    @Named("organizationService")
    private OrganizationService         orgService;

    @RequestMapping(value = "/getBranches")
    public @ResponseBody
    Map<Long, String> getBranchesByQueryTerm(@RequestParam("queryTerm") String queryTerm) {

        List<Object[]> idNames = orgService.getApprovedAndActiveOrgBranches(queryTerm);
        return toIdNameMap(idNames);
    }
    
    @RequestMapping(value = "/getRoles")
    public @ResponseBody
    Map<Long, String> getRoles() {
    	 
    	List<Object[]> idNames = roleService.getAllApprovedAndActiveRoles();
        return toIdNameMap(idNames);
    }
    
    @RequestMapping(value = "/getTeams")
    public @ResponseBody
    Map<Long, String> getTeams() {
    	List<Object[]> idNames = teamService.getAllTeamIdsAndNames();
        return toIdNameMap(idNames);
    }

    private Map<Long, String> toIdNameMap(List<Object[]> idNames) {
        Map<Long, String> map = new HashMap<Long, String>();

        for (Object[] objects : idNames) {
            if (objects != null && objects.length > 1 && objects[0] != null && objects[1] != null) {
                map.put((Long) objects[0], (String) objects[1]);
            }
        }

        return map;
    }

}
