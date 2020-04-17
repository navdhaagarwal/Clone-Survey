package com.nucleus.rules.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

/**
 * 
 * @author Nucleus Software Exports Limited
 * RuleSetActionMapping class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class RuleSetActionMapping extends BaseMasterEntity {

    private static final long                 serialVersionUID = 1L;

    @ManyToOne
    private RuleSet                           ruleSet;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "rs_action_map_fk")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<RuleSetPatternActionMapping> patternMappings;

    /**
     * @return the patternMappings
     */
    public List<RuleSetPatternActionMapping> getPatternMappings() {
        return patternMappings;
    }

    /**
     * @param patternMappings the patternMappings to set
     */
    public void setPatternMappings(List<RuleSetPatternActionMapping> patternMappings) {
        this.patternMappings = patternMappings;
    }

    /**
     * @return the ruleAction
     */
    public RuleSet getRuleSet() {
        return ruleSet;
    }

    /**
     * @param ruleAction the ruleAction to set
     */
    public void setRuleSet(RuleSet ruleAction) {
        this.ruleSet = ruleAction;
    }

    /**
     * @param cloning entity
     */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleSetActionMapping ruleSetActionMapping = (RuleSetActionMapping) baseEntity;
        super.populate(ruleSetActionMapping, cloneOptions);
        ruleSetActionMapping.setRuleSet(ruleSet);
        if (patternMappings != null && patternMappings.size() > 0) {
            List<RuleSetPatternActionMapping> ruleSetPatternActionMappingClone = new ArrayList<RuleSetPatternActionMapping>();
            for (RuleSetPatternActionMapping ruleSetPatternActionMappingItr : patternMappings) {
                ruleSetPatternActionMappingClone.add((RuleSetPatternActionMapping) ruleSetPatternActionMappingItr
                        .cloneYourself(cloneOptions));
            }
            ruleSetActionMapping.setPatternMappings(ruleSetPatternActionMappingClone);
        }
    }
}
