package com.nucleus.rules.model.eventDefinition;

import javax.persistence.*;


import com.nucleus.eventInterfaceCode.EventInterfaceCode;



import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.event.EventTask;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.service.RuleBasedEventTask;

import java.util.List;

@Entity
@DynamicInsert
@DynamicUpdate
@Cacheable
public class InterfaceTask extends EventTask implements RuleBasedEventTask{

    private static final long  serialVersionUID = 1L;



    @OneToOne
    private EventInterfaceCode interfaceCode;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RuleGroup ruleGroup;



    public EventInterfaceCode getInterfaceCode() {
        return interfaceCode;
    }

    public void setInterfaceCode(EventInterfaceCode interfaceCode) {
        this.interfaceCode = interfaceCode;
    }

    public RuleGroup getRuleGroup() {
        return ruleGroup;
    }

    public void setRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        InterfaceTask interfaceTask = (InterfaceTask) baseEntity;
        super.populate(interfaceTask, cloneOptions);
        interfaceTask.setInterfaceCode(interfaceCode);

        if (null != ruleGroup) {
            interfaceTask.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }


    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        InterfaceTask interfaceTask = (InterfaceTask) baseEntity;
        super.populateFrom(interfaceTask, cloneOptions);
        this.setInterfaceCode(interfaceTask.getInterfaceCode());

        if (interfaceTask.getRuleGroup() != null) {
            this.setRuleGroup((RuleGroup) interfaceTask.getRuleGroup().cloneYourself(cloneOptions));

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
