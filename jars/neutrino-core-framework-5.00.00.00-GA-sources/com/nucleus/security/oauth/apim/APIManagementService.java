package com.nucleus.security.oauth.apim;

import java.util.List;

import com.nucleus.license.pojo.LicenseApiDetailsVO;

public interface APIManagementService{
	
	public List<APIDetails> getAPIDetailsFromCodes(List<String> codes);
	public List<APIDetails> getAllFunctioningAPIs();
	public List<APIDetails> getAPIDetailsFromLicense(List<LicenseApiDetailsVO> apisFromlicense);
}
