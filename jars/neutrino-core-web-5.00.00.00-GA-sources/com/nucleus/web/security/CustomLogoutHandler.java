package com.nucleus.web.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;
import com.nucleus.user.UserSessionManagerService;

public class CustomLogoutHandler implements LogoutHandler {
  @Inject
  @Named("userSessionManagerService")
  private UserSessionManagerService userSessionManagerService;
  @Inject
  @Named("sessionRegistry")
  private NeutrinoSessionRegistry sessionRegistry;
  
  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
	{	
		if (request.getSession(false) != null) {
			NeutrinoSessionInformation neutrinoSessionInformation = (NeutrinoSessionInformation) sessionRegistry
					.getSessionInformation(request.getSession(false).getId());
		if (null != neutrinoSessionInformation) {	
			if (null != request.getParameter(NeutrinoSessionInformation.LOGOUT_TYPE_BY_INACTIVITY) && NeutrinoSessionInformation.LOGOUT_TYPE_BY_INACTIVITY
					.equalsIgnoreCase(request.getParameter(NeutrinoSessionInformation.LOGOUT_TYPE_BY_INACTIVITY))) {
				neutrinoSessionInformation.setLogOutType(NeutrinoSessionInformation.LOGOUT_TYPE_BY_INACTIVITY);
			}else if(neutrinoSessionInformation.getLogOutType()!=null){
				request.setAttribute(neutrinoSessionInformation.getLogOutType(), neutrinoSessionInformation.getLogOutType());
			} else{
				neutrinoSessionInformation.setLogOutType(NeutrinoSessionInformation.LOGOUT_TYPE_BY_USER);
			}
			sessionRegistry.updatRegisteredSession(neutrinoSessionInformation);
		}
			userSessionManagerService.invalidateCurrentUserSession(request.getSession(false).getId());
		
		}
	}
}
