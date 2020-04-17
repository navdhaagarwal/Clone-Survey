package com.nucleus.cas.businessmapping;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "jt_UserProdToScheme")
@Cacheable
@Synonym(grant="ALL")
public class UserOrgBranchProdSchemeMapping extends BaseMasterEntity {

	
	private static final long serialVersionUID = 1L;

	private Long  userId;
	
	private Long  productId;
	
	private Long  schemeId;	
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getSchemeId() {
		return schemeId;
	}

	public void setSchemeId(Long schemeId) {
		this.schemeId = schemeId;
	}
	
	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		UserOrgBranchProdSchemeMapping userOrgBranchProdSchemeMapping = (UserOrgBranchProdSchemeMapping) baseEntity;
        super.populate(userOrgBranchProdSchemeMapping, cloneOptions);
        userOrgBranchProdSchemeMapping.setSchemeId(schemeId);
        userOrgBranchProdSchemeMapping.setProductId(productId);
        userOrgBranchProdSchemeMapping.setUserId(userId);
    }

    /* (non-Javadoc)
     * @see com.nucleus.master.BaseMasterEntity#populateFrom(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions)
     */
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	UserOrgBranchProdSchemeMapping userOrgBranchProdSchemeMapping = (UserOrgBranchProdSchemeMapping) baseEntity;
        super.populateFrom(userOrgBranchProdSchemeMapping, cloneOptions);
        this.setSchemeId(userOrgBranchProdSchemeMapping.getSchemeId());
        this.setProductId(userOrgBranchProdSchemeMapping.getProductId());
        this.setUserId(userOrgBranchProdSchemeMapping.getUserId());
   }
	
}
