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
package com.nucleus.businessmapping.entity;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.SelectiveMapping;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.user.User;

/**
 * This entity maintains the mappings between following entities,
 * 1. User 2. Loan Product 3. Organization.
 * This entity would be used to map access/privileges assigned to a user on specific products. 
 * @author Nucleus Software Exports Limited.
 * 
 */
@Entity
@DynamicUpdate
@DynamicInsert
//@Table(uniqueConstraints = @UniqueConstraint(name="UserPrimaryBranchUniqueConstraint", columnNames = { "associated_user", "organization_branch", "is_primary_branch" }))
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="user_index",columnList="associated_user"),@Index(name="org_branch_index",columnList="organization_branch")})
public class UserOrgBranchMapping extends BaseMasterEntity implements SelectiveMapping {

    private static final long  serialVersionUID = 7759268158102450630L;

    @ManyToOne
    @JoinColumn(name="associated_user")
    private User               associatedUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="organization_branch", insertable=false, updatable=false)
    private OrganizationBranch organizationBranch;
    
    @Column(name="organization_branch")
    private Long organizationBranchId;

    @Column(name="is_primary_branch")
    private boolean            isPrimaryBranch;

    private boolean            includesSubBranches;

    private Boolean            isBranchAdmin;
    
    private String operationType;

    public User getAssociatedUser() {
        return associatedUser;
    }

    public void setAssociatedUser(User associatedUser) {
        this.associatedUser = associatedUser;
    }

    public OrganizationBranch getOrganizationBranch() {
        return organizationBranch;
    }

    public void setOrganizationBranch(OrganizationBranch organizationBranch) {
        this.organizationBranch = organizationBranch;
    }

    public boolean isPrimaryBranch() {
        return isPrimaryBranch;
    }

    public void setPrimaryBranch(boolean isPrimaryBranch) {
        this.isPrimaryBranch = isPrimaryBranch;
    }

    public boolean isIncludesSubBranches() {
        return includesSubBranches;
    }

    public void setIncludesSubBranches(boolean includesSubBranches) {
        this.includesSubBranches = includesSubBranches;
    }

    public UserOrgBranchMapping(OrganizationBranch organizationBranch, boolean includesSubBranches) {
        super();
        this.organizationBranch = organizationBranch;
        this.organizationBranchId=organizationBranch.getId();
        this.includesSubBranches = includesSubBranches;
    }
    public UserOrgBranchMapping(OrganizationBranch organizationBranch, boolean includesSubBranches,long id, String operationType) {
        super();
        this.organizationBranch = organizationBranch;
        this.includesSubBranches = includesSubBranches;
        this.organizationBranchId=organizationBranch.getId();
        this.setId(id);
        this.setOperationType(operationType);
    }

    public UserOrgBranchMapping() {
        super();
    }

    public Boolean isBranchAdmin() {
        return isBranchAdmin == null ? Boolean.FALSE : isBranchAdmin;
    }

    public void setBranchAdmin(Boolean isBranchAdmin) {
        this.isBranchAdmin = isBranchAdmin;
    }

	@Override
	public String getOperationType() {
		
		return (operationType!=null? operationType:SelectiveMapping.ADDITION_OPERATION);
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public Long getOrganizationBranchId() {
		return organizationBranchId;
	}

	public void setOrganizationBranchId(Long organizationBranchId) {
		this.organizationBranchId = organizationBranchId;
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.nucleus.master.BaseMasterEntity#populate(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions)
	 */
	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	UserOrgBranchMapping userOrgBranchMapping = (UserOrgBranchMapping) baseEntity;
        super.populate(userOrgBranchMapping, cloneOptions);
        userOrgBranchMapping.setOrganizationBranchId(organizationBranchId);
        userOrgBranchMapping.setPrimaryBranch(isPrimaryBranch);
        userOrgBranchMapping.setBranchAdmin(isBranchAdmin);
        userOrgBranchMapping.setIncludesSubBranches(includesSubBranches);
        userOrgBranchMapping.setOperationType(operationType);
    }

    /* (non-Javadoc)
     * @see com.nucleus.master.BaseMasterEntity#populateFrom(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions)
     */
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	UserOrgBranchMapping userOrgBranchMapping = (UserOrgBranchMapping) baseEntity;
        super.populateFrom(userOrgBranchMapping, cloneOptions);
        this.setOrganizationBranchId(userOrgBranchMapping.getOrganizationBranchId());
        this.setPrimaryBranch(userOrgBranchMapping.isPrimaryBranch);
        this.setBranchAdmin(userOrgBranchMapping.isBranchAdmin());
        this.setIncludesSubBranches(userOrgBranchMapping.isIncludesSubBranches());
        this.setOperationType(userOrgBranchMapping.getOperationType());
    }
	
	

}