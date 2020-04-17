package com.nucleus.web.security.oauth;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nucleus.password.reset.ResetPasswordService;
import com.nucleus.security.oauth.constants.RESTfulSecurityConstants;
import com.nucleus.user.UserSessionManagerService;

@Configuration
@PropertySource("classpath:neutrino-product-suite-config/common-config/core-framework-config/oauth-security-config.properties")
@RestController
@RequestMapping("/restservice")
public class RestLogoutController {
	
	@Autowired
	private Environment env;
	
	//Conditional bean that might be null if API portal is enabled.
	@Autowired(required = false)
	private TokenStore tokenStore;
	
	@Inject
	@Named("resetPasswordService")
	private ResetPasswordService resetPasswordService;
	
	@Inject
	@Named("userSessionManagerService")
	private UserSessionManagerService userSessionManagerService;

	@RequestMapping(value = "/logout")
	public void getLogoutPage(HttpServletResponse response, @RequestParam("access_token") String accessTokenValue) {
		response.setStatus(resetPasswordService.invalidateAccessToken(accessTokenValue));
	}

	@RequestMapping(value = "/ping")
	public void updateToken(HttpServletResponse response, @RequestParam("access_token") String accessTokenValue) {
		boolean pingSuccessfull = false;
		if (SecurityContextHolder.getContext() != null
				&& SecurityContextHolder.getContext().getAuthentication() != null) {
			Object principal = SecurityContextHolder.getContext().getAuthentication();
			if (accessTokenValue != null && !accessTokenValue.isEmpty() && tokenStore != null) {
				DefaultOAuth2AccessToken accessToken = (DefaultOAuth2AccessToken) tokenStore
						.readAccessToken(accessTokenValue);
				if (accessToken != null) {
					accessToken.setExpiration(new Date(System.currentTimeMillis()
							+ (Long.parseLong(env.getProperty(RESTfulSecurityConstants.ACCESS_TOKEN_VALIDITY_SECONDS)))
									* 1000L));
					if (principal instanceof OAuth2Authentication)
						tokenStore.storeAccessToken((OAuth2AccessToken) accessToken, (OAuth2Authentication) principal);
					response.setStatus(HttpStatus.OK.value());
					pingSuccessfull = true;
				}
			}
			if (!pingSuccessfull) {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
			}
		}
	}
}