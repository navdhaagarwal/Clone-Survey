package com.nucleus.security.core.session;

import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

import com.nucleus.user.UserInfo;

public interface NeutrinoSessionRegistry extends SessionRegistry, ApplicationListener<SessionDestroyedEvent>  {

	
	Set<String> getAllSessionIds(Object principal);
	
	Set<String> getAllSessionIds(Object principal, boolean includeExpiredSessions);
	
	SessionInformation getSessionInformation(String sessionId);
	
	void expireSessionByUserId(Object targetPrincipal);
	
	void expireSessionBySessionId(String sessionId);
	
	void expireAllSession();

	String getModuleCode();
	
	NeutrinoSessionInformation getLeastRecentlyCreatedSessionInformationByUserId(Long userId);

	void updatRegisteredSession(NeutrinoSessionInformation neutrinoSessionInformation);

	void expireSsoTicketByServiceTicket(List<String> serviceTicketIdList);

	void expireSsoTicketByServiceTicket(String serviceTicketId);
	
	NeutrinoSessionInformation getSessionByLogginIP(String loginIP);

	List<UserInfo> getAllLoggedInUsers();
	
	Set<NeutrinoSessionInformation> getAllSessionsAcrossModule(Object principal, boolean includeExpiredSessions);

	List<UserInfo> getAllLoggedInUsersAcrossModule();

	Set<NeutrinoSessionInformation> getAllValidSessionsForUser(Long userid);

}
