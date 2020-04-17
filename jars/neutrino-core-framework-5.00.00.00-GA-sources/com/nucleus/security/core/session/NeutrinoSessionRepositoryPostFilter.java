package com.nucleus.security.core.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//This is a temporary CLASS to handle movement to Dashboard in case of FAILOVER
//This is to be removed once code clean up for session attribute usage across modules is completely done

public class NeutrinoSessionRepositoryPostFilter extends OncePerRequestFilter {

    private static final int CUSTOM_SESSION_FAILOVER_ERROR_CODE = 902;

    @Value("${session.failover.enabled:false}")
    private boolean isSessionFailoverEnabled;

    //Once clean up is done - This property to be removed from framework-config.properties of all modules
    @Value("${session.failover.move.to.homepage:false}")
    private boolean moveToHomePageOnSessionFailover;

    @Value("${session.failover.cookie.suffix:ROUTE}")
    private String stickySessionCookieSuffix;

    //Once clean up is done - This property to be removed from core-web-security-config.properties of all modules
    @Value("${core.web.config.session.failover.url:/app/dashboard?errCode=ERR.SESSIONFAILOVER.MSG}")
    private String sessionFailoverBaseUrl;

    private boolean reportServer = false;
    private RedirectStrategy redirectStrategy;


    @PostConstruct
    public void init() {
        if (!stickySessionCookieSuffix.equals("ROUTE")) {
            reportServer = true;
        }
    }

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isSessionFailoverEnabled) {
            if (!moveToHomePageOnSessionFailover || reportServer) {
                filterChain.doFilter(request, response);
            } else if (request.getSession(false) != null && request.getSession(false).getAttribute(NeutrinoMapSession.FROM_REMOTE_STORE_ATTR).equals(true)) {
                request.getSession().setAttribute(NeutrinoMapSession.FROM_REMOTE_STORE_ATTR, false);
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.sendError(CUSTOM_SESSION_FAILOVER_ERROR_CODE);
                } else {
                    redirectStrategy.sendRedirect(request, response, getSessionFailoveUrl());
                }
            } else {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String getSessionFailoveUrl() {
        return sessionFailoverBaseUrl + "&errorOccurred=true";
    }


}
