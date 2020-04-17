package com.nucleus.license.utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.config.persisted.vo.ValueType;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.license.cache.BaseLicenseService;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.master.BaseMasterServiceImpl;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserService;

@Component(value = "licenseSetupUtil")
public class LicenseSetupUtil extends BaseServiceImpl {

	@Value("${core.web.config.license.setup.enabled}")
	private String licenseSetupEnabled;
	@Value("${core.web.config.license.setup.url}")
	private String licenseSetUpUrl;
	@Inject
	@Named("licenseClientCacheService")
	private   BaseLicenseService licenseClientCacheService;
	@Inject
	@Named("messageSource")
	protected MessageSource messageSource;
	@Value("${system.config.systemSetup.flag}")
	private String systemSetupPropertyConfig;
	@Inject
    @Named("userService")
    private UserService                 userService;

	private static final String DATE_FORMAT = "MM/dd/yyyy";
	private static Boolean isSystemSetupFlag = null;
	public static final String LICENSE_ALERT_BEFORE_EXPIRY = "LicenseAlertShowBeforeExpiry";
	public static final String LICENSE_ALERT_BEFORE_GRACE = "LicenseAlertShowBeforeGrace";
	public static final String LICENSE_ALERT_ON_THRESHOLD_NAMED_USER="licenseAlertOnThresholdNamedUserConsumption";
	public static final String LICENSE_ALERT_AFTER_MAX_NAMED_USER="licenseAlertOnMaxNamedUserConsumption";
	@Inject
	@Named("baseMasterService")
	private BaseMasterServiceImpl baseMasterService;
	@Inject
	@Named(value = "configurationService")
	private ConfigurationService configurationService;
	private String productCode = ProductInformationLoader.getProductCode();

	public boolean isSystemSetup() {

		if (Boolean.valueOf(licenseSetupEnabled)) {
			if (StringUtils.isEmpty(systemSetupPropertyConfig)) {
				throw new SystemException("System setup Property is not defined.");
			}
			if (isSystemSetupFlag == null || !isSystemSetupFlag) {
				Configuration configuration = configurationService.getConfigurationPropertyFor(
						SystemEntity.getSystemEntityId(), productCode + "." + systemSetupPropertyConfig);
				isSystemSetupFlag = false;
				if (configuration != null && StringUtils.isNotBlank(configuration.getPropertyValue())) {
					isSystemSetupFlag = Boolean.valueOf(configuration.getPropertyValue());
				} else {
					List<ConfigurationVO> configVOList = new ArrayList<ConfigurationVO>();
					ConfigurationVO configVO = new ConfigurationVO();
					configVO.setPropertyKey(productCode + "." + systemSetupPropertyConfig);
					configVO.setValueType(ValueType.BOOLEAN_VALUE);
					configVO.setConfigurable(false);
					configVO.setUserModifiable(false);
					configVOList.add(configVO);

					configurationService.syncConfiguration(EntityId.fromUri("com.nucleus.entity.SystemEntity:1"),
							configVOList);
				}
			}
			return isSystemSetupFlag;
		}
		return true;
	}
	public void checkNamedUserThresholdCunsumedMsg(HttpServletRequest request) {
		
		LicenseDetail licenseInformation = licenseClientCacheService.getCurrentProductLicenseDetail();
		if (licenseInformation == null) {
			return;
		}
		Integer thresholdLimit = 5;
		Configuration configuration = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
				"config.license.named.user.threshold.limit");

		if (configuration != null && StringUtils.isNotBlank(configuration.getPropertyValue())) {
			thresholdLimit = Integer.valueOf(configuration.getPropertyValue());
		}
		if (request.getSession().getAttribute(LICENSE_ALERT_ON_THRESHOLD_NAMED_USER) != null) {

			request.getSession().removeAttribute(LICENSE_ALERT_ON_THRESHOLD_NAMED_USER);
		}
		
		if (request.getSession().getAttribute(LICENSE_ALERT_AFTER_MAX_NAMED_USER) != null) {

			request.getSession().removeAttribute(LICENSE_ALERT_AFTER_MAX_NAMED_USER);
		}
		
		Integer maximumNumberOfUsersAllowed = licenseInformation.getMaxNamedUsers();
		if(maximumNumberOfUsersAllowed == -1)
			return;
		int userCreatedSoFar=userService.getUsersCountByProductName(ProductInformationLoader.getProductCode(),"null") ;
		int thresholdValue=maximumNumberOfUsersAllowed-userCreatedSoFar;
		if (thresholdValue<=0) {

			request.getSession().setAttribute(LICENSE_ALERT_AFTER_MAX_NAMED_USER,
					messageSource.getMessage("label.license.named.user.max.created",
							new String[] { String.valueOf(maximumNumberOfUsersAllowed),String.valueOf( userCreatedSoFar) } ,
							request.getLocale()));
		}
		else if (thresholdValue<=thresholdLimit) {
			request.getSession().setAttribute(LICENSE_ALERT_ON_THRESHOLD_NAMED_USER,
					messageSource.getMessage("label.license.named.user.on.threshold.limit",
							new String[] { String.valueOf(maximumNumberOfUsersAllowed),String.valueOf( userCreatedSoFar) } ,
							request.getLocale()));
			
		}
		

	}
	public String getLicenseSetUpUrl() {
		return licenseSetUpUrl;
	}

	public void updateSystemSetupFlagValue(boolean systemSetupFlag) {

		isSystemSetupFlag = systemSetupFlag;
	}

	public String getSystemSetupPropertyConfig() {
		return systemSetupPropertyConfig;
	}

	public void setSystemSetupPropertyConfig(String systemSetupPropertyConfig) {
		this.systemSetupPropertyConfig = systemSetupPropertyConfig;
	}

	public void checkLicenseExpiryMessage(HttpServletRequest request) {
		String dateFormat = baseMasterService.getUserPreferredDateFormat();
		LicenseDetail licenseInformation = licenseClientCacheService.getCurrentProductLicenseDetail();
		if (licenseInformation == null) {
			return;
		}

		DateTimeFormatter dtf = DateTimeFormat.forPattern(DATE_FORMAT);

		DateTime now = new DateTime();
		now = dtf.parseDateTime(now.toString(DATE_FORMAT));
		DateTime licenseExpiryDate = licenseInformation.getExpiryDate();
		licenseExpiryDate = dtf.parseDateTime(licenseExpiryDate.toString(DATE_FORMAT));

		Integer alertDays = 30;
		Configuration configuration = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
				"config.license.alert.days");

		if (configuration != null && StringUtils.isNotBlank(configuration.getPropertyValue())) {
			alertDays = Integer.valueOf(configuration.getPropertyValue());
		}
		if (request.getSession().getAttribute(LICENSE_ALERT_BEFORE_EXPIRY) != null) {

			request.getSession().removeAttribute(LICENSE_ALERT_BEFORE_EXPIRY);
		}
		if (request.getSession().getAttribute(LICENSE_ALERT_BEFORE_GRACE) != null) {
			request.getSession().removeAttribute(LICENSE_ALERT_BEFORE_GRACE);

		}
		if (licenseExpiryDate.minusDays(alertDays).isBefore(now)) {

			long daysLeftBeforeExpiry = (licenseExpiryDate.toDate().getTime() - now.toDate().getTime())
					/ (1000 * 60 * 60 * 24);
			if (daysLeftBeforeExpiry <= alertDays && daysLeftBeforeExpiry >= 0) {
				request.getSession().setAttribute(LICENSE_ALERT_BEFORE_EXPIRY,
						messageSource.getMessage("label.license.status.license.about.expiry",
								new String[] { licenseExpiryDate.toString(dateFormat) }, request.getLocale()));

			} else if (request.getSession().getAttribute(LICENSE_ALERT_BEFORE_EXPIRY) != null) {

				request.getSession().removeAttribute(LICENSE_ALERT_BEFORE_EXPIRY);
			}

		}
		if (licenseExpiryDate.isBefore(now) && licenseInformation.getGracePeriod() != null
				&& licenseExpiryDate.plusDays(licenseInformation.getGracePeriod() + 1).isAfter(now)) {

			Long daysAfterExpiry = (now.toDate().getTime() - licenseExpiryDate.toDate().getTime())
					/ (1000 * 60 * 60 * 24);
			Long daysOfGraceLeft = licenseInformation.getGracePeriod() - daysAfterExpiry;
			if (daysOfGraceLeft >= 0) {
				String graceExpiryDate = licenseExpiryDate.plusDays(licenseInformation.getGracePeriod())
						.toString(dateFormat);
				request.getSession().removeAttribute(LICENSE_ALERT_BEFORE_EXPIRY);
				request.getSession().setAttribute(LICENSE_ALERT_BEFORE_GRACE,
						messageSource.getMessage("label.license.status.license.about.grace.expiry",
								new String[] { licenseInformation.getGracePeriod().toString(), graceExpiryDate },
								request.getLocale()));
			} else if (request.getSession().getAttribute(LICENSE_ALERT_BEFORE_GRACE) != null) {
				request.getSession().removeAttribute(LICENSE_ALERT_BEFORE_EXPIRY);
				request.getSession().removeAttribute(LICENSE_ALERT_BEFORE_GRACE);

			}
		}

	}
	public boolean isLicenseExpired() {
		LicenseDetail licenseInformation = licenseClientCacheService.getCurrentProductLicenseDetail();
		if (licenseInformation == null) {
			return false;
		}

		DateTimeFormatter dtf = DateTimeFormat.forPattern(DATE_FORMAT);

		DateTime now = new DateTime();
		now = dtf.parseDateTime(now.toString(DATE_FORMAT));
		DateTime licenseExpiryDate = licenseInformation.getExpiryDate();
		if (licenseExpiryDate.isBefore(now)) {

			return true;
		}

		return false;
	}

}
