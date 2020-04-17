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
package com.nucleus.core.organization.service;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.organization.entity.RootOrganization;
import com.nucleus.entity.EntityId;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.UserInfo;

/**
 * One of the core service which will allow to work with organization branches and root bank.
 * @author Nucleus Software Exports Limited
 */
/**
 * @author neha.garg1
 *
 */
public interface OrganizationService {

    /**
     * This method returns Root Organization.
     * 
     * @return RootOrganization: Root Organization Entity
     */
    public RootOrganization getRootOrganization();

    /**
     * This method returns Head Office .
     * @return OrganizationBranch : Organization Branch which is Head Office in the system
     */
    public OrganizationBranch getHeadOffice();


    /**
     * It returns the list of Parent Organizations by Organization Type.
     * 
     * @param organizationType : It tells the type of Organization
     * @return List of OrganizationBranch
     */
    public List<OrganizationBranch> getParentBranchesForType(OrganizationType organizationType);

    /**
     * It is called after the approval of an Organization Branch.
     * 
     * @param orgBrEntityId : Entity Id of an Organization Branch on Approval of which this function is called
     * @return OrganizationBranch: Organization Branch after doing some operations on it
     */
    public OrganizationBranch postOrgBranchApprovalAction(EntityId orgBrEntityId);

    /**
     * This method returns all the child organization branches under a given organization branch.
     *
     * @param branchID the branch id
     * @param systemName the system name
     * @return OrganizationBranch List in tree structure
     * @Param Long branchID : Id of Organization Branch whose children to be fetched
     */
    public List<OrganizationBranch> getAllChildBranches(Long branchID, String systemName);

    /**
     * It returns total number of children of a particular branch.
     *
     * @param branchID : Id of an Organization Branch whose children count is to be calculated
     * @return Total number of child branches
     */
    public int getAllChildBranchesCount(Long branchID);

    /**
     * This method returns the orgBranchInfo tree with children organizations count for UI Tree graph requirements.
     *
     * @param branchId the branch id
     * @param systemName the system name
     * @return organBranchInfo List in tree structure.
     */
    public List<OrgBranchInfo> getOrganizationTree(Long branchId, String systemName);

    /**
     * This method returns the branchCalendar of the branch.
     *
     * @param branch the branch
     * @return branchCalendar
     */
    public BranchCalendar getDerivedBranchCalendar(OrganizationBranch branch);

    /**
     * It returns Organization Branch. 
     *
     * @param branchId : Id of an Organization Branch
     * @return OrganizationBranch
     */
    public OrganizationBranch getOrganizationBranchById(Long branchId);

    /**
     * It returns all Organization Branches of type Branch.
     *
     * @return List of Organization Branches of type Branch
     */
    public List<Map<String, ?>> getOrgBranchesOfBranchType();

    //autocomplete
  //  public List<Map<String, ?>> getOrgBranchesOfBranchTypeByPage(int page);
    
    public List<Map<String, ?>> getOrgBranchesOfBranchTypeByPage(String searchColumnList,String value,int page) ;

    /**
     * It returns All Organization Branches.
     *
     * @return List of Organization branches
     */
    public List<OrganizationBranch> getAllOrganizationBranch();

    /**
     * Gets the maximum emails for branch.
     *
     * @param organizationBranch the organization branch
     * @return the maximum emails for branch
     */
    public Long getMaximumEmailsForBranch(OrganizationBranch organizationBranch);

    /**
     * Gets the email filtering enabled status.
     *
     * @param organizationBranch the organization branch
     * @return the email filtering enabled status
     */
    public Boolean getEmailFilteringEnabledStatus(OrganizationBranch organizationBranch);

    /**
     * Gets the all branches for type.
     *
     * @param organizationType the organization type
     * @return the all branches for type
     */
    public List<OrganizationBranch> getAllBranchesForType(OrganizationType organizationType);

    /**
     * Gets the user login time valid.
     *
     * @param user the user
     * @return the user login time valid
     */
    public Boolean getUserLoginTimeValid(UserInfo user);

    /**
     * Custom login time validator.
     *
     * @param user the user
     * @param dateTime the date time
     * @return the boolean
     */
    public Boolean customLoginTimeValidator(UserInfo user, DateTime dateTime);

    /**
     * Gets the branches where user is branch admin.
     *
     * @param userId the user id
     * @return the branches where user is branch admin
     */
    public List<OrganizationBranch> getBranchesWhereUserIsBranchAdmin(Long userId);

    /**
     * This method returns the orgBranchInfo tree with children organizations count for UI Tree graph requirements.
     * This tree will consist of only those branches which are under current logged in user.
     *
     * @param branchId the branch id
     * @param userId the user id
     * @return organBranchInfo List in tree structure.
     */
    public List<OrgBranchInfo> getOrganizationTreeForLoggedInUser(Long branchId, Long userId);

    /**
     * This method returns the orgBranchInfo tree with children organizations count for UI Tree graph requirements.
     * This tree will consist of only those branches which are under current logged in user.
     *
     * @param userId the user id
     * @param loggedInUserId the logged in user id
     * @return organBranchInfo List in tree structure.
     */
    public List<OrganizationBranch> getUserOrgBranchesUnderCurrentLoggedInUser(Long userId, Long loggedInUserId);

    /**
     * Gets the org branch by city.
     *
     * @param cityId the city id
     * @return the org branch by city
     */
    public List<OrganizationBranch> getOrgBranchByCity(Long cityId);

    /**
     * Gets the id and name of org branch by city.
     *
     * @param cityId the city id
     * @return the id and name of org branch by city
     */
    public List<Object[]> getIdAndNameOfOrgBranchByCity(Long cityId);

    /**
     * Gets the all ids and names do organization branch.
     *
     * @return the all ids and names do organization branch
     */
    public List<OrganizationBranch> getAllIdsAndNamesDOOrganizationBranch();

    /**
     * Gets the all child branches ids.
     *
     * @param branchID the branch id
     * @param systemName the system name
     * @return the all child branches ids
     */
    public List<Long> getAllChildBranchesIds(Long branchID, String systemName);

    /**
     * Gets the org branches of branch type ro.
     *
     * @return the org branches of branch type ro
     */
    public List<Map<String, ?>> getOrgBranchesOfBranchTypeRO();

    public List<OrganizationBranch> getOrgBranchByServedCityId(Long cityId);

    public List<OrgBranchInfo> getAllAprovedAndActiveOrganizationBranches(String organizationTypeCode);
    
    
    public List<OrganizationBranch> getBranchesOfUser(Long userId);

    OrganizationBranch getOrgBranchByBranchCode(String code);

    List<OrganizationBranch> getOrgBranchByBranchCodeAndStatus(String code,
            List<Integer> statusList);

    boolean checkIfBranchCodeExists(String branchCode, List<Integer> statusList);

    List<OrgBranchInfo> getTopBranchesAmongBranchIds(String systemName,List<Long> branchIds);

    List<OrgBranchInfo> getUniqueChildBranchesTree(Long branchId,
            String systemName);
    
    public List<Object[]> getApprovedAndActiveOrgBranches(String queryTerm);
    
    
    List<OrganizationBranch> getAllApprovedBranches();
	
	
    
	public boolean checkForDuplicateHeadOffice(OrganizationBranch organizationBranch);

	public RootOrganization getRootOrganizationFromDB();
	public String getScreenDescription(Long screenId);
 
}