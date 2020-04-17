package com.nucleus.user.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.nucleus.core.NeutrinoSpringAppContextUtil;

/**
 * 
 * Encode passwords with neutrinoPasswordEncoder bean in static context
 * 
 * @author gajendra.jatav
 *
 */
public class UserPasswordEncodingUtil {

	private static PasswordEncoder passwordEncoder;

	private UserPasswordEncodingUtil() {

	}

	public static String encode(String rawPassword) {
		if(rawPassword==null){
			return null;
		}
		if (passwordEncoder == null) {
			passwordEncoder = NeutrinoSpringAppContextUtil.getBeanByName("neutrinoPasswordEncoder",
					PasswordEncoder.class);
		}

		return passwordEncoder.encode(rawPassword);
	}
	
	public static PasswordEncoder  getPasswordEncoder() {
		return passwordEncoder;
	}
	
}
