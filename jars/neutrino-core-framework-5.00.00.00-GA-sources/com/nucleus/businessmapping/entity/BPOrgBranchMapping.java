package com.nucleus.businessmapping.entity;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.master.BaseMasterEntity;

/**
 * This entity maintains the mappings between following entities,
 * 1. Business Partner 2. Loan Product 3. Organization.
 * This entity would be used to map access/privileges assigned to a Business Partner on specific products. 
 * @author Nucleus Software Exports Limited.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class BPOrgBranchMapping extends BaseMasterEntity {

    private static final long  serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.EAGER)
    private OrganizationBranch organizationBranch;

    @Column(name="ASSOCIATED_BUSINESS_PARTNER")
    private Long associatedBusinessPartnerId;
    
    private boolean            isPrimaryBranch;

    private boolean            includesSubBranches;

    
    public Long getAssociatedBusinessPartnerId() {
		return associatedBusinessPartnerId;
	}

	public void setAssociatedBusinessPartnerId(Long associatedBusinessPartnerId) {
		this.associatedBusinessPartnerId = associatedBusinessPartnerId;
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

}
