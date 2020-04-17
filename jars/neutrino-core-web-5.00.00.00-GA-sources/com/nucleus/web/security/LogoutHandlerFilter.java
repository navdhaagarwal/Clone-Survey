package com.nucleus.web.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

public class LogoutHandlerFilter implements LogoutHandler {

	@Override
	public void logout(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication) {

		
		  /*Cookie cookie = new Cookie("JSESSIONID", null);
		  cookie.setPath(request.getContextPath());
		  cookie.setMaxAge(0);
		  response.addCookie(cookie);*/
		
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookieToDelete : cookies) {
				if ("JSESSIONID".equals(cookieToDelete.getName())) {
				//	request.getCookies()[index].setValue(null);
					response.setContentType("text/html");
					//cookie.setPath(request.getContextPath());
					cookieToDelete.setMaxAge(0);
					cookieToDelete.setValue("");
					cookieToDelete.setVersion(0);
				//	cookieToDelete.setDomain(SSORealm.SSO_DOMAIN);
					cookieToDelete.setComment("EXPIRING COOKIE at " + System.currentTimeMillis());
					cookieToDelete.setPath("/");
					response.addCookie(cookieToDelete);
					break;
				}
			}
		}
	}

}