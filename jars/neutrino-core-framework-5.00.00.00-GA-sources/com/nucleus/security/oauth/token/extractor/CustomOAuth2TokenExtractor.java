package com.nucleus.security.oauth.token.extractor;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.nucleus.logging.BaseLoggers;

/**
 * {@link TokenExtractor} that strips the authenticator from header (access_token)
 *  <code><TOKEN></code>", or as a request parameter if that fails). The access token is the principal in
 * the authentication token that is extracted.
 * 
 * @author rohit.singh
 * 
 */
public class CustomOAuth2TokenExtractor implements TokenExtractor{
	
	
	@Override
	public Authentication extract(HttpServletRequest request) {
		String tokenValue = extractToken(request);
		if (tokenValue != null) {
			return  new PreAuthenticatedAuthenticationToken(tokenValue, "");
		}
		return null;
	}

	protected String extractToken(HttpServletRequest request) {
		// first check the header...
		String token = extractHeaderToken(request);

		// bearer type allows a request parameter as well
		if (token == null) {
			BaseLoggers.flowLogger.debug("Token not found in headers. Trying request parameters.");
			token = request.getParameter(OAuth2AccessToken.ACCESS_TOKEN);
			if (token == null) {
				BaseLoggers.flowLogger.debug("Token not found in request parameters.  Not an OAuth2 request.");
			}
			else {
				request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE, OAuth2AccessToken.BEARER_TYPE);
			}
		}

		return token;
	}

	/**
	 * This is custom implementation for Neo.
	 * Get the OAuth access_token  from a header.
	 * 
	 * @param request The request.
	 * @return The token, or null if no OAuth authorization header was supplied.
	 */
	protected String extractHeaderToken(HttpServletRequest request) {
		return request.getHeader(OAuth2AccessToken.ACCESS_TOKEN);
	}
	
	
}
