package com.nucleus.web.exceptionHandler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nucleus.license.cache.BaseLicenseService;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.security.CustomCasAuthenticationEntryPoint;

@Controller
@RequestMapping("/ssoExceptionHandler")
@Profile("sso")
public class SsoExceptionHandler {
	@Inject
	@Named("licenseClientCacheService")
	private   BaseLicenseService licenseClientCacheService;
	
    @Inject
    @Named("casAuthenticationEntryPoint")
    private CustomCasAuthenticationEntryPoint casAuthenticationEntryPoint;
    
    
    @RequestMapping(value = "/handleSsoConcurrencyException")
    public String handleSsoConcurrencyException(HttpServletRequest request, Model model) {
    	if(casAuthenticationEntryPoint ==null){
    		return "error";
    	}
		 LicenseDetail licenseInformation = licenseClientCacheService.getCurrentProductLicenseDetail();
		 Integer maxConcurrentUsers=null;
		 if(licenseInformation!=null){
			 maxConcurrentUsers = licenseInformation.getMaxConcurrentUsers();
		 }
		 BaseLoggers.flowLogger.error("*****************Maximum concurrent users for this license**************************" + maxConcurrentUsers);
    	request.setAttribute("errorMessage","Internal Server Error");
        String ssoUrl = casAuthenticationEntryPoint.getLoginUrl();
        return "redirect:" + ssoUrl + "?concurrencyError=true&maxConcurrentUsers="+maxConcurrentUsers;
    }
    
    @RequestMapping(value = "/handleSsoSessionConcurrencyException")
    public String handleSsoSessionConcurrencyException(HttpServletRequest request, Model model) {
    	if(casAuthenticationEntryPoint ==null){
    		return "error";
    	}
    	request.setAttribute("errorMessage","Internal Server Error");
    	
        String ssoUrl = casAuthenticationEntryPoint.getLoginUrl();
        return "redirect:" + ssoUrl + "?concurrencySessionError=true";
    }
    
    @RequestMapping(value = "/handleSSOLockedException")
    public String handleSSOLockedException(HttpServletRequest request, Model model) {
    	if(casAuthenticationEntryPoint ==null){
    		return "error";
    	}
    
        String ssoUrl = casAuthenticationEntryPoint.getLoginUrl();
        ssoUrl = ssoUrl.replace("login", "logout");
        return "redirect:" + ssoUrl + "?param=2";
    }
    
    
}
