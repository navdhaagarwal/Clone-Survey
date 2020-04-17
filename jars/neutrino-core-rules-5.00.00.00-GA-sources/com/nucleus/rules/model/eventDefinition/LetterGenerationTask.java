package com.nucleus.rules.model.eventDefinition;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.event.EventTask;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.letterMaster.LetterType;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.service.RuleBasedEventTask;

@Entity
@DynamicInsert
@DynamicUpdate
@Cacheable
public class LetterGenerationTask extends EventTask implements RuleBasedEventTask{

    private static final long  serialVersionUID = 1L;

    @OneToOne
    private LetterType letterType;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RuleGroup ruleGroup;

    public LetterType getLetterType() {
        return letterType;
    }

    public void setLetterType(LetterType letterType) {
        this.letterType = letterType;
    }

    public RuleGroup getRuleGroup() {
        return ruleGroup;
    }

    public void setRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        LetterGenerationTask letterGenerationTask = (LetterGenerationTask) baseEntity;
        super.populate(letterGenerationTask, cloneOptions);
        letterGenerationTask.setLetterType(letterType);
        if (null != ruleGroup) {
            letterGenerationTask.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }


    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        LetterGenerationTask letterGenerationTask = (LetterGenerationTask) baseEntity;
        super.populateFrom(letterGenerationTask, cloneOptions);
        this.setLetterType(letterGenerationTask.getLetterType());
        if (letterGenerationTask.getRuleGroup() != null) {
            this.setRuleGroup((RuleGroup) letterGenerationTask.getRuleGroup().cloneYourself(cloneOptions));

        }

    }

	@Override
	public RuleGroup fetchRuleGroup() {		
		return this.getRuleGroup();
	}

	@Override
	public void setRuleGroupInEventTask(RuleGroup ruleGroup) {
		this.setRuleGroup(ruleGroup);
		
	}

	@Override
	public Boolean isEventRuleBased() {
		return this.getIsRuleBased();
	}
}
