package com.nucleus.web.security;

import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

/**
 * Application event which indicates successful authentication.
 *
 * @author Aman Garg
 */
@SuppressWarnings("serial")
public class SSOAuthenticationSuccessEvent extends AuthenticationSuccessEvent {
	// ~ Constructors
	// ===================================================================================================

	public SSOAuthenticationSuccessEvent(Authentication authentication) {
		super(authentication);
	}
}

