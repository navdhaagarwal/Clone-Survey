package com.nucleus.rules.model;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Class to create Rule Group
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="ALL")
public class CriteriaRules extends BaseMasterEntity {

    private static final long serialVersionUID = 3032950161687619591L;

    @ManyToOne
    @EmbedInAuditAsReference
    private EntityType        entityType;

    @EmbedInAuditAsValue
    private String            sourceProduct;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "criteriarulegroup")
    private RuleGroup         ruleGroup;

    @EmbedInAuditAsValue
    private String            name;

    /**
     * 
     * Getter for name property
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * Setter for name property
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the ruleGroup
     */
    public RuleGroup getRuleGroup() {
        return ruleGroup;
    }

    /**
     * @param ruleGroup the ruleGroup to set
     */
    public void setRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    /**
     * 
     * Getter for sourceProduct property
     * @return
     */
    public String getSourceProduct() {
        return sourceProduct;
    }

    /**
     * 
     * Setter for sourceProduct property
     * @param sourceProduct
     */
    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    /**
     * 
     * Getter for entityType property
     * @return
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * 
     * Setter for entityType property
     * @param entityType
     */
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public CriteriaRules() {
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        CriteriaRules criteriaRule = (CriteriaRules) baseEntity;
        super.populate(criteriaRule, cloneOptions);
        criteriaRule.setEntityType(entityType);
        criteriaRule.setSourceProduct(sourceProduct);
        if (null != ruleGroup) {
            criteriaRule.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }
        criteriaRule.setName(name);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        CriteriaRules criteriaRule = (CriteriaRules) baseEntity;
        super.populateFrom(criteriaRule, cloneOptions);
        this.setEntityType(criteriaRule.getEntityType());
        this.setSourceProduct(criteriaRule.getSourceProduct());
        if (null != criteriaRule.getRuleGroup()) {
            this.setRuleGroup((RuleGroup) criteriaRule.getRuleGroup().cloneYourself(cloneOptions));
        }
        this.setName(criteriaRule.getName());
    }
}
