/**
 * /* This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights
 * reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */

package com.nucleus.web.useradministration;

import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.authenticationToken.PasswordResetToken;
import com.nucleus.businessmapping.service.BusinessMappingServiceCore;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.cas.businessmapping.UserManagementService;
import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.team.managementService.TeamManagementService;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.EventBus;
import com.nucleus.event.EventService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.mail.MimeMailMessageBuilder;
import com.nucleus.master.BaseMasterService;
import com.nucleus.menu.MenuEntity;
import com.nucleus.password.reset.ResetPasswordEmailHelper;
import com.nucleus.passwordpolicy.service.PasswordValidationService;
import com.nucleus.process.BPMNProcessService;
import com.nucleus.reason.BlockReason;
import com.nucleus.reason.ReasonVO;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;
import com.nucleus.security.core.session.NeutrinoSessionRegistryImpl;
import com.nucleus.systemSetup.service.SystemSetupService;
import com.nucleus.template.TemplateService;
import com.nucleus.user.*;
import com.nucleus.web.master.CommonFileIOMasterGridLoad;
import com.nucleus.web.security.CustomUsernamePasswordAuthenticationFilter;
import com.nucleus.web.security.SystemSetupUtil;
import com.nucleus.web.usermgmt.UserManagementForm;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nucleus.web.security.AesUtil.PASS_PHRASE;

/**
 * @author Nucleus Software India Pvt Ltd
 * @description This Class is used for User Management This Controller Extends
 *              Master Controller and Override Some Function of Master
 *              Controller to load data in case the bean is not an entity or not
 *              extending to base master entity.
 * 
 * 
 */
@Transactional
@Controller
@RequestMapping(value = "/UserInfo")
public class UserAdminController extends UserAdminBaseController {
	
	@Inject
    @Named("templateService")
    protected TemplateService             templateService;

    @Inject
    @Named("teamService")
    private TeamService                   teamService;

    @Inject
    @Named("teamManagementService")
    private TeamManagementService         teamManagementService;

    @Inject
    @Named("userService")
    private UserService                   userService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService             baseMasterService;

    @Inject
    @Named("userManagementService")
    private UserManagementService     userManagementService;

    @Inject
    @Named("businessMappingServiceCore")
    private BusinessMappingServiceCore    businessMappingServiceCore;

    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService     userSessionManagerService;

    @Inject
    @Named("eventService")
    private EventService                  eventService;

    @Inject
    @Named("masterXMLDocumentBuilder")
    private CommonFileIOMasterGridLoad    commonFileIOMasterGridLoad;

    @Inject
    private OrganizationService           organizationService;

    @Inject
    @Named("configurationService")
    private ConfigurationService          configurationService;

    @Inject
    @Named("messageSource")
    protected MessageSource               messageSource;

    @Inject
    @Named("mailService")
    private MailService                   mailService;

    @Inject
    @Named("authenticationTokenService")
    private AuthenticationTokenService    authenticationTokenService;

    @Inject
    @Named("mailMessageIntegrationService")
    private MailMessageIntegrationService mailMessageIntegrationService;

    @Inject
    @Named("bpmnProcessService")
    private BPMNProcessService            bpmnProcessService;
    
    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore   userManagementServiceCore;

    @Inject
    @Named("resetPasswordEmailHelper")
    private ResetPasswordEmailHelper resetPasswordEmailHelper;

    @Autowired
    protected EventBus                    eventBus;
    
    @Inject
    @Named("systemSetupService")
    private SystemSetupService          systemSetupService;
    
    @Inject
    @Named("customUsernamePasswordAuthenticationFilter")
    private AbstractAuthenticationProcessingFilter customUsernamePasswordAuthenticationFilter;

    @Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil 					   systemSetupUtil;
    
    @Value(value = "#{'${internetchannel.mail.from}'}")
    private String                       from;

    @Inject
    @Named(value="passwordValidationService")
    private PasswordValidationService passwordValidationService;
    
    @Inject
    @Named("sessionRegistry")
    private NeutrinoSessionRegistryImpl neutrinoSessionRegistry;
    
    @Value(value = "#{'${core.web.config.SSO.ticketvalidator.url.value}'}")
	private String ssoUrl;
    
    @Inject
    @Named("sessionRegistry")
    private NeutrinoSessionRegistry sessionRegistry;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;
    
    @Inject
    @Named("userAdminHelper")
    private UserAdminHelper userAdminHelper;


    /* @Inject
     @Named("productService")
     private ProductService              productService;*/

    // private final String MSG_ERROR = "error";
    private final static String                  MSG_SUCCESS           = "success";

    private final static String                  pswdKey               = "password_pattren";
    private static final String           USER_CREATE_EVENT     = "Create";
    private static final String           USER_EDIT_EVENT       = "Edit";
	private static final String           REDIRECT_KEY	      	= "redirect:";
    private static final String           configErrorCode       = "msg.8000001";

	@PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
    @RequestMapping(value = "/activate/{userId}", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public @ResponseBody
    void activateUser(@PathVariable("userId") String[] uidList,@RequestParam(required = false) Long activeReasonId, @RequestParam(required = false) String remarks) {
        for (String uid : uidList) {
        	
            Map<String, Object> userMap = userManagementServiceCore.findUserById(Long.parseLong(uid));
            User user = (User) userMap.get("user");
            
            //save the reason so that it can be viewed on After Approval
            UserManagementForm userManagementFormForOriginalUser = new UserManagementForm();
            userManagementFormForOriginalUser.setAssociatedUser(user);
            saveUserAuditLog(userManagementFormForOriginalUser, UserConstants.USER_ACTIVATE_EVENT, activeReasonId, remarks);
            
            User changedEntity = userAdminHelper.markActivatedForMakerCheckerFlow(Long.parseLong(uid));

        	//start the work-flow
        	User updatedUser =  userManagementService.updateUserAtMakerStageSendForApproval(changedEntity, user);
        	
            //save the reason so that it can be viewed on the checker screen
            UserManagementForm userManagementForm = new UserManagementForm();
            userManagementForm.setAssociatedUser(updatedUser);
            saveUserAuditLog(userManagementForm, UserConstants.USER_ACTIVATE_EVENT, activeReasonId, remarks);
        }
        flushCurrentTransaction();        
    }

	@PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
	@RequestMapping(value = "/inActivate/{userId}", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public @ResponseBody
    void inactivateUser(@PathVariable("userId") String[] uidList,@RequestParam(required = false) Long inactiveReasonId, @RequestParam(required = false) String remarks) {
        for (String uid : uidList) {
            Map<String, Object> userMap = userManagementServiceCore.findUserById(Long.parseLong(uid));
            User user = (User) userMap.get("user");
            
            //save the reason so that it can be viewed on After Approval
            UserManagementForm userManagementFormForOriginalUser = new UserManagementForm();
            userManagementFormForOriginalUser.setAssociatedUser(user);
            saveUserAuditLog(userManagementFormForOriginalUser, UserConstants.USER_INACTIVATE_EVENT, inactiveReasonId, remarks);
            
            User changedEntity = userAdminHelper.markInActivateForMakerCheckerFlow(Long.parseLong(uid));
            
            //start the work-flow
       	 	User updatedUser =  userManagementService.updateUserAtMakerStageSendForApproval(changedEntity, user);
            
        	//save the reason so that it can be viewed on the checker screen
            UserManagementForm userManagementForm = new UserManagementForm();
            userManagementForm.setAssociatedUser(updatedUser);
            saveUserAuditLog(userManagementForm, UserConstants.USER_INACTIVATE_EVENT, inactiveReasonId, remarks);
	    
        }
    }

	@PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
	@RequestMapping(value = "/blockUser/{userId}", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public @ResponseBody
    void blockUser(@PathVariable("userId") String[] uidList, @RequestParam(required = false) Integer daysToBlock,
                   @RequestParam(required = false) Long blockReasonId, @RequestParam(required = false) String remarks) {
        for (String uid : uidList) {
            Map<String, Object> userMap = userManagementServiceCore.findUserById(Long.parseLong(uid));
            User user = (User) userMap.get("user");
            
            //save the reason so that it can be viewed on After Approval also
            UserManagementForm userManagementFormForOriginalUser = new UserManagementForm();
            userManagementFormForOriginalUser.setAssociatedUser(user);
            saveUserAuditLog(userManagementFormForOriginalUser, UserConstants.USER_BLOCK_EVENT, blockReasonId, remarks);
            
            User changedEntity = userAdminHelper.markBlockedForMakerChecker(Long.parseLong(uid), getUserDetails().getUserEntityId(), daysToBlock);
              
            //start the work-flow
       	 	User updatedUser =  userManagementService.updateUserAtMakerStageSendForApproval(changedEntity, user);
       	 
       	 	//save the reason so that it can be viewed on checker screen
            UserManagementForm userManagementForm = new UserManagementForm();
            userManagementForm.setAssociatedUser(updatedUser);
            saveUserAuditLog(userManagementForm, UserConstants.USER_BLOCK_EVENT, blockReasonId, remarks);
	    
        }
        flushCurrentTransaction();
    }

    @RequestMapping(value = "/getReason/{userId}", method = RequestMethod.GET)
    public @ResponseBody
    ReasonVO getReason(@PathVariable("userId") Long userId){
        return userManagementServiceCore.getReasonByUserId(userId);
    }

    @RequestMapping(value = "/getAllReasons", method = RequestMethod.GET)
    public @ResponseBody Map<String,String> getAllReasons(){
        return userManagementServiceCore.getAllReasons();
    }
    /**
     * Validate password with regular expression
     * 
     * @param passwordEntered
     *            password for validation
     * @return true valid password, false invalid password
     * @throws IOException
     */
    @RequestMapping(value = "/validatePassword", method = RequestMethod.POST)
    public @ResponseBody
    String validatePassword(@RequestParam("passwordEntered") String passwordEntered, @RequestParam("username") String username, HttpServletRequest request)
            throws IOException {
      
            String passwordPattern = commonFileIOMasterGridLoad.getResourceBundleFileReader(request, pswdKey);
            return userManagementServiceCore.validatePassowrd(passwordEntered, username, passwordPattern);
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
    public @ResponseBody
    String resetPasswordForAllUsers(@RequestParam("changedPassword") String changedPassword,
            @RequestParam("oldPassword") String oldPassword) {
        UserInfo ui = getUserDetails();
        return userManagementServiceCore.updateUserPassword(ui, oldPassword, changedPassword, "", true, ui.getLicenseAccepted());
    }

    @RequestMapping(value = "/resetPasswordForDBUsers", method = RequestMethod.POST)
    public @ResponseBody
    String resetPasswordForDBUsers(@RequestParam(value = "username", required = false) String username,
            @RequestParam("changedPassword") String changedPassword,
            @RequestParam(value = "oldPassword", required = false) String oldPassword,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "token", required = false) String token,
            HttpServletRequest request) {
    	User thisUser = null;
        if (userId != null && token != null) {
            Map<String, ?> thisUserDetails = userManagementServiceCore.findUserById(userId);
            thisUser = (User) thisUserDetails.get("user");
            if (!(thisUser.getPasswordResetToken().getTokenId() != null
                    && (thisUser.getPasswordResetToken().getTokenId().equals(authenticationTokenService.getEncryptedToken(token))
                    || thisUser.getPasswordResetToken().getTokenId().equals(authenticationTokenService.getEncryptedTokenMD5(token)))
                    && thisUser.getPasswordResetToken().getTokenValidity().isAfter(DateTime.now()))){
                    flushCurrentTransaction();
                    return "not a valid token or the token has expired";
            }
        }
        String decryptedChangedPassword = ((CustomUsernamePasswordAuthenticationFilter) customUsernamePasswordAuthenticationFilter)
                .decryptPass(changedPassword, (String) request.getSession(false).getAttribute(PASS_PHRASE));
        String decryptedOldPassword = ((CustomUsernamePasswordAuthenticationFilter) customUsernamePasswordAuthenticationFilter)
                .decryptPass(oldPassword, (String) request.getSession(false).getAttribute(PASS_PHRASE));
        
        if ((StringUtils.isNotBlank(decryptedChangedPassword) && (StringUtils.isNotBlank(decryptedOldPassword) || StringUtils
                .isNotBlank(token))) || (userId != null && token != null)) {
            User user = null;
            UserInfo userInfo = null;
            String sourceSystem = null;
            if (StringUtils.isNotBlank(token) && userId != null) {
                user = thisUser;
                if (user != null) {
                    userInfo = new UserInfo(user);
                    sourceSystem = user.getSourceSystem();
                }
            } else if (StringUtils.isBlank(username)) {
                userInfo = getUserDetails();
                Long uid = userInfo.getId();
                Map<String, ?> userDetails = userManagementServiceCore.findUserById(uid);
                user = (User) userDetails.get("user");
                if (user != null) {
                    sourceSystem = user.getSourceSystem();
                }
            } else {
                user = userService.findUserByUsername(username);
                if (user != null) {
                    userInfo = new UserInfo(user);
                    sourceSystem = user.getSourceSystem();
                }
            }
            if (StringUtils.isNotBlank(sourceSystem) && sourceSystem.equals(UserService.SOURCE_DB) && userInfo != null) {
                String result = userManagementServiceCore.updateUserPassword(userInfo, decryptedOldPassword, decryptedChangedPassword, token, false, userInfo.getLicenseAccepted());
                if (result.equals(MSG_SUCCESS)) {
                    if (StringUtils.isBlank(username)) {
                        BaseLoggers.flowLogger.info("Expiring all user sessions and telling user to login again");
                        NeutrinoSessionInformation neutrinoSessionInformation = (NeutrinoSessionInformation) sessionRegistry
            					.getSessionInformation(request.getSession(false).getId());
                        // this will not work when password reset is done from webservice.
                        if(neutrinoSessionInformation !=null){
	                        String serviceTicketId = neutrinoSessionInformation.getServiceTicketId();
	                        userSessionManagerService.invalidateCurrentLoggedinUserSession();
                        }
                    }
                    flushCurrentTransaction();
                    return "success";
                } else {
                    flushCurrentTransaction();
                    return result;
                }
            }
        }
        flushCurrentTransaction();
        return "failure";
    }

    @PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
    @RequestMapping(value = "/forceResetPassword/{userId}", method = RequestMethod.POST)
    public @ResponseBody
    String forceResetPassword(@PathVariable("userId") String[] uidList, HttpServletRequest request) {
        // property for mailBasedPasswordReset
        boolean mailBasedPasswordReset = Boolean.valueOf(configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), "config.passwordReset.mailBased").getPropertyValue());
        if (mailBasedPasswordReset == true) {
            for (String uid : uidList) {

                Map<String, String> mapKeys = new HashMap<String, String>();

                Map<String, Object> userMap = userManagementServiceCore.findUserById(Long.parseLong(uid));
                User user = (User) userMap.get("user");
                if(user != null && !UserService.SOURCE_DB.equals(user.getSourceSystem())){
                	continue;
                }
                mapKeys.put("USER", user.getDisplayName());

                String result = userManagementServiceCore.forceResetPassword(uid);
                if (!result.equals(MSG_SUCCESS)) {
                    try {
                        mailhelper( mapKeys, user, result, request);
                    } catch (NullPointerException e) {
                    	return "Error,User's E-mail Id not provided. Password can not be reset !!,error";
                    } catch (MessagingException e) {
                        return "Error,Mesage Exception Ocurred !!,error";
                    } catch (IOException e) {
                        return"Error,IO Exception Ocurred !!,error";
                        
                    }
                }
            }
            return "Done,User Password Reset Forced Successfully !!,success";
            }else{
            // password will be reset when the user will login again
            for (String uid : uidList) {
                Map<String, Object> userMap = userManagementServiceCore.findUserById(Long.parseLong(uid));
                User user = (User) userMap.get("user");
                if (user != null) {
                	if ("ldap".equals(user.getSourceSystem())) {
    					continue;
    				}
    				user.setForcePasswordResetOnLogin(true);
                }
            }
            return "Done,User Password Reset Forced Successfully !!,success";
        }
    }

    @PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
    @RequestMapping(value = "/sendNotification/{userId}", method = RequestMethod.POST)
    public @ResponseBody
    String sendNotificationToUser(@PathVariable("userId") String[] uidList, @RequestParam("message") String message,
            HttpServletRequest request) {
        for (String uid : uidList) {
            userManagementServiceCore.sendNotificationToUser(uid, message);
        }
        return "success";
    }
    
    @RequestMapping(value = "/resetPasswordSecurityQuestion/{userName}/{timeToken}")
    public String getResetPasswordSecurityQuestion(@PathVariable("userName") String userName,
            @PathVariable("timeToken") String timeToken, ModelMap map) {
        
    	if (StringUtils.isBlank(userName) || StringUtils.isBlank(timeToken)) {
    		return REDIRECT_KEY + systemSetupUtil.getLoginFormUrl();
    	}

    	User user = userService.findUserByPasswordResetTimeToken(timeToken);
    	if (user == null) {
    		return REDIRECT_KEY + systemSetupUtil.getLoginFormUrl();
    	}
    	
        PasswordResetToken userToken = user.getPasswordResetToken();
        
        if (userToken == null || StringUtils.isBlank(userToken.getTokenId()) ||  !userName.equalsIgnoreCase(user.getUsername()) ) {
        	return REDIRECT_KEY + systemSetupUtil.getLoginFormUrl();
        }
        
    	List<UserSecurityQuestion> userSecurityQuestionList = null;
        List<String> questionsList = new ArrayList<String>();
        if (userName != null && userName != "") {
            userSecurityQuestionList = userService.getUserSecurityQuestions(userName);
        }

        if (CollectionUtils.isNotEmpty(userSecurityQuestionList)) {
            for (UserSecurityQuestion userSecurityQuestion : userSecurityQuestionList) {
                if (userSecurityQuestion.getName() != null) {
                    questionsList.add(userSecurityQuestion.getName());
                }
            }
            map.put("userSecurityQuestionList", userSecurityQuestionList);
        }
        map.put("username", userName);
        map.put("token", timeToken);
        return "resetSecurityQuestionMainPage";
    }
    
    private void saveUserAuditLog(UserManagementForm userManagementForm, String event, Long reasonId, String remarks) {
        UserAuditLog userAuditLog = new UserAuditLog();
        userAuditLog.setUserModificationString(populateUserAuditDetails(userManagementForm));
        if (userManagementForm.getAssociatedUser() != null) {
            userAuditLog.setUserId(userManagementForm.getAssociatedUser().getId());
        }
        if (getUserDetails().getUserEntityId() != null) {
            userAuditLog.setUserEntityId(getUserDetails().getUserEntityId().getLocalId());
        }
        if (event.equalsIgnoreCase(USER_CREATE_EVENT)) {
            userAuditLog.setVersion(0);
        } else {
            if (userManagementForm.getAssociatedUser() != null) {
                Integer version = userService.getLatestVersionOfAuditForUser(userManagementForm.getAssociatedUser().getId());
                if (version != null) {
                    version = version + 1;
                    userAuditLog.setVersion(version);
                } else {
                    version = 0;
                    userAuditLog.setVersion(version);
                }
            }
        }
        userAuditLog.setUserEvent(event);
        //update reason
        if(reasonId != null){
            userManagementServiceCore.updateReason(userAuditLog, reasonId, event);
            userAuditLog.setReasonRemarks(remarks);
        }
        userService.saveUserAuditLog(userAuditLog);
    }
    
    @RequestMapping(value="/getSourceSystemByUserName")
	public @ResponseBody String getSourceSystemByUserName(@RequestParam("username") String userName){
		String sourceSystemForUserName = userService.getSourceSystemForUserName(userName);
		NeutrinoValidator.notNull(sourceSystemForUserName);
		return sourceSystemForUserName;
	}

    public void mailhelper(Map<String, String> mapKeys,User user,String result, HttpServletRequest request) throws IOException,MessagingException {

        String mailServiceProvider = templateService.getResolvedStringFromResourceBundle("mail.service.provider", null, mapKeys);

        String appPath = null;
        if(neutrinoSessionRegistry.isSsoActive()){
            if(StringUtils.isEmpty(ssoUrl)){
                throw new SystemException("Sso url property core.web.config.SSO.ticketvalidator.url.value can not be null.");
            }
            appPath = ssoUrl + "/redirectDirectToResetPasswordPage/"+user.getId()+"/"+result;
        }else{

            String serverName = request.getServerName();
            if (serverName == null) {
                serverName = request.getLocalAddr();
            }
            int serverPort = request.getServerPort();

            appPath = systemSetupService.getSystemHttpProtocol() + "://" + serverName+ ":"+ serverPort+ request.getContextPath()
                    + "/app/resetPassword/redirectDirectToResetPasswordPage/"+user.getId()+"/"+result;
        }

        Map<String, ConfigurationVO> conf = configurationService.getFinalConfigurationForEntity(SystemEntity.getSystemEntityId());
        String tokenValidityTimeInMillis = conf.get("config.user.passwordResetToken.duration").getPropertyValue();

        Map<String, Object> dataMap = new HashMap<>();
        String to = user.getMailId();

        String subject=configurationService.getPropertyValueByPropertyKey("config.User.forcepasswordReset.emailSubject","Configuration.getPropertyValueFromPropertyKey");

        dataMap.put("resetPasswordLink",appPath);
        dataMap.put("userName",user.getDisplayName());
        dataMap.put("tokenValidityMinutes",Long.parseLong(tokenValidityTimeInMillis)/60000);

        String htmlBody = resetPasswordEmailHelper.getEmailBody(dataMap,"forceResetPasswordEmail.vm");

        MimeMailMessageBuilder mime = mailService.createMimeMailBuilder();
        mime.setFrom(from);
        mime.setTo(to);
        mime.setSubject(subject);
        mime.setHtmlBody(htmlBody);
        if (("direct").equalsIgnoreCase(mailServiceProvider)) {
            mailService.sendMail(mime);
        } else {
            mailMessageIntegrationService.sendMailMessageToIntegrationServer(mime.getMimeMessage());
        }

    }

    @RequestMapping(value="/userMappedDefaultRedirect")
    public String redirectToMappedDefaultPage(ModelMap map, HttpServletRequest httpRequest){
        SourceProduct sourceProduct=genericParameterService.findByCode(ProductInformationLoader.getProductCode(),SourceProduct.class);
        map.put("sourceProductName", sourceProduct);
        if(sourceProduct!=null) {
            MenuEntity menuEntity=userService.getMappedDefaultMenu(getUserDetails().getId(), sourceProduct.getId());
            map.put("linkedTargetFunction", menuEntity.getLinkedFunction());
            return "userMappedDefaultRedirect";
        }else{
            return systemSetupUtil.getAuthenticationSuccessUrl(httpRequest);
        }

    }

    @RequestMapping(value = "/getUserBlockReason/{userId}", method = RequestMethod.POST)
    public String getUserBlockReason(@PathVariable("userId") String[] uidList,ModelMap map,@RequestParam(required = false) String blockOperationType){
        List<BlockReason> highPriorityBlockReasonList = new ArrayList<>();
        if (blockOperationType.equalsIgnoreCase("blockUser")) {
            if (uidList != null && uidList.length>0){
                if (uidList.length==1){
                    int userStatus = userService.getUserStatusByUserId(Long.parseLong(uidList[0]));
                    if (userStatus == UserStatus.STATUS_ACTIVE){
                        List<BlockReason> blockReasonList = genericParameterService.retrieveTypes(BlockReason.class);
                        highPriorityBlockReasonList = blockReasonList.stream().filter(b -> "UNBLOCK_NO".equalsIgnoreCase(b.getParentCode())).collect(Collectors.toList());
                    }else {
                        highPriorityBlockReasonList = userService.getHighPriorityBlockReasonExist(Long.parseLong(uidList[0]), "UNBLOCK_NO");
                    }
                }else {
                    List<BlockReason> existingBlockReasonList = new ArrayList<>();
                    List<Long> userIdList = null;
                    userIdList = Stream.of(uidList).map(Long::valueOf).collect(Collectors.toList());
                    userIdList = userService.getUserStatusCountByUserId(userIdList);
                    if (CollectionUtils.isNotEmpty(userIdList)) {
                        for (Long uid : userIdList) {
                            BlockReason currentBlockReason = userService.getCurrentBlockReason(uid);
                            existingBlockReasonList.add(currentBlockReason);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(existingBlockReasonList)){
                        List<BlockReason> blockReasonList = genericParameterService.retrieveTypes(BlockReason.class);
                        Optional<BlockReason> existingHighPriorityblockReason = existingBlockReasonList.stream().max(Comparator.comparing(BlockReason::getLevelInHierarchy));
                        if (existingHighPriorityblockReason.isPresent()){
                            BlockReason currentBlockReason = existingHighPriorityblockReason.get();
                            highPriorityBlockReasonList = blockReasonList.stream().filter(b -> (b.getLevelInHierarchy() > currentBlockReason.getLevelInHierarchy()) && ("UNBLOCK_NO".equalsIgnoreCase(b.getParentCode()))).collect(Collectors.toList());
                        }
                    }else {
                        highPriorityBlockReasonList = userService.getHighPriorityBlockReasonExist(Long.parseLong(uidList[0]), "UNBLOCK_NO");
                    }
                }
            }

        }else if (blockOperationType.equalsIgnoreCase("blockReason")){
            if (uidList != null && uidList.length>0) {
                for (String uid : uidList) {
                    ReasonVO reasonVO = userManagementServiceCore.getBlockInactiveReasonByUserId(Long.parseLong(uid));
                    map.put("reasonVO", reasonVO);
                }
            }
        }
        map.put("blockReasonList", highPriorityBlockReasonList);
        return "userBlockReason";
    }


}
