package com.nucleus.web.common;

import java.math.BigDecimal;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.nucleus.address.Country;
import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.contact.PhoneNumberType;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.money.utils.MoneyUtils;
import com.nucleus.core.mutitenancy.service.MultiTenantService;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.csrf.CSRFTokenManager;
import com.nucleus.web.security.URLBuilderHelper;
import com.nucleus.web.userpreferences.PreferenceFormBean;

import flexjson.JSONSerializer;

@Named("commonConfigUtility")
public class CommonConfigUtility {

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    @Inject
    @Named("configurationService")
    private ConfigurationService        configurationService;
    
    @Value(value = "${core.web.config.commonsMultipartResolver.maxUploadSize.value}")
    private String  maxPermittedNoteSize;
    
    @Value(value="${core.web.config.commonsMultipartResolver.maxUserProfileImgSize}")
    private String maxPermittedUserProfileImageSize;
    
    @Value(value = "${noteController.supportedFileTypes}")
    private String[]  supportedFileTypes;
    
    private String  systemDefaultlocale;
    
    @Value(value = "#{'${phone.code.for.regions}'}")
    private String   phoneCountryCodes;
    
    @Value(value = "#{'${core.web.config.default.target.url}'}")
    private String   defaultTargetUrl;
    
    @Value(value = "#{'${core.web.config.default.target.url}'}")
    private String   defaultTargetUrlWithhkstd;

	@Value(value = "#{'${core.web.config.SSO.login.url.value}'}")
    private String   ssoLoginUrl;
    
    private boolean isSsoActive;
    
    @Value(value = "#{'${core.web.config.logout.perform.url}'}")
    private String	logoutURL;
    
    @Value(value = "#{'${core.web.config.SSO.logout.perform.url.value}'}")
    private String	ssoLogoutURL;

    @Value(value = "#{'${cas.addresstag.phone.country.flag}'}")
    private String  phoneBasedOnCountry;
    
	private String defaultDateMinimumYear;
    
    private static final String ANONYMOUS_USER="anonymousUser";
    
    @Value(value = "#{'${core.web.config.SSO.ticketvalidator.url.value}'}")
    private String   ssoTicketValidatorUrl;

    @Value("${core.web.config.session.failover.url:/app/dashboard?errCode=ERR.SESSIONFAILOVER.MSG}")
    private String sessionFailoverBaseUrl;
    
    @Value("${core.web.config.SSO.logout.url.value}")
    private String ssoLogoutRedirectUri;
    
    private String appServer_IpAddress;
    
   
    @Inject
    @Named("userService")
    private UserService userService;
    
    @Inject
    @Named("genericParameterService")
    private GenericParameterService        genericParameterService;
    
    @Inject
    @Named("multiTenantService")
    private MultiTenantService multiTenantService;
    
    private String phoneInitializerData = null;
    private boolean sanitizingEnabled ;
    
    @Inject
	@Named("phoneTagDataCachePopulator")
	private NeutrinoCachePopulator phoneTagDataCachePopulator;

    public final static String GETPHONETAGDATA="GETPHONETAGDATA";
    
    public final static String GETPHONETAGINITIALIZERDATA="GETPHONETAGINITIALIZERDATA";

    public final static String GETCOUNTRYCODEFROMCOUNTRYMASTER="GETCOUNTRYCODEFROMCOUNTRYMASTER";
    
    public final static String GETCOUNTRYCODEALPHA2ALPHA3MAP="GETCOUNTRYCODEALPHA2ALPHA3MAP";
	
	@Value("${allowed.alphacharset.range}")
	private String  allowedAlphaCharSet;
    
    public String getAllowedAlphaCharSet() {
		return allowedAlphaCharSet;
	}
    
	public void setAllowedAlphaCharSet(String allowedAlphaCharSet) {
		this.allowedAlphaCharSet = allowedAlphaCharSet;
	}

	public boolean isSanitizingEnabled() {
		return sanitizingEnabled;
	}

	public void setSanitizingEnabled(boolean sanitizingEnabled) {
		this.sanitizingEnabled = sanitizingEnabled;
	}

	public String getSystemDateInUserPreferredFormat() {
        String currentServerDate = null;
        String dateFormat = null;
        Map<String, ConfigurationVO> userPreferences;
        UserInfo userInfo;

        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
	    	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	        if (principal != null && !ANONYMOUS_USER.equals(principal.toString()) && (principal instanceof UserInfo) ) {
	        	userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	        	userPreferences = userInfo.getUserPreferences();
	   			dateFormat = userPreferences.get(Configuration.DATE_FORMATS).getText();
	        } else {
	            ConfigurationVO dateFormatConfiguration = configurationService.getConfigurationPropertyFor(
	                    SystemEntity.getSystemEntityId(), Configuration.DATE_FORMATS);
	            if (dateFormatConfiguration != null && dateFormatConfiguration.getPropertyValue() != null) {
	            	dateFormat = dateFormatConfiguration.getPropertyValue();
	            }
	        }
	   		DateTime serverDate = new DateTime();
	   		DateTimeFormatter format = DateTimeFormat.forPattern(dateFormat);
	    	currentServerDate = format.print(serverDate);
        } 
    	return currentServerDate;
    }

    public void setMaxPermittedNoteSize(String maxPermittedNoteSize) {
        this.maxPermittedNoteSize = maxPermittedNoteSize;
    }

    public String getPreferredDecimalSeparator() {
    	return MoneyUtils.getDecimalSeparatorFromLocale(new Locale (getUserLocale()));
	}
    
    public String getPreferredGroupingSeparator() {
      
        return MoneyUtils.getGroupingSeparatorFromLocale(new Locale(getUserLocale()));
    }
    
    public String getMaxPermittedFileSizeForUpload(){
    	return maxPermittedNoteSize;
    }

    
    public String getMaxPermittedFileSizeForUploadLabel(){
        double convertStringToDouble, temp;
        convertStringToDouble = Double.parseDouble(maxPermittedNoteSize);
        temp = convertStringToDouble/(1024*1024);
        BigDecimal labelValue = new BigDecimal(temp);
        labelValue = labelValue.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return String.valueOf(labelValue);
    }
    
    public String getSupportedFileTypes(){
        JSONSerializer iSerializer = new JSONSerializer();
    	return  iSerializer.serialize(supportedFileTypes);
    }

    public String getDefaultCountryISOCode(){
        if(ValidatorUtils.notNull(getSystemDefaultlocale())){
            String[] splitLocaleString = getSystemDefaultlocale().split("[_-]");
            String language = splitLocaleString[0];
            String countryCode = splitLocaleString[1];
            Locale loc = new Locale(language,countryCode);
            return loc.getISO3Country();
        }
        return null;

    }

    public String getSystemDefaultlocale() {
    	if(systemDefaultlocale==null) {
    		systemDefaultlocale=multiTenantService.getDefaultTenant().getLocale();
    	}
		return systemDefaultlocale;
	}

	public String getCountryCodeFromCountryMaster(){
    	return (String) phoneTagDataCachePopulator.get(GETCOUNTRYCODEFROMCOUNTRYMASTER);
    }
    
    
    public String getUpdatedCountryCodeFromCountryMaster(){
        JSONSerializer iSerializer = new JSONSerializer();
        Map<String, String> countryISOCodeMap = new HashMap<String, String>();
        String language = "en";
        List<Locale> locales = new ArrayList<>();
        String[] isoCountries = Locale.getISOCountries();
        for(String isoCounty : isoCountries) {
            locales.add(new Locale(language, isoCounty));
        }

        List<String> countryIsoCodes= getAllCountryCodes();

        for(Locale locale : locales) {
            for(String l : countryIsoCodes) {
                if(locale.getISO3Country().equalsIgnoreCase(l)) {
                    countryISOCodeMap.put(locale.getISO3Country(), locale.getCountry());                }
            }

        }


        return iSerializer.serialize(countryISOCodeMap);
    }

    public String getCountryCodeAlpha2Alpha3Map(){
    	return (String) phoneTagDataCachePopulator.get(GETCOUNTRYCODEALPHA2ALPHA3MAP);
    }
    
    
    public String getUpdatedCountryCodeAlpha2Alpha3Map(){
        JSONSerializer iSerializer = new JSONSerializer();
        Map<String, String> countryISOCodeMap = new HashMap<String, String>();
        String language = "en";
        String[] regionCodes = phoneCountryCodes.split(",");

        if (ValidatorUtils.notNull(regionCodes)) {
            for (String region : regionCodes) {
                if (StringUtils.isNotEmpty(region)) {
                    Locale loc = new Locale(language, region);
                    countryISOCodeMap.put(loc.getISO3Country(), loc.getCountry());
                }
            }
        }

        return iSerializer.serialize(countryISOCodeMap);
    }



    public String getPhoneTagInitializerData(){
    	String userCountryCode=userService.getUserLocale().getCountry();
    	return (String) phoneTagDataCachePopulator.get(new StringBuilder(GETPHONETAGINITIALIZERDATA).append(FWCacheConstants.KEY_DELIMITER)
				.append(userCountryCode).toString());
    }

    public String getUpdatedPhoneTagInitializerData(String userCountryCode)
    {


        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        SortedMap<String, Object> numberMap = new TreeMap<String, Object>();
        String language = "en";
        List<Locale> locales = new ArrayList<>();
        String[] isoCountries = Locale.getISOCountries();
        for(String isoCounty : isoCountries) {
            locales.add(new Locale(language, isoCounty));
        }

        List<String> countryIsoCodes=getAllCountryCodes();



        for(Locale locale : locales) {
            for (String l : countryIsoCodes) {
                if (locale.getISO3Country().equalsIgnoreCase(l)) {
                    numberMap.put(locale.getCountry(), "" + phoneUtil.getCountryCodeForRegion(locale.getCountry()));
                }

            }
        }
        Map<String, String> otherMap = new HashMap<String, String>();
        JSONSerializer iSerializer = new JSONSerializer();
        // countryCode is the code of current user
        otherMap.put("countryCode", userCountryCode);

        otherMap.put("isMobileNumber", String.valueOf(genericParameterService.findByCode(PhoneNumberType.MOBILE_NUMBER,
                PhoneNumberType.class).getId()));
        otherMap.put("isLandlineNumber", String.valueOf(genericParameterService.findByCode(PhoneNumberType.LANDLINE_NUMBER,
                PhoneNumberType.class).getId()));

        numberMap.put("otherMap", otherMap);

        String pushData = prepareCountryOptions(numberMap);

        numberMap.put("pushData", pushData);

        phoneInitializerData = iSerializer.serialize(numberMap);

        return phoneInitializerData;

    
    }
    
    public String getPhoneTagData(){
    	String userCountryCode=userService.getUserLocale().getCountry();
    	return (String) phoneTagDataCachePopulator.get(new StringBuilder(GETPHONETAGDATA).append(FWCacheConstants.KEY_DELIMITER)
				.append(userCountryCode).toString());    
    }

    public String getUpdatedPhoneTagData(String userCountryCode)
    {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        SortedMap<String, Object> numberMap = new TreeMap<String, Object>();
        String language = "en";
        if(phoneBasedOnCountry.equals("true")){
            List<Locale> locales = new ArrayList<>();
            String[] isoCountries = Locale.getISOCountries();
            for(String isoCounty : isoCountries) {
                locales.add(new Locale(language, isoCounty));
            }

            List<String> countryIsoCodes=getAllCountryCodes();
            for(Locale locale : locales) {
                for (String l : countryIsoCodes) {
                    if (locale.getISO3Country().equalsIgnoreCase(l)) {
                        numberMap.put(locale.getCountry(), "" + phoneUtil.getCountryCodeForRegion(locale.getCountry()));
                    }

                }
            }
        }
        else {

            String[] countryCodes = prepareCountryCodes();
            if (countryCodes != null) {
                for (String countrySymbol : countryCodes) {
                    if (countrySymbol != null && !countrySymbol.equals("")) {
                        Locale loc = new Locale(language, countrySymbol);
                        String countryName = loc.getCountry();
                        numberMap.put(countryName, "" + phoneUtil.getCountryCodeForRegion(countrySymbol));
                    }
                }
            }
        }
        Map<String, String> otherMap = new HashMap<String, String>();
        JSONSerializer iSerializer = new JSONSerializer();
        // countryCode is the code of current user
        otherMap.put("countryCode", userCountryCode);

        otherMap.put("isMobileNumber", String.valueOf(genericParameterService.findByCode(PhoneNumberType.MOBILE_NUMBER,
                PhoneNumberType.class).getId()));
        otherMap.put("isLandlineNumber", String.valueOf(genericParameterService.findByCode(PhoneNumberType.LANDLINE_NUMBER,
                PhoneNumberType.class).getId()));

        numberMap.put("otherMap", otherMap);

        String pushData = prepareCountryOptions(numberMap);

        numberMap.put("pushData", pushData);

        phoneInitializerData = iSerializer.serialize(numberMap);

        return phoneInitializerData;
    }

	private List<String> getAllCountryCodes() {
		List<Country> countries = entityDao.findAll(Country.class);

		return countries.stream().filter(country -> {
			return isCountryApprovedAndActive(country);
		}).map(Country::getCountryISOCode).collect(Collectors.toList());
	}
	
	private boolean isCountryApprovedAndActive(Country country)
	{
		if(!(country.getApprovalStatus() == ApprovalStatus.APPROVED
				|| country.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED))
		{
			return false;
		}
		
		boolean isSnapShotRecord =false;
		if(country.getEntityLifeCycleData() != null && country.getEntityLifeCycleData().getSnapshotRecord()!=null)
			
		{
			isSnapShotRecord=country.getEntityLifeCycleData().getSnapshotRecord();
		}
		return  country.isActiveFlag() && true!=isSnapShotRecord;
		
	}

	public Boolean getFlagForPhoneOutsideAddressTag(){
        if(phoneBasedOnCountry.equals("true")){
            return true;
        }
        return false;
    }
    
    public String[] prepareCountryCodes() {
        String[] countryCodes = phoneCountryCodes.split(",");
        return countryCodes;
        
    }
    
    public String prepareCountryOptions(SortedMap<String, Object> numberMap){
    		StringBuilder pushDataObj=new StringBuilder("<option value=''>Select</option>");
    		for(Map.Entry<String, Object> entry : numberMap.entrySet()){
    			if(entry.getKey() != "otherMap"){
    				pushDataObj.append("<option value='");
    				pushDataObj.append(entry.getKey());
    				pushDataObj.append("'>");
    				pushDataObj.append(entry.getKey());
    				pushDataObj.append("</option>");
    			}
    		}
    	return pushDataObj.toString();
    }
    
    private String getUserLocale(){
        String userLocale = null;
        Map<String, ConfigurationVO> userPreferences;
        UserInfo userInfo;

        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            if (principal != null
                    && !ANONYMOUS_USER.equals(principal.toString())
                    && (principal instanceof UserInfo)) {
                userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                userPreferences = userInfo.getUserPreferences();
                userLocale = userPreferences.get(Configuration.USER_LOCALE).getText();
            } 
        }
		if (userLocale == null) {
			ConfigurationVO userLocaleConfiguration = configurationService
					.getConfigurationPropertyFor(
							SystemEntity.getSystemEntityId(),
							Configuration.USER_LOCALE);
			if (userLocaleConfiguration != null
					&& userLocaleConfiguration.getPropertyValue() != null) {
				userLocale = userLocaleConfiguration.getPropertyValue();
			}
		}
        return userLocale;
    }
    
    public String getMaxPermittedUserProfileImageSize() {
        return maxPermittedUserProfileImageSize;
    }

    public void setMaxPermittedUserProfileImageSize(
            String maxPermittedUserProfileImageSize) {
        this.maxPermittedUserProfileImageSize = maxPermittedUserProfileImageSize;
    }

	public PreferenceFormBean getUserPreferenceFormBean() {
		HttpSession currentHttpSession = getCurrentSessionIfExists();
		if (currentHttpSession == null) {
			return null;
		}
		UserInfo userInfo = getLoggedInUserInfo();
		if (userInfo == null) {
			return null;
		}
		PreferenceFormBean preferenceFormBean = (PreferenceFormBean) currentHttpSession
				.getAttribute(Configuration.PREFERENCES);
		if (preferenceFormBean != null) {
			return preferenceFormBean;
		}

		Map<String, ConfigurationVO> userPreferencesMap = userInfo.getUserPreferences();
		List<ConfigurationVO> configVOList = new ArrayList<ConfigurationVO>();
        List<ConfigurationVO> configVOCAList = new ArrayList<ConfigurationVO>();
        for(ConfigurationVO configVO : userPreferencesMap.values()) {
            if (configVO.getPropertyKey().startsWith("config.CAStage")) {
                configVOCAList.add(configVO);
            } else {
                configVOList.add(configVO);
            }
        }
		preferenceFormBean = new PreferenceFormBean();
		preferenceFormBean.setConfigVOList(configVOList);
        preferenceFormBean.setConfigVOCAList(configVOCAList);
		currentHttpSession.setAttribute(Configuration.PREFERENCES, preferenceFormBean);

		return preferenceFormBean;
	}
	
	public UserInfo getLoggedInUserInfo() {
		if (SecurityContextHolder.getContext() != null
				&& SecurityContextHolder.getContext().getAuthentication() != null) {
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal != null && !ANONYMOUS_USER.equals(principal.toString()) && (principal instanceof UserInfo)) {
				return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

			}
		}
		return null;
	}
	
	public HttpSession getCurrentSessionIfExists() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		return attr.getRequest().getSession(false);
	}

	public String getDefaultDateMinimumYear() {
		return defaultDateMinimumYear;
	}

	public void setDefaultDateMinimumYear(String defaultDateMinimumYear) {
		this.defaultDateMinimumYear = defaultDateMinimumYear;
	}
	
	public String getDefaultTargetUrl() {
		return defaultTargetUrl;
	}
	
	public void setDefaultTargetUrl(String defaultTargetUrl) {
		this.defaultTargetUrl = defaultTargetUrl;
	}
	
    
    public String getDefaultTargetUrlWithhkstd() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = attr.getRequest();
		String contextPath = request.getContextPath();
		String csrfToken = CSRFTokenManager.getTokenForSession(request);
		return getURIWithSecurityToken(contextPath+defaultTargetUrlWithhkstd,csrfToken);
	}
    
	public String getURIWithSecurityToken(String location, String csrfToken){
		return URLBuilderHelper.appendSecurityTokenAndTimeStampToURL(location, csrfToken);
	}

	public void setDefaultTargetUrlWithhkstd(String defaultTargetUrlWithhkstd) {
		this.defaultTargetUrlWithhkstd = defaultTargetUrlWithhkstd;
	}


    public String getsessionFailoverBaseUrl() {
        return sessionFailoverBaseUrl;
    }
	
	public String getSsoLoginUrl() {
		return ssoLoginUrl;
	}

	public void setSsoLoginUrl(String ssoLoginUrl) {
		this.ssoLoginUrl = ssoLoginUrl;
	}
	 public boolean getSsoActive(){
	    	return this.isSsoActive;
	}
	    
	public void setSsoActive(boolean isSsoActive) {
		this.isSsoActive = isSsoActive;
	}

	public String getLogoutURL() {
		return logoutURL;
	}

	public void setLogoutURL(String logoutURL) {
		this.logoutURL = logoutURL;
	}

	public String getSsoLogoutURL() {
		return ssoLogoutURL;
	}

	public void setSsoLogoutURL(String ssoLogoutURL) {
		this.ssoLogoutURL = ssoLogoutURL;
	}

	public String getSsoTicketValidatorUrl() {
		return ssoTicketValidatorUrl;
	}

	public void setSsoTicketValidatorUrl(String ssoTicketValidatorUrl) {
		this.ssoTicketValidatorUrl = ssoTicketValidatorUrl;
	}
	
	public String getSsoLogoutRedirectUri() {
		return ssoLogoutRedirectUri;
	}

	public void setSsoLogoutRedirectUri(String ssoLogoutRedirectUri) {
		this.ssoLogoutRedirectUri = ssoLogoutRedirectUri;
	}

	public String getNodeIPAddress() {
		if (null == appServer_IpAddress) {
			try {
				appServer_IpAddress = InetAddress.getLocalHost().getHostAddress();

			} catch (UnknownHostException e) {

				try {
					appServer_IpAddress = getIPv6Addresses(
							InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())).getHostAddress();
				} catch (UnknownHostException e1) {

					throw new RuntimeException("Unable to resolve host IP address.");

				}
			}
		}
		return appServer_IpAddress;
	}
	
	private Inet6Address getIPv6Addresses(InetAddress[] addresses) {
	    for (InetAddress addr : addresses) {
	        if (addr instanceof Inet6Address) {
	            return (Inet6Address) addr;
	        }
	    }
	    throw new RuntimeException("Unable to resolve host IP address.");
	}
}