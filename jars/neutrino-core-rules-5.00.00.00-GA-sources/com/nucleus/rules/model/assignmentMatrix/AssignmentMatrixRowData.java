package com.nucleus.rules.model.assignmentMatrix;

import java.util.LinkedHashMap;

import javax.persistence.*;

import com.nucleus.rules.model.CriteriaRules;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import com.nucleus.rules.model.ScriptRule;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * 
 * @author Nucleus Software Exports Limited
 * class to store row data for 
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class AssignmentMatrixRowData extends BaseEntity {

    private static final long             serialVersionUID = 1L;

    // Json String - key value - Key will be name property of AssignmentFieldMetaData

    @Lob
    @EmbedInAuditAsValue(displayKey="label.if.condition.type")
    private String                        rowMapValues;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ScriptRule                    rule;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @EmbedInAuditAsValueObject(displayKey="label.then.action.table")
    private AssignmentMatrixAction        assignmentMatrixAction;

    @Lob
    @EmbedInAuditAsValue
    private String                        expression;

    @Transient
    private LinkedHashMap<Object, Object> linkedMap;

    @EmbedInAuditAsValue(displayKey="label.AssignmentMaster.AssignmentSet.priority")
    private Integer                       priority;

    /** The criteria rules. */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CriteriaRules criteriaRules;

    /** The allocation strategy. */
    @EmbedInAuditAsValue
    private String             allocationStrategy;

    @Transient
    private String                        ruleExpression;

    @Transient
    private String                        sourceProduct;

    @Transient
    private Boolean                       editedOrNewFlag = false;
    
    @Lob
    private String                        ruleExp;
    
    @EmbedInAuditAsValue
    private Boolean holdFlag;
    
    

    public Boolean getHoldFlag() {
		return holdFlag;
	}

	public void setHoldFlag(Boolean holdFlag) {
		this.holdFlag = holdFlag;
	}

	public String getRuleExp() {
        return ruleExp;
    }

    public void setRuleExp(String ruleExp) {
        this.ruleExp = ruleExp;
    }

    public LinkedHashMap<Object, Object> getLinkedMap() {
        return linkedMap;
    }

    public void setLinkedMap(LinkedHashMap<Object, Object> linkedMap) {
        this.linkedMap = linkedMap;
    }

    /**
     * @return the rowMapValues
     */
    public String getRowMapValues() {
        return rowMapValues;
    }

    /**
     * @param rowMapValues the rowMapValues to set
     */
    public void setRowMapValues(String rowMapValues) {
        this.rowMapValues = rowMapValues;
    }

    /**
     * @return the rule
     */
    public ScriptRule getRule() {
        return rule;
    }

    /**
     * @param rule the rule to set
     */
    public void setRule(ScriptRule rule) {
        this.rule = rule;
    }

    /**
     * @return the assignmentMatrixAction
     */
    public AssignmentMatrixAction getAssignmentMatrixAction() {
        return assignmentMatrixAction;
    }

    /**
     * @param assignmentMatrixAction the assignmentMatrixAction to set
     */
    public void setAssignmentMatrixAction(AssignmentMatrixAction assignmentMatrixAction) {
        this.assignmentMatrixAction = assignmentMatrixAction;
    }

    /**
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @param expression the expression to set
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * Getter for property 'criteriaRules'.
     *
     * @return Value for property 'criteriaRules'.
     */
    public CriteriaRules getCriteriaRules() {
        return criteriaRules;
    }

    /**
     * Setter for property 'criteriaRules'.
     *
     * @param criteriaRules Value to set for property 'criteriaRules'.
     */
    public void setCriteriaRules(CriteriaRules criteriaRules) {
        this.criteriaRules = criteriaRules;
    }

    /**
     * Getter for property 'allocationStrategy'.
     *
     * @return Value for property 'allocationStrategy'.
     */
    public String getAllocationStrategy() {
        return allocationStrategy;
    }

    /**
     * Setter for property 'allocationStrategy'.
     *
     * @param allocationStrategy Value to set for property 'allocationStrategy'.
     */
    public void setAllocationStrategy(String allocationStrategy) {
        this.allocationStrategy = allocationStrategy;
    }

    /**
     * Getter for property 'ruleExpression'.
     *
     * @return Value for property 'ruleExpression'.
     */
    public String getRuleExpression() {
        return ruleExpression;
    }

    /**
     * Setter for property 'ruleExpression'.
     *
     * @param ruleExpression Value to set for property 'ruleExpression'.
     */
    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }


    /**
     * Getter for property 'sourceProduct'.
     *
     * @return Value for property 'sourceProduct'.
     */
    public String getSourceProduct() {
        return sourceProduct;
    }

    /**
     * Setter for property 'sourceProduct'.
     *
     * @param sourceProduct Value to set for property 'sourceProduct'.
     */
    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public Boolean getEditedOrNewFlag() {
        return editedOrNewFlag;
    }

    public void setEditedOrNewFlag(Boolean editedOrNewFlag) {
        this.editedOrNewFlag = editedOrNewFlag;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentMatrixRowData assignmentMatrixRowData = (AssignmentMatrixRowData) baseEntity;
        super.populate(assignmentMatrixRowData, cloneOptions);

        if (null != rule) {
            assignmentMatrixRowData.setRule((ScriptRule) this.getRule().cloneYourself(cloneOptions));
        }
        if (null != assignmentMatrixAction) {
            assignmentMatrixRowData.setAssignmentMatrixAction((AssignmentMatrixAction) this.getAssignmentMatrixAction()
                    .cloneYourself(cloneOptions));
        }

        assignmentMatrixRowData.setRowMapValues(rowMapValues);
        assignmentMatrixRowData.setExpression(expression);
        assignmentMatrixRowData.setPriority(priority);
        assignmentMatrixRowData.setHoldFlag(holdFlag);
        assignmentMatrixRowData.setRuleExp(ruleExp);
        assignmentMatrixRowData.setAllocationStrategy(allocationStrategy);
        if (null != criteriaRules) {
            assignmentMatrixRowData.setCriteriaRules((CriteriaRules) this.getCriteriaRules().cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentMatrixRowData assignmentMatrixRowData = (AssignmentMatrixRowData) baseEntity;
        super.populateFrom(assignmentMatrixRowData, cloneOptions);
        this.setAssignmentMatrixAction(null != assignmentMatrixRowData.getAssignmentMatrixAction() ? (AssignmentMatrixAction) assignmentMatrixRowData
                .getAssignmentMatrixAction().cloneYourself(cloneOptions) : null);
        this.setRule(null != assignmentMatrixRowData.getRule() ? (ScriptRule) assignmentMatrixRowData.getRule()
                .cloneYourself(cloneOptions) : null);
        this.setRule(assignmentMatrixRowData.getRule());
        this.setRowMapValues(assignmentMatrixRowData.getRowMapValues());
        this.setExpression(assignmentMatrixRowData.getExpression());
        this.setPriority(assignmentMatrixRowData.getPriority());
        this.setHoldFlag(assignmentMatrixRowData.getHoldFlag());
        this.ruleExp=assignmentMatrixRowData.getRuleExp();
        this.setAllocationStrategy(assignmentMatrixRowData.getAllocationStrategy());
        if (assignmentMatrixRowData.getCriteriaRules() != null) {
            CriteriaRules criteriaRulesClone = (CriteriaRules) assignmentMatrixRowData.getCriteriaRules().cloneYourself(cloneOptions);
            this.setCriteriaRules(criteriaRulesClone);
        }else{
            this.setCriteriaRules(null);
        }

    }
}
