/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.nucleus.authenticationToken.TokenDetails;
import com.nucleus.token.CustomLoginTokenHandlingService;
import com.nucleus.user.UserService;

/**
 * Base class for token based auto login services
 * 
 * @author Nucleus Software Exports Limited
 * 
 */
public abstract class AbstractCustomTokenLoginServices implements CustomTokenLoginServices, InitializingBean, LogoutHandler {

    protected final Logger                                     logger                      = LoggerFactory
                                                                                                   .getLogger(getClass());

    public static final int                                    TWO_WEEKS_S                 = 1209600;
    private static final String                                DELIMITER                   = ":";

    private String                                             key;
    private int                                                tokenValiditySeconds        = TWO_WEEKS_S;

    private GrantedAuthoritiesMapper                           authoritiesMapper           = new NullAuthoritiesMapper();
    private UserDetailsService                                 userDetailsService;
    private UserDetailsChecker                                 userDetailsChecker          = new AccountStatusUserDetailsChecker();
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private String                                             requestParamName;

    @Inject
    @Named("customLoginTokenHandlingService")
    protected CustomLoginTokenHandlingService                  customLoginTokenHandlingService;

    @Inject
    @Named("userService")
    protected UserService                                      userService;

    protected AbstractCustomTokenLoginServices(String key, UserDetailsService userDetailsService) {
        Assert.hasLength(key, "key cannot be empty or null");
        Assert.notNull(userDetailsService, "UserDetailsService cannot be null");
        this.key = key;
        this.userDetailsService = userDetailsService;

    }

    public void afterPropertiesSet() throws Exception {
        Assert.hasLength(key, "key cannot be empty or null");
        Assert.notNull(userDetailsService, "A UserDetailsService is required");
    }

    public final Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        String loginToken = extractCustomLoginToken(request);
        String otp = extractOTP(request);

        if (loginToken == null || otp == null) {
            logger.debug("Login token not available");
            return null;
        }

        logger.debug("Custom auto login parameter detected");

        if (loginToken.length() == 0 || otp.length() == 0) {
            logger.debug("Login token was empty");
            return null;
        }

        UserDetails user = null;

        try {
            String[] loginTokens = decodeToken(loginToken);
            user = processAutoLoginToken(loginTokens, request, response);
            TokenDetails otpDetails = customLoginTokenHandlingService.getOTPTokenDetails(otp);

            // check for OTP validity with Custom Login Token
            processCustomTokenAndOTP(user, otpDetails);

            userDetailsChecker.check(user);

            logger.debug("Auto login token accepted");

            return createSuccessfulAuthentication(request, user);
        } catch (UsernameNotFoundException noUser) {
            logger.debug("Token login was valid but corresponding user not found.", noUser);
        } catch (InvalidLoginTokenException invalidToken) {
            logger.debug("Invalid login token: " + invalidToken.getMessage());
        } catch (AccountStatusException statusInvalid) {
            logger.debug("Invalid UserDetails: " + statusInvalid.getMessage());
        } catch (TokenLoginAuthenticationException e) {
            logger.debug(e.getMessage());
        }

        return null;
    }

    public final Authentication validateLoginToken(HttpServletRequest request, HttpServletResponse response) {
        String loginToken = extractCustomLoginToken(request);

        if (loginToken == null) {
            logger.debug("Login token not available");
            return null;
        }

        logger.debug("Custom auto login parameter detected");

        if (loginToken.length() == 0) {
            logger.debug("Login token was empty");
            return null;
        }

        UserDetails user = null;

        try {
            String[] loginTokens = decodeToken(loginToken);
            user = processAutoLoginToken(loginTokens, request, response);
            userDetailsChecker.check(user);

            logger.debug("Auto login token accepted");

            return createSuccessfulAuthentication(request, user);
        } catch (UsernameNotFoundException noUser) {
            logger.debug("Token login was valid but corresponding user not found.", noUser);
        } catch (InvalidLoginTokenException invalidToken) {
            logger.debug("Invalid login token: " + invalidToken.getMessage());
        } catch (AccountStatusException statusInvalid) {
            logger.debug("Invalid UserDetails: " + statusInvalid.getMessage());
        } catch (TokenLoginAuthenticationException e) {
            logger.debug(e.getMessage());
        }

        return null;
    }

    protected String extractCustomLoginToken(HttpServletRequest req) {
        String loginToken = req.getParameter(getRequestParamName());
        return loginToken;
    }

    protected Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails user) {
        AutoLoginAuthenticationToken auth = new AutoLoginAuthenticationToken(key, user,
                authoritiesMapper.mapAuthorities(user.getAuthorities()));
        auth.setDetails(authenticationDetailsSource.buildDetails(request));
        return auth;
    }

    protected String[] decodeToken(String tokenValue) throws InvalidCookieException {
        // for (int j = 0 ; j < tokenValue.length() % 4 ; j++) {
        // tokenValue = tokenValue + "=";
        // }

        if (!Base64.isBase64(tokenValue.getBytes())) {
            throw new InvalidLoginTokenException("Login token was not Base64 encoded; value was '" + tokenValue + "'");
        }

        String tokenAsPlainText = new String(Base64.decode(tokenValue.getBytes()));

        String[] tokens = StringUtils.delimitedListToStringArray(tokenAsPlainText, DELIMITER);

        if ((tokens[0].equalsIgnoreCase("http") || tokens[0].equalsIgnoreCase("https")) && tokens[1].startsWith("//")) {
            // Assume we've accidentally split a URL (OpenID identifier)
            String[] newTokens = new String[tokens.length - 1];
            newTokens[0] = tokens[0] + ":" + tokens[1];
            System.arraycopy(tokens, 2, newTokens, 1, newTokens.length - 1);
            tokens = newTokens;
        }

        return tokens;
    }

    protected String encodeToken(String[] tokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < tokens.length ; i++) {
            sb.append(tokens[i]);

            if (i < tokens.length - 1) {
                sb.append(DELIMITER);
            }
        }

        String value = sb.toString();

        sb = new StringBuilder(new String(Base64.encode(value.getBytes())));

        return sb.toString();
    }

    protected abstract UserDetails processAutoLoginToken(String[] loginTokenParts, HttpServletRequest request,
            HttpServletResponse response) throws TokenLoginAuthenticationException, UsernameNotFoundException;

    protected abstract void processCustomTokenAndOTP(UserDetails user, TokenDetails otpDetails)
            throws InvalidLoginTokenException;

    /**
     * @return the authoritiesMapper
     */
    public GrantedAuthoritiesMapper getAuthoritiesMapper() {
        return authoritiesMapper;
    }

    /**
     * @return the userDetailsService
     */
    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    /**
     * @return the userDetailsChecker
     */
    public UserDetailsChecker getUserDetailsChecker() {
        return userDetailsChecker;
    }

    /**
     * @return the authenticationDetailsSource
     */
    public AuthenticationDetailsSource<HttpServletRequest, ?> getAuthenticationDetailsSource() {
        return authenticationDetailsSource;
    }

    /**
     * @param authoritiesMapper
     *            the authoritiesMapper to set
     */
    public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }

    /**
     * @param userDetailsService
     *            the userDetailsService to set
     */
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * @param userDetailsChecker
     *            the userDetailsChecker to set
     */
    public void setUserDetailsChecker(UserDetailsChecker userDetailsChecker) {
        this.userDetailsChecker = userDetailsChecker;
    }

    /**
     * @param authenticationDetailsSource
     *            the authenticationDetailsSource to set
     */
    public void setAuthenticationDetailsSource(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the tokenValiditySeconds
     */
    public int getTokenValiditySeconds() {
        return tokenValiditySeconds;
    }

    /**
     * @param tokenValiditySeconds
     *            the tokenValiditySeconds to set
     */
    public void setTokenValiditySeconds(int tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    /**
     * 
     * @param username
     * @return
     */
    public String generateToken(String username) {
        return null;
    }

    /**
     * @return the requestParamName
     */
    public String getRequestParamName() {
        return requestParamName;
    }

    /**
     * @param requestParamName
     *            the requestParamName to set
     */
    public void setRequestParamName(String requestParamName) {
        this.requestParamName = requestParamName;
    }

    protected String extractOTP(HttpServletRequest request) {
        return request.getParameter("OTP");
    }

    public CustomLoginTokenHandlingService getCustomLoginTokenHandlingService() {
        return customLoginTokenHandlingService;
    }

    public void setCustomLoginTokenHandlingService(CustomLoginTokenHandlingService customLoginTokenHandlingService) {
        this.customLoginTokenHandlingService = customLoginTokenHandlingService;
    }

}
