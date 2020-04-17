package com.nucleus.core.organization.entity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Indexed;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
//@Indexed
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="org_branch_fk_index",columnList="org_branch_fk")})
public class ParentBranchMapping extends BaseEntity {

    /**
     * 
     */
    private static final long  serialVersionUID = -4527057709565299133L;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrganizationBranch parentBranch;
    @ManyToOne(fetch = FetchType.LAZY)
    private SystemName         moduleName;

    public OrganizationBranch getParentBranch() {
        return parentBranch;
    }

    public void setParentBranch(OrganizationBranch parentBranch) {
        this.parentBranch = parentBranch;
    }

    public SystemName getModuleName() {
        return moduleName;
    }

    public void setModuleName(SystemName moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ParentBranchMapping pbm = (ParentBranchMapping) baseEntity;
        super.populate(pbm, cloneOptions);
        pbm.setParentBranch(parentBranch);
        pbm.setModuleName(moduleName);
    }
}
