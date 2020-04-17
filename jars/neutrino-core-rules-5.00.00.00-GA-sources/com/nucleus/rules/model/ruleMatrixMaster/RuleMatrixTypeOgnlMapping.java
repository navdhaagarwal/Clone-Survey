package com.nucleus.rules.model.ruleMatrixMaster;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.ObjectGraphTypes;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class RuleMatrixTypeOgnlMapping extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    private RuleMatrixMasterType ruleMatrixMasterType;

    @ManyToOne
    private ObjectGraphTypes     objectGraphType;

    public RuleMatrixMasterType getRuleMatrixMasterType() {
        return ruleMatrixMasterType;
    }

    public void setRuleMatrixMasterType(RuleMatrixMasterType ruleMatrixMasterType) {
        this.ruleMatrixMasterType = ruleMatrixMasterType;
    }

    public ObjectGraphTypes getObjectGraphType() {
        return objectGraphType;
    }

    public void setObjectGraphType(ObjectGraphTypes objectGraphType) {
        this.objectGraphType = objectGraphType;
    }

}
