package com.nucleus.web.security.frameoptions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.header.HeaderWriter;

/**
 * This is Custom HeaderWriter to add X-Frame-Options, added in place of spring
 * XFrameOptionsHeaderWriter. XFrameOptionsHeaderWriter is calling
 * response.addHeader which can result in multiple values of header, same not
 * identified by browser. This has been fixed in spring-security 5.1.0.M1 so we
 * can use spring support for X-Frame-Options 5.1.0.M1 onwards.
 * 
 * Refer : https://github.com/spring-projects/spring-security/commit/8a458eb9e1cf0349fc4884cd718972c933948e60#diff-f5896119a0140b81d5c95a0698b00838
 * 
 * https://docs.spring.io/spring-security/site/docs/current/reference/html/headers.html#headers-frame-options
 * 
 * @author gajendra.jatav
 *
 */
public class NeutrinoXFrameOptionsHeaderWriter implements HeaderWriter {

	public static final String XFRAME_OPTIONS_HEADER = "X-Frame-Options";

	public static final String SAMEORIGIN = "SAMEORIGIN";

	@Override
	public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
		response.setHeader(XFRAME_OPTIONS_HEADER, SAMEORIGIN);
	}

}
