package com.nucleus.web.security;

import static com.nucleus.web.login.LoginConstants.INVALID_LOGIN_HIGH_CONCURRENCY;
import static com.nucleus.web.login.LoginConstants.INVALID_LOGIN_IP_ADDRESS;
import static com.nucleus.web.login.LoginConstants.NO_ACTIVE_PRIMARY_BRANCH_AVAILABLE;
import static com.nucleus.web.login.LoginConstants.NO_ROLES_AVAILABLE;
import static com.nucleus.web.login.LoginConstants.USER_NON_LOGIN_CHECK;
import static com.nucleus.web.security.AesUtil.PASS_PHRASE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.misc.util.ExceptionUtility;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.EventService;
import com.nucleus.event.EventTypes;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.license.cache.BaseLicenseService;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.license.utils.LicenseSetupUtil;
import com.nucleus.license.utils.LicenseStateConstant;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;
import com.nucleus.security.core.session.SessionModuleService;
import com.nucleus.security.oauth.dao.CustomOauthTokenStoreDAO;
import com.nucleus.user.AccessType;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;
import com.nucleus.user.ipaddress.IpAddress;
import com.nucleus.web.login.LoginConstants;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;

import net.bull.javamelody.MonitoredWithSpring;

/**
* Authentication mechanism is based on the type of user trying to get authenticated.
* Old functionality of multipleProviders stacking is still provided and authenticationManager needs to be set(this is mandatory but not used in general)
* authenticationManagerMap can be used to map the authenticationManagers to a sourceSystem that a user belongs to.
* @author Nucleus Software Exports Limited
*/
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    
    private static final String APP_AUTH_LOGIN = "/app/auth/login?error=true";
    @Inject
	@Named("licenseClientCacheService")
	private   BaseLicenseService licenseClientCacheService;
    @Autowired
    private ImageCaptchaService captchaService;
    @Autowired
    private   LicenseSetupUtil licenseSetupUtil;
    @Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil     systemSetupUtil;
    
    @Inject
    @Named(value = "userService")
    private UserService userService;

    @Inject
    @Named(value = "sessionModuleService")
    private SessionModuleService sessionModuleService;

    private boolean             captchaEnabled;
    

    private Map<String, AuthenticationManager> authenticationManagerMap; 
    
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    @Inject
    @Named("organizationService")
    OrganizationService         organizationService;

    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService  userSessionManagerService;
    
    @Inject
    @Named("eventService")
    private EventService eventService;
    
    @Inject
    @Named("sessionRegistry")
    private NeutrinoSessionRegistry sessionRegistry;
    

    // possible values : "Y" or "N"
    @Value(value = "#{'${core.web.config.webClientToEncryptpwd}'}")
    private String              webClientToEncryptpwd;
    
	//Conditional bean that might be null if API portal is enabled.
    @Autowired(required = false)
	private TokenStore tokenStore;
    
    private Integer              maximumSessions=1;
    
    // ~ Static fields/initializers
    // =====================================================================================
    private static final String SPRING_SECURITY_FORM_CAPTCHA_KEY = "j_captcha";

    // ~ Constructors
    // ===================================================================================================

    public CustomUsernamePasswordAuthenticationFilter() {
        super();
    }

    // ~ Methods
    // ========================================================================================================
   @Override
    @MonitoredWithSpring(name = "CUPAF_VERIFY_AUTHENTICATION")
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        
        checkUserAlreadyLoggedIn(request,response);
      
       // check if  request has no valid HttpSession
       if (request.getSession(false) == null) {
           throw new AuthenticationServiceException("currentSessionExpired");
       }
        
        String userName = obtainUsername(request);        
        String sourceSystem = userService.getUserSourceSystemByUsername(userName);        
        if(!StringUtils.isNotBlank(sourceSystem)){
              throw new AuthenticationServiceException("User's source system is not available");
        }
        // if sourceSystem does not belong to 'db or 'ldap'
        if(sourceSystem!=null && !sourceSystem.equals("db") && !sourceSystem.equals("ldap")){
            throw new AuthenticationServiceException("User does not belong to ldap or db");
        }
        
        // Source system flag set for ldap user to use into subsequent filter.
        if(UserService.SOURCE_LDAP.equalsIgnoreCase(sourceSystem)){
        	request.setAttribute("isUserSourceSystemLDAP", true);
        }
        
        //Replacing the default authenticationManager previously set with the required authentication manager from the map based on sourceSystem
        setAuthenticationManager(authenticationManagerMap.get(sourceSystem));
        
        //rest remains same as previous implementation
        boolean captchaEnabledOrgValue = captchaEnabled;
        if (!systemSetupUtil.isSystemSetup()) {
            setCaptchaEnabled(false);
        }
        AuthenticationManager authManager =null;
        String singleUserSessionExceededFlag = String.valueOf(request.getSession(false).getAttribute(LoginConstants.SINGLE_USER_SESSION_EXCEEDED_FLAG));
        if(!LoginConstants.TRUE.equals(request.getParameter(LoginConstants.IS_FORCED_LOGGED_IN_PARAMETER)) && !validateCaptcha(obtainChallengeId(request), obtainCaptcha(request),userName)){
                setAuthenticationManager(authManager);
                throw new AuthenticationServiceException("CaptchaValidationFailed");
        }
        /** Set captcha to its original value **/
        if (!systemSetupUtil.isSystemSetup()) {
            setCaptchaEnabled(captchaEnabledOrgValue);
        }

        /** Change for validating that the login time is within the branch operating Time STARTS **/        
        Authentication authentication = super.attemptAuthentication(request, response);      
        LicenseDetail licenseInformation =licenseClientCacheService.getCurrentProductLicenseDetail();
       Locale loc = RequestContextUtils.getLocale(request);
    
        if(licenseSetupUtil.isSystemSetup()&&licenseInformation!=null)
        {
              DateTimeFormatter dtf = DateTimeFormat.forPattern(DATE_FORMAT);
              DateTime licenseExpiryDate=licenseInformation.getExpiryDate();
              DateTime now=new DateTime();
              now=dtf.parseDateTime(now.toString(DATE_FORMAT));
              if(licenseInformation.getGracePeriod()!=null )
              {
            	  licenseExpiryDate=licenseExpiryDate.plusDays(licenseInformation.getGracePeriod());
              }
              
              licenseExpiryDate=dtf.parseDateTime(licenseExpiryDate.toString(DATE_FORMAT));
       if (licenseExpiryDate.isBefore(now)) {
   
              redirectToApplyNewLicense(authentication);
                     
            
       }
       DateTime licenseStartDate=licenseInformation.getStartDate();
        if (licenseStartDate!=null&&dtf.parseDateTime(licenseStartDate.toString("MM/dd/yyyy")).isAfter(now)) {
              
       
              throw  new AuthenticationServiceException(LicenseStateConstant.LICENSE_STARTDATE_INVALID);
       }
   
    
        }
        else if(licenseSetupUtil.isSystemSetup()&&!("LICENSE").equals(ProductInformationLoader.getProductCode()))
        {
    
              
              throw     new       AuthenticationServiceException(LicenseStateConstant.LICENSE_INFORMATION_NOT_AVAILABLE);
        }
        if (authentication.getPrincipal() != null) {
            UserInfo userInfo = null;
            if (UserInfo.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
                userInfo = (UserInfo) authentication.getPrincipal();
                if(userInfo != null){
                	checkIfUserAuthorisedToApplyLicense(userInfo);
        
                    
                OrgBranchInfo loggedInBranch=userInfo.getLoggedInBranch();
                if (loggedInBranch != null) {
                	
                	if(!isPrimaryBranchApprovedAndActive(loggedInBranch.getId()))
                    {
                    	 throw new AuthenticationServiceException(NO_ACTIVE_PRIMARY_BRANCH_AVAILABLE);
                    }
                	
                	Boolean result =  true;
                	if((userInfo.getUserReference().getMaintainLoginDays()==null || 
                		!userInfo.getUserReference().getMaintainLoginDays())){
                		result = organizationService.getUserLoginTimeValid(userInfo);
                	}else{
                		result = userService.getUserLoginTimeValid(userInfo);
                	}
                    if (!result) {
                        setAuthenticationManager(authManager);
                        throw new AuthenticationServiceException("branchTimeValidationFailed");
                    }
                    
                    if(LoginConstants.TRUE.equals(singleUserSessionExceededFlag) && LoginConstants.TRUE.equals(request.getParameter(LoginConstants.IS_FORCED_LOGGED_IN_PARAMETER))){ 
                    	NeutrinoSessionInformation  neutrinoSessionInformation =  sessionRegistry.getLeastRecentlyCreatedSessionInformationByUserId(userInfo.getId());
                    	//neutrinoSessionInformation should not be null here except one case mentioned below.
                    	if (neutrinoSessionInformation != null) {
                    		//this check is necessary only for a very unique case where at the time of concurrent login before forcing
                    		//logout, the same user has already logged out from his existing session. 
                    		neutrinoSessionInformation.setLogOutType(NeutrinoSessionInformation.LOGOUT_TYPE_ON_DIFF_DEVICE_BROWSER);
                            neutrinoSessionInformation.setForceLogOutIP(request.getRemoteAddr());
                            eventService.createUserSecurityTrailEventEntry(neutrinoSessionInformation, userInfo.getId(), EventTypes.USER_SECURITY_TRAIL_LOGOUT);
                            sessionRegistry.updatRegisteredSession(neutrinoSessionInformation);
     	                   	userSessionManagerService.invalidateUserSessionAndUpdateRegistry(userInfo.getId(), neutrinoSessionInformation);    
                    	}
                    	// Access token should be removed regardless of neutrinoSessionInformation is null or not 
                    	if (tokenStore != null) {
                    		((CustomOauthTokenStoreDAO) tokenStore).removeAccessTokensByUserName(userInfo.getUsername());
                    	}
                  
                  }

                }
             
                if (!userInfo.isLoginEnabled()){
                     throw new AuthenticationServiceException(USER_NON_LOGIN_CHECK);
                     
                }
                if(userInfo.getAuthorities() == null || userInfo.getAuthorities().isEmpty()){
                	throw new AuthenticationServiceException(NO_ROLES_AVAILABLE);
                }
                if(!isValidIPForLogin(userName,request)){
                    throw new AuthenticationServiceException(INVALID_LOGIN_IP_ADDRESS);
                }
              //  isUserAlreadyLoginFromIp(request.getRemoteAddr(),userInfo.getUsername());  	
                }
                if (!sessionModuleService.isSsoEnabled() && sessionModuleService.isConcurrencySwichingEnabled() && !sessionModuleService.isAllowLoginForConcurrencyMode(userInfo)) {
                    throw new AuthenticationServiceException(INVALID_LOGIN_HIGH_CONCURRENCY);
                }
            }
        }
        /** Change for validating that the login time is within the branch operating Time ENDS **/
        setAuthenticationManager(authManager);
        return authentication;
    }
   
	private boolean isPrimaryBranchApprovedAndActive(Long branchId) {
		OrganizationBranch orgBranch = organizationService.getOrganizationBranchById(branchId);
		return ApprovalStatus.APPROVED_RECORD_STATUS_LIST.contains(orgBranch.getApprovalStatus())
				&& orgBranch.isActiveFlag();
	}

		
	

	private void checkIfUserAuthorisedToApplyLicense(UserInfo userInfo) {
    	if(!userInfo.hasAuthority("APPLY_LICENSE") && !licenseSetupUtil.isSystemSetup())
        {
        	throw     new       AuthenticationServiceException("UnauthorizedToApplyLicense");
        }
		
	}

	private void redirectToApplyNewLicense(Authentication authentication) {
    	 
    	  if (UserInfo.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
    		  UserInfo      userInfo = (UserInfo) authentication.getPrincipal();
              if(userInfo != null &&!userInfo.hasAuthority("APPLY_LICENSE")){
                  
                  	throw     new       AuthenticationServiceException("UnauthorizedToApplyLicense");
                  
              }
    	  }
    	  
    	  
    	  
}

	private void checkUserAlreadyLoggedIn(HttpServletRequest request, HttpServletResponse response) {
      Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
      if(authentication!=null && authentication.getPrincipal() !=null && UserInfo.class.isAssignableFrom(SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass())){
          try{
          response.sendRedirect(request.getContextPath() + APP_AUTH_LOGIN);
          ExceptionUtility.rethrowSystemException(new SystemException("User already Logedin"));
          }catch(IOException e)
          {
            ExceptionUtility.rethrowSystemException(e);
          }
       }      
    }

    protected String obtainCaptcha(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_FORM_CAPTCHA_KEY);
    }

    protected String obtainChallengeId(HttpServletRequest request) {
        return request.getSession().getId();
    }

    protected boolean validateCaptcha(String challengeId, String captcha,String userName) {
        if (!captchaEnabled || userService.isBotUser(userName)) {
            return true;
        }
        try {
            if (StringUtils.isNotEmpty(challengeId) && StringUtils.isNotEmpty(captcha)) {                   
                return captchaService.validateResponseForID(challengeId, captcha);
            } else {
                return false;
            }
        } catch (CaptchaServiceException e) {
            throw new AuthenticationServiceException("CaptchaServiceException occurred : " + e.getMessage(), e);
        }
    }

    /**
     * @param captchaEnabled the captchaEnabled to set
     */
    public void setCaptchaEnabled(boolean captchaEnabled) {
        this.captchaEnabled = captchaEnabled;
    }

    public boolean isCaptchaEnabled() {
        return captchaEnabled;
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
       String password = obtainPasswordFromSession(request);
       if(password == null){
               String providedpwd = super.obtainPassword(request);
            String passPhraseVal = (String) request.getSession(false).getAttribute(PASS_PHRASE);
            password =  decryptPass(providedpwd, passPhraseVal);
           
            if(isSingleSessionAllowed()){
                   request.getSession(false).setAttribute(LoginConstants.SESSION_PASSWORD_PARAMETER,password);
            }
       }else if(!LoginConstants.TRUE.equals(request.getParameter(LoginConstants.IS_FORCED_LOGGED_IN_PARAMETER))){
              String providedpwd = super.obtainPassword(request);
                  String passPhraseVal = (String) request.getSession(false).getAttribute(PASS_PHRASE);
                  String requestParamPassword =  decryptPass(providedpwd, passPhraseVal);
                 password = requestParamPassword;
       }
       
       return password;
    }

    @Override
       protected String obtainUsername(HttpServletRequest request) {
              String username = obtainUsernameFromSession(request);
              if(username == null){
                     username = super.obtainUsername(request);
                     if(isSingleSessionAllowed()){
                         request.getSession(false).setAttribute(LoginConstants.SESSION_USERNAME_PARAMETER,username);
                  }
       }else if(!LoginConstants.TRUE.equals(request.getParameter(LoginConstants.IS_FORCED_LOGGED_IN_PARAMETER))){
              String requestParamUsername = super.obtainUsername(request);
              username = requestParamUsername;
       }
              return username;
       }

    public String decryptPass(String providedpwd, String passPhraseVal) {
    	 try {
    		 providedpwd = providedpwd.replaceAll("\\+", "%2B");
    		 providedpwd = URLDecoder.decode(providedpwd, "UTF-8");
    		} catch (UnsupportedEncodingException e) {
    			BaseLoggers.flowLogger.error("Error occurred while decoding password with URLDecoder!!!" + e.getMessage());
    			
    		}
       return AesUtil.decrypt(providedpwd, passPhraseVal,"Y".equalsIgnoreCase(webClientToEncryptpwd));
    }

    public Map<String, AuthenticationManager> getAuthenticationManagerMap() {
        return authenticationManagerMap;
    }

    public void setAuthenticationManagerMap(Map<String, AuthenticationManager> authenticationManagerMap) {
        this.authenticationManagerMap = authenticationManagerMap;
    }
    
    
    private String obtainUsernameFromSession(HttpServletRequest request) {
              HttpSession session = request.getSession(false);
              if(session != null){
                     return (String) session.getAttribute(LoginConstants.SESSION_USERNAME_PARAMETER);
              }
              return null;
       }
       
       private String obtainPasswordFromSession(HttpServletRequest request) {
              HttpSession session = request.getSession(false);
              if(session != null){
                     return (String) session.getAttribute(LoginConstants.SESSION_PASSWORD_PARAMETER);
              }
              return null;
              
       }
       
       private boolean isSingleSessionAllowed() {
          return LoginConstants.SINGLE_SESSION.equals(maximumSessions);
       }

    private boolean isValidIPForLogin(String userName,HttpServletRequest request)
    {
        String ipAddressOrLocal=null;
        URL url=null;
        if(request!=null) {
            try {
                url=new URL(request.getRequestURL().toString());
            } catch (MalformedURLException e) {

            }
        }


        if(url!=null && url.getHost()!=null){
            ipAddressOrLocal=url.getHost();
        } else {
            InetAddress ip;
            try {

                ip = InetAddress.getLocalHost();
                ipAddressOrLocal = ip.getHostAddress();

            } catch (UnknownHostException e) {
                BaseLoggers.flowLogger.error("User IP not recognized,hence,redirecting to login");
                return true;

            }
        }
        if(ipAddressOrLocal.equals("localhost")){
            return true;
        }


        User user=userService.findUserByUsername(userName);
        UserProfile userProfile=userService.getUserProfile(user);
        AccessType accessType=userProfile.getUserAccessType();
        HibernateUtils.initializeAndUnproxy(userProfile.getAddressRange());
        String userIP=request.getRemoteAddr();
        String[] userIpParts=StringUtils.split(userIP, '.');
        if(userProfile.getAddressRange()!=null){
            if(userProfile.getAddressRange().getSecuredIp()!=null && userProfile.getAddressRange().getSecuredIp()
                    && userProfile.getUserAccessType()!=null){

                List<IpAddress> ipAddressList=userService.getIPAddress(userIP,accessType);
                if(CollectionUtils.isNotEmpty(ipAddressList)){
                    for(IpAddress ipAddress :ipAddressList){
                        String[]ipParts = StringUtils.split(ipAddress.getIpAddress(), '.');
                        if((ipParts[0].equals(userIpParts[0]) || ipParts[0].equals("*"))
                                && (ipParts[1].equals(userIpParts[1]) || ipParts[1].equals("*"))
                                && (ipParts[2].equals(userIpParts[2])|| ipParts[2].equals("*"))
                                && (ipParts[3].equals(userIpParts[3])|| ipParts[3].equals("*"))){
                            return true;
                        }
                    }
                    return false;
                }else{
                    return false;
                }
            }
        }
        return true;
    }
    
    
    private void isUserAlreadyLoginFromIp(String ipaddress,String userName){
 	   if(ipaddress == null){
 		   return;
 	   }
 	   ConfigurationService configurationService=NeutrinoSpringAppContextUtil.getBeanByName(
 		        "configurationService", ConfigurationService.class);
 	   ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(
		        SystemEntity.getSystemEntityId(), "config.system.OneIpOneUser.enable");
 	   if(configVo!=null && Boolean.valueOf(configVo.getPropertyValue())){
 		   NeutrinoSessionInformation sessionInfo = sessionRegistry.getSessionByLogginIP(ipaddress);
 		   if(sessionInfo == null){
 			   return ;
 		   }
 		   UserInfo userInfo = (UserInfo) sessionInfo.getPrincipal();
 		   if(!userInfo.getUsername().equalsIgnoreCase(userName)){
 			   throw new AuthenticationServiceException(LoginConstants.ONE_USER_ONE_IP_CHECK);
 		   }
 	   }
    }
}


