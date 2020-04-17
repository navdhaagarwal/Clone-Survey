/**
 *
 */
package com.nucleus.web.useradministration;

import static com.nucleus.entity.CloneOptionConstants.MAKER_CHECKER_CLONING_OPTION;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static com.nucleus.user.UserConstants.NEUTRINO_SYSTEM_USER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.nucleus.address.Address;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.AccessType;
import com.nucleus.user.DeviceIdentifierType;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.OrgBranchTree;
import com.nucleus.user.User;
import com.nucleus.user.UserCalendar;
import com.nucleus.user.UserDeviceMapping;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserMobilityInfo;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserService;
import com.nucleus.user.UserStatus;
import com.nucleus.user.UserVO;
import com.nucleus.web.usermgmt.UserManagementForm;

/**
 * @author vengadesh.kannan
 *
 */
@Named("userAdminHelper")
public class UserAdminHelper {

	@Inject
	@Named("userService")
	private UserService                 userService;

	@Inject
	private OrganizationService           organizationService;


	@Inject
	@Named("configurationService")
	private ConfigurationService        configurationService;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService   genericParameterService;
	
    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore   userManagementServiceCore;
    
	/**
	 * 
	 * @param jsonString
	 * @return updatedBranchData
	 */
	public List<Map<String, Object>>  parseUserOrgBranchMappingString(String jsonString){

		/*
		 * Convert json string for updated branch data to Map
		 */
		List<Map<String, Object>> updatedBranchData = new ArrayList<Map<String,Object>>();
		ObjectMapper mapper = new ObjectMapper();
		if (StringUtils.isNotBlank(jsonString)) {

			try {


				updatedBranchData = mapper.readValue(jsonString,
						new TypeReference<List<Map<String, Object>>>() {
				});

			} catch (JsonMappingException e) {
				BaseLoggers.exceptionLogger.error("JsonMappingException while converting json string updated branch data" , e);
			}  catch (IOException e) {
				BaseLoggers.exceptionLogger.error("IOException while converting json string updated branch data" , e);
			}

		}
		return updatedBranchData;
	}
	/**
	 * 
	 * @param jsonString
	 * @return updatedBranchProductData
	 */
	public Map<Long,List<Map<Long, String>>>  parseUserOrgBranchProductMappingString(String jsonString){

		/*
		 * Convert json string for updated branch data to Map
		 */
		Map<Long,List<Map<Long, String>>> updatedBranchProductData = new  HashMap<Long,List<Map<Long, String>>>();
		ObjectMapper mapper = new ObjectMapper();
		if (StringUtils.isNotBlank(jsonString)) {

			try {


				updatedBranchProductData = mapper.readValue(jsonString,
						new TypeReference<Map<Long,List<Map<Long, String>>>>() {
				});

			} catch (JsonMappingException e) {
				BaseLoggers.exceptionLogger.error("JsonMappingException while converting json string ton updatedBranchProductData" , e);
			}  catch (IOException e) {
				BaseLoggers.exceptionLogger.error("IOException while converting json string to updatedBranchProductData" , e);
			}

		}
		return updatedBranchProductData;
	}
	/**
	 * 
	 * @param userReference
	 * @param userManagementForm
	 * @return userReference
	 */
	public User updateWhetherUserHasAccessToAllBranchAndProduct(User userReference, UserManagementForm userManagementForm){
		// for saving user branches and products
		if (userManagementForm.getAssociatedUser().getAccessToAllBranches() != null
				&& userManagementForm.getAssociatedUser().getAccessToAllBranches().toString().equalsIgnoreCase("Y")) {
			userReference.setAccessToAllBranches('Y');
		} else {
			userReference.setAccessToAllBranches('N');
		}
		if (userManagementForm.getAssociatedUser().getAccessToAllProducts() != null
				&& userManagementForm.getAssociatedUser().getAccessToAllProducts().toString().equalsIgnoreCase("Y")) {
			userReference.setAccessToAllProducts('Y');
		} else {
			userReference.setAccessToAllProducts('N');
		}	
		return userReference;
	}

	/**
	 * 
	 * @param userProfile
	 * @param userReference
	 * @return userReference
	 */
	public User updateEmailOfUser(UserProfile userProfile, User userReference){
		if ((userProfile.getSimpleContactInfo() != null) && (userProfile.getSimpleContactInfo().getEmail() != null)
				&& StringUtils.isNotBlank(userProfile.getSimpleContactInfo().getEmail().getEmailAddress())) {
			userReference.setMailId(userProfile.getSimpleContactInfo().getEmail().getEmailAddress());
		} else {
			userReference.setMailId(userReference.getMailId());
		}
		return userReference;
	}

	/**
	 * 
	 * @param jsonString
	 * @return updatedTeamMappings
	 */
	public Map<Long, String> parseTeamMappingString(String jsonString){
		/*
		 * Convert json string for updated branch data to Map
		 */
		Map<Long, String> updatedTeamMappings = new HashMap<Long,String>();
		ObjectMapper mapper = new ObjectMapper();
		if (jsonString != null) {

			try {


				updatedTeamMappings = mapper.readValue(jsonString,
						new TypeReference<Map<Long, String>>() {
				});

			} catch (JsonMappingException e) {
				BaseLoggers.exceptionLogger.error("JsonMappingException while converting json string to updatedTeamMappings" , e);
			}  catch (IOException e) {
				BaseLoggers.exceptionLogger.error("IOException while converting json string to updatedTeamMappings" , e);
			}

		}
		return updatedTeamMappings;
	}
	/**
	 * 
	 * @param jsonString
	 * @return updatedRoleMappings
	 */
	public Map<Long, String> parseRoleMappingString(String jsonString){
		/*
		 * Convert json string for updated role data to Map
		 */
		Map<Long, String> updatedRoleMappings = new HashMap<Long,String>();
		ObjectMapper mapper = new ObjectMapper();
		if (jsonString != null) {
			try {
				updatedRoleMappings = mapper.readValue(jsonString,
						new TypeReference<Map<Long, String>>() {
				});
			} catch (JsonMappingException e) {
				BaseLoggers.exceptionLogger.error("JsonMappingException while converting json string to updatedRoleMappings" , e);
			}  catch (IOException e) {
				BaseLoggers.exceptionLogger.error("IOException while converting json string to updatedRoleMappings" , e);
			}

		}
		return updatedRoleMappings;
	}
	/**
	 * 
	 * @param updatedUserMobilityInfo
	 * @param userReference
	 * @return userReference
	 */
	public User updateMobilityInfoInUser(UserMobilityInfo updatedUserMobilityInfo, User userReference){
		if (updatedUserMobilityInfo != null) {

			List<UserDeviceMapping> userRegisteredDeviceList = updatedUserMobilityInfo.getRegisteredDeviceList();
							if(updatedUserMobilityInfo.getIsDeviceAuthEnabled() && userRegisteredDeviceList.size()==0){
								BaseLoggers.exceptionLogger.error("NO device registered");
								throw new ServiceInputException("NO device registered");
								
							}
			for (Iterator<UserDeviceMapping> iterator = userRegisteredDeviceList
					.iterator(); iterator.hasNext();) {
				UserDeviceMapping udm = iterator.next();
				if (udm == null || udm.getDeviceId() == null) {
					iterator.remove();
				} else {

					if (!validLengthForDeviceIdentifier(udm)) {
						BaseLoggers.exceptionLogger.error("Invalid length for device ID");
						throw new ServiceInputException("Invalid length for device ID");
					}
					udm.setDeviceStatus("ACTIVE");
				}
			} 
			UserMobilityInfo userMobilityInfo = new UserMobilityInfo(updatedUserMobilityInfo);
			userReference.setUserMobileInfo(userMobilityInfo);
		} else {
			UserMobilityInfo userMobilityInfo = userService.getUserMobilityInfo(userReference.getId());
			userReference.setUserMobileInfo(userMobilityInfo);
		}

		return userReference;
	}

	/**
	 * @param formUser
	 * @return formUser
	 */
	public User updateMiscellaneousInfoInUser(User formUser) {
		if (formUser.getAccessToAllBranches() == null) {
			formUser.setAccessToAllBranches('N');
		}
		if (formUser.getAccessToAllProducts() == null) {
			formUser.setAccessToAllProducts('N');
		}
		if (formUser.getDeviationLevel() != null
				&& formUser.getDeviationLevel().getId() == null) {
			formUser.setDeviationLevel(null);
		}
		
		if (formUser.getUserClassification() != null
				&& formUser.getUserClassification().getId() == null) {
			formUser.setUserClassification(null);
		}
		
		if (formUser.getUserCategory() != null
				&& formUser.getUserCategory().getId() == null) {
			formUser.setUserCategory(null);
		}
		return formUser;
	}

	public List<OrgBranchInfo> formOrganizationBranchTree(List<OrgBranchInfo> topBranchIds,
			String systemName,Set<OrganizationBranch> accessibleBranches, Set<Long> parentBranchIds){
		List<OrgBranchInfo> branchList = new ArrayList<OrgBranchInfo>();
		for (OrgBranchInfo branch : topBranchIds) {
			OrgBranchInfo branchTree = new OrgBranchInfo();
			branchTree.setTitle(branch.getOrgName());
			branchTree.setKey((branch.getId()).toString());
			if (branch.getChildOrgCount()  != 0L || OrganizationType.ORGANIZATION_TYPE_BRANCH.equals(branch.getOrganizationType())) {
				branchTree.setLazy(false);
				branchTree.setOrganizationType(branch.getOrganizationType());
				List<Boolean> flags = formTreeNodes(branch.getId(), branchTree, systemName,accessibleBranches,
						parentBranchIds,new ArrayList<Boolean>());

				if(flags.contains(true)){
					branchList.add(branchTree);
				}
			} 


		}
		return branchList;
	}

	private List<Boolean> formTreeNodes(Long branchid,final OrgBranchInfo branchTree,String systemName, 
			Set<OrganizationBranch> accessibleBranches, Set<Long> parentBranches, final List<Boolean> flags){
		final List<OrgBranchInfo> childBranches = organizationService.getUniqueChildBranchesTree(branchid, systemName);
		final List<OrgBranchInfo> childList = new ArrayList<OrgBranchInfo>();
		for (final OrgBranchInfo child : childBranches) {
			if(!child.getId().equals(branchid))
			{
				if(OrganizationType.ORGANIZATION_TYPE_BRANCH.equals(child.getOrganizationType()) &&
						checkIfBranchIdExistsInAdminList(accessibleBranches,child.getId())){
					if(!flags.contains(true)){
						flags.add(true);
					}
					final OrgBranchInfo childTree = new OrgBranchInfo();
					childTree.setTitle(child.getOrgName());
					childTree.setKey(child.getId().toString());
					childTree.setLazy(false);
					childTree.setOrganizationType(OrganizationType.ORGANIZATION_TYPE_BRANCH);
					childList.add(childTree);
					if(child.getChildOrgCount() != 0L){
						List<OrgBranchInfo> childBranchesList = organizationService.getUniqueChildBranchesTree(child.getId(), systemName);
						for(OrgBranchInfo childOfBranch : childBranchesList){
							final OrgBranchInfo childBranchTree = new OrgBranchInfo();
							childBranchTree.setTitle(childOfBranch.getOrgName());
							childBranchTree.setKey(childOfBranch.getId().toString());
							childBranchTree.setLazy(false);
							if(checkIfBranchIdExistsInAdminList(accessibleBranches,childOfBranch.getId())){
								childBranchTree.setOrganizationType(OrganizationType.ORGANIZATION_TYPE_BRANCH);
								childList.add(childBranchTree);
								getChildBranchesForBranchType(childOfBranch.getId(), childList, systemName, accessibleBranches);
							}
						}
					}
				}else if (parentBranches.contains(child.getId())){
					final OrgBranchInfo newParentBranchTree = new OrgBranchInfo();
					newParentBranchTree.setTitle(child.getOrgName());
					newParentBranchTree.setKey((child.getId()).toString());
					newParentBranchTree.setLazy(false);
					childList.add(newParentBranchTree);
					formTreeNodes(child.getId(),newParentBranchTree,systemName,accessibleBranches,parentBranches,flags);
				}else{
					if(OrganizationType.ORGANIZATION_TYPE_BRANCH.equals(child.getOrganizationType()) &&
							checkIfBranchIdExistsInAdminList(accessibleBranches,child.getId())){
						final OrgBranchInfo finalLeaf = new OrgBranchInfo();
						finalLeaf.setTitle(child.getOrgName());
						finalLeaf.setKey((child.getId()).toString());
						finalLeaf.setLazy(false);
						finalLeaf.setOrganizationType(child.getOrganizationType());
						childList.add(finalLeaf);
					}
				}
			}
		}
		branchTree.setChildren(childList);
		return flags;

	}

	public List<OrgBranchTree> formOrganizationBranchTreeTopLevel(List<OrgBranchInfo> topBranchIds,
																  String systemName,Set<OrganizationBranch> accessibleBranches, Set<Long> parentBranchIds){
		List<OrgBranchTree> branchList = new ArrayList<OrgBranchTree>();
		for(OrgBranchInfo branch : topBranchIds) {
			OrgBranchTree branchTree = new OrgBranchTree();
			branchTree.setTitle(branch.getOrgName());
			branchTree.setKey((branch.getId()).toString());
			branchTree.setUrl("/app/UserInfo/user/branchTreeLevels");
			if(branch.getChildOrgCount() != 0L)
			{
				branchTree.setOrganizationType(branch.getOrganizationType());
				branchTree.setIsLazy(true);
			}
			else
				branchTree.setIsLazy(false);

			branchList.add(branchTree);

		}
		return branchList;
	}

	public List<OrgBranchTree> formLazyBranchTreeNodes(Long branchid,String systemName,
													   Set<OrganizationBranch> accessibleBranches, Set<Long> parentBranches){
		final List<OrgBranchInfo> childBranches = organizationService.getUniqueChildBranchesTree(branchid, systemName);
		final List<OrgBranchTree> childList = new ArrayList<OrgBranchTree>();
		for (final OrgBranchInfo child : childBranches) {
			if(!child.getId().equals(branchid))
			{
				if(OrganizationType.ORGANIZATION_TYPE_BRANCH.equals(child.getOrganizationType()) &&
						checkIfBranchIdExistsInAdminList(accessibleBranches,child.getId())){
					final OrgBranchTree childTree = new OrgBranchTree();
					childTree.setTitle(child.getOrgName());
					childTree.setKey(child.getId().toString());
					childTree.setIsLazy(false);
					childTree.setOrganizationType(OrganizationType.ORGANIZATION_TYPE_BRANCH);
					childList.add(childTree);
					if(child.getChildOrgCount() != 0L){
						List<OrgBranchInfo> childBranchesList = organizationService.getUniqueChildBranchesTree(child.getId(), systemName);
						for(OrgBranchInfo childOfBranch : childBranchesList){
							final OrgBranchTree childBranchTree = new OrgBranchTree();
							childBranchTree.setTitle(childOfBranch.getOrgName());
							childBranchTree.setKey(childOfBranch.getId().toString());
							childBranchTree.setIsLazy(false);

							if(checkIfBranchIdExistsInAdminList(accessibleBranches,childOfBranch.getId())){
								childBranchTree.setOrganizationType(OrganizationType.ORGANIZATION_TYPE_BRANCH);
								childList.add(childBranchTree);
								getChildOrgBranchesForBranchType(childOfBranch.getId(), childList, systemName, accessibleBranches);
							}
						}
					}
				}else if (parentBranches.contains(child.getId())){
					final OrgBranchTree newParentBranchTree = new OrgBranchTree();
					newParentBranchTree.setTitle(child.getOrgName());
					newParentBranchTree.setKey((child.getId()).toString());
					newParentBranchTree.setIsLazy(true);
					newParentBranchTree.setUrl("/app/UserInfo/user/branchTreeLevels");
					childList.add(newParentBranchTree);

				}else{
					if(OrganizationType.ORGANIZATION_TYPE_BRANCH.equals(child.getOrganizationType()) &&
							checkIfBranchIdExistsInAdminList(accessibleBranches,child.getId())){
						final OrgBranchTree finalLeaf = new OrgBranchTree();
						finalLeaf.setTitle(child.getOrgName());
						finalLeaf.setKey((child.getId()).toString());
						finalLeaf.setIsLazy(false);
						finalLeaf.setOrganizationType(child.getOrganizationType());
						childList.add(finalLeaf);
					}
				}
			}
		}

		return childList;

	}
	private void getChildOrgBranchesForBranchType(Long branchid, final List<OrgBranchTree> childList,String systemName,
												  Set<OrganizationBranch> accessibleBranches) {
		List<OrgBranchInfo> childBranchesListforBranchType = organizationService.getUniqueChildBranchesTree(branchid, systemName);
		for(OrgBranchInfo childOfBranchForBranchType : childBranchesListforBranchType) {
			final OrgBranchTree childBranchTree = new OrgBranchTree();
			childBranchTree.setTitle(childOfBranchForBranchType.getOrgName());
			childBranchTree.setKey(childOfBranchForBranchType.getId().toString());
			childBranchTree.setIsLazy(false);

			if(checkIfBranchIdExistsInAdminList(accessibleBranches,childOfBranchForBranchType.getId())) {
				childBranchTree.setOrganizationType(OrganizationType.ORGANIZATION_TYPE_BRANCH);
				childList.add(childBranchTree);
				if(childOfBranchForBranchType.getChildOrgCount() != 0L) {
					getChildOrgBranchesForBranchType(childOfBranchForBranchType.getId(), childList, systemName, accessibleBranches);
				}
			}
		}
	}

	public boolean checkIfBranchIdExistsInAdminList(Set<OrganizationBranch> accessibleBranches,Long branchId){
		for(OrganizationBranch branch : accessibleBranches){
			if(branch.getId().equals(branchId)){
				return true;
			}
		}
		return false;
	}

	private void getChildBranchesForBranchType(Long branchid, final List<OrgBranchInfo> childList,String systemName, 
			Set<OrganizationBranch> accessibleBranches) {
		List<OrgBranchInfo> childBranchesListforBranchType = organizationService.getUniqueChildBranchesTree(branchid, systemName);
		for(OrgBranchInfo childOfBranchForBranchType : childBranchesListforBranchType) {
			final OrgBranchInfo childBranchTree = new OrgBranchInfo();
			childBranchTree.setTitle(childOfBranchForBranchType.getOrgName());
			childBranchTree.setKey(childOfBranchForBranchType.getId().toString());
			childBranchTree.setLazy(false);
			if(checkIfBranchIdExistsInAdminList(accessibleBranches,childOfBranchForBranchType.getId())) {
				childBranchTree.setOrganizationType(OrganizationType.ORGANIZATION_TYPE_BRANCH);
				childList.add(childBranchTree);
				if(childOfBranchForBranchType.getChildOrgCount() != 0L) {
					getChildBranchesForBranchType(childOfBranchForBranchType.getId(), childList, systemName, accessibleBranches);
				}
			}
		}     
	}


	private boolean validLengthForDeviceIdentifier(UserDeviceMapping udm){
	    DeviceIdentifierType deviceType=genericParameterService.findById(udm.getDeviceType().getId(), DeviceIdentifierType.class);
	       ConfigurationVO fcmIdLength = configurationService.getConfigurationPropertyFor(
	                SystemEntity.getSystemEntityId(), Configuration.FCMID_LENGTH);
	    if("FCMID".equals(deviceType.getCode())){
	        if(Integer.valueOf(fcmIdLength.getPropertyValue()) >udm.getDeviceId().length()&&udm.getDeviceId().length()>0)
	            return true;
	        else return false;
	    }
		//only digits are allowed.So, returning false on parse
		try {
			Long.parseLong(udm.getDeviceId());
		} catch (NumberFormatException e) {
			BaseLoggers.exceptionLogger.error("Number Format Exception for device id >>>"+udm.getDeviceId());
			return false;
		}
		
		ConfigurationVO imeiLength = configurationService.getConfigurationPropertyFor(
				SystemEntity.getSystemEntityId(), Configuration.IMEI_LENGTH);
		ConfigurationVO meidLength = configurationService.getConfigurationPropertyFor(
				SystemEntity.getSystemEntityId(), Configuration.MEID_LENGTH);

		int imeiLen = 15;
		int meidLen = 14;
		if(imeiLength != null){
			imeiLen = Integer.parseInt(imeiLength.getPropertyValue());
		}
		if(meidLength != null){
			meidLen = Integer.parseInt(meidLength.getPropertyValue());
		}
		if("IMEI".equals(deviceType.getCode()) && udm.getDeviceId().length()==imeiLen){
			return true;
		}

		if("MEID".equals(deviceType.getCode()) && udm.getDeviceId().length()==meidLen){
			return true;
		}

		return false;
	}


	/*
	 * Changed for optimization from n3 to n
	 */
	public Map<String,String>  parseUserOrgBranchProductMappingStringModified(String jsonString){
		/*
		 * Convert json string for updated branch data to Map
		 */
		Map<String,String> updatedBranchProductData = new  HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		if (StringUtils.isNotBlank(jsonString)) {

			try {


				updatedBranchProductData = mapper.readValue(jsonString,
						new TypeReference<Map<String,String>>() {
						});

			} catch (JsonMappingException e) {
				BaseLoggers.exceptionLogger.error("JsonMappingException while converting json string ton updatedBranchProductData" , e);
			}  catch (IOException e) {
				BaseLoggers.exceptionLogger.error("IOException while converting json string to updatedBranchProductData" , e);
			}

		}
		return updatedBranchProductData;
	}
	
	
	/**
	 * @param uid
	 * @return
	 */
	public User markActivatedForMakerCheckerFlow(Long uid) {
		//fetch the original entity 
        Map<String, Object> userMap = userManagementServiceCore.findUserById(uid);
        User user = (User) userMap.get("user");
        
        NeutrinoValidator.notNull(user);
        
        //get the copy to change so that original entity will be remain as it is.
        User changingEntity  =  (User) user.cloneYourself(MAKER_CHECKER_CLONING_OPTION);
        changingEntity.setId(user.getId());
        
        if(user.isAccountLocked()){
        	changingEntity.unlock();
        	changingEntity.setDaysToBlock(null);
        }else{
        	throw new SystemException("User is not marked locked for maker checker flow"); 
        }
        
		return changingEntity;
	}
	
	/**
	 * @param uid
	 * @return User
	 */
	public User markInActivateForMakerCheckerFlow(Long uid) {
		//fetch the original entity 
        Map<String, Object> userMap = userManagementServiceCore.findUserById(uid);
        User user = (User) userMap.get("user");
        
        NeutrinoValidator.notNull(user);
        
        //get the copy to change so that original entity will be remain as it is.
        User changingEntity  =  (User) user.cloneYourself(MAKER_CHECKER_CLONING_OPTION);
        
        changingEntity.setId(user.getId());
        changingEntity.setUserStatus(UserStatus.STATUS_INACTIVE);
		
		return changingEntity;
	}
	
	/**
	 * @param uid
	 * @param byUserEntityId
	 * @param daysToBlock
	 * @return User
	 */
	public User markBlockedForMakerChecker(Long uid, EntityId byUserEntityId, Integer daysToBlock) {
		//fetch the original entity 
        Map<String, Object> userMap = userManagementServiceCore.findUserById(uid);
        User user = (User) userMap.get("user");
        
        NeutrinoValidator.notNull(user);
        
        //get the copy to change so that original entity will be remain as it is.
        User changingEntity  =  (User) user.cloneYourself(MAKER_CHECKER_CLONING_OPTION);
        changingEntity.setId(user.getId());
        
		//if User is system User then cannot be marked as blocked
		if(isNotBlank(user.getUsername()) && !(user.getUsername().equalsIgnoreCase(NEUTRINO_SYSTEM_USER))){
			changingEntity.setUserStatus(UserStatus.STATUS_LOCKED);
			changingEntity.setLastLockedDate(DateUtils.getCurrentUTCTime());
            if (daysToBlock != null && daysToBlock > 0) {
            	changingEntity.setDaysToBlock(daysToBlock);
            }
        }else {
        	 throw new InvalidDataException("User cannot be blocked");
        }
		return changingEntity;
	}
}
