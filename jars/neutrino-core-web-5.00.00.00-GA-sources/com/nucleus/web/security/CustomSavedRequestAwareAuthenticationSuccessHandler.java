/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.security;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nucleus.core.genericparameter.service.GenericParameterServiceImpl;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.menu.MenuEntity;
import com.nucleus.rules.model.SourceProduct;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.nucleus.core.web.conversation.ConversationalSessionAttributeStore;
import com.nucleus.license.utils.LicenseSetupUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.standard.context.NeutrinoExecutionContextInitializationHelper;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;

/**
 * An authentication success strategy which can make use of the
 * {@link DefaultSavedRequest} which may have been stored in the session by the
 * {@link ExceptionTranslationFilter}. When such a request is intercepted and
 * requires authentication, the request data is stored to record the original
 * destination before the authentication process commenced, and to allow the
 * request to be reconstructed when a redirect to the determined redirect URL
 * occurs. This class is responsible for performing the redirect to the redirect
 * URL if appropriate.
 * <p>
 * Following a successful authentication, it decides on the redirect
 * destination, based on the following scenarios:
 * <ul>
 * <li>
 * If the {@code alwaysUseDefaultTargetUrl} property is set to true, the
 * {@code defaultTargetUrl} will be used for the destination. Any
 * {@code DefaultSavedRequest} stored in the session will be removed.</li>
 * <li>
 * If the {@code targetUrlParameter} has been set on the request, the value will
 * be used as the destination. Any {@code DefaultSavedRequest} will again be
 * removed.</li>
 * <li>
 * If a {@link SavedRequest} is found in the {@code RequestCache} (as set by the
 * {@link ExceptionTranslationFilter} to record the original destination before
 * the authentication process commenced), a redirect will be performed to the
 * {@code defaultTargetUrl} if {@code targetUrlParameter} request parameter
 * value is absent in the original request. Any {@code DefaultSavedRequest} will
 * again be removed.</li>
 * <li>
 * If no {@code SavedRequest} is found, it will delegate to the base class.</li>
 * </ul>
 * 
 * @author Nucleus Software Exports Limited
 */
public class CustomSavedRequestAwareAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	@Inject
	@Named("neutrinoExecutionContextInitializationHelper")
	NeutrinoExecutionContextInitializationHelper neutrinoExecutionContextInitializationHelper;
	private RequestCache    requestCache = new HttpSessionRequestCache();

	private SavedRequest    savedRequest = null;
	  @Autowired
	    private LicenseSetupUtil             licenseSetupUtil;
	@Inject
	@Named(value = "systemSetupUtil")
	private SystemSetupUtil systemSetupUtil;

	@Inject
	@Named(value = "userService")
	UserService userService;

	@Inject
	@Named("userSessionManagerService")
	private UserSessionManagerService userSessionManagerService;

	@Inject
	@Named("genericParameterService")
	private GenericParameterServiceImpl genericParameterService;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		savedRequest = requestCache.getRequest(request, response);

		if (savedRequest != null) {
			requestCache.removeRequest(request, response);
		}
		licenseSetupUtil.checkLicenseExpiryMessage(request);
		licenseSetupUtil.checkNamedUserThresholdCunsumedMsg( request);
		super.onAuthenticationSuccess(request, response, authentication);
		return;
	}
	

	/**
	 * Builds the target URL according to the logic defined below: If the
	 * {@code alwaysUseDefaultTargetUrl} property is set to true, the
	 * {@code defaultTargetUrl} will be used for the destination otherwise if
	 * the {@link SavedRequest} contains the {@code targetUrlParameter} then it
	 * will be used for the destination. In all other cases the
	 * {@code defaultTargetUrl} will be used.
	 */
	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {

		String targetURL = doDetermineTargetUrl(request);

		// start conversation from first page itself.
		String firstConversationalId = RandomStringUtils.randomAlphanumeric(15);
		request.getSession().setAttribute("initialConversationalId", firstConversationalId); //reverted as this was removed unintentionally in PDDEV-13168  
		targetURL = UriComponentsBuilder.fromPath(targetURL)
				.queryParam(ConversationalSessionAttributeStore.CID_FIELD, firstConversationalId).build().toUriString();

		return targetURL;
	}

	private String doDetermineTargetUrl(HttpServletRequest request) {

		UserInfo userInfo=(UserInfo)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Boolean forceReset = userService.getForceResetPassForUserId(userInfo.getId());
		if(forceReset){
			User user = userService.findUserByUsername(userInfo.getUsername());
			if(user != null && !"db".equals(user.getSourceSystem())){
				forceReset = false;
			}
		}
		if (forceReset) {
			userSessionManagerService.invalidateCurrentLoggedinUserSession();
			if (systemSetupUtil.isSystemDeployedOnCloud()) {
				return systemSetupUtil.getLicenseAgreementUrl() + "/" + userInfo.getUsername();
			} else {
				return systemSetupUtil.getResetPasswordUrl() + "/" + userInfo.getUsername();
			}
		}

		String defaultTargetUrl = systemSetupUtil.getAuthenticationSuccessUrl(request);
		setDefaultTargetUrl(defaultTargetUrl);
		String targetUrlParameter = getTargetUrlParameter();

		if (isAlwaysUseDefaultTargetUrl()) {
			BaseLoggers.securityLogger.debug("alwaysUseDefaultTargetUrl is true. Using default Url: " + defaultTargetUrl);
			return defaultTargetUrl;
		}

		//Check whether user specific default url has been mapped
		SourceProduct sourceProduct=genericParameterService.findByCode(ProductInformationLoader.getProductName(),SourceProduct.class);
		MenuEntity menuEntity=userService.getMappedDefaultMenu(userInfo.getId(),sourceProduct!=null?sourceProduct.getId():null);
		if(menuEntity!=null){
			return userService.returnLandingUrlFromMenu(menuEntity,request);
		}



		// Check for the parameter and use that if available
		String targetUrl = null;
		if (systemSetupUtil.isSystemSetup()) {
			if (targetUrlParameter != null && savedRequest != null) {
				Map<String, String[]> parameterMap = savedRequest.getParameterMap();
				if (parameterMap.size() > 0) {
					String[] parameterValues = parameterMap.get(targetUrlParameter);
					if (parameterValues != null && parameterValues.length > 0) {
						targetUrl = parameterValues[0];
					}
				}

				if (StringUtils.hasText(targetUrl)) {
					if (!targetUrl.startsWith("/")) {
						targetUrl = "/" + targetUrl;
					}
					BaseLoggers.securityLogger.debug("Found targetUrlParameter in request. Using Url: " + targetUrl);
					return targetUrl;
				}
			}
		} else {
			targetUrl = defaultTargetUrl;
		}
		if(savedRequest != null && savedRequest.toString().contains("ExternalCAMReport")){
			targetUrl="/app/ExternalCAMReport/loadJreport";
		}
		if (!StringUtils.hasText(targetUrl)) {
			targetUrl = defaultTargetUrl;
		}
		BaseLoggers.securityLogger.debug("Using default Url: " + targetUrl);

		return targetUrl;

	}

	public void setRequestCache(RequestCache requestCache) {
		this.requestCache = requestCache;
	}

	
}
