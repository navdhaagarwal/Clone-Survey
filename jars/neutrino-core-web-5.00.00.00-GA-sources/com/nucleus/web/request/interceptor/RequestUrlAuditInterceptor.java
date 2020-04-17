package com.nucleus.web.request.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class RequestUrlAuditInterceptor implements HandlerInterceptor {


	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		  return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {		

	    /*if(request != null){
			HttpSession httpSession = RequestUrlAuditInterceptorUtil.getExistingHttpSession(request);
			String userId = RequestUrlAuditInterceptorUtil.getLoggedInUserId();

			
	    	if( httpSession != null && userId != null ){
								
 				String sessionAttributeKey = RequestUrlAuditInterceptorUtil.prepareSessionAttributeKey(httpSession.getId(),userId);
				
				//Putting current time into session.
				DateTime lastUpdatedAccessTimeFromSessionValue = RequestUrlAuditInterceptorUtil.getCurrentDateTime();
				httpSession.setAttribute(sessionAttributeKey,lastUpdatedAccessTimeFromSessionValue );
	
			}
	    }*/
	}
		
}
