package com.nucleus.web.security;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
/**
 * Appends security token in location header url while sending redirect.
 * 
 * @author gajendra.jatav
 *
 */
public class NeutrinoResponseWrapper extends HttpServletResponseWrapper{


    private String csrfToken;
    
	public NeutrinoResponseWrapper(HttpServletResponse response) {
		super(response);
	}
	
	public String getCsrfToken() {
		return csrfToken;
	}

	public void setCsrfToken(String csrfToken) {
		this.csrfToken = csrfToken;
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		super.sendRedirect(URLBuilderHelper.appendSecurityTokenAndTimeStampToURL(location, csrfToken));
	}

}
