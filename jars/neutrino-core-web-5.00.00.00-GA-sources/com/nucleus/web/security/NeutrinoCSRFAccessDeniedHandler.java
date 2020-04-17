package com.nucleus.web.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import com.nucleus.web.common.CommonConfigUtility;


public class NeutrinoCSRFAccessDeniedHandler extends AccessDeniedHandlerImpl {

	@Inject
	@Named("commonConfigUtility")
	private CommonConfigUtility commonConfigUtility;
	
	private static final String LOGIN = "login";
	private static final String APP_AUTH_LOGIN = "/app/auth/login?error=true";
	private static final String CURRENT_SESSION_EXPIRED = "currentSessionExpired";
	
	
	
	@Override
	public void handle(HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException,
			ServletException {
		
				if (accessDeniedException instanceof MissingCsrfTokenException
						|| accessDeniedException instanceof InvalidCsrfTokenException) {
		
					if (request.getRequestURI().contains(LOGIN)) {
						request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", new SessionAuthenticationException(CURRENT_SESSION_EXPIRED));
						response.sendRedirect(request.getContextPath() + APP_AUTH_LOGIN);
						
					}
					else if(commonConfigUtility.getSsoActive()){
					response.sendRedirect(request.getContextPath() + APP_AUTH_LOGIN);
					}
				super.handle(request, response, accessDeniedException);
	}else if(accessDeniedException instanceof AccessDeniedException){
		response.sendRedirect(request.getContextPath() + "/app/webExceptionHandler/accessDenied");
		super.handle(request, response, accessDeniedException);
	}
}

}
