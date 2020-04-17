package com.nucleus.web.ldap;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.contact.EMailInfo;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.employment.EmploymentInfo;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.person.entity.SalutationType;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserDao;
import com.nucleus.user.UserDepartment;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserService;

@Named(value = "ldapService")
public class LdapServiceImpl extends BaseServiceImpl implements LdapService {

    @Value(value = "#{'${core.web.config.activeDirectoryAuthenticationProvider.url.value}'}")
    private String                  url;
    @Value(value = "#{'${core.web.config.activeDirectoryAuthenticationProvider.domain.value}'}")
    private String                  domain;

    private String                  rootDn;

    @Value(value = "#{'${core.web.config.ldapusersearch.searchfilter.value}'}")
    private String                  searchFilter;

    // Get attributes from ldap
    @Value(value = "#{'${core.web.config.default.branchCode.ldap}'}")
    private String                  code;
    @Value(value = "#{'${core.web.config.ldap.default.nickname}'}")
    private String                  nickname;
    @Value(value = "#{'${core.web.config.ldap.default.initials}'}")
    private String                  initials;
    @Value(value = "#{'${core.web.config.attribute.mailid}'}")
    private String                  mailId;
    @Value(value = "#{'${core.web.config.attribute.firstname}'}")
    private String                  firstname;
    @Value(value = "#{'${core.web.config.attribute.lastname}'}")
    private String                  lastname;
    @Value(value = "#{'${core.web.config.attribute.mobileNo}'}")
    private String                  mobileNo;
    @Value(value = "#{'${core.web.config.attribute.phoneNo}'}")
    private String                  phoneNo;
    @Value(value = "#{'${core.web.config.attribute.company}'}")
    private String                  company;
    @Value(value = "#{'${core.web.config.attribute.department}'}")
    private String                  department;
    @Value(value = "#{'${core.web.config.attribute.title}'}")
    private String                  title;
    @Value(value = "#{'${core.web.config.attribute.empCode}'}")
    private String                  employeeId;
    @Value(value = "#{'${core.web.config.ldap.default.thumbnailPhoto}'}")
    private String                  thumbnailPhoto;
    @Value(value = "#{'${core.web.config.ldap.user.autoApproval.required}'}")
    private boolean autoApproveLdapUser;
    
    private final String  importedUser = "msg.ldap.imported";
	private final String  updatedUser = "msg.ldap.updated";
   

    private boolean                 convertSubErrorCodesToExceptions = true;

    protected final Log             logger                           = LogFactory.getLog(getClass());
    protected MessageSourceAccessor messages                         = SpringSecurityMessageSource.getAccessor();
    private static final Pattern    SUB_ERROR_CODE                   = Pattern.compile(".*data\\s([0-9a-f]{3,4}).*");

    // Error codes
    private static final int        USERNAME_NOT_FOUND               = 0x525;
    private static final int        INVALID_PASSWORD                 = 0x52e;
    private static final int        NOT_PERMITTED                    = 0x530;
    private static final int        PASSWORD_EXPIRED                 = 0x532;
    private static final int        ACCOUNT_DISABLED                 = 0x533;
    private static final int        ACCOUNT_EXPIRED                  = 0x701;
    private static final int        PASSWORD_NEEDS_RESET             = 0x773;
    private static final int        ACCOUNT_LOCKED                   = 0x775;

    @Autowired
    private UserDao                 userDao;

    @Inject
    @Named("userService")
    private UserService             userService;

    @Inject
    @Named("organizationService")
    private OrganizationService     organizationService;

    @Inject
    @Named("couchDataStoreDocumentService")
    private DatastorageService      docService2;

    @Inject
    @Named("genericParameterService")
    GenericParameterService         genericParameterService;
    
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService        makerCheckerService;

    @Inject
    @Named("ldapDBUserMapper")
    private LdapDBUserMapper ldapDBUserMapper;
    
    @Inject
    @Named("configurationService")
    private ConfigurationService     configurationService;
    
    @Inject
    @Named("messageSource")
    MessageSource              messageSource;
    
	@Inject
	@Named("tika")
	private Tika tika;

    
    /* (non-Javadoc)
     * @see com.nucleus.web.ldap.LdapService#bindAsUser(java.lang.String, java.lang.String)
     */
    @Override
    public DirContext bindAsUser(String username, String password) {

        String bindUrl = getUrl();
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        String bindPrincipal = createBindPrincipal(username);
        env.put(Context.SECURITY_PRINCIPAL, bindPrincipal);
        env.put(Context.PROVIDER_URL, bindUrl);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.OBJECT_FACTORIES, DefaultDirObjectFactory.class.getName());

        try {
            return new InitialLdapContext(env, null);
        } catch (NamingException e) {
            if ((e instanceof AuthenticationException) || (e instanceof OperationNotSupportedException)) {
                handleBindException(bindPrincipal, e);
                throw badCredentials(e);
            } else if (e instanceof CommunicationException) {
                throw new SystemException("failure in communication with LDAP", e);
            } else {
                throw LdapUtils.convertLdapException(e);
            }
        }
    }

    public String createBindPrincipal(String username) {
        if (getDomain() == null || username.toLowerCase().endsWith(getDomain())) {
            return username;
        }

        return username + "@" + getDomain();
    }

    void handleBindException(String bindPrincipal, NamingException exception) {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication for " + bindPrincipal + " failed:" + exception);
        }

        int subErrorCode = parseSubErrorCode(exception.getMessage());

        if (subErrorCode > 0) {
            logger.info("Active Directory authentication failed: " + subCodeToLogMessage(subErrorCode));

            if (isConvertSubErrorCodesToExceptions()) {
                raiseExceptionForErrorCode(subErrorCode, exception);
            }
        } else {
            logger.debug("Failed to locate AD-specific sub-error code in message");
        }
    }

    public BadCredentialsException badCredentials(Throwable cause) {
        return (BadCredentialsException) badCredentials().initCause(cause);
    }

    public BadCredentialsException badCredentials() {
        return new BadCredentialsException(messages.getMessage("LdapAuthenticationProvider.badCredentials",
                "Bad credentials"));
    }

    int parseSubErrorCode(String message) {
        Matcher m = SUB_ERROR_CODE.matcher(message);

        if (m.matches()) {
            return Integer.parseInt(m.group(1), 16);
        }

        return -1;
    }

    String subCodeToLogMessage(int code) {
        switch (code) {
            case USERNAME_NOT_FOUND:
                return "User was not found in directory";
            case INVALID_PASSWORD:
                return "Supplied password was invalid";
            case NOT_PERMITTED:
                return "User not permitted to logon at this time";
            case PASSWORD_EXPIRED:
                return "Password has expired";
            case ACCOUNT_DISABLED:
                return "Account is disabled";
            case ACCOUNT_EXPIRED:
                return "Account expired";
            case PASSWORD_NEEDS_RESET:
                return "User must reset password";
            case ACCOUNT_LOCKED:
                return "Account locked";
        }

        return "Unknown (error code " + Integer.toHexString(code) + ")";
    }

    void raiseExceptionForErrorCode(int code, NamingException exception) {
        Throwable cause = new RuntimeException(exception.getMessage(), exception);
        switch (code) {
            case PASSWORD_EXPIRED:
                throw new CredentialsExpiredException(messages.getMessage("LdapAuthenticationProvider.credentialsExpired",
                        "User credentials have expired"), cause);
            case ACCOUNT_DISABLED:
                throw new DisabledException(messages.getMessage("LdapAuthenticationProvider.disabled", "User is disabled"),
                        cause);
            case ACCOUNT_EXPIRED:
                throw new AccountExpiredException(messages.getMessage("LdapAuthenticationProvider.expired",
                        "User account has expired"), cause);
            case ACCOUNT_LOCKED:
                throw new LockedException(
                        messages.getMessage("LdapAuthenticationProvider.locked", "User account is locked"), cause);
            default:
                throw badCredentials(cause);
        }
    }

    /* (non-Javadoc)
    * @see com.nucleus.web.ldap.LdapService#searchForUser(javax.naming.directory.DirContext, java.lang.String)
    */
    @Override
    public DirContextOperations searchForUser(DirContext ctx, String username) throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        // String searchFilter = "(&(objectClass=user)(userPrincipalName={0}))";

        final String bindPrincipal = createBindPrincipal(username);

        String searchRoot = getRootDn() != null ? getRootDn() : searchRootFromPrincipal(bindPrincipal);

        try {
            return SpringSecurityLdapTemplate.searchForSingleEntryInternal(ctx, searchCtls, searchRoot, getSearchFilter(),
                    new Object[] { bindPrincipal });
        } catch (IncorrectResultSizeDataAccessException incorrectResults) {
            if (incorrectResults.getActualSize() == 0) {
                UsernameNotFoundException userNameNotFoundException = new UsernameNotFoundException("User " + username
                        + " not found in directory.", incorrectResults);
                throw badCredentials(userNameNotFoundException);
            }
            // Search should never return multiple results if properly configured, so just rethrow
            throw incorrectResults;
        }
    }

    private String searchRootFromPrincipal(String bindPrincipal) {
        int atChar = bindPrincipal.lastIndexOf('@');

        if (atChar < 0) {
            logger.debug("User principal '" + bindPrincipal
                    + "' does not contain the domain, and no domain has been configured");
            throw badCredentials();
        }

        return rootDnFromDomain(bindPrincipal.substring(atChar + 1, bindPrincipal.length()));
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

    public String rootDnFromDomain(String domain) {
        String[] tokens = StringUtils.tokenizeToStringArray(domain, ".");
        StringBuilder root = new StringBuilder();

        for (String token : tokens) {
            if (root.length() > 0) {
                root.append(',');
            }
            root.append("dc=").append(token);
        }

        return root.toString();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        Assert.isTrue(StringUtils.hasText(url), "Url cannot be empty");
        this.url = url;

    }

    public String getDomain() {

        return domain;
    }

    public void setDomain(String domain) {
        this.domain = StringUtils.hasText(domain) ? domain.toLowerCase() : null;
        rootDn = this.domain == null ? null : rootDnFromDomain(this.domain);

    }

    public String getRootDn() {
        return rootDn;
    }

    public void setRootDn(String rootDn) {
        this.rootDn = rootDn;
    }

    public boolean isConvertSubErrorCodesToExceptions() {
        return convertSubErrorCodesToExceptions;
    }

    /**
     * By default, a failed authentication (LDAP error 49) will result in a {@code BadCredentialsException}.
     * <p>
     * If this property is set to {@code true}, the exception message from a failed bind attempt will be parsed
     * for the AD-specific error code and a {@link CredentialsExpiredException}, {@link DisabledException},
     * {@link AccountExpiredException} or {@link LockedException} will be thrown for the corresponding codes. All
     * other codes will result in the default {@code BadCredentialsException}.
     *
     * @param convertSubErrorCodesToExceptions {@code true} to raise an exception based on the AD error code.
     */
    public void setConvertSubErrorCodesToExceptions(boolean convertSubErrorCodesToExceptions) {
        this.convertSubErrorCodesToExceptions = convertSubErrorCodesToExceptions;
    }

  
    public Map<String,Object> createUpdateUserFromLdap(String username, DirContextOperations ctx) {

        Map<String, Object> map = convertDirectContextToMap(ctx);

        return createUpdateUserFromLdap(username, map);
    }

    public Map<String, Object> convertDirectContextToMap(DirContextOperations ctx) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map.put(thumbnailPhoto, ctx.getAttributes().get(thumbnailPhoto).get());
        } catch (Exception e) {
        }

        try {
            map.put(mailId, ctx.getAttributes().get(mailId).get().toString());

        } catch (Exception e) {
        }

        try {
            map.put(firstname, ctx.getAttributes().get(firstname).get().toString());
        } catch (Exception e) {
        }

        try {
            map.put(lastname, ctx.getAttributes().get(lastname).get().toString());
        } catch (Exception e) {
        }

        try {
            map.put(nickname, ctx.getAttributes().get(nickname).get().toString());
        } catch (Exception e) {
        }

        try {
            map.put(initials, ctx.getAttributes().get(initials).get().toString());
        } catch (Exception e) {
        }

        try {
            map.put(mobileNo, ctx.getAttributes().get(mobileNo).get().toString());

        } catch (Exception e) {
        }
        try {
            map.put(phoneNo, ctx.getAttributes().get(phoneNo).get().toString());

        } catch (Exception e) {
        }
        try {
            map.put(company, ctx.getAttributes().get(company).get().toString());

        } catch (Exception e) {
        }
        try {
            map.put(department, ctx.getAttributes().get(department).get().toString());

        } catch (Exception e) {
        }
        try {
            map.put(title, ctx.getAttributes().get(title).get().toString());

        } catch (Exception e) {
        }
        try {
            map.put(employeeId, ctx.getAttributes().get(employeeId).get().toString());
        } catch (Exception e) {
        }
        return map;
    }

    
    public User updateUserFromLdapIfAlreadyExists(User user,Map<String, Object> map){
    	return updateUserAndProfileDetails(user,map);
    }
    
    public User importUserFromLdapInDb(String username, Map<String, Object> map){

    
     
    	User user = new User();
        user.setUsername(username);
     
   
        if(autoApproveLdapUser){
        	/*
        	 * SaveAndSend For Approval LDAP User
        	 */
        	ldapDBUserMapper.saveAndSendForApprovalUserFromLdapAndStartMakerFlow(user);
        	
        }else{
        	/*
        	 * Save LDAP User
        	 */
        	ldapDBUserMapper.saveUserFromLdapInDbAndStartMakerFlow(user);
        }

        
       

        // default primary branch code must be present in database and configured in property file
        if (code == null) {
            throw new SystemException("No default code set as primary branch for ldap user.");
        }
        OrganizationBranch orgBranchByBranchCode = organizationService.getOrgBranchByBranchCode(code);
        if (orgBranchByBranchCode == null) {
            throw new SystemException("No branch found with code :" + code);
        }
        
        boolean includesSubBranches = true;
        if(ValidatorUtils.hasElements(orgBranchByBranchCode.getParentBranchMapping())){
        	includesSubBranches=false;
        }
        UserOrgBranchMapping branchMapping = new UserOrgBranchMapping(orgBranchByBranchCode, includesSubBranches);
        branchMapping.setAssociatedUser(user);
        branchMapping.setPrimaryBranch(true);
        branchMapping.setApprovalStatus(ApprovalStatus.APPROVED);
        entityDao.persist(branchMapping);

        // image to be uploaded only once during creation of user.
        try {
        	if(map.get(this.thumbnailPhoto) != null){
        		byte[] bytes = (byte[])map.get(this.thumbnailPhoto);
	            map.put(this.thumbnailPhoto, new String(Base64.encodeBase64(bytes)));
	            
	            
	            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
	            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
	            MimeType mimeType = allTypes.forName(findMimeType(stream));
	            if (mimeType == null) {
	    			BaseLoggers.exceptionLogger.error("******Invalid mime type ***** ");
	    			throw new SystemException("Mime type not supported");
	    		}
	            String extension = mimeType.getExtension(); 
	            if (StringUtils.isEmpty(extension)) {
	    			BaseLoggers.exceptionLogger.error("******Extension is either null or empty. ***** ");
	    			throw new SystemException("Extension not supported : Extension is either null or empty.");
	    		}
	            
	            int indexOfDot = extension.indexOf('.');
	            if(indexOfDot == 0){
	            	extension = extension.substring(indexOfDot + 1);
	            }
	            
	            String photoUrl = docService2.saveDocument(stream, "ldapPhoto_"
	                    + username, extension);
	            
	            userService.getUserProfile(user).setPhotoUrl(photoUrl);
        	}
        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch photograph for LDAP user while Importing", e);    
        }
       
         
        user=updateUserAndProfileDetails(user,map);
        
         if(autoApproveLdapUser){
        	 /*
        	  * Approve LDAP User
        	  */
        	 ldapDBUserMapper.startCheckerFlowForLDAPUser(user);        	 
         }
        
         
        return user;
    
    }
    
    /**
     * @param bais
     * @return
     */
    private String findMimeType(ByteArrayInputStream bais){
    	String mimeType = null;
    	try {
			mimeType = tika.detect(bais);
			return mimeType;			
		} catch (Exception e) {
			BaseLoggers.exceptionLogger
					.error("Unable to detect the mime type.",
							e.fillInStackTrace());
			throw new SystemException(
					"Tika is unable to detect File Type"+e);
		}
    }
    
    
 /*   private Boolean isAutoApprovalOfUserRequired() {
    	Boolean autoApproveUser=Boolean.FALSE;
    	 ConfigurationVO configVO = configurationService.getConfigurationPropertyFor(
                 EntityId.fromUri("com.nucleus.entity.SystemEntity:1"), "ldap.user.autoApproval.required");
    	 if(configVO!=null && "TRUE".equalsIgnoreCase(configVO.getPropertyValue())){
    		 autoApproveUser=Boolean.TRUE;
    	 }
    	 return autoApproveUser;
	}*/

	public Map<String,Object> createUpdateUserFromLdap(String checkName, Map<String, Object> map) {
    	
		String[] username = checkName.split("@");
		
    	List<User> userList=userService.getUserReferenceByUsername(username[0]);
    	Map<String,Object> userDetailsMap = new HashMap<String, Object>();
        User user = null;
        if (userList.size() > 0) {
            // update existing user
            user = userList.get(0);            
            user=updateUserFromLdapIfAlreadyExists(user,map);
            userDetailsMap.put("UserStatus", messageSource.getMessage(updatedUser, null, null, getUserLocale()));
        } else {
        	user=importUserFromLdapInDb(username[0],map);
        	userDetailsMap.put("UserStatus", messageSource.getMessage(importedUser, null, null, getUserLocale()));
        }
        userDetailsMap.put("user", user);
        	
        return userDetailsMap;
    }

    private User updateUserAndProfileDetails(User user,Map<String, Object> map) {
        /*
    	 * It should be true/false not sure
    	 */
    	user.setForcePasswordResetOnLogin(false);
       
        UserProfile userProfile = userService.getUserProfile(user);
        // updating user based on attributes from ldap
        String mailId = null;
        try {
            mailId = (String) map.get(this.mailId);
            user.setMailId(mailId);
            EMailInfo eMailInfo = new EMailInfo();
            eMailInfo.setEmailAddress(mailId);
            eMailInfo.setPrimaryEmail(true);
            userProfile.getSimpleContactInfo().setEmail(eMailInfo);
        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch mailId for LDAP user  while Importing", e);        	
        }

        try {
            userProfile.setFirstName((String) map.get(this.firstname));
        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch firstName for LDAP user  while Importing", e);    
        }

        try {
            userProfile.setLastName((String) map.get(this.lastname));
        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch lastName for LDAP user  while Importing", e);    
        }

        try {
            userProfile.setAliasName((String) map.get(this.nickname));
        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch nickName for LDAP user  while Importing", e);    
        }
        
        if(userProfile != null ){
        	userProfile.setFullName(userProfile.getFirstName() + " " + userProfile.getLastName()!=null?userProfile.getLastName() : "");
        }
        
        try {
        	String userInitials = (String) map.get(this.initials);
        	if(userInitials != null && ! StringUtils.isEmpty(userInitials)){
        		userProfile.setSalutation(genericParameterService.findByName(userInitials,SalutationType.class));
        	}
        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch initials/salutation for LDAP user  while Importing", e);    
        }
        
        try {
        	String userDepartment = (String) map.get(this.department);
        	if(userDepartment != null && ! StringUtils.isEmpty(userDepartment)){
        		userProfile.setUserDepartment(genericParameterService.findByCode(userDepartment,UserDepartment.class));
        		user.setUserDepartment(userProfile.getUserDepartment());
        	}
        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch initials/salutation for LDAP user  while Importing", e);    
        }

        String mobileNo = null;
        try {
            mobileNo = (String) map.get(this.mobileNo);
            // as per discussion with Gaurav Marwaha, need to pick last 10 digits of mobile number while importing the mobile
            // number.
            mobileNo = mobileNo.replaceAll(PhoneNumber.STRIP_CHARS_REGEX, "");
            mobileNo = mobileNo.substring(mobileNo.length() - 10, mobileNo.length());
            userProfile.getSimpleContactInfo().getMobileNumber().setPhoneNumber(mobileNo);
        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch mobileNo for LDAP user  while Importing", e);    
        }
     
        EmploymentInfo employmentInfo = null;
        if(user.getEmployee() != null){
        	employmentInfo = user.getEmployee().getEmploymentInfo();
        }
        
        String company = null;
        try {
            company = (String) map.get(this.company);
            employmentInfo.setCompany(company);

        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch company for LDAP user  while Importing", e);    
        }
        String department = null;
        try {
            department = (String) map.get(this.department);
            employmentInfo.setDepartment(department);

        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch department for LDAP user  while Importing", e);    
        }
        String title = null;
        try {
            title = (String) map.get(this.title);
            employmentInfo.setTitle(title);

        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch title for LDAP user  while Importing", e);    
        }
        String employeeId = null;
        try {
            employeeId = (String) map.get(this.employeeId);
            employmentInfo.setEmployeeId(employeeId);
        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Unable to fetch employeeId for LDAP user  while Importing", e);    
        }
        user.setPassword(null);
        return user;
	}


	

}
