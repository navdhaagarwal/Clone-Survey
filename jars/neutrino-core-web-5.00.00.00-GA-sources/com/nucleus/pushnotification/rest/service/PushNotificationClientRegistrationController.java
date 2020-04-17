package com.nucleus.pushnotification.rest.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nucleus.pushnotification.service.PushNotificationClientService;
import com.nucleus.pushnotification.vo.PushNoticationsClient;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.ws.core.entities.PushNotificationClientDetail;

@RestController
@RequestMapping("/restservice")
@Transactional(propagation = Propagation.REQUIRED)
public class PushNotificationClientRegistrationController {
	private static final String STATUS = "status";
	private static final String MESSAGE = "message";
	private static final String FAILURE = "failure";
	private static final String SUCCESS = "success";
	@Inject
	@Named("pushNotificationClientService")
	private PushNotificationClientService pushNotificationClientService;

	@Inject
	@Named(value = "userService")
	UserService userService;

	@Autowired
	private Environment environment;

	private Boolean isApiManagerEnabled = null;

	@PostMapping(value = "/pushnotification/register", consumes = "application/json")
	public ResponseEntity<Map<String, String>> register(HttpServletRequest req,
			@RequestBody PushNoticationsClient pushNoticationsClientReq) {
		Map<String, String> resultMap = new HashMap<>();
		if (pushNoticationsClientReq == null || pushNoticationsClientReq.getNotificationClientId() == null) {
			resultMap.put(STATUS, FAILURE);
			resultMap.put(MESSAGE, "Notification client id is null");
	return		new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		UserInfo uInfo = obtainUserInfo();
		if (uInfo == null) {
			resultMap.put(STATUS, FAILURE);
			resultMap.put(MESSAGE, "OAuth token does not a valid user");
			return	new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		pushNotificationClientService.registerPushNotification(uInfo, obtainTrustedSourceId(req),
				pushNoticationsClientReq);
		resultMap.put(STATUS, SUCCESS);
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}

	private String obtainTrustedSourceId(HttpServletRequest req) {
		String trustedSourceId = null;

		if (isApiManagerEnabled()) {
			trustedSourceId = (String) req.getAttribute("trustedSourceName");

		} else {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication instanceof OAuth2Authentication) {

				trustedSourceId = ((OAuth2Authentication) authentication).getOAuth2Request().getClientId();

			}
		}
		return trustedSourceId;
	}

	private UserInfo obtainUserInfo() {
		SecurityContext securityContext = SecurityContextHolder.getContext();

		if (securityContext != null && securityContext.getAuthentication() != null
				&& securityContext.getAuthentication().getPrincipal() instanceof UserInfo) {
			return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}
		return null;

	}

	@PostMapping(value = "/pushnotification/unregister")
	@ResponseBody
	public ResponseEntity<Map<String, String>> unregister(
			@RequestParam("notificationClientId") String notificationClientId) {
		Map<String, String> resultMap = new HashMap<>();
		if (notificationClientId == null) {
			resultMap.put(STATUS, FAILURE);
			resultMap.put(MESSAGE, "Notification client id is null");
			return		new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		PushNotificationClientDetail pushNotificationClientDtl = pushNotificationClientService
				.unregisterPushNotification(notificationClientId);
		if (pushNotificationClientDtl == null) {
			resultMap.put(STATUS, FAILURE);
			resultMap.put(MESSAGE, "Provided notification client id is not registered");
			return	new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		resultMap.put(STATUS, SUCCESS);
		return new ResponseEntity<>(resultMap, HttpStatus.OK);

	}

	private boolean isApiManagerEnabled() {
		if (isApiManagerEnabled == null) {
			String[] defaultProfiles = environment.getDefaultProfiles();
			String[] activeProfiles = environment.getActiveProfiles();

			// check if profile is api-manager-enabled then this filter is not required.
			if (defaultProfiles != null && defaultProfiles.length > 0
					&& (Arrays.asList(defaultProfiles).contains("api-manager-enabled"))
					|| (activeProfiles != null && activeProfiles.length > 0
							&& (Arrays.asList(activeProfiles).contains("api-manager-enabled")))) {
				isApiManagerEnabled = Boolean.TRUE;
			} else {
				isApiManagerEnabled = Boolean.FALSE;
			}
		}
		return isApiManagerEnabled;
	}

}
