/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.cas.businessmapping;

import java.util.List;
import java.util.Map;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.menu.MenuEntity;
import com.nucleus.menu.MenuVO;
import com.nucleus.service.BaseService;
import com.nucleus.user.RecordComparatorVO;
import com.nucleus.user.User;
import com.nucleus.user.UserDefaultUrlMapping;
import com.nucleus.user.UserDefaultUrlMappingVO;
import com.nucleus.user.UserVO;

/**
 * @author Nucleus Software Exports Limited TODO -> amit.parashar Add
 *         documentation to class
 */
public interface UserManagementService extends BaseService {

    String USER_TO_ORGANIZATION    = "userToOrg";

    String USER_TO_ORG_TO_PRODUCTS = "userToOrgToProducts";

    public void saveBranchesAndProductsToUser(User user, Map<String, List<String>> branchPrpoductMap,
            List<Long> originalOrgBranchList, List<Long> adminOfBranches, List<Long> selectedBranchesList, Long defaultBranch);

    public List<UserOrgBranchProdMapping> getUserOrgBranchProductMapping(Long userID);

    public List<UserOrgBranchMapping> getUserOrgBranchMappingByBranchAndUserID(Long branchID, Long userID);

    public void saveBranchesToUser(User user, List<Long> selectedBranchesList, List<Long> originalOrgBranchList,
            List<Long> adminOfBranches, List<Long> orgAdminBranches, Long defaultBranch);

    public Long getUserOrganizationPrimaryBranchIdFromUserId(Long userId);
    
    void saveBranchesToUser(User formUser,List<Map<String, Object>> changeBranchList);

	/**
	 * Updating user as per maker changes
	 * @param userVO
	 * @return User
	 */
	User updateUserAtMakerStage(UserVO userVO);

	/**
	 * @param userID
	 * @return
	 */
	List<UserOrgBranchMapping> getUserOrgBranchMapping(Long userID);

	/**
	 * @param userOrganizationBranchId
	 * @return
	 */
	List<UserOrgBranchProdMapping> findOrgBranchProdMappingsByUserOrgBranche(
			Long userOrganizationBranchId);

	/**
	 * @param userVO
	 * @return
	 */
	User updateUserAtMakerStageSendForApproval(UserVO userVO);

	
	/**
	 * @param loanProductId
	 * @param userOrganizationBranchId
	 * @return
	 */
	List<UserOrgBranchProdMapping> findOrgBranchProdMappingsByProductAndAndBranchMapping(
			Long loanProductId, Long userOrganizationBranchId);
	
	/**
	 * 
	 * @param updatedUser
	 * @param teamIds
	 */
	void saveTeamsForUser(User updatedUser, Long[] teamIds);

	List<RecordComparatorVO> getUserAuditLog(Long originalUserId,Long changedUserId);

	public void updateUserProductSchemeMappings(String schemeListId ,User updatedUser);
	
	public List<UserOrgBranchProdSchemeMapping> getUserProductSchemeList(Long userId);

	void updateUserUrlMappingAtMakerStage(
			List<UserDefaultUrlMappingVO> userDefaultUrlMappingVOList, User userToBeUpdated, User originalUser,
			int approvalStatusBeforeUpdate, List<Long> deletedMappings);

	void removeAllUrlMappingsOfUser(Long userId);

	void deleteSelectedUrlMappingsOfUser(Long userId,List<Long> deletedUrlMappingIds);

	List<UserDefaultUrlMapping> getAllUrlMappingsOfUser(Long userId);

	List<MenuVO> menuListToVoForUserMapping(List<MenuEntity> menuEntityList);

	MenuVO menuToVoForUserMapping(MenuEntity entity);

	List<UserDefaultUrlMappingVO> userUrlMappingListToVO(List<UserDefaultUrlMapping> defaultMappingList);

	UserDefaultUrlMappingVO defaultUrlMappingToVo(UserDefaultUrlMapping defaultUrlMapping);

	Long getTargetUrlMappingCount(Long userId);
	
	/**
	 * @param updatingUser
	 * @param fromUser
	 * @return updated user record
	 */
	public User updateUserAtMakerStageSendForApproval(User updatingUser, User fromUser);

	List<UserOrgBranchProdMapping> findAllOrgBranchProdMappingsByUserOrgBranche(List<Long> userOrgBranchId);
}
