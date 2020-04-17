
package com.nucleus.security.oauth.constants;

public class TrustedSourceRegistrationConstant {
	
	public static final String MASTER_NAME = "OauthClientDetails";

	public static final String MASTER_ID = "masterID";
	public static final String OAUTH_CLIENT = "oauthClient";
	public static final String OAUTH_CLIENT_DETAILS = "oauthClientDetails";
	public static final String AUTHORIZED_GRANT_TYPES_LIST = "authorizedGrantTypesList";
	/**
	 * API Management Specific changes
	 * 
	 */
	public static final String ANONYMOUS="anonymous";
	public static final String ALLOWED_APIS_LIST = "allowedApisList";
	public static final String MAPPED_APIS_LIST = "mappedApisList";
	public static final String TRUSTED_USERS_MAP_LIST = "trustedUsersList";
	public static final String MAX_TRUSTED_SOURCES_LIMIT_EXCEEDED = "maxTrustedSourcesLimitExceeded";
	public static final String IP_ADDRESS_RANGE_LIST = "ipAddressRangeList";
	public static final String MAX_MAPPABLE_USERS= "maxMappableUsers";
	
	public static final String SCOPE_LIST = "scopeList";
	public static final String TRUSTED_SOURCE = "trustedSource";
	public static final String CLIENT_ID = "clientId";
	public static final String PASS_PHRASE = "Pass Phrase";
	public static final String CLIENT_SECRET = "Client Secret ";
	public static final String CLIENTID = "Client ID";

	public static final String MESSAGE_EXCEPTION = "Message Exception Ocurred";
	public static final String FROM_ADDRESS = "config.communication.from.address";
	public static final String IO_EXCEPTION = "IO Exception Occurred";
	
	public static final String GRANT_TYPE_FEDERATED = "federated";
	
	TrustedSourceRegistrationConstant() {

	}
}
