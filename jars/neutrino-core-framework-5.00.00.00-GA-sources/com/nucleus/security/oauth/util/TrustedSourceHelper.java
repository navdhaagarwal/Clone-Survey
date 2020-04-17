package com.nucleus.security.oauth.util;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.util.Assert;

import com.nucleus.license.cache.LicenseClientCacheService;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.license.pojo.LicenseMobilityModuleInfo;

@Named("trustedSourceHelper")
public class TrustedSourceHelper {
	@Inject
	@Named("licenseClientCacheService")
	private LicenseClientCacheService licenseClientCacheService;
	
	public static final List<String> INTERNAL_MODULE_LIST = Collections
			.unmodifiableList(Arrays.asList("mCAS","mApply","eApply","mFin","mCollect","neoCollectionsClient","casapp","lmsapp","commonapp","geoapp","ecmapp","eServe","mServe","neoSalesAssist","cmsapp","scannerapp","eccapp"));

	public static boolean isInternalModule(String clientId) {

		for (String module : INTERNAL_MODULE_LIST) {
			if (module.equalsIgnoreCase(clientId)) {
				return true;
			}
		}

		return false;
	}

	public boolean isLicensedModule(String clientId) {
		   Assert.notNull(clientId, "client_id cannot be null");
		boolean isLicensedModule = false;
		Map<String, LicenseDetail> licenseMap = licenseClientCacheService.getAll();
		if (licenseMap == null) {
			return isLicensedModule;
		}
		for (Map.Entry<String, LicenseDetail> entry : licenseMap.entrySet()) {
			List<LicenseMobilityModuleInfo> mobilityInfoList = entry.getValue().getLicenseMobilityModuleInfoList();
			if (notNull(mobilityInfoList)) {

				for (LicenseMobilityModuleInfo moblityInfo : mobilityInfoList) {
					if (clientId.equals(moblityInfo.getMobilityModuleCode())) {
						isLicensedModule = true;

						break;
					}
				}

			}
		}

		return isLicensedModule;
	}
}
