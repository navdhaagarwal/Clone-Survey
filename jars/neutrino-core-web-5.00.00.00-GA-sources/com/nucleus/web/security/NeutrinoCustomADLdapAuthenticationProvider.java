package com.nucleus.web.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.ldap.LdapService;

/**
 * Below code has been taken from Spring source, Modified for Neutrino requirements to support multiple authentication
 * providers even after communication AD communication failure.
 */

/**
 * Specialized LDAP authentication provider which uses Active Directory configuration conventions.
 * <p>
 * It will authenticate using the Active Directory
 * <a href="http://msdn.microsoft.com/en-us/library/ms680857%28VS.85%29.aspx">{@code userPrincipalName}</a>
 * (in the form {@code username@domain}). If the username does not already end with the domain name, the
 * {@code userPrincipalName} will be built by appending the configured domain name to the username supplied in the
 * authentication request. If no domain name is configured, it is assumed that the username will always contain the
 * domain name.
 * <p>
 * The user authorities are obtained from the data contained in the {@code memberOf} attribute.
 *
 * <h3>Active Directory Sub-Error Codes</h3>
 *
 * When an authentication fails, resulting in a standard LDAP 49 error code, Active Directory also supplies its own
 * sub-error codes within the error message. These will be used to provide additional log information on why an
 * authentication has failed. Typical examples are
 *
 * <ul>
 * <li>525 - user not found</li>
 * <li>52e - invalid credentials</li>
 * <li>530 - not permitted to logon at this time</li>
 * <li>532 - password expired</li>
 * <li>533 - account disabled</li>
 * <li>701 - account expired</li>
 * <li>773 - user must reset password</li>
 * <li>775 - account locked</li>
 * </ul>
 *
 * If you set the {@link #setConvertSubErrorCodesToExceptions(boolean) convertSubErrorCodesToExceptions} property to
 * {@code true}, the codes will also be used to control the exception raised.
 *
 * @author Luke Taylor
 * @since 3.1
 */
public final class NeutrinoCustomADLdapAuthenticationProvider extends AbstractLdapAuthenticationProvider {

    
    @Inject
    @Named("ldapService")
    private LdapService ldapService;
    
    
    private String groupforRole;


    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
        String username = auth.getName();
        String password = (String) auth.getCredentials();

        DirContext ctx = null;
        try {
            ctx = ldapService.bindAsUser(username, password);
        } catch (SystemException e) {
            BaseLoggers.securityLogger.info("A communication exception ocuured while connecting to Active directory",
                    e.getCause());
            throw ldapService.badCredentials();
        }

        try {
            return ldapService.searchForUser(ctx, username);

        } catch (NamingException e) {
            logger.error("Failed to locate directory entry for authenticated user: " + username, e);
            throw ldapService.badCredentials(e);
        } finally {
            LdapUtils.closeContext(ctx);
        }
    }

    /**
     * Creates the user authority list from the values of the {@code memberOf} attribute obtained from the user's
     * Active Directory entry.
     */
    @SuppressWarnings("deprecation")
	@Override
    protected Collection<? extends GrantedAuthority> loadUserAuthorities(DirContextOperations userData, String username,
            String password) {
        String[] groups = userData.getStringAttributes(getGroupforRole());

        if (groups == null) {
            logger.debug("No values for 'memberOf' attribute.");

            return AuthorityUtils.NO_AUTHORITIES;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("'memberOf' attribute values: " + Arrays.asList(groups));
        }

        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(groups.length);

        for (String group : groups) {
            authorities.add(new SimpleGrantedAuthority(new DistinguishedName(group).removeLast().getValue()));
        }

        return authorities;
    }

    public String getGroupforRole() {
        return groupforRole;
    }

    public void setGroupforRole(String groupforRole) {
        this.groupforRole = groupforRole;
    }


    

}
