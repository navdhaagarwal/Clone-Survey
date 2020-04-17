/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */

package com.nucleus.web.useradministration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;
import com.nucleus.security.oauth.service.RESTfulAuthenticationService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;
import com.nucleus.web.common.controller.BaseController;

@Transactional
@Controller
@RequestMapping(value = "/Admin")
public class UserSessionManagerController extends BaseController {

    @Inject
    @Named("coreUtility")
    private CoreUtility coreUtility;
    
	@Value("${soap.service.trusted.client.id}")
	private String clientID;
	
	@Value("${INTG_BASE_URL}/app/restservice/revokeTokenByUsers")
	private String revokeUserTokenUrl;
    
    @Inject
    @Named("oauthauthenticationService")
    private RESTfulAuthenticationService oauthauthenticationService;
    
    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService userSessionManagerService;

    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore userManagementService;
    
    @Inject
    @Named("userService")
    private UserService userService;
    
    @Inject
    @Named("sessionRegistry")
    private NeutrinoSessionRegistry sessionRegistry;
    
    @PreAuthorize("hasAuthority('ADMIN_AUTHORITY')") 
    @RequestMapping(method = { RequestMethod.POST }, value = "/removeUser/{userId}")
    public @ResponseBody
    String closeUserSession(HttpServletRequest request, @PathVariable("userId") String[] userIds) {

    	List<String> usersnames = new ArrayList<String>();
    	
        for (String userId : userIds) {
        	
            UserInfo userInfo = userService.getUserById(Long.parseLong(userId));
            
            usersnames.add(userInfo.getUsername());
            
			// get all non-expired session Ids
			Set<NeutrinoSessionInformation> nonExpiredSessions = sessionRegistry.getAllSessionsAcrossModule(userInfo,false);
            
            //Collect all service ticket
            List<String> serviceTkts = nonExpiredSessions.stream().map(s -> s.getServiceTicketId()).collect(Collectors.toList()); 
            
            //invalidate all the users session across module.
			userSessionManagerService.invalidateUserSessionAcrossModulesAndUpdateRegistry(userInfo.getId(),
					getUserDetails().getId(), request.getRemoteAddr(), NeutrinoSessionInformation.LOGOUT_TYPE_BY_ADMIN);
			
			//expiring SsoTickets
			sessionRegistry.expireSsoTicketByServiceTicket(serviceTkts);
        }
        
		/**
         *  rest call in API Manager to invalidate the tokens based on the usernames
         */
        if(coreUtility.isApiManagerEnabled() && usersnames.size() > 0) {
        	oauthauthenticationService.revokeTokenByUsers(revokeUserTokenUrl, clientID, usersnames);
        }
        return "";
    }
    
    @PreAuthorize("hasAuthority('ADMIN_AUTHORITY')") 
    @RequestMapping(method = { RequestMethod.GET }, value = "/refresh")
    public String getAllUsers(HttpServletRequest request, ModelMap uiModel) {

        List<UserInfo> userInfoList = userSessionManagerService.getAllLoggedInUsers();

        uiModel.addAttribute("userInfoList", userInfoList);
        userInfoList = null;

        return "allUsersInner";

    }

}
