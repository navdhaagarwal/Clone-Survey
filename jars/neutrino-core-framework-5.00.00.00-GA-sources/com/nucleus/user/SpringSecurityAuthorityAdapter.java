package com.nucleus.user;

import org.springframework.security.core.GrantedAuthority;

public class SpringSecurityAuthorityAdapter implements GrantedAuthority {

	private static final long serialVersionUID = -4382157346612670824L;

	private final String      authorityString;

	public SpringSecurityAuthorityAdapter(String authorityString) {
		this.authorityString = authorityString;
	}

	@Override
	public String getAuthority() {
		return authorityString;
	}
}
