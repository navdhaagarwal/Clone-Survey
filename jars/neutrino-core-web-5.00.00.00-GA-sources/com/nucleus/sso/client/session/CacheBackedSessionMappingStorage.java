package com.nucleus.sso.client.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSessionManagerService;

public class CacheBackedSessionMappingStorage implements NeutrinoSessionMappingStorage{
	
	private FWCacheHelper fwCacheHelper;
    
    @Override
    public void addSessionById(String mappingId, HttpSession session) {
    	NeutrinoCachePopulator sessionIdToServiceTicketCache = getCachePopulatorInstance(FWCacheConstants.SESSION_ID_TO_SERVICE_TICKET_CACHE);
    	Map<String,String> sessionIdToServiceTicketMap = new HashMap<>();
    	sessionIdToServiceTicketMap.put(session.getId(), mappingId);
    	sessionIdToServiceTicketCache.update(Action.INSERT, sessionIdToServiceTicketMap);
    	
    	NeutrinoCachePopulator serviceTicketToSessionCache = getCachePopulatorInstance(FWCacheConstants.SERVICE_TICKET_TO_SESSION_CACHE);
    	Map<String,String> serviceTicketToSessionIdMap = new HashMap<>();
    	serviceTicketToSessionIdMap.put(mappingId, session.getId());
    	serviceTicketToSessionCache.update(Action.INSERT, serviceTicketToSessionIdMap);
    }

    @Override
	public void removeBySessionById(final String sessionId) {
		BaseLoggers.flowLogger.debug("Attempting to remove Session=[{}]", sessionId);

		NeutrinoCachePopulator sessionIdToServiceTicketCache = getCachePopulatorInstance(
				FWCacheConstants.SESSION_ID_TO_SERVICE_TICKET_CACHE);
		String mappingId = (String) sessionIdToServiceTicketCache.get(sessionId);
		sessionIdToServiceTicketCache.update(Action.DELETE, sessionId);

		if (mappingId != null) {
			NeutrinoCachePopulator serviceTicketToSessionCache = getCachePopulatorInstance(
					FWCacheConstants.SERVICE_TICKET_TO_SESSION_CACHE);
			serviceTicketToSessionCache.update(Action.DELETE, mappingId);
			
			removeApiSecurityKeysFromCache(sessionId);
			expireSession(sessionId);
		}
	}

    @Override
    public void removeSessionByMappingId(String mappingId, HttpServletRequest request) {
    	NeutrinoCachePopulator serviceTicketToSessionCache = getCachePopulatorInstance(FWCacheConstants.SERVICE_TICKET_TO_SESSION_CACHE);
    	String sessionId =  (String) serviceTicketToSessionCache.get(mappingId);
        
    	if (!StringUtils.isEmpty(sessionId)) {        	
            removeBySessionById(sessionId);
        }
    }
    
    
    private NeutrinoCachePopulator getCachePopulatorInstance(String cacheName){
    	if(fwCacheHelper == null){
    		fwCacheHelper = NeutrinoSpringAppContextUtil.getBeanByName("fwCacheHelper", FWCacheHelper.class);
    	}
    	
    	return fwCacheHelper.getFWCachePopulator(cacheName);
    	
    }
    
    
    private void expireSession(String sessionId){
        
        NeutrinoCachePopulator sessionInformationCache = getCachePopulatorInstance(FWCacheConstants.SESSION_REGISTRY_SESSION_IDS_CACHE);
    	NeutrinoSessionInformation neutrinoSessionInformation = (NeutrinoSessionInformation) sessionInformationCache.get(sessionId);
        if (neutrinoSessionInformation == null) {
        	BaseLoggers.exceptionLogger.error("No session information found in session registry against session id: {}", sessionId);
        	return;
        }
    	UserInfo userInfo = (UserInfo) neutrinoSessionInformation.getPrincipal();
    	
    	//In case logout type is already set in session information, do not set it over here
    	if (StringUtils.isEmpty(neutrinoSessionInformation.getLogOutType())) {
    		neutrinoSessionInformation.setLogOutType(NeutrinoSessionInformation.LOGOUT_TYPE_BY_USER);
    	}
    	
        UserSessionManagerService  userSessionManagerService = NeutrinoSpringAppContextUtil.getBeanByName("userSessionManagerService", UserSessionManagerService.class);
        userSessionManagerService.invalidateUserSessionAndUpdateRegistry(userInfo.getId(), neutrinoSessionInformation);
        
    }

	@Override
	public HttpSession removeSessionByMappingId(String mappingId) {
		return null;
	}
	
    @SuppressWarnings("unchecked")
	private void removeApiSecurityKeysFromCache(String sessionId) {
    	NeutrinoCachePopulator apiSecurityCache = getCachePopulatorInstance(FWCacheConstants.API_SECURITY_KEY_CACHE);
    	Object list = apiSecurityCache.get(sessionId);
    	
    	if(list==null) {
    		return;
    	}
    	List<String> cookieList = (List<String>) list;
    	
    	if(cookieList.isEmpty()) {
    		return;
    	}
    	
    	for(int i=0; i<cookieList.size(); i++) {
    		apiSecurityCache.update(Action.DELETE, cookieList.get(i));
    	}
    	
    	apiSecurityCache.update(Action.DELETE, sessionId);
    	
	}

}
