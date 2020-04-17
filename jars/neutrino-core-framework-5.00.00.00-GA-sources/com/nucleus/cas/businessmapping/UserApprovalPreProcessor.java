/**
 * 
 */
package com.nucleus.cas.businessmapping;

import static com.nucleus.entity.ApprovalStatus.APPROVED;
import static com.nucleus.entity.ApprovalStatus.UNAPPROVED_HISTORY;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;


import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.service.OrganizationService;
import org.apache.commons.collections.CollectionUtils;

import com.nucleus.user.*;
import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.businessmapping.service.UserBPMappingService;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.config.persisted.enity.ConfigurationGroup;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.SelectiveMapping;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.organization.entity.UserBPMapping;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.process.beans.EntityApprovalPreProcessor;

/**
 * @author om.giri
 * 
 */



@Named("userApprovalPreProcessor")
public class UserApprovalPreProcessor implements
		EntityApprovalPreProcessor {

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;

	@Inject
    @Named("userManagementService")
    private UserManagementService       userManagementService;
	
	 @Inject
	 @Named("userService")
	 private UserService                   userService;
	 
	 @Inject
	 @Named("teamService")
	 private TeamService                 teamService;
	 
	 @Inject
	 @Named("configurationService")
	 private ConfigurationService        configurationService;
	 
	 @Inject
	 @Named("userManagementServiceCore")
     private UserManagementServiceCore   userManagementServiceCore;
	 
	 @Inject
	 @Named("userBPMappingService")
	 private UserBPMappingService   userBPMappingService;
	 
	@Inject
	@Named("userCityVillageMappingService")
	private UserCityVillageMappingService userCityVillageMappingService;

	@Inject
	@Named("organizationService")
	private OrganizationService organizationService;

	/**
	 * @param originalUserRecord
	 * @param changedEntity
	 * @param userEntityId
	 */
	private void processUserOrgBranchMapping(User originalUserRecord,
			User changedEntity, User clonedEntity, EntityId userEntityId) {

		List<UserOrgBranchMapping> changedUserOrgBranchMappings = userManagementService
				.getUserOrgBranchMapping(changedEntity.getId());

		for (UserOrgBranchMapping lastUpdatedEntity : changedUserOrgBranchMappings) {
			
			if (ValidatorUtils.notNull(originalUserRecord)
					&& lastUpdatedEntity.getOperationType().equalsIgnoreCase(
							SelectiveMapping.ADDITION_OPERATION)) {
				lastUpdatedEntity.setAssociatedUser(originalUserRecord);
				processUserOrgBranchMapAddition(lastUpdatedEntity, clonedEntity, userEntityId);
			} else if (lastUpdatedEntity.getOperationType().equalsIgnoreCase(
					SelectiveMapping.MODIFICATION_OPERATION)) {
				
				processUserOrgBranchMapModification(originalUserRecord,
						lastUpdatedEntity, clonedEntity, userEntityId);
				entityDao.delete(lastUpdatedEntity);
			} else if (lastUpdatedEntity.getOperationType().equalsIgnoreCase(
					SelectiveMapping.DELETION_OPERATION)) {
				processUserOrgBranchMapDeletion(originalUserRecord,
						lastUpdatedEntity, clonedEntity, userEntityId);
				entityDao.delete(lastUpdatedEntity);
				
			}else{
				processUserOrgBranchMapAddition(lastUpdatedEntity, clonedEntity, userEntityId);
			}
		}

	}
	
	
	/**
	 * @param originalUserRecord
	 * @param changedEntity
	 * @param clonedEntity
	 */
	private void processUserRoleMapping(User originalUserRecord,
			User changedEntity, User clonedEntity) {
		
		List<Role> originalRoleList = userService.getRolesFromUserId(originalUserRecord.getId());
		List<Role> changeRoleList = userService.getRolesFromUserId(changedEntity.getId());
		
		saveRolesForUser(clonedEntity, originalRoleList);
		saveRolesForUser(originalUserRecord, changeRoleList);
		removeAllRolesForUser(changedEntity);
		
	}
	
	/**
	 * @param originalUserRecord
	 * @param changedEntity
	 * @param clonedEntity
	 */
	private void processUserTeamMapping(User originalUserRecord,
			User changedEntity, User clonedEntity) {
		
		List<Long> originalTeamIds = teamService.getTeamIdAssociatedToUserByUserId(originalUserRecord.getId());
		List<Long> changedTeamIds = teamService.getTeamIdAssociatedToUserByUserId(changedEntity.getId());
		
		userManagementService.saveTeamsForUser(clonedEntity, originalTeamIds.toArray(new Long[originalTeamIds.size()]));
		userManagementService.saveTeamsForUser(originalUserRecord, changedTeamIds.toArray(new Long[changedTeamIds.size()]));
		removeAllTeamsForUser(changedEntity);
		/*BusinessPartner mappedBP = userManagementServiceCore.getBusinessPartnerByUserId(changedEntity.getId());
		BusinessPartner associatedBP=null;
		if(mappedBP!=null){
			associatedBP = baseMasterService.getMasterEntityById(BusinessPartner.class,mappedBP.getId());
		}*/
		
		String associatedBPCode =  userBPMappingService.getAssociatedBPCodeByUserId(changedEntity.getId());
		if (!org.apache.commons.lang.StringUtils.isEmpty(associatedBPCode)) {
			Team team = teamService.getTeamByTeamName(associatedBPCode);
			if (team != null) {
            Set<User> userSet = team.getUsers();

            if (changedEntity.getTeamLead()) {
                team.setTeamLead(originalUserRecord);
                teamService.saveTeamsForUser(team);

            } 

            if (userSet!=null && !userSet.contains(originalUserRecord)) {
                userSet.add(originalUserRecord);
                team.setUsers(userSet);
                teamService.saveTeamsForUser(team);
            }

			}
		}
		
	}
	
	/**
	 * @param originalUserRecord
	 * @param changedEntity
	 * @param clonedEntity
	 */
	private void processUserProfile(User originalUserRecord,
			User changedEntity, User clonedEntity) {
		
		UserProfile originalUserProfile = userService.getUserProfile(originalUserRecord);
		UserProfile changeUserProfile = userService.getUserProfile(changedEntity);
		if (null != changeUserProfile) {
			changeUserProfile.setAssociatedUser(originalUserRecord);
		}
		if(null != originalUserProfile)
		{
		originalUserProfile.setAssociatedUser(clonedEntity);
		}
	}
	
	private void processUserPreferences(User originalUserRecord,
			User changedEntity, User clonedEntity) {
		
		EntityId originalUserEntityId = originalUserRecord.getEntityId();
		EntityId changedUserEntityId = changedEntity.getEntityId();
		EntityId clonedUserEntityId = clonedEntity.getEntityId();
		
		ConfigurationGroup originalEntityConfigurationGroup = configurationService.getConfigurationGroupFor(originalUserEntityId, false);
		originalEntityConfigurationGroup.setAssociatedEntityId(clonedUserEntityId);

		ConfigurationGroup changedEntityConfigurationGroup = configurationService.getConfigurationGroupFor(changedUserEntityId, false);
		changedEntityConfigurationGroup.setAssociatedEntityId(originalUserEntityId);

	}
	
	/**
	 * @param originalUserRecord
	 * @param changedEntity
	 * @param clonedEntity
	 */
	private void processUserBusinessPartner(User originalUserRecord,
			User changedEntity, User clonedEntity) {
		
		UserBPMapping originalUserBPMapping = userService.getBPMappedToUser(originalUserRecord.getId());
		UserBPMapping changedUserBPMapping = userService.getBPMappedToUser(changedEntity.getId());
		if (null != changedUserBPMapping){
		changedUserBPMapping.setAssociatedUser(originalUserRecord);
		}
		if (null != originalUserBPMapping){
		originalUserBPMapping.setAssociatedUser(clonedEntity);
		}
	}
	
	
	    /**
	     * @param user
	     * @param toBeUpdateRoleList
	     */
	
	private void saveRolesForUser(User user, List<Role> toBeUpdateRoleList) {

		List<Role> roleListFromDB = userService.getRolesFromUserId(user.getId());
		List<Role> roleList=new ArrayList<>();
		for(Role roleFromDB:roleListFromDB)
		{
			roleList.add(roleFromDB);
		}
		if (toBeUpdateRoleList != null) {
			for (int i = 0; i < toBeUpdateRoleList.size(); i++) { 
				Role role = new Role();
				role.setId(toBeUpdateRoleList.get(i).getId());
				// TODO use alternate method instead of contains in below line to improve performance
				if (roleList.contains(role)) {
					roleList.remove(role);
				} else {
					role = userService.getRoleById(role.getId());
					if (role.getUsers() == null) {
						Set<User> userSet = new HashSet<User>();
						role.setUsers(userSet);
					}
					roleListFromDB.add(role);
					role.getUsers().add(user);
					userService.saveRole(role);
				}
			}
		}
		
			for (Role userRole : roleList) {
				roleListFromDB.remove(userRole);
				userRole.getUsers().remove(user);
				userService.saveRole(userRole);
			}
		

	}
	    
	    /**
	     * @param user
	     */
	// TODO need to look deadlock condition
	private void removeAllRolesForUser(User user) {
		List<Role> roleList = userService.getRolesFromUserId(user.getId());
		if (roleList != null) {
			for (Role userRole : roleList) {
				userRole.getUsers().remove(user);
				userService.saveRole(userRole);
			}
		}
	}
	
	/**
	 * 
	 * @param user
	 */
	private void removeAllTeamsForUser(User user) {
		List<Long> teamIds = teamService.getAllTeamIdAssociatedToUserByUserId(user.getId());
		if (CollectionUtils.isNotEmpty(teamIds)) {
			for (Long teamId : teamIds) {
				Team team = teamService.getTeamByTeamId(teamId);
				Set<User> userSet = team.getUsers();
				if (CollectionUtils.isNotEmpty(userSet)) {
					userSet.remove(user);
					team.setUsers(userSet);
				}
				teamService.saveTeamsForUser(team);
			}
		}
	}
	 
	/**
	 * @param originalRecord
	 * @param changedEntity
	 * @param userEntityId
	 */
	private void processUserOrgBranchMappingDecline(User originalRecord,
			User changedEntity, EntityId userEntityId) {
		
		List<UserOrgBranchMapping> changedUserOrgBranchMappings = userManagementService
				.getUserOrgBranchMapping(changedEntity.getId());

		for (UserOrgBranchMapping lastUpdatedEntity : changedUserOrgBranchMappings) {
			processUserOrgBranchMapDecline(lastUpdatedEntity, userEntityId);
		}
		
		}

	/**
	 * @param originalUserRecord
	 * @param lastUpdatedEntity
	 * @param userEntityId
	 */
	void processUserOrgBranchMapModification(User originalUserRecord,
			UserOrgBranchMapping lastUpdatedEntity,User clonedUserEntity, EntityId userEntityId) {
		UserOrgBranchMapping initialEntity = (UserOrgBranchMapping) getInitialUserOrgBranchEntity(originalUserRecord,
				lastUpdatedEntity);

		if (initialEntity != null) {
			lastUpdatedEntity.getMasterLifeCycleData().setReviewedByEntityId(
					userEntityId);
			lastUpdatedEntity.getMasterLifeCycleData().setReviewedTimeStamp(
					DateUtils.getCurrentUTCTime());

			initialEntity.getMasterLifeCycleData().setReviewedByEntityId(
					userEntityId);
			initialEntity.getMasterLifeCycleData().setReviewedTimeStamp(
					DateUtils.getCurrentUTCTime());

			initialEntity.getEntityLifeCycleData().setLastUpdatedByUri(
					lastUpdatedEntity.getEntityLifeCycleData()
							.getCreatedByUri());
			
			UserOrgBranchMapping clonedSourceEntity = (UserOrgBranchMapping) initialEntity
					.cloneYourself(CloneOptionConstants.MAKER_CHECKER_CLONING_OPTION);
			clonedSourceEntity.setAssociatedUser(clonedUserEntity);
			clonedSourceEntity.setApprovalStatus(UNAPPROVED_HISTORY);
			clonedSourceEntity.getEntityLifeCycleData().setCreatedByUri(
					lastUpdatedEntity.getEntityLifeCycleData()
							.getCreatedByUri());
			entityDao.persist(clonedSourceEntity);
			processUserOrgBranchProdEntities(lastUpdatedEntity, initialEntity, clonedSourceEntity, userEntityId);
			initialEntity.copyFrom(lastUpdatedEntity,
					CloneOptionConstants.MAKER_CHECKER_COPY_OPTION);
			initialEntity.setAssociatedUser(originalUserRecord);
			initialEntity.setApprovalStatus(APPROVED);
			processUserOrgBranchProdEntityDeletion(lastUpdatedEntity,clonedSourceEntity, userEntityId);
		}
	}
	
	/**
	 * @param lastUpdatedEntity
	 * @param userEntityId
	 */
	void processUserOrgBranchMapAddition(
			UserOrgBranchMapping lastUpdatedEntity, User clonedEntity, EntityId userEntityId) {
		lastUpdatedEntity.setApprovalStatus(ApprovalStatus.APPROVED);
		lastUpdatedEntity.getMasterLifeCycleData().setReviewedByEntityId(
				userEntityId);
		lastUpdatedEntity.getMasterLifeCycleData().setReviewedTimeStamp(
				DateUtils.getCurrentUTCTime());
		UserOrgBranchMapping clonedSourceEntity = (UserOrgBranchMapping) lastUpdatedEntity
				.cloneYourself(CloneOptionConstants.MAKER_CHECKER_CLONING_OPTION);
		clonedSourceEntity.setAssociatedUser(clonedEntity);
		clonedSourceEntity.setApprovalStatus(UNAPPROVED_HISTORY);
		clonedSourceEntity.getEntityLifeCycleData().setCreatedByUri(
				lastUpdatedEntity.getEntityLifeCycleData()
						.getCreatedByUri());
		entityDao.persist(clonedSourceEntity);
		processUserOrgBranchProdEntities(lastUpdatedEntity, lastUpdatedEntity, clonedSourceEntity, userEntityId);
	}
	
	
	/**
	 * @param lastUpdatedEntity
	 * @param userEntityId
	 */
	void processUserOrgBranchMapDecline(
			UserOrgBranchMapping lastUpdatedEntity, EntityId userEntityId) {
		lastUpdatedEntity.setApprovalStatus(ApprovalStatus.UNAPPROVED);
		lastUpdatedEntity.getMasterLifeCycleData().setReviewedByEntityId(
				userEntityId);
		lastUpdatedEntity.getMasterLifeCycleData().setReviewedTimeStamp(
				DateUtils.getCurrentUTCTime());
		processUserOrgBranchProdEntitiesDecline(lastUpdatedEntity, lastUpdatedEntity, userEntityId);
	}

	/**
	 * @param originalUserRecord
	 * @param lastUpdatedEntity
	 * @param userEntityId
	 */
	void processUserOrgBranchMapDeletion(User originalUserRecord,
			UserOrgBranchMapping lastUpdatedEntity, User clonedUser, EntityId userEntityId) {
		UserOrgBranchMapping initialEntity = getInitialUserOrgBranchEntity(originalUserRecord,
				lastUpdatedEntity);
		if (ValidatorUtils.notNull(initialEntity)) {
			initialEntity.setAssociatedUser(clonedUser);
			initialEntity
					.setApprovalStatus(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
			initialEntity.getMasterLifeCycleData().setReviewedByEntityId(
					userEntityId);
			initialEntity.getMasterLifeCycleData().setReviewedTimeStamp(
					DateUtils.getCurrentUTCTime());
			initialEntity.getEntityLifeCycleData().setLastUpdatedByUri(
					lastUpdatedEntity.getEntityLifeCycleData()
							.getCreatedByUri());
			UserOrgBranchMapping clonedSourceEntity = (UserOrgBranchMapping) initialEntity
					.cloneYourself(CloneOptionConstants.MAKER_CHECKER_CLONING_OPTION);
			clonedSourceEntity.setAssociatedUser(clonedUser);
			clonedSourceEntity.setApprovalStatus(UNAPPROVED_HISTORY);
			clonedSourceEntity.getEntityLifeCycleData().setCreatedByUri(
					lastUpdatedEntity.getEntityLifeCycleData()
							.getCreatedByUri());
			entityDao.persist(clonedSourceEntity);
			processUserOrgBranchProdEntities(lastUpdatedEntity, initialEntity, clonedSourceEntity, userEntityId);
			initialEntity.copyFrom(lastUpdatedEntity,
					CloneOptionConstants.MAKER_CHECKER_COPY_OPTION);
			
		}
	}

	/**
	 * @param originalUserRecord
	 * @param lastUpdatedEntity
	 * @return initialEntity
	 */
		private UserOrgBranchMapping getInitialUserOrgBranchEntity(User originalUserRecord,
			UserOrgBranchMapping lastUpdatedEntity) {
			UserOrgBranchMapping initialEntity = null;
			if(null != originalUserRecord){
		List<UserOrgBranchMapping> originalUserOrgBranchMappings = userManagementService
				.getUserOrgBranchMappingByBranchAndUserID(
						lastUpdatedEntity.getOrganizationBranchId(),
						originalUserRecord.getId());
		if (originalUserOrgBranchMappings.size() > 0) {
			initialEntity = originalUserOrgBranchMappings.get(0);
		}
			}

		return initialEntity;
	}
	
	
	/**
	 * @param userOrgBranchId
	 * @return orgBranchProdMappings
	 */
	private List<UserOrgBranchProdMapping> getUserOrgBranchProdEntities(Long userOrgBranchId) {
		
		List<UserOrgBranchProdMapping> orgBranchProdMappings = userManagementService.findOrgBranchProdMappingsByUserOrgBranche(userOrgBranchId);
		return orgBranchProdMappings;
	}

	private List<UserOrgBranchProdMapping> getAllUserOrgBranchProdEntities(List<Long> userOrgBranchId) {

		List<UserOrgBranchProdMapping> orgBranchProdMappings = userManagementService.findAllOrgBranchProdMappingsByUserOrgBranche(userOrgBranchId);

		return orgBranchProdMappings;
	}
	
	private List<UserOrgBranchProdMapping> getUserOrgBranchProdEntityByLoanProductAndBranchMapping(Long loanProductId, Long userOrgBranchMappingId) {
		
		List<UserOrgBranchProdMapping> orgBranchProdMappings = userManagementService.findOrgBranchProdMappingsByProductAndAndBranchMapping(loanProductId, userOrgBranchMappingId);
		return orgBranchProdMappings;
	}
	
	
	/**
	 * @param lastUpdatedUserOrgBranchMapEntity
	 * @param clonedSourceEntity
	 * @param userEntityId
	 */
	private void processUserOrgBranchProdEntities(UserOrgBranchMapping lastUpdatedUserOrgBranchMapEntity, UserOrgBranchMapping sourceEntity, UserOrgBranchMapping clonedSourceEntity, EntityId userEntityId) {
		
		List<UserOrgBranchProdMapping> orgBranchProdMappings =  getUserOrgBranchProdEntities(lastUpdatedUserOrgBranchMapEntity.getId());
		for(UserOrgBranchProdMapping lastUpdatedEntity : orgBranchProdMappings)
		{
			if(lastUpdatedEntity.getOperationType().equalsIgnoreCase(SelectiveMapping.ADDITION_OPERATION))
			{
				processUserOrgBranchProdEntityAddition(lastUpdatedEntity, sourceEntity, userEntityId);
			}else if(lastUpdatedEntity.getOperationType().equalsIgnoreCase(SelectiveMapping.DELETION_OPERATION))
			{
				processUserOrgBranchProdEntityDeletionApprove(sourceEntity, lastUpdatedEntity, clonedSourceEntity, userEntityId);
			}
		
		}
	}
	
	
/**
 * @param lastUpdatedUserOrgBranchMapEntity
 * @param clonedSourceEntity
 * @param userEntityId
 */
private void processUserOrgBranchProdEntitiesDecline(UserOrgBranchMapping lastUpdatedUserOrgBranchMapEntity, UserOrgBranchMapping clonedSourceEntity, EntityId userEntityId) {
		
		List<UserOrgBranchProdMapping> orgBranchProdMappings =  getUserOrgBranchProdEntities(lastUpdatedUserOrgBranchMapEntity.getId());
		for(UserOrgBranchProdMapping lastUpdatedEntity : orgBranchProdMappings)
		{
			lastUpdatedEntity.setUserOrgBranchMappingId(clonedSourceEntity.getId());
			lastUpdatedEntity.setApprovalStatus(ApprovalStatus.UNAPPROVED);
			lastUpdatedEntity.getMasterLifeCycleData().setReviewedByEntityId(
					userEntityId);
			lastUpdatedEntity.getMasterLifeCycleData().setReviewedTimeStamp(
				DateUtils.getCurrentUTCTime());
		
		}
	}
	
	
	/**
	 * @param lastUpdatedEntity
	 * @param clonedSourceEntity
	 * @param userEntityId
	 */
	private void processUserOrgBranchProdEntityAddition(UserOrgBranchProdMapping lastUpdatedEntity, UserOrgBranchMapping clonedSourceEntity, EntityId userEntityId) {
			
				lastUpdatedEntity.setUserOrgBranchMappingId(clonedSourceEntity.getId());
				lastUpdatedEntity.setApprovalStatus(ApprovalStatus.APPROVED);
				lastUpdatedEntity.getMasterLifeCycleData().setReviewedByEntityId(
						userEntityId);
				lastUpdatedEntity.getMasterLifeCycleData().setReviewedTimeStamp(
					DateUtils.getCurrentUTCTime());
			
	}

	/**
	 * @param initialEntity
	 * @param userEntityId
	 */
	private void processUserOrgBranchProdEntityDeletionApprove(UserOrgBranchMapping sourceEntity,UserOrgBranchProdMapping lastUpdatedEntity, UserOrgBranchMapping clonedUserOrgBranchMapEntity, EntityId userEntityId) {
	
		List<UserOrgBranchProdMapping> orgBranchProdMappings =  getUserOrgBranchProdEntityByLoanProductAndBranchMapping(lastUpdatedEntity.getLoanProductId(), sourceEntity.getId());
		if(orgBranchProdMappings.size()>0)
		{
			entityDao.delete(orgBranchProdMappings.get(0));
		}
		
		if (ValidatorUtils.notNull(lastUpdatedEntity)) {
			lastUpdatedEntity.setUserOrgBranchMappingId(clonedUserOrgBranchMapEntity.getId());
			lastUpdatedEntity
					.setApprovalStatus(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
			lastUpdatedEntity.getMasterLifeCycleData().setReviewedByEntityId(
					userEntityId);
			lastUpdatedEntity.getMasterLifeCycleData().setReviewedTimeStamp(
					DateUtils.getCurrentUTCTime());
		}
		
		
	
	}

	/**
	 * @param lastUpdatedUserOrgBranchMapEntity
	 */
	private void processUserOrgBranchProdEntityDeletion(UserOrgBranchMapping lastUpdatedUserOrgBranchMapEntity, UserOrgBranchMapping clonedSourceEntity, EntityId userEntityId) {
		List<UserOrgBranchProdMapping> orgBranchProdMappings =  getUserOrgBranchProdEntities(lastUpdatedUserOrgBranchMapEntity.getId());
		for(UserOrgBranchProdMapping lastUpdatedEntity : orgBranchProdMappings)
		{
			lastUpdatedEntity.setUserOrgBranchMappingId(clonedSourceEntity.getId());
			lastUpdatedEntity
			.setApprovalStatus(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
			lastUpdatedEntity.getMasterLifeCycleData().setReviewedByEntityId(
			userEntityId);
			lastUpdatedEntity.getMasterLifeCycleData().setReviewedTimeStamp(
			DateUtils.getCurrentUTCTime());
		}
	
	}

	public void setEntityDao(EntityDao entityDao) {
		this.entityDao = entityDao;
	}

	public void setBaseMasterService(BaseMasterService baseMasterService) {
		this.baseMasterService = baseMasterService;
	}

	public void setUserManagementService(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}



	/* (non-Javadoc)
	 * @see com.nucleus.process.beans.EntityApprovalPreProcessor#handleApprovalForModification(com.nucleus.master.BaseMasterEntity, com.nucleus.master.BaseMasterEntity, com.nucleus.master.BaseMasterEntity, java.lang.Long)
	 */
	@Override
	public void handleApprovalForModification(BaseMasterEntity originalRecord,
			BaseMasterEntity toBeDeletedRecord,
			BaseMasterEntity toBeHistoryRecord,
			Long reviewerId) {
		
		
		EntityId userEntityId = new EntityId(User.class, reviewerId);
		User changedEntity = (User) toBeDeletedRecord;
		User originalUserRecord = (User)originalRecord;
		User clonedUserRecord = (User)toBeHistoryRecord;
		
		/** Mapping branch and branch_product with approve user **/
		/** Mapping roles with approve user **/
		if(ValidatorUtils.notNull(originalUserRecord))
		{
			processAuditProductData(originalUserRecord,changedEntity,clonedUserRecord);
			processUserRoleMapping(originalUserRecord, changedEntity, clonedUserRecord);
			processUserProfile(originalUserRecord, changedEntity, clonedUserRecord);
			processUserTeamMapping(originalUserRecord, changedEntity, clonedUserRecord);
			processUserPreferences(originalUserRecord, changedEntity, clonedUserRecord);
			processUserBusinessPartner(originalUserRecord, changedEntity, clonedUserRecord);
			processUserOrgBranchMapping(originalUserRecord, changedEntity, clonedUserRecord, userEntityId);
			processUserCityVillageMapping(originalUserRecord, changedEntity, clonedUserRecord);
			processUserProdSchemeMapping(originalUserRecord, changedEntity, clonedUserRecord, userEntityId);
            processUserDefaultUrlMapping(originalUserRecord,changedEntity,clonedUserRecord,userEntityId);
		}

		
	}

    private void processUserDefaultUrlMapping(User originalUserRecord,
                                              User changedEntity, User clonedEntity, EntityId userEntityId) {
        List<UserDefaultUrlMapping> newUrlMappings=userManagementService.getAllUrlMappingsOfUser(changedEntity.getId());
        List<UserDefaultUrlMapping> originalUrlMappings=userManagementService.getAllUrlMappingsOfUser(originalUserRecord.getId());
        userManagementService.removeAllUrlMappingsOfUser(originalUserRecord.getId());
        userManagementService.removeAllUrlMappingsOfUser(changedEntity.getId());
        userManagementService.removeAllUrlMappingsOfUser(clonedEntity.getId());
        UserDefaultUrlMapping tempMapping=null;

        if(CollectionUtils.isNotEmpty(newUrlMappings)) {
            for (UserDefaultUrlMapping newMapping : newUrlMappings) {
                tempMapping=new UserDefaultUrlMapping();
                tempMapping.setMappedUser(originalUserRecord);
                tempMapping.setMenuEntity(newMapping.getMenuEntity());
                tempMapping.setSourceProduct(newMapping.getSourceProduct());
                entityDao.persist(tempMapping);
            }
        }
        if(CollectionUtils.isNotEmpty(originalUrlMappings)) {
            for (UserDefaultUrlMapping originalMapping : originalUrlMappings) {
                tempMapping=new UserDefaultUrlMapping();
                tempMapping.setMappedUser(clonedEntity);
                tempMapping.setMenuEntity(originalMapping.getMenuEntity());
                tempMapping.setSourceProduct(originalMapping.getSourceProduct());
                entityDao.persist(tempMapping);
            }
        }

    }



    private void processAuditProductData(User originalUserRecord, User changedEntity, User clonedUserRecord) {
		UserAuditTrailVO userAuditTrailVO = new UserAuditTrailVO();
		Map<Long, Set<String>> productBranchList = new HashMap();
		//products
		List<UserOrgBranchProdMapping> userBranchMapping = userManagementService.getUserOrgBranchProductMapping(originalUserRecord.getId());
		Set<Long> products = new HashSet();
		if(userBranchMapping!=null){
			for (UserOrgBranchProdMapping ubm : userBranchMapping) {
				if (!(products.contains(ubm.getLoanProductId().toString()))) {
					products.add(ubm.getLoanProductId());
				}
			}
		}
		//productBranch
		if (products != null && !products.isEmpty()) {
			for (Long prodId : products) {
				Set<String> branch = new HashSet<>();
				for (UserOrgBranchProdMapping ubm : userBranchMapping) {
					if (String.valueOf(prodId).equals(ubm.getLoanProductId().toString())) {
						branch.add(ubm.getUserOrgBranchMapping().getOrganizationBranch().getBranchCode());
					}
				}

				productBranchList.put(prodId, branch);
			}

		}
		userAuditTrailVO.setProducts(products);
		userAuditTrailVO.setProductBranchList(productBranchList);

		//scheme

		List<UserOrgBranchProdSchemeMapping> userProductSchemeList = userManagementService.getUserProductSchemeList(originalUserRecord.getId());
		Map<Long,List<Long>> productSchemeMap = new HashMap<>();
		if(userProductSchemeList!=null){
			for(UserOrgBranchProdSchemeMapping schemeMapping :userProductSchemeList){
				if(schemeMapping!=null){
                    List<Long> schemes;
				    if(productSchemeMap.containsKey(schemeMapping.getProductId())){
				        schemes = productSchemeMap.get(schemeMapping.getProductId());
                    }else{
                        schemes = new ArrayList<>();
                    }
					schemes.add(schemeMapping.getSchemeId());
				    productSchemeMap.put(schemeMapping.getProductId(),schemes);
				}
			}

		}
		userAuditTrailVO.setSchemeProductMap(productSchemeMap);
		//branches
		List<OrganizationBranch> userOrgBranchList = userManagementServiceCore.getUserOrganizationBranchObject(originalUserRecord.getId());
		Set<Long> branches = new HashSet<Long>();
		if(userOrgBranchList!=null){
			for(OrganizationBranch organizationBranch : userOrgBranchList){
				if(organizationBranch!=null) {
					branches.add(organizationBranch.getId());
				}
			}
		}
		userAuditTrailVO.setBranches(branches);


		//branchAdmin
		List<OrganizationBranch> userBranchAdminList = organizationService.getBranchesWhereUserIsBranchAdmin(originalUserRecord.getId());;
		Set<Long> branchAdmin = new HashSet<Long>();
		if(userBranchAdminList!=null){
			for(OrganizationBranch organizationBranch : userBranchAdminList){
				if(organizationBranch!=null){
					branchAdmin.add(organizationBranch.getId());
				}
			}
		}
		userAuditTrailVO.setBranchAdmin(branchAdmin);

        //roles
        ArrayList<Role> origRoles = new ArrayList<>();
        if(originalUserRecord.getUserRoles()!=null){
            for (Role role : originalUserRecord.getUserRoles()) {
                origRoles.add(role);
            }
        }
        userAuditTrailVO.setRoles(origRoles);

		clonedUserRecord.setAuditTrailVO(userAuditTrailVO);


		//preferences
		UserAuditTrailVO userAuditTrailVOForOgRec = new UserAuditTrailVO();
		if(changedEntity!=null) {
			userAuditTrailVOForOgRec.setLastUpdatedUserId(changedEntity.getId());
		}
		originalUserRecord.setAuditTrailVO(userAuditTrailVOForOgRec);
	}


	/* (non-Javadoc)
	 * @see com.nucleus.process.beans.EntityApprovalPreProcessor#handleApprovalForNew(com.nucleus.master.BaseMasterEntity, com.nucleus.master.BaseMasterEntity, com.nucleus.master.BaseMasterEntity, java.lang.Long)
	 */
	@Override
    public void handleApprovalForNew(BaseMasterEntity originalRecord,
            BaseMasterEntity toBeDeletedRecord,
            BaseMasterEntity toBeHistoryRecord,
            Long reviewerId) {
        EntityId userEntityId = new EntityId(User.class, reviewerId);
        User changedEntity = (User) toBeDeletedRecord;
        User originalUserRecord = (User)originalRecord;
        User clonedUserRecord = (User)toBeHistoryRecord;
		if(ValidatorUtils.notNull(originalUserRecord))
		{
			processUserOrgBranchMapping(originalUserRecord, changedEntity, clonedUserRecord, userEntityId);
		}else{
			List<UserOrgBranchMapping> changedUserOrgBranchMappings = userManagementService
					.getUserOrgBranchMapping(changedEntity.getId());
			List<Long> userOrgBranchIds= new ArrayList<>();
			for (UserOrgBranchMapping lastUpdatedEntity : changedUserOrgBranchMappings) {
				if(SelectiveMapping.ADDITION_OPERATION.equals(lastUpdatedEntity.getOperationType())
						|| SelectiveMapping.MODIFICATION_OPERATION.equals(lastUpdatedEntity.getOperationType())){
					lastUpdatedEntity.setApprovalStatus(APPROVED);


					userOrgBranchIds.add(lastUpdatedEntity.getId());
				}
			}
			List<UserOrgBranchProdMapping> userOrgBranchProdMappingLongMap =getAllUserOrgBranchProdEntities(userOrgBranchIds);
			for(UserOrgBranchProdMapping lastUpdatedProdEntity : userOrgBranchProdMappingLongMap)
			{
				if(SelectiveMapping.ADDITION_OPERATION.equals(lastUpdatedProdEntity.getOperationType())){
					lastUpdatedProdEntity.setApprovalStatus(APPROVED);
				}

			}
		}

	}



	@Override
	public void handleDeclineForModification(BaseMasterEntity originalRecord,
			BaseMasterEntity toBeDeletedRecord,
			Long reviewerId) {
		
		EntityId userEntityId = new EntityId(User.class, reviewerId);
		User originalUserRecord = (User)originalRecord;
		User changedEntity = (User) toBeDeletedRecord;
		processUserOrgBranchMappingDecline(originalUserRecord,changedEntity, userEntityId);
		
	}



	@Override
	public void handleDeclineForNew(BaseMasterEntity originalRecord,
			BaseMasterEntity toBeDeletedRecord,
			Long reviewerId) {
		
		EntityId userEntityId = new EntityId(User.class, reviewerId);
		User originalUserRecord = (User)originalRecord;
		User changedEntity = (User) toBeDeletedRecord;
		processUserOrgBranchMappingDecline(originalUserRecord,changedEntity, userEntityId);
		
	}



	@Override
	public void handleSendBackForNew(BaseMasterEntity originalRecord,
			BaseMasterEntity toBeDeletedRecord,
			BaseMasterEntity toBeHistoryRecord,
			Long reviewerId) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void handleSendBackForModification(BaseMasterEntity originalRecord,
			BaseMasterEntity toBeDeletedRecord,
			BaseMasterEntity toBeHistoryRecord,
			Long reviewerId) {
	
		
	}
	
	
	private void processUserCityVillageMapping(User originalUserRecord,
									User changedEntity, User clonedEntity) {

		UserCityVillageMapping originalCityVillageMapping = userCityVillageMappingService.getCityVillageMappingByUserId(originalUserRecord.getId());
		UserCityVillageMapping changedCityVillageMapping = userCityVillageMappingService.getCityVillageMappingByUserId(changedEntity.getId());
		if (null != changedCityVillageMapping) {
			changedCityVillageMapping.setUser(originalUserRecord);
		}
		if(null != originalCityVillageMapping)
		{
			originalCityVillageMapping.setUser(clonedEntity);
		}
	}
	
	private void processUserProdSchemeMapping(User originalUserRecord,
			User changedEntity, User clonedEntity, EntityId userEntityId) {
		List<UserOrgBranchProdSchemeMapping> changedUserProdSchemeMappings = userManagementService
				.getUserProductSchemeList(changedEntity.getId());
		List<UserOrgBranchProdSchemeMapping> toBeDeletedUserProdSchemeMappings = userManagementService
				.getUserProductSchemeList(originalUserRecord.getId());
		for(UserOrgBranchProdSchemeMapping toBeDeletedUserProdSchemeMapping : toBeDeletedUserProdSchemeMappings){
			entityDao.delete(toBeDeletedUserProdSchemeMapping);
		}
		for (UserOrgBranchProdSchemeMapping lastUpdatedEntity : changedUserProdSchemeMappings) {
				lastUpdatedEntity.setUserId(originalUserRecord.getId());
				processUserProdSchemeAddition(lastUpdatedEntity, clonedEntity, userEntityId);
		}
	}
	
	void processUserProdSchemeAddition(
			UserOrgBranchProdSchemeMapping lastUpdatedEntity, User clonedEntity, EntityId userEntityId) {
		lastUpdatedEntity.setApprovalStatus(ApprovalStatus.APPROVED);
		lastUpdatedEntity.getMasterLifeCycleData().setReviewedByEntityId(
				userEntityId);
		lastUpdatedEntity.getMasterLifeCycleData().setReviewedTimeStamp(
				DateUtils.getCurrentUTCTime());
		UserOrgBranchProdSchemeMapping clonedSourceEntity = (UserOrgBranchProdSchemeMapping) lastUpdatedEntity
				.cloneYourself(CloneOptionConstants.MAKER_CHECKER_CLONING_OPTION);
		clonedSourceEntity.setUserId(clonedEntity.getId());
		clonedSourceEntity.setApprovalStatus(UNAPPROVED_HISTORY);
		clonedSourceEntity.getEntityLifeCycleData().setCreatedByUri(
				lastUpdatedEntity.getEntityLifeCycleData()
						.getCreatedByUri());
		entityDao.persist(clonedSourceEntity);
		
	}
	
	

}
