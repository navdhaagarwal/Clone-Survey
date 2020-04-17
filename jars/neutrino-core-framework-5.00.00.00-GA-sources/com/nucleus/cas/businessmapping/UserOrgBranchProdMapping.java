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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.core.SelectiveMapping;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

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
@Table(name = "jt_UserOrgToProduct")
@Cacheable
@Synonym(grant="ALL")
public class UserOrgBranchProdMapping extends BaseMasterEntity implements SelectiveMapping{

    private static final long  serialVersionUID = 7759268158102450630L;
    
    @ManyToOne
    @JoinColumn(name = "user_org_branch_mapping", insertable=false, updatable=false)
    private UserOrgBranchMapping userOrgBranchMapping;
    
    @Column(name = "user_org_branch_mapping")
    private Long userOrgBranchMappingId;

    @Column(name = "loan_product")
    private Long  loanProductId;
    
    private String operationType;

	public UserOrgBranchMapping getUserOrgBranchMapping() {
		return userOrgBranchMapping;
	}

	public void setUserOrgBranchMapping(UserOrgBranchMapping userOrgBranchMapping) {
		this.userOrgBranchMapping = userOrgBranchMapping;
	}
	
	public Long getUserOrgBranchMappingId() {
		return userOrgBranchMappingId;
	}

	public void setUserOrgBranchMappingId(Long userOrgBranchMappingId) {
		this.userOrgBranchMappingId = userOrgBranchMappingId;
	}


	
	/**
	 * @return the loanProductId
	 */
	public Long getLoanProductId() {
		return loanProductId;
	}

	/**
	 * @param loanProductId the loanProductId to set
	 */
	public void setLoanProductId(Long loanProductId) {
		this.loanProductId = loanProductId;
	}

	@Override
	public String getOperationType() {
		return (operationType!=null? operationType:SelectiveMapping.ADDITION_OPERATION);
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	
	/* (non-Javadoc)
	 * @see com.nucleus.master.BaseMasterEntity#populate(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions)
	 */
	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	UserOrgBranchProdMapping userOrgBranchProdMapping = (UserOrgBranchProdMapping) baseEntity;
        super.populate(userOrgBranchProdMapping, cloneOptions);
        userOrgBranchProdMapping.setUserOrgBranchMappingId(userOrgBranchMappingId);
        userOrgBranchProdMapping.setLoanProductId(loanProductId);
        userOrgBranchProdMapping.setOperationType(operationType);
    }

    /* (non-Javadoc)
     * @see com.nucleus.master.BaseMasterEntity#populateFrom(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions)
     */
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	UserOrgBranchProdMapping userOrgBranchProdMapping = (UserOrgBranchProdMapping) baseEntity;
        super.populateFrom(userOrgBranchProdMapping, cloneOptions);
        this.setUserOrgBranchMappingId(userOrgBranchProdMapping.getUserOrgBranchMappingId());
        this.setLoanProductId(userOrgBranchProdMapping.getLoanProductId());
        this.setOperationType(userOrgBranchProdMapping.getOperationType());
    }
	
}