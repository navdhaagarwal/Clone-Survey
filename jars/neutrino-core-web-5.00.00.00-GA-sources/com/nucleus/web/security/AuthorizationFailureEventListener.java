package com.nucleus.web.security;

import javax.inject.Named;

import org.springframework.context.ApplicationListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;

@Named("authorizationFailureEventListener")
public class AuthorizationFailureEventListener implements ApplicationListener<AuthorizationFailureEvent> {
	@Override
	public void onApplicationEvent(AuthorizationFailureEvent event) {
		throw event.getAccessDeniedException();
	}
}
