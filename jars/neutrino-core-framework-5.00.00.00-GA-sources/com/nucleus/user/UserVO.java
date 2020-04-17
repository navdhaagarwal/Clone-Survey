/**
 * 
 */
package com.nucleus.user;

import java.util.List;
import java.util.Map;

import com.nucleus.config.persisted.vo.ConfigurationVO;

/**
 * @author om.giri
 * This class is used to updated transfer data from controller to service 
 *
 */
public class UserVO {

	private User                                 formUser;
	
	private UserProfile                          formUserProfile;
	
	private Long[]                               roleMappings;
	
	private List<Map<String, Object>>            updatedUserOrgBranchMappings;
	
	private Map<Long,List<Map<Long, String>>>    updatedUserOrgBranchProductMappings;
	 
	private List<ConfigurationVO>                updatedUserPreferences;
	 
	private Long[]                               teamMappings;
	 
	private Long                                 defaultBranch;
	
	private List<Long>                           selectedBranchesList;
    
	private List<Long>                           originalOrgBranchList;
	
	private List<Long>                           originalAdminBranches;
	
	private List<String>                         myFavs;
	
	private Long                                 mappedBPId;
	
	private UserCityVillageMapping 				 cityVillageMapping;

	private List<UserDefaultUrlMappingVO>		userDefaultUrlMappingVOList;

	public List<Long>              				deletedUserUrlMappings;

	private Map<String,String>                   updatedUserOrgBranchProductMappingsModified;

	public Map<String, String> getUpdatedUserOrgBranchProductMappingsModified() {
		return updatedUserOrgBranchProductMappingsModified;
	}

	public void setUpdatedUserOrgBranchProductMappingsModified(Map<String, String> updatedUserOrgBranchProductMappingsModified) {
		this.updatedUserOrgBranchProductMappingsModified = updatedUserOrgBranchProductMappingsModified;
	}



	/**
	 * @return User
	 */
	public User getFormUser() {
		return formUser;
	}

	/**
	 * @param formUser
	 */
	public void setFormUser(User formUser) {
		this.formUser = formUser;
	}

	/**
	 * @return UserProfile
	 */
	public UserProfile getFormUserProfile() {
		return formUserProfile;
	}

	/**
	 * @param formUserProfile
	 */
	public void setFormUserProfile(UserProfile formUserProfile) {
		this.formUserProfile = formUserProfile;
	}

	
	/**
	 * @return the roleMappings
	 */
	public Long[] getRoleMappings() {
		return roleMappings;
	}

	/**
	 * @param roleMappings the roleMappings to set
	 */
	public void setRoleMappings(Long[] roleMappings) {
		this.roleMappings = roleMappings;
	}

	/**
	 * @return List<Map<String, Object>>
	 */
	public List<Map<String, Object>> getUpdatedUserOrgBranchMappings() {
		return updatedUserOrgBranchMappings;
	}

	/**
	 * @param updatedUserOrgBranchMappings
	 */
	public void setUpdatedUserOrgBranchMappings(
			List<Map<String, Object>> updatedUserOrgBranchMappings) {
		this.updatedUserOrgBranchMappings = updatedUserOrgBranchMappings;
	}

	/**
	 * @return Map<Long, List<Map<Long, String>>>
	 */
	public Map<Long, List<Map<Long, String>>> getUpdatedUserOrgBranchProductMappings() {
		return updatedUserOrgBranchProductMappings;
	}

	/**
	 * @param updatedUserOrgBranchProductMappings
	 */
	public void setUpdatedUserOrgBranchProductMappings(
			Map<Long, List<Map<Long, String>>> updatedUserOrgBranchProductMappings) {
		this.updatedUserOrgBranchProductMappings = updatedUserOrgBranchProductMappings;
	}

	/**
	 * @return List<ConfigurationVO>
	 */
	public List<ConfigurationVO> getUpdatedUserPreferences() {
		return updatedUserPreferences;
	}

	/**
	 * @param updatedUserPreferences
	 */
	public void setUpdatedUserPreferences(
			List<ConfigurationVO> updatedUserPreferences) {
		this.updatedUserPreferences = updatedUserPreferences;
	}

	/**
	 * @return Map<Long, String>
	 */
	public Long[] getTeamMappings() {
		return teamMappings;
	}

	/**
	 * @param teamMappings
	 */
	public void setTeamMappings(Long[] teamMappings) {
		this.teamMappings = teamMappings;
	}

	/**
	 * @return Long
	 */
	public Long getDefaultBranch() {
		return defaultBranch;
	}

	/**
	 * @param defaultBranch
	 */
	public void setDefaultBranch(Long defaultBranch) {
		this.defaultBranch = defaultBranch;
	}

	/**
	 * @return List<Long>
	 */
	public List<Long> getSelectedBranchesList() {
		return selectedBranchesList;
	}

	/**
	 * @param selectedBranchesList
	 */
	public void setSelectedBranchesList(List<Long> selectedBranchesList) {
		this.selectedBranchesList = selectedBranchesList;
	}

	/**
	 * @return List<Long>
	 */
	public List<Long> getOriginalOrgBranchList() {
		return originalOrgBranchList;
	}

	/**
	 * @param originalOrgBranchList
	 */
	public void setOriginalOrgBranchList(List<Long> originalOrgBranchList) {
		this.originalOrgBranchList = originalOrgBranchList;
	}

	/**
	 * @return List<Long>
	 */
	public List<Long> getOriginalAdminBranches() {
		return originalAdminBranches;
	}

	/**
	 * @param originalAdminBranches
	 */
	public void setOriginalAdminBranches(List<Long> originalAdminBranches) {
		this.originalAdminBranches = originalAdminBranches;
	}

	/**
	 * 
	 * @return myFavs
	 */
	public List<String> getMyFavs() {
		return myFavs;
	}

	/**
	 * 
	 * @param myFavs
	 */
	public void setMyFavs(List<String> myFavs) {
		this.myFavs = myFavs;
	}
	
	/**
	 * 
	 * @return mappedBPid
	 */
	public Long getMappedBPId() {
		return mappedBPId;
	}

	/**
	 * 
	 * @param mappedBPid
	 */
	public void setMappedBPId(Long mappedBPId) {
		this.mappedBPId = mappedBPId;
	}
	
	public UserCityVillageMapping getCityVillageMapping() {
		return cityVillageMapping;
	}

	public void setCityVillageMapping(UserCityVillageMapping cityVillageMapping) {
		this.cityVillageMapping = cityVillageMapping;
	}

	public List<UserDefaultUrlMappingVO> getUserDefaultUrlMappingVOList() {
		return userDefaultUrlMappingVOList;
	}

	public void setUserDefaultUrlMappingVOList(List<UserDefaultUrlMappingVO> userDefaultUrlMappingVOList) {
		this.userDefaultUrlMappingVOList = userDefaultUrlMappingVOList;
	}

	public List<Long> getDeletedUserUrlMappings() {
		return deletedUserUrlMappings;
	}

	public void setDeletedUserUrlMappings(List<Long> deletedUserUrlMappings) {
		this.deletedUserUrlMappings = deletedUserUrlMappings;
	}
}
