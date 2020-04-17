package com.nucleus.web.security;

import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;

import com.nucleus.web.request.interceptor.RequestUrlAuditInterceptorUtil;

public class TimeoutHelper {

	private static final String FALSE = "false";

	private TimeoutHelper() {

	}

	public static String checkIdleTimeout(String userId, String logoutDecision, Integer clientBrowserIdleTimeout,
			HttpSession httpSession) {
		String updatedLogoutDecision = logoutDecision;
		String sessionAttributeKey = RequestUrlAuditInterceptorUtil.prepareSessionAttributeKey(httpSession.getId(),
				userId);
		DateTime lastUpdatedAccessTime = RequestUrlAuditInterceptorUtil.getLastUpdatedAccessTimeFromSession(httpSession,
				sessionAttributeKey);
		DateTime currentTime = RequestUrlAuditInterceptorUtil.getCurrentDateTime();

		if (lastUpdatedAccessTime != null) {
			int differenceInMinutes = RequestUrlAuditInterceptorUtil.getDifferenceInMinutes(lastUpdatedAccessTime,
					currentTime);
			if (differenceInMinutes < clientBrowserIdleTimeout.intValue()) {
				updatedLogoutDecision = FALSE;
			}
		}
		return updatedLogoutDecision;
	}

}
