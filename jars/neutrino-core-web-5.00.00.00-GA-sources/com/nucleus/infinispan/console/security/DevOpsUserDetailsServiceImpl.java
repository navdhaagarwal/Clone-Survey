package com.nucleus.infinispan.console.security;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class DevOpsUserDetailsServiceImpl implements UserDetailsService{

	@Named("devOpsUserManager")
	@Inject
	private DevOpsUserManager devOpsUserManager;

	
	@Override
	public UserDetails loadUserByUsername(String username)  {
		return devOpsUserManager.getDevOpsUser();
	}


}
