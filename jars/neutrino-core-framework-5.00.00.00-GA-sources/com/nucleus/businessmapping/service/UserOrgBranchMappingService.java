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

import java.util.List;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.entity.EntityId;
import com.nucleus.user.User;

/**
 * @author Nucleus Software Exports Limited
 * 
 * This is a business service which provides common functionality to create {@link UserOrgBranchMapping}.
 */
public interface UserOrgBranchMappingService {
    
    
    /**
     * Generate the mode basic {@link OrganizationBranch}.
     * 
     * @param organizationBranchEntityId - EntityId of the {@link OrganizationBranch}
     * @param user - The user which should be mapped to the provided {@link OrganizationBranch}
     * 
     * @return the generated {@link UserOrgBranchMapping}
     * */
    public UserOrgBranchMapping generateUserOrgBranchMapping(EntityId organizationBranchEntityId, User user);
    
    /**
     * All the user(s) marked as superAdmin are made branch admins the branch.
     * 
     * When a new {@link OrganizationBranch} is approved through maker checker flow
     * then all the super admins should be made branch admins of the newly approved branch.
     * 
     * @param organizationBranchEntityId - EntityId of the newly approved {@link OrganizationBranch}
     * 
     * @return the mapped {@link UserOrgBranchMapping}(s)
     * */
    public List<UserOrgBranchMapping> mapSuperAdminToNewlyCreatedBranch(EntityId organizationBranchEntityId);

}