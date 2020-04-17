package com.nucleus.web.ldap;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.address.Address;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.contact.SimpleContactInfo;
import com.nucleus.employment.Employee;
import com.nucleus.employment.EmploymentInfo;
import com.nucleus.grid.IGridService;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserSecurityService;
import com.nucleus.user.UserService;
import com.nucleus.user.UserStatus;

@Named(value = "ldapDBUserMapper")
public class LdapDBUserMapper extends BaseServiceImpl {

	@Inject
	@Named("userService")
	private UserService userService;
	
	@Inject
    @Named("makerCheckerService")
    private MakerCheckerService        makerCheckerService;
	
	@Inject
    @Named("baseMasterService")
    private BaseMasterService          baseMasterService;
	
	@Inject
	@Named("masterGridService")
	private IGridService masterGridService;

	@Transactional
	public User saveUserFromLdapInDbAndStartMakerFlow(User user) {
		user=setUserDetailsForLdapUser(user);
		User loggedInUser = getUserDetails().getUserReference();
		if (loggedInUser != null) {
			makerCheckerService.masterEntityChangedByUser(user, loggedInUser);
		}
		updateAndCreateUserProfile(user);
		return user;
	}

	 private void updateAndCreateUserProfile(User user) {
			SimpleContactInfo simpleContactInfo = new SimpleContactInfo();
			PhoneNumber mobNum = new PhoneNumber();
			PhoneNumber PhNum = new PhoneNumber();
			simpleContactInfo.setPhoneNumber(PhNum);
			simpleContactInfo.setMobileNumber(mobNum);
			Address adress = new Address();
			simpleContactInfo.setAddress(adress);
			UserProfile profile = new UserProfile();
			profile.setSimpleContactInfo(simpleContactInfo);
			profile.setAssociatedUser(user);

			userService.saveNewUserProfile(profile);
			
	}

	public UserInfo getUserDetails() {
	        UserInfo userInfo = null;
	        SecurityContext securityContext = SecurityContextHolder.getContext();
	        if (securityContext != null) {
	            Object principal = securityContext.getAuthentication().getPrincipal();
	            if (UserInfo.class.isAssignableFrom(principal.getClass())) {
	                userInfo = (UserInfo) principal;
	            }
	        }
	        return userInfo;
	    }
	 
	 @Transactional
	public void startCheckerFlowForLDAPUser(User user){
		 Long taskId=baseMasterService.getApprovalTaskIdbyRefUUID(user.getEntityLifeCycleData().getUuid());
	  		
		if(taskId!=null){
			User reviewer =userService.findUserByUsername(getCurrentUser().getUsername());
			makerCheckerService.completeTaskWithCheckerDecisionInExistingTransaction(taskId,"Approved", reviewer.getEntityId());
		}
		 
	 }
	 
	 @Transactional
	 public User saveAndSendForApprovalUserFromLdapAndStartMakerFlow(User user){
		 	user=setUserDetailsForLdapUser(user);
			User loggedInUser = getUserDetails().getUserReference();
			if (loggedInUser != null) {
				makerCheckerService.saveAndSendForApproval(user,loggedInUser);
			}

			updateAndCreateUserProfile(user);

			return user;				
	 }

	private User setUserDetailsForLdapUser(User user) {
		user.setSourceSystem(UserSecurityService.SOURCE_LDAP);
		user.setUserStatus(UserStatus.STATUS_ACTIVE);
		// user.setApprovalStatus(ApprovalStatus.APPROVED);
		EmploymentInfo empInf = new EmploymentInfo();
		Employee emp = new Employee();
		emp.setEmploymentInfo(empInf);
		user.setEmployee(emp);
		user.getEntityLifeCycleData().setCreatedByUri(
				userService.getUserFromUsername("system").getUserEntityId()
						.getUri());
		return user;
	}
}
