package com.nucleus.web.security;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.cache.common.CacheManager;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.NeutrinoSessionRegistryImpl;
import com.nucleus.security.masking.MaskingUtility;
import com.nucleus.web.common.CommonConfigUtility;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Arrays;
import java.util.Map;

@Named("genericContextListener")
public class GenericContextListener implements ServletContextListener {

	private static ApplicationContext appCtx;
	
	private static final String    IV_KEY  = "config.encryption.iv";
    private static final String    SALT_KEY = "config.encryption.salt";

    private static final String    KEY_SIZE_KEY        = "config.encryption.keysize";
    private static final String    ITERATION_COUNT_KEY = "config.encryption.iterationcount";
    
    private static final String    NOTIFICATIONS_TOOLTIP  = "config.notifications.tooltip";
    private static final String    SANITIZE_ENABELED="config.security.sanitizingEnabled";
	private static final String    BROWSER_IDLE_TIMEOUT_MINUTES  = "config.client.browser.idle.timeout";
	private static final String    UI_DEBUG_MODE  = "config.ui.debug.mode";//PDDEV-13595 flag to load unbundled files into browser(only for debugging).
	private static final String    DATE_MINIMUM_YEAR_KEY  = "config.user.date.minimum.year";
	private static final String    MESSAGE_EXCEPTION = "Error in method contextInitialized in GenericContextListener ";
	
	public void setApplicationContext(ServletContext servletConext){
		appCtx =WebApplicationContextUtils.getRequiredWebApplicationContext(servletConext);
	}

	@Override
	public void contextInitialized(ServletContextEvent paramServletContextEvent) {
		try{
		ServletContext servletContext = paramServletContextEvent.getServletContext();
		setApplicationContext(servletContext);
		publishFwCache();
		//set password encryption parameters
		ConfigurationService configurationService = (ConfigurationService) appCtx.getBean("configurationService");
		Map<String, ConfigurationVO> conf = configurationService.getFinalConfigurationForEntity(SystemEntity.getSystemEntityId());
    	AesUtil.setIv(conf.get(IV_KEY).getPropertyValue()); 
    	AesUtil.setSalt(conf.get(SALT_KEY).getPropertyValue());
    	AesUtil.setKeysize(Integer.parseInt(conf.get(KEY_SIZE_KEY).getPropertyValue())) ;
    	AesUtil.setIterationCount(Integer.parseInt(conf.get(ITERATION_COUNT_KEY).getPropertyValue())) ;
    	
 // Added for masking policy change
    	MaskingUtility maskingUtility = (MaskingUtility)appCtx.getBean("maskingUtility");
    	servletContext.setAttribute("maskingUtility", maskingUtility);
    	
    	updateNodeDetails(servletContext);
      
		String systemTooltipValue = "false";
		String sanitizeEnabled = "true";
		if (conf.get(NOTIFICATIONS_TOOLTIP) != null) {
			systemTooltipValue = conf.get(NOTIFICATIONS_TOOLTIP).getPropertyValue();
		}
		servletContext.setAttribute("systemTooltipValue", systemTooltipValue);
		if (conf.get(SANITIZE_ENABELED) != null) {
			sanitizeEnabled = conf.get(SANITIZE_ENABELED).getPropertyValue();
		}
		Integer clientBrowserIdleTimeout=30;
		if (conf.get(BROWSER_IDLE_TIMEOUT_MINUTES) != null) {
			clientBrowserIdleTimeout = Integer.valueOf(conf.get(BROWSER_IDLE_TIMEOUT_MINUTES).getPropertyValue());
		}
		boolean uiDebugMode = false;
		if (conf.get(UI_DEBUG_MODE) != null) {
			uiDebugMode = Boolean.valueOf(conf.get(UI_DEBUG_MODE).getPropertyValue());
		}
		servletContext.setAttribute("uiDebugMode", uiDebugMode);
		String defaultDateMinimumYear="";		
		if (conf.get(DATE_MINIMUM_YEAR_KEY) != null) {
			defaultDateMinimumYear = conf.get(DATE_MINIMUM_YEAR_KEY).getPropertyValue();
		}
		
		servletContext.setAttribute("clientBrowserIdleTimeout", clientBrowserIdleTimeout);
		servletContext.setAttribute("systemTooltipValue", systemTooltipValue);
		updateCdnUrlDetails(servletContext, conf);
		updatePageRefresh(servletContext, conf);
		CommonConfigUtility commonConfigUtility = (CommonConfigUtility) appCtx.getBean("commonConfigUtility");
		NeutrinoSessionRegistryImpl neutrinoSessionRegistry = (NeutrinoSessionRegistryImpl) appCtx.getBean("sessionRegistry");
		
		
		commonConfigUtility.setSanitizingEnabled(Boolean.valueOf(sanitizeEnabled));
		commonConfigUtility.setDefaultDateMinimumYear(defaultDateMinimumYear);
	   	String [] activeProfiles = appCtx.getEnvironment()
					.getActiveProfiles();
					if(activeProfiles.length == 0){
						activeProfiles = appCtx.getEnvironment()
					.getDefaultProfiles();
					}
			String allActiveProfiles = Arrays.toString(activeProfiles);
			if (allActiveProfiles.contains("sso")) {
				commonConfigUtility.setSsoActive(true);
				neutrinoSessionRegistry.setSsoActive(true);

			}
			servletContext.setAttribute("commonConfigUtility", commonConfigUtility);
			servletContext.setAttribute("neutrinoUrlValidatorFilterConfig", appCtx.getBean("neutrinoUrlValidatorFilterConfig"));
		
		NeutrinoSecurityUtility.updateSantizingEnabled(Boolean.valueOf(sanitizeEnabled));
		}
	    catch (Exception exception) {
	      BaseLoggers.exceptionLogger.error(MESSAGE_EXCEPTION,exception);
		  throw ExceptionBuilder.getInstance(SystemException.class, MESSAGE_EXCEPTION, MESSAGE_EXCEPTION)
		  .setOriginalException(exception)
          .setMessage(CoreUtility.prepareMessage(MESSAGE_EXCEPTION)).setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
	    }

	}

  private void updateNodeDetails(ServletContext servletContext) {
	  CoreUtility coreUtility = (CoreUtility) appCtx.getBean("coreUtility", CoreUtility.class);
	  if (coreUtility == null) {
		  throw new com.nucleus.core.exceptions.SystemException("Bean Not Found for CORE UTILITY");
	  }
	  servletContext.setAttribute("nodeId", coreUtility.getServerNodeId());
  }

	@Override
	public void contextDestroyed(ServletContextEvent paramServletContextEvent) {
		BaseLoggers.flowLogger.debug("GenericContextListener contextDestroyed()");
		
	}
	
	private void publishFwCache(){
		CacheManager cacheManager = (CacheManager) appCtx.getBean(FWCacheConstants.CACHE_MANAGER);
		cacheManager.startCacheManager();		
	}
	
	private void updateCdnUrlDetails(ServletContext servletContext, Map<String, ConfigurationVO> conf){
		
		ConfigurationVO cdnEnabledConfigurationVO = conf.get(ProductInformationLoader.getProductCode()+"."+Configuration.IS_CDN_ENABLED);
		ConfigurationVO cdnUrlConfigurationVO = conf.get(ProductInformationLoader.getProductCode()+"."+Configuration.CDN_URL);
		if(cdnEnabledConfigurationVO!=null 
			&& cdnUrlConfigurationVO!=null
			&& cdnEnabledConfigurationVO.getPropertyValue()!=null 
			&& "true".equalsIgnoreCase(cdnEnabledConfigurationVO.getPropertyValue())
			&&  cdnUrlConfigurationVO.getPropertyValue()!=null){
			
			servletContext.setAttribute("isCdnEnabled", true);
			servletContext.setAttribute("cdnUrl", cdnUrlConfigurationVO.getPropertyValue());
			
		}
	}
	
	private void updatePageRefresh(ServletContext servletContext, Map<String, ConfigurationVO> conf){
		ConfigurationVO pageRefreshEnabledConfigurationVO = conf.get(ProductInformationLoader.getProductCode()+"."+Configuration.IS_PAGE_REFRESH_ENABLED);
		if(pageRefreshEnabledConfigurationVO!=null 
				&& pageRefreshEnabledConfigurationVO.getPropertyValue()!=null 
				&& "true".equalsIgnoreCase(pageRefreshEnabledConfigurationVO.getPropertyValue())){
				servletContext.setAttribute("isPageRefreshEnabled", Boolean.TRUE);
			}else{
				servletContext.setAttribute("isPageRefreshEnabled", Boolean.FALSE);
			}
	}
}
