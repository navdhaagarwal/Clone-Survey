package com.nucleus.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.nucleus.authenticationToken.ApprovalLinkToken;
import com.nucleus.authenticationToken.PasswordResetToken;
import com.nucleus.authority.Authority;
import com.nucleus.businessmapping.entity.UserPasswordHistory;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.exceptions.InvalidDataException;
import io.swagger.annotations.ApiModelProperty;
import com.nucleus.core.money.entity.Money;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.core.role.entity.Role;
import com.nucleus.employment.Employee;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import com.nucleus.master.audit.annotation.NeutrinoAuditableMaster;
import com.nucleus.user.cache.UserEntityListener;
import com.nucleus.user.security.UserPasswordEncodingUtil;


/**
 * Represents a user in the system.
 */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "Users", indexes = { @Index(name = "username_index", columnList = "username"),
		@Index(name = "USERS_IDX1", columnList = "PASSWORD_RESET_TOKEN") })
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="ALL")
@EntityListeners(UserEntityListener.class)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = User.class
)
@NeutrinoAuditableMaster(identifierColumn="username")
public class User extends BaseMasterEntity {
    // ~ Static fields/initializers
    // =================================================================

    private static final long                serialVersionUID          = 1;
    public static final String               PSWD_NEVER_EXPIRES         = "Never";

    // ~ Instance fields
    // ============================================================================

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime                         passwordExpirationDate;
    
    @Sortable(index = 1)
    @EmbedInAuditAsValue(displayKey="label.user.profile.userName")
    private String                           username;

    @Column(unique = false)
    @ApiModelProperty(hidden=true)
    private String                           password;

    @EmbedInAuditAsValue(displayKey="label.user.isSuperAdmin",setterName = "setSuperAdmin" , getterName = "isSuperAdmin")
    private boolean                          isSuperAdmin;

    @Column(nullable = false)
    private int                              userStatus;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Employee                         employee;

    private String                           passwordHintQuestion;

    private String                           passwordHintAnswer;
    

    @Transient
    @ApiModelProperty(hidden=true)
    private Set<Authority>                   authorities;

    @Column(updatable = false)
    @ApiModelProperty(hidden=true)
    private String                           hashKey;

    @EmbedInAuditAsValue(displayKey="label.emailid")
    private String                           mailId;

    @OneToOne(fetch = FetchType.LAZY)
    @ApiModelProperty(hidden=true)
    private PasswordResetToken               passwordResetToken;

    @OneToMany(fetch = FetchType.LAZY)
    @ApiModelProperty(hidden=true)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(name = "USERS_APPROVAL_LINK_TOKEN", joinColumns= {@JoinColumn(name="USERS", referencedColumnName = "ID")},
    inverseJoinColumns = {@JoinColumn(name="APPROVAL_LINK_TOKEN", referencedColumnName = "ID")})
    private List<ApprovalLinkToken>          approvalLinkToken;

    /** This field indicates the origin source system of user**/
    private String                           sourceSystem;

    /** This field captures the time when user account was locked**/
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime                         lastLockedDate;

    private Integer                          daysToBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(displayKey="label.deviation.deviationLevel.labelKey")
    private DeviationLevel                   deviationLevel;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(displayKey="label.user.classification")
    private UserClassification               userClassification;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(displayKey="label.user.category")
    private UserCategory                     userCategory;





    @EmbedInAuditAsValue(displayKey="label.user.isBusinessPartner",setterName = "setBusinessPartner" , getterName = "isBusinessPartner")
	private boolean                          isBusinessPartner;

    @EmbedInAuditAsValue(displayKey="label.user.isRelationshipOfficer",setterName = "setRelationshipOfficer" , getterName = "isRelationshipOfficer")
    private boolean                          isRelationshipOfficer;

    @EmbedInAuditAsValue(displayKey="label.user.isSupervisor",setterName = "setSupervisor" , getterName = "isSupervisor")
    private boolean                          isSupervisor;

    @EmbedInAuditAsValue(displayKey="label.user.isLoginEnabled",setterName = "setLoginEnabled" , getterName = "isLoginEnabled")
    private boolean 						 isLoginEnabled;
    
    @Embedded
    private OutOfOfficeDetails               outOfOfficeDetails        = new OutOfOfficeDetails(false, null, null, null,
                                                                               null);
    @EmbedInAuditAsValue(displayKey="label.password.expires.in")
    private String                           passwordExpiresInDays;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime                         lastPasswordResetDate;

    private int                              numberOfFailedLoginAttempts;

    private int                              numberOfFailedPassResetAttempts;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY/*, orphanRemoval = true*/)
    private UserMobilityInfo                 userMobileInfo;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL/*, orphanRemoval = true*/)
    @JoinColumn(name = "user_fk")
    @ApiModelProperty(hidden=true)
    private List<UserPasswordHistory>        userPasswordHistories;

    @ManyToOne(fetch = FetchType.LAZY)
    private SystemName                       sysName;

    @EmbedInAuditAsValue(displayKey = "Access to All Branches")
    private Character                        accessToAllBranches;

    @EmbedInAuditAsValue(displayKey = "Access to All Products")
    private Character                        accessToAllProducts;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="baseAmount.baseValue",column=@Column(name="slmt_cl_base_value",precision = 25, scale = 7)),
        @AttributeOverride(name="baseAmount.baseCurrencyCode",column=@Column(name="slmt_cl_base_curr_code")),
        @AttributeOverride(name="nonBaseAmount.nonBaseValue",column=@Column(name="slmt_cl_non_base_value",precision = 25, scale = 7)),
        @AttributeOverride(name="nonBaseAmount.nonBasecurrencyCode",column=@Column(name="slmt_cl_non_base_curr_code"))
        })
    @EmbedInAuditAsValue(displayKey="label.user.sanctioned.limit")
    private Money                            sanctionedLimit;

    @EmbedInAuditAsValue
    private Boolean                          teamLead;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL/*, orphanRemoval = true*/)
    @JoinColumn(name = "user_fk")
    private List<UserSecurityQuestionAnswer> securityQuestionAnswers;

    private boolean                          forcePasswordResetOnLogin = Boolean.TRUE;
    
    private Boolean                          licenseAccepted;
    private Boolean                          mfaEnabled;
    
  

	@ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(displayKey="User Department")
    private UserDepartment					userDepartment;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_CALENDAR")
    @EmbedInAuditAsValueObject
    private UserCalendar 					userCalendar; 
    
    public UserCalendar getUserCalendar() {
		return userCalendar;
	}

	public void setUserCalendar(UserCalendar userCalendar) {
		this.userCalendar = userCalendar;
	}

	public Boolean getMaintainLoginDays() {
		return maintainLoginDays;
	}

	public void setMaintainLoginDays(Boolean maintainLoginDays) {
		this.maintainLoginDays = maintainLoginDays;
	}

	@Column
    @EmbedInAuditAsValue(displayKey = "Maintain Login Days")
    private Boolean							maintainLoginDays;
    

    @ManyToMany(fetch = FetchType.LAZY,mappedBy="users") 
    @Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
    @EmbedInAuditAsReference(displayKey="User Roles",columnToDisplay = "name")
    private List<Role> userRoles;

    @EmbedInAuditAsValue(displayKey = "Is Bot User")
    private Boolean isBotUser;
    
    @Transient
    @ApiModelProperty(hidden=true)
    private UserAuditTrailVO auditTrailVO;
    

    public void setUserRoles(List<Role> userRoles) {
        this.userRoles = userRoles;
    }

    public List<Role> getUserRoles() {
		return userRoles;
	}

	public Money getSanctionedLimit() {
        return sanctionedLimit;
    }

    public void setSanctionedLimit(Money sanctionedLimit) {
        this.sanctionedLimit = sanctionedLimit;
    }

    public UserMobilityInfo getUserMobileInfo() {
        return userMobileInfo;
    }

    public void setUserMobileInfo(UserMobilityInfo userMobileInfo) {
        this.userMobileInfo = userMobileInfo;
    }

    public OutOfOfficeDetails getOutOfOfficeDetails() {
        if (outOfOfficeDetails == null) {
            outOfOfficeDetails = new OutOfOfficeDetails(false, null, null, null, null);
        }
        return outOfOfficeDetails;
    }

    public void setOutOfOfficeDetails(OutOfOfficeDetails outOfOfficeDetails) {
        this.outOfOfficeDetails = outOfOfficeDetails;
    }

    public boolean isBusinessPartner() {
        return isBusinessPartner;
    }

    public void setBusinessPartner(boolean isBusinessPartner) {
        this.isBusinessPartner = isBusinessPartner;
    }

    public DeviationLevel getDeviationLevel() {
        return deviationLevel;
    }

    public void setDeviationLevel(DeviationLevel deviationLevel) {
        this.deviationLevel = deviationLevel;
    }
    
    public UserClassification getUserClassification() {
		return userClassification;
	}

	public void setUserClassification(UserClassification userClassification) {
		this.userClassification = userClassification;
	}
    


	public UserCategory getUserCategory() {
		return userCategory;
	}

	public void setUserCategory(UserCategory userCategory) {
		this.userCategory = userCategory;
	}

	
	
	
	

    public PasswordResetToken getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(PasswordResetToken passwordResetAuthenticationToken) {
        this.passwordResetToken = passwordResetAuthenticationToken;
    }

    /**
     * @return the approvalLinkAuthenticationToken
     */
    public List<ApprovalLinkToken> getApprovalLinkToken() {
        return approvalLinkToken;
    }

    /**
     * @param approvalLinkToken the approvalLinkAuthenticationToken to set
     */
    public void setApprovalLinkToken(List<ApprovalLinkToken> approvalLinkAuthenticationTokens) {
        this.approvalLinkToken = approvalLinkAuthenticationTokens;
    }

    public void addApprovalLinkToken(ApprovalLinkToken approvalLinkAuthenticationToken) {

        if (this.approvalLinkToken == null) {
            this.approvalLinkToken = new ArrayList<ApprovalLinkToken>();
        }
        this.approvalLinkToken.add(approvalLinkAuthenticationToken);
    }

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public User() {
    }
    
    public User(Boolean forcePasswordResetOnLogin) {
    	this.forcePasswordResetOnLogin = forcePasswordResetOnLogin;
    }

    public User(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Id cannot be null or <= 0");
        }
        setId(id);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public DateTime getPasswordExpirationDate() {
        return passwordExpirationDate;
    }

    public void setPasswordExpirationDate(DateTime passwordExpirationDate) {
        this.passwordExpirationDate = passwordExpirationDate;
    }

    public void setPasswordExpirationDate(Date passwordExpirationDate) {
        setPasswordExpirationDate(new DateTime(passwordExpirationDate));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
    }

    public void lock() {
        userStatus = UserStatus.STATUS_LOCKED;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void unlock() {
        userStatus = UserStatus.STATUS_ACTIVE;
    }

    public void markAsInactive() {
        userStatus = UserStatus.STATUS_INACTIVE;
    }

    public void markAsDeleted() {
        userStatus = UserStatus.STATUS_DELETED;
    }

    public String getPasswordHintQuestion() {
        return passwordHintQuestion;
    }

    public void setPasswordHintQuestion(String passwordHintQuestion) {
        this.passwordHintQuestion = passwordHintQuestion;
    }

    public String passwordHintAnswer() {
        return passwordHintAnswer;
    }
    public boolean isLoginEnabled() {
  		return isLoginEnabled;
  	}

  	public void setLoginEnabled(boolean isLoginEnabled) {
  		this.isLoginEnabled = isLoginEnabled;
  	}

    // hide the real passwordHintAnswer from being serialized to the client side
    public String getPasswordHintAnswer() {
        return passwordHintAnswer;
    }

    public void setPasswordHintAnswer(String passwordHintAnswer) {
        this.passwordHintAnswer = passwordHintAnswer;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public void addToAuthorities(Set<Authority> authorities) {
        if (authorities == null) {
            return;
        }
        if (this.authorities == null) {
            this.authorities = new HashSet<Authority>();
        }
        authorities.addAll(authorities);
    }

    /**
     * Gets the display name for this user. This will be derived from user type
     * (employee, dealer etc).
     * 
     * @return The display name for user
     */
    @Override
    public String getDisplayName() {
        if (employee != null && employee.getPersonInfo() != null) {
            return employee.getPersonInfo().getFirstName() + employee.getPersonInfo().getLastName();
        }
        return username;
    }

    /*
     * @PreUpdate is removed to handle the different scenarios
     * 1) prePersist - at the time of user creation, password will always be encrypted 
     * 2) update expiry date forced by admin - password is not to be encrypted
     * 3) change password - Password needs to be encrypted before saving
     * Update scenario 2 is handled in updatePassword.
     * */
    // @PreUpdate
    @PrePersist
    void createHashKey() {

        if (getEntityLifeCycleData() != null && getEntityLifeCycleData().getCreationTimeStamp() != null) {
            this.hashKey = Long.valueOf(getEntityLifeCycleData().getCreationTimeStamp().getMillis()).toString();
        } else if (this.hashKey == null) {
            this.hashKey = String.valueOf(System.currentTimeMillis());
        }
        this.password = UserPasswordEncodingUtil.encode(password);
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public DateTime getLastLockedDate() {
        return lastLockedDate;
    }

    public void setLastLockedDate(DateTime lastLockedDate) {
        this.lastLockedDate = lastLockedDate;
    }

    public String getPasswordExpiresInDays() {
        return passwordExpiresInDays;
    }

    public void setPasswordExpiresInDays(String passwordExpiresInDays) {
        this.passwordExpiresInDays = passwordExpiresInDays;
    }

    public DateTime getLastPasswordResetDate() {
        return lastPasswordResetDate;
    }

    public void setLastPasswordResetDate(DateTime lastPasswordResetDate) {
        this.lastPasswordResetDate = lastPasswordResetDate;
    }

    public int getNumberOfFailedLoginAttempts() {
        return numberOfFailedLoginAttempts;
    }

    public void setNumberOfFailedLoginAttempts(int numberOfFailedLoginAttempts) {
        this.numberOfFailedLoginAttempts = numberOfFailedLoginAttempts;
    }

    public int getNumberOfFailedPassResetAttempts() {
        return numberOfFailedPassResetAttempts;
    }

    public void setNumberOfFailedPassResetAttempts(int numberOfFailedPassResetAttempts) {
        this.numberOfFailedPassResetAttempts = numberOfFailedPassResetAttempts;
    }

    public List<UserPasswordHistory> getUserPasswordHistories() {
        return userPasswordHistories;
    }

    public void setUserPasswordHistories(List<UserPasswordHistory> userPasswordHistories) {
        this.userPasswordHistories = userPasswordHistories;
    }

    public Character getAccessToAllBranches() {
        return accessToAllBranches;
    }

    public void setAccessToAllBranches(Character accessToAllBranches) {
        this.accessToAllBranches = accessToAllBranches;
    }

    public Character getAccessToAllProducts() {
        return accessToAllProducts;
    }

    public void setAccessToAllProducts(Character accessToAllProducts) {
        this.accessToAllProducts = accessToAllProducts;
    }

    public boolean isRelationshipOfficer() {
        return isRelationshipOfficer;
    }

    public void setRelationshipOfficer(boolean isRelationshipOfficer) {
        this.isRelationshipOfficer = isRelationshipOfficer;
    }

    public boolean isSupervisor() {
        return isSupervisor;
    }

    public void setSupervisor(boolean isSupervisor) {
        this.isSupervisor = isSupervisor;
    }

    /**
     * @return the teamLead
     */
    public Boolean getTeamLead() {
    	return teamLead == null ? Boolean.FALSE : teamLead;
    }

    /**
     * @param teamLead the teamLead to set
     */
    public void setTeamLead(Boolean teamLead) {
        this.teamLead = teamLead;
    }

    public Integer getDaysToBlock() {
        return daysToBlock;
    }

    public void setDaysToBlock(Integer daysToBlock) {
        this.daysToBlock = daysToBlock;
    }

    public List<UserSecurityQuestionAnswer> getSecurityQuestionAnswers() {
        return securityQuestionAnswers;
    }

    public void setSecurityQuestionAnswers(List<UserSecurityQuestionAnswer> securityQuestionAnswers) {
        this.securityQuestionAnswers = securityQuestionAnswers;
    }

    public boolean isForcePasswordResetOnLogin() {
        return forcePasswordResetOnLogin;
    }

    public void setForcePasswordResetOnLogin(boolean forcePasswordResetOnLogin) {
        this.forcePasswordResetOnLogin = forcePasswordResetOnLogin;
    }

    public SystemName getSysName() {
        return sysName;
    }

    public void setSysName(SystemName sysName) {
        this.sysName = sysName;
    }

    public void setSuperAdmin(boolean isSuperAdmin) {
        this.isSuperAdmin = isSuperAdmin;
    }
    
    public Boolean isLicenseAccepted() {
		return licenseAccepted;
	}

	public void setLicenseAccepted(Boolean licenseAccepted) {
		this.licenseAccepted = licenseAccepted;
	}
    
    public boolean isAccountNonLocked() {
        return userStatus != UserStatus.STATUS_LOCKED;
    }
	
	public boolean isAccountLocked() {
        return userStatus == UserStatus.STATUS_LOCKED;
    }
    
    public boolean isEnabled() {
        return userStatus == UserStatus.STATUS_ACTIVE;
    }
    
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	User user = (User) baseEntity;
        super.populate(user, cloneOptions);
        user.setUsername(username);
        user.setAccessToAllBranches(accessToAllBranches);
        user.setAccessToAllProducts(accessToAllProducts);
        user.setDeviationLevel(deviationLevel);
        user.setUserClassification(userClassification);
        user.setUserCategory(userCategory);
        user.setMailId(mailId);
        user.setPasswordExpiresInDays(passwordExpiresInDays);
        if (StringUtils.isNotBlank(passwordExpiresInDays) && !passwordExpiresInDays.equalsIgnoreCase(User.PSWD_NEVER_EXPIRES)) {
        	user.setPasswordExpirationDate(new DateTime(new Date()).plusDays(Integer.parseInt(passwordExpiresInDays)));
        }else{
        	user.setPasswordExpirationDate(passwordExpirationDate);
        }
        user.setMfaEnabled(mfaEnabled);
        user.setBusinessPartner(isBusinessPartner);
        user.setSanctionedLimit(sanctionedLimit);
        user.setRelationshipOfficer(isRelationshipOfficer);
        user.setTeamLead(teamLead);
        user.setSupervisor(isSupervisor);
        user.setSuperAdmin(isSuperAdmin);
        user.setUserMobileInfo(userMobileInfo);
        user.setSysName(sysName);
        user.setLoginEnabled(isLoginEnabled);
        user.setUserDepartment(userDepartment);
        user.setMaintainLoginDays(maintainLoginDays);
        if (userCalendar != null) {
        	user.setUserCalendar((UserCalendar) userCalendar.cloneYourself(cloneOptions));
        }
        user.setIsBotUser(isBotUser);
        user.setUserStatus(userStatus);
        user.setDaysToBlock(daysToBlock);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	User user = (User) baseEntity;
        super.populateFrom(user, cloneOptions);
        this.setUsername(user.getUsername());
        this.setAccessToAllBranches(user.getAccessToAllBranches());
        this.setAccessToAllProducts(user.getAccessToAllProducts());
        this.setDeviationLevel(user.getDeviationLevel());
        this.setUserClassification(user.getUserClassification());
      
      
        this.setUserCategory(user.getUserCategory());
        this.setMailId(user.getMailId());
        this.setPasswordExpiresInDays(user.getPasswordExpiresInDays());
        this.setPasswordExpirationDate(user.getPasswordExpirationDate());
        this.setBusinessPartner(user.isBusinessPartner());
        this.setSanctionedLimit(user.getSanctionedLimit());
        this.setRelationshipOfficer(user.isRelationshipOfficer());
        this.setTeamLead(user.getTeamLead());
        this.setSupervisor(user.isSupervisor());
        this.setSuperAdmin(user.isSuperAdmin());
        this.setUserMobileInfo(user.getUserMobileInfo());
        this.setSysName(user.getSysName());
        this.setLoginEnabled(user.isLoginEnabled());
        this.setUserDepartment(user.getUserDepartment()); 
        this.setMaintainLoginDays(user.getMaintainLoginDays());
        if (user.getUserCalendar() != null) {
            this.setUserCalendar((UserCalendar) user.getUserCalendar().cloneYourself(cloneOptions));

        }else{
        	this.setUserCalendar(null);
        }
        this.setMfaEnabled(user.isMfaEnabled());
        this.setIsBotUser(user.getIsBotUser());
        this.setUserStatus(user.getUserStatus());
        this.setDaysToBlock(user.getDaysToBlock());
    }
    
    @Override
    public void loadLazyFields()
    {
    	super.loadLazyFields();
    	if(getEmployee()!=null)
    	{
    		getEmployee().loadLazyFields();
    	}
    	if(getUserRoles()!=null)
    	{
    		for(Role role:getUserRoles())
    		{
    			role.loadLazyFields();
	}

	
    	}
    	if(getPasswordResetToken()!=null)
    	{
    		getPasswordResetToken().loadLazyFields();
    	}
    	initializeApprovalLinkTonen();
    	
    	if(getDeviationLevel()!=null)
    	{
    		getDeviationLevel().loadLazyFields();
    	}
    	
    	if(getUserClassification()!=null)
    	{
    		getUserClassification().loadLazyFields();
    	}
    	
    	if(getUserCategory()!=null)
    	{
    		getUserCategory().loadLazyFields();
    	}
    	
    	
    	if(getUserMobileInfo()!=null)
    	{
    		userMobileInfo.loadLazyFields();
    	}
    	
    	initializePasswordHistories();
    	
    	if(getSysName()!=null)
    	{
    		getSysName().loadLazyFields();
    	}
    	
    	initializeSecurityQuestionAnswers();
    }

	private void initializeSecurityQuestionAnswers() {
    	if(getSecurityQuestionAnswers()!=null)
    	{
    		for(UserSecurityQuestionAnswer securityQuestionAnswer:getSecurityQuestionAnswers())
    		{
    			securityQuestionAnswer.loadLazyFields();
	}


	
    	}
		
	}

	private void initializePasswordHistories() {
    	if(getUserPasswordHistories()!=null)
    	{
    		for(UserPasswordHistory userPasswordHistory:getUserPasswordHistories())
    		{
    			if(userPasswordHistory!=null)
    			{
    	   			userPasswordHistory.loadLazyFields();
	}

	
    		}
    	}
		
	}

	private void initializeApprovalLinkTonen() {
    	if(getApprovalLinkToken()!=null)
    	{
    		for(ApprovalLinkToken approvalLinkTkn:getApprovalLinkToken())
    		{
    			if(approvalLinkTkn!=null)
    			{
        			approvalLinkTkn.loadLazyFields();
    			}
    		}
    	}
		
	}
	

	public UserDepartment getUserDepartment() {
		return userDepartment;
	}

	public void setUserDepartment(UserDepartment userDepartment) {
		this.userDepartment = userDepartment;
	}

	public Boolean getIsBotUser() {
		return isBotUser;
	}

	public void setIsBotUser(Boolean isBotUser) {
		this.isBotUser = isBotUser;
	}


    public UserAuditTrailVO getAuditTrailVO() {
        return auditTrailVO;
    }

    public void setAuditTrailVO(UserAuditTrailVO auditTrailVO) {
        this.auditTrailVO = auditTrailVO;
    }
    public Boolean isMfaEnabled() {
    	if(mfaEnabled==null)
    	{
    		return Boolean.TRUE;
    	}
  		return mfaEnabled;
  	}

  	public void setMfaEnabled(Boolean mfaEnabled) {
  		this.mfaEnabled = mfaEnabled;
  	}
}