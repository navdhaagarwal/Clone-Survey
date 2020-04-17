package com.nucleus.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * @author Nucleus Software Exports Limited
 * @description implementation of Spring LogoutSuccessHandler for OAuth token store.
 */
public class OAuthLogoutHandler implements LogoutSuccessHandler {

    private static final String tokenSeparator = " ";
    
    //Conditional bean that might be null if API portal is enabled.
  	@Autowired(required = false)
    private TokenStore      tokenStore;

    /**
     * Default constructor.
     */
    public OAuthLogoutHandler() {

    }

    /**
     * Constructor with a TokenStore
     * 
     * @param tokenStore Spring token store to handle logout.
     */
    public OAuthLogoutHandler(TokenStore tokenStore) {
        super();
        this.tokenStore = tokenStore;
    }

    /**
     * Handle a logout request and return appropriate response.
     * 
     * @param request HttpServletRequest object of incoming HTTP request.
     * @param response HttpServletResponse object. 
     * @param authentication Represents the token for an authentication request.
     * 
     * @return ServiceError with error message and description
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        boolean logoutSuccessfull = false;
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null) {
            int indexOfSeparator = authorizationHeader.indexOf(tokenSeparator);
            if (indexOfSeparator > -1) {
                String tokenValue = authorizationHeader.substring(indexOfSeparator).trim();
                if (tokenValue != null && !tokenValue.isEmpty() && tokenStore != null) {
                    OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
                    if (accessToken != null) {
                        tokenStore.removeRefreshToken(accessToken.getRefreshToken());
                        tokenStore.removeAccessToken(accessToken);
                        response.setStatus(HttpStatus.OK.value());
                        logoutSuccessfull = true;
                    }
                }
            }
        }
        if (!logoutSuccessfull) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    public TokenStore getTokenStore() {
        return tokenStore;
    }

    public void setTokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

}