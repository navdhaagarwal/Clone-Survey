package com.nucleus.rules.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
 * Represents a Rule in the system.
 * @author Nucleus Software Exports Limited
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@DeletionPreValidator
@Table(name = "re_rule")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class Rule extends BaseMasterEntity {
    // ~ Static fields/initializers =================================================================

    private static final long    serialVersionUID = 1;

    // ~ Instance fields ============================================================================

    private String               code;

    @Sortable
    private String               name;

    private String               description;

    @Transient
    private List<RuleExpression> expression;

    private String               sourceProduct;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private RuntimeRuleMapping   runtimeRuleMapping;

    private String               errorMessage;

    private String               errorMessageKey;

    private String               successMessage;

    private String               successMessageKey;

    private boolean              criteriaRuleFlag;

    @ElementCollection
    @JoinTable(name = "rule_tagged_name", joinColumns = @JoinColumn(name = "re_rule", referencedColumnName = "id"))
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<String>         ruleTagNames;

    private Integer              ruleType;

    @Lob
    private String               ruleExpression;

    @ManyToOne
    private ModuleName           moduleName;

    /**
     * @return the moduleName
     */
    public ModuleName getModuleName() {
        return moduleName;
    }

    /**
     * @param moduleName the moduleName to set
     */
    public void setModuleName(ModuleName moduleName) {
        this.moduleName = moduleName;
    }

    public String getRuleExpression() {
        return ruleExpression;
    }

    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    /**
     * @return the ruleTagNames
     */
    public List<String> getRuleTagNames() {
        return ruleTagNames;
    }

    /**
     * @param ruleTagNames the ruleTagNames to set
     */
    public void setRuleTagNames(List<String> ruleTagNames) {
        this.ruleTagNames = ruleTagNames;
    }

    /**
     * @return the criteriaRuleFlag
     */
    public boolean isCriteriaRuleFlag() {
        return criteriaRuleFlag;
    }

    /**
     * @param criteriaRuleFlag the criteriaRuleFlag to set
     */
    public void setCriteriaRuleFlag(boolean criteriaRuleFlag) {
        this.criteriaRuleFlag = criteriaRuleFlag;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the errorMessageKey
     */
    public String getErrorMessageKey() {
        return errorMessageKey;
    }

    /**
     * @param errorMessageKey the errorMessageKey to set
     */
    public void setErrorMessageKey(String errorMessageKey) {
        this.errorMessageKey = errorMessageKey;
    }

    /**
     * @return the successMessage
     */
    public String getSuccessMessage() {
        return successMessage;
    }

    /**
     * @param successMessage the successMessage to set
     */
    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    /**
     * @return the successMessageKey
     */
    public String getSuccessMessageKey() {
        return successMessageKey;
    }

    /**
     * @param successMessageKey the successMessageKey to set
     */
    public void setSuccessMessageKey(String successMessageKey) {
        this.successMessageKey = successMessageKey;
    }

    /**
     * 
     * Getter for sourceProduct
     * @return
     */
    public String getSourceProduct() {
        return sourceProduct;
    }

    /**
     * 
     * Setter for sourceProduct Property
     * @param sourceProduct
     */
    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return expression
     */

    public RuleExpression getExpression() {
        if (expression == null || expression.size() == 0) {
            return null;
        }
        return expression.get(0);
    }

    /**
     * set expression 
     * @param expression
     */

    public void setExpression(RuleExpression expression) {
        if (this.expression == null || this.expression.size() == 0) {
            this.expression = new ArrayList<RuleExpression>();
        } else {
            this.expression.remove(0);
        }
        this.expression.add(expression);
    }

    /**
     * @param name
     * @param expression
     */

    public Rule(String name, RuleExpression expression) {
        super();
        this.name = name;
        this.expression = new ArrayList<RuleExpression>();
        this.expression.add(expression);
    }

    /**
     * Default Constructor
     */

    public Rule() {

    }

    /**
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * set description
     * @param description
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the runtimeRuleMapping
     */
    public RuntimeRuleMapping getRuntimeRuleMapping() {
        return runtimeRuleMapping;
    }

    /**
     * @param runtimeRuleMapping the runtimeRuleMapping to set
     */
    public void setRuntimeRuleMapping(RuntimeRuleMapping runtimeRuleMapping) {
        this.runtimeRuleMapping = runtimeRuleMapping;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    /**
     * delete expression
     */

    public void deleteExpression() {
        if (expression == null || expression.size() <= 0) {
            return;
        }
    }

    /**
     * @return the ruleType
     */
    public Integer getRuleType() {
        return ruleType;
    }

    /**
     * @param ruleType the ruleType to set
     */
    public void setRuleType(Integer ruleType) {
        this.ruleType = ruleType;
    }

    /**
     * 
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * 
     * set code
     * @param code
     */

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Rule rule = (Rule) baseEntity;
        super.populate(rule, cloneOptions);
        rule.setName(name);
        rule.setCode(code);
        rule.setDescription(description);
        rule.setSourceProduct(sourceProduct);
        rule.setErrorMessage(errorMessage);
        rule.setErrorMessageKey(errorMessageKey);
        rule.setSuccessMessage(successMessage);
        rule.setSuccessMessageKey(successMessageKey);
        rule.setRuleExpression(ruleExpression);
        if (ruleTagNames != null && ruleTagNames.size() > 0) {
            rule.setRuleTagNames(new ArrayList<String>(ruleTagNames));
        }
        rule.setRuntimeRuleMapping(getRuntimeRuleMapping() != null ? (RuntimeRuleMapping) getRuntimeRuleMapping()
                .cloneYourself(cloneOptions) : null);
        rule.setRuleType(ruleType);
        rule.setCriteriaRuleFlag(criteriaRuleFlag);
        rule.setModuleName(moduleName);
        rule.setEntityLifeCycleData(this.getEntityLifeCycleData());
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Rule rule = (Rule) baseEntity;
        super.populateFrom(rule, cloneOptions);
        this.setName(rule.getName());
        this.setCode(rule.getCode());
        this.setDescription(rule.getDescription());
        this.setSourceProduct(rule.getSourceProduct());
        this.setErrorMessage(rule.getErrorMessage());
        this.setErrorMessageKey(rule.getErrorMessageKey());
        this.setSuccessMessage(rule.getSuccessMessage());
        this.setSuccessMessageKey(rule.getSuccessMessageKey());
        this.setRuleExpression(rule.getRuleExpression());
        if (rule.getRuleTagNames() != null && rule.getRuleTagNames().size() > 0) {
            this.setRuleTagNames(new ArrayList<String>(rule.getRuleTagNames()));
        }
        this.setRuntimeRuleMapping(rule.getRuntimeRuleMapping() != null ? (RuntimeRuleMapping) rule.getRuntimeRuleMapping()
                .cloneYourself(cloneOptions) : null);
        this.setRuleType(rule.getRuleType());
        this.setCriteriaRuleFlag(rule.isCriteriaRuleFlag());
        this.setRuleExpression(rule.getRuleExpression());
        this.setModuleName(rule.getModuleName());

        this.setEntityLifeCycleData(rule.getEntityLifeCycleData());
    }
}