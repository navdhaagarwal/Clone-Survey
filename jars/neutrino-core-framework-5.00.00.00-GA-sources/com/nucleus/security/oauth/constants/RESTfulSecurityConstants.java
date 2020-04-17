
package com.nucleus.security.oauth.constants;

public class RESTfulSecurityConstants {
	
	public static final String OAUTH_TOKEN_REQUISITION_URL  ="oauth_token_requisition_url";
	public static final String EXCEPTION_MSG = "Exception occured while getting token.";
	public static final String GET_ACCESS_TOKEN_ERROR_MSG = "Could not get access token OauthTokenDetails. Error for Client Id : ";
	public static final String GET_ACCESS_TOKEN_REFRESHED_MSG = "Could not get access token refreshed for existing OauthTokenDetails. Error for Client Id : ";
	public static final String CLIENT_ID_NOT_CONFIGURED_MSG = "No OauthConfig property is configured for the clientId: ";
	public static final String GET_LOGGED_IN_USERS_TRUSTED_SOURCE_ERROR_MSG = "Could not get logged in trsuted source users. Error for Client Id : ";
	public static final String REVOKE_TOKENS_BY_USER_ERROR_MSG = "Could not revoked users access token. Error for Client Id : ";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String GRANT_TYPE = "grant_type";
	public static final String PASS_WORD = "password";
	public static final String ANONYMOUS = "anonymous";
	public static final String USERNAME = "username";
	public static final String CLIENT_ID = "client_id";
	public static final String IS_ANONYMOUS_ENABLED = "isAnonymousEnabled";
	public static final String CLIENT_SECRET = "client_secret";
	public static final String EXPIRY_TIME = "expires_in";
	public static final String SCOPE = "scope";
	public static final String DOT = ".";
	public static final String SECRET = "secret";
	public static final String ERROR_DESCRIPTION="error_description";
	public static final String INVALID_REFRESH_TOKEN="Invalid refresh token";
	public static final String ACCESS_TOKEN_VALIDITY_SECONDS="accessTokenValiditySeconds";
	public static final  int NETWORK_LATENCY=1;
	public static final String CLIENT_CREDENTIALS = "client_credentials";
	public static final String AUTHORIZATION_CODE = "authorization_code";
	public static final String REDIRECT_URI = "redirect_uri";
	public static final String CODE = "code";
	public static final String LOGGED_IN_MODULES = "logged_in_modules";
	
	RESTfulSecurityConstants() {
		
	}
}
