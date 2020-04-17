package com.nucleus.core.organization.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.organization.calendar.DailySchedule;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.organization.entity.ParentBranchMapping;
import com.nucleus.core.organization.entity.RootOrganization;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCommonCacheKeys;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;


@Named("organizationService")
public class OrganizationServiceImpl extends BaseServiceImpl implements OrganizationService {

    private static final String ORGANIZATION_GET_ORG_BRANCHES_OF_USER                                     = "Organization.getOrgBranchesOfUser";
    private static final String QUERY_FOR_ORGANIZATION_BRANCH                                             = "Organization.getOrganizationBranch";
    private static final String QUERY_FOR_ORGANIZATION_BRANCH_GET_CHILDREN                                = "Organization.getAllChildBranches";
    private static final String QUERY_FOR_ORGANIZATION_BRANCH_GET_CHILDREN_COUNT                          = "Organization.getBranchChildCount";
   private static final String QUERY_FOR_BRANCH_TYPE_ORGANIZATION_BRANCH                                  = "Organization.getOrgBranchesOfBranchType";
    private static final String QUERY_FOR_BRANCH_TYPE_ORGANIZATION_BRANCH_PAGE                            = "Organization.getOrgBranchesOfBranchTypeLike";
    private static final String QUERY_FOR_ORGANIZATION_BRANCH_GET_ORG_BRANCHES_WHERE_USER_IS_BRANCH_ADMIN = "Organization.getOrgBranchesWhereUserIsBranchAdmin";
    private static final String QUERY_FOR_EXTERNAL_ORG_BRANCHES_BY_CITY                                   = "Organization.getOrgBranchesByCity";
    private static final String QUERY_FOR_ORGANIZATION_BRANCH_GET_CHILDREN_IDS                            = "Organization.getAllChildBranchesId";
    private static final String QUERY_FOR_EXTERNAL_ORG_BRANCHES_ID_AND_NAME_BY_CITY                       = "Organization.getIdAndNameOFOrgBranchesByCity";
    private static final String QUERY_FOR_EXTERNAL_ORG_BRANCHES_BY__SERVED_CITY                           = "Organization.getOrgBranchesByServedCity";
    private static final String source_CAS                                                                = com.nucleus.core.organization.entity.SystemName.SOURCE_PRODUCT_TYPE_CAS;
    private static final String source_LMS                                                                = com.nucleus.core.organization.entity.SystemName.SOURCE_PRODUCT_TYPE_LMS;
    private static final String QUERY_FOR_ORG_BRANCHES_BY_BRANCH_CODE                                     = "Organization.getOrgBranchByBranchCode";   
    private static final String PARENT_ORGANIZATION_ID                                                    ="parentOrgId";
    private static final String SYSTEM_NAME                                                               ="systemName";
    
    private static final int DEFAULT_PAGE_SIZE =3;
    @Inject
    @Named("baseMasterService")
    private BaseMasterService           baseMasterService;
 
    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore   userManagementServiceCore;
    @Inject
    @Named("genericParameterService")
    private GenericParameterService    genericParameterService;
    @Inject
	 @Named("configurationService")
	 private ConfigurationService configurationService;
    /**
     * This method returns Root Organization which is one for one system.
     * 
     * @return RootOrganization: Root Organization Entity
     */

    @Inject
    @Named("fwCommonCachePopulator")
    private NeutrinoCachePopulator fwCommonCachePopulator;
    
    @Override
    public RootOrganization getRootOrganization() {
		return entityDao.find(RootOrganization.class, (Long) fwCommonCachePopulator
				.get(FWCommonCacheKeys.ROOT_ORGANIZATION));	
    }
    
    
    @Override
    public RootOrganization getRootOrganizationFromDB() {
    		return entityDao.findAll(RootOrganization.class).get(0);
    }
    
    

    /**
     * This method returns Head Office which is head office of the Root Organization
     * 
     * @return OrganizationBranch : Organization Branch which is Head Office in the system
     */
    @Override
    public OrganizationBranch getHeadOffice() {
        RootOrganization rootOrganization = getRootOrganization();
        return rootOrganization.getHeadOffice();
    }

    /**
     * It returns the list of all Parent Organizations by Organization Type.
     * 
     * @param organizationType : It tells the type of Organization
     * @return List of OrganizationBranch
     */
    @Override
    public List<OrganizationBranch> getParentBranchesForType(OrganizationType organizationType) {
        NeutrinoValidator.notNull(organizationType);
        int levelInHirarchy = organizationType.getLevelInHierarchy();
        int headOfficeLvlInHierarchy = getHeadOffice().getOrganizationType().getLevelInHierarchy();
        List<Integer> hirarchyList = new ArrayList<Integer>();
        if (ValidatorUtils.notNull(organizationType) && OrganizationType.ORGANIZATION_TYPE_BRANCH_SB.equalsIgnoreCase(organizationType.getCode())) {
            getHirarchyList(levelInHirarchy-1, hirarchyList, headOfficeLvlInHierarchy);
        } else {
            getHirarchyList(levelInHirarchy, hirarchyList, headOfficeLvlInHierarchy);
        }
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_ORGANIZATION_BRANCH).addParameter("levelInHirarchy", hirarchyList).addParameter("statusList",
                statusList);
        return entityDao.executeQuery(executor);
    }
    
    private void getHirarchyList(int levelInHirarchy,
            List<Integer> hirarchyList, int headOfficeLvlInHierarchy) {
        int hierarchyLevel = levelInHirarchy;
        while (hierarchyLevel >= headOfficeLvlInHierarchy) {
            hirarchyList.add(hierarchyLevel);
            hierarchyLevel--;
        }
    }

    @Override
    public List<OrganizationBranch> getAllBranchesForType(OrganizationType organizationType) {
        NeutrinoValidator.notNull(organizationType);
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        List<Integer> hirarchyList = new ArrayList<Integer>();
        hirarchyList.add(organizationType.getLevelInHierarchy());
        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_ORGANIZATION_BRANCH).addParameter("levelInHirarchy", hirarchyList).addParameter("statusList",
                statusList);
        return entityDao.executeQuery(executor);
    }

    /**
     * It is called after the approval of an Organization Branch to update parentBranchIds field in the Organization Branch Entity.
     * 
     * @param orgBrEntityId : Entity Id of an Organization Branch on Approval of which this function is called
     * @return OrganizationBranch: Organization Branch after doing some operations on it
     */
    @Override
    public OrganizationBranch postOrgBranchApprovalAction(EntityId orgBrEntityId) {
        OrganizationBranch orgBranch = entityDao.get(orgBrEntityId);
        String parentBranchIdList = "";

        if (orgBranch != null && null != orgBranch.getId()) {// always true

            if (orgBranch.getParentBranchMapping() != null) {
                for (ParentBranchMapping mapping : orgBranch.getParentBranchMapping()) {
                    String parentBranchIds = mapping.getParentBranch().getParentBranchIds();
                    if (parentBranchIds != null && !parentBranchIds.equalsIgnoreCase("")) {
                        String[] parentBranches = parentBranchIds.split(";");
                        for (int i = 0 ; i < parentBranches.length ; i++) {
                            if (parentBranches[i] != null && parentBranches[i].contains(source_CAS)
                                    && mapping.getModuleName().getCode().equalsIgnoreCase(source_CAS)) {
                                parentBranches[i] += source_CAS + orgBranch.getId().toString() + "_";
                                parentBranchIdList = parentBranches[i];
                            } else if (parentBranches[i] != null && parentBranches[i].contains(source_LMS)
                                    && mapping.getModuleName().getCode().equalsIgnoreCase(source_LMS)) {
                                parentBranches[i] += source_LMS + orgBranch.getId().toString() + "_";
                                parentBranchIdList = parentBranchIdList + ";" + parentBranches[i];
                            }

                        }
                    } else {
                        if (mapping.getModuleName().getCode().equalsIgnoreCase(source_CAS)) {
                            parentBranchIds = "_CAS_" + source_CAS + mapping.getParentBranch().getId() + "_" + source_CAS
                                    + orgBranch.getId() + "_;_LMS_";
                        } else if (mapping.getModuleName().getCode().equalsIgnoreCase(source_LMS)) {
                            parentBranchIds = "_CAS_" + ";_LMS_" + source_LMS + mapping.getParentBranch().getId() + "_"
                                    + source_LMS + orgBranch.getId();
                        }
                    }

                }

            }
            orgBranch.setParentBranchIds(parentBranchIdList);
            entityDao.update(orgBranch);
        }
        return orgBranch;
    }

    /**
     * This method returns all the child organization branches under a given organization branch.
     * 
     * @Param Long branchID : Id of Organization Branch whose children to be fetched
     * @return OrganizationBranch List in tree structure
     */
    @Override
    public List<OrganizationBranch> getAllChildBranches(Long branchID, String systemName) {
        NeutrinoValidator.notNull(branchID, "Branch Id cannot be null");
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        String branchid = branchID.toString();
        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_ORGANIZATION_BRANCH_GET_CHILDREN).addParameter("branchID", "%_" + systemName + branchid + "_%")
                .addParameter("approvalStatus", statusList);
        List<OrganizationBranch> organizationBranchList = entityDao.executeQuery(executor);
        Iterator<OrganizationBranch> orgBranchItr = organizationBranchList.listIterator();
        while (orgBranchItr.hasNext()) {
            if (orgBranchItr.next().getId().equals(branchID)) {
                orgBranchItr.remove();
                break;
            }

        }

        return organizationBranchList;

    }

    /**
     * This method returns all the child organization branches under a given organization branch.
     * 
     * @Param Long branchID : Id of Organization Branch whose children to be fetched
     * @return OrganizationBranch List in tree structure
     */
    @Override
    public int getAllChildBranchesCount(Long branchID) {
        NeutrinoValidator.notNull(branchID, "Branch Id cannot be null");
        String branchid = branchID.toString();
        Long allParentsCount = new Long(0);
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>(QUERY_FOR_ORGANIZATION_BRANCH_GET_CHILDREN_COUNT)
                .addParameter("branchID", "%_" + branchid + "_%");
        List<Long> counts = entityDao.executeQuery(executor, 0, 1);
        if (null != counts && counts.size() > 0) {
            allParentsCount = counts.get(0) - 1;
        }
        return allParentsCount.intValue();
    }

    /**
     * This method returns the orgBranchInfo tree with children organizations count for UI Tree graph requirements.
     * @param Long branchID : If the branch ID is passed as null , all the branches, directly under the root organization are returned.    
     * @return organBranchInfo List in tree structure.
     */
    @Override
    public List<OrgBranchInfo> getOrganizationTree(Long branchId, String systemName) {
        List<OrgBranchInfo> orgBranchInfoList = null;
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        if (branchId == null) {
            NamedQueryExecutor<OrgBranchInfo> executor = new NamedQueryExecutor<OrgBranchInfo>(
                    "Organization.getTopOrgBranches");
            executor.addParameter(SYSTEM_NAME, systemName);
            executor.addParameter("approvalStatus", statusList);
            
            orgBranchInfoList = entityDao.executeQuery(executor);
            
        } else {
            NamedQueryExecutor<OrgBranchInfo> executor = new NamedQueryExecutor<OrgBranchInfo>(
                    "Organization.getChildOrgBranches").addParameter(PARENT_ORGANIZATION_ID, branchId);
            executor.addParameter(SYSTEM_NAME, systemName);
            executor.addParameter("approvalStatus", statusList);
            orgBranchInfoList = entityDao.executeQuery(executor);
        }

        return orgBranchInfoList;
    }
    
    @Override
    public List<OrgBranchInfo> getUniqueChildBranchesTree(Long branchId, String systemName) {
        List<OrgBranchInfo> orgBranchInfoList = null;
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        
        NamedQueryExecutor<OrgBranchInfo> executor = new NamedQueryExecutor<OrgBranchInfo>(
                "Organization.getUniqueChildOrgBranches").addParameter(PARENT_ORGANIZATION_ID, branchId);
        executor.addParameter(SYSTEM_NAME, systemName);
        executor.addParameter("approvalStatus", statusList);
        orgBranchInfoList = entityDao.executeQuery(executor);
        

        return orgBranchInfoList;
    }
    
    @Override
    public List<OrgBranchInfo> getTopBranchesAmongBranchIds(String systemName, List<Long> branchIds) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        Map<String, Object> parameters=new HashMap<>();
        parameters.put(SYSTEM_NAME, systemName);
        parameters.put("approvalStatus", statusList);
        return entityDao.executeSingleInClauseHQLQuery("Organization.getTopOrgBranchesAmongBranchIds", "branchIds",
				branchIds, parameters,OrgBranchInfo.class);
    }

    /**
     * This method returns the orgBranchInfo tree with children organizations count for UI Tree graph requirements.
     * This tree will consist of only those branches which are under current logged in user.
     * @param Long branchID : If the branch ID is passed as null , all the branches, directly under the root organization are returned.    
     * @return organBranchInfo List in tree structure.
     */
    @Override
    public List<OrgBranchInfo> getOrganizationTreeForLoggedInUser(Long branchId, Long userId) {
        List<OrgBranchInfo> orgBranchInfoList = null;
        if (branchId == null) {
            NamedQueryExecutor<OrgBranchInfo> executor = new NamedQueryExecutor<OrgBranchInfo>(
                    "Organization.getOrgBranchesForLoggedInUser");
            executor.addParameter("userId", userId);
            orgBranchInfoList = entityDao.executeQuery(executor);
        } else {
            /* Todo: Will consider this else part later*/
            NamedQueryExecutor<OrgBranchInfo> executor = new NamedQueryExecutor<OrgBranchInfo>(
                    "Organization.getChildOrgBranchesForLoggedInUser").addParameter(PARENT_ORGANIZATION_ID, branchId);
            executor.addParameter("userId", userId);
            orgBranchInfoList = entityDao.executeQuery(executor);
        }
        return orgBranchInfoList;
    }

    /**
     * 
     * This method returns the branchCalendar of the branch if its has one, otherwise parent's branch calendar is cloned and returned.
     * In case it doesn't have a parent or parent's branchCalendar it returns null.  
     * @param branch
     * @return branchCalendar
     */
    @Override
    public BranchCalendar getDerivedBranchCalendar(OrganizationBranch branch) {
        NeutrinoValidator.notNull(branch, "While retrieving branch calendar organization branch can not be null.");
        BranchCalendar branchCalendar = null;
        if (branch.getBranchCalendar() != null) {
            return branch.getBranchCalendar();
        } else {
            List<ParentBranchMapping> parentBranchMappingList = branch.getParentBranchMapping();
            if (CollectionUtils.isNotEmpty(parentBranchMappingList)) {
                OrganizationBranch organizationBranch = null;
                for (ParentBranchMapping branchMapping : parentBranchMappingList) {
                    if (branchMapping.getModuleName() != null
                            && SystemName.SOURCE_PRODUCT_TYPE_CAS.equals(branchMapping.getModuleName().getCode())
                            && branchMapping.getParentBranch() != null) {
                        organizationBranch = branchMapping.getParentBranch();
                        break;

                    }
                }
                if (organizationBranch != null) {
                    return getDerivedBranchCalendar(organizationBranch);
                }
            }
          
            return branchCalendar;
       

        }
    }

    /**
     * It returns Organization Branch. 
     *
     * @param branchId : Id of an Organization Branch
     * @return OrganizationBranch
     */
    @Override
    public OrganizationBranch getOrganizationBranchById(Long branchId) {
        NeutrinoValidator.notNull(branchId, "BranchId can not be null.");
        return entityDao.find(OrganizationBranch.class, branchId);
    }

    @Override
    public List<OrganizationBranch> getAllOrganizationBranch() {
    
        return entityDao.findAll(OrganizationBranch.class);
    }

    @Override
    public List<OrganizationBranch> getAllIdsAndNamesDOOrganizationBranch() {

        String[] a = { "name" };

      
        List<Object[]> x = entityDao.findAllWithSpecifiedColumns(OrganizationBranch.class, a);
        List<OrganizationBranch> obl = new ArrayList<OrganizationBranch>();
        for (Object[] temp : x) {
            OrganizationBranch tmpOrgBrch = new OrganizationBranch();
            tmpOrgBrch.setId((Serializable) temp[0]);
            tmpOrgBrch.setName(String.valueOf(temp[1]));
            obl.add(tmpOrgBrch);
        }
        return obl;
    }

    /**
     * It returns all Organization Branches of type 'Branch'
     * 
     * @return List of Organization Branches of type Branch
     */
    @Override
    public List<Map<String, ?>> getOrgBranchesOfBranchType() {
        List<Map<String, ?>> orgBranchList = null;
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<Map<String, ?>> executor = new NamedQueryExecutor<Map<String, ?>>(
                QUERY_FOR_BRANCH_TYPE_ORGANIZATION_BRANCH).addParameter("approvalStatus", statusList).addParameter(
                "orgType", OrganizationType.ORGANIZATION_TYPE_BRANCH);
        orgBranchList = entityDao.executeQuery(executor);

        return orgBranchList;
    }
    
  
    
	@Override
	public List<Map<String, ?>> getOrgBranchesOfBranchTypeByPage(String searchColumnList, String value, int page) {
		List<Map<String, ?>> orgBranchList = null;

		List<Map<String, ?>> finalResult = new ArrayList<Map<String, ?>>();
		int counter = 0;
		long totalRecords = 0;

		List<Integer> statusList = new ArrayList<Integer>();
		statusList.add(ApprovalStatus.APPROVED);
		statusList.add(ApprovalStatus.APPROVED_MODIFIED);
		statusList.add(ApprovalStatus.APPROVED_DELETED);
		statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		NamedQueryExecutor<Map<String, ?>> executor = new NamedQueryExecutor<Map<String, ?>>(
				QUERY_FOR_BRANCH_TYPE_ORGANIZATION_BRANCH_PAGE).addParameter("approvalStatus", statusList)
						.addParameter("orgType", OrganizationType.ORGANIZATION_TYPE_BRANCH)
						.addParameter("value", value.toLowerCase().concat("%"));

		orgBranchList = entityDao.executeQuery(executor, page * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE);

		for (Map<String, ?> temp : orgBranchList) {
			finalResult.add(counter, temp);
			counter++;
		}
		totalRecords = totalRecords + entityDao.executeTotalRowsQuery(executor);

		Map<String, Long> sizeMap = new HashMap<String, Long>();

		sizeMap.put("size", totalRecords);
		finalResult.add(counter, sizeMap);
		if (finalResult != null) {
			BaseLoggers.flowLogger.debug("size of finalResult :" + finalResult.size());
		}

		return finalResult;
	}
    

    @Override
    public List<Map<String, ?>> getOrgBranchesOfBranchTypeRO() {
        List<Map<String, ?>> orgBranchList = null;
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<Map<String, ?>> executor = new NamedQueryExecutor<Map<String, ?>>(
                "Organization.getOrgBranchesOfBranchTypeRO").addParameter("approvalStatus", statusList).addParameter(
                "orgType", OrganizationType.ORGANIZATION_TYPE_BRANCH_RO);
        orgBranchList = entityDao.executeQuery(executor);

        return orgBranchList;
    }

    @Override
    public Long getMaximumEmailsForBranch(OrganizationBranch organizationBranch) {

        Long maxEmails = null;
        maxEmails = organizationBranch.getMaximumEmails();

        if (null == maxEmails && organizationBranch.getParentBranchMapping() != null) {
            for (ParentBranchMapping mapping : organizationBranch.getParentBranchMapping()) {
                if (mapping != null && mapping.getModuleName().getCode().equalsIgnoreCase(source_CAS)) {
                    maxEmails = getMaximumEmailsForBranch(mapping.getParentBranch());
                }
            }

        }

        return maxEmails;

    }

    @Override
    public Boolean getEmailFilteringEnabledStatus(OrganizationBranch organizationBranch) {
        Boolean isEmailFilterEnabled = isNull(organizationBranch.getIsEmailFilterEnabled())?Boolean.FALSE:organizationBranch.getIsEmailFilterEnabled();
        
        

        if (!isEmailFilterEnabled && organizationBranch.getParentBranchMapping() != null) {
            for (ParentBranchMapping mapping : organizationBranch.getParentBranchMapping()) {
                if (mapping != null && mapping.getModuleName().getCode().equalsIgnoreCase(source_CAS)) {
                    isEmailFilterEnabled = getEmailFilteringEnabledStatus(mapping.getParentBranch());
                }
            }
        }

        return isEmailFilterEnabled;
    }

    @Override
    public Boolean getUserLoginTimeValid(UserInfo user) {
        return customLoginTimeValidator(user, new DateTime());
    }

    @Override
    public Boolean customLoginTimeValidator(UserInfo user, DateTime currentTime) {

        

        OrgBranchInfo branch = user.getLoggedInBranch();
        OrganizationBranch orgBranch = getOrganizationBranchById(branch.getId());
        BranchCalendar branchCalendar = getDerivedBranchCalendar(orgBranch);
        //CAS-40443 -- null check handling for branchCalendar is done.
        DailySchedule dailySchedule =null;
        if(branchCalendar!=null)
            dailySchedule = branchCalendar.getSchedule(currentTime.getDayOfWeek());

        Boolean allowed = orgBranch.getIsOutOfWorkingHoursLoginAllowed();
        Boolean workingDay = null;
        if (dailySchedule != null) {
            workingDay = dailySchedule.isWorkingDay();
        } else {
            workingDay = Boolean.FALSE;
        }
        BaseLoggers.flowLogger.info("IsOutOfWorkingHoursLoginAllowed = " + allowed);
        BaseLoggers.flowLogger.info("workingDay = " + workingDay);
        if (!workingDay) {
            BaseLoggers.flowLogger.info("User is not authorized to login today");
          //  throw new AuthenticationServiceException("notWorkingDay");
            return false;
        }
        if (null != allowed && !allowed) {

            BaseLoggers.flowLogger.info("currentTime = " + currentTime.getMillisOfDay());
            if (dailySchedule != null && dailySchedule.getOpeningTime() != null) {
                BaseLoggers.flowLogger.info("getOpeningTime = " + dailySchedule.getOpeningTime().getMillisOfDay());
            }
            if (dailySchedule != null && dailySchedule.getClosingTime() != null) {
                BaseLoggers.flowLogger.info("getClosingTime = " + dailySchedule.getClosingTime().getMillisOfDay());
            }
            if ((dailySchedule.getOpeningTime() != null && currentTime.getMillisOfDay() < dailySchedule.getOpeningTime()
                    .getMillisOfDay())
                    || (dailySchedule.getClosingTime() != null && currentTime.getMillisOfDay() > dailySchedule
                            .getClosingTime().getMillisOfDay())) {

                return false;

            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    @Override
    public List<OrganizationBranch> getBranchesWhereUserIsBranchAdmin(Long userId) {
        NeutrinoValidator.notNull(userId, "User id cannot be null");
        User user = entityDao.find(User.class, userId);
        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_ORGANIZATION_BRANCH_GET_ORG_BRANCHES_WHERE_USER_IS_BRANCH_ADMIN);
        List<OrganizationBranch> finalUsersBranchesWhereUserIsAdmin = null;
        if(user.getApprovalStatus()==ApprovalStatus.APPROVED
                || user.getApprovalStatus()==ApprovalStatus.APPROVED_MODIFIED)
        {
            executor.addParameter("userId", userId)
            		.addParameter("isBranchAdmin", true);
            finalUsersBranchesWhereUserIsAdmin =entityDao.executeQuery(executor);
            
        }
       
        
        if(!(user.getApprovalStatus()==ApprovalStatus.APPROVED 
                || user.getApprovalStatus()==ApprovalStatus.APPROVED_MODIFIED)){
            User originalUser = (User) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(user.getEntityId());
            Map<Long, OrganizationBranch> originalOrganisationMapWhereUserIsAdmin = new HashMap<Long, OrganizationBranch>();
            if(originalUser!=null){
                executor.addParameter("userId", originalUser.getId())
                		.addParameter("isBranchAdmin", true);
                finalUsersBranchesWhereUserIsAdmin =entityDao.executeQuery(executor);   
                for(OrganizationBranch organizationBranch : finalUsersBranchesWhereUserIsAdmin){
                    originalOrganisationMapWhereUserIsAdmin.put(organizationBranch.getId(), organizationBranch);
                }
            }
            
            /*
             * Find user-org-mapping for unapproved user
             */
            List<UserOrgBranchMapping> unApprovedUsersBranches = userManagementServiceCore.getUserOrgBranchMapping(userId);
             for(UserOrgBranchMapping userOrgBranchMapping : unApprovedUsersBranches){
                 if(userOrgBranchMapping.isBranchAdmin()){
                    originalOrganisationMapWhereUserIsAdmin.put(userOrgBranchMapping.getOrganizationBranchId(),
                            userOrgBranchMapping.getOrganizationBranch());
                 }else{
                    originalOrganisationMapWhereUserIsAdmin.remove(userOrgBranchMapping.getOrganizationBranchId());
                }
                 
            }
             
             finalUsersBranchesWhereUserIsAdmin=new ArrayList<OrganizationBranch>(originalOrganisationMapWhereUserIsAdmin.values());
            
        }
       
        

        return finalUsersBranchesWhereUserIsAdmin;
    }

   
    @Override
    public List<OrganizationBranch> getBranchesOfUser(Long userId) {
        NeutrinoValidator.notNull(userId, "User id cannot be null");

        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                ORGANIZATION_GET_ORG_BRANCHES_OF_USER);
        executor.addParameter("userId", userId)
        		.addParameter("approvalStatus", ApprovalStatus.APPROVED);

        return entityDao.executeQuery(executor);
    }

    @Override
    public List<OrganizationBranch> getUserOrgBranchesUnderCurrentLoggedInUser(Long userId, Long loggedInUserId) {
        return null;
    }

    @Override
    public List<OrganizationBranch> getOrgBranchByCity(Long cityId) {
        List<OrganizationBranch> branches = null;

        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_EXTERNAL_ORG_BRANCHES_BY_CITY).addParameter("cityId", cityId);
        branches = entityDao.executeQuery(executor);

        return branches;
    }

    @Override
    public List<OrganizationBranch> getOrgBranchByServedCityId(Long cityId) {
        List<OrganizationBranch> branches = null;
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);

        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_EXTERNAL_ORG_BRANCHES_BY__SERVED_CITY).addParameter("cityId", cityId).addParameter(
                "approvalStatus", statusList);
        branches = entityDao.executeQuery(executor);

        return branches;
    }

    @Override
    public List<Object[]> getIdAndNameOfOrgBranchByCity(Long cityId) {
        List<Object[]> branches = null;

        NamedQueryExecutor<Object[]> executor = new NamedQueryExecutor<Object[]>(
                QUERY_FOR_EXTERNAL_ORG_BRANCHES_ID_AND_NAME_BY_CITY).addParameter("cityId", cityId);
        branches = entityDao.executeQuery(executor);

        return branches;
    }

    /**
     * This method returns all the child organization branches under a given organization branch.
     * 
     * @Param Long branchID : Id of Organization Branch whose children to be fetched
     * @return OrganizationBranch List in tree structure
     */
    @Override
    public List<Long> getAllChildBranchesIds(Long branchID, String systemName) {
        NeutrinoValidator.notNull(branchID, "Branch Id cannot be null");
        String branchid = branchID.toString();
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>(QUERY_FOR_ORGANIZATION_BRANCH_GET_CHILDREN_IDS)
                .addParameter("branchID", "%_" + systemName + branchid + "_%");
        List<Long> organizationBranchIdList = entityDao.executeQuery(executor);
        Iterator<Long> orgBranchItr = organizationBranchIdList.listIterator();
        while (orgBranchItr.hasNext()) {
            if (orgBranchItr.next().equals(branchID)) {
                orgBranchItr.remove();
                break;
            }

        }

        return organizationBranchIdList;

    }

    @Override
    public List<OrgBranchInfo> getAllAprovedAndActiveOrganizationBranches(String organizationTypeCode) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);

        List<OrgBranchInfo> orgBranchInfoList = null;
        NamedQueryExecutor<OrgBranchInfo> executor = new NamedQueryExecutor<OrgBranchInfo>(
                "Organization.getAllApprovedAndActiveOrganizationBranches");
        executor.addParameter("approvalStatus", statusList).addParameter("organizationTypeCode", organizationTypeCode)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        orgBranchInfoList = entityDao.executeQuery(executor);

        return orgBranchInfoList;
    }
    
    @Override
    public OrganizationBranch getOrgBranchByBranchCode(String code) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
		statusList.add(ApprovalStatus.APPROVED_MODIFIED);
		statusList.add(ApprovalStatus.APPROVED_DELETED);
	    statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
	        

        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_ORG_BRANCHES_BY_BRANCH_CODE).addParameter("branchCode", code).addParameter("approvalStatus",
                statusList);
        return entityDao.executeQueryForSingleValue(executor);

    }
    
    @Override
    public List<OrganizationBranch> getOrgBranchByBranchCodeAndStatus(String code,List<Integer> statusList) {
       
        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_ORG_BRANCHES_BY_BRANCH_CODE).addParameter("branchCode", code).addParameter("approvalStatus",
                statusList);
        return entityDao.executeQuery(executor);

    }
    
    @Override
    public boolean checkIfBranchCodeExists(String branchCode,List<Integer> statusList) {
        List<OrganizationBranch> branches = getOrgBranchByBranchCodeAndStatus(branchCode,statusList);
        if(ValidatorUtils.hasElements(branches)){
            return true;
        }
        return false;
    }

	@Override
	public List<Object[]> getApprovedAndActiveOrgBranches(String queryTerm) {
		NamedQueryExecutor<Object[]> executor = new NamedQueryExecutor<Object[]>("Organization.getApprovedAndActiveOrgBranches")
												.addParameter("searchTerm", queryTerm.toLowerCase().concat("%"))
												.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(executor, 0, 20);
	}
	@Override
	 public List<OrganizationBranch> getAllApprovedBranches()
	    {
	   	 
	 
			OrganizationType orgType = genericParameterService.findByCode(OrganizationType.ORGANIZATION_TYPE_BRANCH,
	            OrganizationType.class);

	  
			return getAllBranchesForType(orgType);
	    }
		
	
	@Override
	public boolean checkForDuplicateHeadOffice(OrganizationBranch organizationBranch){
		OrganizationType organizationTypeSelected= genericParameterService.findById(organizationBranch.getOrganizationType().getId(), OrganizationType.class);
		boolean multiHeadOfficeCreationAllowed = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), "config.multiHeadOfficeCreationAllowed.flag") != null ? 
    			Boolean.valueOf(configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), "config.multiHeadOfficeCreationAllowed.flag").getPropertyValue()) : false;
		return(!multiHeadOfficeCreationAllowed && null!=getHeadOffice() && OrganizationType.ORGANIZATION_TYPE_BRANCH_HO.equalsIgnoreCase(organizationTypeSelected!= null ? organizationTypeSelected.getCode():null));
			
	}
	@Override
    public String getScreenDescription(Long screenId)
    {
        NamedQueryExecutor<String> screenDef=new NamedQueryExecutor<String>("dynamicForm.getDescriptionOfPlaceHolderIdsMappedToSourceProductForAutoComplete").
                addParameter("value", screenId);

        return entityDao.executeQueryForSingleValue(screenDef);
    }

	
}
