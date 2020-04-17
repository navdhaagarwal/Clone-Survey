package com.nucleus.user.security;

import javax.inject.Named;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.nucleus.user.User;

/**
 * 
 * @deprecated use UserPasswordEncodingUtil instead
 *
 */
@Deprecated
@Named("userPasswordEncodingManager")
public class UserPasswordEncodingManager {

	public PasswordEncoder getPasswordEncoder() {
		return UserPasswordEncodingUtil.getPasswordEncoder();
	}

	public boolean matches(String password,User user,String encryptedPassword){
		return true;
	}
}
