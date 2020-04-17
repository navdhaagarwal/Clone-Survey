package com.nucleus.security.core.session;

import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.SESSION_REGISTRY_PRINCIPALS_CACHE;
import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.SESSION_REGISTRY_SESSION_IDS_CACHE;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.base.utility.CoreUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.common.NeutrinoRestTemplateFactory;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

/**
 * Custom implementation of Session registry as we need sessions on 
 * clusters. This impementation uses distributed cache that can be used 
 * on multiple nodes instead of <code>ConcurrentHashMap</code>.
 * 
 * 
 * Also it customizes different logins for SSO.
 * 
 * @author debashish.bharali
 * @author syambrij.maurya
 *
 */

public class NeutrinoSessionRegistryImpl implements NeutrinoSessionRegistry {

	private static final String ERROR_MESSAGE = "SessionId required as per interface contract.";
	
	/************************************Instance variables***************************/
	
	private String moduleCode;
	
	private String keyPattern;
	
	private boolean isSsoActive;

	private RestTemplate restTemplate;
	
	private String userUrikeyPattern;

	@Inject
	@Named("fwCacheHelper")
	private FWCacheHelper fwCacheHelper;

	@Inject
	@Named(value = "sessionModuleService")
	private SessionModuleService sessionModuleService;

    @Autowired
    private NeutrinoRestTemplateFactory neutrinoRestTemplateFactory;
    
    @Value(value = "#{'${core.web.config.SSO.ticketvalidator.url.value}'}")
	private String ssoUrl;
    
    private String expireTGTURL="/expireTicketGrantingTicketsForModule";
    
    @Value(value = "#{'${core.web.config.SSO.request.encryption.key}'}")
	private String ssoEncryptionKey;
    
    private SessionRegistrySessionIdsCachePopulator sessionIdsCachePopulator;
    
    //This would not be actual principals cache.
    //We are creating cache based on "loggedInModuleCode + '-' + entityURI".
    private SessionRegistryPrincipalsCachePopulator sessionPrincipalsCachePopulator;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**************************************** Methods ********************************/

	public boolean isSsoActive() {
		return isSsoActive;
	}

	public void setSsoActive(boolean isSsoActive) {
		this.isSsoActive = isSsoActive;
	}
	
    public String getKeyPattern() {
		return keyPattern;
	}

	public void setKeyPattern(String keyPattern) {
		this.keyPattern = keyPattern;
	}

	@Override
    public String getModuleCode() {
		return moduleCode;
	}
	
	public String getUserUrikeyPattern() {
        return userUrikeyPattern;
	}


	public void setUserUrikeyPattern(String userUrikeyPattern) {
	        this.userUrikeyPattern = userUrikeyPattern;
	}


	private String getCrossModuleKeyCodeFromPrincipal(UserInfo principal) {
		return this.userUrikeyPattern + principal.getUserEntityId().getUri();
	}
	    
	private SessionRegistrySessionIdsCachePopulator getSessionIdsCachePopulator() {
		if (sessionIdsCachePopulator == null) {
			return (sessionIdsCachePopulator = (SessionRegistrySessionIdsCachePopulator)
					this.fwCacheHelper.getFWCachePopulator(SESSION_REGISTRY_SESSION_IDS_CACHE));
		}
		return sessionIdsCachePopulator;
	}
	
	private SessionRegistryPrincipalsCachePopulator getSessionPrincipalsCachePopulator() {
		if (sessionPrincipalsCachePopulator == null) {
			return (sessionPrincipalsCachePopulator = (SessionRegistryPrincipalsCachePopulator)
					this.fwCacheHelper.getFWCachePopulator(SESSION_REGISTRY_PRINCIPALS_CACHE));
		}
		return sessionPrincipalsCachePopulator;
	}
	
	private String getKeyCodeFromPrincipal(UserInfo principal) {
		return principal.getLoggedInModule() + '-' + principal.getUserEntityId().getUri();
	}
	
	private Collection<Object> getAllPrincipals(Collection<Object> principalsCollection) {
		Set<Map.Entry<Object, Object>> entrySet = null;
		
		entrySet = getSessionPrincipalsCachePopulator().entrySet(keyPattern);
		
		for (Entry<Object, Object> principalsCacheEntry : entrySet) {
			String sessionId = (String) principalsCacheEntry.getValue();
			//Iterating on keyset and calling getAll(keySet) with keySet is an alternate option.
			//But We do not have this implementation in infinispan so we did not considered that option.
   			SessionInformation si = getSessionInformation(sessionId);
   			if (si != null) {
   				UserInfo userInfo = (UserInfo) si.getPrincipal();
   	   			principalsCollection.add(userInfo);
   			}
   		}
   		return principalsCollection;
	}
	
	@PostConstruct
    public void init() {
        if (ProductInformationLoader.productInfoExists()) {
            this.moduleCode = ProductInformationLoader.getProductCode();
            // * is appended as prefix because on redis key is appended after some prefix in bytes. 
            // So we are adding prefix of * into pattern.
            this.keyPattern = "*" + moduleCode + "-*";
            this.userUrikeyPattern = "*-";
        }
        this.restTemplate = neutrinoRestTemplateFactory.createRestTemplate(null, null, null, null, null);
    }
	
    @Override
	public List<Object> getAllPrincipals() {
    	return (List<Object>) getAllPrincipals(new ArrayList<>());
	}
    
	@Override
	public List<SessionInformation> getAllSessions(Object principal, boolean includeExpiredSessions) {
		String keyCode = getKeyCodeFromPrincipal((UserInfo) principal);
		String sessionId = (String) getSessionPrincipalsCachePopulator().get(keyCode);
		Object castTempObj = null;
		if (sessionId != null && (castTempObj = getSessionIdsCachePopulator().get(sessionId)) != null) {
			SessionInformation nsi = (SessionInformation) castTempObj;
			if ((includeExpiredSessions || !nsi.isExpired())
					&& nsi.getPrincipal().equals(principal)) {
				List<SessionInformation> sessionInformation = new ArrayList<>();
				sessionInformation.add(nsi);
				return sessionInformation;
			}
		}
		return Collections.emptyList();
	}

	@Override
	public Set<String> getAllSessionIds(Object principal, boolean includeExpiredSessions) {
    	List<SessionInformation> allSessions = getAllSessions(principal, includeExpiredSessions);
    	if (allSessions.isEmpty()) {
    		return Collections.emptySet();
    	}
    	Set<String> setOfStrings = new HashSet<>(1);
    	setOfStrings.add(allSessions.get(0).getSessionId());
    	return setOfStrings;
    }

    @Override
    public SessionInformation getSessionInformation(String sessionId) {
    	Assert.hasText(sessionId, ERROR_MESSAGE);
        return (SessionInformation) getSessionIdsCachePopulator().get(sessionId);
    }

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
    	//removed for PDDEV-21826 since session information is removed by UserSecurityTrailSessionDestroyedEventListener class
    }

    @Override
    public void refreshLastRequest(String sessionId) {
    	Assert.hasText(sessionId, ERROR_MESSAGE);
        SessionInformation info = getSessionInformation(sessionId);
        if (info != null) {
            info.refreshLastRequest();
        }
    }
   
    @Override
    public void registerNewSession(String sessionId, Object principal) {
    	Assert.hasText(sessionId, ERROR_MESSAGE);
        Assert.notNull(principal, "Principal required as per interface contract.");
        UserInfo prin = (UserInfo)principal;
        if (StringUtils.isEmpty((prin).getLoggedInModule()) && ValidatorUtils.notNull(moduleCode)){
			prin.setLoggedInModule(moduleCode);
			CoreUtility.syncSecurityContextHolderInSession(prin.getMappedSessionId());
        }
        updateSessionInformation(sessionId, new NeutrinoSessionInformation(new UserInfo(prin), sessionId, new Date()));      
		sessionModuleService.createSessionModuleMapping(sessionId);
    }
    
    protected void updateSessionInformation(String sessionId, NeutrinoSessionInformation neutrinoSessionInformation) {
        Map<String, NeutrinoSessionInformation> sessionMap = new HashMap<>();
        sessionMap.put(sessionId, neutrinoSessionInformation);
        getSessionIdsCachePopulator().update(Action.UPDATE, sessionMap);
        //Update principals cache.
        Map<String, String> principalsMap = new HashMap<>();
        UserInfo principal = (UserInfo) neutrinoSessionInformation.getPrincipal();
        principalsMap.put(getKeyCodeFromPrincipal(principal), sessionId);
        getSessionPrincipalsCachePopulator().update(Action.UPDATE, principalsMap);
    }
    
    @Override
    public void updatRegisteredSession(NeutrinoSessionInformation neutrinoSessionInformation) {
    	updateSessionInformation(neutrinoSessionInformation.getSessionId(), neutrinoSessionInformation);
    }
    
	@Override
    public void removeSessionInformation(String sessionId) {
        Assert.hasText(sessionId, ERROR_MESSAGE);
        SessionInformation sessionInfo = getSessionInformation(sessionId);
        if (sessionInfo == null) {
            return ;
        }
        getSessionIdsCachePopulator().update(Action.DELETE, sessionId);
        UserInfo principal = (UserInfo) sessionInfo.getPrincipal();
        String keyCode = getKeyCodeFromPrincipal(principal);
        getSessionPrincipalsCachePopulator().update(Action.DELETE, keyCode);
    }

    @Override
    public void expireSessionBySessionId(String sessionId) {
    	if (notNull(sessionId)) {
            SessionInformation sessionInformation = getSessionInformation(sessionId);
            if (sessionInformation == null) {
                return;
            }
            sessionInformation.expireNow();
			Map<String, SessionInformation> sessionMap = new HashMap<>();
			sessionMap.put(sessionId, sessionInformation);
			getSessionIdsCachePopulator().update(Action.UPDATE, sessionMap);
			String keyCode = getKeyCodeFromPrincipal((UserInfo) sessionInformation.getPrincipal());
			getSessionPrincipalsCachePopulator().update(Action.DELETE, keyCode);
        }
    }

    @Override
    public void expireSessionByUserId(Object targetPrincipal) {
    	if (notNull(targetPrincipal)) {
            Set<String> userSessionIds = getAllSessionIds(targetPrincipal, false);
            Map<String,SessionInformation> sessionMap = new HashMap<>(); //Although only one session id is possible for each user.
            for (String sessionId : userSessionIds) {
                SessionInformation sessionInformation = getSessionInformation(sessionId);
                if (sessionInformation != null) {
                	sessionInformation.expireNow();
                    sessionMap.put(sessionId, sessionInformation);	
                }
            }
            getSessionIdsCachePopulator().update(Action.UPDATE, sessionMap);
            String keyCode = getKeyCodeFromPrincipal((UserInfo) targetPrincipal);
            getSessionIdsCachePopulator().update(Action.DELETE, keyCode);
        }
    }
    
    @Override
	public NeutrinoSessionInformation getLeastRecentlyCreatedSessionInformationByUserId(Long userId) {
		if (notNull(userId)) {
			EntityId entityId = new EntityId(User.class, userId);
			String sessionId = (String) getSessionPrincipalsCachePopulator().get(moduleCode + "-" + entityId.toUri());
   			if (sessionId != null) {
   				return (NeutrinoSessionInformation) getSessionInformation(sessionId);
   			}
		}
		return null;
	}

	@Override
	public void expireAllSession() {
		if (isSsoActive()) {
			expireAllTicketGeneratingTickets();
		} 
		expireAllSessions();
	}

	private void expireAllTicketGeneratingTickets() {
		// get the list of service ticket id's for which TGT needs to be expired
		List<String> serviceTicketIdList = getServiceTicketIdList();
		expireSsoTicketByServiceTicket(serviceTicketIdList);
	}
	
	@Override
	public void expireSsoTicketByServiceTicket(String serviceTicketId){
		List<String> serviceTicketIdList=new ArrayList<>();
		serviceTicketIdList.add(serviceTicketId);
		expireSsoTicketByServiceTicket(serviceTicketIdList);	
	}
	
	public void expireSsoTicketByServiceTicket(List<String> serviceTicketIdList) {
		if (!isSsoActive()) {
			return;
		}
		String response = restTemplate.postForObject(ssoUrl+expireTGTURL, serviceTicketIdList, String.class);
		if ("success".equals(response)) {
			logger.info("sessions marked expired for users of module {}", moduleCode);
		} else {
			logger.error("Unable to expire session for users of module {}", moduleCode);
		}
	}
	
	private List<String> getServiceTicketIdList(){
		List<String> serviceTicketIdList = new ArrayList<>();
		for (Map.Entry<Object, Object> principalCacheEntry : getSessionPrincipalsCachePopulator()
				.entrySet(keyPattern)) {
			SessionInformation sessionInformation = getSessionInformation((String) principalCacheEntry.getValue());
			if (sessionInformation != null && !sessionInformation.isExpired()) {
				NeutrinoSessionInformation neutrinoSessionInformation = (NeutrinoSessionInformation) sessionInformation;
				serviceTicketIdList.add(neutrinoSessionInformation.getServiceTicketId());
			}
		}
		return serviceTicketIdList;
	}
	
	private void expireAllSessions() {
		Map<String, SessionInformation> sessionMap = new HashMap<>();
		for (Map.Entry<Object, Object> principalCacheEntry : getSessionPrincipalsCachePopulator().entrySet(keyPattern)) {
			SessionInformation sessionInformation = getSessionInformation((String) principalCacheEntry.getValue());
			if (sessionInformation != null && !sessionInformation.isExpired()) {
				sessionInformation.expireNow();
		        sessionMap.put((String) sessionInformation.getSessionId(), sessionInformation);
			}
		}
		getSessionIdsCachePopulator().update(Action.UPDATE, sessionMap);
	}
    
	@Override
	public Set<String> getAllSessionIds(Object principal) {
    	return getAllSessionIds(principal, true);
	}
    
	public NeutrinoSessionInformation getSessionByLogginIP(String loginIP) {
		if (StringUtils.isEmpty(loginIP)) {
			return null;
		}
		Set<Map.Entry<Object, Object>> entrySet = null;

		entrySet = getSessionPrincipalsCachePopulator().entrySet(keyPattern);

		for (Entry<Object, Object> principalsCacheEntry : entrySet) {
			SessionInformation sessionInformation = getSessionInformation((String) principalsCacheEntry.getValue());
			if (sessionInformation != null && !sessionInformation.isExpired()) {
				String loggedinIP = ((NeutrinoSessionInformation) sessionInformation).getLoginIp();
				if (!StringUtils.isEmpty(loggedinIP) && loggedinIP.equalsIgnoreCase(loginIP)) {
					return (NeutrinoSessionInformation) sessionInformation;
				}

			}
		}
		return null;
	}
	
	/**
     * Only gives all logged in users for particular module even in case of SSO.
     * 
     * @return
     */
	@Override
    public List<UserInfo> getAllLoggedInUsers() {
    	List<UserInfo> loggerInUsers = new ArrayList<>();
    	for (Entry<Object, Object> principalsCacheEntry : getSessionPrincipalsCachePopulator().entrySet(keyPattern)) {
			String sessionId = (String) principalsCacheEntry.getValue();
   			SessionInformation si = getSessionInformation(sessionId);
   			if (si != null && !si.isExpired()) {
   				loggerInUsers.add((UserInfo) si.getPrincipal());
			}
   		}
		return loggerInUsers;
	}

	/**
     * Only gives all logged in users for particular module even in case of SSO.
     * 
     * @return
     */
	@Override
    public List<UserInfo> getAllLoggedInUsersAcrossModule() {
    	List<UserInfo> loggerInUsers = new ArrayList<>();
    	for (Entry<Object, Object> principalsCacheEntry : getSessionPrincipalsCachePopulator().entrySet(userUrikeyPattern + "*")) {
			String sessionId = (String) principalsCacheEntry.getValue();
   			SessionInformation si = getSessionInformation(sessionId);
   			if (si != null && !si.isExpired()) {
   				loggerInUsers.add((UserInfo) si.getPrincipal());
			}
   		}
		return loggerInUsers;
	}
	
	@Override
	public Set<NeutrinoSessionInformation> getAllSessionsAcrossModule(Object principal,
			boolean includeExpiredSessions) {
		String keyCode = getCrossModuleKeyCodeFromPrincipal((UserInfo) principal);

		Set<NeutrinoSessionInformation> sessionInformationList = new HashSet<>();
		NeutrinoSessionInformation nsi = null;

		for (Map.Entry<Object, Object> principalCacheEntry : getSessionPrincipalsCachePopulator()
				.entrySet(keyCode)) {
			nsi = (NeutrinoSessionInformation) getSessionInformation((String) principalCacheEntry.getValue());
			if (nsi != null && (!nsi.isExpired() || includeExpiredSessions)) {
				sessionInformationList.add(nsi);
			}
		}

		return sessionInformationList;
	}

	@Override
	public Set<NeutrinoSessionInformation> getAllValidSessionsForUser(Long userid){
		Set<NeutrinoSessionInformation> sessionInformationList = new HashSet<>();
		Set<Map.Entry<Object, Object>> entrySet = null;
		EntityId entityId = new EntityId(User.class, userid);
		entrySet = getSessionPrincipalsCachePopulator().entrySet("*-"+entityId.toUri());

		for (Entry<Object, Object> principalsCacheEntry : entrySet) {
			String sessionId = (String) principalsCacheEntry.getValue();
			SessionInformation sessionInformation = getSessionInformation(sessionId);
			if (sessionInformation != null && !sessionInformation.isExpired())  {
				sessionInformationList.add((NeutrinoSessionInformation) sessionInformation);
			}
		}

		return sessionInformationList;
	}
}
