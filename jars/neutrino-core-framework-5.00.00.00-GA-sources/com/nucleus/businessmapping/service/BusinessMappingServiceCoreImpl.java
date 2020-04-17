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
package com.nucleus.businessmapping.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import com.nucleus.businessmapping.entity.BPOrgBranchMapping;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.service.BaseServiceImpl;

/**
 * @author Nucleus Software Exports Limited
 */
@Named("businessMappingServiceCore")
@MonitoredWithSpring(name = "BusinessMappingServiceCore")
public class BusinessMappingServiceCoreImpl extends BaseServiceImpl implements BusinessMappingServiceCore {

    @Inject
    @Named("organizationService")
    private OrganizationService organizationService;

    @Override
    public List<OrganizationBranch> getBusinessPartnerOrgBranches(Long bpId) {
        NamedQueryExecutor<BPOrgBranchMapping> getBranch = new NamedQueryExecutor<BPOrgBranchMapping>(
                "BusinessPartner.getBPOrgBranches").addParameter("bpId", bpId);
        List<OrganizationBranch> bpBranchesList = new ArrayList<OrganizationBranch>();
        List<BPOrgBranchMapping> bpOrgBranchMappingList = entityDao.executeQuery(getBranch);
        for (BPOrgBranchMapping x : bpOrgBranchMappingList) {
            if ((x.getMasterLifeCycleData().getApprovalStatus() == ApprovalStatus.APPROVED)) {
                if (x.isIncludesSubBranches()) {
                    bpBranchesList.add(x.getOrganizationBranch());
                    List<OrganizationBranch> organizationBranchList = organizationService.getAllChildBranches(x
                            .getOrganizationBranch().getId(), SystemName.SOURCE_PRODUCT_TYPE_CAS);
                    bpBranchesList.addAll(organizationBranchList);

                } else {
                    bpBranchesList.add(x.getOrganizationBranch());
                }

            }

        }
        bpBranchesList.sort(Comparator.comparing(OrganizationBranch::getName));

        return bpBranchesList;
    }


}
