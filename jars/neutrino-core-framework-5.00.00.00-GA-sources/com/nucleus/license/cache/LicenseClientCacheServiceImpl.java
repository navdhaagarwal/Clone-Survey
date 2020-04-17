package com.nucleus.license.cache;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.user.UserService;

@Named("licenseClientCacheService")
public class LicenseClientCacheServiceImpl implements LicenseClientCacheService {
	@Inject
	@Named("licenseClientCachePopulator")
	public LicenseClientCachePopulator licenseClientCachePopulator;
	@Inject
	@Named("userService")
	UserService userService;
	@Override
	public LicenseDetail get(String key) {
		Object obj = licenseClientCachePopulator.get(key);
		return notNull(obj) ? (LicenseDetail) obj : null;
	}

	@Override
	public Map<String, LicenseDetail> getAll() {

		return licenseClientCachePopulator.getAll();
	}

	@Override
	public void update(Action action, Map<String, LicenseDetail> object) {
		licenseClientCachePopulator.update(action, object);

	}

	@Override
	public LicenseDetail getCurrentProductLicenseDetail() {
		return get(ProductInformationLoader.getProductCode());
	}
	private List<String> getLicensedModuleCodeListFromMap(Map<String, LicenseDetail> codeLicenseDtlMap)
	{
		ArrayList<String> licensedModuleCodeList=new ArrayList<>();
		if(codeLicenseDtlMap!=null)
		{
			licensedModuleCodeList=	new ArrayList<>(codeLicenseDtlMap.keySet());
		}
		return licensedModuleCodeList;
	}

	@Override
	public List<String> getLicensedModuleList() {
		
		List<String> licensedModuleList= getLicensedModuleCodeListFromMap(getAll());
	
		List<Long> userRoleIDs = new ArrayList(userService.getCurrentUser().getUserRoleIds());
				
		List<String> productAssociatedWithUser = userService.getProductListFromRoleIds(userRoleIDs);
		licensedModuleList.retainAll(productAssociatedWithUser); 
		return licensedModuleList;
		
	}
	

}
