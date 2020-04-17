package com.nucleus.rules.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
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
 * Represents a Condition in the system.
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Table(name = "Conditions")
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class Condition extends BaseMasterEntity {
    // ~ Static fields/initializers =================================================================

    private static final long         serialVersionUID = 1;

    // ~ Instance fields ============================================================================

    private String                    code;

    private String                    name;

    private String                    description;

    @Transient
    private List<ConditionExpression> expression;

    private String                    sourceProduct;

    private boolean                   criteriaConditionFlag;

    @Lob
    private String                    conditionExpression;

    @ManyToOne
    private ModuleName                moduleName;

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

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    /**
     * @return the criteriaConditionFlag
     */
    public boolean isCriteriaConditionFlag() {
        return criteriaConditionFlag;
    }

    /**
     * @param criteriaConditionFlag the criteriaConditionFlag to set
     */
    public void setCriteriaConditionFlag(boolean criteriaConditionFlag) {
        this.criteriaConditionFlag = criteriaConditionFlag;
    }

    /**
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

    /**
     * 
     * getter for Description
     * @return
     */

    public String getDescription() {
        return description;
    }

    /**
     * 
     * Setter for description
     * @param description
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * getter for name
     * @return
     */

    public String getName() {
        return name;
    }

    /**
     * 
     * setter for name
     * @param name
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * get expression 
     * @return
     */

    public ConditionExpression getExpression() {
        if (expression == null || expression.size() == 0) {
            return null;
        }
        return expression.get(0);
    }

    /**
     * 
     * setter for expression
     * @param expression
     */

    public void setExpression(ConditionExpression expression) {
        this.expression = new ArrayList<ConditionExpression>();
        this.expression.add(expression);
    }

    /**
     * @param name
     * @param expression
     */

    public Condition(String name, ConditionExpression expression) {
        this.name = name;
        this.expression = new ArrayList<ConditionExpression>();
        this.expression.add(expression);
    }

    /**
     * 
     * default constructor
     */

    public Condition() {

    }

    /**
     * getter for code
     * @return
     */

    public String getCode() {
        return code;
    }

    /**
     * 
     * Setter for code
     * @param code
     */

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Condition condition = (Condition) baseEntity;
        super.populate(condition, cloneOptions);
        condition.setName(name);
        condition.setCode(code);
        condition.setDescription(description);
        condition.setSourceProduct(sourceProduct);
        condition.setConditionExpression(conditionExpression);
        condition.setModuleName(moduleName);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Condition condition = (Condition) baseEntity;
        super.populateFrom(condition, cloneOptions);
        this.setName(condition.getName());
        this.setCode(condition.getCode());
        this.setDescription(condition.getDescription());
        this.setSourceProduct(condition.getSourceProduct());
        this.setConditionExpression(condition.getConditionExpression());
        this.setModuleName(condition.getModuleName());
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

}