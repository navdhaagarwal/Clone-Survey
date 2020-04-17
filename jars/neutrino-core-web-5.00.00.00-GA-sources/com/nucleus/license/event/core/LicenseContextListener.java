package com.nucleus.license.event.core;

import java.util.Map;

import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.license.cache.LicenseClientCacheService;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.license.model.LicenseValidator;
import com.nucleus.license.service.LicenseClientService;
import com.nucleus.license.service.LoginService;
import com.nucleus.logging.BaseLoggers;

@Named("licenseContextListener")
public class LicenseContextListener implements ServletContextListener {

    private static  ApplicationContext appCtx;

    private static final String MESSAGE_EXCEPTION = "Error in method contextInitialized in LicenseContextListener ";
    
    public void setApplicationContext(ServletContext servletConext){
		appCtx =WebApplicationContextUtils.getRequiredWebApplicationContext(servletConext);
	}

	@Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
			setApplicationContext(sce.getServletContext());

			String productCode = ProductInformationLoader.getProductCode();

			setLicenseInformationInContext( productCode);

		} catch (Exception exception) {
			BaseLoggers.exceptionLogger.error(MESSAGE_EXCEPTION,exception);
			
			  throw ExceptionBuilder.getInstance(SystemException.class, MESSAGE_EXCEPTION, MESSAGE_EXCEPTION)
			  .setOriginalException(exception)
	          .setMessage(CoreUtility.prepareMessage(MESSAGE_EXCEPTION)).setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
		}
    }

	private void setLicenseInformationInContext( String productCode) {
		LicenseClientService licenseClientService = (LicenseClientService) appCtx.getBean("licenseClientService");
		
		Map<String, LicenseDetail> productCodeAndlicDetailMap = licenseClientService.getLicenseFromAppliedLicenses();

		if (productCodeAndlicDetailMap != null) {
			for (Map.Entry<String, LicenseDetail> entry : productCodeAndlicDetailMap.entrySet()) {
				
				if (productCode.equals(entry.getKey())) {		
					setLicenseInformationForValidMAC(productCodeAndlicDetailMap,entry.getValue());
					
					break;
				}

			}
			
		}                                                      

	}

	private void setLicenseInformationForValidMAC(Map<String, LicenseDetail> productCodeAndlicDetailMap, LicenseDetail licenseDetail) {
		
		if(LicenseValidator.isValidMacAddress(licenseDetail)){
			LoginService loginService = (LoginService) appCtx.getBean("loginService");
			LicenseClientCacheService licenseClientCacheService = (LicenseClientCacheService) appCtx.getBean("licenseClientCacheService");
			licenseClientCacheService.update(NeutrinoCachePopulator.Action.INSERT, productCodeAndlicDetailMap);
			
			
			loginService.updateRegistery();
			BaseLoggers.flowLogger.debug("License Context loaded for application");
			
			
			
			
		}else {
			BaseLoggers.exceptionLogger.error("Exception in LicenseContextListener occured:Invalid MAC address for license");
			throw new SystemException("Invalid MAC address for license");
		}
		
	}

	@Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void contextDestroyed(ServletContextEvent sce) {
        LoginService loginService = (LoginService) appCtx.getBean("loginService");
        loginService.saveUserRegistery();
        BaseLoggers.flowLogger.debug("LicenseContextListener contextDestroyed(): LicenseInformation saved to Registery");
    }

}
