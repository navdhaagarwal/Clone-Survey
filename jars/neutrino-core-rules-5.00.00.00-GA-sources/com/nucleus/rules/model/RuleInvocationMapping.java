package com.nucleus.rules.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Rule Invocation Mapping class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Table(name = "rule_inv_map")
@Synonym(grant="ALL")
@DeletionPreValidator
public class RuleInvocationMapping extends BaseMasterEntity {

    private static final long          serialVersionUID = 1L;

    @Sortable
    private String                     invocationPoint;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name="RULE_INV_MAP_RULE_MAPPING", joinColumns= {@JoinColumn(name="RULE_INV_MAP", referencedColumnName = "ID")},
   	inverseJoinColumns = {@JoinColumn(name="RULE_MAPPING", referencedColumnName = "ID")})
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<RuleActionMapping>    ruleMapping;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name="RULE_INV_MAP_RULESET_MAPPING", joinColumns= {@JoinColumn(name="RULE_INV_MAP", referencedColumnName = "ID")},
   	inverseJoinColumns = {@JoinColumn(name="RULESET_MAPPING", referencedColumnName = "ID")})
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<RuleSetActionMapping> rulesetMapping;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RuleGroup                  ruleGroup;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CriteriaRules              criteriaRules;

    /**
     * @return the criteriaRules
     */
    public CriteriaRules getCriteriaRules() {
        return criteriaRules;
    }

    /**
     * @param criteriaRules the criteriaRules to set
     */
    public void setCriteriaRules(CriteriaRules criteriaRules) {
        this.criteriaRules = criteriaRules;
    }

    /** 
     * @return ruleGroup
     */
    public RuleGroup getRuleGroup() {
        return ruleGroup;
    }

    /**
     * @param ruleGroup
     */
    public void setRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    /** 
     * @return invocationPoint
     */
    public String getInvocationPoint() {
        return invocationPoint;
    }

    /**
     * 
     * @param invocationPoint
     */
    public void setInvocationPoint(String invocationPoint) {
        this.invocationPoint = invocationPoint;
    }

    /** 
     * @return RuleMapping
     */
    public List<RuleActionMapping> getRuleMapping() {
        return ruleMapping;
    }

    /**
     * 
     * @param ruleMapping
     */
    public void setRuleMapping(List<RuleActionMapping> ruleMapping) {
        this.ruleMapping = ruleMapping;
    }

    /** 
     * @return RuleSetMapping
     */
    public List<RuleSetActionMapping> getRulesetMapping() {
        return rulesetMapping;
    }

    /**
     * 
     * @param rulesetMapping
     */
    public void setRulesetMapping(List<RuleSetActionMapping> rulesetMapping) {
        this.rulesetMapping = rulesetMapping;
    }

    /**
     * @param baseEntity and CloneOption
     */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleInvocationMapping invocationMapping = (RuleInvocationMapping) baseEntity;
        super.populate(invocationMapping, cloneOptions);
        invocationMapping.setInvocationPoint(invocationPoint);

        if (null != ruleGroup) {
            invocationMapping.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }

        if (null != ruleMapping && ruleMapping.size() > 0) {
            List<RuleActionMapping> ruleMappingClone = new ArrayList<RuleActionMapping>();
            for (RuleActionMapping ruleMappingIterator : ruleMapping) {
                ruleMappingClone.add((RuleActionMapping) ruleMappingIterator.cloneYourself(cloneOptions));
            }
            invocationMapping.setRuleMapping(ruleMappingClone);
        }

        if (null != rulesetMapping && rulesetMapping.size() > 0) {
            List<RuleSetActionMapping> ruleSetMappingClone = new ArrayList<RuleSetActionMapping>();
            for (RuleSetActionMapping ruleSetMappingIterator : rulesetMapping) {
                ruleSetMappingClone.add((RuleSetActionMapping) ruleSetMappingIterator.cloneYourself(cloneOptions));
            }
            invocationMapping.setRulesetMapping(ruleSetMappingClone);
        }

        if (null != criteriaRules) {
            invocationMapping.setCriteriaRules((CriteriaRules) this.getCriteriaRules().cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleInvocationMapping invocationMapping = (RuleInvocationMapping) baseEntity;
        super.populateFrom(invocationMapping, cloneOptions);
        this.setInvocationPoint(invocationMapping.getInvocationPoint());

        this.setRuleGroup(null);

        if (null != invocationMapping.getRuleGroup()) {
            this.setRuleGroup((RuleGroup) invocationMapping.getRuleGroup().cloneYourself(cloneOptions));
        }

        if (this.getRuleMapping() != null) {
            this.getRuleMapping().clear();
        }

        if (null != invocationMapping.getRuleMapping() && invocationMapping.getRuleMapping().size() > 0) {

            for (RuleActionMapping ruleMappingIterator : invocationMapping.getRuleMapping()) {
                this.getRuleMapping().add((RuleActionMapping) ruleMappingIterator.cloneYourself(cloneOptions));
            }
        }

        if (null != this.getRulesetMapping()) {
            this.getRulesetMapping().clear();
        }

        if (null != invocationMapping.getRulesetMapping() && invocationMapping.getRulesetMapping().size() > 0) {

            for (RuleSetActionMapping ruleSetMappingIterator : invocationMapping.getRulesetMapping()) {
                this.getRulesetMapping().add((RuleSetActionMapping) ruleSetMappingIterator.cloneYourself(cloneOptions));
            }
        }
        if (null != invocationMapping.getCriteriaRules()) {
            this.setCriteriaRules((CriteriaRules) invocationMapping.getCriteriaRules().cloneYourself(cloneOptions));
        }
    }

    @Override
    public String getDisplayName() {
        return getInvocationPoint();
    }

}
