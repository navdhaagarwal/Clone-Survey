package com.nucleus.businessmapping.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.nucleus.cas.delegation.service.DelegationLoginService;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.organization.cache.OrganizationBranchCacheService;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

@Named("userInfoOrgBranchMappingService")
public class UserInfoOrgBranchMappingService {

	@Inject
	@Named("userManagementServiceCore")
	private UserManagementServiceCore userManagementService;

	@Inject
	@Named("businessMappingServiceCore")
	private BusinessMappingServiceCore businessMappingService;

	@Inject
	@Named("organizationBranchCacheService")
	public OrganizationBranchCacheService organizationBranchCacheService;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("organizationService")
	private OrganizationService organizationService;

	@Inject
	@Named("userService")
	private UserService userService;

	public void updateOrgBranchInfo(UserInfo userInfo) {
		OrganizationBranch organizationBranch = null;
		if (userInfo.getAccessToAllBranches() != null
				&& "Y".equalsIgnoreCase(userInfo.getAccessToAllBranches().toString())) {

			userInfo.setUserBranchList(getAccessToAllBranches());
			userInfo.setApprovedAndActiveUserBranchList(getAccessToAllApprovedAndActiveBranches());
			if (!userInfo.isBusinessPartner()) {

				organizationBranch = userManagementService.getUserPrimaryOrganizationBranch(userInfo.getId());

			}

		}

		// Check if the loggedIn user is not BusinessPartner
		else if (!userInfo.isBusinessPartner()) {

			organizationBranch = userManagementService.getUserPrimaryOrganizationBranch(userInfo.getId());
			String sysName = ProductInformationLoader.getProductName();
			if (userInfo.getSysName() != null && userInfo.getSysName().getId() != null) {
				sysName = userInfo.getSysName().getCode();
			}
			userInfo.setUserBranchList(getUserOrgBranches(userInfo, sysName));
			userInfo.setApprovedAndActiveUserBranchList(getUserOrgApprovedAndActiveBranches(userInfo, sysName));

		}

		// set the logged in branch of the user
		if (organizationBranch != null) {
			OrgBranchInfo orgBranchInfo = new OrgBranchInfo();
			orgBranchInfo.setId(organizationBranch.getId());
			orgBranchInfo.setOrgName(organizationBranch.getName());
			userInfo.setPrimaryOrgBranchInfo(orgBranchInfo);

			userInfo.setLoggedInBranch(orgBranchInfo);
		}

	}

	private List<OrgBranchInfo> getUserOrgBranches(UserInfo userInfo, String systemName) {
		List<OrganizationBranch> orgBranchList = userManagementService.getUserOrgBranches(userInfo.getId(), systemName);
		DelegationLoginService delegationLoginService = null;
		try {
			delegationLoginService = NeutrinoSpringAppContextUtil.getBeanByName("delegationService",
					DelegationLoginService.class);
		} catch (NoSuchBeanDefinitionException e) {
			BaseLoggers.exceptionLogger
					.error("No implementation is available for interface DelegationLoginService, moving ahead.");
		}
		if (delegationLoginService != null) {
			delegationLoginService.getOrganizationBranchesDelegatedToUser(userInfo, orgBranchList);
		}
		List<OrgBranchInfo> orgBranchInfoList = new ArrayList<>();
		OrgBranchInfo orgBranchInfo;

		for (OrganizationBranch orgBranch : orgBranchList) {
			orgBranchInfo = new OrgBranchInfo();
			orgBranchInfo.setId(orgBranch.getId());
			orgBranchInfo.setOrgName(orgBranch.getName());
			orgBranchInfoList.add(orgBranchInfo);
		}

		return orgBranchInfoList;
	}

	private List<OrgBranchInfo> getUserOrgApprovedAndActiveBranches(UserInfo userInfo, String systemName) {
		List<OrganizationBranch> orgBranchList = userManagementService.getUserOrgBranches(userInfo.getId(), systemName);
		DelegationLoginService delegationLoginService = null;
		try {
			delegationLoginService = NeutrinoSpringAppContextUtil.getBeanByName("delegationService",
					DelegationLoginService.class);
		} catch (NoSuchBeanDefinitionException e) {
			BaseLoggers.exceptionLogger
					.error("No implementation is available for interface DelegationLoginService, moving ahead.");
		}
		if (delegationLoginService != null) {
			delegationLoginService.getOrganizationBranchesDelegatedToUser(userInfo, orgBranchList);
		}
		List<OrgBranchInfo> updatedOrgBranchInfoList = getUpdatedOrgBranchInfoList(orgBranchList);

		updatedOrgBranchInfoList.sort(Comparator.comparing(OrgBranchInfo::getOrgName));
		return updatedOrgBranchInfoList;
	}

	private List<OrgBranchInfo> getBusinessPartnerUserOrgBranches(Long bpId) {
		List<OrganizationBranch> orgBranchList = businessMappingService.getBusinessPartnerOrgBranches(bpId);
		List<OrgBranchInfo> orgBranchInfoList = new ArrayList<>();
		OrgBranchInfo orgBranchInfo;

		for (OrganizationBranch orgBranch : orgBranchList) {
			orgBranchInfo = new OrgBranchInfo();
			orgBranchInfo.setId(orgBranch.getId());
			orgBranchInfo.setOrgName(orgBranch.getName());
			orgBranchInfoList.add(orgBranchInfo);
		}

		return orgBranchInfoList;
	}

	private List<OrganizationBranch> getAllApprovedBranches() {
		return organizationBranchCacheService.getAllApprovedBranchesFromCache();

	}

	private List<OrgBranchInfo> getAccessToAllBranches() {

		List<OrganizationBranch> orgBranchList = getAllApprovedBranches();
		List<OrgBranchInfo> orgBranchInfoList = new ArrayList<>();
		OrgBranchInfo orgBranchInfo;

		for (OrganizationBranch orgBranch : orgBranchList) {
			orgBranchInfo = new OrgBranchInfo();
			orgBranchInfo.setId(orgBranch.getId());
			orgBranchInfo.setOrgName(orgBranch.getName());
			orgBranchInfoList.add(orgBranchInfo);
		}

		return orgBranchInfoList;
	}

	/**
	 * @return
	 */
	private List<OrgBranchInfo> getAccessToAllApprovedAndActiveBranches() {

		List<OrganizationBranch> orgBranchList = getAllApprovedBranches();
		return getUpdatedOrgBranchInfoList(orgBranchList);

	}

	/**
	 * @param organizationBranchs
	 * @return
	 */
	private List<OrgBranchInfo> getUpdatedOrgBranchInfoList(List<OrganizationBranch> organizationBranchs) {

		List<OrgBranchInfo> orgBranchInfoList = new ArrayList<>();
		OrgBranchInfo orgBranchInfo;

		List<Integer> statusList = new ArrayList<>();
		statusList.add(ApprovalStatus.APPROVED);
		statusList.add(ApprovalStatus.APPROVED_MODIFIED);
		statusList.add(ApprovalStatus.APPROVED_DELETED);
		statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

		for (OrganizationBranch organizationBranch : organizationBranchs) {

			if (statusList.contains(organizationBranch.getApprovalStatus()) && organizationBranch.isActiveFlag()) {
				orgBranchInfo = new OrgBranchInfo();
				orgBranchInfo.setId(organizationBranch.getId());
				orgBranchInfo.setOrgName(organizationBranch.getName());
				orgBranchInfoList.add(orgBranchInfo);
			}
		}

		return orgBranchInfoList;
	}

}
