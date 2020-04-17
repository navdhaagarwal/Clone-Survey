package com.nucleus.web.csrf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
* A Spring MVC <code>HandlerInterceptor</code> which is responsible to enforce CSRF token validity on incoming posts requests. The interceptor
* should be registered with Spring MVC servlet using the following syntax:
* <pre>
* &lt;mvc:interceptors&gt;
* &lt;bean class="com.nucleus.web.csrf.CSRFHandlerInterceptor"/&gt;
* &lt;/mvc:interceptors&gt;
* </pre>
* Code adapted from Eyallupu's blog suggestion to handle csrf.
* @author Nucleus software. 
* @see CSRFRequestDataValueProcessor
*/
public class CSRFHandlerInterceptor extends HandlerInterceptorAdapter {

 

}
