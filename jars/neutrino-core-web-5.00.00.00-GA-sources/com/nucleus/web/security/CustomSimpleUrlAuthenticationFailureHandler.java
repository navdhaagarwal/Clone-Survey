package com.nucleus.web.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

public class CustomSimpleUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil systemSetupUtil;

    private String          defaultFailureUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        if (exception.getClass().equals(CredentialsExpiredException.class)) {
        	Boolean isLDAPUser = (Boolean)request.getAttribute("isUserSourceSystemLDAP");
        	
        	if(isLDAPUser != null && isLDAPUser.booleanValue() == true) {
                 logger.debug("Redirecting to " + "/app/auth/login?error=true&ldapUserCredentialExpired=true");
                 getRedirectStrategy().sendRedirect(request, response, "/app/auth/login?error=true&ldapUserCredentialExpired=true");
                 return;
    	    }
        	
            logger.debug("Redirecting to " + "/app/resetPassword/redirectDirectToResetPasswordPage");
            getRedirectStrategy().sendRedirect(request, response, "/app/resetPassword/redirectDirectToResetPasswordPage");
        } else {
            this.defaultFailureUrl = systemSetupUtil.getAuthenticationFailureUrl();

            setDefaultFailureUrl(defaultFailureUrl);

            if (defaultFailureUrl == null) {
                logger.debug("No failure URL set, sending 401 Unauthorized error");

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: " + exception.getMessage());
            } else {
                saveException(request, exception);

                if (isUseForward()) {
                    logger.debug("Forwarding to " + defaultFailureUrl);

                    request.getRequestDispatcher(defaultFailureUrl).forward(request, response);
                } else {
                    logger.debug("Redirecting to " + defaultFailureUrl);
                    getRedirectStrategy().sendRedirect(request, response, defaultFailureUrl);
                }
            }
        }
    }

}
