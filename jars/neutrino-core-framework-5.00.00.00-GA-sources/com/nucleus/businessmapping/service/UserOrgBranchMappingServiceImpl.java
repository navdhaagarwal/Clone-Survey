/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.businessmapping.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.User;
import com.nucleus.user.UserService;

/**
 * @author Nucleus Software Exports Limited
 * 
 * This is the implementation of {@link UserOrgBranchMappingService}.
 */
@Named("userOrgBranchMappingService")
public class UserOrgBranchMappingServiceImpl implements UserOrgBranchMappingService {
    
    @Inject
    @Named("entityDao")
    private EntityDao                   entityDao;

    @Inject
    @Named(value = "userService")
    UserService                 userService;

    @Override
    public UserOrgBranchMapping generateUserOrgBranchMapping(EntityId organizationBranchEntityId, User user) {
        UserOrgBranchMapping userOrgBranchMapping = new UserOrgBranchMapping();
        userOrgBranchMapping.setAssociatedUser(user);
        userOrgBranchMapping.setOrganizationBranch((OrganizationBranch) entityDao.get(organizationBranchEntityId));
        userOrgBranchMapping.setOrganizationBranchId(organizationBranchEntityId.getLocalId());
        userOrgBranchMapping.setBranchAdmin(true);
        userOrgBranchMapping.setActiveFlag(true);
        userOrgBranchMapping.setApprovalStatus(ApprovalStatus.APPROVED);
        entityDao.persist(userOrgBranchMapping);
        return userOrgBranchMapping; 
    }

    @Override
    public List<UserOrgBranchMapping> mapSuperAdminToNewlyCreatedBranch(EntityId organizationBranchEntityId) {
        List<UserOrgBranchMapping> userOrgBranchMappings = new ArrayList<UserOrgBranchMapping>();
                
        List<User> superAdmins = userService.getAllSuperAdmin();
        if (CollectionUtils.isNotEmpty(superAdmins)) {
            for (User superAdmin : superAdmins) {
                userOrgBranchMappings.add(generateUserOrgBranchMapping(organizationBranchEntityId, superAdmin));
            }
        }
        
        return userOrgBranchMappings;
    }

}
