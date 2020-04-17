package com.nucleus.rules.model;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

/**
 * 
 * @author Nucleus Software Exports Limited
 * RuleSetPatternActionMapping class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "rs_pattern_action_map")
@Cacheable
@Synonym(grant="ALL")
public class RuleSetPatternActionMapping extends BaseMasterEntity {

    private static final long serialVersionUID = 1242167058961287482L;

    private String            pattern;

    @Transient
    private List<String>      rulePattern;

    @ManyToOne
    private RuleAction        ruleAction;

    /**
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @param pattern the pattern to set
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @return the ruleAction
     */
    public RuleAction getRuleAction() {
        return ruleAction;
    }

    /**
     * @param ruleAction the ruleAction to set
     */
    public void setRuleAction(RuleAction ruleAction) {
        this.ruleAction = ruleAction;
    }

    /**
     * @param rule pattern list to get
     */
    public List<String> getRulePattern() {
        return rulePattern;
    }

    /**
     * @param rule pattern list to set
     */

    public void setRulePattern(List<String> rulePattern) {
        this.rulePattern = rulePattern;
    }

    /**this is from cloning the object
     * @param baseEntity and cloneOption
     */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleSetPatternActionMapping ruleSetPatternActionMapping = (RuleSetPatternActionMapping) baseEntity;
        super.populate(ruleSetPatternActionMapping, cloneOptions);
        ruleSetPatternActionMapping.setRuleAction(ruleAction);
        ruleSetPatternActionMapping.setPattern(pattern);
    }
}
