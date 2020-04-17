package com.nucleus.web.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.security.core.session.NeutrinoSessionRegistryImpl;
import com.nucleus.user.UserSessionManagerService;

/**
 * <p>Due to a bug in Spring Security, SSO launches the 
 * {@link HttpSessionDestroyedEvent} in a different context
 * than usual.</p>
 * 
 * 
 * <p>To bypass that issue so Concurrency works normally, this is
 * a custom logout filter to clear the sessions.</p>
 * 
 * 
 * 
 * @author prateek.chachra
 *
 */
public class CustomSSOLogoutFilter extends LogoutFilter {

	  @Inject
	  @Named("userSessionManagerService")
	  private UserSessionManagerService userSessionManagerService;
	
	private SessionRegistry sessionRegistry;

	
	public CustomSSOLogoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler[] handlers) {
		super(logoutSuccessHandler, handlers);

	}

	public CustomSSOLogoutFilter(String logoutSuccessUrl, LogoutHandler... handlers) {
		super(logoutSuccessUrl, handlers);
	}
	
	
	public SessionRegistry getSessionRegistry() {
		return sessionRegistry;
	}

	public void setSessionRegistry(SessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}


	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		if (requiresLogout(request, response)) {
			BaseLoggers.flowLogger.info("Logout request obtained for SSO.\nType of request : ");
			if(request.getRequestURI().contains("perform_sso_logout")){
				BaseLoggers.flowLogger.info("Single Sign Out.");
			}
			if(request.getRequestURI().contains("perform_sso_nonslo_logout")){
				BaseLoggers.flowLogger.info("Service Specific Sign Out. Currently user only signed out from "
			+ ProductInformationLoader.getProductName());
			}

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
			((NeutrinoSessionRegistryImpl) sessionRegistry).updatRegisteredSession(neutrinoSessionInformation);
		}
			userSessionManagerService.invalidateCurrentUserSession(request.getSession(false).getId());
		
		

		}

		super.doFilter(request, response, chain);
	}
}
