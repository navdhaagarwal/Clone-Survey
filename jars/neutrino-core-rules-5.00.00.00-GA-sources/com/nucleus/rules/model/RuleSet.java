package com.nucleus.rules.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;

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
 * Rule Set class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
@DeletionPreValidator
public class RuleSet extends BaseMasterEntity {

    private static final long serialVersionUID = 1L;

    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "rule_set_rule", inverseJoinColumns = @JoinColumn(name = "re_rule"))
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @OrderBy
    private List<Rule>        rules;

    @Sortable(index = 1)
    private String            name;

    private String            description;

    private String            sourceProduct;

    @Transient
    private long[]            ruleId;

    /**
     * 
     * Getter to set the Rules Id
     * @return
     */
    public long[] getRuleId() {
        return ruleId;
    }

    /**
     * 
     * Setter to set the Rules Id
     * @param ruleId
     */
    public void setRuleId(long[] ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * 
     * Getter for description property
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * 
     * Setter for description property
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

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

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> ruleList) {
        this.rules = ruleList;
    }

    public void addRules(Rule rule) {
        if (rules == null) {
            rules = new ArrayList<Rule>();
        }
        if (rules.contains(rule)) {
            return;
        }
        rules.add(rule);
    }

    public void removeRules(Rule rule) {
        if (rules == null) {
            rules = new ArrayList<Rule>();
        }
        if (rules.contains(rule)) {
            rules.remove(rule);
        }
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleSet ruleSet = (RuleSet) baseEntity;
        super.populate(ruleSet, cloneOptions);
        ruleSet.setName(name);
        ruleSet.setDescription(description);

        if (this.getRules() != null && this.getRules().size() > 0) {
            for (Iterator<Rule> iterator = rules.iterator() ; iterator.hasNext() ;) {
                Rule rule = iterator.next();
                ruleSet.addRules(rule);
            }
        }

        ruleSet.setSourceProduct(sourceProduct);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleSet ruleSet = (RuleSet) baseEntity;
        super.populateFrom(ruleSet, cloneOptions);
        this.setName(ruleSet.getName());
        this.setDescription(ruleSet.getDescription());
        if (getRules() != null) {
            getRules().clear();
        }
        if (ruleSet.getRules() != null && ruleSet.getRules().size() > 0) {
            for (Iterator<Rule> iterator = ruleSet.getRules().iterator() ; iterator.hasNext() ;) {
                Rule cloneRule = iterator.next();
                addRules(cloneRule);
            }
        }

        this.setSourceProduct(ruleSet.getSourceProduct());
    }
}
