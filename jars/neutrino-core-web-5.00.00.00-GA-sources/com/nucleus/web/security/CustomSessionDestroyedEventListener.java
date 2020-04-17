package com.nucleus.web.security;

import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.session.SessionRegistry;

import com.nucleus.security.core.session.NeutrinoSessionRegistryImpl;

public class CustomSessionDestroyedEventListener implements ApplicationListener<SessionDestroyedEvent> {

	
	
	private SessionRegistry sessionRegistry;
	
	
	public SessionRegistry getSessionRegistry() {
		return sessionRegistry;
	}
	public void setSessionRegistry(SessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}
	
	
	@Override
	public void onApplicationEvent(SessionDestroyedEvent event) {
		NeutrinoSessionRegistryImpl sessionRegistryImpl = (NeutrinoSessionRegistryImpl) sessionRegistry;
		String sessionId = event.getId();
		sessionRegistryImpl.removeSessionInformation(sessionId);
		
	}

}
