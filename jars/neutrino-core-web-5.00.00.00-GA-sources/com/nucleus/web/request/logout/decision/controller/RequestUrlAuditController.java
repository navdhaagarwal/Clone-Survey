package com.nucleus.web.request.logout.decision.controller;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSessionManagerService;
import com.nucleus.web.common.CommonConfigUtility;
import com.nucleus.web.request.interceptor.RequestUrlAuditInterceptorUtil;
import com.nucleus.web.security.TimeoutHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.ServletContextAware;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@Controller
@RequestMapping("/logout")
public class RequestUrlAuditController implements ServletContextAware {

	private static final String LOGOUT = "LOGOUT";

	private static final String LOGOUT_FROM_ALL = "LOGOUT_FROM_ALL";

	private static ServletContext servletContext;

	@Inject
	@Named("commonConfigUtility")
	private CommonConfigUtility commonConfigUtility;

	@Inject
	@Named("userSessionManagerService")
	private UserSessionManagerService userSessionManagerService;

	@Value(value = "#{'${core.web.config.SSO.request.encryption.key}'}")
	private String ssoEncryptionKey;

	@Inject
	@Named("neutrinoRestTemplate")
	private RestTemplate restTemplate;

	@Value(value = "#{'${core.web.config.SSO.ticketvalidator.url.value}'}")
	private String ssoUrl;

	@Inject
	@Named("sessionRegistry")
	private NeutrinoSessionRegistry sessionRegistry;

	/**private String checkIdleTimeOutURL = "/checkIdleTimeOutForAllModules";
	
	private static final String UTF_8 = "UTF-8";

	private static final String SEPARATOR = ":";**/

	@RequestMapping("/decision/clientIdleTimeout")
	public ResponseEntity<String> getDecisionForLogout(@RequestParam("userId") String userId,
			HttpServletRequest request){

		String logoutDecision = LOGOUT;

		/*Integer clientBrowserIdleTimeout = null;
		HttpSession httpSession = RequestUrlAuditInterceptorUtil.getExistingHttpSession(request);
		try {
			clientBrowserIdleTimeout = (Integer) servletContext.getAttribute("clientBrowserIdleTimeout");

			if (httpSession != null && userId != null) {
				logoutDecision = TimeoutHelper.checkIdleTimeout(userId, logoutDecision, clientBrowserIdleTimeout,
						httpSession);
			}
		} catch (Exception exception) {
			BaseLoggers.flowLogger
					.error("Exception during logout decision on idle timeout of client browser " , exception);
			logoutDecision = LOGOUT;
		}*/

		logoutDecision = getDecisonForSSOLogout( logoutDecision);
		return new ResponseEntity<>(logoutDecision, HttpStatus.OK);

	}

	private String getDecisonForSSOLogout( String logoutDecision)
	{

		if (!commonConfigUtility.getSsoActive() || !LOGOUT.equals(logoutDecision)) {
			return logoutDecision;
		}
		String finalLogoutDecision = logoutDecision;
		UserInfo user = userSessionManagerService.getLoggedinUserInfo();

		Set<NeutrinoSessionInformation> sessionIdsList=sessionRegistry.getAllSessionsAcrossModule(user,true);
		if(sessionIdsList!=null && !sessionIdsList.isEmpty() && sessionIdsList.size()<=1){
			finalLogoutDecision = LOGOUT_FROM_ALL;
		}
		return finalLogoutDecision;
	}


	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
