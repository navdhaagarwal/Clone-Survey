package com.nucleus.web.trustedsource;

import com.nucleus.security.oauth.domainobject.OauthClientDetails;

interface TrustedSourceSecretService {
	public void setDefaultData(OauthClientDetails trustedSource);
	public void editClientSecret(OauthClientDetails trustedSource);
	public void persistTrustedSource(OauthClientDetails trustedSource);
}
