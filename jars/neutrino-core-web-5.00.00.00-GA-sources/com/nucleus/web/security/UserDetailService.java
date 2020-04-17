package com.nucleus.web.security;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSecurityService;

/**
 * Implementation of the Spring Security's UserDetailsService Interface, it loads 
 * the User information to authenticate the User.
 * 
 *  @author Nucleus Software Exports Limited
 */

@Named(value = "userDetailService")
public class UserDetailService implements UserDetailsService {

    @Inject
    @Named("userSecurityService")
    private UserSecurityService userSecurityService;

    @Override
    @MonitoredWithSpring(name = "UDC_LOAD_USR_BY_USRNAME")
    @Transactional(readOnly=true)
    public UserInfo loadUserByUsername(String username) throws UsernameNotFoundException, CredentialsExpiredException {
    
        UserInfo userInfo = userSecurityService.getCompleteUserFromUsername(username);
   
        return userInfo;

    }

}
