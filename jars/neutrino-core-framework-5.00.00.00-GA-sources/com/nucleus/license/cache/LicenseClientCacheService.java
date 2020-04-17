package com.nucleus.license.cache;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.license.content.model.LicenseDetail;

public interface LicenseClientCacheService extends BaseLicenseService{
	
	public Map<String, LicenseDetail> getAll();

	public void update(Action action, Map<String, LicenseDetail> object) ;
	public List<String> getLicensedModuleList() ;
}
