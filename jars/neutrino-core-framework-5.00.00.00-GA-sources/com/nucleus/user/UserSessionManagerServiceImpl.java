package com.nucleus.user;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;

import net.bull.javamelody.MonitoredWithSpring;

@Named("userSessionManagerService")
public class UserSessionManagerServiceImpl implements UserSessionManagerService {

    @Inject
    @Named("sessionRegistry")
    private NeutrinoSessionRegistry sessionRegistry;
    
    @Inject
    @Named("messageSource")
    protected MessageSource messageSource;
    
    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    @Override
    public boolean invalidateUserSessionAndUpdateRegistry(Long userId, NeutrinoSessionInformation neutrinoSessionInformation) {
    	if(notNull(userId)){
    		List<Object> principalsList = sessionRegistry.getAllPrincipals();
            Object targetPrincipal = null;
            //get the first and break
            for (Object principal : principalsList) {
                if (principal instanceof UserInfo && ((UserInfo) principal).getId().equals(userId)) {
                    	targetPrincipal = principal;
                        break;
                   }
            }
            if (targetPrincipal == null) {
                return false;
            }
            //get all non-expired session Ids
            Set<String>  sessionIds = sessionRegistry.getAllSessionIds(targetPrincipal, false);
            for(String sessionId:sessionIds){
            	NeutrinoSessionInformation sessionInformation=(NeutrinoSessionInformation)sessionRegistry.getSessionInformation(sessionId);
            	if(null!=neutrinoSessionInformation){
            	sessionInformation.setLogOutBy(neutrinoSessionInformation.getLogOutBy());
            	sessionInformation.setForceLogOutIP(neutrinoSessionInformation.getForceLogOutIP());
            	sessionInformation.setLogOutType(neutrinoSessionInformation.getLogOutType());
            	}
            	sessionInformation.expireNow();
            	sessionRegistry.updatRegisteredSession(sessionInformation);
            }
            return true;
    	}
        return false;
    }
    
    
    @Override
    public boolean invalidateUserSessionAcrossModulesAndUpdateRegistry(Long userId, Long logOutBy, String forceLogOutIP, String logOutType) {
    	if(notNull(userId)){
    		List<Object> principalsList = sessionRegistry.getAllPrincipals();
            Object targetPrincipal = null;
            //get the first and break
            for (Object principal : principalsList) {
                if (principal instanceof UserInfo && ((UserInfo) principal).getId().equals(userId)) {
                    	targetPrincipal = principal;
                        break;
                   }
            }
            if (targetPrincipal == null) {
                return false;
            }
            //get all non-expired session Ids
            Set<NeutrinoSessionInformation> nonExpiredSessions = sessionRegistry.getAllSessionsAcrossModule(targetPrincipal, false);
            
            for(NeutrinoSessionInformation sessionInformation: nonExpiredSessions){
            	if(null!=sessionInformation){
            		sessionInformation.setLogOutBy(logOutBy);
            		sessionInformation.setForceLogOutIP(forceLogOutIP);
            		sessionInformation.setLogOutType(logOutType);
            		sessionInformation.expireNow();
            	}
            	sessionRegistry.updatRegisteredSession(sessionInformation);
            }
            return true;
    	}
        return false;
    }

    /*
     * This method is used to first expire the session information of the user from the current Module
     * with the given user Id. Then the function sends a rest call to SSO Server with the service ticket 
     * to expire all other sessions of user in other modules.
     * 
     * @param userId User Id of the user whose session is to be expired
     * 
     */
    @Override
    public boolean invalidateUserSession(Long userId) {
    	boolean result =  invalidateUserSessionAndUpdateRegistry(userId, null);
    	expireSSOTicketByServiceTicket(userId);
		return result;
    }

    @SuppressWarnings("unchecked")
	@Override
    @MonitoredWithSpring(name = "USMSI_FETCH_ALL_LOGGED_IN_USR")
    public List<UserInfo> getAllLoggedInUsers() {
        return sessionRegistry.getAllLoggedInUsers();
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public List<UserInfo> getAllLoggedInUsersAcrossModule() {
        return sessionRegistry.getAllLoggedInUsersAcrossModule();
    }
    
    @Override
    public int getCurrentWebUserSessionCount(String username) {
    	int activeSessionCount = 0;
    	for (Object principal : sessionRegistry.getAllPrincipals()) {
    		if (((UserInfo) principal).getUsername().equals(username)) {
    			activeSessionCount++;
    			break;
    		}
    	}		
    	return activeSessionCount;
    }
    
    @Override
    public UserInfo getLoggedinUserInfo() {
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal != null && !(principal.equals("anonymousUser")) && (principal instanceof UserInfo) ) {
                return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            }
        }
        return null;
    }

    @Override
    public void invalidateCurrentLoggedinUserSession() {
        UserInfo user = getLoggedinUserInfo();
        if (user != null) {
            invalidateUserSession(user.getId());
        }

    }
    
    @Override
	public void invalidateCurrentUserSession(String sessionId) {
    	if(notNull(sessionId)){
    		sessionRegistry.expireSessionBySessionId(sessionId);
    	}
	}

	    
   	@Override
	public void logOutAllUsers() {
   		logOutAllUsers(null, null, null, null);
	}
   	
    @Override
	public void logOutAllUsers(String logoutEvent, Message logoutMessage) {
    	logOutAllUsers(logoutEvent, logoutMessage, null, null);
	}

	@Override
	public void logOutAllUsers(String logoutEvent, Message message, Long logoutBy) {
		logOutAllUsers(logoutEvent, message, logoutBy, null);
	}
    

    @Override
    public void logOutAllUsers(String logoutEvent,Message message, Long logoutBy, String forceLogoutIP){
    	updateSessionInformation(logoutEvent, message, logoutBy, forceLogoutIP);
    	sessionRegistry.expireAllSession();
    }

    private void expireSSOTicketByServiceTicket(Long userId){
   		NeutrinoSessionInformation neutrinoSessionInformation = sessionRegistry.getLeastRecentlyCreatedSessionInformationByUserId(userId);
		if(neutrinoSessionInformation != null){
			sessionRegistry.expireSsoTicketByServiceTicket(neutrinoSessionInformation.getServiceTicketId());
		}
   	}
    
	private void updateSessionInformation(String logoutEvent,Message message, Long logoutBy, String forceLogoutIP) {
		String logoutMessage = null;
		if(message!=null) {
			logoutMessage = messageSource.getMessage(message.getI18nCode(),message.getMessageArguments(),configurationService.getSystemLocale());
		}
		List<Object> principalList = sessionRegistry.getAllPrincipals();
    	for(Object principal:principalList) {
    		Set<NeutrinoSessionInformation> sessionInformationList = sessionRegistry.getAllSessionsAcrossModule(principal,false);
    		for(NeutrinoSessionInformation sessionInfo:sessionInformationList) {
    			sessionInfo.updateLogoutInfo(logoutEvent, logoutMessage, logoutBy, forceLogoutIP);
    			sessionRegistry.updatRegisteredSession(sessionInfo);
    		}
    	}
	}
	
	public List<String> getLoggedInModules(UserInfo principal) {
		List<String> moduleList = new ArrayList<String>();
		Set<NeutrinoSessionInformation> sessionInformationList = sessionRegistry.getAllSessionsAcrossModule(principal,false);
		for(NeutrinoSessionInformation sessionInfo:sessionInformationList) {
			UserInfo sessionPrincipal = (UserInfo)sessionInfo.getPrincipal();
			if(sessionPrincipal != null) {
				moduleList.add(sessionPrincipal.getLoggedInModule());
			}
		}
		return moduleList;
	}

	public boolean invalidateUserSessionInAllModules(Long userId, Long logOutBy, String forceLogOutIP, String logOutType){
    	if(notNull(userId)) {
			Set<NeutrinoSessionInformation> sessionInformationSet = sessionRegistry.getAllValidSessionsForUser(userId);

			if (sessionInformationSet.isEmpty()) {
				return false;
			}

			for (NeutrinoSessionInformation sessionInformation : sessionInformationSet) {
				if (null != sessionInformation) {
					sessionInformation.setLogOutBy(logOutBy);
					sessionInformation.setForceLogOutIP(forceLogOutIP);
					sessionInformation.setLogOutType(logOutType);
					sessionInformation.expireNow();
				}
				sessionRegistry.updatRegisteredSession(sessionInformation);
			}

			List<String> serviceTkts = sessionInformationSet.stream().map(s -> s.getServiceTicketId()).collect(Collectors.toList());

			sessionRegistry.expireSsoTicketByServiceTicket(serviceTkts);

			return true;
		}
    	return false;
	}

}
