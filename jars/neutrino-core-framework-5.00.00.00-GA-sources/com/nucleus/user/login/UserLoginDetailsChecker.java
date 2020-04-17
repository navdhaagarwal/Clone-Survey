package com.nucleus.user.login;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import com.nucleus.user.User;
import com.nucleus.user.UserService;

@Named("userLoginDetailsChecker")
public class UserLoginDetailsChecker{
	
	public boolean validateLoginUser(User user) {
		
		if (!user.isAccountNonLocked()) {
			throw new LockedException("User account is locked");
		}

		if (!user.isEnabled()) {
			throw new DisabledException("User is disabled");
		}

		if(!user.isLoginEnabled()) {
			throw new AuthenticationServiceException("User is disabled to login");
		}
		
		String sourceSystem = user.getSourceSystem();
		if (StringUtils.isBlank(sourceSystem)) {
			throw new AuthenticationServiceException("User's source system is not available");
		}

		if (!UserService.SOURCE_DB.equals(sourceSystem) && !UserService.SOURCE_LDAP.equals(sourceSystem) && !UserService.SOURCE_FEDERATED.equals(sourceSystem)) {
			throw new AuthenticationServiceException("User does not belong to ldap or db");
		}

		return true;
	}

}
