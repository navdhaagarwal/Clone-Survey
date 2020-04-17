package com.nucleus.web.trustedsource;

import java.util.List;

import com.nucleus.license.pojo.LicenseMobilityModuleInfo;
import com.nucleus.security.oauth.apim.APIDetails;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.user.User;

public interface TrustedSourceRegistrationService {

	public List<OauthClientDetails> getAllActiveInternalTrustedSource();
	void	deActivateInternalTrustedSource();

	void registerInternalTrustedSourceFromLicense(List<LicenseMobilityModuleInfo> mobilityModuleInfoList,
			String emailId, List<APIDetails> apiDetailsList, User user);
}
