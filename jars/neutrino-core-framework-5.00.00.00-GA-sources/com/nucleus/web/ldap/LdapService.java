package com.nucleus.web.ldap;

import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;

import com.nucleus.user.User;

public interface LdapService {

	public abstract DirContext bindAsUser(String username, String password);

	public abstract DirContextOperations searchForUser(DirContext ctx,
			String username) throws NamingException;

	public BadCredentialsException badCredentials(Throwable cause);

	public BadCredentialsException badCredentials();

	public String rootDnFromDomain(String domain);

	Map<String,Object> createUpdateUserFromLdap(String username, DirContextOperations ctx);
	Map<String,Object> createUpdateUserFromLdap(String username, Map<String, Object> map);

    Map<String, Object> convertDirectContextToMap(DirContextOperations ctx);

}