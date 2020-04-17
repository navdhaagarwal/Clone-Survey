package com.nucleus.rules.model.assignmentMatrix;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.nucleus.rules.model.PurposeType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import com.nucleus.rules.model.ModuleName;

/**
 * 
 * @author Nucleus Software Exports Limited Master class for base assignment
 *         master
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class BaseAssignmentMaster extends BaseMasterEntity {

    private static final long serialVersionUID = 1L;

    @Sortable
    @EmbedInAuditAsValue
    private String            name;

    @EmbedInAuditAsValue
    private String            code;

    @EmbedInAuditAsValue
    private String            description;

    @EmbedInAuditAsReference
    @OneToOne(fetch = FetchType.LAZY)
    private ModuleName        moduleName;

    @ManyToOne(fetch = FetchType.LAZY)
    private PurposeType purpose;

    public PurposeType getPurpose() {
        return purpose;
    }

    public void setPurpose(PurposeType purpose) {
        this.purpose = purpose;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fk_assignment_master")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @NotFound(action=NotFoundAction.IGNORE)
    @EmbedInAuditAsValueObject
    List<AssignmentSet>       assignmentSet;

    private String            sourceProduct;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the display name
     */
    public String getDisplayName() {
        return getName();
    }
    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the moduleName
     */
    public ModuleName getModuleName() {
        return moduleName;
    }

    /**
     * @param moduleName
     *            the moduleName to set
     */
    public void setModuleName(ModuleName moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * @return the assignmentSet
     */
    public List<AssignmentSet> getAssignmentSet() {
        return assignmentSet;
    }

    /**
     * @param assignmentSet
     *            the assignmentSet to set
     */
    public void setAssignmentSet(List<AssignmentSet> assignmentSet) {
        this.assignmentSet = assignmentSet;
    }

    public String getSourceProduct() {
		return sourceProduct;
	}

	public void setSourceProduct(String sourceProduct) {
		this.sourceProduct = sourceProduct;
	}
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        BaseAssignmentMaster baseAssignmentMatrix = (BaseAssignmentMaster) baseEntity;
        super.populate(baseAssignmentMatrix, cloneOptions);

        baseAssignmentMatrix.setCode(code);
        baseAssignmentMatrix.setName(name);
        baseAssignmentMatrix.setDescription(description);
        baseAssignmentMatrix.setModuleName(moduleName);
        baseAssignmentMatrix.setSourceProduct(sourceProduct);
        baseAssignmentMatrix.setPurpose(purpose);
        if (assignmentSet != null && assignmentSet.size() > 0) {
            baseAssignmentMatrix.setAssignmentSet(new ArrayList<AssignmentSet>());
            for (AssignmentSet assignment : assignmentSet) {
                baseAssignmentMatrix.getAssignmentSet().add((AssignmentSet) assignment.cloneYourself(cloneOptions));
            }
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        BaseAssignmentMaster baseAssignmentMatrix = (BaseAssignmentMaster) baseEntity;
        super.populateFrom(baseAssignmentMatrix, cloneOptions);

        this.setName(baseAssignmentMatrix.getName());
        this.setDescription(baseAssignmentMatrix.getDescription());
        this.setCode(baseAssignmentMatrix.getCode());
        this.setModuleName(baseAssignmentMatrix.getModuleName());
        this.setSourceProduct(baseAssignmentMatrix.getSourceProduct());
        this.setPurpose(baseAssignmentMatrix.getPurpose());
        getAssignmentSet().clear();
        if (baseAssignmentMatrix.getAssignmentSet() != null && baseAssignmentMatrix.getAssignmentSet().size() > 0) {

            for (AssignmentSet assignment : baseAssignmentMatrix.getAssignmentSet()) {
                getAssignmentSet().add((AssignmentSet) assignment.cloneYourself(cloneOptions));
            }
        }

    }
}