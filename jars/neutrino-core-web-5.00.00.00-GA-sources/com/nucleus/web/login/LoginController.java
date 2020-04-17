package com.nucleus.web.login;



import static com.nucleus.web.login.LoginConstants.INVALID_LOGIN_HIGH_CONCURRENCY;
import static com.nucleus.web.login.LoginConstants.INVALID_LOGIN_IP_ADDRESS;
import static com.nucleus.web.login.LoginConstants.NO_ROLES_AVAILABLE;
import static com.nucleus.web.login.LoginConstants.USER_NON_LOGIN_CHECK;
import static com.nucleus.web.login.LoginConstants. NO_ACTIVE_PRIMARY_BRANCH_AVAILABLE;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.nucleus.password.reset.ResetPasswordEmailHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.Sanselan;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.license.cache.BaseLicenseService;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.license.utils.LicenseSetupUtil;
import com.nucleus.license.utils.LicenseStateConstant;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.sso.password.SsoResetPasswordService;
import com.nucleus.systemSetup.service.SystemSetupService;
import com.nucleus.user.OutOfOfficeDetails;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSecurityQuestion;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;
import com.nucleus.web.common.CommonConfigUtility;
import com.nucleus.web.security.AesUtil;
import com.nucleus.web.security.InvalidLoginTokenException;
import com.nucleus.web.security.SystemSetupUtil;
import com.nucleus.web.useradministration.UserAdminController;
import com.octo.captcha.service.image.ImageCaptchaService;
/**
 * Handles and retrieves the login page depending on the URI template
 */
@Controller
@RequestMapping("/auth")
public class LoginController {

    @Inject
    @Named("captchaService")
    private ImageCaptchaService          captchaService;

    @Inject
    @Named("userService")
    protected UserService                userService;

    @Inject
    @Named("mailService")
    private MailService                  mailService;

    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService    userSessionManagerService;

    @Inject
    @Named("messageSource")
    protected MessageSource              messageSource;

    @Inject
    @Named("authenticationTokenService")
    protected AuthenticationTokenService authenticationTokenService;

    @Inject
    @Named("baseMasterService")
    protected BaseMasterService          baseMasterService;

    @Inject
    @Named("configurationService")
    protected ConfigurationService       configurationService;
    
    @Inject
    @Named("licenseClientCacheService")
   private  BaseLicenseService licenseClientCacheService;
    
    
    @Inject
    @Named("ssoResetPasswordService")
    private SsoResetPasswordService ssoResetPasswordService;
    
    @Inject
    @Named("commonConfigUtility")
    private CommonConfigUtility commonConfigUtility;

    @Inject
    @Named(value = "genericParameterService")
    private GenericParameterService genericParameterService;

    @Inject
    @Named("resetPasswordEmailHelper")
    private ResetPasswordEmailHelper resetPasswordEmailHelper;

    @Inject
    private UserAdminController adminController;

    private static final String           CAPTCHA_VALIDATION_FAILED     = "login.controller.captcha.validation.failed";

    private static final String           USER_ALREADY_LOGGED           = "login.controller.user.already.logged";

    private static final String           INVALID_CREDENTIALS           = "login.controller.invalid.credentials";
    
    private static final String           RESET_PASSWORD_NOT_ALLOWED_LDAPUSER  = "label.error.reset.password.not.allowed.ldapuser";
    
    private static final String           BRANCH_TIME_VALIDATION_FAILED = "login.controller.invalid.login.time";

    private static final String           NOT_WORKING_DAY               = "login.controller.invalid.working.day";
    
    private static final String 		  USER_IS_NON_LOGIN				= "login.controller.user.nonlogin";
    
    private static final String 		  NO_ROLE_AVAILABLE				= "login.controller.user.noRolesAvailable";
    
    private static final String			  INVALID_PAGE_SESSION 		    = "ERR.PAGE.EXPIRED.MSG";
    
    private static final String 		  INVALID_CURRENT_SESSION 	    = "ERR.INVALIDSESSION.MSG";
    
    private static final String 		  AUTHENTICATION_SUCCESS 	    = "onAuthenticationSuccess";
    
    private static final String ERROR = "error";
    
	private static final String           USER_DISABLED          	     = "login.controller.user.status.inactive.disabled";
    
	private static final String 		  USER_LOCKED_LDAP			      ="login.controller.user.status.blocked.ldap";
	
	private static final String 		LDAP_USER_PASSWORD_EXPIRED 		= "login.controller.ldap.user.password.expired";

    private static final String 		  INVALID_USER_IP_ADDRESS				= "login.controller.user.invalid.user.ip.address";

    private static final String 		  INVALID_LOGIN_HIGH_CONCURRENCY_MSG				= "login.controller.user.low.priority";

    private static final String 		  NO_ACTIVE_PRIMARY_BRANCH_AVAILABLE_MSG				= "login.controller.user.no.active.branch";
    @Value("${core.web.config.token.validity.time.millis}")
    private String                       tokenValidityTimeInMillis;

    @Value(value = "#{'${system.sso.enable}'}")
    private String                       ssoEnableFlag;

    @Value(value = "#{'${system.forgotPassword.mail.from}'}")
    private String                       forgotPasswordFromMail;

    @Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil              systemSetupUtil;

    @Autowired
    private LicenseSetupUtil             licenseSetupUtil;
    
    @Inject
    @Named("systemSetupService")
    private SystemSetupService          systemSetupService;
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    /**
     * Handles and retrieves the login JSP page
     * 
     * @return the name of the JSP page
     */    
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage(@RequestParam(value = "error", required = false) boolean error,
            @RequestParam(value = "success", required = false) boolean success, ModelMap model, 
            @RequestParam(value = "ldapUserCredentialExpired", required = false) boolean ldapUserCredentialExpired,
            HttpServletRequest request) {
        BaseLoggers.flowLogger.debug("Received request to show login page");
       boolean isForcedLoginSessionAttributesToBeClear = true;
        if (request.getSession().getAttribute(AUTHENTICATION_SUCCESS) != null) {
			request.getSession().removeAttribute(AUTHENTICATION_SUCCESS);
		}
              
        if (!systemSetupUtil.isSystemSetup()) {
        		if(systemSetupUtil.isSystemSetUpInProgress()){
        			BaseLoggers.flowLogger.debug("Detected that system setup is in progress.");
        	        return "redirect:" + systemSetupUtil.getSystemSetUpInProgressUrl();
        		}else{
        			BaseLoggers.flowLogger.debug("Detected that system is not setup. Redirecting to system setup module..");
        	        return "redirect:" + systemSetupUtil.getLoginFormUrlForSetup();
        		}          
        }

        if (systemSetupUtil.isSystemSetup() && !licenseSetupUtil.isSystemSetup()) {
            BaseLoggers.flowLogger.debug("Detected that system is not setup. Redirecting to system setup module..");
            return "redirect:" + licenseSetupUtil.getLicenseSetUpUrl();
        }

        boolean isLoggedIn = false;
        boolean outOfOficeFlag = false;
        UserInfo user = userSessionManagerService.getLoggedinUserInfo();
        isLoggedIn = user != null;
        if (user != null) {
            User userObj = userService.findUserByUsername(user.getUsername());
            OutOfOfficeDetails outOfOffice = userObj.getOutOfOfficeDetails();
            if (outOfOffice != null) {
                outOfOficeFlag = outOfOffice.isOutOfOffice();
            }
        }
        if (isLoggedIn && !outOfOficeFlag) {
            BaseLoggers.flowLogger.debug("User: " + user.getUsername() + " is already logged in.");
            return "redirect:loggedin";
        }

        // Add an error message to the model if login is unsuccessful
        // The 'error' parameter is set to true based on the when the
        // authentication has failed.
        // We declared this under the authentication-failure-url attribute
        // inside the security-context.xml

        if (error) {
        	BaseLoggers.flowLogger
            .error("Login called with error parameter set to true.Extracting SPRING_SECURITY_LAST_EXCEPTION from session.");
    AuthenticationException exception = (AuthenticationException) request.getSession().getAttribute(
            "SPRING_SECURITY_LAST_EXCEPTION");
    BaseLoggers.flowLogger.error(
            "Exception(extracted from SPRING_SECURITY_LAST_EXCEPTION) occured while Logging In", exception);
    // Assign an error message
    if (exception != null && InvalidLoginTokenException.class.isAssignableFrom(exception.getClass())) {
        model.put("error", "Invalid Login Token");
    }else if (ldapUserCredentialExpired == true) {
		model.put("error", messageSource.getMessage(LDAP_USER_PASSWORD_EXPIRED, null, request.getLocale()));
	} else if (exception != null && LockedException.class.isAssignableFrom(exception.getClass())) {
		if (exception.getMessage().contains("AbstractUserDetailsAuthenticationProvider.locked.ldap")) {
			model.put("error", messageSource.getMessage(USER_LOCKED_LDAP, null, request.getLocale()));

		} else {
        String username = null;
        UserInfo userInfo = null;
        User currentUser = null;
        
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
        	 username = ((UserDetails)principal).getUsername();
        } else {
        	 username = principal.toString();
        }
        
		
        userInfo = userService.getUserFromUsername(username);
        
        if (userInfo != null)
            currentUser = baseMasterService.getMasterEntityById(User.class, userInfo.getId());
        if (currentUser != null) {
            Map<String, ConfigurationVO> conf = configurationService.getFinalConfigurationForEntity(SystemEntity.getSystemEntityId());
            if (currentUser.getNumberOfFailedLoginAttempts() >= Integer.parseInt(conf.get(
                    "config.user.allowed.failedPasswordResetAttempts").getPropertyValue()))
                model.put("error",
                        "As per Company Policy You are blocked because of more failed Login attempts than allowed .Please contact the Administrator");
           
            else
                model.put("error", "User is blocked .Please contact the Administrator");
        }

            model.put("askSecurityQuestions", true);

            String unblock = userService.showUnblockLink((String) request.getSession(false).getAttribute("sessionUsernameParameter"));
            boolean show = (unblock == null || unblock.equalsIgnoreCase("NO"))?false:true;
			if (!show) {
				model.put("error", "User is blocked. Please contact the Administrator");
				model.put("askSecurityQuestions", show);
			}


    }} else {
        if (exception != null && exception.getMessage() == null) {
            BaseLoggers.flowLogger.error("Exception occured while Logging In");
            BaseLoggers.flowLogger.error(ExceptionUtils.getFullStackTrace(exception));
        } else if (exception != null && exception.getMessage().equals("CaptchaValidationFailed")) {
            model.put("error", messageSource.getMessage(CAPTCHA_VALIDATION_FAILED, null, request.getLocale()));
        }else if (exception != null && exception.getMessage().equals("currentSessionExpired")) {
            model.put("error", messageSource.getMessage(INVALID_PAGE_SESSION, null, request.getLocale()));
        }
        
        else if (exception != null && (exception.getMessage().contains("AbstractUserDetailsAuthenticationProvider.disabled")
				&& exception.getClass().equals(DisabledException.class))) {
			model.put("error", messageSource.getMessage(USER_DISABLED, null, request.getLocale()));
		}else if (exception != null&& (exception.getMessage().contains("ConcurrentSessionControlStrategy.exceededAllowed") && exception.getClass().equals(
                        SessionAuthenticationException.class))) {
            isForcedLoginSessionAttributesToBeClear = false;
            model.put("error", messageSource.getMessage(USER_ALREADY_LOGGED, null, request.getLocale()));
        } 
        
        else if (exception != null&&exception.getMessage().contains("label.license.concurrent.user.exceeds")&& exception.getClass().equals(SessionAuthenticationException.class)) {
  
        model.put("error", messageSource.getMessage("label.license.concurrent.user.exceeds", null, request.getLocale()));
        }    
        
        else if (exception != null&&exception.getMessage().contains(LicenseStateConstant.LICENSE_STARTDATE_INVALID))
        {
        	
        model.put("error", messageSource.getMessage("label.license.status.LICENSE_START_DATE_INVALID", null, request.getLocale()));
    		
	    }
        else if (exception != null&&exception.getMessage().contains(LicenseStateConstant.LICENSE_INFORMATION_NOT_AVAILABLE))
        {
        	
        model.put("error", messageSource.getMessage("label.license.empty.license.string", null, request.getLocale()));
    		
	    }
        else if (exception != null&&exception.getMessage().contains("UnauthorizedToApplyLicense"))
        {
        	
        	model.put("error", messageSource.getMessage("label.license.status.unauthorized.apply.license", null, request.getLocale()));
    		
        }
    	
				else if (request.getParameter("errCode") != null
						&& !("".equals(request.getParameter("errCode").trim()))) {
					prepareErrorMessageForInvalidSession(request,model);
				} else if (exception != null && exception.getMessage().equals("branchTimeValidationFailed")) {
            model.put("error", messageSource.getMessage(BRANCH_TIME_VALIDATION_FAILED, null, request.getLocale()));
        } else if (exception != null && exception.getMessage().equals("notWorkingDay")) {
            model.put("error", messageSource.getMessage(NOT_WORKING_DAY, null, request.getLocale()));
        } else if (exception != null && exception.getMessage().equals(USER_NON_LOGIN_CHECK)){
        	model.put("error", messageSource.getMessage(USER_IS_NON_LOGIN, null, request.getLocale()));
        } else if (exception != null && exception.getMessage().equals(NO_ROLES_AVAILABLE)){
        	model.put("error", messageSource.getMessage(NO_ROLE_AVAILABLE, null, request.getLocale()));
        }
        else if (exception != null && exception.getMessage().equals(NO_ACTIVE_PRIMARY_BRANCH_AVAILABLE)){
        	model.put("error", messageSource.getMessage(NO_ACTIVE_PRIMARY_BRANCH_AVAILABLE_MSG, null, request.getLocale()));
        }
        
        
        else if (exception != null && exception.getMessage().equals(INVALID_LOGIN_IP_ADDRESS)){
            model.put("error", messageSource.getMessage(INVALID_USER_IP_ADDRESS, null, request.getLocale()));
        }else if(exception != null && exception.getMessage().equals(LoginConstants.ONE_USER_ONE_IP_CHECK)){
        	model.put("error", messageSource.getMessage("oneUserOneIp.exceed", new Object[]{request.getRemoteHost()},
        			"Another User Already LoggedIn from {0}",request.getLocale()));
        }else if (exception != null && exception.getMessage().equals(INVALID_LOGIN_HIGH_CONCURRENCY)){
            model.put("error", messageSource.getMessage(INVALID_LOGIN_HIGH_CONCURRENCY_MSG, null, request.getLocale()));
        }
        else {
            model.put("error", messageSource.getMessage(INVALID_CREDENTIALS, null, request.getLocale()));
        }
    }
        }
    if (isForcedLoginSessionAttributesToBeClear) {
        	clearSessionAttributesOnForcedLogin(request.getSession(false));
        }
        String userAgent = request.getHeader("user-agent");
        model.put("address", userAgent);
        model.put("loginPage", "loginPage");
        model.put("authenticationMode", "dual"); 
        checkExpiryValidationOfLicense(model,request);
        // This will resolve to /WEB-INF/jsp/loginpage.jsp

        
     
        return "loginpage";

    }

	private void checkExpiryValidationOfLicense(ModelMap model, HttpServletRequest request) {

		LicenseDetail licenseDetailObj = licenseClientCacheService.getCurrentProductLicenseDetail();
		if (licenseDetailObj == null) {
			return;
		}
		
		DateTimeFormatter dtf = DateTimeFormat.forPattern(DATE_FORMAT);
		DateTime licenseExpiryDate = licenseDetailObj.getExpiryDate();

		DateTime now = new DateTime();
		now = dtf.parseDateTime(now.toString(DATE_FORMAT));

		if (licenseDetailObj.getGracePeriod() != null) {
			licenseExpiryDate = licenseExpiryDate.plusDays(licenseDetailObj.getGracePeriod());

		}
		licenseExpiryDate = dtf.parseDateTime(licenseExpiryDate.toString(DATE_FORMAT));
		if (licenseExpiryDate.isBefore(now)) {
			model.put("licenseGraceExpired", messageSource.getMessage("label.license.status.LICENSE_DATE_GRACE_EXPIRED",
					null, request.getLocale()));

		}

	}
    

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String getLogoutPage(ModelMap model,  HttpServletRequest request) {
        BaseLoggers.flowLogger.debug("Received request to show Logout page");
     
        if (ssoEnableFlag.equals("true")) {
            model.put("ssoEnableFlag", "1");
        } else {
            model.put("ssoEnableFlag", "0");
        }

        model.put("logout", "logout");
        model.put("loginPage", "loginPage");
        
        HttpSession session =request.getSession(false);
        if(session!=null){
        	
        	if(NeutrinoSessionInformation.LOGOUT_TYPE_BY_PAGE_REFRESH.equals(session.getAttribute(NeutrinoSessionInformation.LOGOUT_TYPE_BY_PAGE_REFRESH))){
            	BaseLoggers.flowLogger.error("Logging out user due to page refresh");
            	model.put(ERROR, messageSource.getMessage("logoutby.page.refresh", null,request.getLocale()));
            	session.removeAttribute(NeutrinoSessionInformation.LOGOUT_TYPE_BY_PAGE_REFRESH);
        	}else if(NeutrinoSessionInformation.LOGOUT_TYPE_BY_INVALID_CONFIGURATION.equals(session.getAttribute(NeutrinoSessionInformation.LOGOUT_TYPE_BY_INVALID_CONFIGURATION))){
            	BaseLoggers.flowLogger.error("Logging out user due to invalid configuartion. ResponseNoCachingFilter was not enabled");
            	model.put(ERROR, messageSource.getMessage("logoutby.incorrect.config", null,request.getLocale()));
            	session.removeAttribute(NeutrinoSessionInformation.LOGOUT_TYPE_BY_INVALID_CONFIGURATION);
        	}
        	
        }
        return "logout";
    }

    @RequestMapping(value = "/getCaptchaPage", method = RequestMethod.GET)
    public String getCaptchaPage(ModelMap model) {
        BaseLoggers.flowLogger.debug("Received request to show Captcha page");
        return "captcha";
    }

    @RequestMapping(value = "/getCaptcha", method = RequestMethod.GET)
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String captchaId = request.getSession().getId();

        BufferedImage challenge = captchaService.getImageChallengeForID(captchaId, request.getLocale());
        request.getSession().setAttribute("Id", captchaId);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/png");
        ServletOutputStream responseOutputStream = response.getOutputStream();
        Sanselan.writeImage(challenge, responseOutputStream, ImageFormat.IMAGE_FORMAT_PNG, null);
        responseOutputStream.flush();
        responseOutputStream.close();
        return null;
    }

    @RequestMapping(value = "/forgotPswd")
    public @ResponseBody
    Map<String, String> forgotPassword(@RequestParam("mailId") String userEmailId, @RequestParam("username") String userName,
            HttpServletRequest request) {
    	Map<String, String> messageMap = new HashMap<String, String>();
    	
    	if(commonConfigUtility.getSsoActive()){
    		return ssoResetPasswordService.sendForgotPasswordMail(userName, userEmailId);
    	}
    	
        User lstUser = userService.userExistenceInForgotPassword(userName, userEmailId);
        if (lstUser != null) {
        	String sourceSystem = lstUser.getSourceSystem();
			if (!"db".equals(sourceSystem)) {
				messageMap.put("ldapMessage", messageSource.getMessage(RESET_PASSWORD_NOT_ALLOWED_LDAPUSER,new String[]{lstUser.getUsername()} , request.getLocale()));
			} else {
				String timeToken = this.authenticationTokenService.generatePasswordResetTokenForUser(lstUser,
	                    this.tokenValidityTimeInMillis);
	            String serverName = request.getServerName();
	            if (serverName == null) {
	                serverName = request.getLocalAddr();
	            }
	            int serverPort = request.getServerPort();
	            BaseLoggers.flowLogger.debug("User updated with Token ID and reset Password Time Stamp");
	            String subject = resetPasswordEmailHelper.getForgotPasswordEmailSubject();
	            String fromEmaiId = getForgotPasswordFromMail();

                Map<String, Object> dataMap = new HashMap<>();
                String tokenLink = systemSetupService.getSystemHttpProtocol()
                        + "://"
                        + serverName
                        + ":"
                        + serverPort
                        + request.getContextPath()
                        + "/app/UserInfo/resetPasswordSecurityQuestion/"
                        + userName
                        + "/"
                        + timeToken;

                dataMap.put("resetPasswordLink",tokenLink);
                dataMap.put("userName",lstUser.getDisplayName());
                dataMap.put("tokenValidityMinites",Long.parseLong(this.tokenValidityTimeInMillis)/60000);

                String htmlBody = resetPasswordEmailHelper.getEmailBody(dataMap,"forgotPasswordEmail.vm");

	            BaseLoggers.flowLogger.debug("Sending Mail to the Registered User Id....");
	            mailService.sendMail(htmlBody, subject, userEmailId, fromEmaiId);
	            BaseLoggers.flowLogger.debug("Mail Sent to the Registered Email ID");
	            messageMap.put("message", "success");
			}
        } else {
        	 messageMap.put("message", "failure");
        }
        return messageMap;
    }

    @RequestMapping(value = "/loggedin", method = RequestMethod.GET)
    public String getLoggedInPage(ModelMap model) {
        BaseLoggers.flowLogger.debug("Received request to show loggedin page");
        model.put("loggedin", "loggedin");
        return "loggedin";
    }

    public String getForgotPasswordFromMail() {
        return forgotPasswordFromMail;
    }

    public void setForgotPasswordFromMail(String forgotPasswordFromMail) {
        BaseLoggers.flowLogger.info("Setting forgotPasswordFromMail to {}", forgotPasswordFromMail);
        this.forgotPasswordFromMail = forgotPasswordFromMail;
    }
    
    @RequestMapping(value = "/getEncryptionParameters", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> listAdditionalDataTransactionType() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			 resultMap.put("iv", AesUtil.getIv());
			 resultMap.put("salt", AesUtil.getSalt());
			 resultMap.put("keysize", AesUtil.getKeysize());
			 resultMap.put("iterationcount", AesUtil.getIterationCount());
		} catch (Exception ex) {
			BaseLoggers.exceptionLogger.error("Exception: " + ex.getMessage(),
					ex);
		}
		return resultMap;
	}
    
    private void clearSessionAttributesOnForcedLogin(HttpSession session) {
		if(session != null){
			BaseLoggers.flowLogger.info("Removing session attribute values before redirecting to login page.");
			session.removeAttribute(LoginConstants.SESSION_USERNAME_PARAMETER);
			session.removeAttribute(LoginConstants.SESSION_PASSWORD_PARAMETER);
			session.removeAttribute(LoginConstants.SINGLE_USER_SESSION_EXCEEDED_FLAG);
		}		
	}

	private void prepareErrorMessageForInvalidSession(HttpServletRequest request, ModelMap model) {
		if (ValidatorUtils.notNull(request.getParameter("logoutByAdmin"))) {
			if ("true".equalsIgnoreCase(request.getParameter("logoutByAdmin"))) {

				model.put("error", messageSource.getMessage("forced.logoutby.admin",
						new String[] { request.getParameter("loggedOutBY"), request.getParameter("forcedLogoutIP")},
						request.getLocale()));
			} else {
				model.put("error", messageSource.getMessage("forced.logoutby.concurrentSession",
						new String[] { request.getParameter("forcedLogoutIP")}, request.getLocale()));

			}

		} else {
			model.put("error", messageSource.getMessage(INVALID_CURRENT_SESSION, null, request.getLocale()));
		}

	}

    @RequestMapping(value = "/forgotPassword")
    public String userForgotPassword(@RequestParam("mailId") String userEmailId, @RequestParam("username") String userName,
                                     HttpServletRequest request,ModelMap map) {

        User lstUser = userService.userExistenceInForgotPassword(userName, userEmailId);
        if (lstUser != null) {
            String sourceSystem = lstUser.getSourceSystem();
            if (!"db".equals(sourceSystem)) {
                map.put("ldapMessage", messageSource.getMessage(RESET_PASSWORD_NOT_ALLOWED_LDAPUSER, new String[]{lstUser.getUsername()}, request.getLocale()));
            } else {
                String timeToken = this.authenticationTokenService.generatePasswordResetTokenForUser(lstUser,
                        this.tokenValidityTimeInMillis);
                List<UserSecurityQuestion> userSecurityQuestionList = null;

                userSecurityQuestionList = userService.getUserSecurityQuestions(userName);

                if (CollectionUtils.isNotEmpty(userSecurityQuestionList)) {
                    userSecurityQuestionList = genericParameterService.retrieveTypes(UserSecurityQuestion.class);
                    map.put("message", "success");
                    map.put("userSecurityQuestionList", userSecurityQuestionList);
                    map.put("username", userName);
                    map.put("token", timeToken);
                } else {
                    Map<String, String> mapKeys = new HashMap<String, String>();
                    mapKeys.put("USER", lstUser.getDisplayName());
                    try {
                        adminController.mailhelper(mapKeys, lstUser, timeToken, request);
                        map.put("message", "NoSecurityQuestionFound");
                    } catch (NullPointerException e) {
                        map.put("message", "Error,User's E-mail Id not provided. Password can not be reset !!,error");
                    } catch (MessagingException e) {
                        map.put("message", "Error,Mesage Exception Ocurred !!,error");
                    } catch (IOException e) {
                        map.put("message", "Error,IO Exception Ocurred !!,error");
                    }

                }
            }
        } else {
            map.put("message", "failure");
        }
        return "resetSecurityQuestionMainPage";
    }
}
