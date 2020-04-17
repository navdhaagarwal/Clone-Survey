package com.nucleus.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.mutitenancy.service.MultiTenantService;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.EventBus;
import com.nucleus.persistence.EntityDao;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;
import com.nucleus.standard.context.NeutrinoExecutionContextHolder;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

@Transactional(propagation = Propagation.REQUIRED)
public abstract class BaseServiceImpl implements BaseService {

    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    public static final String DEFAULT_TIME_FORMAT = "hh:mm:ss a";
    private static final String SPACE_STRING        = " ";

    
    private Locale              defaultUserlocale;
    
    private Boolean isApiManagerEnabled = null;

    @Autowired
    protected EventBus          eventBus;
    
    @Autowired
	private Environment environment;

    @Inject
    @Named("entityDao")
    protected EntityDao         entityDao;
    
    @Inject
    @Named("neutrinoExecutionContextHolder")
    protected INeutrinoExecutionContextHolder         neutrinoExecutionContextHolder;
   
	   
    @Inject
    @Named("multiTenantService")
    private MultiTenantService multiTenantService;

    public Locale getSystemLocale() {
    	if(defaultUserlocale==null) {
    		defaultUserlocale=multiTenantService.getSystemLocale();
    	}
		return defaultUserlocale;
	}

    
    public EventBus getEventBus() {
        return eventBus;
    }

    public UserInfo getCurrentUser() {
        
       UserInfo  userInfo= neutrinoExecutionContextHolder.getLoggedInUserDetails();
       if(userInfo!=null){
    	   return userInfo;
       }
       SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null && null != securityContext.getAuthentication()) {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (UserInfo.class.isAssignableFrom(principal.getClass())) {
                userInfo = (UserInfo) principal;
            }
        }
        return userInfo;
    }

    /**
     * Returns preferred date format string of current user. If there is no current user then a default
     * date format is returned.
     *   
     * @return user preferred date format
     */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String getUserPreferredDateFormat() {
		UserInfo userInfo = getCurrentUser();

		// If UserInfo is null, return DEFAULT_DATE_FORMAT i.e. MM/dd/yyyy
		if (userInfo == null) {
			return DEFAULT_DATE_FORMAT;
		}

		/*
		 * Check for User Preferences in UserInfo, if Not Null set dateFormat from
		 * ConfigurationVo, else fetch ConfigurationVo for User from DB.
		 */
		ConfigurationVO configVo = null;
		if (userInfo.getUserPreferences() != null) {
			configVo = userInfo.getUserPreferences().get("config.date.formats");

		} else {
			ConfigurationService configurationService = (ConfigurationService) NeutrinoSpringAppContextUtil
					.getBeanByName("configurationService", ConfigurationService.class);
			configVo = configurationService.getConfigurationPropertyFor(new EntityId(User.class, userInfo.getId()),
					"config.date.formats");
		}
		return configVo != null ? configVo.getText() : getSystemDateFormat();

    }

    /**
     * Returns preferred TimeZone string Id of current user
     *   
     * @return user preferred Time Zone 
     */
    public String getUserPreferredTimeZone() {
        String timeZoneId = DateTimeZone.getDefault().getID();
        UserInfo userInfo = getCurrentUser();
        if (userInfo != null) {
            timeZoneId = userInfo.getUserPreferences().get("config.user.time.zone").getText();
        }
        return timeZoneId;
    }

    public DateTime parseDateTime(String dateTime) throws ParseException {
        String timeZoneId = getUserPreferredTimeZone();
        DateTimeZone dtz = DateTimeZone.forID(timeZoneId);
        String pattern = getUserPreferredDateFormat();
        String alternatePattern = pattern + DateUtils.DEFAULT_TIME_FORMAT;
        String timePattern = DateUtils.DEFAULT_TIME_FORMAT;
        String altTimePattern = DateUtils.ALTERNATE_TIME_FORMAT;
        return new DateTime(org.apache.commons.lang3.time.DateUtils.parseDateStrictly(dateTime, new String[] {
                alternatePattern, pattern, timePattern, altTimePattern }), dtz);
    }
    
    public Date parseDate(String date) throws ParseException {
    	String pattern = getUserPreferredDateFormat();
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.parse(date);
    }
    
    public LocalDate parseLocalDate(String dateTime) throws ParseException {
        String timeZoneId = getUserPreferredTimeZone();
        DateTimeZone dtz = DateTimeZone.forID(timeZoneId);
        String pattern = getUserPreferredDateFormat();
        return new DateTime(org.apache.commons.lang3.time.DateUtils.parseDateStrictly(dateTime, new String[] { pattern }),
                dtz).toLocalDate();
    }

    /**
     * Returns preferred date and time format string of current user. If there is no current user then a default
     * date/time format is returned.
     *   
     * @return user preferred date format
     */
    public String getUserPreferredDateTimeFormat() {
        return getUserPreferredDateFormat() + SPACE_STRING + DEFAULT_TIME_FORMAT;
    }

    /**
     * Returns the current user's locale.If can not be determined,default locale is returned.
     * 
     * @return current user's locale
     */
    public Locale getUserLocale() {
        Locale locale = null;
        UserInfo ui = getCurrentUser();
        if (ui != null && ui.getUserPreferences()!=null) {
            ConfigurationVO preferences = ui.getUserPreferences().get("config.user.locale");
            String[] localeString = preferences.getText().split("_");
            if (localeString.length >= 2) {
                locale = new Locale(localeString[0], localeString[1]);
            }
        } else {
            if (getSystemLocale() != null) {
                    locale = getSystemLocale();
            } else {
                locale = Locale.getDefault();
            }
        }
        return locale;

    }
    
    public String getSystemDateFormat(){
      String systemDateFormat = DEFAULT_DATE_FORMAT;
      ConfigurationService configurationService = (ConfigurationService)NeutrinoSpringAppContextUtil.getBeanByName("configurationService", ConfigurationService.class);
      ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), "config.date.formats");
      if (configVo != null) {
        systemDateFormat = configVo.getPropertyValue();
      }

      return systemDateFormat;
    }
    
    public void flushCurrentTransaction() {
    	entityDao.flush();    		
	}
    
    /*This method is now moved to common-masters framework CasBaseServiceImpl*/
    /*public DateTime getCurrentDate() {
        BusinessDate businessDate = businessDateService.findBusinessDate();
        DateTime currentDate = null;
        if (null != businessDate && null != businessDate.getSelectedBusinessDate()) {
            currentDate = businessDate.getSelectedBusinessDate();
        } else {
            currentDate = DateUtils.getCurrentUTCTime();
        }

        return currentDate;
    }*/
    
    
    private boolean isApiManagerEnabled() {
		if (isApiManagerEnabled == null) {
			String[] defaultProfiles = environment.getDefaultProfiles();
			String[] activeProfiles = environment.getActiveProfiles();

			// check if profile is api-manager-enabled then this filter is not required.
			if (defaultProfiles != null && defaultProfiles.length > 0
					&& (Arrays.asList(defaultProfiles).contains("api-manager-enabled"))
					|| (activeProfiles != null && activeProfiles.length > 0
							&& (Arrays.asList(activeProfiles).contains("api-manager-enabled")))) {
				isApiManagerEnabled = Boolean.TRUE;
			} else {
				isApiManagerEnabled = Boolean.FALSE;
			}
		}
		return isApiManagerEnabled;
	}
    
  
	public String getTrustedSourceName() {
		String trustedSourceId = null;

		if (isApiManagerEnabled()) {
			
			trustedSourceId = (String) neutrinoExecutionContextHolder.getFromLocalContext("trustedSourceName");
			

		} else {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication instanceof OAuth2Authentication) {

				trustedSourceId = ((OAuth2Authentication) authentication).getOAuth2Request().getClientId();

			}
		}
		return trustedSourceId;
	}

	

}
