package com.nucleus.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSecurityService;

/**
 * 
 * @author Nucleus Software India Pvt Ltd TODO -> ruchir.sachdeva Add
 *         documentation to class
 */
@Named(value = "ldapUserDetailMapper")
public class LdapUserDetailMapper implements UserDetailsContextMapper {

    // @Inject
    // @Named("userSecurityService")
    // private UserSecurityService userSecurityService;

	 @Inject
	 @Named("userSecurityService")
	 private UserSecurityService userSecurityService;
	 
	 
    @Override
    public UserInfo mapUserFromContext(DirContextOperations ctx, String username,
            Collection<? extends GrantedAuthority> ldapUserAuthorities) {
        BaseLoggers.flowLogger.info("user authenticated from LDAP.");

        List<String> roleNames = new ArrayList<String>();
        for (GrantedAuthority ldapUserAuthority : ldapUserAuthorities) {
            /*String ldapGroup = ldapUserAuthority.getAuthority();
            if (ldapGroup.startsWith("NEUTRINO.")) {
            roleNames.add(StringUtils.substringAfter(ldapGroup, "NEUTRINO."));
            }else if(ldapGroup.startsWith(ProductInformationLoader.getProductName().toUpperCase())){
            roleNames.add(ldapGroup);
            }*/
            roleNames.add(ldapUserAuthority.getAuthority());
        }

        /*if (roleNames.isEmpty()) {
            throw new SystemException("The user does not have any CAS roles configured in the Active directory");
        }*/
         UserInfo userInfo = userSecurityService.populateUserFromLDAP(username, roleNames, ctx,true);
		if (!ValidatorUtils.isNull(userInfo)) {
			if (!(userInfo.isAccountNonLocked())) {
				throw new LockedException("AbstractUserDetailsAuthenticationProvider.locked.ldap");
			}
			if (!userInfo.isEnabled()) {
				throw new DisabledException("AbstractUserDetailsAuthenticationProvider.disabled");
			}
			
		}
        //UserInfo userInfo = userDetailService.loadUserByUsername(username);
        BaseLoggers.flowLogger.info("user loaded by username from database " + userInfo);
        return userInfo;

    }

    @Override
    public void mapUserToContext(UserDetails userDetails, DirContextAdapter ctx) {

    }
}
