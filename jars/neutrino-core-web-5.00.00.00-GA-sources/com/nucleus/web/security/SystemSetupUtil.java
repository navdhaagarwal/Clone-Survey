package com.nucleus.web.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.configconvertors.ConfigConvertorFactory;
import com.nucleus.config.persisted.configconvertors.IConfigConvertor;
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
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.web.common.CommonConfigUtility;

@Named("systemSetupUtil")
public class SystemSetupUtil extends BaseServiceImpl {

	@Inject
	@Named("licenseClientCacheService")
	private   BaseLicenseService licenseClientCacheService;

    @Value("${core.web.config.setup.enabled}")
    private String               systemSetupEnabled;

    @Value("${core.web.config.invalidsession.failure.url}")
    private String               invalidOrExpiredSessionUrl;

    @Value("${core.web.config.invalidsession.failure.url.setup}")
    private String               invalidOrExpiredSessionUrlForSetup;

    @Value("${core.web.config.default.target.url}")
    private String               targetUrl;

    @Value("${core.web.config.default.target.url.setup}")
    private String               targetUrlForSetup;

    @Value("${core.web.config.login.form.url}")
    private String               loginFormUrl;

    @Value("${core.web.config.login.form.url.setup}")
    private String               loginFormUrlForSetup;
    
    @Value("${core.web.config.system.setup.progress.url}")
    private String               systemSetUpInProgressUrl;

	@Value("${core.web.config.logout.success.url}")
    private String               logoutSuccessUrl;

    @Value("${core.web.config.logout.success.url.setup}")
    private String               logoutSuccessUrlForSetup;

    @Value("${core.web.config.default.failure.url}")
    private String               failureUrl;

    @Value("${core.web.config.default.failure.url.setup}")
    private String               failureUrlForSetup;
    
    @Value("${system.cloud.deployed}")
    private String               deployedOnCloud;
    
    @Value("${core.web.config.reset.password.target.url}")
    private String               resetPasswordUrl;
    
    @Value("${core.web.config.license.agreement.target.url}")
    private String               licenseAgreementUrl;
    
    @Value("${system.config.systemSetup.flag}")
    private String               systemSetupPropertyConfig;
    
    @Value("${config.systemSetUp.inProgress.flag}")
    private String               systemSetupInProgressPropertyConfig;
    
    @Value("${core.web.config.SSO.logout.url.value}")
    private String               ssoLogoutUrl;

	private static Boolean       isSystemSetupFlag        = null;
    
    private static Boolean 		isSystemSetupInProgress  = null;
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private String  renewLicenseUrl= "/app/systemSetup/license/updateLicense";
    private  String productCode=ProductInformationLoader.getProductCode();
    @Inject
    @Named(value = "configurationService")
    private ConfigurationService configurationService;
    
	@Inject
    @Named("commonConfigUtility")
    private CommonConfigUtility commonConfigUtility;
    
    public boolean isSystemSetup() {

        if (Boolean.valueOf(systemSetupEnabled)) {
        	if(StringUtils.isEmpty(systemSetupPropertyConfig)){
        		throw new SystemException("System setup Property is not defined.");
        	}
            if (isSystemSetupFlag == null) {
            	Configuration configuration = configurationService.getConfigurationPropertyFor(
                        SystemEntity.getSystemEntityId(), productCode+"."+systemSetupPropertyConfig);
                isSystemSetupFlag = false;
                if(configuration!=null 
                		&& StringUtils.isNotBlank(configuration.getPropertyValue())){
                	isSystemSetupFlag=Boolean.valueOf(configuration.getPropertyValue());
                }else{
                	List<ConfigurationVO> configVOList = new ArrayList<ConfigurationVO>();
                	ConfigurationVO configVO = new ConfigurationVO();
                	configVO.setPropertyKey(productCode+"."+systemSetupPropertyConfig);
                	configVO.setValueType(ValueType.BOOLEAN_VALUE);
                	configVO.setConfigurable(false);
                	configVO.setUserModifiable(false);
                	configVOList.add(configVO);
                	
                	configurationService.syncConfiguration(EntityId.fromUri("com.nucleus.entity.SystemEntity:1"), configVOList);
                }
            }
            return isSystemSetupFlag;
        }
        return true;	
    }
    
    public boolean isSystemSetUpInProgress(){
        if (Boolean.valueOf(systemSetupEnabled)) {
        	if(StringUtils.isEmpty(systemSetupInProgressPropertyConfig)){
        		throw new SystemException("System setup Property is not defined.");
        	}
            if (isSystemSetupInProgress == null) {
            	Configuration configuration =configurationService.getConfigurationPropertyFor(
                        SystemEntity.getSystemEntityId(), productCode+"."+systemSetupInProgressPropertyConfig);
            	isSystemSetupInProgress = false;
            	if(configuration!=null 
                		&& StringUtils.isNotBlank(configuration.getPropertyValue())){
            		isSystemSetupInProgress = Boolean.valueOf(configuration.getPropertyValue());
            	}else{
                	List<ConfigurationVO> configVOList = new ArrayList<ConfigurationVO>();
                	ConfigurationVO configVO = new ConfigurationVO();
                	configVO.setPropertyKey(productCode+"."+systemSetupInProgressPropertyConfig);
                	configVO.setValueType(ValueType.BOOLEAN_VALUE);
                	configVO.setConfigurable(false);
                	configVO.setUserModifiable(false);
                	configVOList.add(configVO);
                	
                	configurationService.syncConfiguration(EntityId.fromUri("com.nucleus.entity.SystemEntity:1"), configVOList);
                }
            	
            }
            return isSystemSetupInProgress;
        }
        return true;
    
    }
    
    public void updateSystemSetupFlagValue(boolean systemSetupFlag) {
        ArrayList<ConfigurationVO> configVOList = new ArrayList<ConfigurationVO>();
        Map<String, ConfigurationVO> configurationMap = configurationService.getFinalConfigurationForEntity(SystemEntity
                .getSystemEntityId());
        Iterator<ConfigurationVO> it = configurationMap.values().iterator();
        while (it.hasNext()) {
            ConfigurationVO configObj = it.next();
            if (configObj.getValueType().toString().equalsIgnoreCase(ValueType.BOOLEAN_VALUE.toString())
                    && (configObj.getPropertyKey().equalsIgnoreCase(productCode+"."+systemSetupPropertyConfig))) {
            	Long configId = configObj.getId();
            	Boolean usermodifiable = configObj.isUserModifiable();
         
				configObj.setAssociatedEntityId(SystemEntity.getSystemEntityId());
                configObj.setValueType(ValueType.BOOLEAN_VALUE);
                configObj.setConfigurable(systemSetupFlag);
                configObj.setId(configId);
                configObj.setUserModifiable(usermodifiable);
            }
            configVOList.add(configObj);
        }

        configurationService.syncConfiguration(SystemEntity.getSystemEntityId(), configVOList);
        isSystemSetupFlag = systemSetupFlag;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSystemSetupInProgressFlagValue(boolean systemSetUpInProgressFlag){

        ArrayList<ConfigurationVO> configVOList = new ArrayList<ConfigurationVO>();
        Map<String, ConfigurationVO> configurationMap = configurationService.getFinalConfigurationForEntity(SystemEntity
                .getSystemEntityId());
        Iterator<ConfigurationVO> it = configurationMap.values().iterator();
        while (it.hasNext()) {
            ConfigurationVO configObj = it.next();
            if (configObj.getValueType().toString().equalsIgnoreCase(ValueType.BOOLEAN_VALUE.toString())
                    && (configObj.getPropertyKey().equalsIgnoreCase(productCode+"."+systemSetupInProgressPropertyConfig))) {
            	//TODO write a constructor to construct ConfigurationVO from an existing ConfigurationVO object and remove below conversion
            	Long configId = configObj.getId();
            	Boolean usermodifiable = configObj.isUserModifiable();
            	IConfigConvertor configConvertor = ConfigConvertorFactory
						.getConvertorFromVO(configObj);
				Configuration config = configConvertor
						.toConfiguration(configObj);
				configObj=configConvertor.fromConfiguration(config);
				
				configObj.setAssociatedEntityId(SystemEntity.getSystemEntityId());
                configObj.setValueType(ValueType.BOOLEAN_VALUE);
                configObj.setConfigurable(systemSetUpInProgressFlag);
                configObj.setId(configId);
                configObj.setUserModifiable(usermodifiable);
            }
            configVOList.add(configObj);
        }

        configurationService.syncConfiguration(SystemEntity.getSystemEntityId(), configVOList);
        isSystemSetupInProgress = systemSetUpInProgressFlag;
    
    }

    public String getInvalidOrExpiredSessionUrl() {
        if (isSystemSetup()) {
            return invalidOrExpiredSessionUrl;
        }
        return invalidOrExpiredSessionUrlForSetup;

    }

    public String getAuthenticationSuccessUrl(HttpServletRequest req) {

        if (isSystemSetup()) {
        	
            String expiryUrl= checkLicenseRenewUrlForExpired(req);
            if(expiryUrl!=null)
            	return expiryUrl;
        	
        	BaseLoggers.flowLogger.error("****isSystemSetup is true redirecting to dashboard*****");
            return targetUrl;
        }
        BaseLoggers.flowLogger.error("*****isSystemSetup is false redirecting to systemSetupUrl*****");
        return targetUrlForSetup;

    }

    private String checkLicenseRenewUrlForExpired(HttpServletRequest req) {
    	 LicenseDetail licenseInformation =licenseClientCacheService.getCurrentProductLicenseDetail(); 
    	if(licenseInformation!=null)
        {
              DateTimeFormatter dtf = DateTimeFormat.forPattern(DATE_FORMAT);
              DateTime licenseExpiryDate=licenseInformation.getExpiryDate();
              DateTime now=new DateTime();
              now=dtf.parseDateTime(now.toString(DATE_FORMAT));
              if(licenseInformation.getGracePeriod()!=null)
              {
            	  licenseExpiryDate=licenseExpiryDate.plusDays(licenseInformation.getGracePeriod());
              }
              
              licenseExpiryDate=dtf.parseDateTime(licenseExpiryDate.toString(DATE_FORMAT));
       if (licenseExpiryDate.isBefore(now)) {
		return renewLicenseUrl ;
       }
	}
    	return null;
    }

	public String getAuthenticationEntryPointLoginFormUrl() {

        if (isSystemSetup()) {
            return loginFormUrl;
        }
        return loginFormUrlForSetup;

    }

    public String getLogoutSuccessTargetUrl() {

        if (isSystemSetup()) {
            return logoutSuccessUrl;
        }
        return logoutSuccessUrlForSetup;

    }

    public String getCustomConcurrentSessionFilterExpiredUrl() {
    	
    	if(commonConfigUtility.getSsoActive()) {
    		return ssoLogoutUrl.concat("?error=true&errCode=ERR.INVALIDSESSION.MSG");
    	}

        if (isSystemSetup()) {
            return invalidOrExpiredSessionUrl;
        }
        return invalidOrExpiredSessionUrlForSetup;

    }

    public String getAuthenticationFailureUrl() {

        if (isSystemSetup()) {
            return failureUrl;
        }
        return failureUrlForSetup;

    }

    public static Boolean getSystemFlag() {
        return isSystemSetupFlag;
    }

    public static void setSystemFlag(Boolean systemFlag) {
        SystemSetupUtil.isSystemSetupFlag = systemFlag;
    }

    public String getLoginFormUrl() {
        return loginFormUrl;
    }

    public void setLoginFormUrl(String loginFormUrl) {
        this.loginFormUrl = loginFormUrl;
    }

    public String getLoginFormUrlForSetup() {
        return loginFormUrlForSetup;
    }

    public void setLoginFormUrlForSetup(String loginFormUrlForSetup) {
        this.loginFormUrlForSetup = loginFormUrlForSetup;
    }

    public String getLogoutSuccessUrl() {
        return logoutSuccessUrl;
    }

    public void setLogoutSuccessUrl(String logoutSuccessUrl) {
        this.logoutSuccessUrl = logoutSuccessUrl;
    }

    public String getLogoutSuccessUrlForSetup() {
        return logoutSuccessUrlForSetup;
    }

    public void setLogoutSuccessUrlForSetup(String logoutSuccessUrlForSetup) {
        this.logoutSuccessUrlForSetup = logoutSuccessUrlForSetup;
    }
    
	public boolean isSystemDeployedOnCloud() {
		BaseLoggers.flowLogger.info("SystemCloudDeployed = {}", deployedOnCloud);
		return Boolean.valueOf(deployedOnCloud);
	}

	public void setDeployedOnCloud(String deployedOnCloud) {
		this.deployedOnCloud = deployedOnCloud;
	}

	public String getResetPasswordUrl() {
		return resetPasswordUrl;
	}

	public void setResetPasswordUrl(String resetPasswordUrl) {
		this.resetPasswordUrl = resetPasswordUrl;
	}

	public String getLicenseAgreementUrl() {
		return licenseAgreementUrl;
	}

	public void setLicenseAgreementUrl(String licenseAgreementUrl) {
		this.licenseAgreementUrl = licenseAgreementUrl;
	}
	
	 public String getSystemSetUpInProgressUrl() {
			return systemSetUpInProgressUrl;
	}

	public void setSystemSetUpInProgressUrl(String systemSetUpInProgressUrl) {
		this.systemSetUpInProgressUrl = systemSetUpInProgressUrl;
	}

	public String getSystemSetupPropertyConfig() {
		return systemSetupPropertyConfig;
	}

	public void setSystemSetupPropertyConfig(String systemSetupPropertyConfig) {
		this.systemSetupPropertyConfig = systemSetupPropertyConfig;
	}
		

}
