package com.nucleus.license.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.license.service.LicenseClientService;
import com.nucleus.logging.BaseLoggers;

/**
 * @author neha.garg1
 *
 */
@Named("licenseClientCachePopulator")
public class LicenseClientCachePopulator extends FWCachePopulator {
	@Inject
	@Named("licenseClientService")
	private LicenseClientService licenseClientService;

	private Map<String, LicenseDetail> localProductCodeLicenseDetailMap = new ConcurrentHashMap<>();
	private Map<String, Long> localLastUpdatedTimeStampMap = new ConcurrentHashMap<>();

	@Override
	public String getNeutrinoCacheName() {

		return FWCacheConstants.LICENSE_DETAIL_CACHE;
	}

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : LicenseClientCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		return getCurrentTimeStamp();
	}

	private Long getCurrentTimeStamp() {

		return new DateTime().getMillis();
	}

	@Override
	public void build(Long tenantId) {
		Map<String, LicenseDetail> map = licenseClientService.getLicenseFromAppliedLicenses();
		if (map != null) {
			for (Entry<String, LicenseDetail> entry : map.entrySet()) {
				put(entry.getKey(), getCurrentTimeStamp());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Action action, Object object) {
		
		Map<String, LicenseDetail> map = (Map<String, LicenseDetail>) object;
		if (ValidatorUtils.notNull(map)) {
			for (Entry<String, LicenseDetail> entry : map.entrySet()) {

				update(action, entry.getValue(), entry.getKey(), getCurrentTimeStamp(), true);
			}
		}
		BaseLoggers.flowLogger.debug("Update Called : ScriptRuleEvaluatorCachePopulator.");

	}

	private void updateLicenseDetailMaps(Long lastUpdateTimeStamp, String productCode, LicenseDetail licenseDetail,
			boolean isDistributedCachePutRequired) {

		localLastUpdatedTimeStampMap.put(productCode, lastUpdateTimeStamp);
		localProductCodeLicenseDetailMap.put(productCode, licenseDetail);
		if (isDistributedCachePutRequired) {
			put(productCode, lastUpdateTimeStamp); // To put time stamp in distributed cache.
		}

	}

	private void update(Action action, LicenseDetail licenseDetail, String productCode, Long lastUpdateTimeStamp,
			boolean isDistributedCachePutRequired) {

		if (!action.equals(Action.DELETE) && ValidatorUtils.notNull(licenseDetail)) {
			if (isDistributedCachePutRequired) {
				lastUpdateTimeStamp = getCurrentTimeStamp();
			}
			updateLicenseDetailMaps(lastUpdateTimeStamp, productCode, licenseDetail, isDistributedCachePutRequired);
		}
	}

	@Override
	public Object get(Object key) {
		String code = (String) key;
		Long distributedTimeStamp = (Long) super.get(code);
		Long localTimeStamp = localLastUpdatedTimeStampMap.get(code);
		if (localTimeStamp == null || !distributedTimeStamp.equals(localTimeStamp)) {
			update(Action.INSERT, licenseClientService.getLicenseDetail((String) key), code, distributedTimeStamp,
					false);
		}
		return localProductCodeLicenseDetailMap.get(key);
	}

	public Map<String, LicenseDetail> getAll() {
		Map<String, LicenseDetail> licenseDetailMap = new HashMap<>();
		for (Entry<?, ?> obj : super.entrySet()) {

			String code = (String) obj.getKey();
			Long distributedTimeStamp = (Long) super.get(code);
			Long localTimeStamp = localLastUpdatedTimeStampMap.get(code);
			if (localTimeStamp == null || !distributedTimeStamp.equals(localTimeStamp)) {
				
				update(Action.INSERT, licenseClientService.getLicenseDetail(code), code, distributedTimeStamp, false);
			}
			LicenseDetail licenseDtl=localProductCodeLicenseDetailMap.get(code);
			if (ValidatorUtils.notNull(licenseDtl)) {
				licenseDetailMap.put(code, licenseDtl);
			}
		}
		return licenseDetailMap;
	}
}
