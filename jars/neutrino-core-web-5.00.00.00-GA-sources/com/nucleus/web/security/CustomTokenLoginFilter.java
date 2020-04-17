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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filter to process auto login requests using valid token
 * 
 * @author Nucleus Software Exports Limited
 * 
 */
public class CustomTokenLoginFilter extends GenericFilterBean implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher    eventPublisher;
    private AuthenticationManager        authenticationManager;
    private CustomTokenLoginServices     customTokenLoginServices;
    private String                       filterProcessesUrl;
    private RequestMatcher               requestMatcher;

    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;

    public CustomTokenLoginFilter() {
    }

    public CustomTokenLoginFilter(AuthenticationManager authenticationManager,
            CustomTokenLoginServices customTokenLoginServices, String filterProcessesUrl,
            AuthenticationSuccessHandler successHandler, AuthenticationFailureHandler failureHandler) {
        this.authenticationManager = authenticationManager;
        this.customTokenLoginServices = customTokenLoginServices;
        this.filterProcessesUrl = filterProcessesUrl;
        this.requestMatcher = new FilterProcessUrlRequestMatcher(filterProcessesUrl);
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;

    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // if request matches for custom token login
        if (requestMatcher.matches(request)) {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                Authentication customLoginAuth = null;
                if ("GET".equals(request.getMethod())) {

                    // if request is GET
                    // let the user go to the otp generating page if token is present and valid
                    // extract token form request url

                    customLoginAuth = customTokenLoginServices.validateLoginToken(request, response);

                    if (customLoginAuth != null) {
                        // Attempt authenticaton via AuthenticationManager
                        try {
                            customLoginAuth = authenticationManager.authenticate(customLoginAuth);

                            // Store to SecurityContextHolder
                            /*
                             * if (req.getParameter("otp") != null) {
                             * SecurityContextHolder
                             * .getContext().setAuthentication(customLoginAuth);
                             * }
                             */
                            onSuccessfulAuthentication(request, response, customLoginAuth);

                            if (logger.isDebugEnabled()) {
                                logger.debug("Token is valid, request is directed to OTP page.");

                            }
                        } catch (AuthenticationException authenticationException) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Authentication exception occured while validating custon login token, "
                                        + "AuthenticationManager rejected Authentication returned by CustomLogin token: '"
                                        + customLoginAuth + "'; invalidating custom login token", authenticationException);
                            }

                            // No need for extra processing if login failed
                            // customTokenLoginServices.loginFail(request,
                            // response);

                            onUnsuccessfulAuthentication(request, response, authenticationException);

                            // Ask this one. (If token is invalid in case of get request then return and don't process filter
                            // chain further.)
                            return;
                        }
                    } else {

                        /* Handle Authentication failure when Token not available or invalid*/
                        onUnsuccessfulAuthentication(request, response,
                                new InvalidLoginTokenException("Invlaid Login Token"));
                    }
                } else if ("POST".equals(request.getMethod())) {

                    customLoginAuth = customTokenLoginServices.autoLogin(request, response);

                    if (customLoginAuth != null) {
                        // Attempt authenticaton via AuthenticationManager
                        try {
                            customLoginAuth = authenticationManager.authenticate(customLoginAuth);

                            // Store to SecurityContextHolder
                            SecurityContextHolder.getContext().setAuthentication(customLoginAuth);

                            onSuccessfulAuthentication(request, response, customLoginAuth);

                            if (logger.isDebugEnabled()) {
                                logger.debug("SecurityContextHolder populated with auto-login token");

                            }

                            // Fire event
                            if (this.eventPublisher != null) {
                                eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(SecurityContextHolder
                                        .getContext().getAuthentication(), this.getClass()));
                            }

                            if (successHandler != null) {
                                successHandler.onAuthenticationSuccess(request, response, customLoginAuth);

                                return;
                            }

                        } catch (AuthenticationException authenticationException) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("SecurityContextHolder not populated with custom login token, as "
                                        + "AuthenticationManager rejected Authentication returned by CustomLogin token: '"
                                        + customLoginAuth + "'; invalidating custom login token", authenticationException);
                            }

                            // No need for extra processing if login failed
                            // customTokenLoginServices.loginFail(request,
                            // response);

                            onUnsuccessfulAuthentication(request, response, authenticationException);
                        }

                        // if rrequest is POST
                        // check for presence of token and otp, authenticate and
                        // go forward
                    } else {

                        /* Handle Authentication failure when Token not available or invalid*/
                        onUnsuccessfulAuthentication(request, response,
                                new InvalidLoginTokenException("Invlaid Login Token"));
                    }

                }
            }

            chain.doFilter(request, response);
        } else { // If request is not for custom login
            chain.doFilter(request, response);
        }

        // if not authenticated yet, attempt auto login
    }

    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            Authentication authResult) {
    }

    protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) {
        try {
            failureHandler.onAuthenticationFailure(request, response, failed);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception occured while calling Authentication Failure Handler: " + e.getMessage());
            }
        } catch (ServletException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception occured while calling Authentication Failure Handler: " + e.getMessage());
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(authenticationManager, "authenticationManager must be specified");
        Assert.notNull(customTokenLoginServices, "token login Services must be specified");
        Assert.notNull(failureHandler, "failureHandler must be specified");
        Assert.notNull(requestMatcher, "requestMatcher must be specified");
        Assert.notNull(filterProcessesUrl, "filterProcessesUrl must be specified");
    }

    /**
     * @return the eventPublisher
     */
    public ApplicationEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    /**
     * @return the successHandler
     */
    public AuthenticationSuccessHandler getSuccessHandler() {
        return successHandler;
    }

    /**
     * @return the authenticationManager
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * @return the customTokenLoginServices
     */
    public CustomTokenLoginServices getCustomTokenLoginServices() {
        return customTokenLoginServices;
    }

    /**
     * @param eventPublisher
     *            the eventPublisher to set
     */
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * @param successHandler
     *            the successHandler to set
     */
    public void setSuccessHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    /**
     * @param authenticationManager
     *            the authenticationManager to set
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * @param customTokenLoginServices
     *            the customTokenLoginServices to set
     */
    public void setCustomTokenLoginServices(CustomTokenLoginServices customTokenLoginServices) {
        this.customTokenLoginServices = customTokenLoginServices;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public String getFilterProcessesUrl() {
        return filterProcessesUrl;
    }

    public void setFilterProcessesUrl(String filterProcessesUrl) {
        this.filterProcessesUrl = filterProcessesUrl;
    }

    public RequestMatcher getRequestMatcher() {
        return requestMatcher;
    }

    public void setRequestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }

    public AuthenticationFailureHandler getFailureHandler() {
        return failureHandler;
    }

    public void setFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    private static final class FilterProcessUrlRequestMatcher implements RequestMatcher {
        private final String filterProcessesUrl;

        private FilterProcessUrlRequestMatcher(String filterProcessesUrl) {
            Assert.hasLength(filterProcessesUrl, "filterProcessesUrl must be specified");
            Assert.isTrue(UrlUtils.isValidRedirectUrl(filterProcessesUrl), filterProcessesUrl
                    + " isn't a valid redirect URL");
            this.filterProcessesUrl = filterProcessesUrl;
        }

        public boolean matches(HttpServletRequest request) {
            String uri = request.getRequestURI();
            int pathParamIndex = uri.indexOf(';');

            if (pathParamIndex > 0) {
                // strip everything after the first semi-colon
                uri = uri.substring(0, pathParamIndex);
            }

            String filterProcessesUrl = this.filterProcessesUrl;

            if (filterProcessesUrl.endsWith("*")) {
                filterProcessesUrl = filterProcessesUrl.replaceAll("\\*", "");
            }

            return uri.contains(filterProcessesUrl);
        }
    }
}
