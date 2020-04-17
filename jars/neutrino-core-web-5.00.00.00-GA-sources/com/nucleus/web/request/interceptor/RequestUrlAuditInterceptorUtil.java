package com.nucleus.web.request.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nucleus.user.UserInfo;

public class RequestUrlAuditInterceptorUtil {
	
	private RequestUrlAuditInterceptorUtil(){}
	
	public static boolean hasPreviousSessionValue(HttpSession httpSession,String lastUrlAccessTimeStampSessionAttrKey) {
		
		return httpSession.getAttribute(lastUrlAccessTimeStampSessionAttrKey)!=null?true:false;
	}
	
	public static DateTime getCurrentDateTime(){
		return (new DateTime());
	}
	
	public static HttpSession getExistingHttpSession(HttpServletRequest request){
		return request.getSession(false);
	}
	
	public static  int getDifferenceInMinutes(DateTime lastAccessTime,DateTime currentTime) {
		Period period = new Period(lastAccessTime, currentTime);
		int differenceInMinutes = period.getMinutes();
		int differenceInhour = period.getHours();
		
	    return (differenceInhour*60+differenceInMinutes);
	}
	
	public static DateTime getLastUpdatedAccessTimeFromSession(HttpSession httpSession,String lastUpdatedAccessTimeFromSessionKey) {
	
		return (DateTime)httpSession.getAttribute(lastUpdatedAccessTimeFromSessionKey);
	}
	
	public static  String prepareSessionAttributeKey(String sessionId, String userId) {
		String sessionAttributeKey = null;
		
		if(!sessionId.isEmpty() && !userId.isEmpty()){
			sessionAttributeKey = sessionId+"_"+userId; 
		}
		return sessionAttributeKey;
	}
		
	public static String  getLoggedInUserId() {
        UserInfo userInfo = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null && securityContext.getAuthentication() != null) {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (UserInfo.class.isAssignableFrom(principal.getClass())) {
                userInfo = (UserInfo) principal;
            }
        }
        if(userInfo != null){
        	return String.valueOf(userInfo.getId());
        }
        return null;
    }	
}
